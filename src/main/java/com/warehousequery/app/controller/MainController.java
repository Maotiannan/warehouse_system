package com.warehousequery.app.controller;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.WarehouseEntry;
import com.warehousequery.app.service.WarehouseService;
import com.warehousequery.app.util.ExceptionHandler;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.SelectionMode;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.stage.FileChooser;
import java.util.prefs.Preferences;
import javafx.scene.input.MouseButton;
import javafx.geometry.Side;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Optional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import java.io.OutputStream;

/**
 * 主窗口控制器 - 重构后的版本
 * 使用配置类，改进异常处理，拆分大方法
 */
public class MainController implements Initializable {

    // FXML注入的UI组件
    @FXML private TextField jcbhTextField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TableView<WarehouseEntry> resultTableView;
    @FXML private Label totalJsLabel;
    @FXML private Label totalTjLabel;
    @FXML private Label totalKctjLabel;
    @FXML private Label totalMzLabel;
    @FXML private Label totalKcjsLabel;
    @FXML private Button queryButton;
    @FXML private Button webVersionButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private MenuItem viewLogsMenuItem;
    @FXML private MenuItem exportMenuItem;
    @FXML private CheckMenuItem showHeadersMenuItem;
    @FXML private MenuItem calcSumMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem exportSelectedMenuItem;
    @FXML private MenuItem saveColumnConfigMenuItem;
    @FXML private MenuItem columnConfigMenuItem;
    @FXML private MenuItem resetColumnConfigMenuItem;
    @FXML private Label selectedRowsCountLabel;
    @FXML private Label selectedJsLabel;
    @FXML private Label selectedTjLabel;
    @FXML private Label selectedKctjLabel;
    @FXML private Label selectedMzLabel;
    @FXML private Label selectedKcjsLabel;
    @FXML private Button exportSelectedButton;
    @FXML private Label queryResultLabel;
    @FXML private VBox selectedTotalsBox;
    
    // 业务组件
    private final WarehouseService service = new WarehouseService();
    private final ObservableList<WarehouseEntry> entryList = FXCollections.observableArrayList();
    
    // 配置管理
    private final Preferences prefs = Preferences.userNodeForPackage(MainController.class);
    private int currentStatusIndex = 1; // 默认为进仓状态
    
    // POI库可用性检查
    private static boolean isPOIAvailable = false;
    
    // 右键菜单
    private ContextMenu cellContextMenu;
    
    // 静态初始化块，检查POI库是否可用
    static {
        try {
            Class.forName("org.apache.poi.hssf.usermodel.HSSFWorkbook");
            isPOIAvailable = true;
            System.out.println("POI库可用，将使用Excel格式导出");
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            isPOIAvailable = false;
            System.err.println("POI库不可用，将使用CSV格式导出: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("正在初始化主控制器...");
        
        try {
            // 初始化各个组件
            initializeMenuItems();
            initializeStatusComboBox();
            initializeDatePickers();
            initializeTableView();
            initializeProgressComponents();
            initializeEventHandlers();
            initializeLibraryChecks();
            
            System.out.println("主控制器初始化完成");
            
        } catch (Exception e) {
            ExceptionHandler.handleException("初始化主控制器", e);
        }
    }
    
    /**
     * 初始化菜单项
     */
    private void initializeMenuItems() {
        if (exportSelectedMenuItem != null) {
            exportSelectedMenuItem.setOnAction(event -> exportSelectedRows());
        }
        
        if (exportSelectedButton != null) {
            exportSelectedButton.setOnAction(event -> exportSelectedRows());
        }
        
        if (viewLogsMenuItem != null) {
            viewLogsMenuItem.setOnAction(event -> viewLogs());
        }
        
        if (exportMenuItem != null) {
            exportMenuItem.setOnAction(event -> handleExport());
        }
        
        // 新增的列配置菜单项
        if (columnConfigMenuItem != null) {
            columnConfigMenuItem.setOnAction(event -> showColumnConfigDialog());
        }
        
        if (saveColumnConfigMenuItem != null) {
            saveColumnConfigMenuItem.setOnAction(event -> saveColumnConfiguration());
        }
        
        if (resetColumnConfigMenuItem != null) {
            resetColumnConfigMenuItem.setOnAction(event -> resetColumnConfiguration());
        }
    }
    
    /**
     * 初始化状态下拉框
     */
    private void initializeStatusComboBox() {
        ObservableList<String> statusItems = FXCollections.observableArrayList(AppConfig.STATUS_NAMES);
        statusComboBox.setItems(statusItems);
        statusComboBox.getSelectionModel().select(1); // 默认选择"进仓"
        currentStatusIndex = 1;
        
        // 添加状态改变监听器
        statusComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            currentStatusIndex = newVal.intValue();
        });
    }
    
