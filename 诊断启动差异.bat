@echo off
chcp 65001 >nul
title 诊断启动差异

echo 🔍 诊断不同启动方式的差异...
echo.

echo 📋 当前环境信息:
echo    当前工作目录: %CD%
echo    脚本所在目录: %~dp0
echo    用户名: %USERNAME%
echo    用户配置文件: %USERPROFILE%
echo.

echo 🔍 检查Java Preferences位置:
echo    注册表路径: HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller
echo.

echo 📊 统计配置项数量:
for /f %%i in ('reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" ^| find "personal_column_" ^| find /c "personal_"') do set PERSONAL_COUNT=%%i
for /f %%i in ('reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" ^| findstr "column_" ^| findstr /v "personal_" ^| find /c "column_"') do set DEFAULT_COUNT=%%i

echo    个人配置项数量: %PERSONAL_COUNT%
echo    默认配置项数量: %DEFAULT_COUNT%
echo.

echo 🎯 可能的原因分析:
echo.
echo 1. 工作目录差异:
if "%CD%"=="%~dp0" (
    echo    ✅ 当前工作目录与脚本目录一致
) else (
    echo    ⚠️  当前工作目录与脚本目录不一致
    echo    这可能导致配置保存位置不同
)

echo.
echo 2. 启动方式差异:
echo    - 直接启动: 双击简洁启动.bat
echo    - 快捷方式: 通过.lnk文件启动
echo    - 可能的差异: 环境变量、工作目录、用户上下文
echo.

echo 🔧 建议的解决方案:
echo.
echo 方案1: 强制统一工作目录
echo    修改启动脚本，确保工作目录一致
echo.
echo 方案2: 使用绝对路径配置
echo    修改Java代码，使用固定的配置保存路径
echo.
echo 方案3: 清理重复配置
echo    删除冲突的配置项，只保留一套
echo.

choice /c 123 /m "请选择解决方案: [1]修改启动脚本 [2]清理配置数据 [3]查看详细配置"

if errorlevel 3 goto :show_config
if errorlevel 2 goto :clean_config
if errorlevel 1 goto :fix_script

:fix_script
echo.
echo 🔧 正在修改启动脚本...
echo.

:: 创建新的启动脚本，强制工作目录
echo @echo off > 统一启动.bat
echo cd /d "%~dp0" >> 统一启动.bat
echo start "" javaw --module-path lib --add-modules javafx.controls,javafx.fxml -cp target\WarehouseQuerySystem-test.jar;lib\* com.warehousequery.app.MainApp >> 统一启动.bat
echo exit >> 统一启动.bat

echo ✅ 已创建"统一启动.bat"
echo 💡 建议: 使用这个新脚本替代所有其他启动方式
goto :end

:clean_config
echo.
echo 🧹 正在清理配置数据...
echo.

:: 显示当前配置状态
echo 当前配置状态:
echo    个人配置项: %PERSONAL_COUNT% 个
echo    默认配置项: %DEFAULT_COUNT% 个
echo.

choice /c 12 /m "选择保留: [1]保留个人配置 [2]保留默认配置"

if errorlevel 2 (
    echo 删除个人配置...
    for /f "tokens=1" %%i in ('reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" ^| findstr "personal_column_"') do (
        reg delete "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" /v "%%i" /f >nul 2>&1
    )
    echo ✅ 已删除个人配置，保留默认配置
) else (
    echo 删除默认配置...
    for /f "tokens=1" %%i in ('reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" ^| findstr "column_" ^| findstr /v "personal_"') do (
        reg delete "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" /v "%%i" /f >nul 2>&1
    )
    echo ✅ 已删除默认配置，保留个人配置
)
goto :end

:show_config
echo.
echo 📋 详细配置信息:
echo.
echo 个人配置项 (personal_column_*):
reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" | findstr "personal_column_" | more
echo.
echo 默认配置项 (column_*):
reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" | findstr "column_" | findstr /v "personal_" | more
echo.
pause
goto :end

:end
echo.
echo 🎯 诊断完成！
echo.
pause 