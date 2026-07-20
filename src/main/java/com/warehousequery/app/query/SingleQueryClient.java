package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface SingleQueryClient {
    CompletableFuture<List<WarehouseEntry>> query(
        String jcbh,
        int status,
        QuerySegment segment,
        QueryLogSink logs);
}
