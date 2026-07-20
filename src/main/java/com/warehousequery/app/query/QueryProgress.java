package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QueryProgress {
    private final String batchId;
    private final String entryNumber;
    private final QuerySegment segment;
    private final int completedAttempts;
    private final int totalAttempts;
    private final boolean success;
    private final List<WarehouseEntry> rows;
    private final String error;

    public QueryProgress(
        String batchId,
        String entryNumber,
        QuerySegment segment,
        int completedAttempts,
        int totalAttempts,
        boolean success,
        List<WarehouseEntry> rows,
        String error) {
        this.batchId = batchId;
        this.entryNumber = entryNumber;
        this.segment = segment;
        this.completedAttempts = completedAttempts;
        this.totalAttempts = totalAttempts;
        this.success = success;
        this.rows = Collections.unmodifiableList(new ArrayList<WarehouseEntry>(
            rows == null ? Collections.<WarehouseEntry>emptyList() : rows));
        this.error = error == null ? "" : error;
    }

    public String batchId() {
        return this.batchId;
    }

    public String entryNumber() {
        return this.entryNumber;
    }

    public String jcbh() {
        return this.entryNumber;
    }

    public QuerySegment segment() {
        return this.segment;
    }

    public int completedAttempts() {
        return this.completedAttempts;
    }

    public int completed() {
        return this.completedAttempts;
    }

    public int totalAttempts() {
        return this.totalAttempts;
    }

    public int total() {
        return this.totalAttempts;
    }

    public boolean success() {
        return this.success;
    }

    public List<WarehouseEntry> rows() {
        return this.rows;
    }

    public String error() {
        return this.error;
    }

    public String message() {
        return this.error;
    }

    public String identity() {
        return this.entryNumber + "/" + this.segment.ordinal();
    }
}
