package com.warehousequery.app.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QueryBatchRequest {
    private final List<String> entryNumbers;
    private final int statusIndex;
    private final QueryMode mode;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public QueryBatchRequest(
        List<String> entryNumbers,
        int statusIndex,
        QueryMode mode,
        LocalDate endDate) {
        this(entryNumbers, statusIndex, mode, null, endDate);
    }

    public QueryBatchRequest(
        List<String> entryNumbers,
        int statusIndex,
        QueryMode mode,
        LocalDate startDate,
        LocalDate endDate) {
        Objects.requireNonNull(entryNumbers, "entryNumbers");
        List<String> normalized = new ArrayList<String>();
        for (String entryNumber : entryNumbers) {
            if (entryNumber == null) {
                continue;
            }
            String value = entryNumber.trim();
            if (!value.isEmpty()) {
                normalized.add(value);
            }
        }
        this.entryNumbers = Collections.unmodifiableList(normalized);
        this.statusIndex = statusIndex;
        this.mode = Objects.requireNonNull(mode, "mode");
        this.startDate = startDate;
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }

    public List<String> entryNumbers() {
        return this.entryNumbers;
    }

    public List<String> ids() {
        return this.entryNumbers;
    }

    public int statusIndex() {
        return this.statusIndex;
    }

    public int status() {
        return this.statusIndex;
    }

    public QueryMode mode() {
        return this.mode;
    }

    public LocalDate startDate() {
        return this.startDate;
    }

    public LocalDate queryStartDate() {
        return this.startDate;
    }

    public LocalDate endDate() {
        return this.endDate;
    }

    public LocalDate queryEndDate() {
        return this.endDate;
    }

    public List<QuerySegment> segments() {
        if (this.mode == QueryMode.NORMAL && this.startDate != null) {
            return Collections.singletonList(new QuerySegment(
                1,
                1,
                this.startDate,
                this.endDate));
        }
        return QueryRangePlanner.plan(this.mode, this.endDate);
    }
}
