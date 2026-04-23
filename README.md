# 仓库查询系统

这是当前桌面版源码仓库。

## 当前状态

- 基于安装版反向还原并对齐到可编译源码
- 已修复库存查询货主 `hz2` 回退
- 已修复照片、托信息按钮，打开参数使用实际返回的 `inguid`
- 已修复启动后右侧结束日期自动回到今天
- 已新增唛头本地编辑和本地缓存覆盖

## 唛头本地编辑

- 查询结果每次仍然走网络请求
- 唛头列支持本地编辑并保存
- 同一条记录以 `zyh` 为主键做本地缓存
- 如果本地存在已编辑唛头，后续查询优先显示本地值
- 本地缓存文件位置：
  `C:\Users\<当前用户>\.warehouse-query-system\entry-cache.json`

## 启动

- 日常启动只保留：
  `简洁启动.bat`
- 启动方式：
  `javaw -jar target/WarehouseQuerySystem-1.0-SNAPSHOT.jar`

## 生成产物

- fat jar：
  `target/WarehouseQuerySystem-1.0-SNAPSHOT.jar`
- Windows 安装包本地构建输出：
  `target/installer/WarehouseQuerySystem-1.0.1.exe`
- GitHub 仓库不再提交安装包或运行压缩包，最新安装包统一放到 Releases

## 手动测试重点

1. 启动后确认右侧结束日期自动是今天
2. 查询库存编号 `L25MH070359`，确认货主不为空
3. 查询 `L25MH121026`，点击“照片”和“托信息”，确认 URL 中 `jcid=` 后使用的是 `inguid`
4. 双击编辑唛头，重新查询同一 `zyh`，确认优先显示本地保存值

## 仓库说明

- GitHub 已作为主仓库同步
- WSL 目录 `WarehouseQuerySystem电脑新仓库` 已同步为当前版本
