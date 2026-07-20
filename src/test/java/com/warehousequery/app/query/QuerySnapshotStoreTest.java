package com.warehousequery.app.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehousequery.app.model.WarehouseEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuerySnapshotStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsRowsMetadataAndRawLogs() throws Exception {
        QuerySnapshotStore store = new QuerySnapshotStore(tempDir.resolve("query-snapshot.json"));
        QuerySnapshot expected = fixtureSnapshot("MARK-01");

        store.replace(expected);
        QuerySnapshot actual = store.load().orElseThrow();

        assertEquals(QueryMode.ONE_YEAR, actual.mode());
        assertEquals(List.of("L26MH001"), actual.entryNumbers());
        assertEquals(2, actual.segments().size());
        assertEquals("JOB-001", actual.rows().get(0).getZyh());
        assertEquals("MARK-01", actual.rows().get(0).getMt());
        assertEquals("raw request segment 1\nraw request segment 2", actual.requestLog());
        assertEquals("raw response segment 1\nraw response segment 2", actual.responseLog());
        assertTrue(Files.readString(store.path()).contains("raw response segment 2"));
    }

    @Test
    void malformedOrUnknownVersionLoadsAsEmptyWithoutThrowing() throws Exception {
        QuerySnapshotStore store = new QuerySnapshotStore(tempDir.resolve("query-snapshot.json"));
        Files.writeString(store.path(), "{not-json");
        assertTrue(store.load().isEmpty());

        Files.writeString(store.path(), "{\"schemaVersion\":999}");
        assertTrue(store.load().isEmpty());
    }

    @Test
    void failedReplacementLeavesPreviousSnapshotUnchanged() throws Exception {
        Path snapshotPath = tempDir.resolve("query-snapshot.json");
        QuerySnapshotStore store = new QuerySnapshotStore(snapshotPath);
        store.replace(fixtureSnapshot("OLD MARK"));

        Path blockedParent = tempDir.resolve("not-a-directory");
        Files.writeString(blockedParent, "blocks temporary file creation");
        QuerySnapshotStore failingStore = new QuerySnapshotStore(
            snapshotPath,
            ignored -> blockedParent.resolve("query-snapshot.tmp"));

        assertThrows(
            java.io.IOException.class,
            () -> failingStore.replace(fixtureSnapshot("NEW MARK")));
        assertEquals("OLD MARK", store.load().orElseThrow().rows().get(0).getMt());
    }

    static QuerySnapshot fixtureSnapshot(String mark) {
        WarehouseEntry entry = new WarehouseEntry();
        entry.setJcbh("L26MH001");
        entry.setJczyh("ENTRY-JOB-001");
        entry.setZyh("JOB-001");
        entry.setMt(mark);
        entry.setHz("货主甲");
        entry.setJs(10);
        entry.setTj(2.5);

        return new QuerySnapshot(
            QuerySnapshot.SCHEMA_VERSION,
            Instant.parse("2026-07-20T09:00:00Z"),
            QueryMode.ONE_YEAR,
            List.of("L26MH001"),
            1,
            LocalDate.of(2026, 7, 20),
            QueryRangePlanner.plan(QueryMode.ONE_YEAR, LocalDate.of(2026, 7, 20)),
            List.of(entry),
            "raw request segment 1\nraw request segment 2",
            "raw response segment 1\nraw response segment 2");
    }
}
