package com.warehousequery.app.query;

import java.time.Instant;
import java.util.Objects;

public final class QueryFailure {
    private final String entryNumber;
    private final QuerySegment segment;
    private final String message;
    private final Instant timestamp;

    public QueryFailure(
        String entryNumber,
        QuerySegment segment,
        String message) {
        this(entryNumber, segment, message, Instant.now());
    }

    public QueryFailure(
        String entryNumber,
        QuerySegment segment,
        String message,
        Instant timestamp) {
        this.entryNumber = Objects.requireNonNull(entryNumber, "entryNumber");
        this.segment = Objects.requireNonNull(segment, "segment");
        this.message = message == null ? "" : message;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
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

    public String message() {
        return this.message;
    }

    public String error() {
        return this.message;
    }

    public Instant timestamp() {
        return this.timestamp;
    }

    public String identity() {
        return this.entryNumber + "/" + this.segment.ordinal();
    }
}
