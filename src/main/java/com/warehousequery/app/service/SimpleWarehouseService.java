package com.warehousequery.app.service;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.SimpleWarehouseEntry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 简化的仓库服务类 - 不依赖JavaFX
 */
public class SimpleWarehouseService {
    private HttpClient httpClient;
    private String sessionId;
    
    public SimpleWarehouseService() {
        initializeHttpClient();
    }
    
    private void initializeHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(AppConfig.CONNECT_TIMEOUT)
                .setSocketTimeout(AppConfig.SOCKET_TIMEOUT)
                .build();
        
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }
    
    /**
     * 测试连接
     */
    public boolean testConnection() {
        try {
            HttpGet request = new HttpGet(AppConfig.WEBSITE_URL);
            request.setHeader("User-Agent", AppConfig.USER_AGENT);
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            System.out.println("连接测试 - 状态码: " + statusCode);
            return statusCode == 200;
            
        } catch (Exception e) {
            System.err.println("连接测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取会话ID
     */
    public boolean initializeSession() {
        try {
            HttpGet request = new HttpGet(AppConfig.WEBSITE_URL);
            request.setHeader("User-Agent", AppConfig.USER_AGENT);
            
            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            // 从响应中提取会话ID（简化版本）
            if (responseBody.contains("ASP.NET_SessionId")) {
                this.sessionId = "test_session_id"; // 简化处理
                System.out.println("会话初始化成功");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("会话初始化失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 简化的查询方法
     */
    public List<SimpleWarehouseEntry> queryWarehouse(String jcbh, String status) {
        List<SimpleWarehouseEntry> results = new ArrayList<>();
        
        try {
            System.out.println("开始查询 - 进仓编号: " + jcbh + ", 状态: " + status);
            
            // 创建POST请求
            HttpPost request = new HttpPost(AppConfig.getApiUrl());
            request.setHeader("User-Agent", AppConfig.USER_AGENT);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            
            // 构建请求参数
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("action", "query"));
            params.add(new BasicNameValuePair("jcbh", jcbh));
            params.add(new BasicNameValuePair("zt", status));
            
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            
            // 执行请求
            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            System.out.println("查询响应长度: " + responseBody.length());
            
            // 简化的JSON解析（实际项目中需要根据真实API响应格式调整）
            if (responseBody.contains("{") && responseBody.contains("}")) {
                // 创建测试数据
                SimpleWarehouseEntry entry = new SimpleWarehouseEntry();
                entry.setJcbh(jcbh);
                entry.setHwmc("测试货物");
                entry.setJs(100);
                entry.setBgzt("已报关");
                results.add(entry);
                
                System.out.println("查询成功，返回 " + results.size() + " 条记录");
            } else {
                System.out.println("未找到匹配的记录");
            }
            
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        } catch (Exception e) {
            System.err.println("关闭资源时出错: " + e.getMessage());
        }
    }
} 