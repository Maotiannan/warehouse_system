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

        QueryBatchResult result = new QueryBatchService(client).execute(
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

        QueryBatchResult result = new QueryBatchService(client).execute(
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

        QueryBatchResult result = new QueryBatchService(client).execute(
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
