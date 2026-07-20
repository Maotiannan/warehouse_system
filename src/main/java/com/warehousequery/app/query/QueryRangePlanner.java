package com.warehousequery.app.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QueryRangePlanner {
    private static final long DAYS_PER_SEGMENT = 180L;

    private QueryRangePlanner() {
    }

    public static List<QuerySegment> plan(QueryMode mode, LocalDate endDate) {
        QueryMode requiredMode = Objects.requireNonNull(mode, "mode");
        LocalDate requiredEndDate = Objects.requireNonNull(endDate, "endDate");
        List<QuerySegment> segments = new ArrayList<QuerySegment>(requiredMode.segmentCount());

        for (int index = 0; index < requiredMode.segmentCount(); index++) {
            LocalDate segmentEnd = requiredEndDate.minusDays(DAYS_PER_SEGMENT * index);
            LocalDate segmentStart = segmentEnd.minusDays(DAYS_PER_SEGMENT - 1L);
            segments.add(new QuerySegment(
                index + 1,
                requiredMode.segmentCount(),
                segmentStart,
                segmentEnd));
        }

        return Collections.unmodifiableList(segments);
    }
}
