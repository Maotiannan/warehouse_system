/*
 * Decompiled with CFR 0.152.
 */
package com.warehousequery.app.controller;

import com.warehousequery.app.config.AppConfig;
import com.warehousequery.app.model.WarehouseEntry;
import com.warehousequery.app.service.LocalEntryCacheService;
import com.warehousequery.app.service.WarehouseService;
import com.warehousequery.app.util.ExceptionHandler;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.converter.DefaultStringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainController
implements Initializable {
    @FXML
    private TextField jcbhTextField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private FlowPane filterPane;
    @FXML
    private TableView<WarehouseEntry> resultTableView;
    @FXML
    private Label totalJsLabel;
    @FXML
    private Label totalTjLabel;
    @FXML
    private Label totalKctjLabel;
    @FXML
    private Label totalMzLabel;
    @FXML
    private Label totalKcjsLabel;
    @FXML
    private Button queryButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button webVersionButton;
    @FXML
    private Button resetFiltersButton;
    @FXML
    private ToggleButton advancedToggleButton;
    @FXML
    private Button viewLogsButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private MenuItem queryMenuItem;
    @FXML
    private MenuItem viewLogsMenuItem;
    @FXML
    private MenuItem exportMenuItem;
    @FXML
    private CheckMenuItem showHeadersMenuItem;
    @FXML
    private MenuItem calcSumMenuItem;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private MenuItem exportSelectedMenuItem;
    @FXML
    private MenuItem saveColumnConfigMenuItem;
    @FXML
    private MenuItem columnConfigMenuItem;
    @FXML
    private MenuItem resetColumnConfigMenuItem;
    @FXML
    private Label selectedRowsCountLabel;
    @FXML
    private Label selectedJsLabel;
    @FXML
    private Label selectedTjLabel;
    @FXML
    private Label selectedKctjLabel;
    @FXML
    private Label selectedMzLabel;
    @FXML
    private Label selectedKcjsLabel;
    @FXML
    private Button exportSelectedButton;
    @FXML
    private Label queryResultLabel;
    @FXML
    private VBox queryOverlay;
    @FXML
    private Label overlayMessageLabel;
    @FXML
    private VBox selectedTotalsBox;
    @FXML
    private TitledPane advancedFilterPane;
    @FXML
    private TextField ownerTextField;
    @FXML
    private TextField entryNumberTextField;
    @FXML
    private TextField jobNumberTextField;
    @FXML
    private TextField driverTextField;
    @FXML
    private TextField plateTextField;
    @FXML
    private TextField cargoTextField;
    @FXML
    private TextField markTextField;
    @FXML
    private TextField packageTextField;
    @FXML
    private TextField driverPhoneTextField;
    private final WarehouseService service = new WarehouseService();
    private final LocalEntryCacheService localEntryCacheService = LocalEntryCacheService.getInstance();
    private final ObservableList<WarehouseEntry> entryList = FXCollections.observableArrayList();
    private final Preferences prefs = Preferences.userNodeForPackage(MainController.class);
    private int currentStatusIndex = 1;
    private static final int COLUMN_CONFIG_VERSION = 1;
    private static final String LEGACY_SENTINEL = "__MISSING__";
    private static final String FILTER_OWNER = "owner";
    private static final String FILTER_ENTRY_NUMBER = "entryNumber";
    private static final String FILTER_JOB_NUMBER = "jobNumber";
    private static final String FILTER_DRIVER = "driver";
    private static final String FILTER_PLATE = "plate";
    private static final String FILTER_CARGO = "cargo";
    private static final String FILTER_MARK = "mark";
    private static final String FILTER_PACKAGE = "package";
    private static final String FILTER_DRIVER_PHONE = "driverPhone";
    private boolean suppressColumnConfigPersistence = false;
    private boolean columnOrderListenerAttached = false;
    private final List<WarehouseEntry> masterEntries = new ArrayList<WarehouseEntry>();
    private int lastRawEntryCount = 0;
    private boolean lastQueryWasError = false;
    private static boolean isPOIAvailable = false;
    private ContextMenu cellContextMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("\u6b63\u5728\u521d\u59cb\u5316\u4e3b\u63a7\u5236\u5668...");
        try {
            this.initializeMenuItems();
            this.initializeStatusComboBox();
            this.initializeDatePickers();
            this.initializeTableView();
            this.initializeProgressComponents();
            this.initializeEventHandlers();
            this.initializeToolbarControls();
            this.initializeLibraryChecks();
            this.initializeAdvancedFilterControls();
            this.installAdvancedFilterListeners();
            this.restoreLastQueryInputs();
            System.out.println("\u4e3b\u63a7\u5236\u5668\u521d\u59cb\u5316\u5b8c\u6210");
        }
        catch (Exception e) {
            ExceptionHandler.handleException("\u521d\u59cb\u5316\u4e3b\u63a7\u5236\u5668", e);
        }
    }

    private void initializeMenuItems() {
        if (this.queryMenuItem != null) {
            this.queryMenuItem.setOnAction(event -> this.handleQuery());
        }
        if (this.exportSelectedMenuItem != null) {
            this.exportSelectedMenuItem.setOnAction(event -> this.exportSelectedRows());
        }
        if (this.exportSelectedButton != null) {
            this.exportSelectedButton.setOnAction(event -> this.exportSelectedRows());
        }
        if (this.viewLogsMenuItem != null) {
            this.viewLogsMenuItem.setOnAction(event -> this.viewLogs());
        }
        if (this.exportMenuItem != null) {
            this.exportMenuItem.setOnAction(event -> this.handleExport());
        }
        if (this.calcSumMenuItem != null) {
            this.calcSumMenuItem.setOnAction(event -> this.showTotalsDialog());
        }
        if (this.exitMenuItem != null) {
            this.exitMenuItem.setOnAction(event -> this.handleExit());
        }
        if (this.columnConfigMenuItem != null) {
            this.columnConfigMenuItem.setOnAction(event -> this.showColumnConfigDialog());
        }
        if (this.saveColumnConfigMenuItem != null) {
            this.saveColumnConfigMenuItem.setOnAction(event -> this.saveColumnConfiguration());
        }
        if (this.resetColumnConfigMenuItem != null) {
            this.resetColumnConfigMenuItem.setOnAction(event -> this.resetColumnConfiguration());
        }
        if (this.resetFiltersButton != null) {
            this.resetFiltersButton.setOnAction(event -> this.resetAdvancedFilters());
        }
    }

    private void initializeStatusComboBox() {
        ObservableList<String> statusItems = FXCollections.observableArrayList(AppConfig.STATUS_NAMES);
        this.statusComboBox.setItems(statusItems);
        this.statusComboBox.getSelectionModel().select(1);
        this.currentStatusIndex = 1;
        this.statusComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            this.currentStatusIndex = newVal.intValue();
        });
    }

    private void initializeDatePickers() {
        this.applyDefaultDateRange();
        this.startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && this.endDatePicker.getValue() != null) {
                this.validateDateRange((LocalDate)newVal, (LocalDate)this.endDatePicker.getValue());
            }
        });
        this.endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && this.startDatePicker.getValue() != null) {
                this.validateDateRange((LocalDate)this.startDatePicker.getValue(), (LocalDate)newVal);
            }
        });
        this.startDatePicker.setTooltip(new Tooltip("\u5f00\u59cb\u65e5\u671f - \u6ce8\u610f\uff1a\u65e5\u671f\u8303\u56f4\u4e0d\u80fd\u8d85\u8fc7180\u5929"));
        this.endDatePicker.setTooltip(new Tooltip("\u7ed3\u675f\u65e5\u671f - \u6ce8\u610f\uff1a\u65e5\u671f\u8303\u56f4\u4e0d\u80fd\u8d85\u8fc7180\u5929"));
    }

    private void applyDefaultDateRange() {
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusDays(AppConfig.MAX_DATE_RANGE_DAYS - 1L);
        this.startDatePicker.setValue(startDate);
        this.endDatePicker.setValue(currentDate);
    }

    private void initializeAdvancedFilterControls() {
        if (this.advancedFilterPane != null) {
            boolean expanded = this.prefs.getBoolean("filter_advanced_expanded", false);
            this.advancedFilterPane.setExpanded(expanded);
            this.advancedFilterPane.expandedProperty().addListener((obs, oldVal, newVal) -> this.prefs.putBoolean("filter_advanced_expanded", (boolean)newVal));
        }
    }

    private void installAdvancedFilterListeners() {
        this.installFilterListener(this.ownerTextField);
        this.installFilterListener(this.entryNumberTextField);
        this.installFilterListener(this.jobNumberTextField);
        this.installFilterListener(this.driverTextField);
        this.installFilterListener(this.plateTextField);
        this.installFilterListener(this.cargoTextField);
        this.installFilterListener(this.markTextField);
        this.installFilterListener(this.packageTextField);
        this.installFilterListener(this.driverPhoneTextField);
    }

    private void installFilterListener(TextField field) {
        if (field == null) {
            return;
        }
        field.textProperty().addListener((obs, oldVal, newVal) -> this.applyFiltersToView());
    }

    private void initializeTableView() {
        if (this.selectedTotalsBox != null) {
            this.selectedTotalsBox.setVisible(false);
            this.selectedTotalsBox.setManaged(false);
        }
        this.resultTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.resultTableView.setItems(this.entryList);
        this.setupTableColumns();
        this.setupTableContextMenu();
        this.attachColumnOrderListener();
    }

    private void initializeProgressComponents() {
        this.progressBar.setProgress(0.0);
        this.progressBar.setVisible(false);
        this.progressLabel.setVisible(false);
    }

    private void initializeEventHandlers() {
        this.queryButton.setOnAction(event -> this.handleQuery());
        this.webVersionButton.setOnAction(event -> this.handleOpenWebVersion());
        this.jcbhTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.handleQuery();
            }
        });
    }

    private void initializeToolbarControls() {
        if (this.exportButton != null) {
            this.exportButton.setOnAction(event -> this.handleExport());
        }
        if (this.viewLogsButton != null) {
            this.viewLogsButton.setOnAction(event -> this.viewLogs());
        }
        if (this.advancedToggleButton != null && this.advancedFilterPane != null) {
            this.advancedToggleButton.setSelected(this.advancedFilterPane.isExpanded());
            this.advancedToggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (this.advancedFilterPane.isExpanded() != newVal.booleanValue()) {
                    this.advancedFilterPane.setExpanded((boolean)newVal);
                }
            });
            this.advancedFilterPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
                if (this.advancedToggleButton.isSelected() != newVal.booleanValue()) {
                    this.advancedToggleButton.setSelected((boolean)newVal);
                }
            });
        }
    }

    private void initializeLibraryChecks() {
        try {
            Class.forName("org.json.JSONObject");
            System.out.println("JSON\u5e93\u5df2\u52a0\u8f7d\u6210\u529f");
        }
        catch (ClassNotFoundException e) {
            System.err.println("\u8b66\u544a: JSON\u5e93\u672a\u627e\u5230 - " + e.getMessage());
            this.showAlert(Alert.AlertType.WARNING, "\u5e93\u7f3a\u5931\u8b66\u544a", "\u672a\u627e\u5230JSON\u89e3\u6790\u5e93\uff0c\u8bf7\u5b89\u88c5org.json\u5e93\u3002\n\u7cfb\u7edf\u5c06\u5c1d\u8bd5\u56de\u9000\u5230\u65e7\u7248HTML\u89e3\u6790\u65b9\u5f0f\uff0c\u4f46\u53ef\u80fd\u65e0\u6cd5\u6b63\u5e38\u5de5\u4f5c\u3002");
        }
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        long daysBetween;
        if (start != null && end != null && (daysBetween = ChronoUnit.DAYS.between(start, end)) > 180L) {
            LocalDate newStartDate = end.minusDays(180L);
            this.startDatePicker.setValue(newStartDate);
            Platform.runLater(() -> this.showAlert(Alert.AlertType.WARNING, "\u65e5\u671f\u8303\u56f4\u8fc7\u957f", "\u67e5\u8be2\u65e5\u671f\u8303\u56f4\u5df2\u81ea\u52a8\u8c03\u6574\u4e3a180\u5929\uff0c\n\u56e0\u4e3a\u670d\u52a1\u5668\u9650\u5236\u67e5\u8be2\u8303\u56f4\u4e0d\u80fd\u8d85\u8fc7180\u5929\u3002"));
        }
    }

    private void setupTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setOnShowing(event -> {
            contextMenu.getItems().clear();
            Menu columnsMenu = new Menu("\u663e\u793a/\u9690\u85cf\u5217");
            for (TableColumn<WarehouseEntry, ?> tableColumn : this.resultTableView.getColumns()) {
                String columnName = tableColumn.getText();
                if (columnName == null || columnName.isEmpty() || columnName.equals("\u64cd\u4f5c")) continue;
                CheckMenuItem item = new CheckMenuItem(columnName);
                item.setSelected(tableColumn.isVisible());
                item.selectedProperty().addListener((obs, oldVal, newVal) -> tableColumn.setVisible((boolean)newVal));
                columnsMenu.getItems().add(item);
            }
            MenuItem saveConfigItem = new MenuItem("\u4fdd\u5b58\u5f53\u524d\u5217\u914d\u7f6e");
            saveConfigItem.setOnAction(event2 -> this.saveColumnConfiguration());
            MenuItem menuItem = new MenuItem("\u6062\u590d\u9ed8\u8ba4\u5217\u914d\u7f6e");
            menuItem.setOnAction(event2 -> this.resetColumnConfiguration());
            contextMenu.getItems().addAll((MenuItem[])new MenuItem[]{columnsMenu, new SeparatorMenuItem(), saveConfigItem, menuItem});
        });
        this.resultTableView.setContextMenu(contextMenu);
    }

    private void resetColumnConfiguration() {
        try {
            this.clearLegacyColumnPreferences(this.currentStatusIndex);
            this.prefs.remove(this.buildColumnConfigJsonKey(this.currentStatusIndex));
            this.setupTableColumns();
            this.showAlert(Alert.AlertType.INFORMATION, "\u914d\u7f6e\u91cd\u7f6e", "\u72b6\u6001 \"" + AppConfig.getStatusName(this.currentStatusIndex) + "\" \u7684\u5217\u914d\u7f6e\u5df2\u6062\u590d\u4e3a\u9ed8\u8ba4\u503c\u3002");
        }
        catch (Exception e) {
            ExceptionHandler.handleException("\u91cd\u7f6e\u5217\u914d\u7f6e", e);
        }
    }

    private void checkDateRangeValid(LocalDate start, LocalDate end) {
        long daysBetween;
        if (start != null && end != null && (daysBetween = ChronoUnit.DAYS.between(start, end)) > 180L) {
            LocalDate newStartDate = end.minusDays(180L);
            this.startDatePicker.setValue(newStartDate);
            Platform.runLater(() -> this.showAlert(Alert.AlertType.WARNING, "\u65e5\u671f\u8303\u56f4\u8fc7\u957f", "\u67e5\u8be2\u65e5\u671f\u8303\u56f4\u5df2\u81ea\u52a8\u8c03\u6574\u4e3a180\u5929\uff0c\n\u56e0\u4e3a\u670d\u52a1\u5668\u9650\u5236\u67e5\u8be2\u8303\u56f4\u4e0d\u80fd\u8d85\u8fc7180\u5929\u3002"));
        }
    }

    private void setupTableColumns() {
        this.resultTableView.getColumns().clear();
        TableColumn<WarehouseEntry, Boolean> selectCol = new TableColumn<>("");
        selectCol.setCellValueFactory(param -> {
            WarehouseEntry entry = param.getValue();
            SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(entry.isSelected());
            booleanProp.addListener((ChangeListener<? super Boolean>)((ChangeListener<Boolean>)(obs, oldVal, newVal) -> {
                entry.setSelected((boolean)newVal);
                this.updateSelectedRowsStatistics();
            }));
            return booleanProp;
        });
        CheckBox selectAllCheckBox = new CheckBox();
        selectAllCheckBox.setOnAction(event -> {
            boolean select = selectAllCheckBox.isSelected();
            for (WarehouseEntry entry : this.entryList) {
                entry.setSelected(select);
            }
            this.resultTableView.refresh();
            this.updateSelectedRowsStatistics();
        });
        selectCol.setGraphic(selectAllCheckBox);
        selectCol.setCellFactory(param -> {
            CheckBoxTableCell<WarehouseEntry, Boolean> cell = new CheckBoxTableCell<>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        selectCol.setEditable(true);
        selectCol.setPrefWidth(40.0);
        selectCol.setResizable(false);
        this.resultTableView.getColumns().add(selectCol);
        List<String> extractedHeaders = WarehouseService.getExtractedTableHeaders();
        if (!extractedHeaders.isEmpty()) {
            System.out.println("\u4f7f\u7528\u4ece\u7f51\u7ad9\u63d0\u53d6\u7684\u8868\u5934\u540d\u79f0: " + String.join((CharSequence)", ", extractedHeaders));
            this.setupColumnsForStatus(this.currentStatusIndex, extractedHeaders);
        } else {
            System.out.println("\u4f7f\u7528\u9884\u5b9a\u4e49\u8868\u5934");
            this.setupPredefinedColumns();
        }
        this.resultTableView.setEditable(true);
    }

    private void setupColumnsForStatus(int statusIndex, List<String> headers) {
        this.suppressColumnConfigPersistence = true;
        this.resultTableView.getColumns().clear();
        this.addSelectionColumn();
        List<TableColumn<WarehouseEntry, ?>> columnsToAdd = new ArrayList<>();
        switch (statusIndex) {
            case 0: {
                this.setupPreBookingColumns(headers, columnsToAdd);
                break;
            }
            case 1: {
                this.setupInboundColumns(headers, columnsToAdd);
                break;
            }
            case 2: {
                this.setupInventoryColumns(headers, columnsToAdd);
                break;
            }
            case 3: {
                this.setupOutboundColumns(headers, columnsToAdd);
                break;
            }
            default: {
                this.setupInboundColumns(headers, columnsToAdd);
            }
        }
        this.resultTableView.getColumns().addAll(columnsToAdd);
        this.addOperationColumn();
        this.loadColumnConfiguration();
    }

    private void addSelectionColumn() {
        TableColumn<WarehouseEntry, Boolean> selectColumn = new TableColumn<>("");
        selectColumn.setCellValueFactory(param -> {
            WarehouseEntry entry = param.getValue();
            BooleanProperty selected = entry.selectedProperty();
            selected.addListener((obs, oldVal, newVal) -> this.updateSelectedRowsStatistics());
            return selected;
        });
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);
        selectColumn.setPrefWidth(30.0);
        selectColumn.setResizable(false);
        selectColumn.setSortable(false);
        CheckBox selectAllCheckBox = new CheckBox();
        selectAllCheckBox.setOnAction(event -> {
            boolean select = selectAllCheckBox.isSelected();
            for (WarehouseEntry entry : this.entryList) {
                entry.setSelected(select);
            }
            this.resultTableView.refresh();
            this.updateSelectedRowsStatistics();
        });
        selectColumn.setGraphic(selectAllCheckBox);
        this.resultTableView.getColumns().add(selectColumn);
    }

    private void setupPreBookingColumns(List<String> headers, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        if (headers.isEmpty()) {
            this.setupDefaultPreBookingColumns(columnsToAdd);
            return;
        }
        Map<String, String> preBookingFieldMapping = this.createPreBookingFieldMapping();
        Map<String, String> displayNameMapping = this.createPreBookingDisplayNameMapping();
        for (String serverHeader : headers) {
            double width;
            String displayName;
            String fieldName = preBookingFieldMapping.get(serverHeader);
            if (fieldName != null) {
                displayName = displayNameMapping.getOrDefault(serverHeader, serverHeader);
                width = this.getColumnWidthFromConfig(0, displayName, this.calculateColumnWidth(displayName));
                this.addDynamicColumn(displayName, fieldName, width, columnsToAdd);
                continue;
            }
            displayName = displayNameMapping.getOrDefault(serverHeader, serverHeader);
            width = this.getColumnWidthFromConfig(0, displayName, this.calculateColumnWidth(displayName));
            this.addDynamicColumn(displayName, "bz", width, columnsToAdd);
        }
    }

    private Map<String, String> createPreBookingFieldMapping() {
        HashMap<String, String> mapping = new HashMap<String, String>();
        mapping.put("jcbh", "jcbh");
        mapping.put("zyh", "jczyh");
        mapping.put("yyh", "yyh");
        mapping.put("yjrq", "yjrq");
        mapping.put("jcrq", "jcrq");
        mapping.put("srrq", "srrq");
        mapping.put("shrq", "jcrq");
        mapping.put("lf", "lf");
        mapping.put("bzgg", "bzgg");
        mapping.put("hwmc", "hwmc");
        mapping.put("mt", "mt");
        mapping.put("hh", "hh");
        mapping.put("js", "js");
        mapping.put("tj", "tj");
        mapping.put("mz", "mz");
        mapping.put("shdw", "shdw");
        mapping.put("hz", "shdw");
        mapping.put("fgsmc", "fgsmc");
        mapping.put("bz2", "bz");
        mapping.put("yyjcbz", "bz");
        mapping.put("hdmc", "hd");
        mapping.put("hd", "hd");
        mapping.put("ch", "ch");
        mapping.put("jcid", "jcid");
        mapping.put("yyjcid", "jcid");
        mapping.put("shzt", "bgzt");
        mapping.put("jsy", "jsy");
        mapping.put("driverdh", "jsydh");
        mapping.put("jsydh", "jsydh");
        mapping.put("yqqdsj", "yqqdsj");
        return mapping;
    }

    private Map<String, String> createPreBookingDisplayNameMapping() {
        HashMap<String, String> mapping = new HashMap<String, String>();
        mapping.put("jcbh", "\u8fdb\u4ed3\u7f16\u53f7");
        mapping.put("zyh", "\u4f5c\u4e1a\u53f7");
        mapping.put("yyh", "\u9884\u7ea6\u53f7");
        mapping.put("yjrq", "\u9884\u8fdb\u65e5\u671f");
        mapping.put("srrq", "\u5f55\u5165\u65e5\u671f");
        mapping.put("shrq", "\u5ba1\u6838\u65e5\u671f");
        mapping.put("shzt", "\u5ba1\u6838\u72b6\u6001");
        mapping.put("yqqdsj", "\u8981\u6c42\u53d6\u5355\u65f6\u95f4");
        mapping.put("yyjcid", "\u9884\u7ea6\u8fdb\u4ed3ID");
        mapping.put("jcid", "\u8fdb\u4ed3ID");
        mapping.put("lf", "L/F");
        mapping.put("bzgg", "\u5305\u88c5\u89c4\u683c");
        mapping.put("hwmc", "\u8d27\u7269\u540d\u79f0");
        mapping.put("hwmc1", "\u8d27\u7269\u540d\u79f0");
        mapping.put("mt", "\u551b\u5934");
        mapping.put("hh", "\u8d27\u53f7");
        mapping.put("js", "\u4ef6\u6570");
        mapping.put("tj", "\u4f53\u79ef");
        mapping.put("mz", "\u6bdb\u91cd");
        mapping.put("ts", "\u6258\u6570");
        mapping.put("shdw", "\u9001\u8d27\u5355\u4f4d");
        mapping.put("hz", "\u8d27\u4e3b");
        mapping.put("fgsmc", "\u5206\u516c\u53f8\u540d\u79f0");
        mapping.put("hdmc", "\u8d27\u4ee3");
        mapping.put("hd", "\u8d27\u4ee3");
        mapping.put("ch", "\u8f66\u724c\u53f7");
        mapping.put("jsy", "\u53f8\u673a\u59d3\u540d");
        mapping.put("driverdh", "\u53f8\u673a\u7535\u8bdd");
        mapping.put("jsydh", "\u53f8\u673a\u7535\u8bdd");
        mapping.put("yyjcbz", "\u9884\u7ea6\u5907\u6ce8");
        mapping.put("bz", "\u5907\u6ce8");
        mapping.put("bz2", "\u5907\u6ce82");
        return mapping;
    }

    private void addDynamicColumn(String displayName, String fieldName, double width, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        TableColumn<WarehouseEntry, Object> column = new TableColumn<>(displayName);
        column.setCellValueFactory(new PropertyValueFactory(fieldName));
        column.setPrefWidth(width);
        this.configureColumnBehavior(column, fieldName);
        columnsToAdd.add(column);
        Platform.runLater(() -> column.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > 0.0) {
                this.saveColumnWidthConfig(this.currentStatusIndex, displayName, newVal.doubleValue());
                System.out.println("\u4fdd\u5b58\u5217\u5bbd\u5ea6: " + displayName + " = " + newVal.doubleValue());
            }
        }));
    }

    private void setupDefaultPreBookingColumns(List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        this.addDynamicColumn("\u8fdb\u4ed3\u7f16\u53f7", "jcbh", this.getColumnWidthFromConfig(0, "\u8fdb\u4ed3\u7f16\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u7ea6\u53f7", "yyh", this.getColumnWidthFromConfig(0, "\u9884\u7ea6\u53f7", 120.0), columnsToAdd);
        this.addDynamicColumn("\u4f5c\u4e1a\u53f7", "jczyh", this.getColumnWidthFromConfig(0, "\u4f5c\u4e1a\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u5f55\u5165\u65e5\u671f", "srrq", this.getColumnWidthFromConfig(0, "\u5f55\u5165\u65e5\u671f", 140.0), columnsToAdd);
        this.addDynamicColumn("\u5ba1\u6838\u65e5\u671f", "jcrq", this.getColumnWidthFromConfig(0, "\u5ba1\u6838\u65e5\u671f", 140.0), columnsToAdd);
        this.addDynamicColumn("\u5ba1\u6838\u72b6\u6001", "bgzt", this.getColumnWidthFromConfig(0, "\u5ba1\u6838\u72b6\u6001", 80.0), columnsToAdd);
        this.addDynamicColumn("\u8981\u6c42\u53d6\u5355\u65f6\u95f4", "yqqdsj", this.getColumnWidthFromConfig(0, "\u8981\u6c42\u53d6\u5355\u65f6\u95f4", 180.0), columnsToAdd);
        this.addDynamicColumn("\u5305\u88c5\u89c4\u683c", "bzgg", this.getColumnWidthFromConfig(0, "\u5305\u88c5\u89c4\u683c", 80.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u7269\u540d\u79f0", "hwmc", this.getColumnWidthFromConfig(0, "\u8d27\u7269\u540d\u79f0", 100.0), columnsToAdd);
        this.addDynamicColumn("\u551b\u5934", "mt", this.getColumnWidthFromConfig(0, "\u551b\u5934", 100.0), columnsToAdd);
        this.addDynamicColumn("\u4ef6\u6570", "js", this.getColumnWidthFromConfig(0, "\u4ef6\u6570", 60.0), columnsToAdd);
        this.addDynamicColumn("\u4f53\u79ef", "tj", this.getColumnWidthFromConfig(0, "\u4f53\u79ef", 60.0), columnsToAdd);
        this.addDynamicColumn("\u6bdb\u91cd", "mz", this.getColumnWidthFromConfig(0, "\u6bdb\u91cd", 60.0), columnsToAdd);
        this.addDynamicColumn("\u9001\u8d27\u5355\u4f4d", "shdw", this.getColumnWidthFromConfig(0, "\u9001\u8d27\u5355\u4f4d", 150.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u4ee3", "hd", this.getColumnWidthFromConfig(0, "\u8d27\u4ee3", 120.0), columnsToAdd);
        this.addDynamicColumn("\u8f66\u724c\u53f7", "ch", this.getColumnWidthFromConfig(0, "\u8f66\u724c\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u53f8\u673a\u59d3\u540d", "jsy", this.getColumnWidthFromConfig(0, "\u53f8\u673a\u59d3\u540d", 100.0), columnsToAdd);
        this.addDynamicColumn("\u53f8\u673a\u7535\u8bdd", "jsydh", this.getColumnWidthFromConfig(0, "\u53f8\u673a\u7535\u8bdd", 120.0), columnsToAdd);
        this.addDynamicColumn("\u5206\u516c\u53f8\u540d\u79f0", "fgsmc", this.getColumnWidthFromConfig(0, "\u5206\u516c\u53f8\u540d\u79f0", 150.0), columnsToAdd);
        this.addDynamicColumn("\u5907\u6ce8", "bz", this.getColumnWidthFromConfig(0, "\u5907\u6ce8", 200.0), columnsToAdd);
    }

    private double getColumnWidthFromConfig(int statusIndex, String columnName, double defaultWidth) {
        String personalKey = "personal_column_width_" + statusIndex + "_" + columnName;
        double personalWidth = this.prefs.getDouble(personalKey, -1.0);
        if (personalWidth > 0.0) {
            return personalWidth;
        }
        AppConfig.DefaultColumnConfig defaultConfig = AppConfig.getDefaultColumnConfig(statusIndex);
        List<String> defaultColumnNames = defaultConfig.getColumnNames();
        List<Double> defaultColumnWidths = defaultConfig.getColumnWidths();
        int configIndex = defaultColumnNames.indexOf(columnName);
        if (configIndex >= 0 && configIndex < defaultColumnWidths.size()) {
            return defaultColumnWidths.get(configIndex);
        }
        return defaultWidth;
    }

    private void saveColumnWidthConfig(int statusIndex, String columnName, double width) {
        String personalKey = "personal_column_width_" + statusIndex + "_" + columnName;
        this.prefs.putDouble(personalKey, width);
    }

    private void setupInboundColumns(List<String> headers, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        this.addDynamicColumn("\u8fdb\u4ed3ID", "jcid", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8fdb\u4ed3ID", 80.0), columnsToAdd);
        this.addDynamicColumn("\u4f5c\u4e1a\u53f7", "zyh", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u4f5c\u4e1a\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u8fdb\u4ed3\u65e5\u671f", "jcrq", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8fdb\u4ed3\u65e5\u671f", 140.0), columnsToAdd);
        this.addDynamicColumn("\u8fdb\u4ed3\u7f16\u53f7", "jcbh", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8fdb\u4ed3\u7f16\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u4e3b", "hz", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8d27\u4e3b", 150.0), columnsToAdd);
        this.addDynamicColumn("\u551b\u5934", "mt", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u551b\u5934", 120.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u8ba1\u4ef6\u6570", "yjjs", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u9884\u8ba1\u4ef6\u6570", 80.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u8ba1\u4f53\u79ef", "yjtj", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u9884\u8ba1\u4f53\u79ef", 80.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u8ba1\u6bdb\u91cd", "yjmz", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u9884\u8ba1\u6bdb\u91cd", 80.0), columnsToAdd);
        this.addDynamicColumn("\u4ef6\u6570", "js", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u4ef6\u6570", 60.0), columnsToAdd);
        this.addDynamicColumn("\u4f53\u79ef", "tj", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u4f53\u79ef", 60.0), columnsToAdd);
        this.addDynamicColumn("\u6bdb\u91cd", "mz", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u6bdb\u91cd", 60.0), columnsToAdd);
        this.addDynamicColumn("\u5e93\u5b58\u4ef6\u6570", "kcjs", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5e93\u5b58\u4ef6\u6570", 80.0), columnsToAdd);
        this.addDynamicColumn("\u5e93\u5b58\u4f53\u79ef", "kctj", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5e93\u5b58\u4f53\u79ef", 80.0), columnsToAdd);
        this.addDynamicColumn("\u5e93\u5b58\u6bdb\u91cd", "kcmz", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5e93\u5b58\u6bdb\u91cd", 80.0), columnsToAdd);
        this.addDynamicColumn("\u5305\u88c5\u89c4\u683c", "bzgg", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5305\u88c5\u89c4\u683c", 80.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u7269\u540d\u79f0", "hwmc", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8d27\u7269\u540d\u79f0", 120.0), columnsToAdd);
        this.addDynamicColumn("\u5378\u8d27\u65e5\u671f", "xhrq", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5378\u8d27\u65e5\u671f", 140.0), columnsToAdd);
        this.addDynamicColumn("\u5378\u8d27\u5b8c\u6210", "xhrq2", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5378\u8d27\u5b8c\u6210", 140.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u8fdb\u65e5\u671f", "yjrq", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u9884\u8fdb\u65e5\u671f", 140.0), columnsToAdd);
        this.addDynamicColumn("L/F", "lf", this.getColumnWidthFromConfig(this.currentStatusIndex, "L/F", 50.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u7269\u540d\u79f01", "hwmc1", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8d27\u7269\u540d\u79f01", 120.0), columnsToAdd);
        this.addDynamicColumn("\u8f66\u53f7", "ch", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8f66\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u53f8\u673a\u7535\u8bdd", "driverdh", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u53f8\u673a\u7535\u8bdd", 120.0), columnsToAdd);
        this.addDynamicColumn("\u8f66\u957f", "cleng", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8f66\u957f", 60.0), columnsToAdd);
        this.addDynamicColumn("\u627f\u91cd", "chengzhong", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u627f\u91cd", 60.0), columnsToAdd);
        this.addDynamicColumn("\u8fd0\u5355\u53f7", "yyh", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8fd0\u5355\u53f7", 120.0), columnsToAdd);
        this.addDynamicColumn("\u5907\u6ce8", "bz2", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5907\u6ce8", 200.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u4ee3\u540d\u79f0", "hdmc", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8d27\u4ee3\u540d\u79f0", 120.0), columnsToAdd);
        this.addDynamicColumn("\u5206\u516c\u53f8", "fgsmc", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5206\u516c\u53f8", 120.0), columnsToAdd);
    }

    private void setupInventoryColumns(List<String> headers, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        this.addDynamicColumn("\u8fdb\u4ed3ID", "jcid", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8fdb\u4ed3ID", 80.0), columnsToAdd);
        this.addDynamicColumn("\u4f5c\u4e1a\u53f7", "zyh", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u4f5c\u4e1a\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u8fdb\u4ed3\u65e5\u671f", "jcrq", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8fdb\u4ed3\u65e5\u671f", 140.0), columnsToAdd);
        this.addDynamicColumn("\u8fdb\u4ed3\u7f16\u53f7", "jcbh", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8fdb\u4ed3\u7f16\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u4e3b", "hz", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8d27\u4e3b", 150.0), columnsToAdd);
        this.addDynamicColumn("\u551b\u5934", "mt", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u551b\u5934", 120.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u8ba1\u4ef6\u6570", "yjjs", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u9884\u8ba1\u4ef6\u6570", 80.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u8ba1\u4f53\u79ef", "yjtj", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u9884\u8ba1\u4f53\u79ef", 80.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u8ba1\u6bdb\u91cd", "yjmz", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u9884\u8ba1\u6bdb\u91cd", 80.0), columnsToAdd);
        this.addDynamicColumn("\u4ef6\u6570", "js", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u4ef6\u6570", 60.0), columnsToAdd);
        this.addDynamicColumn("\u4f53\u79ef", "tj", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u4f53\u79ef", 60.0), columnsToAdd);
        this.addDynamicColumn("\u6bdb\u91cd", "mz", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u6bdb\u91cd", 60.0), columnsToAdd);
        this.addDynamicColumn("\u5e93\u5b58\u4ef6\u6570", "kcjs", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5e93\u5b58\u4ef6\u6570", 80.0), columnsToAdd);
        this.addDynamicColumn("\u5e93\u5b58\u4f53\u79ef", "kctj", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5e93\u5b58\u4f53\u79ef", 80.0), columnsToAdd);
        this.addDynamicColumn("\u5e93\u5b58\u6bdb\u91cd", "kcmz", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5e93\u5b58\u6bdb\u91cd", 80.0), columnsToAdd);
        this.addDynamicColumn("\u5305\u88c5\u89c4\u683c", "bzgg", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5305\u88c5\u89c4\u683c", 80.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u7269\u540d\u79f0", "hwmc", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8d27\u7269\u540d\u79f0", 120.0), columnsToAdd);
        this.addDynamicColumn("\u5378\u8d27\u65e5\u671f", "xhrq", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5378\u8d27\u65e5\u671f", 140.0), columnsToAdd);
        this.addDynamicColumn("\u5378\u8d27\u5b8c\u6210", "xhrq2", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5378\u8d27\u5b8c\u6210", 140.0), columnsToAdd);
        this.addDynamicColumn("\u9884\u8fdb\u65e5\u671f", "yjrq", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u9884\u8fdb\u65e5\u671f", 140.0), columnsToAdd);
        this.addDynamicColumn("L/F", "lf", this.getColumnWidthFromConfig(this.currentStatusIndex, "L/F", 50.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u7269\u540d\u79f01", "hwmc1", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8d27\u7269\u540d\u79f01", 120.0), columnsToAdd);
        this.addDynamicColumn("\u8f66\u53f7", "ch", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8f66\u53f7", 100.0), columnsToAdd);
        this.addDynamicColumn("\u53f8\u673a\u7535\u8bdd", "driverdh", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u53f8\u673a\u7535\u8bdd", 120.0), columnsToAdd);
        this.addDynamicColumn("\u8f66\u957f", "cleng", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8f66\u957f", 60.0), columnsToAdd);
        this.addDynamicColumn("\u627f\u91cd", "chengzhong", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u627f\u91cd", 60.0), columnsToAdd);
        this.addDynamicColumn("\u8fd0\u5355\u53f7", "yyh", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8fd0\u5355\u53f7", 120.0), columnsToAdd);
        this.addDynamicColumn("\u5907\u6ce8", "bz2", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5907\u6ce8", 200.0), columnsToAdd);
        this.addDynamicColumn("\u8d27\u4ee3\u540d\u79f0", "hdmc", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u8d27\u4ee3\u540d\u79f0", 120.0), columnsToAdd);
        this.addDynamicColumn("\u5206\u516c\u53f8", "fgsmc", this.getColumnWidthFromConfig(this.currentStatusIndex, "\u5206\u516c\u53f8", 120.0), columnsToAdd);
    }

    private void setupOutboundColumns(List<String> headers, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        Map<String, String> headerMapping = this.createOutboundHeaderMapping();
        this.addColumnWithMapping("\u8fdb\u4ed3\u7f16\u53f7", "jcbh", 100.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u8fdb\u4ed3\u4f5c\u4e1a\u53f7", "jczyh", 100.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u9884\u8fdb\u65e5\u671f", "yjrq", 140.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u8fdb\u4ed3\u65e5\u671f", "jcrq", 140.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u51fa\u4ed3\u65e5\u671f", "ccrq", 140.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("L/F", "lf", 50.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u5305\u88c5\u89c4\u683c", "bzgg", 120.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u8d27\u7269\u540d\u79f0", "hwmc", 150.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u551b\u5934", "mt", 120.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u8d27\u53f7", "hh", 80.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u4ef6\u6570", "js", 60.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u4f53\u79ef", "tj", 60.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u6bdb\u91cd", "mz", 60.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u6258\u6570", "ts", 60.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u9001\u8d27\u5355\u4f4d", "shdw", 150.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u63d0\u8d27\u5355\u4f4d", "thdw", 150.0, headers, headerMapping, columnsToAdd);
        this.addColumnWithMapping("\u5907\u6ce8", "bz", 200.0, headers, headerMapping, columnsToAdd);
    }

    private <T> void addColumnWithMapping(String defaultName, String propertyName, double width, List<String> headers, Map<String, String> headerMapping, List<TableColumn<WarehouseEntry, ?>> columnsToAdd) {
        String displayName = this.findHeaderName(defaultName, headers, headerMapping);
        double savedWidth = this.getColumnWidthFromConfig(this.currentStatusIndex, displayName, width);
        TableColumn<WarehouseEntry, Object> column = new TableColumn<>(displayName);
        column.setCellValueFactory(new PropertyValueFactory(propertyName));
        column.setPrefWidth(savedWidth);
        this.configureColumnBehavior(column, propertyName);
        columnsToAdd.add(column);
        Platform.runLater(() -> column.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > 0.0) {
                this.saveColumnWidthConfig(this.currentStatusIndex, displayName, newVal.doubleValue());
                System.out.println("\u4fdd\u5b58\u5217\u5bbd\u5ea6: " + displayName + " = " + newVal.doubleValue());
            }
        }));
    }

    private String findHeaderName(String defaultName, List<String> headers, Map<String, String> headerMapping) {
        if (headers.contains(defaultName)) {
            return defaultName;
        }
        for (String header : headers) {
            String mappedColumn = headerMapping.get(header);
            if (mappedColumn == null || !mappedColumn.equals(defaultName)) continue;
            return header;
        }
        return defaultName;
    }

    private Map<String, String> createPreBookingHeaderMapping() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("\u8fdb\u4ed3\u7f16\u53f7", "\u8fdb\u4ed3\u7f16\u53f7");
        map.put("\u4f5c\u4e1a\u53f7", "\u8fdb\u4ed3\u4f5c\u4e1a\u53f7");
        map.put("\u9884\u8fdb\u65e5\u671f", "\u9884\u8fdb\u65e5\u671f");
        map.put("\u8fdb\u4ed3\u65e5\u671f", "\u8fdb\u4ed3\u65e5\u671f");
        map.put("L/F", "L/F");
        map.put("\u5305\u88c5\u89c4\u683c", "\u5305\u88c5\u89c4\u683c");
        map.put("\u8d27\u7269\u540d\u79f0", "\u8d27\u7269\u540d\u79f0");
        map.put("\u551b\u5934", "\u551b\u5934");
        map.put("\u8d27\u53f7", "\u8d27\u53f7");
        map.put("\u4ef6\u6570", "\u4ef6\u6570");
        map.put("\u4f53\u79ef", "\u4f53\u79ef");
        map.put("\u6bdb\u91cd", "\u6bdb\u91cd");
        map.put("\u9001\u8d27\u5355\u4f4d", "\u9001\u8d27\u5355\u4f4d");
        map.put("\u5907\u6ce8", "\u5907\u6ce8");
        return map;
    }

    private Map<String, String> createInboundHeaderMapping() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("\u8fdb\u4ed3\u7f16\u53f7", "\u8fdb\u4ed3\u7f16\u53f7");
        map.put("\u8fdb\u4ed3\u4f5c\u4e1a\u53f7", "\u8fdb\u4ed3\u4f5c\u4e1a\u53f7");
        map.put("\u4f5c\u4e1a\u53f7", "\u8fdb\u4ed3\u4f5c\u4e1a\u53f7");
        map.put("\u9884\u8fdb\u65e5\u671f", "\u9884\u8fdb\u65e5\u671f");
        map.put("\u8fdb\u4ed3\u65e5\u671f", "\u8fdb\u4ed3\u65e5\u671f");
        map.put("L/F", "L/F");
        map.put("\u5305\u88c5\u89c4\u683c", "\u5305\u88c5\u89c4\u683c");
        map.put("\u8d27\u7269\u540d\u79f0", "\u8d27\u7269\u540d\u79f0");
        map.put("\u551b\u5934", "\u551b\u5934");
        map.put("\u8d27\u53f7", "\u8d27\u53f7");
        map.put("\u4ef6\u6570", "\u4ef6\u6570");
        map.put("\u4f53\u79ef", "\u4f53\u79ef");
        map.put("\u5e93\u5b58\u4f53\u79ef", "\u5e93\u5b58\u4f53\u79ef");
        map.put("\u6bdb\u91cd", "\u6bdb\u91cd");
        map.put("\u6258\u6570", "\u6258\u6570");
        map.put("\u9001\u8d27\u5355\u4f4d", "\u9001\u8d27\u5355\u4f4d");
        map.put("\u5e93\u5b58\u4ef6\u6570", "\u5e93\u5b58\u4ef6\u6570");
        map.put("\u62a5\u5173\u72b6\u6001", "\u62a5\u5173\u72b6\u6001");
        map.put("\u5907\u6ce8", "\u5907\u6ce8");
        return map;
    }

    private Map<String, String> createInventoryHeaderMapping() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("\u8fdb\u4ed3\u7f16\u53f7", "\u8fdb\u4ed3\u7f16\u53f7");
        map.put("\u8fdb\u4ed3\u4f5c\u4e1a\u53f7", "\u8fdb\u4ed3\u4f5c\u4e1a\u53f7");
        map.put("\u4f5c\u4e1a\u53f7", "\u8fdb\u4ed3\u4f5c\u4e1a\u53f7");
        map.put("\u9884\u8fdb\u65e5\u671f", "\u9884\u8fdb\u65e5\u671f");
        map.put("\u8fdb\u4ed3\u65e5\u671f", "\u8fdb\u4ed3\u65e5\u671f");
        map.put("L/F", "L/F");
        map.put("\u5305\u88c5\u89c4\u683c", "\u5305\u88c5\u89c4\u683c");
        map.put("\u8d27\u7269\u540d\u79f0", "\u8d27\u7269\u540d\u79f0");
        map.put("\u551b\u5934", "\u551b\u5934");
        map.put("\u8d27\u53f7", "\u8d27\u53f7");
        map.put("\u4ef6\u6570", "\u4ef6\u6570");
        map.put("\u4f53\u79ef", "\u4f53\u79ef");
        map.put("\u5e93\u5b58\u4f53\u79ef", "\u5e93\u5b58\u4f53\u79ef");
        map.put("\u6bdb\u91cd", "\u6bdb\u91cd");
        map.put("\u6258\u6570", "\u6258\u6570");
        map.put("\u9001\u8d27\u5355\u4f4d", "\u9001\u8d27\u5355\u4f4d");
        map.put("\u5e93\u5b58\u4ef6\u6570", "\u5e93\u5b58\u4ef6\u6570");
        map.put("\u62a5\u5173\u72b6\u6001", "\u62a5\u5173\u72b6\u6001");
        map.put("\u5907\u6ce8", "\u5907\u6ce8");
        return map;
    }

    private Map<String, String> createOutboundHeaderMapping() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("\u8fdb\u4ed3\u7f16\u53f7", "\u8fdb\u4ed3\u7f16\u53f7");
        map.put("\u8fdb\u4ed3\u4f5c\u4e1a\u53f7", "\u8fdb\u4ed3\u4f5c\u4e1a\u53f7");
        map.put("\u4f5c\u4e1a\u53f7", "\u8fdb\u4ed3\u4f5c\u4e1a\u53f7");
        map.put("\u9884\u8fdb\u65e5\u671f", "\u9884\u8fdb\u65e5\u671f");
        map.put("\u8fdb\u4ed3\u65e5\u671f", "\u8fdb\u4ed3\u65e5\u671f");
        map.put("\u51fa\u4ed3\u65e5\u671f", "\u51fa\u4ed3\u65e5\u671f");
        map.put("L/F", "L/F");
        map.put("\u5305\u88c5\u89c4\u683c", "\u5305\u88c5\u89c4\u683c");
        map.put("\u8d27\u7269\u540d\u79f0", "\u8d27\u7269\u540d\u79f0");
        map.put("\u551b\u5934", "\u551b\u5934");
        map.put("\u8d27\u53f7", "\u8d27\u53f7");
        map.put("\u4ef6\u6570", "\u4ef6\u6570");
        map.put("\u4f53\u79ef", "\u4f53\u79ef");
        map.put("\u6bdb\u91cd", "\u6bdb\u91cd");
        map.put("\u6258\u6570", "\u6258\u6570");
        map.put("\u9001\u8d27\u5355\u4f4d", "\u9001\u8d27\u5355\u4f4d");
        map.put("\u63d0\u8d27\u5355\u4f4d", "\u63d0\u8d27\u5355\u4f4d");
        map.put("\u5907\u6ce8", "\u5907\u6ce8");
        return map;
    }

    private void setupPredefinedColumns() {
        TableColumn jcbhCol = new TableColumn("\u8fdb\u4ed3\u7f16\u53f7");
        jcbhCol.setCellValueFactory(new PropertyValueFactory("jcbh"));
        jcbhCol.setPrefWidth(100.0);
        jcbhCol.setCellFactory(col -> this.createCustomCell());
        TableColumn jczyhCol = new TableColumn("\u8fdb\u4ed3\u4f5c\u4e1a\u53f7");
        jczyhCol.setCellValueFactory(new PropertyValueFactory("jczyh"));
        jczyhCol.setPrefWidth(100.0);
        jczyhCol.setCellFactory(col -> this.createCustomCell());
        TableColumn yjrqCol = new TableColumn("\u9884\u8fdb\u65e5\u671f");
        yjrqCol.setCellValueFactory(new PropertyValueFactory("yjrq"));
        yjrqCol.setPrefWidth(140.0);
        yjrqCol.setCellFactory(col -> this.createCustomCell());
        TableColumn jcrqCol = new TableColumn("\u8fdb\u4ed3\u65e5\u671f");
        jcrqCol.setCellValueFactory(new PropertyValueFactory("jcrq"));
        jcrqCol.setPrefWidth(140.0);
        jcrqCol.setCellFactory(col -> this.createCustomCell());
        TableColumn lfCol = new TableColumn("L/F");
        lfCol.setCellValueFactory(new PropertyValueFactory("lf"));
        lfCol.setPrefWidth(50.0);
        lfCol.setCellFactory(col -> this.createCustomCell());
        TableColumn bzggCol = new TableColumn("\u5305\u88c5\u89c4\u683c");
        bzggCol.setCellValueFactory(new PropertyValueFactory("bzgg"));
        bzggCol.setPrefWidth(80.0);
        bzggCol.setCellFactory(col -> this.createCustomCell());
        TableColumn hwmcCol = new TableColumn("\u8d27\u7269\u540d\u79f0");
        hwmcCol.setCellValueFactory(new PropertyValueFactory("hwmc"));
        hwmcCol.setPrefWidth(100.0);
        hwmcCol.setCellFactory(col -> this.createCustomCell());
        TableColumn mtCol = new TableColumn("\u551b\u5934");
        mtCol.setCellValueFactory(new PropertyValueFactory("mt"));
        mtCol.setPrefWidth(100.0);
        this.configureColumnBehavior(mtCol, "mt");
        TableColumn hhCol = new TableColumn("\u8d27\u53f7");
        hhCol.setCellValueFactory(new PropertyValueFactory("hh"));
        hhCol.setPrefWidth(80.0);
        hhCol.setCellFactory(col -> this.createCustomCell());
        TableColumn jsCol = new TableColumn("\u4ef6\u6570");
        jsCol.setCellValueFactory(new PropertyValueFactory("js"));
        jsCol.setPrefWidth(60.0);
        jsCol.setCellFactory(col -> this.createCustomCell());
        TableColumn tjCol = new TableColumn("\u4f53\u79ef");
        tjCol.setCellValueFactory(new PropertyValueFactory("tj"));
        tjCol.setPrefWidth(60.0);
        tjCol.setCellFactory(col -> this.createCustomCell());
        TableColumn kctjCol = new TableColumn("\u5e93\u5b58\u4f53\u79ef");
        kctjCol.setCellValueFactory(new PropertyValueFactory("kctj"));
        kctjCol.setPrefWidth(80.0);
        kctjCol.setCellFactory(col -> this.createCustomCell());
        TableColumn mzCol = new TableColumn("\u6bdb\u91cd");
        mzCol.setCellValueFactory(new PropertyValueFactory("mz"));
        mzCol.setPrefWidth(60.0);
        mzCol.setCellFactory(col -> this.createCustomCell());
        TableColumn tsCol = new TableColumn("\u6258\u6570");
        tsCol.setCellValueFactory(new PropertyValueFactory("ts"));
        tsCol.setPrefWidth(60.0);
        tsCol.setCellFactory(col -> this.createCustomCell());
        TableColumn shdwCol = new TableColumn("\u9001\u8d27\u5355\u4f4d");
        shdwCol.setCellValueFactory(new PropertyValueFactory("shdw"));
        shdwCol.setPrefWidth(150.0);
        shdwCol.setCellFactory(col -> this.createCustomCell());
        TableColumn kcjsCol = new TableColumn("\u5e93\u5b58\u4ef6\u6570");
        kcjsCol.setCellValueFactory(new PropertyValueFactory("kcjs"));
        kcjsCol.setPrefWidth(80.0);
        kcjsCol.setCellFactory(col -> this.createCustomCell());
        TableColumn bgztCol = new TableColumn("\u62a5\u5173\u72b6\u6001");
        bgztCol.setCellValueFactory(new PropertyValueFactory("bgzt"));
        bgztCol.setPrefWidth(80.0);
        bgztCol.setCellFactory(col -> this.createCustomCell());
        TableColumn bzCol = new TableColumn("\u5907\u6ce8");
        bzCol.setCellValueFactory(new PropertyValueFactory("bz"));
        bzCol.setPrefWidth(150.0);
        bzCol.setCellFactory(col -> this.createCustomCell());
        this.resultTableView.getColumns().addAll(jcbhCol, jczyhCol, yjrqCol, jcrqCol, lfCol, bzggCol, hwmcCol, mtCol, hhCol, jsCol, tjCol, kctjCol, mzCol, tsCol, shdwCol, kcjsCol, bgztCol, bzCol);
        this.addOperationColumn();
    }

    private void addOperationColumn() {
        TableColumn actionCol = new TableColumn("\u64cd\u4f5c");
        actionCol.setPrefWidth(120.0);
        actionCol.setCellFactory(column -> {
            TableCell<WarehouseEntry, Void> cell = new TableCell<WarehouseEntry, Void>(){
                private final HBox hbox = new HBox(5.0);
                private final Button txxBtn = new Button("\u6258\u4fe1\u606f");
                private final Button zpBtn = new Button("\u7167\u7247");
                {
                    this.txxBtn.setOnAction(event -> {
                        WarehouseEntry entry = (WarehouseEntry)this.getTableRow().getItem();
                        if (MainController.this.hasOpenEntryId(entry)) {
                            MainController.this.openTxxPage(entry);
                        }
                    });
                    this.zpBtn.setOnAction(event -> {
                        WarehouseEntry entry = (WarehouseEntry)this.getTableRow().getItem();
                        if (MainController.this.hasOpenEntryId(entry)) {
                            MainController.this.openZpPage(entry);
                        }
                    });
                    this.hbox.getChildren().addAll((Node[])new Node[]{this.txxBtn, this.zpBtn});
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        this.setGraphic(null);
                    } else {
                        WarehouseEntry entry = (WarehouseEntry)this.getTableRow().getItem();
                        if (entry != null) {
                            String bz = entry.getBz();
                            boolean isSpecial = bz != null && (bz.contains("\u672a\u627e\u5230\u8bb0\u5f55") || bz.contains("\u6ca1\u6709\u627e\u5230") || bz.contains("\u67e5\u8be2\u7ed3\u679c\u4e3a\u7a7a") || bz.contains("\u672a\u80fd\u89e3\u6790") || bz.contains("\u62b1\u6b49") || bz.contains("\u8bf7\u68c0\u67e5"));
                            boolean hasOpenId = MainController.this.hasOpenEntryId(entry);
                            this.txxBtn.setDisable(isSpecial || !hasOpenId);
                            this.zpBtn.setDisable(isSpecial || !hasOpenId);
                            this.setGraphic(this.hbox);
                        } else {
                            this.setGraphic(null);
                        }
                    }
                }
            };
            return cell;
        });
        this.resultTableView.getColumns().add(actionCol);
    }

    private double calculateColumnWidth(String header) {
        int length = header.length();
        if (length <= 2) {
            return 50.0;
        }
        if (length <= 4) {
            return 80.0;
        }
        if (length <= 6) {
            return 100.0;
        }
        if (length <= 8) {
            return 120.0;
        }
        return 150.0;
    }

    private void openTxxPage(WarehouseEntry entry) {
        String openId = this.getEntryOpenId(entry);
        String jcbh = entry.getJcbh();
        String zyh = this.getEntryZyh(entry);
        String url = AppConfig.getTxxUrl(openId, jcbh, zyh, entry.getBzgg());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("\u67e5\u770b\u6258\u4fe1\u606f");
        alert.setHeaderText("\u8fdb\u4ed3\u7f16\u53f7\uff1a" + jcbh + " | \u4f5c\u4e1a\u53f7\uff1a" + zyh + " | \u6253\u5f00ID\uff1a" + openId);
        alert.setContentText("\u662f\u5426\u5728\u5916\u90e8\u6d4f\u89c8\u5668\u4e2d\u6253\u5f00\u6258\u4fe1\u606f\u9875\u9762\uff1f");
        ButtonType openButton = new ButtonType("\u6253\u5f00");
        ButtonType cancelButton = new ButtonType("\u53d6\u6d88", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll((ButtonType[])new ButtonType[]{openButton, cancelButton});
        Optional result = alert.showAndWait();
        if (result.isPresent() && result.get() == openButton) {
            this.openWebPage(url);
        }
    }

    private void openZpPage(WarehouseEntry entry) {
        String url = AppConfig.getPhotoUrl(this.getEntryOpenId(entry), entry.getJcbh(), this.getEntryZyh(entry), entry.getBzgg());
        this.openWebPage(url);
        System.out.println("\u6253\u5f00\u7167\u7247\u9875\u9762: " + url + " (\u8fdb\u4ed3\u7f16\u53f7: " + entry.getJcbh() + ")");
    }

    private String getEntryOpenId(WarehouseEntry entry) {
        if (entry == null) {
            return "";
        }
        if (entry.getInguid() != null && !entry.getInguid().isBlank()) {
            return entry.getInguid();
        }
        if (entry.getJcid() != null && !entry.getJcid().isBlank()) {
            return entry.getJcid();
        }
        return "";
    }

    private boolean hasOpenEntryId(WarehouseEntry entry) {
        return !this.getEntryOpenId(entry).isEmpty();
    }

    private String getEntryZyh(WarehouseEntry entry) {
        if (entry == null) {
            return "";
        }
        if (entry.getZyh() != null && !entry.getZyh().isBlank()) {
            return entry.getZyh();
        }
        if (entry.getJczyh() != null && !entry.getJczyh().isBlank()) {
            return entry.getJczyh();
        }
        return "";
    }

    private void openWebPage(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        }
        catch (Exception e) {
            this.showAlert(Alert.AlertType.ERROR, "\u6253\u5f00\u9875\u9762\u5931\u8d25", "\u65e0\u6cd5\u6253\u5f00\u7f51\u9875\uff1a" + e.getMessage());
        }
    }

    private void viewLogs() {
        try {
            if (!Files.exists(Paths.get("request_log.txt", new String[0]), new LinkOption[0])) {
                Files.write(Paths.get("request_log.txt", new String[0]), "\u6682\u65e0\u8bf7\u6c42\u65e5\u5fd7".getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            }
            if (!Files.exists(Paths.get("response_log.txt", new String[0]), new LinkOption[0])) {
                Files.write(Paths.get("response_log.txt", new String[0]), "\u6682\u65e0\u54cd\u5e94\u65e5\u5fd7".getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            }
            String requestLog = Files.readString(Paths.get("request_log.txt", new String[0]), StandardCharsets.UTF_8);
            String responseLog = Files.readString(Paths.get("response_log.txt", new String[0]), StandardCharsets.UTF_8);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("\u67e5\u770b\u65e5\u5fd7");
            alert.setHeaderText("\u8bf7\u6c42\u548c\u54cd\u5e94\u65e5\u5fd7");
            TabPane tabPane = new TabPane();
            Tab requestTab = new Tab("\u8bf7\u6c42\u65e5\u5fd7");
            TextArea requestTextArea = new TextArea(requestLog);
            requestTextArea.setEditable(false);
            requestTextArea.setWrapText(true);
            requestTab.setContent(requestTextArea);
            Tab responseTab = new Tab("\u54cd\u5e94\u65e5\u5fd7");
            TextArea responseTextArea = new TextArea(responseLog);
            responseTextArea.setEditable(false);
            responseTextArea.setWrapText(true);
            responseTab.setContent(responseTextArea);
            Tab errorTab = new Tab("\u9519\u8bef\u5206\u6790");
            List<String> errors = this.extractErrorsFromLogs();
            String errorAnalysis = errors.isEmpty() ? "\u672a\u53d1\u73b0\u660e\u663e\u9519\u8bef" : String.join((CharSequence)"\n", errors);
            TextArea errorTextArea = new TextArea(errorAnalysis);
            errorTextArea.setEditable(false);
            errorTextArea.setWrapText(true);
            errorTab.setContent(errorTextArea);
            tabPane.getTabs().addAll((Tab[])new Tab[]{requestTab, responseTab, errorTab});
            alert.getDialogPane().setContent(tabPane);
            alert.getDialogPane().setPrefSize(800.0, 600.0);
            alert.showAndWait();
        }
        catch (IOException e) {
            ExceptionHandler.handleFileException("\u67e5\u770b\u65e5\u5fd7\u6587\u4ef6", e);
        }
    }

    private List<String> extractErrorsFromLogs() {
        ArrayList<String> errors = new ArrayList<String>();
        try {
            if (Files.exists(Paths.get("response_log.txt", new String[0]), new LinkOption[0])) {
                String responseLog = Files.readString(Paths.get("response_log.txt", new String[0]), StandardCharsets.UTF_8);
                Pattern errorPattern = Pattern.compile("\u9519\u8bef\u4fe1\u606f: (.+)");
                Matcher matcher = errorPattern.matcher(responseLog);
                while (matcher.find()) {
                    String error = matcher.group(1).trim();
                    if (error.isEmpty() || errors.contains(error)) continue;
                    errors.add(error);
                }
                Pattern warningPattern = Pattern.compile("\u8b66\u544a: (.+)");
                matcher = warningPattern.matcher(responseLog);
                while (matcher.find()) {
                    String warning = matcher.group(1).trim();
                    if (warning.isEmpty() || errors.contains(warning)) continue;
                    errors.add(warning);
                }
            }
        }
        catch (IOException e) {
            System.err.println("\u8bfb\u53d6\u54cd\u5e94\u65e5\u5fd7\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
        }
        return errors;
    }

    private String fixJcbhFormat(String jcbh) {
        jcbh = jcbh.trim().replaceAll("\\s+", "");
        return jcbh;
    }

    @FXML
    private void handleQuery() {
        String jcbhInput = this.jcbhTextField.getText().trim();
        LocalDate startDate = (LocalDate)this.startDatePicker.getValue();
        LocalDate endDate = (LocalDate)this.endDatePicker.getValue();
        String status = (String)this.statusComboBox.getValue();
        if (startDate == null || endDate == null) {
            this.showAlert(Alert.AlertType.ERROR, "\u8f93\u5165\u9519\u8bef", "\u8bf7\u9009\u62e9\u5f00\u59cb\u65e5\u671f\u548c\u7ed3\u675f\u65e5\u671f");
            return;
        }
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 180L) {
            LocalDate newStartDate = endDate.minusDays(180L);
            this.startDatePicker.setValue(newStartDate);
            startDate = newStartDate;
            this.showAlert(Alert.AlertType.WARNING, "\u65e5\u671f\u8303\u56f4\u8fc7\u957f", "\u67e5\u8be2\u65e5\u671f\u8303\u56f4\u5df2\u81ea\u52a8\u8c03\u6574\u4e3a180\u5929\uff0c\n\u56e0\u4e3a\u670d\u52a1\u5668\u9650\u5236\u67e5\u8be2\u8303\u56f4\u4e0d\u80fd\u8d85\u8fc7180\u5929\u3002");
        }
        this.entryList.clear();
        String jcbhText = jcbhInput;
        if (jcbhText.isEmpty()) {
            this.showAlert(Alert.AlertType.WARNING, "\u8f93\u5165\u9519\u8bef", "\u8bf7\u8f93\u5165\u8fdb\u4ed3\u7f16\u53f7");
            return;
        }
        List<String> jcbhList = Arrays.stream(jcbhText.split("[,\\s]+")).filter(s -> !s.trim().isEmpty()).map(this::fixJcbhFormat).collect(Collectors.toList());
        this.jcbhTextField.setText(String.join((CharSequence)", ", jcbhList));
        this.currentStatusIndex = this.statusComboBox.getSelectionModel().getSelectedIndex();
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        this.saveLastQueryInputs(String.join((CharSequence)", ", jcbhList), startDate, endDate, this.currentStatusIndex);
        this.setQueryInProgress(true, "\u6b63\u5728\u51c6\u5907\u67e5\u8be2...");
        this.queryResultLabel.setText("\u6b63\u5728\u67e5\u8be2\uff0c\u8bf7\u7a0d\u5019...");
        this.service.queryWarehouse(jcbhList, this.currentStatusIndex, startDateStr, endDateStr).thenAcceptAsync(entries -> Platform.runLater(() -> {
            try {
                boolean hasNoClassDefError;
                boolean isErrorEntry;
                MainController.this.localEntryCacheService.mergeNetworkEntries(entries);
                int originalCount = entries.size();
                boolean bl = isErrorEntry = originalCount == 1 && ((WarehouseEntry)entries.get(0)).getBz() != null && (((WarehouseEntry)entries.get(0)).getBz().contains("\u9519\u8bef") || ((WarehouseEntry)entries.get(0)).getBz().contains("API") || ((WarehouseEntry)entries.get(0)).getBz().contains("\u65e5\u671f\u8303\u56f4") || ((WarehouseEntry)entries.get(0)).getBz().contains("\u6ca1\u6709\u627e\u5230"));
                if (isErrorEntry) {
                    LocalDate end;
                    String errorMsg = ((WarehouseEntry)entries.get(0)).getBz();
                    this.queryResultLabel.setText("\u63d0\u793a: " + errorMsg);
                    this.queryResultLabel.setStyle("-fx-text-fill: #ff6600;");
                    if ((errorMsg.contains("\u65e5\u671f\u8303\u56f4") || errorMsg.contains("180\u5929")) && (end = (LocalDate)this.endDatePicker.getValue()) != null) {
                        LocalDate newStart = end.minusDays(179L);
                        this.startDatePicker.setValue(newStart);
                        this.queryResultLabel.setText("\u65e5\u671f\u8303\u56f4\u5df2\u81ea\u52a8\u8c03\u6574\u4e3a180\u5929\uff0c\u8bf7\u91cd\u65b0\u67e5\u8be2");
                        this.queryResultLabel.setStyle("-fx-text-fill: #0066cc;");
                    }
                }
                if (hasNoClassDefError = entries.stream().map(WarehouseEntry::getBz).filter(Objects::nonNull).anyMatch(bz -> bz.contains("NoClassDefFoundError"))) {
                    this.showAlert(Alert.AlertType.ERROR, "\u5e93\u7f3a\u5931\u9519\u8bef", "\u7cfb\u7edf\u7f3a\u5c11\u5fc5\u8981\u7684JSON\u5e93\u3002\n\u8bf7\u5b89\u88c5org.json\u5e93\u540e\u518d\u8bd5\u3002\n\u53ef\u4ee5\u5c06json-20230227.jar\u6587\u4ef6\u6dfb\u52a0\u5230lib\u76ee\u5f55\u4e0b\u89e3\u51b3\u6b64\u95ee\u9898\u3002");
                }
                this.masterEntries.clear();
                this.masterEntries.addAll((Collection<WarehouseEntry>)entries);
                this.lastRawEntryCount = originalCount;
                this.lastQueryWasError = isErrorEntry;
                this.applyFiltersToView();
                if (WarehouseService.hasExtractedTableHeaders()) {
                    List<String> headers = WarehouseService.getExtractedTableHeaders();
                    System.out.println("\u4ece\u670d\u52a1\u83b7\u53d6\u5230\u8868\u5934: " + String.join((CharSequence)", ", headers));
                    this.setupColumnsForStatus(this.currentStatusIndex, headers);
                }
            }
            catch (Exception e) {
                ExceptionHandler.handleException("\u5904\u7406\u67e5\u8be2\u7ed3\u679c", e);
            }
            finally {
                this.setQueryInProgress(false, "");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                try {
                    this.queryResultLabel.setText("\u67e5\u8be2\u5931\u8d25: " + ex.getMessage());
                    this.queryResultLabel.setStyle("-fx-text-fill: #ff6600;");
                    for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
                        if (!(cause instanceof NoClassDefFoundError) || !cause.getMessage().contains("org/json/JSONObject")) continue;
                        this.showAlert(Alert.AlertType.ERROR, "\u5e93\u7f3a\u5931\u9519\u8bef", "\u7cfb\u7edf\u7f3a\u5c11\u5fc5\u8981\u7684JSON\u5e93\u3002\n\u8bf7\u5b89\u88c5org.json\u5e93\u540e\u518d\u8bd5\u3002\n\u53ef\u4ee5\u5c06json-20230227.jar\u6587\u4ef6\u6dfb\u52a0\u5230lib\u76ee\u5f55\u4e0b\u89e3\u51b3\u6b64\u95ee\u9898\u3002");
                        break;
                    }
                }
                finally {
                    this.setQueryInProgress(false, "");
                }
            });
            return null;
        });
    }

    private void updateTotals() {
        int totalJs = this.entryList.stream().mapToInt(WarehouseEntry::getJs).sum();
        double totalTj = this.entryList.stream().mapToDouble(WarehouseEntry::getTj).sum();
        double totalKctj = this.entryList.stream().mapToDouble(WarehouseEntry::getKctj).sum();
        double totalMz = this.entryList.stream().mapToDouble(WarehouseEntry::getMz).sum();
        double totalKcjs = this.entryList.stream().mapToDouble(WarehouseEntry::getKcjs).sum();
        this.totalJsLabel.setText(String.format("%d", totalJs));
        this.totalTjLabel.setText(String.format("%.2f", totalTj));
        this.totalKctjLabel.setText(String.format("%.2f", totalKctj));
        this.totalMzLabel.setText(String.format("%.2f", totalMz));
        this.totalKcjsLabel.setText(String.format("%.2f", totalKcjs));
    }

    private void showTotalsDialog() {
        if (this.entryList.isEmpty()) {
            this.showAlert(Alert.AlertType.INFORMATION, "\u7edf\u8ba1\u4fe1\u606f", "\u5f53\u524d\u6ca1\u6709\u6570\u636e\u53ef\u4f9b\u7edf\u8ba1\u3002");
            return;
        }
        int totalCount = this.entryList.size();
        int totalJs = this.entryList.stream().mapToInt(WarehouseEntry::getJs).sum();
        double totalTj = this.entryList.stream().mapToDouble(WarehouseEntry::getTj).sum();
        double totalKctj = this.entryList.stream().mapToDouble(WarehouseEntry::getKctj).sum();
        double totalMz = this.entryList.stream().mapToDouble(WarehouseEntry::getMz).sum();
        double totalKcjs = this.entryList.stream().mapToDouble(WarehouseEntry::getKcjs).sum();
        List<WarehouseEntry> selectedEntries = this.entryList.stream().filter(WarehouseEntry::isSelected).collect(Collectors.toList());
        int selectedCount = selectedEntries.size();
        int selectedJs = selectedEntries.stream().mapToInt(WarehouseEntry::getJs).sum();
        double selectedTj = selectedEntries.stream().mapToDouble(WarehouseEntry::getTj).sum();
        double selectedKctj = selectedEntries.stream().mapToDouble(WarehouseEntry::getKctj).sum();
        double selectedMz = selectedEntries.stream().mapToDouble(WarehouseEntry::getMz).sum();
        double selectedKcjs = selectedEntries.stream().mapToDouble(WarehouseEntry::getKcjs).sum();
        StringBuilder message = new StringBuilder();
        message.append("\u603b\u8bb0\u5f55: ").append(totalCount).append("\n").append("\u4ef6\u6570: ").append(totalJs).append("\n").append(String.format("\u4f53\u79ef: %.2f (\u5e93\u5b58\u4f53\u79ef: %.2f)\n", totalTj, totalKctj)).append(String.format("\u6bdb\u91cd: %.2f (\u5e93\u5b58\u4ef6\u6570: %.2f)\n", totalMz, totalKcjs));
        message.append("\n\u9009\u4e2d\u8bb0\u5f55: ").append(selectedCount).append("\n").append("\u4ef6\u6570: ").append(selectedJs).append("\n").append(String.format("\u4f53\u79ef: %.2f (\u5e93\u5b58\u4f53\u79ef: %.2f)\n", selectedTj, selectedKctj)).append(String.format("\u6bdb\u91cd: %.2f (\u5e93\u5b58\u4ef6\u6570: %.2f)", selectedMz, selectedKcjs));
        this.showAlert(Alert.AlertType.INFORMATION, "\u7edf\u8ba1\u6c47\u603b", message.toString());
    }

    private void setQueryInProgress(boolean inProgress, String message) {
        if (this.queryOverlay != null) {
            this.queryOverlay.setVisible(inProgress);
            this.queryOverlay.setManaged(inProgress);
        }
        if (this.overlayMessageLabel != null && message != null) {
            this.overlayMessageLabel.setText(message);
        }
        this.progressBar.setVisible(inProgress);
        this.progressBar.setProgress(inProgress ? -1.0 : 0.0);
        this.progressLabel.setVisible(inProgress);
        this.progressLabel.setText(inProgress ? message : "");
        this.queryButton.setDisable(inProgress);
        this.exportSelectedButton.setDisable(inProgress);
        if (this.exportButton != null) {
            this.exportButton.setDisable(inProgress);
        }
        if (this.advancedToggleButton != null) {
            this.advancedToggleButton.setDisable(inProgress);
        }
        if (this.viewLogsButton != null) {
            this.viewLogsButton.setDisable(inProgress);
        }
        if (this.webVersionButton != null) {
            this.webVersionButton.setDisable(inProgress);
        }
        if (this.filterPane != null) {
            this.filterPane.setDisable(inProgress);
        }
        if (this.advancedFilterPane != null) {
            this.advancedFilterPane.setDisable(inProgress);
        }
        if (this.resetFiltersButton != null) {
            this.resetFiltersButton.setDisable(inProgress);
        }
        if (this.resultTableView != null) {
            this.resultTableView.setDisable(inProgress);
        }
        if (this.queryMenuItem != null) {
            this.queryMenuItem.setDisable(inProgress);
        }
        if (this.exportMenuItem != null) {
            this.exportMenuItem.setDisable(inProgress);
        }
        if (this.exportSelectedMenuItem != null) {
            this.exportSelectedMenuItem.setDisable(inProgress);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private List<WarehouseEntry> filterEntries(List<WarehouseEntry> entries, Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return entries;
        }
        return entries.stream().filter(entry -> this.matchesAdvancedFilters((WarehouseEntry)entry, filters)).collect(Collectors.toList());
    }

    private boolean matchesAdvancedFilters(WarehouseEntry entry, Map<String, String> filters) {
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            boolean matchesField;
            List<String> terms;
            String key = filter.getKey();
            String keyword = filter.getValue();
            if (keyword == null || keyword.isEmpty() || (terms = this.parseSearchTerms(keyword, this.isExactMatchField(key))).isEmpty() || (matchesField = this.matchEntryField(entry, key, terms))) continue;
            return false;
        }
        return true;
    }

    private List<String> parseSearchTerms(String input, boolean exactMatch) {
        if (input == null) {
            return Collections.emptyList();
        }
        String normalized = input.trim();
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = normalized.split("[\\s,\uff0c\u3001;\uff1b]+");
        ArrayList<String> terms = new ArrayList<String>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            terms.add(exactMatch ? trimmed : trimmed.toLowerCase());
        }
        return terms;
    }

    private boolean isExactMatchField(String key) {
        return FILTER_MARK.equals(key);
    }

    private boolean matchEntryField(WarehouseEntry entry, String key, List<String> terms) {
        switch (key) {
            case "owner": {
                return this.matchesAnySubstring(entry.getHz(), terms);
            }
            case "entryNumber": {
                return this.matchesAnySubstring(entry.getJcbh(), terms);
            }
            case "jobNumber": {
                return this.matchesAnySubstring(entry.getJczyh(), terms) || this.matchesAnySubstring(entry.getZyh(), terms);
            }
            case "driver": {
                return this.matchesAnySubstring(entry.getJsy(), terms);
            }
            case "plate": {
                return this.matchesAnySubstring(entry.getCh(), terms);
            }
            case "cargo": {
                return this.matchesAnySubstring(entry.getHwmc(), terms) || this.matchesAnySubstring(entry.getHwmc1(), terms);
            }
            case "package": {
                return this.matchesAnySubstring(entry.getBzgg(), terms);
            }
            case "driverPhone": {
                return this.matchesAnySubstring(entry.getJsydh(), terms);
            }
            case "mark": {
                return this.matchesExact(entry.getMt(), terms);
            }
        }
        return true;
    }

    private boolean matchesAnySubstring(String source, List<String> terms) {
        if (source == null) {
            return false;
        }
        String lower = source.toLowerCase();
        for (String term : terms) {
            if (!lower.contains(term)) continue;
            return true;
        }
        return false;
    }

    private boolean matchesExact(String source, List<String> terms) {
        if (source == null) {
            return false;
        }
        for (String term : terms) {
            if (!source.equals(term)) continue;
            return true;
        }
        return false;
    }

    private void applyFiltersToView() {
        Map<String, String> filters = this.collectAdvancedFilters();
        this.saveLastAdvancedFilters(filters);
        if (this.masterEntries.isEmpty()) {
            this.entryList.clear();
            this.updateTotals();
            return;
        }
        List<WarehouseEntry> filtered = this.filterEntries(this.masterEntries, filters);
        this.entryList.setAll((Collection<WarehouseEntry>)filtered);
        this.updateTotals();
        if (!this.lastQueryWasError) {
            this.updateFilterStatusMessage(filtered.size(), this.lastRawEntryCount, !filters.isEmpty());
        }
    }

    private void updateFilterStatusMessage(int filteredCount, int rawCount, boolean filtersActive) {
        if (this.queryResultLabel == null) {
            return;
        }
        if (rawCount == 0) {
            this.queryResultLabel.setText("\u672a\u627e\u5230\u4efb\u4f55\u8bb0\u5f55\uff0c\u8bf7\u8c03\u6574\u67e5\u8be2\u6761\u4ef6");
            this.queryResultLabel.setStyle("-fx-text-fill: #ff6600;");
            return;
        }
        if (filteredCount == 0) {
            this.queryResultLabel.setText(filtersActive ? "\u5f53\u524d\u7b5b\u9009\u672a\u5339\u914d\u4efb\u4f55\u8bb0\u5f55" : "\u672a\u627e\u5230\u4efb\u4f55\u8bb0\u5f55");
            this.queryResultLabel.setStyle("-fx-text-fill: #ff6600;");
        } else if (filtersActive && filteredCount != rawCount) {
            this.queryResultLabel.setText("\u7b5b\u9009\u7ed3\u679c: " + filteredCount + " / " + rawCount + " \u6761");
            this.queryResultLabel.setStyle("-fx-text-fill: #2c3e50;");
        } else {
            this.queryResultLabel.setText("\u67e5\u8be2\u5b8c\u6210\uff0c\u627e\u5230 " + filteredCount + " \u6761\u8bb0\u5f55");
            this.queryResultLabel.setStyle("");
        }
    }

    @SuppressWarnings("unchecked")
    private void configureColumnBehavior(TableColumn<WarehouseEntry, ?> column, String fieldName) {
        if ("mt".equals(fieldName)) {
            this.configureEditableMarkColumn((TableColumn<WarehouseEntry, String>)column);
            return;
        }
        ((TableColumn<WarehouseEntry, Object>)column).setCellFactory(col -> this.createCustomCell());
    }

    private void configureEditableMarkColumn(TableColumn<WarehouseEntry, String> column) {
        column.setEditable(true);
        column.setCellFactory(col -> this.createEditableMarkCell());
        column.setOnEditCommit(event -> {
            WarehouseEntry entry = (WarehouseEntry)event.getRowValue();
            if (entry == null || !this.localEntryCacheService.hasPersistentKey(entry) || this.isSpecialEntry(entry)) {
                return;
            }
            String newValue = event.getNewValue() == null ? "" : event.getNewValue().trim();
            entry.setMt(newValue);
            this.localEntryCacheService.saveEditedMark(entry);
            this.resultTableView.refresh();
        });
    }

    private TableCell<WarehouseEntry, String> createEditableMarkCell() {
        return new TextFieldTableCell<WarehouseEntry, String>(new DefaultStringConverter()){
            @Override
            public void startEdit() {
                WarehouseEntry entry = MainController.this.getRowEntry(this);
                if (entry == null || MainController.this.isSpecialEntry(entry) || !MainController.this.localEntryCacheService.hasPersistentKey(entry)) {
                    return;
                }
                super.startEdit();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                MainController.this.applyCellPresentation(this, this.getItem(), this.isEmpty());
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!this.isEditing()) {
                    MainController.this.applyCellPresentation(this, item, empty);
                }
            }
        };
    }

    private <T> TableCell<WarehouseEntry, T> createCustomCell() {
        return new TableCell<WarehouseEntry, T>(){

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                MainController.this.applyCellPresentation(this, item, empty);
            }
        };
    }

    private void applyCellPresentation(TableCell<?, ?> cell, Object item, boolean empty) {
        if (empty || item == null) {
            cell.setText(null);
            cell.setStyle("");
            cell.setOnContextMenuRequested(null);
            return;
        }
        WarehouseEntry entry = this.getRowEntry(cell);
        cell.setStyle(this.isSpecialEntry(entry) ? "-fx-background-color: #ffeeee;" : "");
        cell.setText(item.toString());
        cell.setOnContextMenuRequested(event -> {
            if (cell.getItem() != null && !cell.getItem().toString().isEmpty()) {
                this.showCellContextMenu(cell, event.getScreenX(), event.getScreenY(), cell.getItem().toString());
            }
        });
    }

    private WarehouseEntry getRowEntry(TableCell<?, ?> cell) {
        if (cell == null) {
            return null;
        }
        TableRow<?> tableRow = cell.getTableRow();
        if (tableRow == null) {
            return null;
        }
        Object rowItem = tableRow.getItem();
        if (rowItem instanceof WarehouseEntry) {
            return (WarehouseEntry)rowItem;
        }
        return null;
    }

    private boolean isSpecialEntry(WarehouseEntry entry) {
        if (entry == null) {
            return false;
        }
        String bz = entry.getBz();
        return bz != null && (bz.contains("\u672a\u627e\u5230\u8bb0\u5f55")
            || bz.contains("\u6ca1\u6709\u627e\u5230")
            || bz.contains("\u67e5\u8be2\u7ed3\u679c\u4e3a\u7a7a")
            || bz.contains("\u672a\u80fd\u89e3\u6790")
            || bz.contains("\u62b1\u6b49")
            || bz.contains("\u8bf7\u68c0\u67e5"));
    }

    private void showCellContextMenu(TableCell<?, ?> cell, double x, double y, String content) {
        if (this.cellContextMenu == null) {
            this.cellContextMenu = new ContextMenu();
            MenuItem copyMenuItem = new MenuItem("\u590d\u5236\u5185\u5bb9");
            copyMenuItem.setOnAction(event -> {
                String textToCopy = (String)this.cellContextMenu.getUserData();
                if (textToCopy != null && !textToCopy.isEmpty()) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent clipContent = new ClipboardContent();
                    clipContent.putString(textToCopy);
                    clipboard.setContent(clipContent);
                    Tooltip tooltip = new Tooltip("\u5df2\u590d\u5236\u5230\u526a\u8d34\u677f");
                    tooltip.setAutoHide(true);
                    tooltip.show(cell, x, y + 10.0);
                    PauseTransition delay = new PauseTransition(Duration.seconds(1.0));
                    delay.setOnFinished(e -> tooltip.hide());
                    delay.play();
                }
            });
            this.cellContextMenu.getItems().add(copyMenuItem);
        }
        this.cellContextMenu.setUserData(content);
        this.cellContextMenu.show(cell, x, y);
    }

    @FXML
    private void handleExit() {
        Alert confirmExit = new Alert(Alert.AlertType.CONFIRMATION);
        confirmExit.setTitle("\u9000\u51fa\u786e\u8ba4");
        confirmExit.setHeaderText(null);
        confirmExit.setContentText("\u786e\u5b9a\u8981\u9000\u51fa\u5e94\u7528\u7a0b\u5e8f\u5417\uff1f");
        Optional result = confirmExit.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            this.service.close();
            Platform.exit();
        }
    }

    private void handleExport() {
        if (this.entryList.isEmpty()) {
            this.showAlert(Alert.AlertType.WARNING, "\u5bfc\u51fa\u63d0\u793a", "\u5f53\u524d\u6ca1\u6709\u6570\u636e\u53ef\u4ee5\u5bfc\u51fa");
            return;
        }
        try {
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "\u4ed3\u5e93\u67e5\u8be2_" + timeStamp + ".csv";
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("\u8fdb\u4ed3\u7f16\u53f7,\u8fdb\u4ed3\u4f5c\u4e1a\u53f7,\u9884\u8fdb\u65e5\u671f,\u8fdb\u4ed3\u65e5\u671f,L/F,\u5305\u88c5\u89c4\u683c,\u8d27\u7269\u540d\u79f0,\u551b\u5934,\u8d27\u53f7,\u4ef6\u6570,\u4f53\u79ef,\u5e93\u5b58\u4f53\u79ef,\u6bdb\u91cd,\u6258\u6570,\u9001\u8d27\u5355\u4f4d,\u5e93\u5b58\u4ef6\u6570,\u62a5\u5173\u72b6\u6001,\u5907\u6ce8\n");
            for (WarehouseEntry entry : this.entryList) {
                csvContent.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%.2f,%.2f,%.2f,%d,%s,%.2f,%s,%s\n", entry.getJcbh(), entry.getJczyh(), entry.getYjrq(), entry.getJcrq(), entry.getLf(), entry.getBzgg(), entry.getHwmc(), entry.getMt(), entry.getHh(), entry.getJs(), entry.getTj(), entry.getKctj(), entry.getMz(), entry.getTs(), entry.getShdw(), entry.getKcjs(), entry.getBgzt(), entry.getBz()));
            }
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(fileName), StandardCharsets.UTF_8));){
                writer.write(csvContent.toString());
            }
            this.showAlert(Alert.AlertType.INFORMATION, "\u5bfc\u51fa\u6210\u529f", "\u6570\u636e\u5df2\u6210\u529f\u5bfc\u51fa\u5230\u6587\u4ef6\uff1a" + fileName);
        }
        catch (IOException e) {
            this.showAlert(Alert.AlertType.ERROR, "\u5bfc\u51fa\u5931\u8d25", "\u5bfc\u51fa\u6570\u636e\u65f6\u53d1\u751f\u9519\u8bef\uff1a" + e.getMessage());
        }
    }

    private void calculateSelectedRowsSum() {
        ObservableList<WarehouseEntry> selectedEntries = this.resultTableView.getSelectionModel().getSelectedItems();
        if (selectedEntries.isEmpty()) {
            this.showAlert(Alert.AlertType.INFORMATION, "\u8ba1\u7b97\u63d0\u793a", "\u8bf7\u5148\u9009\u62e9\u8981\u8ba1\u7b97\u7684\u884c");
            return;
        }
        int totalJs = selectedEntries.stream().mapToInt(WarehouseEntry::getJs).sum();
        double totalTj = selectedEntries.stream().mapToDouble(WarehouseEntry::getTj).sum();
        double totalKctj = selectedEntries.stream().mapToDouble(WarehouseEntry::getKctj).sum();
        double totalMz = selectedEntries.stream().mapToDouble(WarehouseEntry::getMz).sum();
        double totalKcjs = selectedEntries.stream().mapToDouble(WarehouseEntry::getKcjs).sum();
        String message = String.format("\u9009\u4e2d\u884c\u603b\u8ba1\uff1a\n\u4ef6\u6570\uff1a%d\n\u4f53\u79ef\uff1a%.2f\n\u5e93\u5b58\u4f53\u79ef\uff1a%.2f\n\u6bdb\u91cd\uff1a%.2f\n\u5e93\u5b58\u4ef6\u6570\uff1a%.2f", totalJs, totalTj, totalKctj, totalMz, totalKcjs);
        this.showAlert(Alert.AlertType.INFORMATION, "\u9009\u4e2d\u884c\u603b\u8ba1", message);
    }

    private void saveColumnConfiguration() {
        try {
            ColumnConfigData data = this.captureColumnConfigFromTable();
            if (data == null || data.isEmpty()) {
                this.showAlert(Alert.AlertType.INFORMATION, "\u914d\u7f6e\u4fdd\u5b58", "\u5f53\u524d\u6ca1\u6709\u53ef\u4fdd\u5b58\u7684\u5217\u914d\u7f6e\u3002");
                return;
            }
            this.saveColumnConfigData(this.currentStatusIndex, data, true);
            this.showAlert(Alert.AlertType.INFORMATION, "\u914d\u7f6e\u4fdd\u5b58", "\u72b6\u6001 \"" + AppConfig.getStatusName(this.currentStatusIndex) + "\" \u7684\u5217\u914d\u7f6e\u5df2\u4fdd\u5b58\uff08JSON \u7248\u672c\uff09\u3002");
        }
        catch (Exception e) {
            ExceptionHandler.handleException("\u4fdd\u5b58\u5217\u914d\u7f6e", e);
        }
    }

    private void loadColumnConfiguration(Runnable onComplete) {
        Platform.runLater(() -> {
            try {
                ColumnConfigData data = this.readColumnConfigFromJson(this.currentStatusIndex);
                boolean needsPersistence = false;
                if (data == null || data.isEmpty()) {
                    data = this.loadLegacyColumnConfigData(this.currentStatusIndex);
                    if (data == null || data.isEmpty()) {
                        data = this.buildDefaultColumnConfigData(this.currentStatusIndex);
                    }
                    needsPersistence = true;
                }
                this.applyColumnConfigData(data);
                this.attachColumnPreferenceListeners();
                if (needsPersistence) {
                    this.saveColumnConfigData(this.currentStatusIndex, data, false);
                }
            }
            catch (Exception e) {
                ExceptionHandler.handleException("\u52a0\u8f7d\u5217\u914d\u7f6e", e);
            }
            finally {
                this.suppressColumnConfigPersistence = false;
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private void loadColumnConfiguration() {
        this.loadColumnConfiguration(null);
    }

    private ColumnConfigData readColumnConfigFromJson(int statusIndex) {
        String key = this.buildColumnConfigJsonKey(statusIndex);
        String json = this.prefs.get(key, "");
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return ColumnConfigData.fromJson(json);
        }
        catch (Exception e) {
            System.err.println("\u89e3\u6790\u5217\u914d\u7f6eJSON\u5931\u8d25: " + e.getMessage());
            return null;
        }
    }

    private ColumnConfigData loadLegacyColumnConfigData(int statusIndex) {
        ColumnConfigData data = new ColumnConfigData();
        boolean hasLegacyData = false;
        String orderKey = "personal_column_order_" + statusIndex;
        String savedOrder = this.prefs.get(orderKey, "");
        HashMap<String, Integer> orderMap = new HashMap<String, Integer>();
        if (!savedOrder.isEmpty()) {
            hasLegacyData = true;
            String[] columnNames = savedOrder.split(",");
            for (int i = 0; i < columnNames.length; ++i) {
                orderMap.put(columnNames[i], i);
            }
        }
        int fallbackOrder = orderMap.size();
        for (TableColumn<WarehouseEntry, ?> column : this.getConfigurableColumns()) {
            String columnName = column.getText();
            ColumnConfigEntry entry = new ColumnConfigEntry();
            entry.name = columnName;
            entry.order = orderMap.getOrDefault(columnName, fallbackOrder++);
            String visibleKey = "personal_column_visible_" + statusIndex + "_" + columnName;
            String visibleRaw = this.prefs.get(visibleKey, LEGACY_SENTINEL);
            if (!LEGACY_SENTINEL.equals(visibleRaw)) {
                hasLegacyData = true;
                entry.visible = Boolean.parseBoolean(visibleRaw);
            } else {
                entry.visible = true;
            }
            String widthKey = "personal_column_width_" + statusIndex + "_" + columnName;
            String widthRaw = this.prefs.get(widthKey, LEGACY_SENTINEL);
            if (!LEGACY_SENTINEL.equals(widthRaw)) {
                hasLegacyData = true;
                try {
                    entry.width = Double.parseDouble(widthRaw);
                }
                catch (NumberFormatException ex) {
                    entry.width = this.calculateColumnWidth(columnName);
                }
            } else {
                entry.width = this.calculateColumnWidth(columnName);
            }
            data.entries.add(entry);
        }
        data.sortByOrder();
        return hasLegacyData ? data : null;
    }

    private ColumnConfigData buildDefaultColumnConfigData(int statusIndex) {
        ColumnConfigData data = new ColumnConfigData();
        AppConfig.DefaultColumnConfig defaultConfig = AppConfig.getDefaultColumnConfig(statusIndex);
        List<String> defaultNames = defaultConfig.getColumnNames();
        List<Double> defaultWidths = defaultConfig.getColumnWidths();
        List<Boolean> defaultVisible = defaultConfig.getColumnVisible();
        HashMap<String, Integer> defaultOrderMap = new HashMap<String, Integer>();
        for (int i = 0; i < defaultNames.size(); ++i) {
            defaultOrderMap.put(defaultNames.get(i), i);
        }
        int fallbackOrder = defaultNames.size();
        for (TableColumn<WarehouseEntry, ?> column : this.getConfigurableColumns()) {
            String columnName = column.getText();
            ColumnConfigEntry entry = new ColumnConfigEntry();
            entry.name = columnName;
            Integer orderIndex = (Integer)defaultOrderMap.get(columnName);
            if (orderIndex != null) {
                entry.order = orderIndex;
                entry.visible = orderIndex < defaultVisible.size() ? defaultVisible.get(orderIndex) : true;
                entry.width = orderIndex < defaultWidths.size() ? defaultWidths.get(orderIndex).doubleValue() : this.calculateColumnWidth(columnName);
            } else {
                entry.order = fallbackOrder++;
                entry.visible = true;
                entry.width = this.calculateColumnWidth(columnName);
            }
            data.entries.add(entry);
        }
        data.sortByOrder();
        return data;
    }

    private ColumnConfigData captureColumnConfigFromTable() {
        ColumnConfigData data = new ColumnConfigData();
        int order = 0;
        for (TableColumn<WarehouseEntry, ?> column : this.getConfigurableColumns()) {
            ColumnConfigEntry entry = new ColumnConfigEntry();
            entry.name = column.getText();
            entry.order = order++;
            entry.visible = column.isVisible();
            double width = column.getWidth();
            if (width <= 0.0) {
                width = column.getPrefWidth();
            }
            if (width <= 0.0) {
                width = this.calculateColumnWidth(entry.name);
            }
            entry.width = width;
            data.entries.add(entry);
        }
        return data;
    }

    private void saveColumnConfigData(int statusIndex, ColumnConfigData data, boolean logResult) {
        if (data == null || data.isEmpty()) {
            return;
        }
        this.prefs.put(this.buildColumnConfigJsonKey(statusIndex), data.toJsonString());
        this.clearLegacyColumnPreferences(statusIndex);
        if (logResult) {
            System.out.println("\u4fdd\u5b58\u5217\u914d\u7f6e JSON - \u72b6\u6001: " + AppConfig.getStatusName(statusIndex));
        }
    }

    private void clearLegacyColumnPreferences(int statusIndex) {
        try {
            String widthPrefix = "personal_column_width_" + statusIndex + "_";
            String visiblePrefix = "personal_column_visible_" + statusIndex + "_";
            String orderKey = "personal_column_order_" + statusIndex;
            for (String key : this.prefs.keys()) {
                if (!key.startsWith(widthPrefix) && !key.startsWith(visiblePrefix) && !key.equals(orderKey)) continue;
                this.prefs.remove(key);
            }
        }
        catch (BackingStoreException e) {
            System.err.println("\u6e05\u7406\u65e7\u7248\u5217\u914d\u7f6e\u5931\u8d25: " + e.getMessage());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applyColumnConfigData(ColumnConfigData data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        this.suppressColumnConfigPersistence = true;
        try {
            Map<String, ColumnConfigEntry> entryMap = data.toMap();
            List<TableColumn<WarehouseEntry, ?>> configurableColumns = this.getConfigurableColumns();
            for (TableColumn<WarehouseEntry, ?> column2 : configurableColumns) {
                ColumnConfigEntry entry = entryMap.get(column2.getText());
                if (entry != null) {
                    column2.setVisible(entry.visible);
                    if (!(entry.width > 0.0)) continue;
                    column2.setPrefWidth(entry.width);
                    continue;
                }
                column2.setVisible(true);
            }
            configurableColumns.sort(Comparator.comparingInt(column -> {
                ColumnConfigEntry entry = (ColumnConfigEntry)entryMap.get(column.getText());
                return entry != null ? entry.order : Integer.MAX_VALUE;
            }));
            this.rebuildColumnOrder(configurableColumns);
        }
        finally {
            this.suppressColumnConfigPersistence = false;
        }
    }

    private void rebuildColumnOrder(List<TableColumn<WarehouseEntry, ?>> orderedColumns) {
        TableColumn<WarehouseEntry, ?> selectionColumn = this.findSelectionColumn();
        TableColumn<WarehouseEntry, ?> operationColumn = this.findOperationColumn();
        ArrayList newOrder = new ArrayList();
        if (selectionColumn != null) {
            newOrder.add(selectionColumn);
        }
        newOrder.addAll(orderedColumns);
        if (operationColumn != null) {
            newOrder.add(operationColumn);
        }
        this.resultTableView.getColumns().setAll(newOrder);
    }

    private TableColumn<WarehouseEntry, ?> findSelectionColumn() {
        for (TableColumn tableColumn : this.resultTableView.getColumns()) {
            String name = tableColumn.getText();
            if (name != null && !name.isEmpty()) continue;
            return tableColumn;
        }
        return null;
    }

    private TableColumn<WarehouseEntry, ?> findOperationColumn() {
        for (TableColumn tableColumn : this.resultTableView.getColumns()) {
            if (!"\u64cd\u4f5c".equals(tableColumn.getText())) continue;
            return tableColumn;
        }
        return null;
    }

    private boolean isConfigurableColumn(TableColumn<WarehouseEntry, ?> column) {
        if (column == null) {
            return false;
        }
        String name = column.getText();
        return name != null && !name.isEmpty() && !"\u64cd\u4f5c".equals(name);
    }

    private List<TableColumn<WarehouseEntry, ?>> getConfigurableColumns() {
        ArrayList columns = new ArrayList();
        for (TableColumn tableColumn : this.resultTableView.getColumns()) {
            if (!this.isConfigurableColumn(tableColumn)) continue;
            columns.add(tableColumn);
        }
        return columns;
    }

    private void attachColumnPreferenceListeners() {
        for (TableColumn<WarehouseEntry, ?> column : this.getConfigurableColumns()) {
            column.visibleProperty().addListener((obs, oldVal, newVal) -> this.persistColumnConfigIfNeeded());
            column.widthProperty().addListener((obs, oldVal, newVal) -> this.persistColumnConfigIfNeeded());
        }
    }

    private void persistColumnConfigIfNeeded() {
        if (this.suppressColumnConfigPersistence) {
            return;
        }
        this.persistColumnConfigSilently();
    }

    private void persistColumnConfigSilently() {
        ColumnConfigData data = this.captureColumnConfigFromTable();
        this.saveColumnConfigData(this.currentStatusIndex, data, false);
    }

    private void attachColumnOrderListener() {
        if (this.columnOrderListenerAttached) {
            return;
        }
        this.resultTableView.getColumns().addListener(this::handleColumnOrderChanged);
        this.columnOrderListenerAttached = true;
    }

    private void handleColumnOrderChanged(ListChangeListener.Change<? extends TableColumn<WarehouseEntry, ?>> change) {
        if (this.suppressColumnConfigPersistence) {
            return;
        }
        while (change.next()) {
            if (!change.wasPermutated() && !change.wasAdded() && !change.wasRemoved() && !change.wasReplaced()) continue;
            this.persistColumnConfigSilently();
            break;
        }
    }

    private String buildColumnConfigJsonKey(int statusIndex) {
        return "column_config_json_" + statusIndex;
    }

    private Map<String, String> collectAdvancedFilters() {
        HashMap<String, String> filters = new HashMap<String, String>();
        this.putFilterValue(filters, FILTER_OWNER, this.ownerTextField);
        this.putFilterValue(filters, FILTER_ENTRY_NUMBER, this.entryNumberTextField);
        this.putFilterValue(filters, FILTER_JOB_NUMBER, this.jobNumberTextField);
        this.putFilterValue(filters, FILTER_DRIVER, this.driverTextField);
        this.putFilterValue(filters, FILTER_PLATE, this.plateTextField);
        this.putFilterValue(filters, FILTER_CARGO, this.cargoTextField);
        this.putFilterValue(filters, FILTER_MARK, this.markTextField);
        this.putFilterValue(filters, FILTER_PACKAGE, this.packageTextField);
        this.putFilterValue(filters, FILTER_DRIVER_PHONE, this.driverPhoneTextField);
        return filters;
    }

    private void putFilterValue(Map<String, String> filters, String key, TextField field) {
        if (field == null) {
            return;
        }
        String value = field.getText();
        if (value != null && !(value = value.trim()).isEmpty()) {
            filters.put(key, value);
        }
    }

    private void applyAdvancedFilters(Map<String, String> filters) {
        if (filters == null) {
            return;
        }
        this.setFieldValue(this.ownerTextField, filters.get(FILTER_OWNER));
        this.setFieldValue(this.entryNumberTextField, filters.get(FILTER_ENTRY_NUMBER));
        this.setFieldValue(this.jobNumberTextField, filters.get(FILTER_JOB_NUMBER));
        this.setFieldValue(this.driverTextField, filters.get(FILTER_DRIVER));
        this.setFieldValue(this.plateTextField, filters.get(FILTER_PLATE));
        this.setFieldValue(this.cargoTextField, filters.get(FILTER_CARGO));
        this.setFieldValue(this.markTextField, filters.get(FILTER_MARK));
        this.setFieldValue(this.packageTextField, filters.get(FILTER_PACKAGE));
        this.setFieldValue(this.driverPhoneTextField, filters.get(FILTER_DRIVER_PHONE));
    }

    private void setFieldValue(TextField field, String value) {
        if (field != null) {
            field.setText(value != null ? value : "");
        }
    }

    private void clearAdvancedFilters() {
        this.setFieldValue(this.ownerTextField, "");
        this.setFieldValue(this.jobNumberTextField, "");
        this.setFieldValue(this.entryNumberTextField, "");
        this.setFieldValue(this.driverTextField, "");
        this.setFieldValue(this.plateTextField, "");
        this.setFieldValue(this.cargoTextField, "");
        this.setFieldValue(this.markTextField, "");
        this.setFieldValue(this.packageTextField, "");
        this.setFieldValue(this.driverPhoneTextField, "");
    }

    private void saveLastAdvancedFilters(Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            this.prefs.remove("last_advanced_filters");
            return;
        }
        JSONObject json = new JSONObject(filters);
        this.prefs.put("last_advanced_filters", json.toString());
    }

    private Map<String, String> loadLastAdvancedFilters() {
        String json = this.prefs.get("last_advanced_filters", "");
        HashMap<String, String> filters = new HashMap<String, String>();
        if (json == null || json.isEmpty()) {
            return filters;
        }
        try {
            JSONObject obj = new JSONObject(json);
            obj.keys().forEachRemaining(key -> filters.put((String)key, obj.optString((String)key)));
        }
        catch (Exception e) {
            System.err.println("\u89e3\u6790\u9ad8\u7ea7\u7b5b\u9009\u504f\u597d\u5931\u8d25: " + e.getMessage());
        }
        return filters;
    }

    private void resetAdvancedFilters() {
        this.clearAdvancedFilters();
        this.applyFiltersToView();
    }

    private void restoreLastQueryInputs() {
        Map<String, String> lastAdvancedFilters;
        LocalDate storedStart;
        int savedStatus;
        if (this.jcbhTextField == null || this.statusComboBox == null) {
            return;
        }
        String lastJcbh = this.prefs.get("last_query_jcbh", "");
        if (!lastJcbh.isEmpty()) {
            this.jcbhTextField.setText(lastJcbh);
        }
        if ((savedStatus = this.prefs.getInt("last_query_status", this.currentStatusIndex)) >= 0 && savedStatus < this.statusComboBox.getItems().size()) {
            this.statusComboBox.getSelectionModel().select(savedStatus);
        }
        if ((storedStart = this.parseStoredDate(this.prefs.get("last_query_start_date", ""))) != null) {
            this.startDatePicker.setValue(storedStart);
        }
        LocalDate today = LocalDate.now();
        this.endDatePicker.setValue(today);
        if (this.startDatePicker.getValue() == null || ((LocalDate)this.startDatePicker.getValue()).isAfter(today)) {
            this.startDatePicker.setValue(today.minusDays(AppConfig.MAX_DATE_RANGE_DAYS - 1L));
        } else {
            this.validateDateRange((LocalDate)this.startDatePicker.getValue(), today);
        }
        if (!(lastAdvancedFilters = this.loadLastAdvancedFilters()).isEmpty()) {
            this.applyAdvancedFilters(lastAdvancedFilters);
        }
    }

    private void saveLastQueryInputs(String jcbh, LocalDate start, LocalDate end, int statusIndex) {
        this.prefs.put("last_query_jcbh", jcbh != null ? jcbh : "");
        this.prefs.putInt("last_query_status", statusIndex);
        this.prefs.put("last_query_start_date", start != null ? start.toString() : "");
        this.prefs.put("last_query_end_date", end != null ? end.toString() : "");
    }

    private LocalDate parseStoredDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        }
        catch (DateTimeParseException ex) {
            return null;
        }
    }

    private void updateSelectedRowsStatistics() {
        ArrayList<WarehouseEntry> selectedEntries = new ArrayList<WarehouseEntry>();
        for (WarehouseEntry entry : this.entryList) {
            if (!entry.isSelected()) continue;
            selectedEntries.add(entry);
        }
        this.selectedRowsCountLabel.setText(String.valueOf(selectedEntries.size()));
        if (selectedEntries.isEmpty()) {
            if (this.selectedTotalsBox != null) {
                this.selectedTotalsBox.setVisible(false);
                this.selectedTotalsBox.setManaged(false);
            }
            return;
        }
        if (this.selectedTotalsBox != null) {
            this.selectedTotalsBox.setVisible(true);
            this.selectedTotalsBox.setManaged(true);
        }
        int totalJs = selectedEntries.stream().mapToInt(WarehouseEntry::getJs).sum();
        double totalTj = selectedEntries.stream().mapToDouble(WarehouseEntry::getTj).sum();
        double totalKctj = selectedEntries.stream().mapToDouble(WarehouseEntry::getKctj).sum();
        double totalMz = selectedEntries.stream().mapToDouble(WarehouseEntry::getMz).sum();
        double totalKcjs = selectedEntries.stream().mapToDouble(WarehouseEntry::getKcjs).sum();
        this.selectedJsLabel.setText(String.format("%d", totalJs));
        this.selectedTjLabel.setText(String.format("%.2f", totalTj));
        this.selectedKctjLabel.setText(String.format("%.2f", totalKctj));
        this.selectedMzLabel.setText(String.format("%.2f", totalMz));
        this.selectedKcjsLabel.setText(String.format("%.2f", totalKcjs));
    }

    private void exportSelectedRows() {
        try {
            ArrayList<WarehouseEntry> selectedEntries = new ArrayList<WarehouseEntry>();
            for (WarehouseEntry entry : this.entryList) {
                if (!entry.isSelected()) continue;
                selectedEntries.add(entry);
            }
            if (selectedEntries.isEmpty()) {
                this.showAlert(Alert.AlertType.WARNING, "\u5bfc\u51fa\u63d0\u793a", "\u8bf7\u5148\u9009\u62e9\u8981\u5bfc\u51fa\u7684\u6570\u636e\u884c");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("\u5bfc\u51fa\u9009\u4e2d\u884c\u6570\u636e");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV\u6587\u4ef6 (*.csv)", "*.csv"));
            fileChooser.setInitialFileName("\u4ed3\u5e93\u5bfc\u51fa\u6570\u636e_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
            File selectedFile = fileChooser.showSaveDialog(this.resultTableView.getScene().getWindow());
            if (selectedFile == null) {
                return;
            }
            Platform.runLater(() -> {
                this.progressLabel.setText("\u6b63\u5728\u5bfc\u51fa\u6570\u636e...");
                this.progressLabel.setVisible(true);
                this.progressBar.setProgress(-1.0);
                this.progressBar.setVisible(true);
            });
            new Thread(() -> {
                try {
                    StringBuilder csvContent = new StringBuilder();
                    csvContent.append("\ufeff");
                    csvContent.append("\u8fdb\u4ed3\u7f16\u53f7,\u8fdb\u4ed3\u4f5c\u4e1a\u53f7,\u9884\u8fdb\u65e5\u671f,\u8fdb\u4ed3\u65e5\u671f,L/F,\u5305\u88c5\u89c4\u683c,\u8d27\u7269\u540d\u79f0,\u551b\u5934,\u8d27\u53f7,\u4ef6\u6570,\u4f53\u79ef,\u5e93\u5b58\u4f53\u79ef,\u6bdb\u91cd,\u6258\u6570,\u9001\u8d27\u5355\u4f4d,\u5e93\u5b58\u4ef6\u6570,\u62a5\u5173\u72b6\u6001,\u5907\u6ce8\n");
                    for (WarehouseEntry entry : selectedEntries) {
                        csvContent.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%.2f,%.2f,%.2f,%d,%s,%.2f,%s,%s\n", this.csvEscape(entry.getJcbh()), this.csvEscape(entry.getJczyh()), this.csvEscape(entry.getYjrq()), this.csvEscape(entry.getJcrq()), this.csvEscape(entry.getLf()), this.csvEscape(entry.getBzgg()), this.csvEscape(entry.getHwmc()), this.csvEscape(entry.getMt()), this.csvEscape(entry.getHh()), entry.getJs(), entry.getTj(), entry.getKctj(), entry.getMz(), entry.getTs(), this.csvEscape(entry.getShdw()), entry.getKcjs(), this.csvEscape(entry.getBgzt()), this.csvEscape(entry.getBz())));
                    }
                    Files.write(selectedFile.toPath(), csvContent.toString().getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
                    Platform.runLater(() -> {
                        this.progressBar.setVisible(false);
                        this.progressLabel.setVisible(false);
                        this.queryResultLabel.setText("\u5df2\u5bfc\u51fa " + selectedEntries.size() + " \u6761\u8bb0\u5f55\u5230: " + selectedFile.getName());
                    });
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        this.progressBar.setVisible(false);
                        this.progressLabel.setVisible(false);
                        this.showAlert(Alert.AlertType.ERROR, "\u5bfc\u51fa\u5931\u8d25", "\u5bfc\u51fa\u6570\u636e\u65f6\u53d1\u751f\u9519\u8bef\uff1a" + e.getMessage());
                    });
                }
            }).start();
        }
        catch (Exception e) {
            e.printStackTrace();
            this.showAlert(Alert.AlertType.ERROR, "\u5bfc\u51fa\u5931\u8d25", "\u542f\u52a8\u5bfc\u51fa\u8fc7\u7a0b\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if ((value = value.replace("\"", "\"\"")).contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value + "\"";
        }
        return value;
    }

    private void showColumnConfigDialog() {
        try {
            Dialog dialog = new Dialog();
            dialog.setTitle("\u5217\u663e\u793a\u8bbe\u7f6e");
            dialog.setHeaderText("\u72b6\u6001: " + AppConfig.getStatusName(this.currentStatusIndex));
            VBox content = new VBox(10.0);
            content.setPrefWidth(400.0);
            content.setPrefHeight(500.0);
            Label instructionLabel = new Label("\u52fe\u9009\u8981\u663e\u793a\u7684\u5217\uff0c\u53d6\u6d88\u52fe\u9009\u9690\u85cf\u5217\uff1a");
            instructionLabel.setStyle("-fx-font-weight: bold;");
            content.getChildren().add(instructionLabel);
            ScrollPane scrollPane = new ScrollPane();
            VBox checkBoxContainer = new VBox(5.0);
            scrollPane.setContent(checkBoxContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400.0);
            ArrayList<CheckBox> columnCheckBoxes = new ArrayList<CheckBox>();
            for (TableColumn tableColumn : this.resultTableView.getColumns()) {
                String columnName = tableColumn.getText();
                if (columnName == null || columnName.isEmpty() || columnName.equals("\u64cd\u4f5c") || columnName.equals("")) continue;
                CheckBox checkBox = new CheckBox(columnName);
                checkBox.setSelected(tableColumn.isVisible());
                checkBox.setUserData(tableColumn);
                columnCheckBoxes.add(checkBox);
                checkBoxContainer.getChildren().add(checkBox);
            }
            content.getChildren().add(scrollPane);
            HBox buttonBox = new HBox(10.0);
            Button button = new Button("\u5168\u9009");
            Button selectNoneBtn = new Button("\u5168\u4e0d\u9009");
            Button resetBtn = new Button("\u6062\u590d\u9ed8\u8ba4");
            button.setOnAction(e -> {
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
                    cb.setSelected(true);
                }
            });
            buttonBox.getChildren().addAll((Node[])new Node[]{button, selectNoneBtn, resetBtn});
            content.getChildren().add(buttonBox);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll((ButtonType[])new ButtonType[]{ButtonType.OK, ButtonType.CANCEL});
            Optional result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                for (CheckBox checkBox : columnCheckBoxes) {
                    TableColumn column = (TableColumn)checkBox.getUserData();
                    boolean shouldBeVisible = checkBox.isSelected();
                    column.setVisible(shouldBeVisible);
                    String columnName = column.getText();
                    String key = "column_visible_" + this.currentStatusIndex + "_" + columnName;
                    this.prefs.putBoolean(key, shouldBeVisible);
                }
                this.showAlert(Alert.AlertType.INFORMATION, "\u914d\u7f6e\u5df2\u4fdd\u5b58", "\u5217\u663e\u793a\u8bbe\u7f6e\u5df2\u4fdd\u5b58\u5e76\u5e94\u7528");
            }
        }
        catch (Exception e2) {
            ExceptionHandler.handleException("\u663e\u793a\u5217\u914d\u7f6e\u5bf9\u8bdd\u6846", e2);
        }
    }

    @FXML
    private void handleOpenWebVersion() {
        try {
            String jcbh = this.jcbhTextField.getText().trim();
            LocalDate startDate = (LocalDate)this.startDatePicker.getValue();
            LocalDate endDate = (LocalDate)this.endDatePicker.getValue();
            int statusIndex = this.statusComboBox.getSelectionModel().getSelectedIndex();
            StringBuilder urlBuilder = new StringBuilder("http://60.190.0.98:81/csccmisHandler/website/csccmis.aspx");
            boolean hasParams = false;
            if (!jcbh.isEmpty()) {
                String firstJcbh = jcbh.split("[,\\s]+")[0];
                urlBuilder.append(hasParams ? "&" : "?").append("jcbh=").append(URLEncoder.encode(firstJcbh, "UTF-8"));
                hasParams = true;
            }
            if (statusIndex >= 0 && statusIndex < AppConfig.STATUS_CODES.length) {
                String statusCode = AppConfig.getStatusCode(statusIndex);
                urlBuilder.append(hasParams ? "&" : "?").append("zt=").append(statusCode);
                hasParams = true;
            }
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
            System.out.println("\u6253\u5f00\u7f51\u9875\u7248URL: " + finalUrl);
            this.openWebPage(finalUrl);
            this.queryResultLabel.setText("\u5df2\u5728\u6d4f\u89c8\u5668\u4e2d\u6253\u5f00\u7f51\u9875\u7248\u67e5\u8be2");
        }
        catch (Exception e) {
            ExceptionHandler.handleException("\u6253\u5f00\u7f51\u9875\u7248", e);
            this.showAlert(Alert.AlertType.ERROR, "\u6253\u5f00\u7f51\u9875\u7248\u5931\u8d25", "\u65e0\u6cd5\u6253\u5f00\u7f51\u9875\u7248: " + e.getMessage());
        }
    }

    static {
        try {
            Class.forName("org.apache.poi.hssf.usermodel.HSSFWorkbook");
            isPOIAvailable = true;
            System.out.println("POI\u5e93\u53ef\u7528\uff0c\u5c06\u4f7f\u7528Excel\u683c\u5f0f\u5bfc\u51fa");
        }
        catch (ClassNotFoundException | NoClassDefFoundError e) {
            isPOIAvailable = false;
            System.err.println("POI\u5e93\u4e0d\u53ef\u7528\uff0c\u5c06\u4f7f\u7528CSV\u683c\u5f0f\u5bfc\u51fa: " + e.getMessage());
        }
    }

    private static class ColumnConfigData {
        int version = 1;
        final List<ColumnConfigEntry> entries = new ArrayList<ColumnConfigEntry>();

        private ColumnConfigData() {
        }

        boolean isEmpty() {
            return this.entries.isEmpty();
        }

        Map<String, ColumnConfigEntry> toMap() {
            HashMap<String, ColumnConfigEntry> map = new HashMap<String, ColumnConfigEntry>();
            for (ColumnConfigEntry entry : this.entries) {
                map.put(entry.name, entry);
            }
            return map;
        }

        void sortByOrder() {
            this.entries.sort(Comparator.comparingInt(e -> e.order));
        }

        String toJsonString() {
            JSONObject root = new JSONObject();
            root.put("version", this.version);
            JSONArray columns = new JSONArray();
            for (ColumnConfigEntry entry : this.entries) {
                JSONObject obj = new JSONObject();
                obj.put("name", entry.name);
                obj.put("width", entry.width);
                obj.put("visible", entry.visible);
                obj.put("order", entry.order);
                columns.put(obj);
            }
            root.put("columns", columns);
            return root.toString();
        }

        static ColumnConfigData fromJson(String json) {
            if (json == null || json.isEmpty()) {
                return null;
            }
            JSONObject root = new JSONObject(json);
            ColumnConfigData data = new ColumnConfigData();
            data.version = root.optInt("version", 1);
            JSONArray columns = root.optJSONArray("columns");
            if (columns != null) {
                for (int i = 0; i < columns.length(); ++i) {
                    JSONObject obj = columns.getJSONObject(i);
                    ColumnConfigEntry entry = new ColumnConfigEntry();
                    entry.name = obj.getString("name");
                    entry.width = obj.optDouble("width", 100.0);
                    entry.visible = obj.optBoolean("visible", true);
                    entry.order = obj.optInt("order", i);
                    data.entries.add(entry);
                }
            }
            data.sortByOrder();
            return data;
        }
    }

    private static class ColumnConfigEntry {
        String name;
        double width;
        boolean visible;
        int order;

        private ColumnConfigEntry() {
        }
    }
}
