/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.application.Platform
 *  javafx.scene.Node
 *  javafx.scene.control.Alert
 *  javafx.scene.control.Alert$AlertType
 *  javafx.scene.control.TextArea
 */
package com.warehousequery.app.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public class ExceptionHandler {
    private static final String ERROR_LOG_FILE = "logs/error.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void handleException(String context, Throwable throwable) {
        ExceptionHandler.handleException(context, throwable, true);
    }

    public static void handleException(String context, Throwable throwable, boolean showDialog) {
        ExceptionHandler.logError(context, throwable);
        System.err.println("\u9519\u8bef\u53d1\u751f\u5728: " + context);
        throwable.printStackTrace();
        if (showDialog) {
            Platform.runLater(() -> ExceptionHandler.showErrorDialog(context, throwable));
        }
    }

    private static void logError(String context, Throwable throwable) {
        try {
            Files.createDirectories(Paths.get("logs", new String[0]), new FileAttribute[0]);
            StringBuilder logEntry = new StringBuilder();
            logEntry.append("=== \u9519\u8bef\u8bb0\u5f55 ===\n");
            logEntry.append("\u65f6\u95f4: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\n");
            logEntry.append("\u4e0a\u4e0b\u6587: ").append(context).append("\n");
            logEntry.append("\u5f02\u5e38\u7c7b\u578b: ").append(throwable.getClass().getSimpleName()).append("\n");
            logEntry.append("\u9519\u8bef\u4fe1\u606f: ").append(throwable.getMessage()).append("\n");
            logEntry.append("\u5806\u6808\u8ddf\u8e2a:\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            logEntry.append(sw.toString());
            logEntry.append("\n\n");
            Files.write(Paths.get(ERROR_LOG_FILE, new String[0]), logEntry.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.err.println("\u65e0\u6cd5\u5199\u5165\u9519\u8bef\u65e5\u5fd7: " + e.getMessage());
        }
    }

    private static void showErrorDialog(String context, Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("\u7cfb\u7edf\u9519\u8bef");
        alert.setHeaderText("\u64cd\u4f5c\u5931\u8d25: " + context);
        String userMessage = ExceptionHandler.getUserFriendlyMessage(throwable);
        alert.setContentText(userMessage);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String exceptionText = sw.toString();
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        alert.getDialogPane().setExpandableContent((Node)textArea);
        alert.showAndWait();
    }

    private static String getUserFriendlyMessage(Throwable throwable) {
        if (throwable instanceof IOException) {
            return "\u7f51\u7edc\u8fde\u63a5\u6216\u6587\u4ef6\u64cd\u4f5c\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u7f51\u7edc\u8fde\u63a5\u6216\u6587\u4ef6\u6743\u9650\u3002";
        }
        if (throwable instanceof IllegalArgumentException) {
            return "\u8f93\u5165\u53c2\u6570\u6709\u8bef\uff0c\u8bf7\u68c0\u67e5\u8f93\u5165\u7684\u6570\u636e\u683c\u5f0f\u3002";
        }
        if (throwable instanceof NullPointerException) {
            return "\u7cfb\u7edf\u5185\u90e8\u9519\u8bef\uff0c\u8bf7\u91cd\u8bd5\u6216\u8054\u7cfb\u6280\u672f\u652f\u6301\u3002";
        }
        if (throwable instanceof RuntimeException) {
            return "\u8fd0\u884c\u65f6\u9519\u8bef: " + throwable.getMessage();
        }
        return "\u672a\u77e5\u9519\u8bef: " + throwable.getMessage();
    }

    public static void handleNetworkException(String operation, Throwable throwable) {
        String context = "\u7f51\u7edc\u64cd\u4f5c: " + operation;
        ExceptionHandler.handleException(context, throwable);
    }

    public static void handleFileException(String operation, Throwable throwable) {
        String context = "\u6587\u4ef6\u64cd\u4f5c: " + operation;
        ExceptionHandler.handleException(context, throwable);
    }

    public static void handleParseException(String dataType, Throwable throwable) {
        String context = "\u6570\u636e\u89e3\u6790: " + dataType;
        ExceptionHandler.handleException(context, throwable);
    }
}
