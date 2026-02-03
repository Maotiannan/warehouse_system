@echo off
chcp 65001 >nul
title 统一配置数据

echo 🔧 正在统一配置数据...
echo.
echo 问题说明:
echo    您的软件存在两套配置数据:
echo    1. 默认配置 (column_*)
echo    2. 个人配置 (personal_column_*)
echo.
echo    不同启动方式可能加载不同的配置，导致列设置不一致
echo.

choice /c 123 /m "请选择操作: [1]保留个人配置并删除默认配置 [2]保留默认配置并删除个人配置 [3]查看详细信息"

if errorlevel 3 goto :show_details
if errorlevel 2 goto :keep_default
if errorlevel 1 goto :keep_personal

:keep_personal
echo.
echo ✅ 正在保留个人配置，删除默认配置...
echo.

:: 删除默认配置，保留个人配置
for /f "tokens=1" %%i in ('reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" ^| findstr "column_width_\|column_order_\|column_visible_" ^| findstr /v "personal_"') do (
    echo 删除默认配置: %%i
    reg delete "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" /v "%%i" /f >nul 2>&1
)

echo.
echo ✅ 配置统一完成！现在所有启动方式都将使用您的个人配置
echo 💡 建议: 使用"简洁启动.bat"或修复后的快捷方式启动
goto :end

:keep_default
echo.
echo ✅ 正在保留默认配置，删除个人配置...
echo.

:: 删除个人配置，保留默认配置
for /f "tokens=1" %%i in ('reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" ^| findstr "personal_column_"') do (
    echo 删除个人配置: %%i
    reg delete "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" /v "%%i" /f >nul 2>&1
)

echo.
echo ✅ 配置统一完成！现在所有启动方式都将使用默认配置
echo 💡 建议: 重新调整列设置后保存配置
goto :end

:show_details
echo.
echo 📋 详细配置信息:
echo.
echo 🔍 检查个人配置项:
reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" | findstr "personal_column_" | find /c "personal_" >nul
if %errorlevel%==0 (
    echo    ✅ 发现个人配置项
) else (
    echo    ❌ 未发现个人配置项
)

echo.
echo 🔍 检查默认配置项:
reg query "HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\warehousequery\app\controller" | findstr "column_" | findstr /v "personal_" | find /c "column_" >nul
if %errorlevel%==0 (
    echo    ✅ 发现默认配置项
) else (
    echo    ❌ 未发现默认配置项
)

echo.
echo 💡 建议操作:
echo    1. 如果您更喜欢"简洁启动.bat"的列设置，选择保留个人配置
echo    2. 如果您更喜欢其他启动方式的列设置，选择保留默认配置
echo    3. 统一后，所有启动方式的列设置将完全一致
echo.
pause
goto :start

:end
echo.
echo 🎯 解决方案总结:
echo    ✅ 配置数据已统一
echo    ✅ 不同启动方式将显示相同的列设置
echo    ✅ 列宽度、顺序、可见性保持一致
echo.
echo 📝 使用建议:
echo    1. 现在可以使用任意启动方式
echo    2. 列设置修改后会自动保存
echo    3. 如需重置，使用软件内的"重置列配置"功能
echo.
pause 