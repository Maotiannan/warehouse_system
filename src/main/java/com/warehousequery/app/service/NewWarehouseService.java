package com.warehousequery.app.service;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.WarehouseEntry;
import com.warehousequery.app.query.QueryLogSink;
import com.warehousequery.app.query.QuerySegment;
import com.warehousequery.app.query.SingleQueryClient;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 新查询系统（螭吻WMS）匿名查询服务。
 *
 * 走查询平台的公开查询通道，无需登录、无需会话。接口为 REST JSON：
 * 查询进仓单列表（进仓预约）用 POST 请求体带条件，返回分页结构
 * （当前页/列表/每页条数/总数）。本服务把条件与结果映射为程序现有
 * 的数据模型与批量查询接口。
 */
public class NewWarehouseService implements SingleQueryClient {
    private static final String BASE_URL = "https://cw-query.gx-logistics.com";
    private static final String QUERY_ENDPOINT = "/api/query_platform/anonymous/wms_in_order_reservation/query";
    private static final String SYS_FILE_ENDPOINT = "/api/query_platform/anonymous/sys_file/biz_info/";
    private static final String BIZ_TYPE_RESERVATION = "WmsInOrder_Reservation";
    /** 每页条数：一次取大页减少请求次数；若结果超过单页再自动翻页。 */
    private static final int PAGE_SIZE = 500;

    private final CloseableHttpClient httpClient;
    private final ExecutorService queryExecutor;

