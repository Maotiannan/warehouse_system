package com.warehousequery.app.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 应用程序配置管理类
 * 集中管理所有常量和配置项
 */
public class AppConfig {
    
    // API配置
    public static final String BASE_URL = "http://60.190.0.98:81";
    public static final String API_ENDPOINT = "/csccmisHandler/CsccmisHandler.ashx";
    public static final String WEBSITE_URL = BASE_URL + "/csccmisHandler/website/csccmis.aspx";
    
    // 会话管理配置
    public static final long SESSION_REFRESH_INTERVAL = 1000 * 60 * 15; // 15分钟
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int RETRY_DELAY_MS = 1000;
    
    // HTTP配置
    public static final int CONNECT_TIMEOUT = 30000; // 30秒
    public static final int SOCKET_TIMEOUT = 30000; // 30秒
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    
    // 文件路径配置
    public static final String REQUEST_LOG_FILE = "request_log.txt";
    public static final String RESPONSE_LOG_FILE = "response_log.txt";
    public static final String SESSION_RESPONSE_FILE = "session_response.html";
    public static final String LOGS_DIR = "logs";
    
    // UI配置
    public static final String APP_TITLE = "仓库查询系统";
    public static final double DEFAULT_WINDOW_WIDTH = 1000.0;
    public static final double DEFAULT_WINDOW_HEIGHT = 600.0;
    
    // 查询配置
    public static final int DEFAULT_PAGE_SIZE = 200;
    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final long MAX_DATE_RANGE_DAYS = 180; // 最大查询天数
    
    // 状态映射 - 修正状态代码对应关系
    public static final String[] STATUS_NAMES = {"预约进仓", "进仓", "库存", "出仓"};
    public static final String[] STATUS_CODES = {"0", "1", "2", "3"}; // 修正：预约进仓=0, 进仓=1, 库存=2, 出仓=3
    
    // 列配置键前缀 - 分离个人配置和默认配置
    public static final String COLUMN_WIDTH_KEY_PREFIX = "column_width_";
    public static final String COLUMN_ORDER_KEY_PREFIX = "column_order_";
    public static final String COLUMN_VISIBLE_KEY_PREFIX = "column_visible_";
    
    // 个人配置键前缀 - 用于保存用户自定义配置
    public static final String PERSONAL_COLUMN_WIDTH_KEY_PREFIX = "personal_column_width_";
    public static final String PERSONAL_COLUMN_ORDER_KEY_PREFIX = "personal_column_order_";
    public static final String PERSONAL_COLUMN_VISIBLE_KEY_PREFIX = "personal_column_visible_";
    
    // 导出配置
    public static final String EXPORT_FILE_PREFIX = "warehouse_export_";
    public static final String EXPORT_DATE_FORMAT = "yyyyMMdd_HHmmss";
    
    // 照片页面URL模板
    public static final String PHOTO_URL_TEMPLATE = BASE_URL + "/csccmisHandler/website/jczpblank.aspx?Language=zh-cn&jcid=%s";
    
    // 默认列配置 - 基于API返回的JSON字段和网页初始显示
    private static final Map<Integer, DefaultColumnConfig> DEFAULT_COLUMN_CONFIGS = new HashMap<>();
    
