package com.warehousequery.app.query;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 日志滚动测试：主日志只保留当天内容，跨天时归档为带日期文件。
 */
public class QueryLogRotationTest {

    @Test
    public void rotatesOldLogToDatedArchive() throws Exception {
        Path dir = Files.createTempDirectory("wq-rotate-test");
        try {
            Path request = dir.resolve("request_log.txt");
            String oldEntry = "=== batch=old id=X segment=1/1 range=2025-01-01..2025-12-31"
                + " timestamp=2026-01-01T00:00:00Z type=response ===\nHTTP 200\n\n";
            Files.writeString(request, oldEntry);

            QuerySegment segment = new QuerySegment(0, 1,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 30));
            QueryLogSink sink = QueryLogSink.persistent(
                "b1", request, dir.resolve("response_log.txt"));
            sink.request("L26TEST", segment, "BODY-NEW");

            assertTrue(Files.exists(dir.resolve("request_log-20260101.txt")),
                "旧日志应归档为带日期文件");
            String content = Files.readString(request);
            assertFalse(content.contains("batch=old"), "主文件不应再含旧记录");
            assertTrue(content.contains("batch=b1"), "主文件应包含本次新记录");
        } finally {
            deleteRecursively(dir);
        }
    }

    @Test
    public void keepsTodayLogWithoutRotation() throws Exception {
        Path dir = Files.createTempDirectory("wq-rotate-test2");
        try {
            Path request = dir.resolve("request_log.txt");
            String todayEntry = "=== batch=old id=X segment=1/1 range=2025-01-01..2025-12-31"
                + " timestamp=" + Instant.now() + " type=response ===\nHTTP 200\n\n";
            Files.writeString(request, todayEntry);

            QuerySegment segment = new QuerySegment(0, 1,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 30));
            QueryLogSink sink = QueryLogSink.persistent(
                "b2", request, dir.resolve("response_log.txt"));
            sink.request("L26TEST", segment, "BODY-NEW");

            assertFalse(Files.exists(dir.resolve("request_log-20260101.txt")),
                "今天的日志不应滚动归档");
            String content = Files.readString(request);
            assertTrue(content.contains("batch=old"), "今天的旧记录应保留");
            assertTrue(content.contains("batch=b2"), "应追加本次新记录");
        } finally {
            deleteRecursively(dir);
        }
    }

    private static void deleteRecursively(Path root) throws Exception {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (java.util.stream.Stream<Path> walk = Files.walk(root)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                    }
                });
        }
    }
}
