package com.warehousequery.app.service;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.WarehouseEntry;
import com.warehousequery.app.util.ExceptionHandler;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// 添加JSON解析库
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 仓库查询服务类 - 修复版本
 * 负责与仓库管理系统API的交互
 */
public class WarehouseService {
    // API基础URL和端点（严格按照示例格式）
    private static final String BASE_URL = "http://60.190.0.98:81";
    private static final String API_ENDPOINT = "/csccmisHandler/CsccmisHandler.ashx";
    
    private final CookieStore cookieStore = new BasicCookieStore();
    private final HttpClientContext context = HttpClientContext.create();
    private final CloseableHttpClient httpClient;
    
    // Cookie管理相关
    private static final long SESSION_REFRESH_INTERVAL = 1000 * 60 * 15; // 15分钟刷新一次会话
    private long lastSessionRefreshTime = 0;
    
    // 日志文件路径
    private static final String REQUEST_LOG_FILE = "request_log.txt";
    private static final String RESPONSE_LOG_FILE = "response_log.txt";
    
    // 表头信息存储
    private static List<String> extractedTableHeaders = new ArrayList<>();
    
    public WarehouseService() {
        // 配置HTTP客户端
        org.apache.http.client.config.RequestConfig config = org.apache.http.client.config.RequestConfig.custom()
                .setConnectTimeout(AppConfig.CONNECT_TIMEOUT)
                .setSocketTimeout(AppConfig.SOCKET_TIMEOUT)
                .setRedirectsEnabled(true)
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setDefaultCookieStore(cookieStore)
                .build();

        this.context.setCookieStore(cookieStore);
        
        // 初始化会话
        try {
        initSession();
        } catch (Exception e) {
            System.err.println("初始化会话失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 清空日志文件
        clearLogFiles();
    }
    
    /**
     * 清空日志文件
     */
    private void clearLogFiles() {
        try {
            Files.deleteIfExists(Paths.get(REQUEST_LOG_FILE));
            Files.deleteIfExists(Paths.get(RESPONSE_LOG_FILE));
        } catch (IOException e) {
            System.err.println("清空日志文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化会话 - 改进版本，增强错误处理
     */
    private void initSession() {
        try {
            System.out.println("正在初始化会话...");
            
            // 访问主页面获取会话
            String mainPageUrl = AppConfig.WEBSITE_URL;
            HttpGet request = new HttpGet(mainPageUrl);
            setupRequestHeaders(request);
            
            try (CloseableHttpResponse response = httpClient.execute(request, context)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("会话初始化响应状态码: " + statusCode);
                    
                    if (statusCode == 200) {
                    processSessionResponse(response);
                    lastSessionRefreshTime = System.currentTimeMillis();
                    System.out.println("会话初始化成功");
                        } else {
                    System.err.println("会话初始化失败，状态码: " + statusCode);
                }
            }
            
        } catch (Exception e) {
            System.err.println("初始化会话时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 设置请求头
     */
    private void setupRequestHeaders(HttpGet request) {
        request.setHeader("User-Agent", AppConfig.USER_AGENT);
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        request.setHeader("Accept-Encoding", "gzip, deflate");
        request.setHeader("Connection", "keep-alive");
        request.setHeader("Upgrade-Insecure-Requests", "1");
    }
    
    /**
     * 处理会话响应
     */
    private void processSessionResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            
            // 保存响应到文件用于调试
            try {
                Files.write(Paths.get("session_response.html"), 
                           responseBody.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println("保存会话响应失败: " + e.getMessage());
            }
            
            // 记录Cookie信息
            logCookies();
        }
    }
    
    /**
     * 记录Cookie信息
     */
    private void logCookies() {
        System.out.println("当前Cookie信息:");
        cookieStore.getCookies().forEach(cookie -> {
            System.out.println("  " + cookie.getName() + " = " + cookie.getValue());
        });
    }
    
    /**
     * 检查并刷新会话
     */
    private void checkAndRefreshSession() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSessionRefreshTime > SESSION_REFRESH_INTERVAL) {
            System.out.println("会话超时，正在刷新...");
            initSession();
        }
    }
    
    /**
     * 查询仓库信息 - 改进版本，增强错误处理和调试信息
     * @param jcbhList 进仓编号列表
     * @param status 状态索引
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 仓库条目列表的Future
     */
    public CompletableFuture<List<WarehouseEntry>> queryWarehouse(List<String> jcbhList, int status, String startDate, String endDate) {
        CompletableFuture<List<WarehouseEntry>> future = new CompletableFuture<>();
        
        Task<List<WarehouseEntry>> task = new Task<>() {
            @Override
            protected List<WarehouseEntry> call() throws Exception {
                List<WarehouseEntry> allEntries = new ArrayList<>();
                
                int total = jcbhList.size();
                int current = 0;
                
                // 清空日志文件，准备记录新的查询
                clearLogFiles();
                logQueryParameters(jcbhList, status, startDate, endDate);
                
                for (String jcbh : jcbhList) {
                    current++;
                    updateProgress(current, total);
                    updateMessage("正在查询: " + jcbh + " (" + current + "/" + total + ")");
                    
                    try {
                        List<WarehouseEntry> entries = querySingleJcbh(jcbh.trim(), status, startDate, endDate);
                        allEntries.addAll(entries);
                        
                        // 添加小延迟，避免请求过于频繁
                        Thread.sleep(200); // 增加延迟到200ms
                        
                    } catch (Exception e) {
                        System.err.println("查询进仓编号 " + jcbh + " 失败: " + e.getMessage());
                        e.printStackTrace();
                        
                        // 创建错误条目
                        WarehouseEntry errorEntry = new WarehouseEntry();
                        errorEntry.setJcbh(jcbh);
                        errorEntry.setBz("查询失败: " + e.getMessage());
                        allEntries.add(errorEntry);
                    }
                }
                
                updateMessage("查询完成，共找到 " + allEntries.size() + " 条记录");
                return allEntries;
            }
        };
        
        // 在后台线程中执行任务
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        
        // 将Task的结果传递给CompletableFuture
        task.setOnSucceeded(e -> future.complete(task.getValue()));
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            System.err.println("查询任务执行失败: " + exception.getMessage());
            exception.printStackTrace();
            future.completeExceptionally(exception);
        });
        
        return future;
    }
    
    /**
     * 记录查询参数到日志
     */
    private void logQueryParameters(List<String> jcbhList, int status, String startDate, String endDate) {
        try {
            StringBuilder requestLog = new StringBuilder();
            requestLog.append("=== 查询参数 ===\n");
            requestLog.append("查询时间: ").append(java.time.LocalDateTime.now()).append("\n");
            requestLog.append("进仓编号列表: ").append(String.join(", ", jcbhList)).append("\n");
            requestLog.append("状态: ").append(AppConfig.getStatusName(status)).append(" (").append(status).append(")\n");
            requestLog.append("状态代码: ").append(AppConfig.getStatusCode(status)).append("\n");
            requestLog.append("日期范围: ").append(startDate).append(" 至 ").append(endDate).append("\n\n");
            
            Files.write(
                Paths.get(AppConfig.REQUEST_LOG_FILE),
                requestLog.toString().getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            System.err.println("记录查询参数失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询单个进仓编号 - 改进版本，增强调试信息
     */
    private List<WarehouseEntry> querySingleJcbh(String jcbh, int status, String startDate, String endDate) throws IOException {
        System.out.println("开始查询进仓编号: " + jcbh);
        
        // 检查并刷新会话
        checkAndRefreshSession();
        
        // 构建查询参数
        String zt = AppConfig.getStatusCode(status);
        System.out.println("使用状态代码: " + zt + " (状态: " + AppConfig.getStatusName(status) + ")");
        
        String methodParameter = buildMethodParameter(jcbh, zt, startDate, endDate);
        System.out.println("构建的方法参数: " + methodParameter);
        
        // 构建完整的查询URL
        String queryUrl = buildQueryUrl(methodParameter);
        System.out.println("查询URL: " + queryUrl);
        
        // 记录请求信息
        logRequest(queryUrl, jcbh);
        
        // 发送HTTP请求
        HttpGet request = new HttpGet(queryUrl);
        setupApiRequestHeaders(request);
        
        try (CloseableHttpResponse response = httpClient.execute(request, context)) {
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("响应状态码: " + statusCode);
            
            if (statusCode == 200) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println("响应内容长度: " + responseBody.length());
                System.out.println("响应内容前200字符: " + 
                    (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));
                
                logResponse(responseBody, jcbh);
                return parseJsonResponse(responseBody, jcbh);
            } else {
                String errorMsg = "HTTP请求失败，状态码: " + statusCode;
                System.err.println(errorMsg);
                
                // 尝试读取错误响应内容
                try {
                    String errorResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    System.err.println("错误响应内容: " + errorResponse);
                    logResponse("错误响应 (状态码: " + statusCode + "): " + errorResponse, jcbh);
                } catch (Exception e) {
                    System.err.println("读取错误响应失败: " + e.getMessage());
                }
                
                throw new IOException(errorMsg);
            }
        } catch (Exception e) {
            System.err.println("执行HTTP请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("请求执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建方法参数JSON字符串 - 改进版本，增强调试信息
     */
    private String buildMethodParameter(String jcbh, String zt, String startDate, String endDate) {
        try {
            JSONObject params = new JSONObject();
            params.put("jcbh", jcbh);
            params.put("zt", zt);
            params.put("jcrq1", startDate);
            params.put("jcrq2", endDate);
            
            String jsonString = params.toString();
            System.out.println("原始JSON参数: " + jsonString);
            
            String encoded = URLEncoder.encode(jsonString, StandardCharsets.UTF_8.name());
            System.out.println("URL编码后参数: " + encoded);
            
            return encoded;
        } catch (Exception e) {
            System.err.println("构建查询参数失败: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * 构建查询URL
     */
    private String buildQueryUrl(String methodParameter) {
        return String.format("%s?page=%d&limit=%d&CallMethod=QueryJcInfo&MethodParameter=%s",
                AppConfig.getApiUrl(),
                AppConfig.DEFAULT_PAGE_NUMBER,
                AppConfig.DEFAULT_PAGE_SIZE,
                methodParameter);
    }
    
    /**
     * 设置API请求头 - 改进版本
     */
    private void setupApiRequestHeaders(HttpGet request) {
        request.setHeader("User-Agent", AppConfig.USER_AGENT);
        request.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        request.setHeader("X-Requested-With", "XMLHttpRequest");
        request.setHeader("Referer", AppConfig.WEBSITE_URL);
        request.setHeader("Accept-Encoding", "gzip, deflate");
        request.setHeader("Connection", "keep-alive");
        
        // 打印请求头信息用于调试
        System.out.println("请求头信息:");
        request.getAllHeaders();
        for (var header : request.getAllHeaders()) {
            System.out.println("  " + header.getName() + ": " + header.getValue());
        }
    }
    
    /**
     * 记录请求信息
     */
    private void logRequest(String url, String jcbh) {
        try {
            String logEntry = String.format("=== 请求 %s ===\n时间: %s\nURL: %s\n\n",
                    jcbh, Instant.now(), url);
            
            Files.write(
                Paths.get(AppConfig.REQUEST_LOG_FILE),
                logEntry.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
                } catch (IOException e) {
            System.err.println("记录请求日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录响应信息
     */
    private void logResponse(String response, String jcbh) {
        try {
            String logEntry = String.format("=== 响应 %s ===\n时间: %s\n内容: %s\n\n",
                    jcbh, Instant.now(), response);
            
            Files.write(
                Paths.get(AppConfig.RESPONSE_LOG_FILE),
                logEntry.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("记录响应日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析JSON响应数据 - 改进版本，增强错误处理和调试信息
     */
    private List<WarehouseEntry> parseJsonResponse(String jsonResponse, String jcbh) {
        List<WarehouseEntry> entries = new ArrayList<>();
        
        try {
            System.out.println("开始解析JSON响应...");
            
            // 验证JSON格式
            JSONObject json = validateAndParseJson(jsonResponse, jcbh);
            if (json == null) {
                return createErrorEntry(jcbh, "服务器返回了无效的JSON格式");
            }
            
            System.out.println("JSON解析成功，检查返回码...");
            
            // 检查API返回码
            int code = json.optInt("code", -1);
            System.out.println("API返回码: " + code);
            
            if (code != 0) {
                String msg = json.optString("msg", "未知错误");
                System.err.println("API返回错误: " + msg);
                return handleApiError(code, msg, jcbh);
            }
            
            // 处理数据数组
            if (!json.has("data")) {
                System.err.println("API返回格式错误: 缺少数据字段");
                return createErrorEntry(jcbh, "API返回格式错误: 缺少数据字段");
            }
            
            JSONArray dataArray = json.getJSONArray("data");
            System.out.println("数据数组长度: " + dataArray.length());
            
            // 处理空结果
            if (dataArray.length() == 0) {
                System.out.println("查询结果为空");
                return createNoDataEntry(jcbh);
            }
            
            // 记录汇总信息
            logTotalInfo(json);
            
            // 提取并保存表头信息
            extractAndSaveHeaders(dataArray);
            
            // 转换数据
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject rowData = dataArray.getJSONObject(i);
                WarehouseEntry entry = convertJsonToWarehouseEntry(rowData, jcbh);
                entries.add(entry);
            }
            
            System.out.println("成功解析 " + entries.size() + " 条记录");
            
        } catch (Exception e) {
            System.err.println("解析JSON响应失败: " + e.getMessage());
            e.printStackTrace();
            return createErrorEntry(jcbh, "解析响应失败: " + e.getMessage());
        }
        
        return entries;
    }
    
    /**
     * 验证并解析JSON
     */
    private JSONObject validateAndParseJson(String jsonResponse, String jcbh) {
        try {
            return new JSONObject(jsonResponse);
        } catch (Exception e) {
            System.err.println("JSON解析失败: " + e.getMessage());
            System.err.println("响应内容: " + jsonResponse);
            return null;
        }
    }
    
    /**
     * 处理API错误
     */
    private List<WarehouseEntry> handleApiError(int code, String msg, String jcbh) {
        System.err.println("API返回错误码: " + code + ", 消息: " + msg);
        
        String errorMessage;
        if (msg.contains("时间") || msg.contains("日期") || msg.contains("180天")) {
            errorMessage = "【日期范围错误】: " + msg + "。\n请缩短查询时间范围（最多180天）后重试。";
        } else if (msg.contains("参数错误") || code == 1) {
            errorMessage = "【参数错误】: 可能原因：\n" +
                          "1. 进仓编号在系统中不存在\n" +
                          "2. 查询时间范围内无此编号记录\n" +
                          "3. 请求格式有误或服务器暂时无法响应\n\n" +
                          "请检查进仓编号或调整查询条件后重试。";
        } else if (msg.contains("进仓编号") || msg.contains("jcbh")) {
            errorMessage = "【进仓编号错误】: " + msg + "。\n请检查进仓编号是否正确。";
        } else {
            errorMessage = "【API错误】: " + msg;
        }
        
        return createErrorEntry(jcbh, errorMessage);
    }
    
    /**
     * 创建错误条目
     */
    private List<WarehouseEntry> createErrorEntry(String jcbh, String errorMessage) {
        List<WarehouseEntry> entries = new ArrayList<>();
        WarehouseEntry errorEntry = new WarehouseEntry();
        errorEntry.setJcbh(jcbh);
        errorEntry.setBz(errorMessage);
        entries.add(errorEntry);
        return entries;
    }
    
    /**
     * 创建无数据条目
     */
    private List<WarehouseEntry> createNoDataEntry(String jcbh) {
        System.out.println("查询结果为空: 进仓编号 " + jcbh + " 在所选时间范围和状态下没有记录");
        List<WarehouseEntry> entries = new ArrayList<>();
        WarehouseEntry noDataEntry = new WarehouseEntry();
        noDataEntry.setJcbh(jcbh);
        noDataEntry.setBz("在所选时间范围和状态下没有找到该进仓编号的记录");
        entries.add(noDataEntry);
        return entries;
    }
    
    /**
     * 记录汇总信息
     */
    private void logTotalInfo(JSONObject json) {
        if (json.has("totalRow")) {
            JSONObject totalRow = json.getJSONObject("totalRow");
            System.out.println("查询结果汇总: 总件数=" + totalRow.optString("js", "0") + 
                               ", 总体积=" + totalRow.optString("tj", "0.0") + 
                               ", 总毛重=" + totalRow.optString("mz", "0.0"));
        }
    }
    
    /**
     * 提取并保存表头信息
     */
    private void extractAndSaveHeaders(JSONArray dataArray) {
        if (dataArray.length() > 0) {
            List<String> headers = new ArrayList<>();
            JSONObject firstRow = dataArray.getJSONObject(0);
            for (String key : firstRow.keySet()) {
                headers.add(key);
            }
            saveTableHeaders(headers);
            System.out.println("提取到表头: " + String.join(", ", headers));
        }
    }
    
    /**
     * 将JSON对象转换为WarehouseEntry对象 - 完整30个字段版本
     * 字段顺序：jcid, zyh, jcrq, jcbh, hz, mt, yjjs, yjtj, yjmz, js, tj, mz, kcjs, kctj, kcmz, bzgg, hwmc, xhrq, xhrq2, yjrq, lf, hwmc1, ch, driverdh, cleng, chengzhong, yyh, bz2, hdmc, fgsmc
     */
    private WarehouseEntry convertJsonToWarehouseEntry(JSONObject json, String jcbh) {
        WarehouseEntry entry = new WarehouseEntry();
        
        try {
            // 按照30个字段的顺序进行映射
            entry.setJcid(json.optString("jcid", "")); // 进仓ID
            entry.setZyh(json.optString("zyh", "")); // 作业号
            entry.setJcrq(json.optString("jcrq", "")); // 进仓日期
            entry.setJcbh(json.optString("jcbh", jcbh)); // 进仓编号
            entry.setHz(json.optString("hz", "")); // 货主
            entry.setMt(json.optString("mt", "")); // 唛头
            entry.setYjjs(parseIntSafely(json.optString("yjjs", "0"))); // 预计件数
            entry.setYjtj(parseDoubleSafely(json.optString("yjtj", "0.0"))); // 预计体积
            entry.setYjmz(parseDoubleSafely(json.optString("yjmz", "0.0"))); // 预计毛重
            entry.setJs(parseIntSafely(json.optString("js", "0"))); // 件数
            entry.setTj(parseDoubleSafely(json.optString("tj", "0.0"))); // 体积
            entry.setMz(parseDoubleSafely(json.optString("mz", "0.0"))); // 毛重
            entry.setKcjs(parseDoubleSafely(json.optString("kcjs", "0.0"))); // 库存件数
            entry.setKctj(parseDoubleSafely(json.optString("kctj", "0.0"))); // 库存体积
            entry.setKcmz(parseDoubleSafely(json.optString("kcmz", "0.0"))); // 库存毛重
            entry.setBzgg(json.optString("bzgg", "")); // 包装规格
            entry.setHwmc(json.optString("hwmc", "")); // 货物名称
            entry.setXhrq(json.optString("xhrq", "")); // 卸货日期
            entry.setXhrq2(json.optString("xhrq2", "")); // 卸货完成
            entry.setYjrq(json.optString("yjrq", "")); // 预进日期
            entry.setLf(json.optString("lf", "")); // L/F
            entry.setHwmc1(json.optString("hwmc1", "")); // 货物名称1
            entry.setCh(json.optString("ch", "")); // 车号
            entry.setDriverdh(json.optString("driverdh", "")); // 司机电话
            entry.setCleng(json.optString("cleng", "")); // 车长
            entry.setChengzhong(json.optString("chengzhong", "")); // 承重
            entry.setYyh(json.optString("yyh", "")); // 运单号
            entry.setBz2(json.optString("bz2", "")); // 备注
            entry.setHdmc(json.optString("hdmc", "")); // 货代名称
            entry.setFgsmc(json.optString("fgsmc", "")); // 分公司
            
            // 兼容性字段映射（保持向后兼容）
            entry.setJczyh(json.optString("zyh", "")); // 作业号（兼容旧字段名）
            entry.setShdw(json.optString("hz", "")); // 送货单位（使用货主字段）
            entry.setHh(json.optString("hh", "")); // 货号
            entry.setTs(parseIntSafely(json.optString("ts", "0"))); // 托数
            entry.setBgzt(json.optString("bgzt", "")); // 报关状态
            
            // 预约进仓专用字段
            entry.setSrrq(json.optString("srrq", "")); // 录入日期
            entry.setHd(json.optString("hdmc", "")); // 货代
            entry.setJsy(json.optString("jsy", "")); // 司机
            entry.setJsydh(json.optString("jsydh", json.optString("driverdh", ""))); // 司机电话
            entry.setYqqdsj(json.optString("yqqdsj", "")); // 要求取单时间
            
            // 备注字段处理
            StringBuilder bzBuilder = new StringBuilder();
            String yyjcbz = json.optString("yyjcbz", "");
            String bz = json.optString("bz", "");
            String bz2 = json.optString("bz2", "");
            
            if (!yyjcbz.isEmpty()) {
                bzBuilder.append(yyjcbz);
            }
            if (!bz.isEmpty() && !bz.equals(yyjcbz)) {
                if (bzBuilder.length() > 0) bzBuilder.append(" | ");
                bzBuilder.append(bz);
            }
            if (!bz2.isEmpty() && !bz2.equals(yyjcbz) && !bz2.equals(bz)) {
                if (bzBuilder.length() > 0) bzBuilder.append(" | ");
                bzBuilder.append(bz2);
            }
            
            entry.setBz(bzBuilder.toString());
            
            // 输出调试信息
            System.out.println("数据转换 - 进仓编号:" + entry.getJcbh() + 
                             " 作业号:" + entry.getZyh() + 
                             " 货主:" + entry.getHz() + 
                             " 货物:" + entry.getHwmc() + 
                             " 件数:" + entry.getJs() + 
                             " 体积:" + entry.getTj() + 
                             " 毛重:" + entry.getMz());
            
        } catch (Exception e) {
            System.err.println("数据转换失败: " + e.getMessage());
            e.printStackTrace();
            entry.setJcbh(jcbh);
            entry.setBz("转换错误: " + e.getMessage());
        }
        
        return entry;
    }
    
    /**
     * 判断字符串是否像电话号码
     */
    private boolean isLikelyPhoneNumber(String str) {
        // 如果字符串只有1个字符且是数字，可能是占位符而不是真正的电话号码
        if (str.length() == 1 && Character.isDigit(str.charAt(0))) {
            return false;
        }
        
        // 计算数字字符的比例
        int digitCount = 0;
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                digitCount++;
            }
        }
        
        // 如果数字比例超过70%，认为是电话号码
        return (double) digitCount / str.length() > 0.7;
    }
    
    /**
     * 安全解析整数
     */
    private int parseIntSafely(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 安全解析双精度浮点数
     */
    private double parseDoubleSafely(String value) {
        try {
            return Double.parseDouble(value.trim());
                } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    // 表头管理方法
    private void saveTableHeaders(List<String> headers) {
        extractedTableHeaders = new ArrayList<>(headers);
    }
    
    public static List<String> getExtractedTableHeaders() {
        return new ArrayList<>(extractedTableHeaders);
    }
    
    public static void clearExtractedTableHeaders() {
        extractedTableHeaders.clear();
    }
    
    public static boolean hasExtractedTableHeaders() {
        return !extractedTableHeaders.isEmpty();
    }
    
    /**
     * 关闭HTTP客户端
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            System.err.println("关闭HTTP客户端失败: " + e.getMessage());
        }
    }
} 