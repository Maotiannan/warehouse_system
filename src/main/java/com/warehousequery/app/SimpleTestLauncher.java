/*
 * Decompiled with CFR 0.152.
 */
package com.warehousequery.app;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.SimpleWarehouseEntry;
import com.warehousequery.app.service.SimpleWarehouseService;
import java.util.List;
import java.util.Scanner;

public class SimpleTestLauncher {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void main(String[] args) {
        System.out.println("=== \u4ed3\u5e93\u67e5\u8be2\u7cfb\u7edf - \u4e34\u65f6\u6d4b\u8bd5\u7248\u672c ===");
        System.out.println("\u6ce8\u610f\uff1a\u8fd9\u4e0d\u662f\u60a8\u7684\u539f\u59cb\u56fe\u5f62\u754c\u9762\u7a0b\u5e8f\uff01");
        System.out.println("\u8981\u4f7f\u7528\u5b8c\u6574\u529f\u80fd\uff0c\u8bf7\u5b89\u88c5\u5305\u542bJavaFX\u7684Java\u7248\u672c");
        System.out.println("\u63a8\u8350\uff1aAzul Zulu JDK with JavaFX \u6216 BellSoft Liberica JDK Full");
        System.out.println("===============================================");
        System.out.println();
        try (SimpleWarehouseService service = null;){
            System.out.println("\u914d\u7f6e\u4fe1\u606f:");
            System.out.println("- API\u5730\u5740: " + AppConfig.getApiUrl());
            System.out.println("- \u5e94\u7528\u6807\u9898: \u4ed3\u5e93\u67e5\u8be2\u7cfb\u7edf");
            System.out.println();
            service = new SimpleWarehouseService();
            System.out.println("\u2713 \u670d\u52a1\u521d\u59cb\u5316\u6210\u529f");
            System.out.println("\u6b63\u5728\u6d4b\u8bd5\u7f51\u7edc\u8fde\u63a5...");
            boolean connected = service.testConnection();
            System.out.println("\u2713 \u7f51\u7edc\u8fde\u63a5\u72b6\u6001: " + (connected ? "\u6210\u529f" : "\u5931\u8d25"));
            if (connected) {
                System.out.println("\u6b63\u5728\u521d\u59cb\u5316\u4f1a\u8bdd...");
                boolean sessionOk = service.initializeSession();
                System.out.println("\u2713 \u4f1a\u8bdd\u521d\u59cb\u5316\u72b6\u6001: " + (sessionOk ? "\u6210\u529f" : "\u5931\u8d25"));
            }
            System.out.println();
            System.out.println("=== \u6838\u5fc3\u529f\u80fd\u6d4b\u8bd5 ===");
            System.out.println("\u60a8\u53ef\u4ee5\u6d4b\u8bd5\u67e5\u8be2\u529f\u80fd\uff0c\u4f46\u8fd9\u53ea\u662f\u7b80\u5316\u7248\u672c");
            System.out.println("\u5b8c\u6574\u7684\u56fe\u5f62\u754c\u9762\u5305\u542b\u66f4\u591a\u529f\u80fd\uff1a");
            System.out.println("- \u6570\u636e\u8868\u683c\u663e\u793a");
            System.out.println("- \u5bfc\u51fa\u529f\u80fd");
            System.out.println("- \u7167\u7247\u67e5\u770b");
            System.out.println("- \u6258\u4fe1\u606f\u67e5\u770b");
            System.out.println("- \u66f4\u597d\u7684\u7528\u6237\u754c\u9762");
            System.out.println();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\u8bf7\u8f93\u5165\u8fdb\u4ed3\u7f16\u53f7\uff08\u8f93\u5165'exit'\u9000\u51fa\uff09: ");
                String jcbh = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(jcbh)) break;
                if (jcbh.isEmpty()) {
                    System.out.println("\u8fdb\u4ed3\u7f16\u53f7\u4e0d\u80fd\u4e3a\u7a7a");
                    continue;
                }
                System.out.println("\u8bf7\u9009\u62e9\u72b6\u6001\uff1a");
                for (int i = 0; i < AppConfig.STATUS_NAMES.length; ++i) {
                    System.out.printf("%d - %s%n", i + 1, AppConfig.STATUS_NAMES[i]);
                }
                System.out.print("\u8f93\u5165\u5e8f\u53f7\u5e76\u56de\u8f66\uff08\u9ed8\u8ba4 2-\u8fdb\u4ed3\uff09: ");
                String statusInput = scanner.nextLine().trim();
                int statusIndex = 1;
                try {
                    if (!statusInput.isEmpty()) {
                        int parsed = Integer.parseInt(statusInput) - 1;
                        if (parsed >= 0 && parsed < AppConfig.STATUS_NAMES.length) {
                            statusIndex = parsed;
                        } else {
                            System.out.println("\u8f93\u5165\u8d85\u51fa\u8303\u56f4\uff0c\u4f7f\u7528\u9ed8\u8ba4\u72b6\u6001\uff1a\u8fdb\u4ed3");
                        }
                    }
                }
                catch (NumberFormatException nfe) {
                    System.out.println("\u8f93\u5165\u65e0\u6548\uff0c\u4f7f\u7528\u9ed8\u8ba4\u72b6\u6001\uff1a\u8fdb\u4ed3");
                }
                String statusCode = AppConfig.getStatusCode(statusIndex);
                String statusName = AppConfig.getStatusName(statusIndex);
                System.out.println("\u6b63\u5728\u4f7f\u7528\u72b6\u6001\uff1a" + statusName + "\uff08\u4ee3\u7801 " + statusCode + "\uff09");
                System.out.println("\u6b63\u5728\u67e5\u8be2...");
                List<SimpleWarehouseEntry> results = service.queryWarehouse(jcbh, statusCode);
                if (results.isEmpty()) {
                    System.out.println("\u672a\u627e\u5230\u5339\u914d\u7684\u8bb0\u5f55");
                } else {
                    System.out.println("\u67e5\u8be2\u7ed3\u679c\uff1a");
                    for (int i = 0; i < results.size(); ++i) {
                        System.out.println(i + 1 + ". " + results.get(i).toString());
                    }
                }
                System.out.println();
            }
            scanner.close();
        }
        System.out.println();
        System.out.println("=== \u91cd\u8981\u63d0\u9192 ===");
        System.out.println("\u8fd9\u53ea\u662f\u4e34\u65f6\u6d4b\u8bd5\u7248\u672c\uff01");
        System.out.println("\u8981\u4f7f\u7528\u60a8\u7684\u5b8c\u6574\u56fe\u5f62\u754c\u9762\u7a0b\u5e8f\uff0c\u8bf7\uff1a");
        System.out.println("1. \u4e0b\u8f7d\u5e76\u5b89\u88c5\u5305\u542bJavaFX\u7684Java\u7248\u672c");
        System.out.println("2. \u8fd0\u884c start_with_javafx_check.bat");
        System.out.println("3. \u4eab\u53d7\u5b8c\u6574\u7684\u56fe\u5f62\u754c\u9762\u529f\u80fd");
        System.out.println();
        System.out.println("\u7a0b\u5e8f\u5df2\u9000\u51fa\uff0c\u611f\u8c22\u4f7f\u7528\uff01");
    }
}
