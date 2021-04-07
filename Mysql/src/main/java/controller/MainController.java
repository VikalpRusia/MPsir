package controller;

import controller.serviceProvider.Services;
import createdNodes.SuggestingTextField;
import extra.ImagesLink;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import logger.ProjectLogger;
import model.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Considered normal database i.e. it is basic not customisable.
 * Refer point 1 in addDatabase method
 */
public class MainController {

    //    Database database;
    final static private Logger logger = LoggerFactory.getLogger(ProjectLogger.class);
    final private KeyCodeCombination add = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
    final private KeyCodeCombination shiftFocusDataViewToListView = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_DOWN);
    final private KeyCodeCombination showPrimaryKey = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
    final private KeyCodeCombination showDescriptionKey = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    final private KeyCodeCombination shiftFocusDataViewToQueryView = new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_DOWN);
    final private FileChooser fileChooser = new FileChooser();
    final private ObjectProperty<File> initialFolder = new SimpleObjectProperty<>();
    final private Map<String, File> fileName = new HashMap<>();
    final private StringConverter<String> stringConverter = new StringConverter<>() {
        @Override
        public String toString(String s) {
            return s;
        }

        @Override
        public String fromString(String s) {
            return s;
        }
    };
    Services.DatabaseListProvider databaseListProvider;
    Services.TableListProvider tableListProvider;
    Services.ColumnDetailsProvider columnDetailsProvider;
    Services.PrimaryKeyService primaryKeyProvider;
    Services.ForeignKeyService foreignKeyProvider;
    Services.UniqueKeyService uniqueKeyProvider;
    Services.DeleteDatabaseService deleteDatabaseProvider;
    Services.DeleteTableService deleteTableProvider;
    Services.AddDatabase createDatabase;
    Services.CreateTable createTable;
    Services.InsertData insertIntoTable;
    Services.DeleteData deleteData;
    Services.UpdateData updateData;
    Services.DescAll descAll;
    Services.PrimaryKeyValueProvider primaryKeyValueProvider;
    Services.FilterQueryProvider filterQueryProvider;
    Services.ChangeTableName changeTableName;
    Services.BackupDB backupDB;
    Services.LoadSavedDB loadDB;
    Services.ChangeTableColumnName changeColumnNameProvider;
    Services.AddColumn addColumn;
    Services.GetColumnAutoIncrement getIncrementColumn;
    Services.DeleteColumn deleteColumn;

    Map<TableColumn<ObservableList<Object>, String>, String> tableColumnName;
    Map<String, TableColumn<ObservableList<Object>, String>> nameTableColumn;
    Map<TableColumn<ObservableList<Object>, String>, Label> tableColumnLabel;
    ContextMenu contextMenu_Data_Database;
    ContextMenu contextMenuDatabase;
    ContextMenu contextMenu_Data_Table;
    ContextMenu contextMenuTable;
    ContextMenu contextMenuRow;
    ContextMenu contextMenuDataRow;
    ContextMenu contextMenuPrimary;
    ContextMenu contextMenuDataRowWithoutPrimary;
    private Service<Database.Column> lastExecuted;
    @FXML
    private ListView<String> databaseView;
    @FXML
    private ListView<String> tableView;
    @FXML
    private TableView<ObservableList<Object>> dataView;
    @FXML
    private ProgressBar dataProgressBar;
    @FXML
    private HBox progressBarContainer;
    @FXML
    private SuggestingTextField whereQuery;
    @FXML
    private CheckMenuItem autoCommit;

    public static void startService(Service<?> service) {
        logger.atDebug().addArgument(service).log("Started Service {}");
        if (service.getState() == Worker.State.READY) {
            service.start();
        } else if (service.getState() == Worker.State.SUCCEEDED
                || service.getState() == Worker.State.FAILED
        ) {
            service.restart();
        }
    }

    public void initialize() {
        //fileChooser
        logger.atInfo().log("Initialization begin");
        logger.atTrace().log("Configuring FileChooser");
        fileChooser.initialDirectoryProperty().bind(initialFolder);
        logger.atTrace().log("Initial folder of fileChooser bound");
        fileChooser.setInitialFileName("*.sql");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Mysql Database(*.sql)", "*.sql"),
                new FileChooser.ExtensionFilter("All files(*.*)", "*.*")
        );
        logger.atTrace().log("Extension Filter enabled");
        logger.atDebug().log("fileChooser configured");

        //ProgressBar
        logger.atTrace().log("Configuring Progress Bar");
        progressBarContainer.managedProperty().bind(
                progressBarContainer.visibleProperty());
        logger.atTrace().log("Progress Bar container managedProperty bound to visible Property of Progress Bar");
        progressBarContainer.visibleProperty().bind(dataProgressBar.visibleProperty());
        logger.atTrace().log("Visible Property of progress Bar container bound to Progress Bar");
        logger.atDebug().log("Progress Bar configured");

        //ServiceProvider
        logger.atTrace().log("Configuring Database List provider ");
        databaseListProvider = new Services.DatabaseListProvider();
        databaseListProvider.setOnFailed(workerStateEvent ->
                alertShow(databaseListProvider.getException()));

        databaseView.itemsProperty().bind(databaseListProvider.valueProperty());
        logger.atTrace().log("Bound database List item property to database Provider value Property");
        databaseListProvider.setOnSucceeded(workerStateEvent -> {
            databaseView.getSelectionModel().select(0);
            logger.atTrace().log("Selected first item from data fetched");
        });
        logger.atDebug().log("Database List provider configured");

        //TableList service
        logger.atTrace().log("Configuring Table List Provider");
        tableListProvider = new Services.TableListProvider();
        tableListProvider.setOnFailed(workerStateEvent ->
                alertShow(tableListProvider.getException()));

        tableView.itemsProperty().bind(tableListProvider.valueProperty());
        logger.atTrace().log("Table View item property bound to Table List Provider value Property");
        tableListProvider.setOnSucceeded(workerStateEvent -> {
            tableView.getSelectionModel().select(0);
            logger.atTrace().log("Selected first item from table data fetched");
        });
        logger.atDebug().log("Configured database provider");

        //PrimaryKey
        logger.atTrace().log("Configuring Primary Key Provider");
        primaryKeyProvider = new Services.PrimaryKeyService();
        primaryKeyProvider.setOnFailed(workerStateEvent ->
                alertShow(primaryKeyProvider.getException()));
        logger.atDebug().log("Configured Primary Key provider");

        //UniqueKey
        logger.atTrace().log("Configuring Unique Key Provider");
        uniqueKeyProvider = new Services.UniqueKeyService();
        uniqueKeyProvider.setOnFailed(workerStateEvent ->
                alertShow(uniqueKeyProvider.getException()));
        logger.atDebug().log("Configured Unique Key Provider");

        //ForeignKey
        logger.atTrace().log("Configuring Foreign Key Provider");
        foreignKeyProvider = new Services.ForeignKeyService();
        foreignKeyProvider.setOnFailed(workerStateEvent ->
                alertShow(foreignKeyProvider.getException()));
        logger.atDebug().log("Configured Foreign key Provider");

        //DeleteDatabase
        logger.atTrace().log("Configuring Delete Database Provider");
        deleteDatabaseProvider = new Services.DeleteDatabaseService();
        deleteDatabaseProvider.setOnFailed(workerStateEvent ->
                alertShow(deleteDatabaseProvider.getException()));
        logger.atDebug().log("Configured Delete Database Provider");

        //DeleteTable
        logger.atTrace().log("Configuring Delete Table Provider");
        deleteTableProvider = new Services.DeleteTableService();
        deleteTableProvider.setOnFailed(workerStateEvent ->
                alertShow(deleteTableProvider.getException()));
        logger.atDebug().log("Configured Delete Table Provider");

        //createDatabase
        logger.atTrace().log("Configuring Create Database Provider");
        createDatabase = new Services.AddDatabase();
        createDatabase.setOnFailed(workerStateEvent ->
                alertShow(createDatabase.getException()));
        logger.atDebug().log("Configured Create Database Provider");

        //createTable
        logger.atTrace().log("Configuring Create Table Provider");
        createTable = new Services.CreateTable();
        createTable.setOnFailed(workerStateEvent ->
                alertShow(createTable.getException()));
        logger.atDebug().log("Configured Create Table Provider");

        //insertDataIntoTable
        logger.atTrace().log("Configuring Insertion into Table Provider");
        insertIntoTable = new Services.InsertData();
        insertIntoTable.setOnFailed(workerStateEvent ->
                alertShow(insertIntoTable.getException()));
        logger.atDebug().log("Configured Insertion into Table Provider");

        //deleteData
        logger.atTrace().log("Configuring Delete data from Table Provider");
        deleteData = new Services.DeleteData();
        deleteData.setOnFailed(workerStateEvent ->
                alertShow(deleteData.getException()));
        logger.atDebug().log("Configured Delete data from Table Provider");

        //updateData
        logger.atTrace().log("Configuring Update Data in Table Provider");
        updateData = new Services.UpdateData();
        updateData.setOnFailed(workerStateEvent ->
                alertShow(updateData.getException()));
        logger.atDebug().log("Configured Update Data in Table Provider");

        //descAll
        logger.atTrace().log("Configuring Description Table Provider");
        descAll = new Services.DescAll();
        descAll.setOnFailed(workerStateEvent ->
                alertShow(descAll.getException()));
        logger.atDebug().log("Configured Description Table Provider");

        //primaryKeyValueProvider
        logger.atTrace().log("Configuring Update Data in Table Provider");
        primaryKeyValueProvider = new Services.PrimaryKeyValueProvider();
        primaryKeyValueProvider.setOnFailed(workerStateEvent ->
                alertShow(primaryKeyValueProvider.getException()));
        logger.atDebug().log("Configured Delete data from Table Provider");

        //whereQuery
        List<String> strings = Arrays.asList("AND", "IS", "OR", "LIKE", "NOT", "NULL", "REGEX");
        logger.atTrace().addArgument(strings).log("String for searching defined here {}");
        whereQuery.getStrings().addAll(strings);

        //filterQuery
        filterQueryProvider = new Services.FilterQueryProvider();
        dataQueryProvider(filterQueryProvider);
        logger.atDebug().log("Configured data query Provider");

        //changeTableName
        changeTableName = new Services.ChangeTableName();
        changeTableName.setOnFailed(workerStateEvent ->
                alertShow(changeTableName.getException()));
        logger.atDebug().log("Configured change Table Name Service");

        //backupDb
        backupDB = new Services.BackupDB();
        backupDB.setOnFailed(workerStateEvent ->
                alertShow(backupDB.getException()));
        logger.atDebug().log("Configured Backup Database Service");

        //loadDB
        loadDB = new Services.LoadSavedDB();
        loadDB.setOnFailed(workerStateEvent ->
                alertShow(loadDB.getException()));
        logger.atDebug().log("Configured change Table Name Service");

        //changeColumnName
        changeColumnNameProvider = new Services.ChangeTableColumnName();
        changeColumnNameProvider.setOnFailed(workerStateEvent ->
                alertShow(changeColumnNameProvider.getException()));
        logger.atDebug().log("Configured ChangeColumnName");

        //addColumn
        addColumn = new Services.AddColumn();
        addColumn.setOnFailed(workerStateEvent ->
                alertShow(addColumn.getException()));

        //IncrementColumn
        getIncrementColumn = new Services.GetColumnAutoIncrement();
        getIncrementColumn.setOnFailed(workerStateEvent ->
                alertShow(getIncrementColumn.getException()));

        //deleteData
        deleteColumn = new Services.DeleteColumn();
        deleteColumn.setOnFailed(workerStateEvent ->
                alertShow(deleteColumn.getException()));

        //columnRelated
        columnDetailsProvider = new Services.ColumnDetailsProvider();
        dataQueryProvider(columnDetailsProvider);
        logger.atDebug().log("Configured change Table Name Service");

        //Table related
        dataView.setEditable(true);
        dataView.getSelectionModel().setCellSelectionEnabled(true);
        logger.atTrace().log("Configured Data Table View to select cell");

        //Database context
        contextMenu_Data_Database = new ContextMenu();
        contextMenuDatabase = new ContextMenu();

        MenuItem addDatabase = new MenuItem("Add database");
        addDatabase.setOnAction(actionEvent -> addDatabase());

        MenuItem deleteDatabase = new MenuItem("Delete database");
        deleteDatabase.setOnAction(actionEvent -> deleteDatabaseFunction());

        SeparatorMenuItem sep = new SeparatorMenuItem();

        MenuItem loadDatabase = new MenuItem("Restore database");
        loadDatabase.setOnAction(actionEvent -> loadSnapShotDB());

        MenuItem saveAs = new MenuItem("Save As");
        saveAs.setOnAction(actionEvent ->
                changeSavingPath(databaseView.getSelectionModel().getSelectedItem()));

        MenuItem saveDatabase = new MenuItem("Backup database");
        saveDatabase.setOnAction(actionEvent -> saveSnapshotDB());

        contextMenu_Data_Database.getItems().addAll(addDatabase, deleteDatabase,
                sep, loadDatabase, saveAs, saveDatabase);
        logger.atDebug().log("Database View having item context menu set");

        MenuItem addDatabaseEmpty = new MenuItem("Add database");
        addDatabaseEmpty.setOnAction(actionEvent -> addDatabase());

        MenuItem loadDatabaseEmpty = new MenuItem("Restore database");
        loadDatabaseEmpty.setOnAction(actionEvent -> loadSnapShotDB());

        contextMenuDatabase.getItems().addAll(addDatabaseEmpty, loadDatabaseEmpty);
        logger.atDebug().log("Configured Database View having empty item");

        //table Context
        contextMenu_Data_Table = new ContextMenu();
        contextMenuTable = new ContextMenu();

        MenuItem addTable = new MenuItem("Add table");
        addTable.setOnAction(tr -> {
            try {
                addTableFunction();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        MenuItem deleteTable = new MenuItem("Delete table");
        deleteTable.setOnAction(tr -> deleteTableFunction());

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        MenuItem primaryKey = new MenuItem("Primary Key");
        primaryKey.setOnAction(s -> getPrimaryKey());

        MenuItem description = new MenuItem("Description");
        description.setOnAction(s -> descriptionTable());

        contextMenu_Data_Table.getItems().
                addAll(addTable, deleteTable, separatorMenuItem, primaryKey, description);
        logger.atDebug().log("Table View having item context menu set");

        MenuItem addTableEmpty = new MenuItem("Add table");
        addTableEmpty.setOnAction(tr -> {
            try {
                addTableFunction();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        contextMenuTable.getItems().addAll(addTableEmpty);
        logger.atDebug().log("Configured Table View having empty item");

        //table row Context
        contextMenuRow = new ContextMenu();
        contextMenuDataRow = new ContextMenu();
        contextMenuPrimary = new ContextMenu();
        contextMenuDataRowWithoutPrimary = new ContextMenu();

        MenuItem add_row = new MenuItem("Add row");
        add_row.setOnAction(e -> inputDataInTable());
        MenuItem add_column = new MenuItem("Add Column");
        add_column.setOnAction(e -> addColumn());
        SeparatorMenuItem separatorMenuItem1 = new SeparatorMenuItem();
        contextMenuRow.getItems().addAll(add_row, separatorMenuItem1, add_column);
        logger.atDebug().log("Configured Data table view having empty item");

        MenuItem add = new MenuItem("Add row");
        add.setOnAction(e -> inputDataInTable());
        MenuItem column_add = new MenuItem("Add Column");
        column_add.setOnAction(e -> addColumn());
        MenuItem deleteColumn = new MenuItem("Delete Column");
        deleteColumn.setOnAction(e -> delete_a_Column(null));
        contextMenuDataRowWithoutPrimary.getItems().addAll(add, column_add, deleteColumn);

        MenuItem add_row_Empty = new MenuItem("Add row");
        add_row_Empty.setOnAction(e -> inputDataInTable());

        MenuItem add_row_Empty1 = new MenuItem("Add row");
        add_row_Empty1.setOnAction(e -> inputDataInTable());

        MenuItem delete_column = new MenuItem("Delete Column");
        delete_column.setOnAction(e -> delete_a_Column(null));
        MenuItem delete_column1 = new MenuItem("Delete Column");
        delete_column1.setOnAction(e -> delete_a_Column(null));

        MenuItem add_null = new MenuItem("Update to null");
        add_null.setOnAction(z -> {
            @SuppressWarnings("unchecked")
            TablePosition<ObservableList<Object>, String> sample = dataView.getFocusModel().getFocusedCell();
//            System.out.println(tableColumnName.get(sample.getTableColumn()));
            updateToNull(tableColumnName.get(sample.getTableColumn()));
//            System.out.println(dataView.getFocusModel().getFocusedItem());
            ObservableList<Object> change = dataView.getFocusModel().getFocusedItem();
            change.set(sample.getColumn(), null);
            columnDetailsProvider.getValue().getColumn().set(sample.getRow(), change);
            logger.atTrace()
                    .addArgument(sample)
                    .log("Changed to null {}");
            dataView.getSelectionModel().select(sample.getRow(), sample.getTableColumn());
//            System.out.println(dataView.getFocusModel().getFocusedItem());
        });

        MenuItem delete_row = new MenuItem("Delete row");
        delete_row.setOnAction(s -> deleteData());

        MenuItem delete_row1 = new MenuItem("Delete row");
        delete_row1.setOnAction(s -> deleteData());

        contextMenuDataRow.getItems().
                addAll(add_row_Empty, add_null, delete_row, delete_column);

        contextMenuPrimary.getItems().
                addAll(add_row_Empty1, delete_row1, delete_column1);

        logger.atDebug().log("Configured Data table view having item");

        databaseView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observableValue, s, t1) -> {
                    if (t1 != null) {
                        changedDatabase();
                    }
                });

        databaseView.setCellFactory(stringListView ->
                contextFunction(contextMenu_Data_Database, contextMenuDatabase));

        tableView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observableValue, s, t1) -> changedTable(t1));

        tableView.setCellFactory(stringListView ->
                contextFunction(contextMenu_Data_Table, contextMenuTable));

        tableView.setContextMenu(contextMenuTable);
        dataView.setContextMenu(contextMenuRow);

        tableView.setOnEditCommit(stringEditEvent ->
                changeTableNameFunction(stringEditEvent.getNewValue()));

        Platform.runLater(() -> databaseView.getScene().getWindow().setOnCloseRequest(windowEvent -> {

            if (!autoCommit.isSelected()) {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.initOwner(databaseView.getScene().getWindow());
                dialog.setTitle("What do you want to do ?");
                ButtonType rollback = new ButtonType("Rollback", ButtonBar.ButtonData.NO);
                ButtonType commit = new ButtonType("Commit", ButtonBar.ButtonData.YES);
                dialog.getDialogPane().getButtonTypes().addAll(
                        rollback, commit, ButtonType.CANCEL
                );
                Node cancel = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                cancel.setVisible(false);
                Optional<ButtonType> result = dialog.showAndWait();
                System.out.println(result);
                if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
                    windowEvent.consume();
                } else if (result.get() == rollback) {
                    try {
                        Services.rollback();
                    } catch (Exception e) {
                        alertShow(e);
                    }
                } else {
                    try {
                        Services.commit();
                    } catch (Exception e) {
                        alertShow(e);
                    }
                }
            }
        }));

        logger.atInfo().log("Initialisation of controller completed");
    }

    public void setDatabase(Database database) {
        Services.setDatabase(database);
        logger.atDebug().log("Database set in Services class");
        Services.setProgressDataBar(dataProgressBar);
        logger.atTrace().log("Progress Bar set in Service class");
    }

    public void initData() {
        startService(databaseListProvider);
    }

    public void changedDatabase() {
        logger.atInfo().log("Database Selected changed");
        tableListProvider.setDatabaseName(
                databaseView.getSelectionModel().getSelectedItem());
        logger.atDebug().log("configured Table List Provider");
        startService(tableListProvider);
        whereQuery.clear();
        logger.atTrace().log("where Query cleared");
    }

    public void changedTable(String tableName) {
        logger.atInfo().log("Changed Table");
        dataView.getColumns().clear();
        if (tableName == null) {
            return;
        }
        columnDetailsProvider.setTableName(tableName);
        logger.atDebug().addArgument(columnDetailsProvider)
                .log("Configured column Details Provider {}");
        startService(columnDetailsProvider);
        whereQuery.clear();
        logger.atTrace().log("Where query cleared");
    }

    private ListCell<String> contextFunction(ContextMenu contextMenu, ContextMenu contextMenuEmpty) {
        logger.atDebug().log("Cell Factory called");
        logger.atTrace().log("Set Text for non empty fields: null");
        logger.atTrace().log("Set Editable for non empty fields: false");
        logger.atTrace().addArgument(contextMenuEmpty).log(
                "Set Context Menu for non empty fields: {}");
        logger.atTrace().log("Set Text for non empty fields: as per data");
        logger.atTrace().log("Set Editable for non empty fields: true");
        logger.atTrace().addArgument(contextMenu).log(
                "Set Context Menu for non empty fields: {}");
        return new TextFieldListCell<>(stringConverter) {
            @Override
            public void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setEditable(false);
                    setContextMenu(contextMenuEmpty);
                } else {
                    setText(s);
                    setEditable(true);
                    setContextMenu(contextMenu);
                }
            }
        };
    }

    @FXML
    public void exit() {
        databaseView.getScene().getWindow().fireEvent(
                new WindowEvent(databaseView.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    public void refresh() {
        logger.info("re-fresh mysql");
        initData();
    }

    public void deletionHandling(ListView<String> listView, BiConsumer<String, ListView<String>> consumer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(databaseView.getScene().getWindow());
        String selection = listView.getSelectionModel().getSelectedItem();
        alert.setHeaderText("You are going to delete " + selection + " !");
        alert.setContentText("Are you Sure ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            consumer.accept(selection, listView);
            logger.atInfo().addArgument(selection).log("Deleted Successfully {}");
        }
    }

    public void alertShow(Throwable e) {
        logger.atWarn().addArgument(e).log("An unexpected event happened {}");
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.initOwner(databaseView.getScene().getWindow());
        error.setTitle("Query failed!");
        error.setHeaderText("error !");
        error.setContentText(e.getMessage());
        error.showAndWait();
    }

    public void addDatabase() {
        ObservableList<String> databaseList = databaseView.getItems();
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setTitle("Database Name");
        textInputDialog.setHeaderText("Enter database Name: ");
        textInputDialog.setContentText("database cannot be empty");
        textInputDialog.initOwner(databaseView.getScene().getWindow());
        Node buttonOK = textInputDialog.getDialogPane().lookupButton(ButtonType.OK);
        buttonOK.setDisable(true);
        textInputDialog.getEditor().textProperty().addListener((observableValue, s, t1) ->
                buttonOK.setDisable(t1.trim().isEmpty()));
        textInputDialog.getEditor().setPromptText("Database Name");
        Optional<String> result = textInputDialog.showAndWait();
        if (result.isPresent() && !result.get().equals("")) {
            String x = result.get();
            createDatabase.setDatabaseName(x);
            createDatabase.setOnSucceeded(workerStateEvent -> {
                Pattern pattern = Pattern.compile("^(.*?);*$");
                Matcher matcher = pattern.matcher(x);
                String s = null;
                while (matcher.find()) {
                    s = matcher.group(1).toLowerCase();
                    logger.atInfo().addArgument(s).log("Added database");
                    databaseList.addAll(s);
                }
                databaseView.getSelectionModel().select(s);
            });
            startService(createDatabase);
        }
    }

    public void addTableFunction() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTable.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Table creation");
        dialog.initOwner(databaseView.getScene().getWindow());
        dialog.setDialogPane(loader.load());
        CreateTableController controller = loader.getController();
        controller.setTableName(tableView.getItems());
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            if (controller.getTableName().equals("") || controller.getColumnsName().isEmpty()
                    || controller.getColumnsType().isEmpty()
            ) {
                logger.warn("All fields are compulsory");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("All fields are compulsory");
                alert.setContentText("Database left unmodified\nTable Name,Table Column,Column Type\nAll are required fields");
                alert.showAndWait();
                return;
            }
            createTable.setTableName(controller.getTableName());
            createTable.setColumnsName(controller.getColumnsName());
            createTable.setColumnsType(controller.getColumnsType());
            createTable.setPrimaryKeys(controller.getPrimaryKeys());
            createTable.setUniqueKeys(controller.getUniqueKeys());
            createTable.setForeignKeys(controller.getForeignKeys());
            createTable.setUniqueTogether(controller.getUniqueTogether());
            createTable.setNotNull(controller.getNotNull());
            createTable.setAutoIncrement(controller.getAutoIncrement());
            createTable.setOnSucceeded(workerStateEvent -> {
                tableView.getItems().add(controller.getTableName());
                tableView.getSelectionModel().select(controller.getTableName());
                logger.atInfo().log("Successfully created table {}", controller.getTableName());
            });
            startService(createTable);

        }

    }

    protected Callback<TableColumn<ObservableList<Object>, String>, TableCell<ObservableList<Object>, String>> updateItem(boolean isPrimary) {
        logger.atInfo().log("Table cell set");
        return new Callback<>() {
            @Override
            public TableCell<ObservableList<Object>, String> call(TableColumn<ObservableList<Object>, String> observableListStringTableColumn) {
                return new TextFieldTableCell<>(stringConverter) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setFont(Font.font(15));
                        if (empty) {
                            setText(null);
                            setEditable(false);
                            setContextMenu(contextMenuRow);
                            setOnKeyPressed(null);
                        } else if (item == null) {
                            setText("<Null>");
                            setTextFill(Color.PURPLE);
                            setEditable(true);
                            if (dataView.isEditable()) {
                                setContextMenu(contextMenuDataRow);
                            } else {
                                setContextMenu(contextMenuDataRowWithoutPrimary);
                            }
                        } else {
                            setText(item);
                            setTextFill(Color.BLACK);
                            setEditable(true);
                            if (dataView.isEditable()) {
                                if (isPrimary) {
                                    setContextMenu(contextMenuPrimary);
                                } else {
                                    setContextMenu(contextMenuDataRow);
                                }
                            } else {
                                setContextMenu(contextMenuDataRowWithoutPrimary);
                            }
                        }
                    }
                };
            }
        };
    }

    public void inputDataInTable() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DataAddRow.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Insert data in fields");
        dialog.initOwner(databaseView.getScene().getWindow());
        try {
            dialog.setDialogPane(fxmlLoader.load());
        } catch (IOException e) {
            alertShow(e);
            return;
        }
        DataAddRowController controller = fxmlLoader.getController();
        controller.setColumnsList(nameTableColumn.keySet());
        String tableName = tableView.getSelectionModel().getSelectedItem();
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            insertIntoTable.setTableName(tableName);
            insertIntoTable.setValues(controller.values());
            insertIntoTable.setOnSucceeded(workerStateEvent -> {
                getIncrementColumn.setTableName(tableName);
                getIncrementColumn.setOnSucceeded(workerStateEvent1 -> {
                    if (getIncrementColumn.getValue() != -1) {
                        controller.values().set(getIncrementColumn.getValue() - 1, insertIntoTable.getValue());
                    }
                    columnDetailsProvider.getValue().getColumn().add(controller.values());
                    dataView.getSelectionModel().clearSelection();
                    logger.atInfo().addArgument(controller.values())
                            .addArgument(tableName).log("Successfully Inserted data {} into table {}");
                });
                startService(getIncrementColumn);
            });
            startService(insertIntoTable);