    /**
     * 初始化日期选择器
     */
    private void initializeDatePickers() {
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusDays(AppConfig.MAX_DATE_RANGE_DAYS - 1);
        
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(currentDate);
        
        // 添加日期范围验证监听器
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && endDatePicker.getValue() != null) {
                validateDateRange(newVal, endDatePicker.getValue());
            }
        });
        
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && startDatePicker.getValue() != null) {
                validateDateRange(startDatePicker.getValue(), newVal);
            }
        });
        
        // 添加提示信息
        startDatePicker.setTooltip(new Tooltip("开始日期 - 注意：日期范围不能超过" + AppConfig.MAX_DATE_RANGE_DAYS + "天"));
        endDatePicker.setTooltip(new Tooltip("结束日期 - 注意：日期范围不能超过" + AppConfig.MAX_DATE_RANGE_DAYS + "天"));
    }
    
    /**
     * 初始化表格视图
     */
    private void initializeTableView() {
        // 初始状态下隐藏选中行统计区域
        if (selectedTotalsBox != null) {
            selectedTotalsBox.setVisible(false);
            selectedTotalsBox.setManaged(false);
        }
        
        // 设置表格选择模式为多选
        resultTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // 绑定数据
        resultTableView.setItems(entryList);
        
        // 初始化表格列
        setupTableColumns();
        
        // 设置右键菜单
        setupTableContextMenu();
    }
    
    /**
     * 初始化进度组件
     */
    private void initializeProgressComponents() {
        progressBar.setProgress(0);
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
    }
        
    /**
     * 初始化事件处理器
     */
    private void initializeEventHandlers() {
        // 查询按钮点击事件
        queryButton.setOnAction(event -> handleQuery());
        
        // 网页版按钮点击事件
        webVersionButton.setOnAction(event -> handleOpenWebVersion());
        
        // 进仓编号回车查询
        jcbhTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleQuery();
            }
        });
    }
    
    /**
     * 初始化库检查
     */
    private void initializeLibraryChecks() {
        // 检查JSON库是否可用
        try {
            Class.forName("org.json.JSONObject");
            System.out.println("JSON库已加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("警告: JSON库未找到 - " + e.getMessage());
            showAlert(Alert.AlertType.WARNING, "库缺失警告", 
                     "未找到JSON解析库，请安装org.json库。\n" +
                     "系统将尝试回退到旧版HTML解析方式，但可能无法正常工作。");
        }
    }
    
    /**
     * 验证日期范围
     */
    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            
            if (daysBetween > AppConfig.MAX_DATE_RANGE_DAYS) {
                // 自动调整开始日期
                LocalDate newStartDate = end.minusDays(AppConfig.MAX_DATE_RANGE_DAYS);
                startDatePicker.setValue(newStartDate);
                
                // 显示警告
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.WARNING, 
                              "日期范围过长", 
                              "查询日期范围已自动调整为" + AppConfig.MAX_DATE_RANGE_DAYS + "天，\n因为服务器限制查询范围不能超过" + AppConfig.MAX_DATE_RANGE_DAYS + "天。");
                });
            }
        }
    }
    
    /**
     * 设置表格右键菜单
     */
    private void setupTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        contextMenu.setOnShowing(event -> {
            contextMenu.getItems().clear();
            
            Menu columnsMenu = new Menu("显示/隐藏列");
            
            for (TableColumn<WarehouseEntry, ?> column : resultTableView.getColumns()) {
                String columnName = column.getText();
                if (columnName != null && !columnName.isEmpty() && !columnName.equals("操作")) {
                    CheckMenuItem item = new CheckMenuItem(columnName);
                    item.setSelected(column.isVisible());
                    item.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        column.setVisible(newVal);
                    });
                    columnsMenu.getItems().add(item);
                }
            }
            
            // 添加配置菜单项
            MenuItem saveConfigItem = new MenuItem("保存当前列配置");
            saveConfigItem.setOnAction(event2 -> saveColumnConfiguration());
            
            MenuItem resetConfigItem = new MenuItem("恢复默认列配置");
            resetConfigItem.setOnAction(event2 -> resetColumnConfiguration());
            
            contextMenu.getItems().addAll(columnsMenu, new SeparatorMenuItem(), saveConfigItem, resetConfigItem);
        });
        
        resultTableView.setContextMenu(contextMenu);
    }
    
    /**
     * 重置列配置 - 完全重置，显示所有列
     * 清除所有个人配置，显示所有可用列，使用合理的默认宽度
     */
    private void resetColumnConfiguration() {
        try {
            // 清除当前状态的所有个人配置
            String[] keys = prefs.keys();
            for (String key : keys) {
                if ((key.startsWith(AppConfig.PERSONAL_COLUMN_WIDTH_KEY_PREFIX) || 
                     key.startsWith(AppConfig.PERSONAL_COLUMN_VISIBLE_KEY_PREFIX) || 
                     key.startsWith(AppConfig.PERSONAL_COLUMN_ORDER_KEY_PREFIX)) && 
                    key.contains("_" + currentStatusIndex + "_")) {
                    prefs.remove(key);
                    System.out.println("清除个人配置: " + key);
                }
            }
            
            // 重新设置表格列结构
            setupTableColumns();
            
            System.out.println("重置列配置 - 状态: " + AppConfig.getStatusName(currentStatusIndex));
            System.out.println("将显示所有可用列");
            
            int visibleColumnCount = 0;
            int totalColumnCount = 0;
            
            // 遍历表格列，设置所有列为可见并应用合理的默认宽度
            for (TableColumn<WarehouseEntry, ?> column : resultTableView.getColumns()) {
                String columnText = column.getText();
                
                // 跳过特殊列（选择框列和操作列）
                if (columnText.isEmpty()) {
                    continue;
                }
                
                totalColumnCount++;
                
                // 设置所有列为可见
                column.setVisible(true);
                visibleColumnCount++;
                
                // 根据列名设置合理的默认宽度
                double defaultWidth = calculateReasonableColumnWidth(columnText);
                column.setPrefWidth(defaultWidth);
                
                System.out.println("设置列 \"" + columnText + "\" 可见性: true, 宽度: " + defaultWidth);
            }
            
            // 刷新表格显示
            resultTableView.refresh();
            
            showAlert(Alert.AlertType.INFORMATION, "配置重置", 
                     "状态 \"" + AppConfig.getStatusName(currentStatusIndex) + "\" 的列配置已完全重置\n" +
                     "所有列已显示，共 " + visibleColumnCount + " 列\n" +
                     "您现在可以隐藏不需要的列并调整列宽度，这些更改将自动保存为个人配置");
            
        } catch (Exception e) {
            ExceptionHandler.handleException("重置列配置", e);
        }
    }
    
    /**
     * 根据列名计算合理的默认宽度
     */
    private double calculateReasonableColumnWidth(String columnName) {
        // 根据列名内容设置更合理的宽度
        switch (columnName) {
            case "进仓ID":
            case "L/F":
            case "件数":
            case "体积":
            case "毛重":
            case "托数":
                return 60;
            case "包装规格":
            case "货号":
            case "库存件数":
            case "库存体积":
            case "报关状态":
                return 80;
            case "进仓编号":
            case "作业号":
            case "进仓作业号":
            case "唛头":
            case "码头":
            case "车号":
            case "司机姓名":
            case "车长":
            case "承重":
                return 100;
            case "货物名称":
            case "货物名称1":
            case "运单号":
            case "司机电话":
            case "货代名称":
            case "分公司":
                return 120;
            case "预计日期":
            case "进仓日期":
            case "卸货日期":
            case "卸货完成":
                return 140;
            case "送货单位":
            case "货主":
            case "备注":
                return 150;
            case "操作":
                return 120;
            default:
                // 根据字符长度计算
                int length = columnName.length();
                if (length <= 2) return 60;
                if (length <= 4) return 80;
                if (length <= 6) return 100;
                if (length <= 8) return 120;
                return 150;
        }
    }
    
    /**
     * 检查日期范围是否有效，如果超过180天则自动调整
     */
    private void checkDateRangeValid(LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            // 计算日期差
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            
            if (daysBetween > 180) {
                // 如果超过180天，自动调整开始日期
                LocalDate newStartDate = end.minusDays(180);
                startDatePicker.setValue(newStartDate);
                
                // 显示警告
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.WARNING, 
                              "日期范围过长", 
                              "查询日期范围已自动调整为180天，\n因为服务器限制查询范围不能超过180天。");
                });
            }
        }
    }
    
    /**
     * 初始化表格列
     */
    private void setupTableColumns() {
        // 清除现有列
        resultTableView.getColumns().clear();
        
        // 添加全选复选框列
        TableColumn<WarehouseEntry, Boolean> selectCol = new TableColumn<>("");
        selectCol.setCellValueFactory(param -> {
            // 为每个行项目创建布尔属性
            WarehouseEntry entry = param.getValue();
            SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(entry.isSelected());
            
            // 绑定属性变化到选中状态
            booleanProp.addListener((obs, oldVal, newVal) -> {
                entry.setSelected(newVal);
                updateSelectedRowsStatistics();
            });
            
            return booleanProp;
        });
        
        // 自定义的复选框列标题，添加"全选"功能
        CheckBox selectAllCheckBox = new CheckBox();
        selectAllCheckBox.setOnAction(event -> {
            boolean select = selectAllCheckBox.isSelected();
            for (WarehouseEntry entry : entryList) {
                entry.setSelected(select);
            }
            resultTableView.refresh();
            updateSelectedRowsStatistics();
        });
        
        selectCol.setGraphic(selectAllCheckBox);
        
        selectCol.setCellFactory(param -> {
            CheckBoxTableCell<WarehouseEntry, Boolean> cell = new CheckBoxTableCell<>();
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });
        
        selectCol.setEditable(true);
        selectCol.setPrefWidth(40);
        selectCol.setResizable(false); // 设置为不可调整大小，避免意外拖动
        resultTableView.getColumns().add(selectCol);
        
        // 检查是否有从网站提取的表头
        List<String> extractedHeaders = com.warehousequery.app.service.WarehouseService.getExtractedTableHeaders();
        if (!extractedHeaders.isEmpty()) {
            System.out.println("使用从网站提取的表头名称: " + String.join(", ", extractedHeaders));
            setupColumnsForStatus(currentStatusIndex, extractedHeaders);
        } else {
            System.out.println("使用预定义表头");
            setupPredefinedColumns();
        }
        
        // 设置表格为可编辑，支持选择框
        resultTableView.setEditable(true);
    }
    
    /**
     * 根据查询状态设置对应的表头结构 - 改进版本
     */
    private void setupColumnsForStatus(int statusIndex, List<String> headers) {
        // 首先保存当前的列顺序，如果有的话
        String orderKey = AppConfig.COLUMN_ORDER_KEY_PREFIX + statusIndex;
        String savedOrder = prefs.get(orderKey, "");
        Map<String, Integer> columnOrderMap = new HashMap<>();
        
        if (!savedOrder.isEmpty()) {
            String[] columnNames = savedOrder.split(",");
            for (int i = 0; i < columnNames.length; i++) {
                columnOrderMap.put(columnNames[i], i);
            }
            System.out.println("已加载保存的列顺序: " + savedOrder);
        }
        
        // 清除所有现有列
        resultTableView.getColumns().clear();
        
        // 首先添加选择框列（不参与配置保存）
        addSelectionColumn();
        
        // 根据状态设置不同的列结构
        List<TableColumn<WarehouseEntry, ?>> columnsToAdd = new ArrayList<>();
        
        switch (statusIndex) {
            case 0: // 预约进仓
                setupPreBookingColumns(headers, columnsToAdd);
                break;
            case 1: // 进仓
                setupInboundColumns(headers, columnsToAdd);
                break;
            case 2: // 库存
                setupInventoryColumns(headers, columnsToAdd);
                break;
            case 3: // 出仓
                setupOutboundColumns(headers, columnsToAdd);
                break;
            default:
                setupInboundColumns(headers, columnsToAdd); // 默认使用进仓列结构
        }
        
        // 根据保存的顺序排序列
        if (!columnOrderMap.isEmpty()) {
            columnsToAdd.sort((col1, col2) -> {
                String name1 = col1.getText();
                String name2 = col2.getText();
                Integer pos1 = columnOrderMap.getOrDefault(name1, Integer.MAX_VALUE);
                Integer pos2 = columnOrderMap.getOrDefault(name2, Integer.MAX_VALUE);
                return pos1.compareTo(pos2);
            });
        }
        
        // 添加排序后的列
        resultTableView.getColumns().addAll(columnsToAdd);
        
        // 最后添加操作列（不参与配置保存）
        addOperationColumn();
        
        // 加载列配置
        loadColumnConfiguration();
    }
    
    /**
     * 添加选择框列
     */
    private void addSelectionColumn() {
        TableColumn<WarehouseEntry, Boolean> selectColumn = new TableColumn<>("");
        selectColumn.setCellValueFactory(param -> {
            WarehouseEntry entry = param.getValue();
            BooleanProperty selected = entry.selectedProperty();
            selected.addListener((obs, oldVal, newVal) -> updateSelectedRowsStatistics());
            return selected;
        });
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);
        selectColumn.setPrefWidth(30);
        selectColumn.setResizable(false);
        selectColumn.setSortable(false);
        
        // 添加全选复选框到列标题
        CheckBox selectAllCheckBox = new CheckBox();
        selectAllCheckBox.setOnAction(event -> {
            boolean select = selectAllCheckBox.isSelected();
            for (WarehouseEntry entry : entryList) {
                entry.setSelected(select);
            }
            resultTableView.refresh();
            updateSelectedRowsStatistics();
        });
        selectColumn.setGraphic(selectAllCheckBox);
        
        resultTableView.getColumns().add(selectColumn);
    }
    
    /**
     * 设置预约进仓状态的列结构 - 重新设计为动态表头
     */
    private void setupPreBookingColumns(List<String> headers, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        // 预约进仓状态有完全不同的表头结构，需要动态处理
        if (headers.isEmpty()) {
            // 如果没有从服务器获取到表头，使用默认预约进仓表头
            setupDefaultPreBookingColumns(columnsToAdd);
            return;
        }
        
        // 创建预约进仓专用的表头映射
        Map<String, String> preBookingFieldMapping = createPreBookingFieldMapping();
        Map<String, String> displayNameMapping = createPreBookingDisplayNameMapping();
        
        // 动态创建列，按照服务器返回的表头顺序
        for (String serverHeader : headers) {
            String fieldName = preBookingFieldMapping.get(serverHeader);
            if (fieldName != null) {
                // 找到了对应的字段映射，使用中文显示名称
                String displayName = displayNameMapping.getOrDefault(serverHeader, serverHeader);
                double width = getColumnWidthFromConfig(0, displayName, calculateColumnWidth(displayName));
                addDynamicColumn(displayName, fieldName, width, columnsToAdd);
            } else {
                // 没有找到映射，使用默认字段名，但尝试转换为中文显示
                String displayName = displayNameMapping.getOrDefault(serverHeader, serverHeader);
                double width = getColumnWidthFromConfig(0, displayName, calculateColumnWidth(displayName));
                addDynamicColumn(displayName, "bz", width, columnsToAdd); // 默认映射到备注字段
            }
        }
    }
    
    /**
     * 创建预约进仓状态的字段映射 - 将服务器表头映射到WarehouseEntry字段
     */
    private Map<String, String> createPreBookingFieldMapping() {
        Map<String, String> mapping = new HashMap<>();
        
        // 基于实际API返回的表头进行映射
        mapping.put("jcbh", "jcbh");           // 进仓编号
        mapping.put("zyh", "jczyh");           // 作业号
        mapping.put("yyh", "yyh");             // 预约号 -> 预约号字段
        mapping.put("yjrq", "yjrq");           // 预进日期
        mapping.put("jcrq", "jcrq");           // 进仓日期
        mapping.put("srrq", "srrq");           // 录入日期 -> 录入日期字段
        mapping.put("shrq", "jcrq");           // 审核日期 -> 进仓日期字段
        mapping.put("lf", "lf");               // L/F
        mapping.put("bzgg", "bzgg");           // 包装规格
        mapping.put("hwmc", "hwmc");           // 货物名称
        mapping.put("mt", "mt");               // 唛头
        mapping.put("hh", "hh");               // 货号
        mapping.put("js", "js");               // 件数
        mapping.put("tj", "tj");               // 体积
        mapping.put("mz", "mz");               // 毛重
        mapping.put("shdw", "shdw");           // 送货单位
        mapping.put("hz", "shdw");             // 货主 -> 送货单位字段
        mapping.put("fgsmc", "fgsmc");         // 分公司名称
        mapping.put("bz2", "bz");              // 备注2 -> 备注字段
        mapping.put("yyjcbz", "bz");           // 预约进仓备注 -> 备注字段
        mapping.put("hdmc", "hd");             // 货代名称 -> 货代字段
        mapping.put("hd", "hd");               // 货代
        mapping.put("ch", "ch");               // 车牌号
        mapping.put("jcid", "jcid");           // 进仓ID
        mapping.put("yyjcid", "jcid");         // 预约进仓ID -> 进仓ID字段
        mapping.put("shzt", "bgzt");           // 审核状态 -> 报关状态字段
        mapping.put("jsy", "jsy");             // 司机姓名
        mapping.put("driverdh", "jsydh");      // 司机电话（新API）
        mapping.put("jsydh", "jsydh");         // 司机电话
        mapping.put("yqqdsj", "yqqdsj");       // 要求取单时间
        
        return mapping;
    }
    
    /**
     * 创建预约进仓英文表头到中文显示名称的映射 - 基于实际API响应
     */
    private Map<String, String> createPreBookingDisplayNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        
        // 基于实际API响应的字段映射
        mapping.put("jcbh", "进仓编号");
        mapping.put("zyh", "作业号");
        mapping.put("yyh", "预约号");
        mapping.put("yjrq", "预进日期");
        mapping.put("srrq", "录入日期");
        mapping.put("shrq", "审核日期");
        mapping.put("shzt", "审核状态");
        mapping.put("yqqdsj", "要求取单时间");
        mapping.put("yyjcid", "预约进仓ID");
        mapping.put("jcid", "进仓ID");
        mapping.put("lf", "L/F");
        mapping.put("bzgg", "包装规格");
        mapping.put("hwmc", "货物名称");
        mapping.put("hwmc1", "货物名称");
        mapping.put("mt", "唛头");
        mapping.put("hh", "货号");
        mapping.put("js", "件数");
        mapping.put("tj", "体积");
        mapping.put("mz", "毛重");
        mapping.put("ts", "托数");
        mapping.put("shdw", "送货单位");
        mapping.put("hz", "货主");
        mapping.put("fgsmc", "分公司名称");
        mapping.put("hdmc", "货代");
        mapping.put("hd", "货代");
        mapping.put("ch", "车牌号");
        mapping.put("jsy", "司机姓名");
        mapping.put("driverdh", "司机电话");
        mapping.put("jsydh", "司机电话");
        mapping.put("yyjcbz", "预约备注");
        mapping.put("bz", "备注");
        mapping.put("bz2", "备注2");
        
        return mapping;
    }
    
    /**
     * 添加动态列 - 改进版本，确保配置保存正常工作
     */
    private void addDynamicColumn(String displayName, String fieldName, double width, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        TableColumn<WarehouseEntry, Object> column = new TableColumn<>(displayName);
        column.setCellValueFactory(new PropertyValueFactory<>(fieldName));
        column.setPrefWidth(width);
        column.setCellFactory(col -> createCustomCell());
        
        columnsToAdd.add(column);
        
        // 延迟添加监听器，确保列已经完全初始化
        Platform.runLater(() -> {
            // 添加列宽度变化监听器，自动保存配置
            column.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() > 0) {
                    saveColumnWidthConfig(currentStatusIndex, displayName, newVal.doubleValue());
                    System.out.println("保存列宽度: " + displayName + " = " + newVal.doubleValue());
                }
            });
        });
    }
    
    /**
     * 设置预约进仓状态的默认列结构
     */
    private void setupDefaultPreBookingColumns(List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        // 预约进仓状态的默认列结构
        addDynamicColumn("进仓编号", "jcbh", getColumnWidthFromConfig(0, "进仓编号", 100), columnsToAdd);
        addDynamicColumn("预约号", "yyh", getColumnWidthFromConfig(0, "预约号", 120), columnsToAdd);
        addDynamicColumn("作业号", "jczyh", getColumnWidthFromConfig(0, "作业号", 100), columnsToAdd);
        addDynamicColumn("录入日期", "srrq", getColumnWidthFromConfig(0, "录入日期", 140), columnsToAdd);
        addDynamicColumn("审核日期", "jcrq", getColumnWidthFromConfig(0, "审核日期", 140), columnsToAdd);
        addDynamicColumn("审核状态", "bgzt", getColumnWidthFromConfig(0, "审核状态", 80), columnsToAdd);
        addDynamicColumn("要求取单时间", "yqqdsj", getColumnWidthFromConfig(0, "要求取单时间", 180), columnsToAdd);
        addDynamicColumn("包装规格", "bzgg", getColumnWidthFromConfig(0, "包装规格", 80), columnsToAdd);
        addDynamicColumn("货物名称", "hwmc", getColumnWidthFromConfig(0, "货物名称", 100), columnsToAdd);
        addDynamicColumn("唛头", "mt", getColumnWidthFromConfig(0, "唛头", 100), columnsToAdd);
        addDynamicColumn("件数", "js", getColumnWidthFromConfig(0, "件数", 60), columnsToAdd);
        addDynamicColumn("体积", "tj", getColumnWidthFromConfig(0, "体积", 60), columnsToAdd);
        addDynamicColumn("毛重", "mz", getColumnWidthFromConfig(0, "毛重", 60), columnsToAdd);
        addDynamicColumn("送货单位", "shdw", getColumnWidthFromConfig(0, "送货单位", 150), columnsToAdd);
        addDynamicColumn("货代", "hd", getColumnWidthFromConfig(0, "货代", 120), columnsToAdd);
        addDynamicColumn("车牌号", "ch", getColumnWidthFromConfig(0, "车牌号", 100), columnsToAdd);
        addDynamicColumn("司机姓名", "jsy", getColumnWidthFromConfig(0, "司机姓名", 100), columnsToAdd);
        addDynamicColumn("司机电话", "jsydh", getColumnWidthFromConfig(0, "司机电话", 120), columnsToAdd);
        addDynamicColumn("分公司名称", "fgsmc", getColumnWidthFromConfig(0, "分公司名称", 150), columnsToAdd);
        addDynamicColumn("备注", "bz", getColumnWidthFromConfig(0, "备注", 200), columnsToAdd);
    }
    
    /**
     * 从配置中获取列宽度 - 优先个人配置，然后默认配置，最后传入的默认值
     */
    private double getColumnWidthFromConfig(int statusIndex, String columnName, double defaultWidth) {
        // 优先检查个人配置
        String personalKey = AppConfig.PERSONAL_COLUMN_WIDTH_KEY_PREFIX + statusIndex + "_" + columnName;
        double personalWidth = prefs.getDouble(personalKey, -1);
        if (personalWidth > 0) {
            return personalWidth;
        }
        
        // 如果没有个人配置，检查应用默认配置
        AppConfig.DefaultColumnConfig defaultConfig = AppConfig.getDefaultColumnConfig(statusIndex);
        List<String> defaultColumnNames = defaultConfig.getColumnNames();
        List<Double> defaultColumnWidths = defaultConfig.getColumnWidths();
        
        int configIndex = defaultColumnNames.indexOf(columnName);
        if (configIndex >= 0 && configIndex < defaultColumnWidths.size()) {
            return defaultColumnWidths.get(configIndex);
        }
        
        // 最后使用传入的默认宽度
        return defaultWidth;
    }
    
    /**
     * 保存列宽度配置 - 保存为个人配置
     */
    private void saveColumnWidthConfig(int statusIndex, String columnName, double width) {
        // 保存为个人配置
        String personalKey = AppConfig.PERSONAL_COLUMN_WIDTH_KEY_PREFIX + statusIndex + "_" + columnName;
        prefs.putDouble(personalKey, width);
    }
    
    /**
     * 设置进仓状态的列结构 - 完整30个字段版本
     */
    private void setupInboundColumns(List<String> headers, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        // 根据API返回的30个字段创建完整的列结构
        // 按照您提供的字段顺序：jcid, zyh, jcrq, jcbh, hz, mt, yjjs, yjtj, yjmz, js, tj, mz, kcjs, kctj, kcmz, bzgg, hwmc, xhrq, xhrq2, yjrq, lf, hwmc1, ch, driverdh, cleng, chengzhong, yyh, bz2, hdmc, fgsmc
        
        addDynamicColumn("进仓ID", "jcid", getColumnWidthFromConfig(currentStatusIndex, "进仓ID", 80), columnsToAdd);
        addDynamicColumn("作业号", "zyh", getColumnWidthFromConfig(currentStatusIndex, "作业号", 100), columnsToAdd);
        addDynamicColumn("进仓日期", "jcrq", getColumnWidthFromConfig(currentStatusIndex, "进仓日期", 140), columnsToAdd);
        addDynamicColumn("进仓编号", "jcbh", getColumnWidthFromConfig(currentStatusIndex, "进仓编号", 100), columnsToAdd);
        addDynamicColumn("货主", "hz", getColumnWidthFromConfig(currentStatusIndex, "货主", 150), columnsToAdd);
        addDynamicColumn("唛头", "mt", getColumnWidthFromConfig(currentStatusIndex, "唛头", 120), columnsToAdd);
        addDynamicColumn("预计件数", "yjjs", getColumnWidthFromConfig(currentStatusIndex, "预计件数", 80), columnsToAdd);
        addDynamicColumn("预计体积", "yjtj", getColumnWidthFromConfig(currentStatusIndex, "预计体积", 80), columnsToAdd);
        addDynamicColumn("预计毛重", "yjmz", getColumnWidthFromConfig(currentStatusIndex, "预计毛重", 80), columnsToAdd);
        addDynamicColumn("件数", "js", getColumnWidthFromConfig(currentStatusIndex, "件数", 60), columnsToAdd);
        addDynamicColumn("体积", "tj", getColumnWidthFromConfig(currentStatusIndex, "体积", 60), columnsToAdd);
        addDynamicColumn("毛重", "mz", getColumnWidthFromConfig(currentStatusIndex, "毛重", 60), columnsToAdd);
        addDynamicColumn("库存件数", "kcjs", getColumnWidthFromConfig(currentStatusIndex, "库存件数", 80), columnsToAdd);
        addDynamicColumn("库存体积", "kctj", getColumnWidthFromConfig(currentStatusIndex, "库存体积", 80), columnsToAdd);
        addDynamicColumn("库存毛重", "kcmz", getColumnWidthFromConfig(currentStatusIndex, "库存毛重", 80), columnsToAdd);
        addDynamicColumn("包装规格", "bzgg", getColumnWidthFromConfig(currentStatusIndex, "包装规格", 80), columnsToAdd);
        addDynamicColumn("货物名称", "hwmc", getColumnWidthFromConfig(currentStatusIndex, "货物名称", 120), columnsToAdd);
        addDynamicColumn("卸货日期", "xhrq", getColumnWidthFromConfig(currentStatusIndex, "卸货日期", 140), columnsToAdd);
        addDynamicColumn("卸货完成", "xhrq2", getColumnWidthFromConfig(currentStatusIndex, "卸货完成", 140), columnsToAdd);
        addDynamicColumn("预进日期", "yjrq", getColumnWidthFromConfig(currentStatusIndex, "预进日期", 140), columnsToAdd);
        addDynamicColumn("L/F", "lf", getColumnWidthFromConfig(currentStatusIndex, "L/F", 50), columnsToAdd);
        addDynamicColumn("货物名称1", "hwmc1", getColumnWidthFromConfig(currentStatusIndex, "货物名称1", 120), columnsToAdd);
        addDynamicColumn("车号", "ch", getColumnWidthFromConfig(currentStatusIndex, "车号", 100), columnsToAdd);
        addDynamicColumn("司机电话", "driverdh", getColumnWidthFromConfig(currentStatusIndex, "司机电话", 120), columnsToAdd);
        addDynamicColumn("车长", "cleng", getColumnWidthFromConfig(currentStatusIndex, "车长", 60), columnsToAdd);
        addDynamicColumn("承重", "chengzhong", getColumnWidthFromConfig(currentStatusIndex, "承重", 60), columnsToAdd);
        addDynamicColumn("运单号", "yyh", getColumnWidthFromConfig(currentStatusIndex, "运单号", 120), columnsToAdd);
        addDynamicColumn("备注", "bz2", getColumnWidthFromConfig(currentStatusIndex, "备注", 200), columnsToAdd);
        addDynamicColumn("货代名称", "hdmc", getColumnWidthFromConfig(currentStatusIndex, "货代名称", 120), columnsToAdd);
        addDynamicColumn("分公司", "fgsmc", getColumnWidthFromConfig(currentStatusIndex, "分公司", 120), columnsToAdd);
    }
    
    /**
     * 设置库存状态的列结构 - 完整30个字段版本（与进仓状态相同）
     */
    private void setupInventoryColumns(List<String> headers, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        // 库存状态与进仓状态使用相同的30个字段结构
        // 按照您提供的字段顺序：jcid, zyh, jcrq, jcbh, hz, mt, yjjs, yjtj, yjmz, js, tj, mz, kcjs, kctj, kcmz, bzgg, hwmc, xhrq, xhrq2, yjrq, lf, hwmc1, ch, driverdh, cleng, chengzhong, yyh, bz2, hdmc, fgsmc
        
        addDynamicColumn("进仓ID", "jcid", getColumnWidthFromConfig(currentStatusIndex, "进仓ID", 80), columnsToAdd);
        addDynamicColumn("作业号", "zyh", getColumnWidthFromConfig(currentStatusIndex, "作业号", 100), columnsToAdd);
        addDynamicColumn("进仓日期", "jcrq", getColumnWidthFromConfig(currentStatusIndex, "进仓日期", 140), columnsToAdd);
        addDynamicColumn("进仓编号", "jcbh", getColumnWidthFromConfig(currentStatusIndex, "进仓编号", 100), columnsToAdd);
        addDynamicColumn("货主", "hz", getColumnWidthFromConfig(currentStatusIndex, "货主", 150), columnsToAdd);
        addDynamicColumn("唛头", "mt", getColumnWidthFromConfig(currentStatusIndex, "唛头", 120), columnsToAdd);
        addDynamicColumn("预计件数", "yjjs", getColumnWidthFromConfig(currentStatusIndex, "预计件数", 80), columnsToAdd);
        addDynamicColumn("预计体积", "yjtj", getColumnWidthFromConfig(currentStatusIndex, "预计体积", 80), columnsToAdd);
        addDynamicColumn("预计毛重", "yjmz", getColumnWidthFromConfig(currentStatusIndex, "预计毛重", 80), columnsToAdd);
        addDynamicColumn("件数", "js", getColumnWidthFromConfig(currentStatusIndex, "件数", 60), columnsToAdd);
        addDynamicColumn("体积", "tj", getColumnWidthFromConfig(currentStatusIndex, "体积", 60), columnsToAdd);
        addDynamicColumn("毛重", "mz", getColumnWidthFromConfig(currentStatusIndex, "毛重", 60), columnsToAdd);
        addDynamicColumn("库存件数", "kcjs", getColumnWidthFromConfig(currentStatusIndex, "库存件数", 80), columnsToAdd);
        addDynamicColumn("库存体积", "kctj", getColumnWidthFromConfig(currentStatusIndex, "库存体积", 80), columnsToAdd);
        addDynamicColumn("库存毛重", "kcmz", getColumnWidthFromConfig(currentStatusIndex, "库存毛重", 80), columnsToAdd);
        addDynamicColumn("包装规格", "bzgg", getColumnWidthFromConfig(currentStatusIndex, "包装规格", 80), columnsToAdd);
        addDynamicColumn("货物名称", "hwmc", getColumnWidthFromConfig(currentStatusIndex, "货物名称", 120), columnsToAdd);
        addDynamicColumn("卸货日期", "xhrq", getColumnWidthFromConfig(currentStatusIndex, "卸货日期", 140), columnsToAdd);
        addDynamicColumn("卸货完成", "xhrq2", getColumnWidthFromConfig(currentStatusIndex, "卸货完成", 140), columnsToAdd);
        addDynamicColumn("预进日期", "yjrq", getColumnWidthFromConfig(currentStatusIndex, "预进日期", 140), columnsToAdd);
        addDynamicColumn("L/F", "lf", getColumnWidthFromConfig(currentStatusIndex, "L/F", 50), columnsToAdd);
        addDynamicColumn("货物名称1", "hwmc1", getColumnWidthFromConfig(currentStatusIndex, "货物名称1", 120), columnsToAdd);
        addDynamicColumn("车号", "ch", getColumnWidthFromConfig(currentStatusIndex, "车号", 100), columnsToAdd);
        addDynamicColumn("司机电话", "driverdh", getColumnWidthFromConfig(currentStatusIndex, "司机电话", 120), columnsToAdd);
        addDynamicColumn("车长", "cleng", getColumnWidthFromConfig(currentStatusIndex, "车长", 60), columnsToAdd);
        addDynamicColumn("承重", "chengzhong", getColumnWidthFromConfig(currentStatusIndex, "承重", 60), columnsToAdd);
        addDynamicColumn("运单号", "yyh", getColumnWidthFromConfig(currentStatusIndex, "运单号", 120), columnsToAdd);
        addDynamicColumn("备注", "bz2", getColumnWidthFromConfig(currentStatusIndex, "备注", 200), columnsToAdd);
        addDynamicColumn("货代名称", "hdmc", getColumnWidthFromConfig(currentStatusIndex, "货代名称", 120), columnsToAdd);
        addDynamicColumn("分公司", "fgsmc", getColumnWidthFromConfig(currentStatusIndex, "分公司", 120), columnsToAdd);
    }
    
    /**
     * 设置出仓状态的列结构
     */
    private void setupOutboundColumns(List<String> headers, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        Map<String, String> headerMapping = createOutboundHeaderMapping();
        
        // 出仓状态的列结构
        addColumnWithMapping("进仓编号", "jcbh", 100, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("进仓作业号", "jczyh", 100, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("预进日期", "yjrq", 140, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("进仓日期", "jcrq", 140, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("出仓日期", "ccrq", 140, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("L/F", "lf", 50, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("包装规格", "bzgg", 120, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("货物名称", "hwmc", 150, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("唛头", "mt", 120, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("货号", "hh", 80, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("件数", "js", 60, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("体积", "tj", 60, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("毛重", "mz", 60, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("托数", "ts", 60, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("送货单位", "shdw", 150, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("提货单位", "thdw", 150, headers, headerMapping, columnsToAdd);
        addColumnWithMapping("备注", "bz", 200, headers, headerMapping, columnsToAdd);
    }
    
    /**
     * 添加一列，使用映射查找表头名称 - 改进版本，支持自动保存列宽度
     */
    private <T> void addColumnWithMapping(String defaultName, String propertyName, double width, 
                                         List<String> headers, Map<String, String> headerMapping, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        String displayName = findHeaderName(defaultName, headers, headerMapping);
        
        // 从配置中获取保存的列宽度
        double savedWidth = getColumnWidthFromConfig(currentStatusIndex, displayName, width);
        
        TableColumn<WarehouseEntry, T> column = new TableColumn<>(displayName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(savedWidth);
        column.setCellFactory(col -> createCustomCell());
        
        columnsToAdd.add(column);
        
        // 延迟添加监听器，确保列已经完全初始化
        Platform.runLater(() -> {
            // 添加列宽度变化监听器，自动保存配置
            column.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() > 0) {
                    saveColumnWidthConfig(currentStatusIndex, displayName, newVal.doubleValue());
                    System.out.println("保存列宽度: " + displayName + " = " + newVal.doubleValue());
                }
            });
        });
    }
    
    /**
     * 查找表头名称
     */
    private String findHeaderName(String defaultName, List<String> headers, Map<String, String> headerMapping) {
        // 首先尝试直接匹配
        if (headers.contains(defaultName)) {
            return defaultName;
        }
        
        // 然后尝试通过映射查找
        for (String header : headers) {
            String mappedColumn = headerMapping.get(header);
            if (mappedColumn != null && mappedColumn.equals(defaultName)) {
                return header;
            }
        }
        
        return defaultName; // 没有找到匹配项，使用默认名称
    }
    
    /**
     * 创建预约进仓状态的表头映射
     */
    private Map<String, String> createPreBookingHeaderMapping() {
        Map<String, String> map = new HashMap<>();
        map.put("进仓编号", "进仓编号");
        map.put("作业号", "进仓作业号");
        map.put("预进日期", "预进日期");
        map.put("进仓日期", "进仓日期");
        map.put("L/F", "L/F");
        map.put("包装规格", "包装规格");
        map.put("货物名称", "货物名称");
        map.put("唛头", "唛头");
        map.put("货号", "货号");
        map.put("件数", "件数");
        map.put("体积", "体积");
        map.put("毛重", "毛重");
        map.put("送货单位", "送货单位");
        map.put("备注", "备注");
        return map;
    }
    
    /**
     * 创建进仓状态的表头映射
     */
    private Map<String, String> createInboundHeaderMapping() {
        Map<String, String> map = new HashMap<>();
        map.put("进仓编号", "进仓编号");
        map.put("进仓作业号", "进仓作业号");
        map.put("作业号", "进仓作业号");
        map.put("预进日期", "预进日期");
        map.put("进仓日期", "进仓日期");
        map.put("L/F", "L/F");
        map.put("包装规格", "包装规格");
        map.put("货物名称", "货物名称");
        map.put("唛头", "唛头");
        map.put("货号", "货号");
        map.put("件数", "件数");
        map.put("体积", "体积");
        map.put("库存体积", "库存体积");
        map.put("毛重", "毛重");
        map.put("托数", "托数");
        map.put("送货单位", "送货单位");
        map.put("库存件数", "库存件数");
        map.put("报关状态", "报关状态");
        map.put("备注", "备注");
        return map;
    }
    
    /**
     * 创建库存状态的表头映射
     */
    private Map<String, String> createInventoryHeaderMapping() {
        Map<String, String> map = new HashMap<>();
        map.put("进仓编号", "进仓编号");
        map.put("进仓作业号", "进仓作业号");
        map.put("作业号", "进仓作业号");
        map.put("预进日期", "预进日期");
        map.put("进仓日期", "进仓日期");
        map.put("L/F", "L/F");
        map.put("包装规格", "包装规格");
        map.put("货物名称", "货物名称");
        map.put("唛头", "唛头");
        map.put("货号", "货号");
        map.put("件数", "件数");
        map.put("体积", "体积");
        map.put("库存体积", "库存体积");
        map.put("毛重", "毛重");
        map.put("托数", "托数");
        map.put("送货单位", "送货单位");
        map.put("库存件数", "库存件数");
        map.put("报关状态", "报关状态");
        map.put("备注", "备注");
        return map;
    }
    
    /**
     * 创建出仓状态的表头映射
     */
    private Map<String, String> createOutboundHeaderMapping() {
        Map<String, String> map = new HashMap<>();
        map.put("进仓编号", "进仓编号");
        map.put("进仓作业号", "进仓作业号");
        map.put("作业号", "进仓作业号");
        map.put("预进日期", "预进日期");
        map.put("进仓日期", "进仓日期");
        map.put("出仓日期", "出仓日期");
        map.put("L/F", "L/F");
        map.put("包装规格", "包装规格");
        map.put("货物名称", "货物名称");
        map.put("唛头", "唛头");
        map.put("货号", "货号");
        map.put("件数", "件数");
        map.put("体积", "体积");
        map.put("毛重", "毛重");
        map.put("托数", "托数");
        map.put("送货单位", "送货单位");
        map.put("提货单位", "提货单位");
        map.put("备注", "备注");
        return map;
    }
    
    /**
     * 使用预定义结构设置列（原有方法）
     */
    private void setupPredefinedColumns() {
        // 添加进仓编号列（额外添加的列）
        TableColumn<WarehouseEntry, String> jcbhCol = new TableColumn<>("进仓编号");
        jcbhCol.setCellValueFactory(new PropertyValueFactory<>("jcbh"));
        jcbhCol.setPrefWidth(100);
        jcbhCol.setCellFactory(col -> createCustomCell());
        
        // 添加进仓作业号列
        TableColumn<WarehouseEntry, String> jczyhCol = new TableColumn<>("进仓作业号");
        jczyhCol.setCellValueFactory(new PropertyValueFactory<>("jczyh"));
        jczyhCol.setPrefWidth(100);
        jczyhCol.setCellFactory(col -> createCustomCell());
        
        // 添加预进日期列
        TableColumn<WarehouseEntry, String> yjrqCol = new TableColumn<>("预进日期");
        yjrqCol.setCellValueFactory(new PropertyValueFactory<>("yjrq"));
        yjrqCol.setPrefWidth(140);
        yjrqCol.setCellFactory(col -> createCustomCell());
        
        // 添加进仓日期列
        TableColumn<WarehouseEntry, String> jcrqCol = new TableColumn<>("进仓日期");
        jcrqCol.setCellValueFactory(new PropertyValueFactory<>("jcrq"));
        jcrqCol.setPrefWidth(140);
        jcrqCol.setCellFactory(col -> createCustomCell());
        
        // 添加L/F列
        TableColumn<WarehouseEntry, String> lfCol = new TableColumn<>("L/F");
        lfCol.setCellValueFactory(new PropertyValueFactory<>("lf"));
        lfCol.setPrefWidth(50);
        lfCol.setCellFactory(col -> createCustomCell());
        
        // 添加包装规格列
        TableColumn<WarehouseEntry, String> bzggCol = new TableColumn<>("包装规格");
        bzggCol.setCellValueFactory(new PropertyValueFactory<>("bzgg"));
        bzggCol.setPrefWidth(80);
        bzggCol.setCellFactory(col -> createCustomCell());
        
        // 添加货物名称列
        TableColumn<WarehouseEntry, String> hwmcCol = new TableColumn<>("货物名称");
        hwmcCol.setCellValueFactory(new PropertyValueFactory<>("hwmc"));
        hwmcCol.setPrefWidth(100);
        hwmcCol.setCellFactory(col -> createCustomCell());
        
        // 添加唛头列
        TableColumn<WarehouseEntry, String> mtCol = new TableColumn<>("唛头");
        mtCol.setCellValueFactory(new PropertyValueFactory<>("mt"));
        mtCol.setPrefWidth(100);
        mtCol.setCellFactory(col -> createCustomCell());
        
        // 添加货号列
        TableColumn<WarehouseEntry, String> hhCol = new TableColumn<>("货号");
        hhCol.setCellValueFactory(new PropertyValueFactory<>("hh"));
        hhCol.setPrefWidth(80);
        hhCol.setCellFactory(col -> createCustomCell());
        
        // 添加件数列
        TableColumn<WarehouseEntry, Integer> jsCol = new TableColumn<>("件数");
        jsCol.setCellValueFactory(new PropertyValueFactory<>("js"));
        jsCol.setPrefWidth(60);
        jsCol.setCellFactory(col -> createCustomCell());
        
        // 添加体积列
        TableColumn<WarehouseEntry, Double> tjCol = new TableColumn<>("体积");
        tjCol.setCellValueFactory(new PropertyValueFactory<>("tj"));
        tjCol.setPrefWidth(60);
        tjCol.setCellFactory(col -> createCustomCell());
        
        // 添加库存体积列
        TableColumn<WarehouseEntry, Double> kctjCol = new TableColumn<>("库存体积");
        kctjCol.setCellValueFactory(new PropertyValueFactory<>("kctj"));
        kctjCol.setPrefWidth(80);
        kctjCol.setCellFactory(col -> createCustomCell());
        
        // 添加毛重列
        TableColumn<WarehouseEntry, Double> mzCol = new TableColumn<>("毛重");
        mzCol.setCellValueFactory(new PropertyValueFactory<>("mz"));
        mzCol.setPrefWidth(60);
        mzCol.setCellFactory(col -> createCustomCell());
        
        // 添加托数列
        TableColumn<WarehouseEntry, Integer> tsCol = new TableColumn<>("托数");
        tsCol.setCellValueFactory(new PropertyValueFactory<>("ts"));
        tsCol.setPrefWidth(60);
        tsCol.setCellFactory(col -> createCustomCell());
        
        // 添加送货单位列
        TableColumn<WarehouseEntry, String> shdwCol = new TableColumn<>("送货单位");
        shdwCol.setCellValueFactory(new PropertyValueFactory<>("shdw"));
        shdwCol.setPrefWidth(150);
        shdwCol.setCellFactory(col -> createCustomCell());
        
        // 添加库存件数列
        TableColumn<WarehouseEntry, Double> kcjsCol = new TableColumn<>("库存件数");
        kcjsCol.setCellValueFactory(new PropertyValueFactory<>("kcjs"));
        kcjsCol.setPrefWidth(80);
        kcjsCol.setCellFactory(col -> createCustomCell());
        
        // 添加报关状态列
        TableColumn<WarehouseEntry, String> bgztCol = new TableColumn<>("报关状态");
        bgztCol.setCellValueFactory(new PropertyValueFactory<>("bgzt"));
        bgztCol.setPrefWidth(80);
        bgztCol.setCellFactory(col -> createCustomCell());
        
        // 添加备注列
        TableColumn<WarehouseEntry, String> bzCol = new TableColumn<>("备注");
        bzCol.setCellValueFactory(new PropertyValueFactory<>("bz"));
        bzCol.setPrefWidth(150);
        bzCol.setCellFactory(col -> createCustomCell());
        
        resultTableView.getColumns().addAll(
                jcbhCol, jczyhCol, yjrqCol, jcrqCol, lfCol, bzggCol, hwmcCol, 
                mtCol, hhCol, jsCol, tjCol, kctjCol, mzCol, tsCol, shdwCol, 
                kcjsCol, bgztCol, bzCol
        );
        
        // 添加操作列
        addOperationColumn();
    }
    
    /**
     * 添加操作列（查看托信息和照片）
     */
    private void addOperationColumn() {
        TableColumn<WarehouseEntry, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(120);
        
        // 使用匿名Callback实现自定义单元格工厂
        actionCol.setCellFactory(column -> {
            final TableCell<WarehouseEntry, Void> cell = new TableCell<>() {
                private final HBox hbox = new HBox(5);
                private final Button txxBtn = new Button("托信息");
                private final Button zpBtn = new Button("照片");
                
                {
                    txxBtn.setOnAction(event -> {
                        WarehouseEntry entry = getTableRow().getItem();
                        if (entry != null && entry.getJcid() != null && !entry.getJcid().isEmpty()) {
                            openTxxPage(entry.getJcid(), entry.getJcbh());
                        }
                    });
                    
                    zpBtn.setOnAction(event -> {
                        WarehouseEntry entry = getTableRow().getItem();
                        if (entry != null && entry.getJcid() != null && !entry.getJcid().isEmpty()) {
                            openZpPage(entry.getJcid(), entry.getJcbh());
                        }
                    });
                    
                    hbox.getChildren().addAll(txxBtn, zpBtn);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setGraphic(null);
                    } else {
                        WarehouseEntry entry = getTableRow().getItem();
                        if (entry != null) {
                            // 如果是特殊记录（未找到记录或查询结果为空），禁用按钮
                            String bz = entry.getBz();
                            boolean isSpecial = bz != null && 
                                (bz.contains("未找到记录") || bz.contains("没有找到") || 
                                bz.contains("查询结果为空") || bz.contains("未能解析") || 
                                bz.contains("抱歉") || bz.contains("请检查"));
                            boolean hasJcid = entry.getJcid() != null && !entry.getJcid().isEmpty();
                            
                            txxBtn.setDisable(isSpecial || !hasJcid);
                            zpBtn.setDisable(isSpecial || !hasJcid);
                            setGraphic(hbox);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            };
            
            return cell;
        });
        
        resultTableView.getColumns().add(actionCol);
    }
    
    /**
     * 根据表头内容计算合适的列宽
     */
    private double calculateColumnWidth(String header) {
        int length = header.length();
        if (length <= 2) return 50;
        if (length <= 4) return 80;
        if (length <= 6) return 100;
        if (length <= 8) return 120;
        return 150;
    }
    
    /**
     * 打开托信息对话框
     * 由于可能缺少WebView模块，改为使用简单对话框提示用户
     */
    private void openTxxPage(String jcid, String jcbh) {
        // 创建访问URL
        String url = "http://60.190.0.98:81/csccmisHandler/website/jctxx.aspx?jcid=" + jcid + "&jcbh=" + jcbh;
        
        // 创建提示对话框，询问用户是否要在浏览器中打开
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("查看托信息");
        alert.setHeaderText("进仓编号：" + jcbh + " | 进仓ID：" + jcid);
        alert.setContentText("是否在外部浏览器中打开托信息页面？");
        
        ButtonType openButton = new ButtonType("打开");
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(openButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == openButton) {
            openWebPage(url);
        }
    }
    
    /**
     * 打开照片页面
     * 使用新的照片页面URL格式
     */
    private void openZpPage(String jcid, String jcbh) {
        // 使用AppConfig中的照片URL模板
        String url = AppConfig.getPhotoUrl(jcid);
        
        // 直接在浏览器中打开
        openWebPage(url);
        
        System.out.println("打开照片页面: " + url + " (进仓编号: " + jcbh + ")");
    }
    
    /**
     * 在系统默认浏览器中打开网页
     */
    private void openWebPage(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "打开页面失败", "无法打开网页：" + e.getMessage());
        }
    }
    
    /**
     * 查看日志文件
     */
    private void viewLogs() {
        try {
            // 检查日志文件是否存在
            if (!Files.exists(Paths.get(AppConfig.REQUEST_LOG_FILE))) {
                Files.write(Paths.get(AppConfig.REQUEST_LOG_FILE), "暂无请求日志".getBytes(StandardCharsets.UTF_8));
            }
            
            if (!Files.exists(Paths.get(AppConfig.RESPONSE_LOG_FILE))) {
                Files.write(Paths.get(AppConfig.RESPONSE_LOG_FILE), "暂无响应日志".getBytes(StandardCharsets.UTF_8));
            }
            
            // 读取日志内容
            String requestLog = Files.readString(Paths.get(AppConfig.REQUEST_LOG_FILE), StandardCharsets.UTF_8);
            String responseLog = Files.readString(Paths.get(AppConfig.RESPONSE_LOG_FILE), StandardCharsets.UTF_8);
            
            // 创建日志查看窗口
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("查看日志");
            alert.setHeaderText("请求和响应日志");
            
            // 创建标签页
            TabPane tabPane = new TabPane();
            
            // 请求日志标签页
            Tab requestTab = new Tab("请求日志");
            TextArea requestTextArea = new TextArea(requestLog);
            requestTextArea.setEditable(false);
            requestTextArea.setWrapText(true);
            requestTab.setContent(requestTextArea);
            
            // 响应日志标签页
            Tab responseTab = new Tab("响应日志");
            TextArea responseTextArea = new TextArea(responseLog);
            responseTextArea.setEditable(false);
            responseTextArea.setWrapText(true);
            responseTab.setContent(responseTextArea);
            
            // 错误分析标签页
            Tab errorTab = new Tab("错误分析");
            List<String> errors = extractErrorsFromLogs();
            String errorAnalysis = errors.isEmpty() ? "未发现明显错误" : String.join("\n", errors);
            TextArea errorTextArea = new TextArea(errorAnalysis);
            errorTextArea.setEditable(false);
            errorTextArea.setWrapText(true);
            errorTab.setContent(errorTextArea);
            
            tabPane.getTabs().addAll(requestTab, responseTab, errorTab);
            
            alert.getDialogPane().setContent(tabPane);
            alert.getDialogPane().setPrefSize(800, 600);
            alert.showAndWait();
            
        } catch (IOException e) {
            ExceptionHandler.handleFileException("查看日志文件", e);
        }
    }
    
    /**
     * 从日志文件中提取错误信息
     */
    private List<String> extractErrorsFromLogs() {
        List<String> errors = new ArrayList<>();
        
        try {
            if (Files.exists(Paths.get(AppConfig.RESPONSE_LOG_FILE))) {
                String responseLog = Files.readString(Paths.get(AppConfig.RESPONSE_LOG_FILE), StandardCharsets.UTF_8);
                
                // 提取错误信息行
                Pattern errorPattern = Pattern.compile("错误信息: (.+)");
                Matcher matcher = errorPattern.matcher(responseLog);
                
                while (matcher.find()) {
                    String error = matcher.group(1).trim();
                    if (!error.isEmpty() && !errors.contains(error)) {
                        errors.add(error);
                    }
                }
                
                // 提取警告行
                Pattern warningPattern = Pattern.compile("警告: (.+)");
                matcher = warningPattern.matcher(responseLog);
                
                while (matcher.find()) {
                    String warning = matcher.group(1).trim();
                    if (!warning.isEmpty() && !errors.contains(warning)) {
                        errors.add(warning);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("读取响应日志文件失败: " + e.getMessage());
        }
        
        return errors;
    }
    
    /**
     * 尝试修复进仓编号格式
     * 只进行基本清理，不做格式限制
     */
    private String fixJcbhFormat(String jcbh) {
        // 只去除空格，不做其他处理
        jcbh = jcbh.trim().replaceAll("\\s+", "");
        return jcbh;
    }
    
    @FXML
    private void handleQuery() {
        // 获取用户输入
        String jcbhInput = jcbhTextField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String status = statusComboBox.getValue();
        
        // 验证日期输入
        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "请选择开始日期和结束日期");
            return;
        }
        
        // 验证日期范围
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 180) {
            LocalDate newStartDate = endDate.minusDays(180);
            startDatePicker.setValue(newStartDate);
            startDate = newStartDate;
            
            showAlert(Alert.AlertType.WARNING, 
                     "日期范围过长", 
                     "查询日期范围已自动调整为180天，\n因为服务器限制查询范围不能超过180天。");
        }
        
        // 清空之前的结果
        entryList.clear();
        
        // 获取查询参数
        String jcbhText = jcbhInput;
        if (jcbhText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "输入错误", "请输入进仓编号");
            return;
        }
        
        // 分割进仓编号（支持逗号和空格分隔）
        List<String> jcbhList = Arrays.stream(jcbhText.split("[,\\s]+"))
                                     .filter(s -> !s.trim().isEmpty())
                                     .map(this::fixJcbhFormat)  // 只进行基本处理
                                     .collect(Collectors.toList());
        
        // 更新输入框中的内容为处理后的编号列表
        jcbhTextField.setText(String.join(", ", jcbhList));
        
        // 获取查询状态索引
        currentStatusIndex = statusComboBox.getSelectionModel().getSelectedIndex();
        
        // 直接传递状态索引，让服务层处理状态代码映射
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // 显示进度条和标签
        progressBar.setProgress(0);
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressLabel.setText("准备查询...");
        
        // 禁用查询按钮，防止重复点击
        queryButton.setDisable(true);
        
        // 更新查询结果标签
        queryResultLabel.setText("正在查询，请稍候...");
        
        // 创建后台任务，执行查询 - 传递状态索引
        service.queryWarehouse(jcbhList, currentStatusIndex, startDateStr, endDateStr)
            .thenAcceptAsync(entries -> {
                // 在JavaFX线程中更新UI
                Platform.runLater(() -> {
                    // 隐藏进度条
                    progressBar.setVisible(false);
                    progressLabel.setVisible(false);
                    
                    // 重新启用查询按钮
                    queryButton.setDisable(false);
                    
                    // 如果查询结果为空，显示信息
                    if (entries.isEmpty()) {
                        Platform.runLater(() -> {
                            queryResultLabel.setText("未找到任何记录");
                            showAlert(Alert.AlertType.INFORMATION, "查询结果", "未找到任何记录，请尝试调整查询条件");
                        });
                    } else {
                        // 检查是否是错误条目
                        boolean isErrorEntry = entries.size() == 1 && entries.get(0).getBz() != null && 
                                             (entries.get(0).getBz().contains("错误") || 
                                              entries.get(0).getBz().contains("API") || 
                                              entries.get(0).getBz().contains("日期范围") ||
                                              entries.get(0).getBz().contains("没有找到"));
                                             
                        if (isErrorEntry) {
                            // 这是一个错误条目，显示温和的提示信息
                            String errorMsg = entries.get(0).getBz();
                            Platform.runLater(() -> {
                                queryResultLabel.setText("提示: " + errorMsg);
                                queryResultLabel.setStyle("-fx-text-fill: #ff6600;"); // 橙色而不是红色
                                
                                // 如果是时间范围错误，自动调整日期
                                if (errorMsg.contains("日期范围") || errorMsg.contains("180天")) {
                                    // 确保日期范围不超过180天
                                    LocalDate end = endDatePicker.getValue();
                                    if (end != null) {
                                        LocalDate newStart = end.minusDays(179);
                                        startDatePicker.setValue(newStart);
                                        
                                        // 使用温和的信息提示而不是警告弹窗
                                        queryResultLabel.setText("日期范围已自动调整为180天，请重新查询");
                                        queryResultLabel.setStyle("-fx-text-fill: #0066cc;"); // 蓝色信息提示
                                    }
                                }
                                // 不再显示弹窗，只在状态栏显示信息
                            });
                        } else {
                            // 正常显示结果数量
                            final int count = entries.size();
                            Platform.runLater(() -> {
                                queryResultLabel.setText("查询完成，找到 " + count + " 条记录");
                                queryResultLabel.setStyle("");
                            });
                        }
                    }
                    
                    // 检查是否是错误消息记录
                    boolean hasNoClassDefError = false;
                    for (WarehouseEntry entry : entries) {
                        String bz = entry.getBz();
                        if (bz != null && bz.contains("NoClassDefFoundError")) {
                            hasNoClassDefError = true;
                            break;
                        }
                    }
                    
                    if (hasNoClassDefError) {
                        showAlert(Alert.AlertType.ERROR, "库缺失错误", 
                                 "系统缺少必要的JSON库。\n请安装org.json库后再试。\n" +
                                 "可以将json-20230227.jar文件添加到lib目录下解决此问题。");
                    }
                    
                    // 添加到表格
                    entryList.addAll(entries);
                    
                    // 更新统计
                    updateTotals();
                    
                    // 如果有列头信息，根据列头设置表格
                    if (WarehouseService.hasExtractedTableHeaders()) {
                        List<String> headers = WarehouseService.getExtractedTableHeaders();
                        System.out.println("从服务获取到表头: " + String.join(", ", headers));
                        setupColumnsForStatus(currentStatusIndex, headers);
                    }

                    // 列配置已在setupColumnsForStatus中加载，无需重复调用
                });
            })
            .exceptionally(ex -> {
                // 在JavaFX线程中处理异常
                Platform.runLater(() -> {
                    // 隐藏进度条
                    progressBar.setVisible(false);
                    progressLabel.setVisible(false);
                    
                    // 重新启用查询按钮
                    queryButton.setDisable(false);
                    
                    // 显示错误信息
                    queryResultLabel.setText("查询失败: " + ex.getMessage());
                    ex.printStackTrace();
                    
                    // 检查是否是NoClassDefFoundError
                    Throwable cause = ex;
                    while (cause != null) {
                        if (cause instanceof NoClassDefFoundError && cause.getMessage().contains("org/json/JSONObject")) {
                            showAlert(Alert.AlertType.ERROR, "库缺失错误", 
                                     "系统缺少必要的JSON库。\n请安装org.json库后再试。\n" +
                                     "可以将json-20230227.jar文件添加到lib目录下解决此问题。");
                            break;
                        }
                        cause = cause.getCause();
                    }
                });
                return null;
            });
    }
    
    private void updateTotals() {
        int totalJs = entryList.stream().mapToInt(WarehouseEntry::getJs).sum();
        double totalTj = entryList.stream().mapToDouble(WarehouseEntry::getTj).sum();
        double totalKctj = entryList.stream().mapToDouble(WarehouseEntry::getKctj).sum();
        double totalMz = entryList.stream().mapToDouble(WarehouseEntry::getMz).sum();
        double totalKcjs = entryList.stream().mapToDouble(WarehouseEntry::getKcjs).sum();
        
        totalJsLabel.setText(String.format("%d", totalJs));
        totalTjLabel.setText(String.format("%.2f", totalTj));
        totalKctjLabel.setText(String.format("%.2f", totalKctj));
        totalMzLabel.setText(String.format("%.2f", totalMz));
        totalKcjsLabel.setText(String.format("%.2f", totalKcjs));
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * 自定义列渲染器，处理特殊条目
     */
    private <T> TableCell<WarehouseEntry, T> createCustomCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                
                // 安全地获取TableRow和WarehouseEntry
                TableRow<WarehouseEntry> tableRow = getTableRow();
                if (tableRow != null) {
                    WarehouseEntry entry = tableRow.getItem();
                    if (entry != null) {
                        String bz = entry.getBz();
                        if (bz != null && (bz.contains("未找到记录") || bz.contains("没有找到") || 
                            bz.contains("查询结果为空") || bz.contains("未能解析") || 
                            bz.contains("抱歉") || bz.contains("请检查"))) {
                            // 对于特殊条目，设置红色背景
                            setStyle("-fx-background-color: #ffeeee;");
                        } else {
                            setStyle("");
                        }
                    } else {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }
                
                setText(item.toString());
                
                // 为单元格添加右键事件，但不为每个单元格创建独立的ContextMenu实例
                setOnContextMenuRequested(event -> {
                    if (getItem() != null && !getItem().toString().isEmpty()) {
                        // 使用共享的ContextMenu实例
                        showCellContextMenu(this, event.getScreenX(), event.getScreenY(), getItem().toString());
                    }
                });
            }
        };
    }
    
    // 为单元格显示右键菜单
    private void showCellContextMenu(TableCell<?, ?> cell, double x, double y, String content) {
        // 懒加载创建ContextMenu
        if (cellContextMenu == null) {
            cellContextMenu = new ContextMenu();
            MenuItem copyMenuItem = new MenuItem("复制内容");
            
            copyMenuItem.setOnAction(event -> {
                // 直接从菜单的userData获取最新文本内容
                String textToCopy = (String) cellContextMenu.getUserData();
                if (textToCopy != null && !textToCopy.isEmpty()) {
                    // 将单元格内容复制到剪贴板
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent clipContent = new javafx.scene.input.ClipboardContent();
                    clipContent.putString(textToCopy);
                    clipboard.setContent(clipContent);
                    
                    // 显示复制成功提示
                    Tooltip tooltip = new Tooltip("已复制到剪贴板");
                    tooltip.setAutoHide(true);
                    tooltip.show(cell, x, y + 10);
                    
                    // 1秒后自动隐藏提示
                    PauseTransition delay = new PauseTransition(Duration.seconds(1));
                    delay.setOnFinished(e -> tooltip.hide());
                    delay.play();
                }
            });
            
            cellContextMenu.getItems().add(copyMenuItem);
        }
        
        // 关键修改：将当前内容保存到菜单对象的userData属性
        cellContextMenu.setUserData(content);
        
        // 显示菜单
        cellContextMenu.show(cell, x, y);
    }
    
    /**
     * 处理退出应用程序
     */
    @FXML
    private void handleExit() {
        Alert confirmExit = new Alert(Alert.AlertType.CONFIRMATION);
        confirmExit.setTitle("退出确认");
        confirmExit.setHeaderText(null);
        confirmExit.setContentText("确定要退出应用程序吗？");
        
        Optional<ButtonType> result = confirmExit.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }
    
    /**
     * 导出数据到CSV文件
     */
    private void handleExport() {
        if (entryList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "导出提示", "当前没有数据可以导出");
            return;
        }
        
        try {
            // 生成CSV文件名，包含当前时间
            String timeStamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "仓库查询_" + timeStamp + ".csv";
            
            // 创建CSV内容
            StringBuilder csvContent = new StringBuilder();
            
            // 添加表头
            csvContent.append("进仓编号,进仓作业号,预进日期,进仓日期,L/F,包装规格,货物名称,唛头,货号,件数,体积,库存体积,毛重,托数,送货单位,库存件数,报关状态,备注\n");
            
            // 添加数据行
            for (WarehouseEntry entry : entryList) {
                csvContent.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%.2f,%.2f,%.2f,%d,%s,%.2f,%s,%s\n",
                        entry.getJcbh(), entry.getJczyh(), entry.getYjrq(), entry.getJcrq(),
                        entry.getLf(), entry.getBzgg(), entry.getHwmc(), entry.getMt(),
                        entry.getHh(), entry.getJs(), entry.getTj(), entry.getKctj(),
                        entry.getMz(), entry.getTs(), entry.getShdw(), entry.getKcjs(),
                        entry.getBgzt(), entry.getBz()));
            }
            
            // 写入文件
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
                writer.write(csvContent.toString());
            }
            
            showAlert(Alert.AlertType.INFORMATION, "导出成功", "数据已成功导出到文件：" + fileName);
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "导出失败", "导出数据时发生错误：" + e.getMessage());
        }
    }
    
    /**
     * 计算选中行的总和
     */
    private void calculateSelectedRowsSum() {
        List<WarehouseEntry> selectedEntries = resultTableView.getSelectionModel().getSelectedItems();
        
        if (selectedEntries.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "计算提示", "请先选择要计算的行");
            return;
        }
        
        int totalJs = selectedEntries.stream().mapToInt(WarehouseEntry::getJs).sum();
        double totalTj = selectedEntries.stream().mapToDouble(WarehouseEntry::getTj).sum();
        double totalKctj = selectedEntries.stream().mapToDouble(WarehouseEntry::getKctj).sum();
        double totalMz = selectedEntries.stream().mapToDouble(WarehouseEntry::getMz).sum();
        double totalKcjs = selectedEntries.stream().mapToDouble(WarehouseEntry::getKcjs).sum();
        
        String message = String.format(
                "选中行总计：\n件数：%d\n体积：%.2f\n库存体积：%.2f\n毛重：%.2f\n库存件数：%.2f",
                totalJs, totalTj, totalKctj, totalMz, totalKcjs);
        
        showAlert(Alert.AlertType.INFORMATION, "选中行总计", message);
    }
    
    /**
     * 保存当前列配置 - 个人配置，与默认配置分开保存
     */
    private void saveColumnConfiguration() {
        try {
            // 保存列顺序
            StringBuilder orderBuilder = new StringBuilder();
            for (int i = 0; i < resultTableView.getColumns().size(); i++) {
                String columnName = resultTableView.getColumns().get(i).getText();
                if (columnName != null && !columnName.isEmpty()) {
                    if (orderBuilder.length() > 0) {
                        orderBuilder.append(",");
                    }
                    orderBuilder.append(columnName);
                }
            }
            String orderKey = AppConfig.PERSONAL_COLUMN_ORDER_KEY_PREFIX + currentStatusIndex;
            prefs.put(orderKey, orderBuilder.toString());
            
            // 保存列可见性
            for (TableColumn<WarehouseEntry, ?> column : resultTableView.getColumns()) {
                String columnName = column.getText();
                if (columnName != null && !columnName.isEmpty()) {
                    String key = AppConfig.PERSONAL_COLUMN_VISIBLE_KEY_PREFIX + currentStatusIndex + "_" + columnName;
                    prefs.putBoolean(key, column.isVisible());
                }
            }
            
            // 保存列宽度
            for (TableColumn<WarehouseEntry, ?> column : resultTableView.getColumns()) {
                String columnName = column.getText();
                if (columnName != null && !columnName.isEmpty()) {
                    String key = AppConfig.PERSONAL_COLUMN_WIDTH_KEY_PREFIX + currentStatusIndex + "_" + columnName;
                    prefs.putDouble(key, column.getWidth());
                }
            }
            
            System.out.println("保存个人列配置 - 状态: " + AppConfig.getStatusName(currentStatusIndex));
            System.out.println("保存的列顺序: " + orderBuilder.toString());
            
            showAlert(Alert.AlertType.INFORMATION, "配置保存", 
                     "状态 \"" + AppConfig.getStatusName(currentStatusIndex) + "\" 的个人列配置已保存\n" +
                     "注意：这是您的个人配置，不会影响默认配置");
            
        } catch (Exception e) {
            ExceptionHandler.handleException("保存列配置", e);
        }
    }
    
    /**
     * 加载列配置 - 优先加载个人配置，如果没有则使用默认配置
     */
    private void loadColumnConfiguration() {
        // 延迟加载配置，确保所有列都已经创建完成
        Platform.runLater(() -> {
            try {
                // 检查是否有个人配置
                String personalOrderKey = AppConfig.PERSONAL_COLUMN_ORDER_KEY_PREFIX + currentStatusIndex;
                String personalOrder = prefs.get(personalOrderKey, "");
                boolean hasPersonalConfig = !personalOrder.isEmpty();
                
                System.out.println("加载列配置 - 状态: " + AppConfig.getStatusName(currentStatusIndex));
                System.out.println("是否有个人配置: " + hasPersonalConfig);
                
                if (hasPersonalConfig) {
                    // 加载个人配置
                    loadPersonalColumnConfiguration();
                } else {
                    // 使用默认配置
                    loadDefaultColumnConfiguration();
                }
                
            } catch (Exception e) {
                ExceptionHandler.handleException("加载列配置", e);
            }
        });
    }
    
    /**
     * 加载个人列配置
     */
    private void loadPersonalColumnConfiguration() {
        System.out.println("加载个人列配置...");
        
        // 加载列可见性和宽度
        for (TableColumn<WarehouseEntry, ?> column : resultTableView.getColumns()) {
            String columnName = column.getText();
            if (columnName != null && !columnName.isEmpty()) {
                // 加载可见性
                String visibleKey = AppConfig.PERSONAL_COLUMN_VISIBLE_KEY_PREFIX + currentStatusIndex + "_" + columnName;
                boolean isVisible = prefs.getBoolean(visibleKey, true);
                column.setVisible(isVisible);
                
                // 加载宽度
                String widthKey = AppConfig.PERSONAL_COLUMN_WIDTH_KEY_PREFIX + currentStatusIndex + "_" + columnName;
                double savedWidth = prefs.getDouble(widthKey, -1);
                if (savedWidth > 0) {
                    column.setPrefWidth(savedWidth);
                }
                
                // 添加监听器，自动保存个人配置变化
                column.visibleProperty().addListener((obs, oldVal, newVal) -> {
                    prefs.putBoolean(visibleKey, newVal);
                    System.out.println("自动保存个人列可见性: " + columnName + " = " + newVal);
                });
                
                column.widthProperty().addListener((obs, oldVal, newVal) -> {
                    prefs.putDouble(widthKey, newVal.doubleValue());
                    System.out.println("自动保存个人列宽度: " + columnName + " = " + newVal.doubleValue());
                });
            }
        }
        
        // 加载列顺序
        String orderKey = AppConfig.PERSONAL_COLUMN_ORDER_KEY_PREFIX + currentStatusIndex;
        String savedOrder = prefs.get(orderKey, "");
        if (!savedOrder.isEmpty()) {
            applyColumnOrder(savedOrder);
        }
    }
    
    /**
     * 加载默认列配置
     */
    private void loadDefaultColumnConfiguration() {
        System.out.println("加载默认列配置...");
        
        // 获取默认配置
        AppConfig.DefaultColumnConfig defaultConfig = AppConfig.getDefaultColumnConfig(currentStatusIndex);
        List<String> defaultColumnNames = defaultConfig.getColumnNames();
        List<Double> defaultColumnWidths = defaultConfig.getColumnWidths();
        List<Boolean> defaultColumnVisible = defaultConfig.getColumnVisible();
        
        // 应用默认配置
        for (TableColumn<WarehouseEntry, ?> column : resultTableView.getColumns()) {
            String columnName = column.getText();
            if (columnName != null && !columnName.isEmpty()) {
                
                // 查找列在默认配置中的索引
                int configIndex = defaultColumnNames.indexOf(columnName);
                if (configIndex >= 0) {
                    // 应用默认宽度
                    if (configIndex < defaultColumnWidths.size()) {
                        column.setPrefWidth(defaultColumnWidths.get(configIndex));
                    }
                    
                    // 应用默认可见性
                    if (configIndex < defaultColumnVisible.size()) {
                        column.setVisible(defaultColumnVisible.get(configIndex));
                    }
                } else {
                    // 如果列不在默认配置中，使用计算的宽度并设为可见
                    column.setPrefWidth(calculateColumnWidth(columnName));
                    column.setVisible(true);
                }
                
                // 添加监听器，自动保存为个人配置
                String visibleKey = AppConfig.PERSONAL_COLUMN_VISIBLE_KEY_PREFIX + currentStatusIndex + "_" + columnName;
                String widthKey = AppConfig.PERSONAL_COLUMN_WIDTH_KEY_PREFIX + currentStatusIndex + "_" + columnName;
                
                column.visibleProperty().addListener((obs, oldVal, newVal) -> {
                    prefs.putBoolean(visibleKey, newVal);
                    System.out.println("自动保存个人列可见性: " + columnName + " = " + newVal);
                });
                
                column.widthProperty().addListener((obs, oldVal, newVal) -> {
                    prefs.putDouble(widthKey, newVal.doubleValue());
                    System.out.println("自动保存个人列宽度: " + columnName + " = " + newVal.doubleValue());
                });
            }
        }
    }
    
    /**
     * 应用列顺序
     */
    private void applyColumnOrder(String savedOrder) {
        System.out.println("应用列顺序: " + savedOrder);
        
        // 解析保存的列顺序
        String[] columnOrder = savedOrder.split(",");
        
        // 创建一个映射，用于存储列名和对应的TableColumn对象
        Map<String, TableColumn<WarehouseEntry, ?>> columnMap = new HashMap<>();
        
        // 首先保存选择框列（索引0）
        TableColumn<WarehouseEntry, ?> selectColumn = null;
        if (!resultTableView.getColumns().isEmpty()) {
            selectColumn = resultTableView.getColumns().get(0); // 选择框列
        }
        
        // 将当前所有列（除了选择框列）放入映射
        for (TableColumn<WarehouseEntry, ?> column : resultTableView.getColumns()) {
            String columnName = column.getText();
            if (columnName != null && !columnName.isEmpty() && column != selectColumn) {
                columnMap.put(columnName, column);
            }
        }
        
        // 清除当前所有列
        resultTableView.getColumns().clear();
        
        // 首先添加选择框列
        if (selectColumn != null) {
            resultTableView.getColumns().add(selectColumn);
        }
        
        // 按保存的顺序添加列
        for (String columnName : columnOrder) {
            TableColumn<WarehouseEntry, ?> column = columnMap.get(columnName);
            if (column != null) {
                resultTableView.getColumns().add(column);
                columnMap.remove(columnName);
            }
        }
        
        // 添加任何未在保存顺序中但存在的列（可能是新添加的列）
        for (TableColumn<WarehouseEntry, ?> column : columnMap.values()) {
            resultTableView.getColumns().add(column);
        }
    }
    
    /**
     * 更新选中行的统计信息
     */
    private void updateSelectedRowsStatistics() {
        // 获取所有选中的行
        List<WarehouseEntry> selectedEntries = new ArrayList<>();
        
        // 只通过选择框选中的方式统计
        for (WarehouseEntry entry : entryList) {
            if (entry.isSelected()) {
                selectedEntries.add(entry);
            }
        }
        
        // 更新选中行数
        selectedRowsCountLabel.setText(String.valueOf(selectedEntries.size()));
        
        // 根据是否有选中行，显示或隐藏统计区域
        if (selectedEntries.isEmpty()) {
            if (selectedTotalsBox != null) {
                selectedTotalsBox.setVisible(false);
                selectedTotalsBox.setManaged(false);
            }
            return;
        } else {
            if (selectedTotalsBox != null) {
                selectedTotalsBox.setVisible(true);
                selectedTotalsBox.setManaged(true);
            }
        }
        
        // 计算选中行的统计数据
        int totalJs = selectedEntries.stream().mapToInt(WarehouseEntry::getJs).sum();
        double totalTj = selectedEntries.stream().mapToDouble(WarehouseEntry::getTj).sum();
        double totalKctj = selectedEntries.stream().mapToDouble(WarehouseEntry::getKctj).sum();
        double totalMz = selectedEntries.stream().mapToDouble(WarehouseEntry::getMz).sum();
        double totalKcjs = selectedEntries.stream().mapToDouble(WarehouseEntry::getKcjs).sum();
        
        // 更新统计标签
        selectedJsLabel.setText(String.format("%d", totalJs));
        selectedTjLabel.setText(String.format("%.2f", totalTj));
        selectedKctjLabel.setText(String.format("%.2f", totalKctj));
        selectedMzLabel.setText(String.format("%.2f", totalMz));
        selectedKcjsLabel.setText(String.format("%.2f", totalKcjs));
    }

    /**
     * 导出选中行数据到CSV或Excel文件
     */
    private void exportSelectedRows() {
        try {
            List<WarehouseEntry> selectedEntries = new ArrayList<>();
            
            // 获取通过选择框选中的行
            for (WarehouseEntry entry : entryList) {
                if (entry.isSelected()) {
                    selectedEntries.add(entry);
                }
            }
            
            if (selectedEntries.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "导出提示", "请先选择要导出的数据行");
                return;
            }
            
            // 设置文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("导出选中行数据");
            
            // 添加CSV格式选项
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV文件 (*.csv)", "*.csv")
            );
            fileChooser.setInitialFileName("仓库导出数据_" + 
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
            
            // 选择保存位置
            final File selectedFile = fileChooser.showSaveDialog(resultTableView.getScene().getWindow());
            if (selectedFile == null) {
                return; // 用户取消了保存
            }
            
            // 显示进度提示
            Platform.runLater(() -> {
                progressLabel.setText("正在导出数据...");
                progressLabel.setVisible(true);
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                progressBar.setVisible(true);
            });
            
            // 在后台线程中执行导出
            new Thread(() -> {
                try {
                    // 创建CSV内容
                    StringBuilder csvContent = new StringBuilder();
                    
                    // 添加UTF-8 BOM标记，使Excel能正确识别编码
                    csvContent.append("\uFEFF");
                    
                    // 添加表头
                    csvContent.append("进仓编号,进仓作业号,预进日期,进仓日期,L/F,包装规格,货物名称,唛头,货号,件数,体积,库存体积,毛重,托数,送货单位,库存件数,报关状态,备注\n");
                    
                    // 添加数据行
                    for (WarehouseEntry entry : selectedEntries) {
                        csvContent.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%.2f,%.2f,%.2f,%d,%s,%.2f,%s,%s\n",
                                csvEscape(entry.getJcbh()), csvEscape(entry.getJczyh()), 
                                csvEscape(entry.getYjrq()), csvEscape(entry.getJcrq()),
                                csvEscape(entry.getLf()), csvEscape(entry.getBzgg()), 
                                csvEscape(entry.getHwmc()), csvEscape(entry.getMt()),
                                csvEscape(entry.getHh()), entry.getJs(), entry.getTj(), 
                                entry.getKctj(), entry.getMz(), entry.getTs(), 
                                csvEscape(entry.getShdw()), entry.getKcjs(),
                                csvEscape(entry.getBgzt()), csvEscape(entry.getBz())));
                    }
                    
                    // 写入文件
                    Files.write(selectedFile.toPath(), csvContent.toString().getBytes(StandardCharsets.UTF_8));
                    
                    // 导出完成后更新UI
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        progressLabel.setVisible(false);
                        queryResultLabel.setText("已导出 " + selectedEntries.size() + " 条记录到: " + selectedFile.getName());
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    // 导出失败
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        progressLabel.setVisible(false);
                        showAlert(Alert.AlertType.ERROR, "导出失败", "导出数据时发生错误：" + e.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "导出失败", "启动导出过程失败：" + e.getMessage());
        }
    }
    
    // CSV字符串转义，处理包含逗号等特殊字符的情况
    private String csvEscape(String value) {
        if (value == null) return "";
        value = value.replace("\"", "\"\""); // 双引号替换为两个双引号
        
        // 如果字符串包含逗号、双引号或换行符，则用双引号括起来
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value + "\"";
        }
        return value;
    }
    
    /**
     * 显示列配置对话框
     */
    private void showColumnConfigDialog() {
        try {
            // 创建对话框
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("列显示设置");
            dialog.setHeaderText("状态: " + AppConfig.getStatusName(currentStatusIndex));
            
            // 创建内容面板
            VBox content = new VBox(10);
            content.setPrefWidth(400);
            content.setPrefHeight(500);
            
            // 添加说明标签
            Label instructionLabel = new Label("勾选要显示的列，取消勾选隐藏列：");
            instructionLabel.setStyle("-fx-font-weight: bold;");
            content.getChildren().add(instructionLabel);
            
            // 创建滚动面板
            ScrollPane scrollPane = new ScrollPane();
            VBox checkBoxContainer = new VBox(5);
            scrollPane.setContent(checkBoxContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            
            // 为每个列创建复选框（除了选择框列和操作列）
            List<CheckBox> columnCheckBoxes = new ArrayList<>();
            for (TableColumn<WarehouseEntry, ?> column : resultTableView.getColumns()) {
                String columnName = column.getText();
                if (columnName != null && !columnName.isEmpty() && 
                    !columnName.equals("操作") && !columnName.equals("")) {
                    
                    CheckBox checkBox = new CheckBox(columnName);
                    checkBox.setSelected(column.isVisible());
                    checkBox.setUserData(column); // 保存列引用
                    columnCheckBoxes.add(checkBox);
                    checkBoxContainer.getChildren().add(checkBox);
                }
            }
            
            content.getChildren().add(scrollPane);
            
            // 添加快捷操作按钮
            HBox buttonBox = new HBox(10);
            Button selectAllBtn = new Button("全选");
            Button selectNoneBtn = new Button("全不选");
            Button resetBtn = new Button("恢复默认");
            
            selectAllBtn.setOnAction(e -> {
                for (CheckBox cb : columnCheckBoxes) {
                    cb.setSelected(true);
                }
            });
            
            selectNoneBtn.setOnAction(e -> {
                for (CheckBox cb : columnCheckBoxes) {
                    cb.setSelected(false);
                }
            });
            
            resetBtn.setOnAction(e -> {
                for (CheckBox cb : columnCheckBoxes) {
                    cb.setSelected(true); // 默认全部显示
                }
            });
            
            buttonBox.getChildren().addAll(selectAllBtn, selectNoneBtn, resetBtn);
            content.getChildren().add(buttonBox);
            
            // 设置对话框内容
            dialog.getDialogPane().setContent(content);
            
            // 添加按钮
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // 显示对话框并处理结果
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 应用列可见性设置
                for (CheckBox checkBox : columnCheckBoxes) {
                    TableColumn<WarehouseEntry, ?> column = (TableColumn<WarehouseEntry, ?>) checkBox.getUserData();
                    boolean shouldBeVisible = checkBox.isSelected();
                    column.setVisible(shouldBeVisible);
                    
                    // 保存配置
                    String columnName = column.getText();
                    String key = AppConfig.COLUMN_VISIBLE_KEY_PREFIX + currentStatusIndex + "_" + columnName;
                    prefs.putBoolean(key, shouldBeVisible);
                }
                
                showAlert(Alert.AlertType.INFORMATION, "配置已保存", 
                         "列显示设置已保存并应用");
            }
            
        } catch (Exception e) {
            ExceptionHandler.handleException("显示列配置对话框", e);
        }
    }
    
    /**
     * 处理打开网页版按钮点击事件
     * 根据当前查询条件构建网页版URL并打开
     */
    @FXML
    private void handleOpenWebVersion() {
        try {
            // 获取当前查询条件
            String jcbh = jcbhTextField.getText().trim();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            int statusIndex = statusComboBox.getSelectionModel().getSelectedIndex();
            
            // 构建URL参数
            StringBuilder urlBuilder = new StringBuilder(AppConfig.WEBSITE_URL);
            boolean hasParams = false;
            
            // 添加进仓编号参数
            if (!jcbh.isEmpty()) {
                // 如果有多个进仓编号，只取第一个
                String firstJcbh = jcbh.split("[,\\s]+")[0];
                urlBuilder.append(hasParams ? "&" : "?").append("jcbh=").append(java.net.URLEncoder.encode(firstJcbh, "UTF-8"));
                hasParams = true;
            }
            
            // 添加状态参数
            if (statusIndex >= 0 && statusIndex < AppConfig.STATUS_CODES.length) {
                String statusCode = AppConfig.getStatusCode(statusIndex);
                urlBuilder.append(hasParams ? "&" : "?").append("zt=").append(statusCode);
                hasParams = true;
            }
            
            // 添加日期参数
            if (startDate != null) {
                String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                urlBuilder.append(hasParams ? "&" : "?").append("jcrq1=").append(startDateStr);
                hasParams = true;
            }
            
            if (endDate != null) {
                String endDateStr = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                urlBuilder.append(hasParams ? "&" : "?").append("jcrq2=").append(endDateStr);
                hasParams = true;
            }
            
            String finalUrl = urlBuilder.toString();
            System.out.println("打开网页版URL: " + finalUrl);
            
            // 打开网页
            openWebPage(finalUrl);
            
            // 显示成功提示
            queryResultLabel.setText("已在浏览器中打开网页版查询");
            
        } catch (Exception e) {
            ExceptionHandler.handleException("打开网页版", e);
            showAlert(Alert.AlertType.ERROR, "打开网页版失败", "无法打开网页版: " + e.getMessage());
        }
    }
    

}
