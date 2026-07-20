package com.warehousequery.app.query;

import java.time.LocalDate;
import java.util.Objects;

public final class QueryUiState {
    private final QueryMode mode;
    private final LocalDate start;
    private final LocalDate end;
    private final boolean oneYearSelected;
    private final boolean twoYearsSelected;
    private final boolean datePickersDisabled;

    private QueryUiState(
        QueryMode mode,
        LocalDate start,
        LocalDate end,
        boolean oneYearSelected,
        boolean twoYearsSelected,
        boolean datePickersDisabled) {
        this.mode = mode;
        this.start = start;
        this.end = end;
        this.oneYearSelected = oneYearSelected;
        this.twoYearsSelected = twoYearsSelected;
        this.datePickersDisabled = datePickersDisabled;
    }

    public static QueryUiState forMode(QueryMode mode, LocalDate today) {
        QueryMode requiredMode = Objects.requireNonNull(mode, "mode");
        LocalDate requiredToday = Objects.requireNonNull(today, "today");
        LocalDate start = requiredToday.minusDays(requiredMode.totalDays() - 1L);
        return new QueryUiState(
            requiredMode,
            start,
            requiredToday,
            requiredMode == QueryMode.ONE_YEAR,
            requiredMode == QueryMode.TWO_YEARS,
            requiredMode != QueryMode.NORMAL);
    }

    public QueryMode mode() {
        return this.mode;
    }

    public LocalDate start() {
        return this.start;
    }

    public LocalDate end() {
        return this.end;
    }

    public LocalDate startDate() {
        return this.start;
    }

    public LocalDate endDate() {
        return this.end;
    }

    public boolean oneYearSelected() {
        return this.oneYearSelected;
    }

    public boolean twoYearsSelected() {
        return this.twoYearsSelected;
    }

    public boolean datePickersDisabled() {
        return this.datePickersDisabled;
    }
}