    static {
        // 基于API返回的实际字段定义所有状态的默认列配置
        // 字段映射：jcid→进仓ID, zyh→作业号, jcrq→进仓日期, jcbh→进仓编号, hz→货主, mt→码头, 
        // yjjs→预计件数, yjtj→预计体积, yjmz→预计毛重, js→件数, tj→体积, mz→毛重,
        // kcjs→库存件数, kctj→库存体积, kcmz→库存毛重, bzgg→包装规格, hwmc→货物名称,
        // xhrq→卸货日期, xhrq2→卸货完成, yjrq→预计日期, lf→理货, hwmc1→货物名称1,
        // ch→车号, driverdh→司机电话, cleng→车长, chengzhong→承重, yyh→运单号,
        // bz2→备注, hdmc→货代名称, fgsmc→分公司名称
        
        List<String> allColumns = Arrays.asList(
            "进仓ID", "作业号", "进仓日期", "进仓编号", "货主", "码头", "货物名称",
            "预计件数", "预计体积", "预计毛重", "件数", "体积", "毛重",
            "库存件数", "库存体积", "库存毛重", "包装规格", "卸货日期", "卸货完成",
            "预计日期", "理货", "货物名称1", "车号", "司机电话", "车长", "承重",
            "运单号", "备注", "货代名称", "分公司", "操作"
        );
        
        List<Double> allWidths = Arrays.asList(
            80.0, 120.0, 140.0, 120.0, 200.0, 100.0, 120.0,     // 进仓ID到货物名称
            80.0, 80.0, 80.0, 80.0, 80.0, 80.0,                 // 预计件数到毛重
            80.0, 80.0, 80.0, 80.0, 140.0, 140.0,               // 库存件数到卸货完成
            140.0, 60.0, 120.0, 100.0, 120.0, 80.0, 80.0,      // 预计日期到承重
            120.0, 150.0, 120.0, 120.0, 120.0                   // 运单号到操作
        );
        
        // 预约进仓(0)默认列配置 - 显示预约相关信息
        DEFAULT_COLUMN_CONFIGS.put(0, new DefaultColumnConfig(
            allColumns,
            allWidths,
            Arrays.asList(
                false, true, true, true, true, true, true,       // 进仓ID隐藏，作业号到货物名称显示
                true, true, true, false, false, false,           // 预计数据显示，实际数据隐藏
                false, false, false, true, false, false,         // 库存数据隐藏，包装规格显示，卸货隐藏
                true, true, false, false, false, false, false,   // 预计日期、理货显示，其他隐藏
                false, true, true, true, true                    // 运单号隐藏，备注到操作显示
            )
        ));
        
        // 进仓(1)默认列配置 - 显示进仓相关信息
        DEFAULT_COLUMN_CONFIGS.put(1, new DefaultColumnConfig(
            allColumns,
            allWidths,
            Arrays.asList(
                false, true, true, true, true, true, true,       // 进仓ID隐藏，作业号到货物名称显示
                false, false, false, true, true, true,           // 预计数据隐藏，实际数据显示
                true, true, true, true, true, true,              // 库存数据显示，包装规格到卸货完成显示
                false, true, false, true, true, false, false,    // 预计日期隐藏，理货、车号、司机显示
                false, true, true, true, true                    // 运单号隐藏，备注到操作显示
            )
        ));
        
        // 库存(2)默认列配置 - 重点显示库存信息
        DEFAULT_COLUMN_CONFIGS.put(2, new DefaultColumnConfig(
            allColumns,
            allWidths,
            Arrays.asList(
                false, true, true, true, true, true, true,       // 进仓ID隐藏，作业号到货物名称显示
                false, false, false, false, false, false,        // 预计和实际数据隐藏
                true, true, true, true, false, false,            // 库存数据显示，包装规格显示，卸货隐藏
                false, true, false, false, false, false, false,  // 预计日期隐藏，理货显示，其他隐藏
                false, true, true, true, true                    // 运单号隐藏，备注到操作显示
            )
        ));
        
        // 出仓(3)默认列配置 - 显示出仓相关信息
        DEFAULT_COLUMN_CONFIGS.put(3, new DefaultColumnConfig(
            allColumns,
            allWidths,
            Arrays.asList(
                false, true, true, true, true, true, true,       // 进仓ID隐藏，作业号到货物名称显示
                false, false, false, true, true, true,           // 预计数据隐藏，实际数据显示
                false, false, false, true, true, true,           // 库存数据隐藏，包装规格到卸货完成显示
                false, true, false, true, true, false, false,    // 预计日期隐藏，理货、车号、司机显示
                true, true, true, true, true                     // 运单号到操作全部显示
            )
        ));
    }
    
    /**
     * 默认列配置类
     */
    public static class DefaultColumnConfig {
        private final List<String> columnNames;
        private final List<Double> columnWidths;
        private final List<Boolean> columnVisible;
        
        public DefaultColumnConfig(List<String> columnNames, List<Double> columnWidths, List<Boolean> columnVisible) {
            this.columnNames = columnNames;
            this.columnWidths = columnWidths;
            this.columnVisible = columnVisible;
        }
        
        public List<String> getColumnNames() { return columnNames; }
        public List<Double> getColumnWidths() { return columnWidths; }
        public List<Boolean> getColumnVisible() { return columnVisible; }
    }
    
    // 私有构造函数，防止实例化
    private AppConfig() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * 根据状态索引获取状态名称
     */
    public static String getStatusName(int index) {
        if (index >= 0 && index < STATUS_NAMES.length) {
            return STATUS_NAMES[index];
        }
        return STATUS_NAMES[1]; // 默认返回"进仓"
    }
    
    /**
     * 根据状态索引获取状态代码
     */
    public static String getStatusCode(int index) {
        if (index >= 0 && index < STATUS_CODES.length) {
            return STATUS_CODES[index];
        }
        return STATUS_CODES[1]; // 默认返回"1"
    }
    
    /**
     * 获取完整的API URL
     */
    public static String getApiUrl() {
        return BASE_URL + API_ENDPOINT;
    }
    
    /**
     * 获取照片页面URL
     */
    public static String getPhotoUrl(String jcid) {
        return String.format(PHOTO_URL_TEMPLATE, jcid);
    }
    
    /**
     * 获取默认列配置
     */
    public static DefaultColumnConfig getDefaultColumnConfig(int statusIndex) {
        return DEFAULT_COLUMN_CONFIGS.getOrDefault(statusIndex, DEFAULT_COLUMN_CONFIGS.get(1));
    }
    
    /**
     * 测试方法 - 验证状态映射和照片URL生成是否正确
     */
    public static void main(String[] args) {
        System.out.println("=== 状态映射测试 ===");
        for (int i = 0; i < STATUS_NAMES.length; i++) {
            String statusName = getStatusName(i);
            String statusCode = getStatusCode(i);
            System.out.printf("索引%d: %s → 状态代码: %s%n", i, statusName, statusCode);
        }
        
        System.out.println("\n=== 照片URL生成测试 ===");
        String testJcid = "364470";
        String photoUrl = getPhotoUrl(testJcid);
        System.out.println("测试jcid: " + testJcid);
        System.out.println("生成的照片URL: " + photoUrl);
        System.out.println("预期URL格式: " + BASE_URL + "/csccmisHandler/website/jczpblank.aspx?Language=zh-cn&jcid=364470");
        
        System.out.println("\n=== 默认列配置测试 ===");
        for (int i = 0; i < STATUS_NAMES.length; i++) {
            DefaultColumnConfig config = getDefaultColumnConfig(i);
            System.out.printf("状态%d (%s) 默认列数: %d%n", i, getStatusName(i), config.getColumnNames().size());
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
} 