package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;

public final class QueryBatchService {
    /**
     * 连续两次请求之间的最小间隔（毫秒）。服务端对请求频率敏感，
     * 过快会返回 429“请求过于频繁”，这里把整体速率限制在每秒约 3 个请求，
     * 接近网页端人工操作节奏。
     */
    static final long DEFAULT_MIN_INTERVAL_MILLIS = 300L;
    /** 单个分段请求遇到可重试错误时的最大重试次数（不含首次请求）。 */
    static final int DEFAULT_MAX_RETRIES = 4;
    /** 指数退避的基础等待时间（毫秒），实际等待按 1x、2x、4x、8x 增长并加抖动。 */
    static final long DEFAULT_BASE_BACKOFF_MILLIS = 1000L;
    private static final long MAX_BACKOFF_MILLIS = 10000L;

    private final SingleQueryClient client;
    private final Executor executor;
    private final Function<String, QueryLogSink> logSinkFactory;
    private final long minIntervalMillis;
    private final int maxRetries;
    private final long baseBackoffMillis;

    public QueryBatchService(SingleQueryClient client) {
        this(client,
            ForkJoinPool.commonPool(),
            QueryLogSink::inMemory,
            DEFAULT_MIN_INTERVAL_MILLIS,
            DEFAULT_MAX_RETRIES,
            DEFAULT_BASE_BACKOFF_MILLIS);
    }

    public QueryBatchService(
        SingleQueryClient client,
        Function<String, QueryLogSink> logSinkFactory) {
        this(client,
            ForkJoinPool.commonPool(),
            logSinkFactory,
            DEFAULT_MIN_INTERVAL_MILLIS,
            DEFAULT_MAX_RETRIES,
            DEFAULT_BASE_BACKOFF_MILLIS);
    }

    /**
     * 带日志工厂与节流/重试参数：调用方（如主程序按查询系统切换间隔）可以
     * 显式指定最小间隔、重试次数与退避基准时间。
     */
    public QueryBatchService(
        SingleQueryClient client,
        Function<String, QueryLogSink> logSinkFactory,
        long minIntervalMillis,
        int maxRetries,
        long baseBackoffMillis) {
        this(client,
            ForkJoinPool.commonPool(),
            logSinkFactory,
            minIntervalMillis,
            maxRetries,
            baseBackoffMillis);
    }

    public QueryBatchService(
        SingleQueryClient client,
        Executor executor,
        Function<String, QueryLogSink> logSinkFactory) {
        this(client,
            executor,
            logSinkFactory,
            DEFAULT_MIN_INTERVAL_MILLIS,
            DEFAULT_MAX_RETRIES,
            DEFAULT_BASE_BACKOFF_MILLIS);
    }

    /**
     * 测试与特殊场景使用：可显式指定节流间隔、重试次数与退避基准时间。
     * 把间隔和重试次数设为 0 时，行为与不做节流/重试的早期版本完全一致。
     */
    public QueryBatchService(
        SingleQueryClient client,
        long minIntervalMillis,
        int maxRetries,
        long baseBackoffMillis) {
        this(client,
            ForkJoinPool.commonPool(),
            QueryLogSink::inMemory,
            minIntervalMillis,
            maxRetries,
            baseBackoffMillis);
    }

    public QueryBatchService(
        SingleQueryClient client,
        Executor executor,
        Function<String, QueryLogSink> logSinkFactory,
        long minIntervalMillis,
        int maxRetries,
        long baseBackoffMillis) {
        this.client = java.util.Objects.requireNonNull(client, "client");
        this.executor = java.util.Objects.requireNonNull(executor, "executor");
        this.logSinkFactory = java.util.Objects.requireNonNull(
            logSinkFactory,
            "logSinkFactory");
        this.minIntervalMillis = Math.max(0L, minIntervalMillis);
        this.maxRetries = Math.max(0, maxRetries);
        this.baseBackoffMillis = Math.max(0L, baseBackoffMillis);
    }

