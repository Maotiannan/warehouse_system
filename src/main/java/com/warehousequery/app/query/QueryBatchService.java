package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;

public final class QueryBatchService {
    private final SingleQueryClient client;
    private final Executor executor;
    private final Function<String, QueryLogSink> logSinkFactory;

    public QueryBatchService(SingleQueryClient client) {
        this(client, ForkJoinPool.commonPool(), QueryLogSink::inMemory);
    }

    public QueryBatchService(
        SingleQueryClient client,
        Function<String, QueryLogSink> logSinkFactory) {
        this(client, ForkJoinPool.commonPool(), logSinkFactory);
    }

    public QueryBatchService(
        SingleQueryClient client,
        Executor executor,
        Function<String, QueryLogSink> logSinkFactory) {
        this.client = java.util.Objects.requireNonNull(client, "client");
        this.executor = java.util.Objects.requireNonNull(executor, "executor");
        this.logSinkFactory = java.util.Objects.requireNonNull(
            logSinkFactory,
            "logSinkFactory");
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
        List<WarehouseEntry> rows = new ArrayList<WarehouseEntry>();
        List<QueryFailure> failures = new ArrayList<QueryFailure>();
        int totalAttempts = request.entryNumbers().size() * segments.size();
        int completedAttempts = 0;

        for (String entryNumber : request.entryNumbers()) {
            for (QuerySegment segment : segments) {
                String error = "";
                boolean success = false;
                List<WarehouseEntry> returnedRows = Collections.emptyList();
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
                    rows.addAll(returnedRows);
                    success = true;
                }
                catch (RuntimeException exception) {
                    Throwable cause = unwrap(exception);
                    error = readableMessage(cause);
                    QueryFailure failure = new QueryFailure(entryNumber, segment, error);
                    failures.add(failure);
                    try {
                        logs.failure(entryNumber, segment, error);
                    }
                    catch (RuntimeException logException) {
                        System.err.println("记录查询失败日志失败: " + logException.getMessage());
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
            rows,
            failures,
            logs.requestLog(),
            logs.responseLog());
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
}
