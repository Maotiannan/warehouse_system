@echo off
cd /d "%~dp0"
start "" javaw --module-path lib --add-modules javafx.controls,javafx.fxml -cp target\WarehouseQuerySystem-test.jar;lib\* com.warehousequery.app.MainApp
exit 