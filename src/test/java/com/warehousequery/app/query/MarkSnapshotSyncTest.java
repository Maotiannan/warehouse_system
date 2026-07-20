package com.warehousequery.app.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarkSnapshotSyncTest {
    @TempDir
    Path tempDir;

    @Test
    void updatesOnlyTheRowWithTheMatchingStableKey() throws Exception {
        QuerySnapshotStore store = new QuerySnapshotStore(tempDir.resolve("query-snapshot.json"));
        store.replace(QuerySnapshotStoreTest.fixtureSnapshot("OLD MARK"));

        boolean updated = store.updateMark("JOB-001", "NEW MARK");

        QuerySnapshot snapshot = store.load().orElseThrow();
        assertEquals(true, updated);
        assertEquals("NEW MARK", snapshot.rows().get(0).getMt());
        assertEquals("raw response segment 1\nraw response segment 2", snapshot.responseLog());
    }

    @Test
    void missingStableKeyDoesNotRewriteSnapshot() throws Exception {
        QuerySnapshotStore store = new QuerySnapshotStore(tempDir.resolve("query-snapshot.json"));
        store.replace(QuerySnapshotStoreTest.fixtureSnapshot("OLD MARK"));

        boolean updated = store.updateMark("UNKNOWN", "NEW MARK");

        assertEquals(false, updated);
        assertEquals("OLD MARK", store.load().orElseThrow().rows().get(0).getMt());
    }
}
