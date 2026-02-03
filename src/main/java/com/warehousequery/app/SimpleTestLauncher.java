package com.warehousequery.app;

import com.warehousequery.app.service.SimpleWarehouseService;
import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.SimpleWarehouseEntry;

import java.util.List;
import java.util.Scanner;

/**
 * 简单测试启动器 - 用于在没有JavaFX时测试核心功能
 * 注意：这不是您的原始程序，只是临时测试用
 */
public class SimpleTestLauncher {
    public static void main(String[] args) {
        System.out.println("=== 仓库查询系统 - 临时测试版本 ===");
        System.out.println("注意：这不是您的原始图形界面程序！");
        System.out.println("要使用完整功能，请安装包含JavaFX的Java版本");
        System.out.println("推荐：Azul Zulu JDK with JavaFX 或 BellSoft Liberica JDK Full");
        System.out.println("===============================================");
        System.out.println();
        
        SimpleWarehouseService service = null;
        
        try {
            // 测试配置加载
            System.out.println("配置信息:");
            System.out.println("- API地址: " + AppConfig.getApiUrl());
            System.out.println("- 应用标题: " + AppConfig.APP_TITLE);
            System.out.println();
            
            // 测试服务初始化
            service = new SimpleWarehouseService();
            System.out.println("✓ 服务初始化成功");
            
            // 测试网络连接
            System.out.println("正在测试网络连接...");
            boolean connected = service.testConnection();
            System.out.println("✓ 网络连接状态: " + (connected ? "成功" : "失败"));
            
            if (connected) {
                // 测试会话初始化
                System.out.println("正在初始化会话...");
                boolean sessionOk = service.initializeSession();
                System.out.println("✓ 会话初始化状态: " + (sessionOk ? "成功" : "失败"));
            }
            
            System.out.println();
            System.out.println("=== 核心功能测试 ===");
            System.out.println("您可以测试查询功能，但这只是简化版本");
            System.out.println("完整的图形界面包含更多功能：");
            System.out.println("- 数据表格显示");
            System.out.println("- 导出功能");
            System.out.println("- 照片查看");
            System.out.println("- 托信息查看");
            System.out.println("- 更好的用户界面");
            System.out.println();
            
            // 提供简单的交互式查询
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                System.out.print("请输入进仓编号（输入'exit'退出）: ");
                String jcbh = scanner.nextLine().trim();
                
                if ("exit".equalsIgnoreCase(jcbh)) {
                    break;
                }
                
                if (jcbh.isEmpty()) {
                    System.out.println("进仓编号不能为空");
                    continue;
                }
                
                System.out.print("请选择状态 (1-预约进仓, 2-进仓, 3-库存, 4-出仓): ");
                String statusInput = scanner.nextLine().trim();
                
                String statusCode = "2"; // 默认进仓
                switch (statusInput) {
                    case "1": statusCode = "1"; break;
                    case "2": statusCode = "2"; break;
                    case "3": statusCode = "5"; break;
                    case "4": statusCode = "6"; break;
                    default: 
                        System.out.println("使用默认状态：进仓");
                        break;
                }
                
                System.out.println("正在查询...");
                List<SimpleWarehouseEntry> results = service.queryWarehouse(jcbh, statusCode);
                
                if (results.isEmpty()) {
                    System.out.println("未找到匹配的记录");
                } else {
                    System.out.println("查询结果：");
                    for (int i = 0; i < results.size(); i++) {
                        System.out.println((i + 1) + ". " + results.get(i).toString());
                    }
                }
                System.out.println();
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("系统运行时出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (service != null) {
                service.close();
            }
        }
        
        System.out.println();
        System.out.println("=== 重要提醒 ===");
        System.out.println("这只是临时测试版本！");
        System.out.println("要使用您的完整图形界面程序，请：");
        System.out.println("1. 下载并安装包含JavaFX的Java版本");
        System.out.println("2. 运行 start_with_javafx_check.bat");
        System.out.println("3. 享受完整的图形界面功能");
        System.out.println();
        System.out.println("程序已退出，感谢使用！");
    }
} 