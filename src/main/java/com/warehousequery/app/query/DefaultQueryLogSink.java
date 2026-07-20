package com.warehousequery.app.query;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Objects;

final class DefaultQueryLogSink implements QueryLogSink {
    private final String batchId;
    private final Path requestPath;
    private final Path responsePath;
    private final boolean persist;
    private final StringBuilder requestLog = new StringBuilder();
    private final StringBuilder responseLog = new StringBuilder();

    DefaultQueryLogSink(
        String batchId,
        Path requestPath,
        Path responsePath,
        boolean persist) {
        this.batchId = Objects.requireNonNull(batchId, "batchId");
        this.requestPath = requestPath;
        this.responsePath = responsePath;
        this.persist = persist;
    }

    @Override
    public String batchId() {
        return this.batchId;
    }

    @Override
    public synchronized void request(
        String jcbh,
        QuerySegment segment,
        String rawContent) {
        this.append(
            this.requestLog,
            this.requestPath,
            "request",
            jcbh,
            segment,
            rawContent);
    }

    @Override
    public synchronized void response(
        String jcbh,
        QuerySegment segment,
        String rawContent) {
        this.append(
            this.responseLog,
            this.responsePath,
            "response",
            jcbh,
            segment,
            rawContent);
    }

    @Override
    public synchronized void failure(
        String jcbh,
        QuerySegment segment,
        String rawContent) {
        this.append(
            this.responseLog,
            this.responsePath,
            "failure",
            jcbh,
            segment,
            rawContent);
    }

    @Override
    public synchronized String requestLog() {
        return this.requestLog.toString();
    }

    @Override
    public synchronized String responseLog() {
        return this.responseLog.toString();
    }

    private void append(
        StringBuilder target,
        Path path,
        String type,
        String jcbh,
        QuerySegment segment,
        String rawContent) {
        String entry = String.format(
            "=== batch=%s id=%s segment=%d/%d range=%s..%s timestamp=%s type=%s%s ===%n%s%n%n",
            this.batchId,
            jcbh == null ? "" : jcbh,
            segment.ordinal(),
            segment.totalSegments(),
            segment.start(),
            segment.end(),
            Instant.now(),
            type,
            "failure".equals(type)
                ? " failure=" + (jcbh == null ? "" : jcbh) + "/" + segment.ordinal()
                : "",
            rawContent == null ? "" : rawContent);
        target.append(entry);
        if (!this.persist || path == null) {
            return;
        }
        try {
            Path parent = path.toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                path,
                entry,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        }
        catch (IOException exception) {
            System.err.println("写入查询日志失败: " + exception.getMessage());
        }
    }
}
