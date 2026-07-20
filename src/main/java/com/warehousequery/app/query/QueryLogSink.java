package com.warehousequery.app.query;

import java.nio.file.Path;

public interface QueryLogSink {
    String batchId();

    void request(String jcbh, QuerySegment segment, String rawContent);

    void response(String jcbh, QuerySegment segment, String rawContent);

    void failure(String jcbh, QuerySegment segment, String rawContent);

    String requestLog();

    String responseLog();

    default void request(
        String ignoredBatchId,
        String jcbh,
        QuerySegment segment,
        String rawContent) {
        this.request(jcbh, segment, rawContent);
    }

    default void response(
        String ignoredBatchId,
        String jcbh,
        QuerySegment segment,
        String rawContent) {
        this.response(jcbh, segment, rawContent);
    }

    default void failure(
        String ignoredBatchId,
        String jcbh,
        QuerySegment segment,
        String rawContent) {
        this.failure(jcbh, segment, rawContent);
    }

    static QueryLogSink inMemory(String batchId) {
        return new DefaultQueryLogSink(batchId, null, null, false);
    }

    static QueryLogSink persistent(String batchId) {
        return new DefaultQueryLogSink(
            batchId,
            Path.of("request_log.txt"),
            Path.of("response_log.txt"),
            true);
    }

    static QueryLogSink persistent(
        String batchId,
        Path requestPath,
        Path responsePath) {
        return new DefaultQueryLogSink(batchId, requestPath, responsePath, true);
    }
}
