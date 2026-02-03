package com.warehousequery.app;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.util.ExceptionHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 主应用程序类
 * 重构后的版本，使用配置类和改进的异常处理
 */
public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // 加载FXML界面
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            
            // 创建场景
            Scene scene = new Scene(root, AppConfig.DEFAULT_WINDOW_WIDTH, AppConfig.DEFAULT_WINDOW_HEIGHT);
            
            // 设置窗口属性
            primaryStage.setTitle(AppConfig.APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(500);
            
            // 显示窗口
            primaryStage.show();
            
            System.out.println("应用程序启动成功");
            
        } catch (IOException e) {
            ExceptionHandler.handleException("加载主界面", e);
            System.exit(1);
        } catch (Exception e) {
            ExceptionHandler.handleException("应用程序启动", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            ExceptionHandler.handleException("应用程序启动", e);
            System.exit(1);
        }
    }
} 