package com.warehousequery.app.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一异常处理工具类
 */
public class ExceptionHandler {
    
    private static final String ERROR_LOG_FILE = "logs/error.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 处理异常并显示用户友好的错误信息
     */
    public static void handleException(String context, Throwable throwable) {
        handleException(context, throwable, true);
    }
    
    /**
     * 处理异常
     * @param context 异常发生的上下文
     * @param throwable 异常对象
     * @param showDialog 是否显示错误对话框
     */
    public static void handleException(String context, Throwable throwable, boolean showDialog) {
        // 记录到日志
        logError(context, throwable);
        
        // 在控制台输出
        System.err.println("错误发生在: " + context);
        throwable.printStackTrace();
        
        // 显示用户友好的错误对话框
        if (showDialog) {
            Platform.runLater(() -> showErrorDialog(context, throwable));
        }
    }
    
    /**
     * 记录错误到日志文件
     */
    private static void logError(String context, Throwable throwable) {
        try {
            // 确保日志目录存在
            Files.createDirectories(Paths.get("logs"));
            
            StringBuilder logEntry = new StringBuilder();
            logEntry.append("=== 错误记录 ===\n");
            logEntry.append("时间: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\n");
            logEntry.append("上下文: ").append(context).append("\n");
            logEntry.append("异常类型: ").append(throwable.getClass().getSimpleName()).append("\n");
            logEntry.append("错误信息: ").append(throwable.getMessage()).append("\n");
            logEntry.append("堆栈跟踪:\n");
            
            // 获取完整的堆栈跟踪
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            logEntry.append(sw.toString());
            logEntry.append("\n\n");
            
            // 写入日志文件
            Files.write(
                Paths.get(ERROR_LOG_FILE),
                logEntry.toString().getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
            
        } catch (IOException e) {
            System.err.println("无法写入错误日志: " + e.getMessage());
        }
    }
    
    /**
     * 显示错误对话框
     */
    private static void showErrorDialog(String context, Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("系统错误");
        alert.setHeaderText("操作失败: " + context);
        
        // 根据异常类型提供用户友好的错误信息
        String userMessage = getUserFriendlyMessage(throwable);
        alert.setContentText(userMessage);
        
        // 添加详细错误信息
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String exceptionText = sw.toString();
        
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }
    
    /**
     * 根据异常类型返回用户友好的错误信息
     */
    private static String getUserFriendlyMessage(Throwable throwable) {
        if (throwable instanceof IOException) {
            return "网络连接或文件操作失败，请检查网络连接或文件权限。";
        } else if (throwable instanceof IllegalArgumentException) {
            return "输入参数有误，请检查输入的数据格式。";
        } else if (throwable instanceof NullPointerException) {
            return "系统内部错误，请重试或联系技术支持。";
        } else if (throwable instanceof RuntimeException) {
            return "运行时错误: " + throwable.getMessage();
        } else {
            return "未知错误: " + throwable.getMessage();
        }
    }
    
    /**
     * 处理网络相关异常
     */
    public static void handleNetworkException(String operation, Throwable throwable) {
        String context = "网络操作: " + operation;
        handleException(context, throwable);
    }
    
    /**
     * 处理文件操作异常
     */
    public static void handleFileException(String operation, Throwable throwable) {
        String context = "文件操作: " + operation;
        handleException(context, throwable);
    }
    
    /**
     * 处理数据解析异常
     */
    public static void handleParseException(String dataType, Throwable throwable) {
        String context = "数据解析: " + dataType;
        handleException(context, throwable);
    }
} 