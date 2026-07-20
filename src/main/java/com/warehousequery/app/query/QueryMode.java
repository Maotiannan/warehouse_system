package com.warehousequery.app.query;

public enum QueryMode {
    NORMAL(1),
    ONE_YEAR(2),
    TWO_YEARS(4);

    private static final long DAYS_PER_SEGMENT = 180L;
    private final int segmentCount;

    QueryMode(int segmentCount) {
        this.segmentCount = segmentCount;
    }

    public int segmentCount() {
        return this.segmentCount;
    }

    public long totalDays() {
        return DAYS_PER_SEGMENT * this.segmentCount;
    }
}
