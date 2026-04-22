/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.application.Application
 *  javafx.fxml.FXMLLoader
 *  javafx.geometry.Rectangle2D
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.stage.Screen
 *  javafx.stage.Stage
 */
package com.warehousequery.app;

import com.warehousequery.app.util.ExceptionHandler;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp
extends Application {
    private final Preferences windowPrefs = Preferences.userNodeForPackage(MainApp.class);

    public void start(Stage primaryStage) {
        try {
            Parent root = (Parent)FXMLLoader.load((URL)((Object)((Object)this)).getClass().getResource("/fxml/MainView.fxml"));
            Scene scene = new Scene(root, 1000.0, 600.0);
            primaryStage.setTitle("\u4ed3\u5e93\u67e5\u8be2\u7cfb\u7edf");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800.0);
            primaryStage.setMinHeight(500.0);
            this.restoreWindowPreferences(primaryStage);
            this.attachWindowPreferenceListeners(primaryStage);
            primaryStage.show();
            System.out.println("\u5e94\u7528\u7a0b\u5e8f\u542f\u52a8\u6210\u529f");
        }
        catch (IOException e) {
            ExceptionHandler.handleException("\u52a0\u8f7d\u4e3b\u754c\u9762", e);
            System.exit(1);
        }
        catch (Exception e) {
            ExceptionHandler.handleException("\u5e94\u7528\u7a0b\u5e8f\u542f\u52a8", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            MainApp.launch((String[])args);
        }
        catch (Exception e) {
            ExceptionHandler.handleException("\u5e94\u7528\u7a0b\u5e8f\u542f\u52a8", e);
            System.exit(1);
        }
    }

    private void restoreWindowPreferences(Stage stage) {
        double width = this.windowPrefs.getDouble("window_width", 1000.0);
        double height = this.windowPrefs.getDouble("window_height", 600.0);
        double x = this.windowPrefs.getDouble("window_pos_x", Double.NaN);
        double y = this.windowPrefs.getDouble("window_pos_y", Double.NaN);
        if (width > stage.getMinWidth()) {
            stage.setWidth(width);
        }
        if (height > stage.getMinHeight()) {
            stage.setHeight(height);
        }
        if (!Double.isNaN(x) && !Double.isNaN(y)) {
            stage.setX(x);
            stage.setY(y);
            this.ensureStageInBounds(stage);
        }
    }

    private void attachWindowPreferenceListeners(Stage stage) {
        stage.setOnCloseRequest(event -> this.saveWindowPreferences(stage));
        stage.widthProperty().addListener((obs, oldVal, newVal) -> this.saveWindowPreferences(stage));
        stage.heightProperty().addListener((obs, oldVal, newVal) -> this.saveWindowPreferences(stage));
        stage.xProperty().addListener((obs, oldVal, newVal) -> this.saveWindowPreferences(stage));
        stage.yProperty().addListener((obs, oldVal, newVal) -> this.saveWindowPreferences(stage));
    }

    private void saveWindowPreferences(Stage stage) {
        this.windowPrefs.putDouble("window_width", stage.getWidth());
        this.windowPrefs.putDouble("window_height", stage.getHeight());
        this.windowPrefs.putDouble("window_pos_x", stage.getX());
        this.windowPrefs.putDouble("window_pos_y", stage.getY());
    }

    private void ensureStageInBounds(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double x = stage.getX();
        double y = stage.getY();
        if (x + stage.getWidth() > bounds.getMaxX()) {
            x = bounds.getMaxX() - stage.getWidth();
        }
        if (y + stage.getHeight() > bounds.getMaxY()) {
            y = bounds.getMaxY() - stage.getHeight();
        }
        if (x < bounds.getMinX()) {
            x = bounds.getMinX();
        }
        if (y < bounds.getMinY()) {
            y = bounds.getMinY();
        }
        stage.setX(x);
        stage.setY(y);
    }
}