    public NewWarehouseService() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(AppConfig.CONNECT_TIMEOUT)
            .setSocketTimeout(AppConfig.SOCKET_TIMEOUT)
            .setRedirectsEnabled(true)
            .build();
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "new-system-query-worker");
            thread.setDaemon(true);
            return thread;
        };
        int poolSize = Math.max(2, Runtime.getRuntime().availableProcessors());
        this.queryExecutor = Executors.newFixedThreadPool(poolSize, threadFactory);
    }

    @Override
    public CompletableFuture<List<WarehouseEntry>> query(
        String jcbh,
        int status,
        QuerySegment segment,
        QueryLogSink logs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.executeQuery(jcbh, status, segment, logs);
            }
            catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }, this.queryExecutor);
    }

    private List<WarehouseEntry> executeQuery(
        String rawJcbh,
        int status,
        QuerySegment segment,
        QueryLogSink logs) throws IOException {
        if (segment == null) {
            throw new IllegalArgumentException("查询时间段不能为空");
        }
        if (logs == null) {
            throw new IllegalArgumentException("查询日志接收器不能为空");
        }
        String jcbh = rawJcbh == null ? "" : rawJcbh.trim();
        if (jcbh.isEmpty()) {
            throw new IllegalArgumentException("进仓编号不能为空");
        }

        ArrayList<WarehouseEntry> allEntries = new ArrayList<>();
        int pageIndex = 1;
        long total = -1L;
        do {
            JSONObject body = this.buildQueryBody(jcbh, status, segment, pageIndex);
            String endpoint = BASE_URL + QUERY_ENDPOINT;
            logs.request(jcbh, segment,
                "POST " + endpoint + "\nBody: " + body.toString());
            JSONObject page = this.postQuery(endpoint, body, jcbh, segment, logs);
            JSONArray list = page.optJSONArray("list");
            if (list == null) {
                list = new JSONArray();
            }
            for (int index = 0; index < list.length(); index++) {
                Object value = list.get(index);
                if (value instanceof JSONObject) {
                    allEntries.add(this.convertRow((JSONObject) value, jcbh));
                }
            }
            total = page.optLong("total", allEntries.size());
            int loaded = allEntries.size();
            if (list.length() > 0 && loaded < total) {
                pageIndex++;
            } else {
                break;
            }
        } while (pageIndex < 200);
        return allEntries;
    }

    private JSONObject buildQueryBody(
        String jcbh,
        int status,
        QuerySegment segment,
        int pageIndex) {
        JSONObject body = new JSONObject();
        body.put("pageIndex", pageIndex);
        body.put("pageSize", PAGE_SIZE);
        body.put("order", new JSONArray());
        JSONArray where = new JSONArray();
        where.put(condition("forwarderCode", "string", "EQ", jcbh));
        where.put(condition("inAboutDate", "date", "GE", segment.start().toString()));
        where.put(condition("inAboutDate", "date", "LE", segment.end().toString()));
        String coverStatus = AppConfig.newSystemCoverStatus(status);
        if (coverStatus != null) {
            where.put(condition("coverStatus", "number", "EQ", coverStatus));
        }
        body.put("where", where);
        return body;
    }

    private static JSONObject condition(
        String fieldName,
        String primitiveType,
        String type,
        String value) {
        JSONObject condition = new JSONObject();
        condition.put("fieldName", fieldName);
        condition.put("primitiveType", primitiveType);
        condition.put("type", type);
        condition.put("values", new JSONArray().put(value));
        return condition;
    }

    private JSONObject postQuery(
        String endpoint,
        JSONObject body,
        String jcbh,
        QuerySegment segment,
        QueryLogSink logs) throws IOException {
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("User-Agent", AppConfig.USER_AGENT);
        request.setHeader("Accept", "application/json, text/plain, */*");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        request.setHeader("Content-Type", "application/json;charset=UTF-8");
        request.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = this.httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = response.getEntity() == null
                ? ""
                : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            logs.response(jcbh, segment, "HTTP " + statusCode + "\n" + responseBody);
            if (statusCode != 200) {
                throw new IOException("HTTP请求失败，状态码: " + statusCode);
            }
            return this.parsePage(responseBody);
        }
    }

    private JSONObject parsePage(String responseBody) throws IOException {
        final JSONObject json;
        try {
            json = new JSONObject(responseBody);
        }
        catch (Exception exception) {
            throw new IOException("服务器返回了无效的JSON格式", exception);
        }
        if (json.optInt("code", -1) != 0 || !json.optBoolean("success", false)) {
            String message = json.optString("msg", "未知错误");
            throw new IOException("新系统查询失败: " + message);
        }
        Object data = json.opt("data");
        if (data instanceof JSONObject) {
            return (JSONObject) data;
        }
        if (data == null || data == JSONObject.NULL) {
            JSONObject empty = new JSONObject();
            empty.put("list", new JSONArray());
            empty.put("total", 0);
            return empty;
        }
        // 兜底：data 为数组时（无查询条件场景）按空页处理
        JSONObject empty = new JSONObject();
        empty.put("list", new JSONArray());
        empty.put("total", 0);
        return empty;
    }

    private WarehouseEntry convertRow(JSONObject item, String jcbh) {
        WarehouseEntry entry = new WarehouseEntry();
        entry.setJcbh(text(item, "forwarderCode", jcbh));
        entry.setZyh(text(item, "reservationCode", ""));
        entry.setYyh(text(item, "reservationFlowCode", ""));
        entry.setJcrq(text(item, "inAboutDate", ""));
        entry.setHwmc(text(item, "goodsName", ""));
        entry.setMt(text(item, "shippingMark", ""));
        entry.setYjjs(number(item, "goodsCtns"));
        entry.setYjtj(decimal(item, "goodsVolume"));
        entry.setYjmz(decimal(item, "goodsGrossWeight"));
        entry.setJs(number(item, "tallyGoodsCtns"));
        entry.setTj(decimal(item, "tallyGoodsVolume"));
        entry.setMz(decimal(item, "tallyGoodsGrossWeight"));
        entry.setKcjs(decimal(item, "stockCtns"));
        entry.setKctj(decimal(item, "stockGoodsVolume"));
        entry.setKcmz(decimal(item, "stockGoodsGrossWeight"));
        entry.setBzgg(text(item, "goodsPackageName", ""));
        entry.setCh(text(item, "licensePlateNumber", ""));
        entry.setJsy(text(item, "driver", ""));
        entry.setJsydh(text(item, "driverPhone", ""));
        entry.setCleng(text(item, "vehicleLength", ""));
        entry.setShdw(text(item, "factory", ""));
        entry.setBgzt(statusName(item));
        entry.setJcid(text(item, "id", ""));
        entry.setInguid(text(item, "resourceId", ""));
        entry.setSourceSystem(1);
        entry.setBz(text(item, "tallyRemark", ""));
        entry.setSrrq(text(item, "addTime", ""));
        entry.setXhrq(text(item, "checkInTime", ""));
        entry.setLf(text(item, "lfType", ""));
        return entry;
    }

    private static String statusName(JSONObject item) {
        String raw = item.optString("status", "");
        if (raw == null || raw.isBlank()) {
            return "";
        }
        try {
            switch (Integer.parseInt(raw)) {
                case -1: return "订单作废";
                case 5: return "审核不通过";
                case 10: return "待审核";
                case 20: return "已审核";
                case 30: return "已签到";
                case 32: return "已打单";
                case 33: return "已放行";
                case 35: return "已进门";
                case 40: return "开始理货";
                case 57: return "理货完成";
                case 70: return "入库确认";
                default: return raw;
            }
        }
        catch (NumberFormatException exception) {
            return raw;
        }
    }

    private static String text(JSONObject item, String key, String fallback) {
        String value = item.optString(key, fallback == null ? "" : fallback);
        return value == null ? fallback : value;
    }

    private static int number(JSONObject item, String key) {
        Object value = item.opt(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(item.optString(key, "0"));
        }
        catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static double decimal(JSONObject item, String key) {
        Object value = item.opt(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(item.optString(key, "0"));
        }
        catch (NumberFormatException exception) {
            return 0.0;
        }
    }

    /**
     * 查询新系统附件（照片/进仓附件），返回第一个可访问的文件地址；
     * 没有附件时抛出带说明的异常。
     */
    public String fetchFileUrl(String resourceId, String bizType) throws IOException {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IOException("该记录没有附件标识");
        }
        String type = bizType == null || bizType.isBlank() ? BIZ_TYPE_RESERVATION : bizType;
        String endpoint = BASE_URL + SYS_FILE_ENDPOINT
            + URLEncoder.encode(resourceId, StandardCharsets.UTF_8)
            + "/" + URLEncoder.encode(type, StandardCharsets.UTF_8);
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("User-Agent", AppConfig.USER_AGENT);
        request.setHeader("Accept", "application/json, text/plain, */*");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        try (CloseableHttpResponse response = this.httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = response.getEntity() == null
                ? ""
                : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (statusCode != 200) {
                throw new IOException("获取附件失败，状态码: " + statusCode);
            }
            final JSONObject json;
            try {
                json = new JSONObject(responseBody);
            }
            catch (Exception exception) {
                throw new IOException("附件接口返回格式无效", exception);
            }
            if (json.optInt("code", -1) != 0 || !json.optBoolean("success", false)) {
                throw new IOException("附件接口错误: " + json.optString("msg", "未知错误"));
            }
            String fileUrl = extractFileUrl(json.opt("data"));
            if (fileUrl == null || fileUrl.isBlank()) {
                throw new IOException("该单据暂无附件");
            }
            return fileUrl;
        }
    }

    private static String extractFileUrl(Object data) {
        if (data instanceof JSONArray) {
            JSONArray array = (JSONArray) data;
            for (int index = 0; index < array.length(); index++) {
                Object value = array.get(index);
                if (value instanceof JSONObject) {
                    String url = findUrlField((JSONObject) value);
                    if (url != null) {
                        return url;
                    }
                }
            }
            return null;
        }
        if (data instanceof JSONObject) {
            JSONObject object = (JSONObject) data;
            String url = findUrlField(object);
            if (url != null) {
                return url;
            }
            Object list = object.opt("list");
            if (list instanceof JSONArray) {
                return extractFileUrl(list);
            }
            return null;
        }
        return null;
    }

    private static String findUrlField(JSONObject object) {
        String[] candidates = {"url", "fileUrl", "downloadUrl", "src", "path", "file_path"};
        for (String candidate : candidates) {
            String value = object.optString(candidate, "");
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public void close() {
        try {
            this.httpClient.close();
        }
        catch (IOException ignored) {
            // 关闭失败不影响程序退出
        }
        this.queryExecutor.shutdownNow();
    }
}
