package com.warehousequery.app.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehousequery.app.model.WarehouseEntry;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class QueryBatchServiceTest {
    @Test
    void executesEachIdNewestSegmentFirstAndReportsEveryAttempt() throws Exception {
        FakeClient client = new FakeClient();
        List<QueryProgress> progress = new ArrayList<QueryProgress>();

        QueryBatchResult result = new QueryBatchService(client, 0L, 0, 0L).execute(
            new QueryBatchRequest(
                List.of(" A ", "", "B"),
                1,
                QueryMode.ONE_YEAR,
                LocalDate.of(2026, 7, 20)),
            progress::add).get();

        assertEquals(List.of("A/1", "A/2", "B/1", "B/2"), client.calls);
        assertEquals(List.of("A/1", "A/2", "B/1", "B/2"), progress.stream()
            .map(QueryProgress::identity)
            .collect(Collectors.toList()));
        assertEquals(4, progress.size());
        assertTrue(progress.stream().allMatch(QueryProgress::success));
        assertTrue(result.complete());
        assertEquals(4, result.rows().size());
        assertTrue(result.requestLog().contains("batch=" + result.batchId()));
        assertTrue(result.requestLog().contains("segment=1/2"));
        assertTrue(result.responseLog().contains("response A/2"));
    }

    @Test
    void continuesAfterOneSegmentFailureAndMarksBatchIncomplete() throws Exception {
        FakeClient client = new FakeClient(Set.of("A/2"));
        List<QueryProgress> progress = new ArrayList<QueryProgress>();

        QueryBatchResult result = new QueryBatchService(client, 0L, 0, 0L).execute(
            requestFor("A", QueryMode.ONE_YEAR),
            progress::add).get();

        assertEquals(List.of("A/1", "A/2"), client.calls);
        assertEquals(1, result.rows().size());
        assertFalse(result.complete());
        assertEquals("A/2", result.failures().get(0).identity());
        assertEquals(2, progress.size());
        assertFalse(progress.get(1).success());
        assertTrue(result.responseLog().contains("failure=A/2"));
    }

    @Test
    void preservesDuplicateRowsAndTreatsEmptySuccessfulResponsesAsComplete() throws Exception {
        FakeClient client = new FakeClient();
        client.emptyIdentities.add("A/1");

        QueryBatchResult result = new QueryBatchService(client, 0L, 0, 0L).execute(
            new QueryBatchRequest(
                List.of("A", "A"),
                1,
                QueryMode.NORMAL,
                LocalDate.of(2026, 7, 20)),
            progress -> { }).get();

        assertEquals(List.of("A/1", "A/1"), client.calls);
        assertTrue(result.complete());
        assertEquals(0, result.rows().size());
        assertTrue(result.failures().isEmpty());
    }

    @Test
    void deduplicatesIdenticalRowsReturnedByMultipleSegments() throws Exception {
        SingleQueryClient client = (jcbh, status, segment, logs) -> {
            logs.request(jcbh, segment, "request " + jcbh + "/" + segment.ordinal());
            WarehouseEntry row = new WarehouseEntry();
            row.setJcbh(jcbh);
            row.setZyh("SHARED-JOB-001");
            row.setJcid("SHARED-CID-001");
            logs.response(jcbh, segment, "response " + jcbh + "/" + segment.ordinal());
            return CompletableFuture.completedFuture(List.of(row));
        };

        QueryBatchResult result = new QueryBatchService(client, 0L, 0, 0L).execute(
            new QueryBatchRequest(
                List.of("A"),
                2,
                QueryMode.TWO_YEARS,
                LocalDate.of(2026, 8, 26)),
            progress -> { }).get();

        assertTrue(result.complete());
        assertEquals(1, result.rows().size(),
            "多个分段返回相同记录时应去重，只保留一条");
        assertEquals("SHARED-JOB-001", result.rows().get(0).getZyh());
    }

    @Test
    void retriesAfterRateLimitAndEventuallySucceeds() throws Exception {
        int[] calls = {0};
        SingleQueryClient client = (jcbh, status, segment, logs) -> {
            calls[0]++;
            logs.request(jcbh, segment, "request " + calls[0]);
            if (calls[0] <= 2) {
                CompletableFuture<List<WarehouseEntry>> future = new CompletableFuture<List<WarehouseEntry>>();
                future.completeExceptionally(new java.io.IOException(
                    "API错误(429): 429 请求过于频繁，请稍后再试"));
                return future;
            }
            logs.response(jcbh, segment, "HTTP 200");
            WarehouseEntry row = new WarehouseEntry();
            row.setJcbh(jcbh);
            return CompletableFuture.completedFuture(List.of(row));
        };

        QueryBatchResult result = new QueryBatchService(client, 0L, 3, 1L).execute(
            requestFor("A", QueryMode.NORMAL),
            progress -> { }).get();

        assertEquals(3, calls[0], "前两次被限流，应在第三次成功，共调用 3 次");
        assertTrue(result.complete());
        assertEquals(1, result.rows().size());
        assertTrue(result.responseLog().contains("自动重试"),
            "响应日志应记录自动重试过程");
    }

    @Test
    void doesNotRetryNonRetryableErrors() throws Exception {
        int[] calls = {0};
        SingleQueryClient client = (jcbh, status, segment, logs) -> {
            calls[0]++;
            CompletableFuture<List<WarehouseEntry>> future = new CompletableFuture<List<WarehouseEntry>>();
            future.completeExceptionally(new IllegalStateException("API错误(1): 进仓编号不存在"));
            return future;
        };

        QueryBatchResult result = new QueryBatchService(client, 0L, 3, 1L).execute(
            requestFor("A", QueryMode.NORMAL),
            progress -> { }).get();

        assertEquals(1, calls[0], "参数类、确定性错误不应重试");
        assertFalse(result.complete());
        assertEquals(1, result.failures().size());
    }

    @Test
    void marksFailureAfterRetriesExhausted() throws Exception {
        int[] calls = {0};
        SingleQueryClient client = (jcbh, status, segment, logs) -> {
            calls[0]++;
            CompletableFuture<List<WarehouseEntry>> future = new CompletableFuture<List<WarehouseEntry>>();
            future.completeExceptionally(new java.io.IOException(
                "API错误(429): 429 请求过于频繁，请稍后再试"));
            return future;
        };

        QueryBatchResult result = new QueryBatchService(client, 0L, 2, 1L).execute(
            requestFor("A", QueryMode.NORMAL),
            progress -> { }).get();

        assertEquals(3, calls[0], "首次请求加两次重试，共 3 次");
        assertFalse(result.complete());
        assertEquals(1, result.failures().size());
    }

    @Test
    void classifiesRetryableErrors() {
        assertTrue(QueryBatchService.isRetryable(
            new java.io.IOException("API错误(429): 429 请求过于频繁，请稍后再试")));
        assertTrue(QueryBatchService.isRetryable(
            new java.net.SocketTimeoutException("Read timed out")));
        assertTrue(QueryBatchService.isRetryable(
            new java.io.IOException("HTTP请求失败，状态码: 503")));
        assertFalse(QueryBatchService.isRetryable(
            new IllegalStateException("API错误(1): 进仓编号不存在")));
        assertFalse(QueryBatchService.isRetryable(
            new java.io.IOException("服务器返回了无效的JSON格式")));
    }

    private QueryBatchRequest requestFor(String id, QueryMode mode) {
        return new QueryBatchRequest(
            List.of(id),
            1,
            mode,
            LocalDate.of(2026, 7, 20));
    }

    private static final class FakeClient implements SingleQueryClient {
        private final List<String> calls = new ArrayList<String>();
        private final Set<String> failures;
        private final Set<String> emptyIdentities = new HashSet<String>();

        private FakeClient() {
            this(Set.of());
        }

        private FakeClient(Set<String> failures) {
            this.failures = new HashSet<String>(failures);
        }

        @Override
        public CompletableFuture<List<WarehouseEntry>> query(
            String jcbh,
            int status,
            QuerySegment segment,
            QueryLogSink logs) {
            String identity = jcbh + "/" + segment.ordinal();
            this.calls.add(identity);
            logs.request(jcbh, segment, "request " + identity);
            if (this.failures.contains(identity)) {
                return failed(new IllegalStateException("failure " + identity));
            }
            if (this.emptyIdentities.contains(identity)) {
                logs.response(jcbh, segment, "response " + identity);
                return CompletableFuture.completedFuture(List.of());
            }
            WarehouseEntry row = new WarehouseEntry();
            row.setJcbh(jcbh);
            row.setZyh(identity);
            logs.response(jcbh, segment, "response " + identity);
            return CompletableFuture.completedFuture(List.of(row));
        }

        private static <T> CompletableFuture<T> failed(Throwable error) {
            CompletableFuture<T> future = new CompletableFuture<T>();
            future.completeExceptionally(error);
            return future;
        }
    }
}
