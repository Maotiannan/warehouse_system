/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.http.Header
 *  org.apache.http.HttpEntity
 *  org.apache.http.client.CookieStore
 *  org.apache.http.client.config.RequestConfig
 *  org.apache.http.client.methods.CloseableHttpResponse
 *  org.apache.http.client.methods.HttpGet
 *  org.apache.http.client.methods.HttpUriRequest
 *  org.apache.http.client.protocol.HttpClientContext
 *  org.apache.http.impl.client.BasicCookieStore
 *  org.apache.http.impl.client.CloseableHttpClient
 *  org.apache.http.impl.client.HttpClients
 *  org.apache.http.protocol.HttpContext
 *  org.apache.http.util.EntityUtils
 *  org.json.JSONArray
 *  org.json.JSONObject
 */
package com.warehousequery.app.service;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.WarehouseEntry;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class WarehouseService {
    private static final String BASE_URL = "http://60.190.0.98:81";
    private static final String API_ENDPOINT = "/csccmisHandler/CsccmisHandler.ashx";
    private final CookieStore cookieStore = new BasicCookieStore();
    private final HttpClientContext context = HttpClientContext.create();
    private final CloseableHttpClient httpClient;
    private final ExecutorService queryExecutor;
    private static final long SESSION_REFRESH_INTERVAL = 900000L;
    private long lastSessionRefreshTime = 0L;
    private static final String REQUEST_LOG_FILE = "request_log.txt";
    private static final String RESPONSE_LOG_FILE = "response_log.txt";
    private static List<String> extractedTableHeaders = new ArrayList<String>();

    public WarehouseService() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000).setRedirectsEnabled(true).build();
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(config).setDefaultCookieStore(this.cookieStore).build();
        this.context.setCookieStore(this.cookieStore);
        ThreadFactory queryThreadFactory = runnable -> {
            Thread thread = new Thread(runnable, "warehouse-query-worker");
            thread.setDaemon(true);
            return thread;
        };
        int poolSize = Math.max(2, Runtime.getRuntime().availableProcessors());
        this.queryExecutor = Executors.newFixedThreadPool(poolSize, queryThreadFactory);
        try {
            this.initSession();
        }
        catch (Exception e) {
            System.err.println("\u521d\u59cb\u5316\u4f1a\u8bdd\u5931\u8d25: " + e.getMessage());
            e.printStackTrace();
        }
        this.clearLogFiles();
    }

    private void clearLogFiles() {
        try {
            Files.deleteIfExists(Paths.get(REQUEST_LOG_FILE, new String[0]));
            Files.deleteIfExists(Paths.get(RESPONSE_LOG_FILE, new String[0]));
        }
        catch (IOException e) {
            System.err.println("\u6e05\u7a7a\u65e5\u5fd7\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
        }
    }

    private void initSession() {
        try {
            System.out.println("\u6b63\u5728\u521d\u59cb\u5316\u4f1a\u8bdd...");
            String mainPageUrl = "http://60.190.0.98:81/csccmisHandler/website/csccmis.aspx";
            HttpGet request = new HttpGet(mainPageUrl);
            this.setupRequestHeaders(request);
            try (CloseableHttpResponse response = this.httpClient.execute((HttpUriRequest)request, (HttpContext)this.context);){
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("\u4f1a\u8bdd\u521d\u59cb\u5316\u54cd\u5e94\u72b6\u6001\u7801: " + statusCode);
                if (statusCode == 200) {
                    this.processSessionResponse(response);
                    this.lastSessionRefreshTime = System.currentTimeMillis();
                    System.out.println("\u4f1a\u8bdd\u521d\u59cb\u5316\u6210\u529f");
                } else {
                    System.err.println("\u4f1a\u8bdd\u521d\u59cb\u5316\u5931\u8d25\uff0c\u72b6\u6001\u7801: " + statusCode);
                }
            }
        }
        catch (Exception e) {
            System.err.println("\u521d\u59cb\u5316\u4f1a\u8bdd\u65f6\u53d1\u751f\u5f02\u5e38: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupRequestHeaders(HttpGet request) {
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        request.setHeader("Accept-Encoding", "gzip, deflate");
        request.setHeader("Connection", "keep-alive");
        request.setHeader("Upgrade-Insecure-Requests", "1");
    }

    private void processSessionResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String responseBody = EntityUtils.toString((HttpEntity)entity, (Charset)StandardCharsets.UTF_8);
            try {
                Files.write(Paths.get("session_response.html", new String[0]), responseBody.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            }
            catch (IOException e) {
                System.err.println("\u4fdd\u5b58\u4f1a\u8bdd\u54cd\u5e94\u5931\u8d25: " + e.getMessage());
            }
            this.logCookies();
        }
    }

    private void logCookies() {
        System.out.println("\u5f53\u524dCookie\u4fe1\u606f:");
        this.cookieStore.getCookies().forEach(cookie -> System.out.println("  " + cookie.getName() + " = " + cookie.getValue()));
    }

    private void checkAndRefreshSession() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastSessionRefreshTime > 900000L) {
            System.out.println("\u4f1a\u8bdd\u8d85\u65f6\uff0c\u6b63\u5728\u5237\u65b0...");
            this.initSession();
        }
    }

    public CompletableFuture<List<WarehouseEntry>> queryWarehouse(List<String> jcbhList, int status, String startDate, String endDate) {
        return CompletableFuture.supplyAsync(() -> this.executeQuery(jcbhList, status, startDate, endDate), this.queryExecutor);
    }

    private List<WarehouseEntry> executeQuery(List<String> jcbhList, int status, String startDate, String endDate) {
        ArrayList<WarehouseEntry> allEntries = new ArrayList<WarehouseEntry>();
        this.clearLogFiles();
        this.logQueryParameters(jcbhList, status, startDate, endDate);
        int total = jcbhList.size();
        int current = 0;
        for (String jcbh : jcbhList) {
            System.out.printf("\u540e\u53f0\u67e5\u8be2\u8fdb\u5ea6: %d/%d (%s)%n", ++current, total, jcbh);
            try {
                List<WarehouseEntry> entries = this.querySingleJcbh(jcbh.trim(), status, startDate, endDate);
                allEntries.addAll(entries);
                Thread.sleep(200L);
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("\u67e5\u8be2\u4efb\u52a1\u88ab\u4e2d\u65ad", ie);
            }
            catch (Exception e) {
                System.err.println("\u67e5\u8be2\u8fdb\u4ed3\u7f16\u53f7 " + jcbh + " \u5931\u8d25: " + e.getMessage());
                e.printStackTrace();
                WarehouseEntry errorEntry = new WarehouseEntry();
                errorEntry.setJcbh(jcbh);
                errorEntry.setBz("\u67e5\u8be2\u5931\u8d25: " + e.getMessage());
                allEntries.add(errorEntry);
            }
        }
        System.out.println("\u67e5\u8be2\u5b8c\u6210\uff0c\u5171\u627e\u5230 " + allEntries.size() + " \u6761\u8bb0\u5f55");
        return allEntries;
    }

    private void logQueryParameters(List<String> jcbhList, int status, String startDate, String endDate) {
        try {
            StringBuilder requestLog = new StringBuilder();
            requestLog.append("=== \u67e5\u8be2\u53c2\u6570 ===\n");
            requestLog.append("\u67e5\u8be2\u65f6\u95f4: ").append(LocalDateTime.now()).append("\n");
            requestLog.append("\u8fdb\u4ed3\u7f16\u53f7\u5217\u8868: ").append(String.join((CharSequence)", ", jcbhList)).append("\n");
            requestLog.append("\u72b6\u6001: ").append(AppConfig.getStatusName(status)).append(" (").append(status).append(")\n");
            requestLog.append("\u72b6\u6001\u4ee3\u7801: ").append(AppConfig.getStatusCode(status)).append("\n");
            requestLog.append("\u65e5\u671f\u8303\u56f4: ").append(startDate).append(" \u81f3 ").append(endDate).append("\n\n");
            Files.write(Paths.get(REQUEST_LOG_FILE, new String[0]), requestLog.toString().getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        catch (IOException e) {
            System.err.println("\u8bb0\u5f55\u67e5\u8be2\u53c2\u6570\u5931\u8d25: " + e.getMessage());
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private List<WarehouseEntry> querySingleJcbh(String jcbh, int status, String startDate, String endDate) throws IOException {
        System.out.println("\u5f00\u59cb\u67e5\u8be2\u8fdb\u4ed3\u7f16\u53f7: " + jcbh);
        this.checkAndRefreshSession();
        String zt = AppConfig.getStatusCode(status);
        System.out.println("\u4f7f\u7528\u72b6\u6001\u4ee3\u7801: " + zt + " (\u72b6\u6001: " + AppConfig.getStatusName(status) + ")");
        String methodParameter = this.buildMethodParameter(jcbh, zt, startDate, endDate);
        System.out.println("\u6784\u5efa\u7684\u65b9\u6cd5\u53c2\u6570: " + methodParameter);
        String queryUrl = this.buildQueryUrl(methodParameter);
        System.out.println("\u67e5\u8be2URL: " + queryUrl);
        this.logRequest(queryUrl, jcbh);
        HttpGet request = new HttpGet(queryUrl);
        this.setupApiRequestHeaders(request);
        try (CloseableHttpResponse response = this.httpClient.execute((HttpUriRequest)request, (HttpContext)this.context);){
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("\u54cd\u5e94\u72b6\u6001\u7801: " + statusCode);
            if (statusCode == 200) {
                String responseBody = EntityUtils.toString((HttpEntity)response.getEntity(), (Charset)StandardCharsets.UTF_8);
                System.out.println("\u54cd\u5e94\u5185\u5bb9\u957f\u5ea6: " + responseBody.length());
                System.out.println("\u54cd\u5e94\u5185\u5bb9\u524d200\u5b57\u7b26: " + (String)(responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));
                this.logResponse(responseBody, jcbh);
                List<WarehouseEntry> list = this.parseJsonResponse(responseBody, jcbh);
                return list;
            }
            String errorMsg = "HTTP\u8bf7\u6c42\u5931\u8d25\uff0c\u72b6\u6001\u7801: " + statusCode;
            System.err.println(errorMsg);
            try {
                String errorResponse = EntityUtils.toString((HttpEntity)response.getEntity(), (Charset)StandardCharsets.UTF_8);
                System.err.println("\u9519\u8bef\u54cd\u5e94\u5185\u5bb9: " + errorResponse);
                this.logResponse("\u9519\u8bef\u54cd\u5e94 (\u72b6\u6001\u7801: " + statusCode + "): " + errorResponse, jcbh);
                throw new IOException(errorMsg);
            }
            catch (Exception e) {
                System.err.println("\u8bfb\u53d6\u9519\u8bef\u54cd\u5e94\u5931\u8d25: " + e.getMessage());
            }
            throw new IOException(errorMsg);
        }
        catch (Exception e) {
            System.err.println("\u6267\u884cHTTP\u8bf7\u6c42\u65f6\u53d1\u751f\u5f02\u5e38: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("\u8bf7\u6c42\u6267\u884c\u5931\u8d25: " + e.getMessage(), e);
        }
    }

    private String buildMethodParameter(String jcbh, String zt, String startDate, String endDate) {
        try {
            JSONObject params = new JSONObject();
            params.put("jcbh", (Object)jcbh);
            params.put("zt", (Object)zt);
            params.put("jcrq1", (Object)startDate);
            params.put("jcrq2", (Object)endDate);
            String jsonString = params.toString();
            System.out.println("\u539f\u59cbJSON\u53c2\u6570: " + jsonString);
            String encoded = URLEncoder.encode(jsonString, StandardCharsets.UTF_8.name());
            System.out.println("URL\u7f16\u7801\u540e\u53c2\u6570: " + encoded);
            return encoded;
        }
        catch (Exception e) {
            System.err.println("\u6784\u5efa\u67e5\u8be2\u53c2\u6570\u5931\u8d25: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private String buildQueryUrl(String methodParameter) {
        return String.format("%s?page=%d&limit=%d&CallMethod=QueryJcInfo&MethodParameter=%s", AppConfig.getApiUrl(), 1, 200, methodParameter);
    }

    private void setupApiRequestHeaders(HttpGet request) {
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
        request.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        request.setHeader("X-Requested-With", "XMLHttpRequest");
        request.setHeader("Referer", "http://60.190.0.98:81/csccmisHandler/website/csccmis.aspx");
        request.setHeader("Accept-Encoding", "gzip, deflate");
        request.setHeader("Connection", "keep-alive");
        System.out.println("\u8bf7\u6c42\u5934\u4fe1\u606f:");
        request.getAllHeaders();
        for (Header header : request.getAllHeaders()) {
            System.out.println("  " + header.getName() + ": " + header.getValue());
        }
    }

    private void logRequest(String url, String jcbh) {
        try {
            String logEntry = String.format("=== \u8bf7\u6c42 %s ===\n\u65f6\u95f4: %s\nURL: %s\n\n", jcbh, Instant.now(), url);
            Files.write(Paths.get(REQUEST_LOG_FILE, new String[0]), logEntry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.err.println("\u8bb0\u5f55\u8bf7\u6c42\u65e5\u5fd7\u5931\u8d25: " + e.getMessage());
        }
    }

    private void logResponse(String response, String jcbh) {
        try {
            String logEntry = String.format("=== \u54cd\u5e94 %s ===\n\u65f6\u95f4: %s\n\u5185\u5bb9: %s\n\n", jcbh, Instant.now(), response);
            Files.write(Paths.get(RESPONSE_LOG_FILE, new String[0]), logEntry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.err.println("\u8bb0\u5f55\u54cd\u5e94\u65e5\u5fd7\u5931\u8d25: " + e.getMessage());
        }
    }

    private List<WarehouseEntry> parseJsonResponse(String jsonResponse, String jcbh) {
        ArrayList<WarehouseEntry> entries = new ArrayList<WarehouseEntry>();
        try {
            System.out.println("\u5f00\u59cb\u89e3\u6790JSON\u54cd\u5e94...");
            JSONObject json = this.validateAndParseJson(jsonResponse, jcbh);
            if (json == null) {
                return this.createErrorEntry(jcbh, "\u670d\u52a1\u5668\u8fd4\u56de\u4e86\u65e0\u6548\u7684JSON\u683c\u5f0f");
            }
            System.out.println("JSON\u89e3\u6790\u6210\u529f\uff0c\u68c0\u67e5\u8fd4\u56de\u7801...");
            int code = json.optInt("code", -1);
            System.out.println("API\u8fd4\u56de\u7801: " + code);
            if (code != 0) {
                String msg = json.optString("msg", "\u672a\u77e5\u9519\u8bef");
                System.err.println("API\u8fd4\u56de\u9519\u8bef: " + msg);
                return this.handleApiError(code, msg, jcbh);
            }
            if (!json.has("data")) {
                System.err.println("API\u8fd4\u56de\u683c\u5f0f\u9519\u8bef: \u7f3a\u5c11\u6570\u636e\u5b57\u6bb5");
                return this.createErrorEntry(jcbh, "API\u8fd4\u56de\u683c\u5f0f\u9519\u8bef: \u7f3a\u5c11\u6570\u636e\u5b57\u6bb5");
            }
            JSONArray dataArray = json.getJSONArray("data");
            System.out.println("\u6570\u636e\u6570\u7ec4\u957f\u5ea6: " + dataArray.length());
            if (dataArray.length() == 0) {
                System.out.println("\u67e5\u8be2\u7ed3\u679c\u4e3a\u7a7a");
                return this.createNoDataEntry(jcbh);
            }
            this.logTotalInfo(json);
            this.extractAndSaveHeaders(dataArray);
            for (int i = 0; i < dataArray.length(); ++i) {
                JSONObject rowData = dataArray.getJSONObject(i);
                WarehouseEntry entry = this.convertJsonToWarehouseEntry(rowData, jcbh);
                entries.add(entry);
            }
            System.out.println("\u6210\u529f\u89e3\u6790 " + entries.size() + " \u6761\u8bb0\u5f55");
        }
        catch (Exception e) {
            System.err.println("\u89e3\u6790JSON\u54cd\u5e94\u5931\u8d25: " + e.getMessage());
            e.printStackTrace();
            return this.createErrorEntry(jcbh, "\u89e3\u6790\u54cd\u5e94\u5931\u8d25: " + e.getMessage());
        }
        return entries;
    }

    private JSONObject validateAndParseJson(String jsonResponse, String jcbh) {
        try {
            return new JSONObject(jsonResponse);
        }
        catch (Exception e) {
            System.err.println("JSON\u89e3\u6790\u5931\u8d25: " + e.getMessage());
            System.err.println("\u54cd\u5e94\u5185\u5bb9: " + jsonResponse);
            return null;
        }
    }

    private List<WarehouseEntry> handleApiError(int code, String msg, String jcbh) {
        System.err.println("API\u8fd4\u56de\u9519\u8bef\u7801: " + code + ", \u6d88\u606f: " + msg);
        Object errorMessage = msg.contains("\u65f6\u95f4") || msg.contains("\u65e5\u671f") || msg.contains("180\u5929") ? "\u3010\u65e5\u671f\u8303\u56f4\u9519\u8bef\u3011: " + msg + "\u3002\n\u8bf7\u7f29\u77ed\u67e5\u8be2\u65f6\u95f4\u8303\u56f4\uff08\u6700\u591a180\u5929\uff09\u540e\u91cd\u8bd5\u3002" : (msg.contains("\u53c2\u6570\u9519\u8bef") || code == 1 ? "\u3010\u53c2\u6570\u9519\u8bef\u3011: \u53ef\u80fd\u539f\u56e0\uff1a\n1. \u8fdb\u4ed3\u7f16\u53f7\u5728\u7cfb\u7edf\u4e2d\u4e0d\u5b58\u5728\n2. \u67e5\u8be2\u65f6\u95f4\u8303\u56f4\u5185\u65e0\u6b64\u7f16\u53f7\u8bb0\u5f55\n3. \u8bf7\u6c42\u683c\u5f0f\u6709\u8bef\u6216\u670d\u52a1\u5668\u6682\u65f6\u65e0\u6cd5\u54cd\u5e94\n\n\u8bf7\u68c0\u67e5\u8fdb\u4ed3\u7f16\u53f7\u6216\u8c03\u6574\u67e5\u8be2\u6761\u4ef6\u540e\u91cd\u8bd5\u3002" : (msg.contains("\u8fdb\u4ed3\u7f16\u53f7") || msg.contains("jcbh") ? "\u3010\u8fdb\u4ed3\u7f16\u53f7\u9519\u8bef\u3011: " + msg + "\u3002\n\u8bf7\u68c0\u67e5\u8fdb\u4ed3\u7f16\u53f7\u662f\u5426\u6b63\u786e\u3002" : "\u3010API\u9519\u8bef\u3011: " + msg));
        return this.createErrorEntry(jcbh, (String)errorMessage);
    }

    private List<WarehouseEntry> createErrorEntry(String jcbh, String errorMessage) {
        ArrayList<WarehouseEntry> entries = new ArrayList<WarehouseEntry>();
        WarehouseEntry errorEntry = new WarehouseEntry();
        errorEntry.setJcbh(jcbh);
        errorEntry.setBz(errorMessage);
        entries.add(errorEntry);
        return entries;
    }

    private List<WarehouseEntry> createNoDataEntry(String jcbh) {
        System.out.println("\u67e5\u8be2\u7ed3\u679c\u4e3a\u7a7a: \u8fdb\u4ed3\u7f16\u53f7 " + jcbh + " \u5728\u6240\u9009\u65f6\u95f4\u8303\u56f4\u548c\u72b6\u6001\u4e0b\u6ca1\u6709\u8bb0\u5f55");
        ArrayList<WarehouseEntry> entries = new ArrayList<WarehouseEntry>();
        WarehouseEntry noDataEntry = new WarehouseEntry();
        noDataEntry.setJcbh(jcbh);
        noDataEntry.setBz("\u5728\u6240\u9009\u65f6\u95f4\u8303\u56f4\u548c\u72b6\u6001\u4e0b\u6ca1\u6709\u627e\u5230\u8be5\u8fdb\u4ed3\u7f16\u53f7\u7684\u8bb0\u5f55");
        entries.add(noDataEntry);
        return entries;
    }

    private void logTotalInfo(JSONObject json) {
        if (json.has("totalRow")) {
            JSONObject totalRow = json.getJSONObject("totalRow");
            System.out.println("\u67e5\u8be2\u7ed3\u679c\u6c47\u603b: \u603b\u4ef6\u6570=" + totalRow.optString("js", "0") + ", \u603b\u4f53\u79ef=" + totalRow.optString("tj", "0.0") + ", \u603b\u6bdb\u91cd=" + totalRow.optString("mz", "0.0"));
        }
    }

    private void extractAndSaveHeaders(JSONArray dataArray) {
        if (dataArray.length() > 0) {
            ArrayList<String> headers = new ArrayList<String>();
            JSONObject firstRow = dataArray.getJSONObject(0);
            for (String key : firstRow.keySet()) {
                headers.add(key);
            }
            this.saveTableHeaders(headers);
            System.out.println("\u63d0\u53d6\u5230\u8868\u5934: " + String.join((CharSequence)", ", headers));
        }
    }

    private WarehouseEntry convertJsonToWarehouseEntry(JSONObject json, String jcbh) {
        WarehouseEntry entry = new WarehouseEntry();
        try {
            String ownerName = this.firstNonBlank(json.optString("hz", ""), json.optString("hz2", ""));
            entry.setJcid(json.optString("jcid", ""));
            entry.setInguid(this.firstNonBlank(json.optString("inguid", ""), json.optString("jcid", "")));
            entry.setZyh(json.optString("zyh", ""));
            entry.setJcrq(json.optString("jcrq", ""));
            entry.setJcbh(json.optString("jcbh", jcbh));
            entry.setHz(ownerName);
            entry.setMt(json.optString("mt", ""));
            entry.setYjjs(this.parseIntSafely(json.optString("yjjs", "0")));
            entry.setYjtj(this.parseDoubleSafely(json.optString("yjtj", "0.0")));
            entry.setYjmz(this.parseDoubleSafely(json.optString("yjmz", "0.0")));
            entry.setJs(this.parseIntSafely(json.optString("js", "0")));
            entry.setTj(this.parseDoubleSafely(json.optString("tj", "0.0")));
            entry.setMz(this.parseDoubleSafely(json.optString("mz", "0.0")));
            entry.setKcjs(this.parseDoubleSafely(json.optString("kcjs", "0.0")));
            entry.setKctj(this.parseDoubleSafely(json.optString("kctj", "0.0")));
            entry.setKcmz(this.parseDoubleSafely(json.optString("kcmz", "0.0")));
            entry.setBzgg(json.optString("bzgg", ""));
            entry.setHwmc(json.optString("hwmc", ""));
            entry.setXhrq(json.optString("xhrq", ""));
            entry.setXhrq2(json.optString("xhrq2", ""));
            entry.setYjrq(json.optString("yjrq", ""));
            entry.setLf(json.optString("lf", ""));
            entry.setHwmc1(json.optString("hwmc1", ""));
            entry.setCh(json.optString("ch", ""));
            entry.setDriverdh(json.optString("driverdh", ""));
            entry.setCleng(json.optString("cleng", ""));
            entry.setChengzhong(json.optString("chengzhong", ""));
            entry.setYyh(json.optString("yyh", ""));
            entry.setBz2(json.optString("bz2", ""));
            entry.setHdmc(json.optString("hdmc", ""));
            entry.setFgsmc(json.optString("fgsmc", ""));
            entry.setJczyh(json.optString("zyh", ""));
            entry.setShdw(ownerName);
            entry.setHh(json.optString("hh", ""));
            entry.setTs(this.parseIntSafely(json.optString("ts", "0")));
            entry.setBgzt(json.optString("bgzt", ""));
            entry.setSrrq(json.optString("srrq", ""));
            entry.setHd(json.optString("hdmc", ""));
            entry.setJsy(json.optString("jsy", ""));
            entry.setJsydh(json.optString("jsydh", json.optString("driverdh", "")));
            entry.setYqqdsj(json.optString("yqqdsj", ""));
            StringBuilder bzBuilder = new StringBuilder();
            String yyjcbz = json.optString("yyjcbz", "");
            String bz = json.optString("bz", "");
            String bz2 = json.optString("bz2", "");
            if (!yyjcbz.isEmpty()) {
                bzBuilder.append(yyjcbz);
            }
            if (!bz.isEmpty() && !bz.equals(yyjcbz)) {
                if (bzBuilder.length() > 0) {
                    bzBuilder.append(" | ");
                }
                bzBuilder.append(bz);
            }
            if (!(bz2.isEmpty() || bz2.equals(yyjcbz) || bz2.equals(bz))) {
                if (bzBuilder.length() > 0) {
                    bzBuilder.append(" | ");
                }
                bzBuilder.append(bz2);
            }
            entry.setBz(bzBuilder.toString());
            System.out.println("\u6570\u636e\u8f6c\u6362 - \u8fdb\u4ed3\u7f16\u53f7:" + entry.getJcbh() + " \u4f5c\u4e1a\u53f7:" + entry.getZyh() + " \u8d27\u4e3b:" + entry.getHz() + " \u8d27\u7269:" + entry.getHwmc() + " \u4ef6\u6570:" + entry.getJs() + " \u4f53\u79ef:" + entry.getTj() + " \u6bdb\u91cd:" + entry.getMz());
        }
        catch (Exception e) {
            System.err.println("\u6570\u636e\u8f6c\u6362\u5931\u8d25: " + e.getMessage());
            e.printStackTrace();
            entry.setJcbh(jcbh);
            entry.setBz("\u8f6c\u6362\u9519\u8bef: " + e.getMessage());
        }
        return entry;
    }

    private boolean isLikelyPhoneNumber(String str) {
        if (str.length() == 1 && Character.isDigit(str.charAt(0))) {
            return false;
        }
        int digitCount = 0;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) continue;
            ++digitCount;
        }
        return (double)digitCount / (double)str.length() > 0.7;
    }

    private String firstNonBlank(String ... values) {
        for (String value : values) {
            if (value == null) continue;
            String trimmed = value.trim();
            if (trimmed.isEmpty()) continue;
            return trimmed;
        }
        return "";
    }

    private int parseIntSafely(String value) {
        try {
            return Integer.parseInt(value.trim());
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleSafely(String value) {
        try {
            return Double.parseDouble(value.trim());
        }
        catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void saveTableHeaders(List<String> headers) {
        extractedTableHeaders = new ArrayList<String>(headers);
    }

    public static List<String> getExtractedTableHeaders() {
        return new ArrayList<String>(extractedTableHeaders);
    }

    public static void clearExtractedTableHeaders() {
        extractedTableHeaders.clear();
    }

    public static boolean hasExtractedTableHeaders() {
        return !extractedTableHeaders.isEmpty();
    }

    public void close() {
        try {
            if (this.httpClient != null) {
                this.httpClient.close();
            }
            this.queryExecutor.shutdownNow();
        }
        catch (IOException e) {
            System.err.println("\u5173\u95edHTTP\u5ba2\u6237\u7aef\u5931\u8d25: " + e.getMessage());
        }
    }
}
