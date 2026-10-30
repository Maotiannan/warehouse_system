package com.warehousequery.app.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

final class DefaultQueryLogSink implements QueryLogSink {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter ARCHIVE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int ARCHIVE_KEEP_DAYS = 7;

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
            this.rotateIfNeeded(path);
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

    /**
     * 主日志文件只保留当天内容：若文件里最早的记录不是今天，
     * 就把整个文件滚动归档为带日期的文件，主文件从当天重新开始；
     * 归档保留最近几天，更早的自动清理。
     */
    private void rotateIfNeeded(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try {
            LocalDate today = LocalDate.now();
            LocalDate firstDate = this.firstRecordDate(path);
            if (firstDate != null && !firstDate.isBefore(today)) {
                return;
            }
            LocalDate archiveDate = firstDate != null
                ? firstDate
                : Files.getLastModifiedTime(path)
                    .toInstant().atZone(ZONE).toLocalDate();
            String name = path.getFileName().toString();
            String base = name.toLowerCase().endsWith(".txt")
                ? name.substring(0, name.length() - 4)
                : name;
            Path archive = path.resolveSibling(
                base + "-" + archiveDate.format(ARCHIVE_DATE) + ".txt");
            Files.move(path, archive, StandardCopyOption.REPLACE_EXISTING);
            this.cleanupArchives(archive.getParent(), base + "-");
        }
        catch (IOException exception) {
            System.err.println("滚动查询日志失败: " + exception.getMessage());
        }
    }

    private LocalDate firstRecordDate(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            int index = line.indexOf("timestamp=");
            if (index < 0) {
                return null;
            }
            String timestamp = line.substring(index + "timestamp=".length());
            int end = timestamp.indexOf(' ');
            if (end > 0) {
                timestamp = timestamp.substring(0, end);
            }
            return Instant.parse(timestamp).atZone(ZONE).toLocalDate();
        }
        catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    private void cleanupArchives(Path dir, String prefix) {
        if (dir == null) {
            return;
        }
        LocalDate cutoff = LocalDate.now().minusDays((long) ARCHIVE_KEEP_DAYS - 1L);
        try (java.util.stream.Stream<Path> files = Files.list(dir)) {
            files.filter(path -> path.getFileName().toString().startsWith(prefix))
                .forEach(path -> {
                    try {
                        String fileName = path.getFileName().toString();
                        String datePart = fileName.substring(
                            prefix.length(),
                            prefix.length() + ARCHIVE_DATE.toString().length());
                        LocalDate archiveDate = LocalDate.parse(
                            datePart, ARCHIVE_DATE);
                        if (archiveDate.isBefore(cutoff)) {
                            Files.deleteIfExists(path);
                        }
                    }
                    catch (IOException | RuntimeException ignored) {
                        // 单个归档清理失败不影响主流程
                    }
                });
        }
        catch (IOException ignored) {
            // 目录不可读时跳过清理
        }
    }
}
