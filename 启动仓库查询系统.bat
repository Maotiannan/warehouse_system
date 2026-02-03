@echo off
javaw --module-path lib --add-modules javafx.controls,javafx.fxml -cp target\WarehouseQuerySystem-test.jar;lib\* com.warehousequery.app.MainApp 