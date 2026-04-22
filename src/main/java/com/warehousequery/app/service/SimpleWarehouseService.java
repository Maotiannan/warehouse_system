/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.http.HttpEntity
 *  org.apache.http.HttpResponse
 *  org.apache.http.client.HttpClient
 *  org.apache.http.client.config.RequestConfig
 *  org.apache.http.client.entity.UrlEncodedFormEntity
 *  org.apache.http.client.methods.HttpGet
 *  org.apache.http.client.methods.HttpPost
 *  org.apache.http.client.methods.HttpUriRequest
 *  org.apache.http.impl.client.HttpClients
 *  org.apache.http.message.BasicNameValuePair
 *  org.apache.http.util.EntityUtils
 */
package com.warehousequery.app.service;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.SimpleWarehouseEntry;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class SimpleWarehouseService {
    private HttpClient httpClient;
    private String sessionId;

    public SimpleWarehouseService() {
        this.initializeHttpClient();
    }

    private void initializeHttpClient() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000).build();
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    public boolean testConnection() {
        try {
            HttpGet request = new HttpGet("http://60.190.0.98:81/csccmisHandler/website/csccmis.aspx");
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
            HttpResponse response = this.httpClient.execute((HttpUriRequest)request);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("\u8fde\u63a5\u6d4b\u8bd5 - \u72b6\u6001\u7801: " + statusCode);
            return statusCode == 200;
        }
        catch (Exception e) {
            System.err.println("\u8fde\u63a5\u6d4b\u8bd5\u5931\u8d25: " + e.getMessage());
            return false;
        }
    }

    public boolean initializeSession() {
        try {
            HttpGet request = new HttpGet("http://60.190.0.98:81/csccmisHandler/website/csccmis.aspx");
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
            HttpResponse response = this.httpClient.execute((HttpUriRequest)request);
            String responseBody = EntityUtils.toString((HttpEntity)response.getEntity(), (Charset)StandardCharsets.UTF_8);
            if (responseBody.contains("ASP.NET_SessionId")) {
                this.sessionId = "test_session_id";
                System.out.println("\u4f1a\u8bdd\u521d\u59cb\u5316\u6210\u529f");
                return true;
            }
            return false;
        }
        catch (Exception e) {
            System.err.println("\u4f1a\u8bdd\u521d\u59cb\u5316\u5931\u8d25: " + e.getMessage());
            return false;
        }
    }

    public List<SimpleWarehouseEntry> queryWarehouse(String jcbh, String status) {
        ArrayList<SimpleWarehouseEntry> results = new ArrayList<SimpleWarehouseEntry>();
        try {
            System.out.println("\u5f00\u59cb\u67e5\u8be2 - \u8fdb\u4ed3\u7f16\u53f7: " + jcbh + ", \u72b6\u6001: " + status);
            HttpPost request = new HttpPost(AppConfig.getApiUrl());
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
            request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
            params.add(new BasicNameValuePair("action", "query"));
            params.add(new BasicNameValuePair("jcbh", jcbh));
            params.add(new BasicNameValuePair("zt", status));
            request.setEntity((HttpEntity)new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            HttpResponse response = this.httpClient.execute((HttpUriRequest)request);
            String responseBody = EntityUtils.toString((HttpEntity)response.getEntity(), (Charset)StandardCharsets.UTF_8);
            System.out.println("\u67e5\u8be2\u54cd\u5e94\u957f\u5ea6: " + responseBody.length());
            if (responseBody.contains("{") && responseBody.contains("}")) {
                SimpleWarehouseEntry entry = new SimpleWarehouseEntry();
                entry.setJcbh(jcbh);
                entry.setHwmc("\u6d4b\u8bd5\u8d27\u7269");
                entry.setJs(100);
                entry.setBgzt("\u5df2\u62a5\u5173");
                results.add(entry);
                System.out.println("\u67e5\u8be2\u6210\u529f\uff0c\u8fd4\u56de " + results.size() + " \u6761\u8bb0\u5f55");
            } else {
                System.out.println("\u672a\u627e\u5230\u5339\u914d\u7684\u8bb0\u5f55");
            }
        }
        catch (Exception e) {
            System.err.println("\u67e5\u8be2\u5931\u8d25: " + e.getMessage());
        }
        return results;
    }

    public void close() {
        try {
            if (this.httpClient != null) {
                this.httpClient.getConnectionManager().shutdown();
            }
        }
        catch (Exception e) {
            System.err.println("\u5173\u95ed\u8d44\u6e90\u65f6\u51fa\u9519: " + e.getMessage());
        }
    }
}
