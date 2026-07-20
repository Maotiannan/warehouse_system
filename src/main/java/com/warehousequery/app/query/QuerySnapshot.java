package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QuerySnapshot {
    public static final int SCHEMA_VERSION = 1;

    private final int schemaVersion;
    private final Instant savedAt;
    private final QueryMode mode;
    private final List<String> entryNumbers;
    private final int statusIndex;
    private final LocalDate queryEndDate;
    private final List<QuerySegment> segments;
    private final List<WarehouseEntry> rows;
    private final String requestLog;
    private final String responseLog;

    public QuerySnapshot(
        int schemaVersion,
        Instant savedAt,
        QueryMode mode,
        List<String> entryNumbers,
        int statusIndex,
        LocalDate queryEndDate,
        List<QuerySegment> segments,
        List<WarehouseEntry> rows,
        String requestLog,
        String responseLog
    ) {
        if (schemaVersion != SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported snapshot schema version: " + schemaVersion);
        }
        this.schemaVersion = schemaVersion;
        this.savedAt = Objects.requireNonNull(savedAt, "savedAt");
        this.mode = Objects.requireNonNull(mode, "mode");
        this.entryNumbers = Collections.unmodifiableList(new ArrayList<String>(entryNumbers));
        this.statusIndex = statusIndex;
        this.queryEndDate = Objects.requireNonNull(queryEndDate, "queryEndDate");
        this.segments = Collections.unmodifiableList(new ArrayList<QuerySegment>(segments));
        this.rows = Collections.unmodifiableList(new ArrayList<WarehouseEntry>(rows));
        this.requestLog = requestLog == null ? "" : requestLog;
        this.responseLog = responseLog == null ? "" : responseLog;
    }

    public int schemaVersion() {
        return this.schemaVersion;
    }

    public Instant savedAt() {
        return this.savedAt;
    }

    public QueryMode mode() {
        return this.mode;
    }

    public List<String> entryNumbers() {
        return this.entryNumbers;
    }

    public int statusIndex() {
        return this.statusIndex;
    }

    public LocalDate queryEndDate() {
        return this.queryEndDate;
    }

    public List<QuerySegment> segments() {
        return this.segments;
    }

    public List<WarehouseEntry> rows() {
        return this.rows;
    }

    public String requestLog() {
        return this.requestLog;
    }

    public String responseLog() {
        return this.responseLog;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof QuerySnapshot)) {
            return false;
        }
        QuerySnapshot value = (QuerySnapshot)other;
        return new QuerySnapshotCodec().encode(this)
            .equals(new QuerySnapshotCodec().encode(value));
    }

    @Override
    public int hashCode() {
        return new QuerySnapshotCodec().encode(this).hashCode();
    }
}
