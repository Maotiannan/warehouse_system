package com.warehousequery.app.query;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehousequery.app.model.WarehouseEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QueryBatchSnapshotSmokeTest {
    @TempDir
    Path tempDir;

    @Test
    void partialBatchContinuesButDoesNotReplaceLastCompleteSnapshot() throws Exception {
        FakeClient client = new FakeClient();
        QueryBatchService service = new QueryBatchService(
            client,
            QueryLogSink::inMemory);
        QuerySnapshotStore store = new QuerySnapshotStore(
            tempDir.resolve("query-snapshot.json"));
        QueryBatchRequest request = new QueryBatchRequest(
            List.of("A", "B"),
            1,
            QueryMode.ONE_YEAR,
            LocalDate.of(2026, 7, 20));

        QueryBatchResult complete = service.execute(request, progress -> { }).get();
        assertTrue(complete.complete());
        store.replace(snapshotOf(complete));
        byte[] previous = Files.readAllBytes(store.path());

        client.failures.add("B/2");
        QueryBatchResult partial = service.execute(request, progress -> { }).get();

        assertFalse(partial.complete());
        assertTrue(client.calls.containsAll(List.of("A/1", "A/2", "B/1", "B/2")));
        assertArrayEquals(previous, Files.readAllBytes(store.path()));
    }

    private QuerySnapshot snapshotOf(QueryBatchResult result) {
        return new QuerySnapshot(
            QuerySnapshot.SCHEMA_VERSION,
            Instant.parse("2026-07-20T09:00:00Z"),
            result.request().mode(),
            result.request().entryNumbers(),
            result.request().statusIndex(),
            result.request().endDate(),
            result.segments(),
            result.rows(),
            result.requestLog(),
            result.responseLog());
    }

    private static final class FakeClient implements SingleQueryClient {
        private final List<String> calls = new ArrayList<String>();
        private final Set<String> failures = new HashSet<String>();

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
                CompletableFuture<List<WarehouseEntry>> future = new CompletableFuture<List<WarehouseEntry>>();
                future.completeExceptionally(new IllegalStateException(identity));
                return future;
            }
            WarehouseEntry row = new WarehouseEntry();
            row.setJcbh(jcbh);
            row.setZyh(identity);
            logs.response(jcbh, segment, "response " + identity);
            return CompletableFuture.completedFuture(List.of(row));
        }
    }
}