//                dataView.getSelectionModel().clearAndSelect(columnsList.getColumn().size()-1);
        }
    }

    public void getPrimaryKey() {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        primaryKeyProvider.setTableName(tableName);
        primaryKeyProvider.setOnSucceeded(workerStateEvent -> {
            logger.atInfo().addArgument(tableName).log("Primary Key fetched of {}");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            StringBuilder s = new StringBuilder();
            List<String> keys = primaryKeyProvider.getValue();
            keys.forEach(s1 -> s.append(s1).append("\n"));
            alert.initOwner(tableView.getScene().getWindow());
            alert.setHeaderText("Primary keys are !");
            alert.setContentText(s.toString());
            alert.showAndWait();
        });
        startService(primaryKeyProvider);
    }

    public void deleteData() {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        ObservableList<Object> data = dataView.getSelectionModel().getSelectedItem();
        primaryKeyProvider.setTableName(tableName);
        primaryKeyProvider.setOnSucceeded(workerStateEvent -> {
            List<String> primaryKey = primaryKeyProvider.getValue();
            primaryKeyValueProvider.setPrimaryKey(primaryKey);
            primaryKeyValueProvider.setAllColumns(dataView.getColumns());
            primaryKeyValueProvider.setNameTableColumn(nameTableColumn);
            primaryKeyValueProvider.setObjectObservableList(data);
            primaryKeyValueProvider.setOnSucceeded(workerStateEvent1 -> {
                List<String> values = primaryKeyValueProvider.getValue();
                deleteData.setTableName(tableName);
                deleteData.setValues(values);
                deleteData.setPrimaryKey(primaryKey);
                deleteData.setOnSucceeded(workerStateEvent2 -> {
                    lastExecuted.getValue().getColumn().remove(data);
                    dataView.getSelectionModel().clearSelection();
                    dataView.requestFocus();
                    logger.atInfo().log("Deleted successfully and changed focus");
                });
                startService(deleteData);
            });
            startService(primaryKeyValueProvider);

        });
        startService(primaryKeyProvider);

    }

    private void updateData(String newValue, String columnModified) {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        ObservableList<Object> data = dataView.getSelectionModel().getSelectedItem();
        primaryKeyProvider.setTableName(tableName);
        primaryKeyProvider.setOnSucceeded(workerStateEvent -> {
            List<String> primaryKey = primaryKeyProvider.getValue();
            primaryKeyValueProvider.setPrimaryKey(primaryKey);
            primaryKeyValueProvider.setAllColumns(dataView.getColumns());
            primaryKeyValueProvider.setNameTableColumn(nameTableColumn);
            primaryKeyValueProvider.setObjectObservableList(data);
            primaryKeyValueProvider.setOnSucceeded(workerStateEvent1 -> {
                List<String> values = primaryKeyValueProvider.getValue();
                updateData.setTableName(tableName);
                updateData.setColumnModified(columnModified);
                updateData.setNewValue(newValue);
                updateData.setValues(values);
                updateData.setPrimaryKey(primaryKey);
                startService(updateData);
                logger.atInfo().log("Updated data");
            });
            startService(primaryKeyValueProvider);
        });
        startService(primaryKeyProvider);
    }

    public void description() {
        Stage stage = new Stage();
        stage.initOwner(databaseView.getScene().getWindow());
        stage.getIcons().add(
                new Image(ImagesLink.icon)
        );
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/about.fxml"));
            logger.atInfo().log("Description of application loaded");
            stage.setWidth(600);
            stage.setHeight(600);
            stage.setTitle("Descriptions !");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            alertShow(e);
        }

    }

    public void descriptionTable() {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        Stage stage = new Stage();
        stage.initOwner(databaseView.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/descriptionTable.fxml"));
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.sizeToScene();
            stage.setTitle(tableName + " description");
            stage.setResizable(false);

            DescriptionTableController descController = fxmlLoader.getController();
            descAll.setOnSucceeded(workerStateEvent -> {
                descController.setData(descAll.getValue());
                stage.showAndWait();
            });
            logger.atInfo().log("displayed description of a table");
            descAll.setTableName(tableName);
            startService(descAll);
        } catch (IOException e) {
            alertShow(e);
        }


    }

    public void updateToNull(String columnModified) {
        updateData(null, columnModified);
        logger.atInfo().log("Value updated to null");
    }

    //deleteDatabase
    public void deleteDatabaseFunction() {
        BiConsumer<String, ListView<String>> s = (s1, listView) -> {
            deleteDatabaseProvider.setDatabaseName(s1);
            logger.atInfo().log("Attempting to delete database");
            deleteDatabaseProvider.setOnSucceeded(workerStateEvent ->
                    listView.getItems().removeAll(s1));
            startService(deleteDatabaseProvider);

        };
        deletionHandling(databaseView, s);
        if (databaseView.getItems().size() == 0) {
            tableView.getItems().clear();
            dataView.getColumns().clear();
        }
    }

    //KeyEvent delete/drop database
    @FXML
    public void handleKeyPressedOnDatabase(KeyEvent e) {
        if (e.getCode().equals(KeyCode.DELETE)) {
            logger.atDebug().log("DELETE KEY pressed on database");
            deleteDatabaseFunction();
        } else if (add.match(e)) {
            addDatabase();
        }
    }

    //Delete Table
    public void deleteTableFunction() {
        BiConsumer<String, ListView<String>> consumer = (s, listView) -> {
            deleteTableProvider.setTableName(s);
            logger.atInfo().log("Attempting deletion of table");
            deleteTableProvider.setOnSucceeded(workerStateEvent ->
                    listView.getItems().removeAll(s));
            startService(deleteTableProvider);

        };
        deletionHandling(tableView, consumer);
        if (tableView.getItems().size() == 0) {
            dataView.getColumns().clear();
        }
    }

    //KeyEvent delete/drop table
    @FXML
    public void handleKeyPressedOnTable(KeyEvent e) {
        try {
            if (e.getCode().equals(KeyCode.DELETE)) {
                logger.atDebug().log("DELETE KEY pressed on Table");
                deleteDatabaseFunction();
            } else if (add.match(e)) {
                logger.atDebug().log("CTRL + A key pressed on Table");
                addTableFunction();
            } else if (showPrimaryKey.match(e)) {
                logger.atDebug().log("CTRL + P key pressed on Table");
                getPrimaryKey();
            } else if (showDescriptionKey.match(e)) {
                logger.atDebug().log("CTRL + D key pressed on Table");
                descriptionTable();
            }
        } catch (IOException exception) {
            alertShow(exception);
        }
    }

    //KeyEvent delete/del data
    @FXML
    public void handleKeyPressedOnData(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE)) {
            logger.atInfo().log("Pressed CTRL + DEL on Data");
            ObservableList<Object> selectedItem = dataView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                deleteData();
            }
            logger.atWarn().log("No item selected");
        } else if (add.match(keyEvent)) {
            inputDataInTable();
        } else if (shiftFocusDataViewToListView.match(keyEvent)) {
            tableView.requestFocus();
        } else if (shiftFocusDataViewToQueryView.match(keyEvent)) {
            whereQuery.requestFocus();
        }
    }

    //MouseEvent handle on Table
    @FXML
    public void handleMouseClickedEventOnTable(MouseEvent e) {
        if (e.getClickCount() == 3) {
            logger.atDebug().log("Clicked 3 times");
            descriptionTable();
            return;
        }
        logger.atWarn().log("Unknown event");
    }

    public void tableColumnArrangement(Database.Column columnsList,
                                       List<String> primaryKey, List<String> uniqueKey, List<String> foreignKey
    ) {
        dataView.getColumns().clear();
        tableColumnName = new HashMap<>();
        nameTableColumn = new LinkedHashMap<>();
        tableColumnLabel = new HashMap<>();
        int z = 0;
        int h = 0;
        int k = 0;

        for (int i = 0; i < columnsList.getHsize(); i++) {
            TableColumn<ObservableList<Object>, String> tableColumn = new TableColumn<>();
            int finalI = i;
            tableColumn.setCellValueFactory(
                    observableListStringCellDataFeatures -> {
                        Object d = observableListStringCellDataFeatures.getValue().get(finalI);
                        if (d == null) {
                            return null;
                        }
                        return new SimpleStringProperty(
                                d.toString());
                    });
            HBox hBox = new HBox(10);
            Label heading = new Label(columnsList.getHeading().get(i));
            Label dataType = new Label(columnsList.getType().get(i));
            if (primaryKey != null &&
                    z < primaryKey.size() &&
                    columnsList.getHeading().get(i).equals(primaryKey.get(z))) {
                z++;
                ImageView img = new ImageView(new Image(ImagesLink.primaryKeyIcon));
                img.setFitWidth(15);
                img.setFitHeight(15);
                img.getStyleClass().add("primaryKey");
                hBox.getChildren().add(img);
                configureTableHeading(tableColumn, hBox, heading, dataType, true);
            } else {
                configureTableHeading(tableColumn, hBox, heading, dataType, false);
                if (foreignKey != null &&
                        h < foreignKey.size() &&
                        columnsList.getHeading().get(i).equals(foreignKey.get(h))
                ) {
                    h++;
                    ImageView img = new ImageView(new Image(ImagesLink.foreignKeyIcon));
                    img.setFitWidth(15);
                    img.setFitHeight(15);
                    img.getStyleClass().add("primaryKey");
                    hBox.getChildren().add(img);
                } else if (uniqueKey != null &&
                        k < uniqueKey.size() &&
                        columnsList.getHeading().get(i).equals(uniqueKey.get(k))) {
                    k++;
                    ImageView img = new ImageView(new Image(ImagesLink.uniqueKeyIcon));
                    img.setFitWidth(15);
                    img.setFitHeight(15);
                    img.getStyleClass().add("primaryKey");
                    hBox.getChildren().add(img);
                }
            }
            tableColumn.setReorderable(false);

            tableColumnLabel.put(tableColumn, heading);
            logger.debug("Configuring currently {}", columnsList.getHeading().get(i));
            //for columnName suggestions
            whereQuery.getStrings().add(columnsList.getHeading().get(i).toLowerCase());
            dataView.setEditable(primaryKey != null && primaryKey.size() != 0);
            tableColumnName.put(tableColumn, columnsList.getHeading().get(i));
            nameTableColumn.put(columnsList.getHeading().get(i), tableColumn);
            logger.atInfo().log("configuring table view via adding table column");

        }
        dataView.setItems(columnsList.getColumn());
        dataView.getSelectionModel().select(0, dataView.getColumns().get(0));
    }

    @FXML
    public void handleKeyPressedOnSuggestion(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            logger.atDebug().log("Enter pressed on Suggesting field");
            whereClause();
        }
    }

    public void whereClause() {
        logger.atInfo().log("Filtering data");
        if (!whereQuery.getText().isEmpty()) {
            filterQueryProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
            filterQueryProvider.setWhereQuery(whereQuery.getText());
            startService(filterQueryProvider);
        } else {
            changedTable(tableView.getSelectionModel().getSelectedItem());
        }
    }

    public void changeTableNameFunction(String newTableName) {
        logger.atInfo().log("Changing tableName");
        changeTableName.setOldTableName(tableView.getSelectionModel().getSelectedItem());
        changeTableName.setNewTableName(newTableName);
        changeTableName.setOnSucceeded(workerStateEvent ->
                tableView.getItems().set(tableView.getSelectionModel().getSelectedIndex(),
                        newTableName));
        startService(changeTableName);
    }

    public void saveSnapshotDB() {
        logger.atInfo().log("Database saving...");
        String databaseName = databaseView.getSelectionModel().getSelectedItem();
        if (fileName.getOrDefault(databaseName, null) == null) {
            boolean result = changeSavingPath(databaseName);
            if (!result) {
                return;
            }
        }
        System.out.println(fileName.get(databaseName).toString());
        backupDB.setDatabaseName(databaseName);
        backupDB.setToBeSavedAt(fileName.get(databaseName));
        startService(backupDB);
        logger.atInfo().log("Database saved");
    }

    public boolean changeSavingPath(String databaseName) {
        File savingFile = fileChooser.showSaveDialog(databaseView.getScene().getWindow());
        if (savingFile != null) {
            initialFolder.set(savingFile.getParentFile());
            fileName.put(databaseName, savingFile);
            logger.atDebug().addArgument(savingFile).log("Saving at {}");
            return true;
        }
        return false;
    }

    public void loadSnapShotDB() {
        File loadingFile = fileChooser.showOpenDialog(databaseView.getScene().getWindow());
        if (loadingFile != null) {
            initialFolder.set(loadingFile.getParentFile());
            loadDB.setToBeLoaded(loadingFile);

            TextInputDialog textInputDialog = new TextInputDialog();
            textInputDialog.initOwner(databaseView.getScene().getWindow());
            textInputDialog.setTitle("Database Name");
            textInputDialog.setHeaderText("Enter Database Name: ");
            textInputDialog.setContentText("Database name should be uncommon");
            Optional<String> dbName = textInputDialog.showAndWait();
            if (dbName.isPresent()) {
                startService(loadDB);
                fileName.put(loadingFile.getName(), loadingFile);
                Pattern pattern = Pattern.compile("^(.*?);*$");
                Matcher matcher = pattern.matcher(dbName.get());
                while (matcher.find()) {
                    String s = matcher.group(1).toLowerCase();
                    loadDB.setDatabaseName(s);
                    loadDB.setOnSucceeded(workerStateEvent -> {
                        databaseView.getItems().addAll(s);
                        databaseView.getSelectionModel().select(s);
                    });
                }
                logger.atInfo().addArgument(loadingFile).log("Loading DB from {}");
            }
        }
    }

    @FXML
    public void handleMouseEventTableView(MouseEvent e) {
        if (e.getTarget() instanceof TableColumnHeader && e.getClickCount() == 2) {
            changeColumnName(e);
        }
    }

    private void changeColumnName(Event e) {
        @SuppressWarnings("all")//needs to be more precise
        Label toBeRenamed = tableColumnLabel.get(((TableColumnHeader) e.getTarget()).getTableColumn());
        String tableName = tableView.getSelectionModel().getSelectedItem();
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("Changing Column Name ");
        textInputDialog.setContentText("Enter new Column Name for " + toBeRenamed.getText());
        Optional<String> result = textInputDialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            changeColumnNameProvider.setTableName(tableName);
            changeColumnNameProvider.setOldColumnName(toBeRenamed.getText());
            changeColumnNameProvider.setNewColumnName(result.get());
            logger.atInfo().log("Change Column Name {} to {} of table Name {}", toBeRenamed.getText(), result.get(), tableName);
            changeColumnNameProvider.setOnSucceeded(workerStateEvent -> {
                toBeRenamed.setText(result.get());
                dataView.getSortOrder().clear();
            });
            startService(changeColumnNameProvider);
        }
    }

    public void addColumn() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/fxml/modifyTableAddColumn.fxml"));
        String tableName = tableView.getSelectionModel().getSelectedItem();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(dataView.getScene().getWindow());
        dialog.setTitle("Add column");
        try {
            dialog.setDialogPane(loader.load());
            ModifyTableAddColumn controller = loader.getController();
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() &&
                    result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (controller.getColumnName().isEmpty() || controller.getColumnType() == null ||
                        controller.getColumnType().isEmpty()) {
                    alertShow(new Exception("All fields are necessary"));
                    return;
                }
                addColumn.setTableName(tableName);
                addColumn.setDataType(controller.getColumnType());
                addColumn.setAutoIncrement(controller.getAutoIncrement());
                addColumn.setNotNull(controller.getNotNull());
                addColumn.setPrimaryKey(controller.getPrimaryKey());
                addColumn.setNewColumn(controller.getColumnName());
                addColumn.setColumn(columnDetailsProvider.getValue());
                addColumn.setUnique(controller.getUnique());
                addColumn.setOnSucceeded(workerStateEvent -> {
                    TableColumn<ObservableList<Object>, String> tableColumn = new TableColumn<>();
                    final int y = nameTableColumn.size();
                    tableColumn.setCellValueFactory(
                            observableListStringCellDataFeatures -> {
                                Object d;
                                try {
                                    d = observableListStringCellDataFeatures.getValue().get(y);
                                } catch (IndexOutOfBoundsException e) {
                                    logger.error("Error happened although not serious one");
                                    d = null;
                                }
                                if (d == null) {
                                    return null;
                                }
                                return new SimpleStringProperty(
                                        d.toString());
                            });
                    HBox hBox = new HBox(10);

                    if (controller.getPrimaryKey()) {
                        ImageView img = new ImageView(new Image(ImagesLink.primaryKeyIcon));
                        img.setFitWidth(15);
                        img.setFitHeight(15);
                        img.getStyleClass().add("primaryKey");
                        hBox.getChildren().add(img);
                    } else if (controller.getUnique()) {
                        ImageView img = new ImageView(new Image(ImagesLink.uniqueKeyIcon));
                        img.setFitWidth(15);
                        img.setFitHeight(15);
                        img.getStyleClass().add("primaryKey");
                        hBox.getChildren().add(img);
                    }
                    Label heading = new Label(controller.getColumnName());
                    tableColumnLabel.put(tableColumn, heading);
                    logger.debug("Configuring currently {}", controller.getColumnName());
                    //for columnName suggestions
                    whereQuery.getStrings().add(controller.getColumnName());
                    Label dataType = new Label(controller.getColumnType());
                    configureTableHeading(tableColumn, hBox, heading, dataType, controller.getPrimaryKey());
                    if (controller.getPrimaryKey()) {
                        dataView.setEditable(true);
                    }
                    tableColumnName.put(tableColumn, controller.getColumnName());
                    nameTableColumn.put(controller.getColumnName(), tableColumn);
                    logger.atInfo().log("configuring table view via adding table column");

                });
                startService(addColumn);
            }
        } catch (IOException exception) {
            alertShow(exception);
        }
    }

    @SuppressWarnings("rawtypes")
    public void delete_a_Column(String columnName) {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        if (columnName == null) {
            TablePosition y = dataView.getFocusModel().getFocusedCell();
            columnName = tableColumnName.get(dataView.getColumns().get(y.getColumn()));
        }
        deleteColumn.setTableName(tableName);
        deleteColumn.setColumnName(columnName);
        deleteColumn.setColumn(columnDetailsProvider.getValue());
        deleteColumn.setZ(dataView.getColumns().indexOf(nameTableColumn.get(columnName)));
        String finalColumnName = columnName;
        deleteColumn.setOnSucceeded(workerStateEvent -> {
            dataView.getColumns().remove(nameTableColumn.get(finalColumnName));
            nameTableColumn.remove(finalColumnName);

        });
        startService(deleteColumn);
    }

    public void changeAutoCommit() {
        try {
            Services.setAutoCommit(autoCommit.isSelected());
        } catch (Exception e) {
            alertShow(e);
        }
    }

    private void configureTableHeading(TableColumn<ObservableList<Object>, String> tableColumn, HBox hBox, Label heading, Label dataType, boolean isPrimary) {
        VBox textContainer = new VBox(heading, dataType);
        hBox.getChildren().addAll(textContainer);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setMaxWidth(HBox.USE_PREF_SIZE);
        hBox.setMinWidth(HBox.USE_PREF_SIZE);
        tableColumn.setGraphic(hBox);
        hBox.widthProperty().addListener((observableValue, number, t1) ->
                tableColumn.setPrefWidth(hBox.prefWidth(-1) + 10));
        tableColumn.setCellFactory(updateItem(isPrimary));
        tableColumn.setOnEditCommit(t -> {
            updateData.setOnSucceeded(workerStateEvent2 -> {
                TablePosition<ObservableList<Object>, String> tablePosition = t.getTablePosition();
//                System.out.println(tablePosition.getColumn());
                try {
                    t.getRowValue().set(tablePosition.getColumn(), t.getNewValue());
                } catch (IndexOutOfBoundsException e) {
                    t.getRowValue().add(t.getNewValue());
                }
                dataView.requestFocus();
            });
            updateData(t.getNewValue(), tableColumnName.get(t.getTableColumn()));
        });
        ContextMenu contextMenuHeader = new ContextMenu();
        MenuItem add = new MenuItem("Add row");
        add.setOnAction(e -> inputDataInTable());
        MenuItem column_add = new MenuItem("Add Column");
        column_add.setOnAction(e -> addColumn());
        MenuItem deleteColumn = new MenuItem("Delete Column");
        deleteColumn.setOnAction(e -> delete_a_Column(heading.getText()));
        contextMenuHeader.getItems().addAll(add, column_add, deleteColumn);
        tableColumn.setContextMenu(contextMenuHeader);
        dataView.getColumns().add(tableColumn);
    }

    private void dataQueryProvider(Service<Database.Column> queryProvider) {
        queryProvider.setOnFailed(workerStateEvent ->
                alertShow(queryProvider.getException()));

        queryProvider.setOnSucceeded(workerStateEvent -> {
            lastExecuted = queryProvider;
            primaryKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());

            primaryKeyProvider.setOnSucceeded(workerStateEvent1 -> {
                uniqueKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
                uniqueKeyProvider.setOnSucceeded(workerStateEvent2 -> {
                    foreignKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
                    foreignKeyProvider.setOnSucceeded(workerStateEvent3 -> {
                        tableColumnArrangement(
                                queryProvider.getValue(), primaryKeyProvider.getValue(),
                                uniqueKeyProvider.getValue(), foreignKeyProvider.getValue()
                        );
                        logger.atInfo().log("fetched unique key and primary key and foreign key");
                    });
                    startService(foreignKeyProvider);
                });
                startService(uniqueKeyProvider);
            });
            startService(primaryKeyProvider);
        });
    }
}