    public CompletableFuture<QueryBatchResult> execute(
        QueryBatchRequest request,
        Consumer<QueryProgress> progressListener) {
        java.util.Objects.requireNonNull(request, "request");
        Consumer<QueryProgress> listener = progressListener == null
            ? progress -> { }
            : progressListener;
        return CompletableFuture.supplyAsync(
            () -> this.executeSynchronously(request, listener),
            this.executor);
    }

    private QueryBatchResult executeSynchronously(
        QueryBatchRequest request,
        Consumer<QueryProgress> progressListener) {
        String batchId = UUID.randomUUID().toString();
        QueryLogSink logs = this.logSinkFactory.apply(batchId);
        if (logs == null) {
            throw new IllegalStateException("Query log sink factory returned null");
        }
        List<QuerySegment> segments = request.segments();
        Map<String, WarehouseEntry> rowsByKey = new LinkedHashMap<String, WarehouseEntry>();
        List<QueryFailure> failures = new ArrayList<QueryFailure>();
        int totalAttempts = request.entryNumbers().size() * segments.size();
        int completedAttempts = 0;
        long lastRequestNanos = 0L;

        for (String entryNumber : request.entryNumbers()) {
            for (QuerySegment segment : segments) {
                String error = "";
                boolean success = false;
                List<WarehouseEntry> returnedRows = Collections.emptyList();
                int attempt = 0;
                while (true) {
                    lastRequestNanos = this.throttleBeforeRequest(
                        lastRequestNanos,
                        this.minIntervalMillis);
                    try {
                        CompletableFuture<List<WarehouseEntry>> future = this.client.query(
                            entryNumber,
                            request.statusIndex(),
                            segment,
                            logs);
                        if (future == null) {
                            throw new IllegalStateException("Single query client returned null future");
                        }
                        List<WarehouseEntry> value = future.join();
                        returnedRows = value == null
                            ? Collections.<WarehouseEntry>emptyList()
                            : value;
                        for (WarehouseEntry row : returnedRows) {
                            rowsByKey.put(dedupKey(row), row);
                        }
                        success = true;
                        break;
                    }
                    catch (RuntimeException exception) {
                        Throwable cause = unwrap(exception);
                        String message = readableMessage(cause);
                        if (attempt < this.maxRetries && isRetryable(cause)) {
                            long backoff = backoffMillis(attempt, this.baseBackoffMillis);
                            try {
                                logs.response(
                                    entryNumber,
                                    segment,
                                    "\u8bf7\u6c42\u9047\u5230\u4e34\u65f6\u9519\u8bef\uff08"
                                        + message + "\uff09\uff0c"
                                        + backoff + "ms \u540e\u81ea\u52a8\u91cd\u8bd5\uff08\u7b2c "
                                        + (attempt + 1) + "/" + this.maxRetries + " \u6b21\u91cd\u8bd5\uff09");
                            }
                            catch (RuntimeException logException) {
                                System.err.println("\u8bb0\u5f55\u91cd\u8bd5\u65e5\u5fd7\u5931\u8d25: " + logException.getMessage());
                            }
                            if (!sleepQuietly(backoff)) {
                                error = "\u67e5\u8be2\u88ab\u4e2d\u65ad: " + message;
                                failures.add(new QueryFailure(entryNumber, segment, error));
                                this.logFailure(logs, entryNumber, segment, error);
                                break;
                            }
                            attempt++;
                            continue;
                        }
                        error = message;
                        failures.add(new QueryFailure(entryNumber, segment, error));
                        this.logFailure(logs, entryNumber, segment, error);
                        break;
                    }
                }
                completedAttempts++;
                this.publish(
                    progressListener,
                    new QueryProgress(
                        batchId,
                        entryNumber,
                        segment,
                        completedAttempts,
                        totalAttempts,
                        success,
                        returnedRows,
                        error));
            }
        }
        return new QueryBatchResult(
            batchId,
            request,
            segments,
            new ArrayList<WarehouseEntry>(rowsByKey.values()),
            failures,
            logs.requestLog(),
            logs.responseLog());
    }

