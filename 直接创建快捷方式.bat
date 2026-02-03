@echo off
chcp 65001 >nul
title 创建快捷方式

echo 正在创建快捷方式...

for /f "tokens=*" %%i in ('where javaw') do set JAVAW_PATH=%%i

powershell -Command "$WshShell = New-Object -ComObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%~dp0仓库查询系统.lnk'); $Shortcut.TargetPath = '%JAVAW_PATH%'; $Shortcut.Arguments = '--module-path lib --add-modules javafx.controls,javafx.fxml -cp target\\WarehouseQuerySystem-test.jar;lib\\* com.warehousequery.app.MainApp'; $Shortcut.WorkingDirectory = '%~dp0'; $Shortcut.Description = '仓库查询系统'; $Shortcut.Save()"

if exist "%~dp0仓库查询系统.lnk" (
    echo.
    echo 快捷方式创建成功！
    echo 快捷方式位置: %~dp0仓库查询系统.lnk
    echo.
    echo 使用说明:
    echo    1. 快捷方式已创建在当前程序目录下
    echo    2. 请将"仓库查询系统.lnk"文件剪切到您的桌面
    echo 3. 您的桌面路径: D:\文档\桌面
    echo    4. 双击快捷方式即可启动程序（完全无黑色窗口）
    echo.
    echo 提示: 也可以右键快捷方式选择"固定到任务栏"
) else (
    echo.
    echo 创建快捷方式失败，请手动创建:
    echo 1. 右键点击"简洁启动.bat"
    echo 2. 选择"发送到" - "桌面快捷方式"
    echo 3. 重命名为"仓库查询系统"
)

echo.
pause 