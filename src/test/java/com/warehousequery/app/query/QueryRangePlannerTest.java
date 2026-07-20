package com.warehousequery.app.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

class QueryRangePlannerTest {
    @Test
    void plansTwoNonOverlappingInclusiveSegmentsFromToday() {
        List<QuerySegment> segments = QueryRangePlanner.plan(
            QueryMode.ONE_YEAR,
            LocalDate.of(2026, 7, 20));

        assertEquals(2, segments.size());
        assertEquals(LocalDate.of(2026, 1, 22), segments.get(0).start());
        assertEquals(LocalDate.of(2026, 7, 20), segments.get(0).end());
        assertEquals(LocalDate.of(2025, 7, 26), segments.get(1).start());
        assertEquals(LocalDate.of(2026, 1, 21), segments.get(1).end());
        assertEquals(179, ChronoUnit.DAYS.between(
            segments.get(0).start(),
            segments.get(0).end()));
        assertEquals(segments.get(0).start().minusDays(1), segments.get(1).end());
    }

    @Test
    void plansFourSegmentsAcrossLeapDayWithoutOverlap() {
        List<QuerySegment> segments = QueryRangePlanner.plan(
            QueryMode.TWO_YEARS,
            LocalDate.of(2024, 2, 29));

        assertEquals(4, segments.size());
        for (int i = 1; i < segments.size(); i++) {
            assertEquals(segments.get(i - 1).start().minusDays(1), segments.get(i).end());
        }
        assertTrue(segments.stream().allMatch(segment ->
            ChronoUnit.DAYS.between(segment.start(), segment.end()) == 179));
    }

    @Test
    void normalModeUsesOneSegmentAndNullEndDateIsRejected() {
        assertEquals(1, QueryRangePlanner.plan(
            QueryMode.NORMAL,
            LocalDate.of(2026, 7, 20)).size());
        assertThrows(NullPointerException.class,
            () -> QueryRangePlanner.plan(QueryMode.NORMAL, null));
    }
}
