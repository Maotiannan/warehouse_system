/*
 * Decompiled with CFR 0.152.
 */
package com.warehousequery.app.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AppConfig {
    public static final String BASE_URL = "http://60.190.0.98:81";
    public static final String API_ENDPOINT = "/csccmisHandler/CsccmisHandler.ashx";
    public static final String WEBSITE_URL = "http://60.190.0.98:81/csccmisHandler/website/csccmis.aspx";
    public static final long SESSION_REFRESH_INTERVAL = 900000L;
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int RETRY_DELAY_MS = 1000;
    public static final int CONNECT_TIMEOUT = 30000;
    public static final int SOCKET_TIMEOUT = 30000;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    public static final String REQUEST_LOG_FILE = "request_log.txt";
    public static final String RESPONSE_LOG_FILE = "response_log.txt";
    public static final String SESSION_RESPONSE_FILE = "session_response.html";
    public static final String LOGS_DIR = "logs";
    public static final String APP_TITLE = "\u4ed3\u5e93\u67e5\u8be2\u7cfb\u7edf";
    public static final double DEFAULT_WINDOW_WIDTH = 1000.0;
    public static final double DEFAULT_WINDOW_HEIGHT = 600.0;
    public static final int DEFAULT_PAGE_SIZE = 200;
    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final long MAX_DATE_RANGE_DAYS = 180L;
    public static final String[] STATUS_NAMES = new String[]{"\u9884\u7ea6\u8fdb\u4ed3", "\u8fdb\u4ed3", "\u5e93\u5b58", "\u51fa\u4ed3"};
    public static final String[] STATUS_CODES = new String[]{"0", "1", "2", "3"};
    public static final String COLUMN_WIDTH_KEY_PREFIX = "column_width_";
    public static final String COLUMN_ORDER_KEY_PREFIX = "column_order_";
    public static final String COLUMN_VISIBLE_KEY_PREFIX = "column_visible_";
    public static final String PERSONAL_COLUMN_WIDTH_KEY_PREFIX = "personal_column_width_";
    public static final String PERSONAL_COLUMN_ORDER_KEY_PREFIX = "personal_column_order_";
    public static final String PERSONAL_COLUMN_VISIBLE_KEY_PREFIX = "personal_column_visible_";
    public static final String COLUMN_CONFIG_JSON_KEY_PREFIX = "column_config_json_";
    public static final int COLUMN_CONFIG_VERSION = 1;
    public static final String PREF_WINDOW_WIDTH = "window_width";
    public static final String PREF_WINDOW_HEIGHT = "window_height";
    public static final String PREF_WINDOW_X = "window_pos_x";
    public static final String PREF_WINDOW_Y = "window_pos_y";
    public static final String PREF_LAST_QUERY_JCBH = "last_query_jcbh";
    public static final String PREF_LAST_QUERY_STATUS_INDEX = "last_query_status";
    public static final String PREF_LAST_QUERY_START_DATE = "last_query_start_date";
    public static final String PREF_LAST_QUERY_END_DATE = "last_query_end_date";
    public static final String PREF_LAST_ADVANCED_FILTERS = "last_advanced_filters";
    public static final String PREF_FILTER_TEMPLATES = "query_filter_templates";
    public static final String PREF_FILTER_ADVANCED_EXPANDED = "filter_advanced_expanded";
    public static final String PREF_LAST_TEMPLATE_NAME = "last_filter_template";
    public static final String EXPORT_FILE_PREFIX = "warehouse_export_";
    public static final String EXPORT_DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String PHOTO_URL_TEMPLATE = "http://60.190.0.98:81/csccmisHandler/website/jczpblank.aspx?Language=zh-cn&jcid=%s&jcbh=%s&zyh=%s&bzgg=%s";
    public static final String TXX_URL_TEMPLATE = "http://60.190.0.98:81/csccmisHandler/website/jctxxblank.aspx?Language=zh-cn&jcid=%s&jcbh=%s&zyh=%s&bzgg=%s";
    private static final Map<Integer, DefaultColumnConfig> DEFAULT_COLUMN_CONFIGS = new HashMap<Integer, DefaultColumnConfig>();

    private AppConfig() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String getStatusName(int index) {
        if (index >= 0 && index < STATUS_NAMES.length) {
            return STATUS_NAMES[index];
        }
        return STATUS_NAMES[1];
    }

    public static String getStatusCode(int index) {
        if (index >= 0 && index < STATUS_CODES.length) {
            return STATUS_CODES[index];
        }
        return STATUS_CODES[1];
    }

    public static String getApiUrl() {
        return "http://60.190.0.98:81/csccmisHandler/CsccmisHandler.ashx";
    }

    public static String getPhotoUrl(String jcid) {
        return AppConfig.getPhotoUrl(jcid, "", "", "");
    }

    public static String getPhotoUrl(String jcid, String jcbh, String zyh, String bzgg) {
        return String.format(PHOTO_URL_TEMPLATE, AppConfig.encodeUrlParam(jcid), AppConfig.encodeUrlParam(jcbh), AppConfig.encodeUrlParam(zyh), AppConfig.encodeUrlParam(bzgg));
    }

    public static String getTxxUrl(String jcid, String jcbh, String zyh, String bzgg) {
        return String.format(TXX_URL_TEMPLATE, AppConfig.encodeUrlParam(jcid), AppConfig.encodeUrlParam(jcbh), AppConfig.encodeUrlParam(zyh), AppConfig.encodeUrlParam(bzgg));
    }

    private static String encodeUrlParam(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public static DefaultColumnConfig getDefaultColumnConfig(int statusIndex) {
        return DEFAULT_COLUMN_CONFIGS.getOrDefault(statusIndex, DEFAULT_COLUMN_CONFIGS.get(1));
    }

    public static void main(String[] args) {
        System.out.println("=== \u72b6\u6001\u6620\u5c04\u6d4b\u8bd5 ===");
        for (int i = 0; i < STATUS_NAMES.length; ++i) {
            String statusName = AppConfig.getStatusName(i);
            String statusCode = AppConfig.getStatusCode(i);
            System.out.printf("\u7d22\u5f15%d: %s \u2192 \u72b6\u6001\u4ee3\u7801: %s%n", i, statusName, statusCode);
        }
        System.out.println("\n=== \u7167\u7247URL\u751f\u6210\u6d4b\u8bd5 ===");
        String testJcid = "364470";
        String photoUrl = AppConfig.getPhotoUrl(testJcid);
        System.out.println("\u6d4b\u8bd5jcid: " + testJcid);
        System.out.println("\u751f\u6210\u7684\u7167\u7247URL: " + photoUrl);
        System.out.println("\u9884\u671fURL\u683c\u5f0f: http://60.190.0.98:81/csccmisHandler/website/jczpblank.aspx?Language=zh-cn&jcid=364470");
        System.out.println("\n=== \u9ed8\u8ba4\u5217\u914d\u7f6e\u6d4b\u8bd5 ===");
        for (int i = 0; i < STATUS_NAMES.length; ++i) {
            DefaultColumnConfig config = AppConfig.getDefaultColumnConfig(i);
            System.out.printf("\u72b6\u6001%d (%s) \u9ed8\u8ba4\u5217\u6570: %d%n", i, AppConfig.getStatusName(i), config.getColumnNames().size());
        }
        System.out.println("\n=== \u6d4b\u8bd5\u5b8c\u6210 ===");
    }

    static {
        List<String> allColumns = Arrays.asList("\u8fdb\u4ed3ID", "\u4f5c\u4e1a\u53f7", "\u8fdb\u4ed3\u65e5\u671f", "\u8fdb\u4ed3\u7f16\u53f7", "\u8d27\u4e3b", "\u7801\u5934", "\u8d27\u7269\u540d\u79f0", "\u9884\u8ba1\u4ef6\u6570", "\u9884\u8ba1\u4f53\u79ef", "\u9884\u8ba1\u6bdb\u91cd", "\u4ef6\u6570", "\u4f53\u79ef", "\u6bdb\u91cd", "\u5e93\u5b58\u4ef6\u6570", "\u5e93\u5b58\u4f53\u79ef", "\u5e93\u5b58\u6bdb\u91cd", "\u5305\u88c5\u89c4\u683c", "\u5378\u8d27\u65e5\u671f", "\u5378\u8d27\u5b8c\u6210", "\u9884\u8ba1\u65e5\u671f", "\u7406\u8d27", "\u8d27\u7269\u540d\u79f01", "\u8f66\u53f7", "\u53f8\u673a\u7535\u8bdd", "\u8f66\u957f", "\u627f\u91cd", "\u8fd0\u5355\u53f7", "\u5907\u6ce8", "\u8d27\u4ee3\u540d\u79f0", "\u5206\u516c\u53f8", "\u64cd\u4f5c");
        List<Double> allWidths = Arrays.asList(80.0, 120.0, 140.0, 120.0, 200.0, 100.0, 120.0, 80.0, 80.0, 80.0, 80.0, 80.0, 80.0, 80.0, 80.0, 80.0, 80.0, 140.0, 140.0, 140.0, 60.0, 120.0, 100.0, 120.0, 80.0, 80.0, 120.0, 150.0, 120.0, 120.0, 120.0);
        DEFAULT_COLUMN_CONFIGS.put(0, new DefaultColumnConfig(allColumns, allWidths, Arrays.asList(false, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, true, false, false, true, true, false, false, false, false, false, false, true, true, true, true)));
        DEFAULT_COLUMN_CONFIGS.put(1, new DefaultColumnConfig(allColumns, allWidths, Arrays.asList(false, true, true, true, true, true, true, false, false, false, true, true, true, true, true, true, true, true, true, false, true, false, true, true, false, false, false, true, true, true, true)));
        DEFAULT_COLUMN_CONFIGS.put(2, new DefaultColumnConfig(allColumns, allWidths, Arrays.asList(false, true, true, true, true, true, true, false, false, false, false, false, false, true, true, true, true, false, false, false, true, false, false, false, false, false, false, true, true, true, true)));
        DEFAULT_COLUMN_CONFIGS.put(3, new DefaultColumnConfig(allColumns, allWidths, Arrays.asList(false, true, true, true, true, true, true, false, false, false, true, true, true, false, false, false, true, true, true, false, true, false, true, true, false, false, true, true, true, true, true)));
    }

    public static class DefaultColumnConfig {
        private final List<String> columnNames;
        private final List<Double> columnWidths;
        private final List<Boolean> columnVisible;

        public DefaultColumnConfig(List<String> columnNames, List<Double> columnWidths, List<Boolean> columnVisible) {
            this.columnNames = columnNames;
            this.columnWidths = columnWidths;
            this.columnVisible = columnVisible;
        }

        public List<String> getColumnNames() {
            return this.columnNames;
        }

        public List<Double> getColumnWidths() {
            return this.columnWidths;
        }

        public List<Boolean> getColumnVisible() {
            return this.columnVisible;
        }
    }
}
