package com.warehousequery.app.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ControllerStateContractTest {
    @Test
    void modeStateMapsToMutuallyExclusiveDateRanges() {
        QueryUiState state = QueryUiState.forMode(
            QueryMode.ONE_YEAR,
            LocalDate.of(2026, 7, 20));

        assertEquals(LocalDate.of(2025, 7, 26), state.start());
        assertEquals(LocalDate.of(2026, 7, 20), state.end());
        assertTrue(state.oneYearSelected());
        assertFalse(state.twoYearsSelected());
        assertTrue(state.datePickersDisabled());

        QueryUiState normal = QueryUiState.forMode(
            QueryMode.NORMAL,
            LocalDate.of(2026, 7, 20));
        assertFalse(normal.oneYearSelected());
        assertFalse(normal.twoYearsSelected());
        assertFalse(normal.datePickersDisabled());
        assertEquals(LocalDate.of(2026, 1, 22), normal.start());
    }

    @Test
    void startupRestoreClearsFiltersAndDoesNotRequestNetworkData() {
        StartupRestorePlan plan = StartupRestorePlan.from(
            QuerySnapshotStoreTest.fixtureSnapshot("MARK-01"),
            LocalDate.of(2026, 7, 20));

        assertTrue(plan.rows().size() > 0);
        assertEquals(QueryMode.ONE_YEAR, plan.mode());
        assertEquals(LocalDate.of(2025, 7, 26), plan.start());
        assertEquals(LocalDate.of(2026, 7, 20), plan.end());
        assertEquals(Map.of(), plan.filters());
        assertFalse(plan.shouldQueryNetwork());
    }
}