    private void logFailure(
        QueryLogSink logs,
        String entryNumber,
        QuerySegment segment,
        String error) {
        try {
            logs.failure(entryNumber, segment, error);
        }
        catch (RuntimeException logException) {
            System.err.println("\u8bb0\u5f55\u67e5\u8be2\u5931\u8d25\u65e5\u5fd7\u5931\u8d25: " + logException.getMessage());
        }
    }

    /**
     * 在上一次请求之后保证至少经过 {@code minIntervalMillis} 毫秒，避免短时间内
     * 把大量请求打向服务端触发限流。返回本次请求发起前的时间戳。
     */
    private long throttleBeforeRequest(long lastRequestNanos, long minIntervalMillis) {
        if (minIntervalMillis > 0L && lastRequestNanos > 0L) {
            long elapsedMillis = (System.nanoTime() - lastRequestNanos) / 1000000L;
            long waitMillis = minIntervalMillis - elapsedMillis;
            if (waitMillis > 0L) {
                sleepQuietly(waitMillis);
            }
        }
        return System.nanoTime();
    }

    /**
     * 指数退避等待时间：第 1 次失败后等待 1 倍基准，之后依次 2、4、8 倍，
     * 并附加 0~25% 的随机抖动，避免多个请求同时重试再次撞上限流。
     */
    private static long backoffMillis(int attempt, long baseBackoffMillis) {
        if (baseBackoffMillis <= 0L) {
            return 0L;
        }
        long base = baseBackoffMillis * (1L << Math.min(attempt, 4));
        long jitter = (long) (base * 0.25d * Math.random());
        return Math.min(base + jitter, MAX_BACKOFF_MILLIS);
    }

    private static boolean sleepQuietly(long millis) {
        if (millis <= 0L) {
            return true;
        }
        try {
            Thread.sleep(millis);
            return true;
        }
        catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 判断失败是否属于值得重试的临时性错误：服务端限流（429）、服务端 5xx、
     * 连接/超时等网络问题。参数错误、编号不存在、返回内容无法解析等确定性错误
     * 不重试，避免无意义地重复请求。
     */
    static boolean isRetryable(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        String message = throwable.getMessage() == null ? "" : throwable.getMessage();
        String lower = message.toLowerCase(Locale.ROOT);
        if (message.contains("429")
            || message.contains("\u9891\u7e41")
            || lower.contains("too many requests")
            || lower.contains("rate limit")
            || lower.contains("rate-limit")) {
            return true;
        }
        if (throwable instanceof java.net.SocketTimeoutException
            || throwable instanceof java.net.ConnectException) {
            return true;
        }
        if (throwable instanceof java.io.IOException) {
            return lower.contains("timeout")
                || lower.contains("timed out")
                || lower.contains("connection")
                || lower.contains("connect")
                || lower.contains("reset")
                || lower.contains("502")
                || lower.contains("503")
                || lower.contains("504")
                || lower.contains("\u72b6\u6001\u7801: 5")
                || lower.contains("status: 5");
        }
        return false;
    }

    private void publish(
        Consumer<QueryProgress> listener,
        QueryProgress progress) {
        try {
            listener.accept(progress);
        }
        catch (RuntimeException exception) {
            System.err.println("更新查询进度失败: " + exception.getMessage());
        }
    }

    private static Throwable unwrap(Throwable exception) {
        Throwable cause = exception;
        while ((cause instanceof CompletionException
            || cause instanceof java.util.concurrent.ExecutionException)
            && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static String readableMessage(Throwable exception) {
        String message = exception == null ? "" : exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return exception == null ? "未知错误" : exception.getClass().getSimpleName();
        }
        return message.trim();
    }

    public static String dedupKey(WarehouseEntry entry) {
        String inguid = entry.getInguid();
        if (inguid != null && !inguid.isBlank()) {
            return "inguid:" + inguid;
        }
        String jcid = entry.getJcid();
        if (jcid != null && !jcid.isBlank()) {
            return "jcid:" + jcid;
        }
        String jcbh = entry.getJcbh();
        String zyh = entry.getZyh();
        return "jcbh:" + (jcbh == null ? "" : jcbh)
            + "|zyh:" + (zyh == null ? "" : zyh);
    }
}
