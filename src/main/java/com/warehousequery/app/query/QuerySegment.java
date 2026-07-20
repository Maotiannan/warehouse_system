package com.warehousequery.app.query;

import java.time.LocalDate;
import java.util.Objects;

public final class QuerySegment {
    private final int ordinal;
    private final int totalSegments;
    private final LocalDate start;
    private final LocalDate end;

    public QuerySegment(int ordinal, int totalSegments, LocalDate start, LocalDate end) {
        this.ordinal = ordinal;
        this.totalSegments = totalSegments;
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
    }

    public int ordinal() {
        return this.ordinal;
    }

    public int totalSegments() {
        return this.totalSegments;
    }

    public LocalDate start() {
        return this.start;
    }

    public LocalDate end() {
        return this.end;
    }
}
