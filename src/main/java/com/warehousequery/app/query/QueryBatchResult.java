package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QueryBatchResult {
    private final String batchId;
    private final QueryBatchRequest request;
    private final List<QuerySegment> segments;
    private final List<WarehouseEntry> rows;
    private final List<QueryFailure> failures;
    private final String requestLog;
    private final String responseLog;

    public QueryBatchResult(
        String batchId,
        QueryBatchRequest request,
        List<QuerySegment> segments,
        List<WarehouseEntry> rows,
        List<QueryFailure> failures,
        String requestLog,
        String responseLog) {
        this.batchId = Objects.requireNonNull(batchId, "batchId");
        this.request = Objects.requireNonNull(request, "request");
        this.segments = immutable(segments);
        this.rows = immutable(rows);
        this.failures = immutable(failures);
        this.requestLog = requestLog == null ? "" : requestLog;
        this.responseLog = responseLog == null ? "" : responseLog;
    }

    public String batchId() {
        return this.batchId;
    }

    public QueryBatchRequest request() {
        return this.request;
    }

    public QueryBatchRequest queryRequest() {
        return this.request;
    }

    public List<QuerySegment> segments() {
        return this.segments;
    }

    public List<WarehouseEntry> rows() {
        return this.rows;
    }

    public List<QueryFailure> failures() {
        return this.failures;
    }

    public boolean complete() {
        return this.failures.isEmpty();
    }

    public boolean isComplete() {
        return this.complete();
    }

    public String requestLog() {
        return this.requestLog;
    }

    public String responseLog() {
        return this.responseLog;
    }

    private static <T> List<T> immutable(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<T>(
            values == null ? Collections.<T>emptyList() : values));
    }
}
