package controller;

import controller.serviceProvider.Services;
import createdNodes.SuggestingTextField;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
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
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.Database;

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
public class Controller {

    //    Database database;

    final KeyCodeCombination add = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
    final KeyCodeCombination shiftFocusDataViewToListView = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_DOWN);
    final KeyCodeCombination showPrimaryKey = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
    final KeyCodeCombination showDescriptionKey = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    final KeyCodeCombination shiftFocusDataViewToQueryView = new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_DOWN);
    final FileChooser fileChooser = new FileChooser();
    final ObjectProperty<File> initialFolder = new SimpleObjectProperty<>();
    final Map<String, File> fileName = new HashMap<>();
    private final StringConverter<String> stringConverter = new StringConverter<>() {
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
    Services.KeyService primaryKeyProvider;
    Services.KeyService foreignKeyProvider;
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

    Map<TableColumn<ObservableList<Object>, String>, String> tableColumnName;
    Map<String, TableColumn<ObservableList<Object>, String>> nameTableColumn;

    ContextMenu contextMenu_Data_Database;
    ContextMenu contextMenuDatabase;
    ContextMenu contextMenu_Data_Table;
    ContextMenu contextMenuTable;
    ContextMenu contextMenuRow;
    ContextMenu contextMenuDataRow;

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

    public static void startService(Service<?> service) {
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
        fileChooser.initialDirectoryProperty().bind(initialFolder);
        fileChooser.setInitialFileName("*.sql");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Mysql Database(*.sql)", "*.sql"),
                new FileChooser.ExtensionFilter("All files(*.*)", "*.*")
        );

        //ProgressBar
        progressBarContainer.managedProperty().bind(
                progressBarContainer.visibleProperty());
        progressBarContainer.visibleProperty().bind(dataProgressBar.visibleProperty());

        //ServiceProvider
        databaseListProvider = new Services.DatabaseListProvider();
        databaseListProvider.setOnFailed(workerStateEvent ->
                alertShow(databaseListProvider.getException()));

        databaseView.itemsProperty().bind(databaseListProvider.valueProperty());
        databaseListProvider.setOnSucceeded(workerStateEvent ->
                databaseView.getSelectionModel().select(0));

        //TableList service
        tableListProvider = new Services.TableListProvider();
        tableListProvider.setOnFailed(workerStateEvent ->
                alertShow(databaseListProvider.getException()));

        tableView.itemsProperty().bind(tableListProvider.valueProperty());
        tableListProvider.setOnSucceeded(workerStateEvent ->
                tableView.getSelectionModel().select(0));

        //PrimaryKey
        primaryKeyProvider = new Services.KeyService();
        primaryKeyProvider.setPrimaryOrForeign(true);
        primaryKeyProvider.setOnFailed(workerStateEvent ->
                alertShow(primaryKeyProvider.getException()));

        //ForeignKey
        foreignKeyProvider = new Services.KeyService();
        foreignKeyProvider.setPrimaryOrForeign(false);
        foreignKeyProvider.setOnFailed(workerStateEvent ->
                alertShow(foreignKeyProvider.getException()));

        //DeleteDatabase
        deleteDatabaseProvider = new Services.DeleteDatabaseService();
        deleteDatabaseProvider.setOnFailed(workerStateEvent ->
                alertShow(deleteDatabaseProvider.getException()));

        //DeleteTable
        deleteTableProvider = new Services.DeleteTableService();
        deleteTableProvider.setOnFailed(workerStateEvent ->
                alertShow(deleteTableProvider.getException()));

        //createDatabase
        createDatabase = new Services.AddDatabase();
        createDatabase.setOnFailed(workerStateEvent ->
                alertShow(createDatabase.getException()));

        //createTable
        createTable = new Services.CreateTable();
        createTable.setOnFailed(workerStateEvent ->
                alertShow(createTable.getException()));

        //insertDataIntoTable
        insertIntoTable = new Services.InsertData();
        insertIntoTable.setOnFailed(workerStateEvent ->
                alertShow(insertIntoTable.getException()));

        //deleteData
        deleteData = new Services.DeleteData();
        deleteData.setOnFailed(workerStateEvent ->
                alertShow(deleteData.getException()));

        //updateData
        updateData = new Services.UpdateData();
        updateData.setOnFailed(workerStateEvent ->
                alertShow(updateData.getException()));

        //descAll
        descAll = new Services.DescAll();
        descAll.setOnFailed(workerStateEvent ->
                alertShow(descAll.getException()));

        //primaryKeyValueProvider
        primaryKeyValueProvider = new Services.PrimaryKeyValueProvider();
        primaryKeyValueProvider.setOnFailed(workerStateEvent ->
                alertShow(primaryKeyValueProvider.getException()));

        //whereQuery
        List<String> strings = Arrays.asList("AND", "IS", "OR", "LIKE", "NOT", "NULL", "REGEX");
        whereQuery.getStrings().addAll(strings);

        //filterQuery
        filterQueryProvider = new Services.FilterQueryProvider();
        dataQueryProvider(filterQueryProvider);
//        filterQueryProvider.setOnFailed(workerStateEvent ->
//                alertShow(filterQueryProvider.getException()));
//
//        filterQueryProvider.setOnSucceeded(workerStateEvent -> {
//            primaryKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
//
//            primaryKeyProvider.setOnSucceeded(workerStateEvent1 ->{
//                foreignKeyProvider.setOnSucceeded(workerStateEvent2 ->
//                    tableColumnArrangement(filterQueryProvider, primaryKeyProvider.getValue(),foreignKeyProvider.getValue()));
//                startService(foreignKeyProvider);
//            });
//            startService(primaryKeyProvider);
//        });

        //changeTableName
        changeTableName = new Services.ChangeTableName();
        changeTableName.setOnFailed(workerStateEvent ->
                alertShow(changeTableName.getException()));

        //backupDb
        backupDB = new Services.BackupDB();
        backupDB.setOnFailed(workerStateEvent ->
                alertShow(backupDB.getException()));

        //loadDB
        loadDB = new Services.LoadSavedDB();
        loadDB.setOnFailed(workerStateEvent ->
                alertShow(loadDB.getException()));


        //columnRelated
        columnDetailsProvider = new Services.ColumnDetailsProvider();
        dataQueryProvider(columnDetailsProvider);
//        columnDetailsProvider.setOnFailed(workerStateEvent ->
//                alertShow(columnDetailsProvider.getException()));
//
//        columnDetailsProvider.setOnSucceeded(workerStateEvent -> {
//            primaryKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
//
//            primaryKeyProvider.setOnSucceeded(workerStateEvent1 ->{
//                foreignKeyProvider.setOnSucceeded(workerStateEvent2 ->
//                        tableColumnArrangement(filterQueryProvider, primaryKeyProvider.getValue(),foreignKeyProvider.getValue()));
//                startService(foreignKeyProvider);
//            });
//            startService(primaryKeyProvider);
//
//        });
        //Table related
        dataView.setEditable(true);
        dataView.getSelectionModel().setCellSelectionEnabled(true);

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

        MenuItem addDatabaseEmpty = new MenuItem("Add database");
        addDatabaseEmpty.setOnAction(actionEvent -> addDatabase());

        MenuItem loadDatabaseEmpty = new MenuItem("Restore database");
        loadDatabaseEmpty.setOnAction(actionEvent -> loadSnapShotDB());

        contextMenuDatabase.getItems().addAll(addDatabaseEmpty, loadDatabaseEmpty);

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

        MenuItem addTableEmpty = new MenuItem("Add table");
        addTableEmpty.setOnAction(tr -> {
            try {
                addTableFunction();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        contextMenuTable.getItems().addAll(addTableEmpty);

        //table row Context
        contextMenuRow = new ContextMenu();
        contextMenuDataRow = new ContextMenu();

        MenuItem add_row = new MenuItem("Add row");
        add_row.setOnAction(e -> inputDataInTable());
        contextMenuRow.getItems().addAll(add_row);


        MenuItem add_row_Empty = new MenuItem("Add row");
        add_row_Empty.setOnAction(e -> inputDataInTable());

        MenuItem add_null = new MenuItem("Update to null");
        add_null.setOnAction(z -> {
            TablePosition<ObservableList<Object>, String> sample = dataView.getFocusModel().getFocusedCell();
//            System.out.println(tableColumnName.get(sample.getTableColumn()));
            updateToNull(tableColumnName.get(sample.getTableColumn()));
//            System.out.println(dataView.getFocusModel().getFocusedItem());
            ObservableList<Object> change = dataView.getFocusModel().getFocusedItem();
            change.set(sample.getColumn(), null);
            columnDetailsProvider.getValue().getColumn().set(sample.getRow(), change);
            dataView.getSelectionModel().select(sample.getRow(), sample.getTableColumn());
//            System.out.println(dataView.getFocusModel().getFocusedItem());
        });

        MenuItem delete_row = new MenuItem("Delete row");
        delete_row.setOnAction(s -> deleteData());

        SeparatorMenuItem separatorMenuItem1 = new SeparatorMenuItem();

        MenuItem searchField = new MenuItem("Search Field");
        contextMenuDataRow.getItems().
                addAll(add_row_Empty, add_null, delete_row, separatorMenuItem1, searchField);

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
    }

    public void setDatabase(Database database) {
        Services.setDatabase(database);
        Services.setProgressDataBar(dataProgressBar);
    }

    public void initData() {
        startService(databaseListProvider);
    }

    public void changedDatabase() {
        tableListProvider.setDatabaseName(
                databaseView.getSelectionModel().getSelectedItem());
        startService(tableListProvider);
        whereQuery.clear();
    }

    public void changedTable(String tableName) {
        dataView.getColumns().clear();
        if (tableName == null) {
            return;
        }
        columnDetailsProvider.setTableName(tableName);
        columnDetailsProvider.setProgressBar(dataProgressBar);
        startService(columnDetailsProvider);
        whereQuery.clear();
    }

    private ListCell<String> contextFunction(ContextMenu contextMenu, ContextMenu contextMenuEmpty) {
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
        Platform.exit();
    }

    @FXML
    public void refresh() {
        initData();
    }

    public void deletionHandling(ListView<String> listView, BiConsumer<String, ListView<String>> consumer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(databaseView.getScene().getWindow());
        alert.setHeaderText("You are going to delete " + listView.getSelectionModel().getSelectedItem() + " !");
        alert.setContentText("Are you Sure ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String Name = listView.getSelectionModel().getSelectedItem();
            consumer.accept(Name, listView);
        }
    }

    public void alertShow(Throwable e) {
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
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (controller.getTableName().equals("") || controller.getColumnsName().isEmpty()
                    || controller.getColumnsType().isEmpty()
            ) {
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
            createTable.setForeignKeys(controller.getForeignKeys());
            createTable.setOnSucceeded(workerStateEvent -> {
                tableView.getItems().add(controller.getTableName());
                tableView.getSelectionModel().select(controller.getTableName());
            });
            startService(createTable);

        }

    }

    protected Callback<TableColumn<ObservableList<Object>, String>, TableCell<ObservableList<Object>, String>> updateItem() {
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
                            setContextMenu(contextMenuDataRow);
                        } else {
                            setText(item);
                            setTextFill(Color.BLACK);
                            setEditable(true);
                            setContextMenu(contextMenuDataRow);
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
        }
        DataAddRowController controller = fxmlLoader.getController();
        controller.setColumnsList(columnDetailsProvider.getValue());
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            insertIntoTable.setTableName(tableView.getSelectionModel().getSelectedItem());
            insertIntoTable.setValues(controller.values());
            insertIntoTable.setOnSucceeded(workerStateEvent -> {
                columnDetailsProvider.getValue().getColumn().add(controller.values());
                dataView.getSelectionModel().clearSelection();
            });
            startService(insertIntoTable);

//                dataView.getSelectionModel().clearAndSelect(columnsList.getColumn().size()-1);
        }
    }

    public void getPrimaryKey() {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        primaryKeyProvider.setTableName(tableName);
        primaryKeyProvider.setOnSucceeded(workerStateEvent -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            StringBuilder s = new StringBuilder();
            List<String> keys = primaryKeyProvider.getValue();
            keys.forEach(s1 -> s.append(s1).append("\n"));
            alert.setHeaderText("Primary keys are !");
            alert.setContentText(s.toString());
            alert.showAndWait();
        });
        startService(primaryKeyProvider);
    }

    public void deleteData() {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        primaryKeyProvider.setTableName(tableName);
        primaryKeyProvider.setOnSucceeded(workerStateEvent -> {
            List<String> primaryKey = primaryKeyProvider.getValue();
            primaryKeyValueProvider.setPrimaryKey(primaryKey);
            primaryKeyValueProvider.setAllColumns(dataView.getColumns());
            primaryKeyValueProvider.setNameTableColumn(nameTableColumn);
            primaryKeyValueProvider.setObjectObservableList(dataView.getSelectionModel().getSelectedItem());
            primaryKeyValueProvider.setOnSucceeded(workerStateEvent1 -> {
                List<String> values = primaryKeyValueProvider.getValue();
                deleteData.setTableName(tableName);
                deleteData.setValues(values);
                deleteData.setPrimaryKey(primaryKey);
                deleteData.setOnSucceeded(workerStateEvent2 -> {
                    columnDetailsProvider.getValue().getColumn().remove(dataView.getSelectionModel().getSelectedItem());
                    dataView.getSelectionModel().clearSelection();
                    dataView.requestFocus();
                });
                startService(deleteData);
            });
            startService(primaryKeyValueProvider);

        });
        startService(primaryKeyProvider);

    }

    private void updateData(String newValue, String columnModified) {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        primaryKeyProvider.setTableName(tableName);
        primaryKeyProvider.setOnSucceeded(workerStateEvent -> {
            List<String> primaryKey = primaryKeyProvider.getValue();
            primaryKeyValueProvider.setPrimaryKey(primaryKey);
            primaryKeyValueProvider.setAllColumns(dataView.getColumns());
            primaryKeyValueProvider.setNameTableColumn(nameTableColumn);
            primaryKeyValueProvider.setObjectObservableList(dataView.getSelectionModel().getSelectedItem());
            primaryKeyValueProvider.setOnSucceeded(workerStateEvent1 -> {
                List<String> values = primaryKeyValueProvider.getValue();
                updateData.setTableName(tableName);
                updateData.setColumnModified(columnModified);
                updateData.setNewValue(newValue);
                updateData.setValues(values);
                updateData.setPrimaryKey(primaryKey);
                startService(updateData);
            });
            startService(primaryKeyValueProvider);
        });
        startService(primaryKeyProvider);
    }

    public void description() {
        Stage stage = new Stage();
        stage.initOwner(databaseView.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/about.fxml"));
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
            descAll.setTableName(tableName);
            startService(descAll);
        } catch (IOException e) {
            alertShow(e);
        }


    }

    public void updateToNull(String columnModified) {
        updateData(null, columnModified);
    }

    //deleteDatabase
    public void deleteDatabaseFunction() {
        BiConsumer<String, ListView<String>> s = (s1, listView) -> {
            deleteDatabaseProvider.setDatabaseName(s1);
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
            deleteDatabaseFunction();
        } else if (add.match(e)) {
            addDatabase();
        }
    }

    //Delete Table
    public void deleteTableFunction() {
        BiConsumer<String, ListView<String>> consumer = (s, listView) -> {
            deleteTableProvider.setTableName(s);
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
                deleteDatabaseFunction();
            } else if (add.match(e)) {
                addTableFunction();
            } else if (showPrimaryKey.match(e)) {
                getPrimaryKey();
            } else if (showDescriptionKey.match(e)) {
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
            ObservableList<Object> selectedItem = dataView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                deleteData();
            }
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
            descriptionTable();
        }
    }

    public void tableColumnArrangement(Database.Column columnsList,
                                       List<String> primaryKey, List<String> foreignKey
    ) {
        dataView.getColumns().clear();
        tableColumnName = new HashMap<>();
        nameTableColumn = new HashMap<>();
        int z = 0;
        int h = 0;

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
            if (primaryKey != null &&
                    z < primaryKey.size() &&
                    columnsList.getHeading().get(i).equals(primaryKey.get(z))) {
                z++;
                ImageView img = new ImageView(new Image(getClass()
                        .getResource("/image/primaryKey.png").toExternalForm()));
                img.setFitWidth(15);
                img.setFitHeight(15);
                img.getStyleClass().add("primaryKey");
                hBox.getChildren().add(img);
            } else if (foreignKey != null &&
                    h < foreignKey.size() &&
                    columnsList.getHeading().get(i).equals(foreignKey.get(h))
            ) {
                h++;
                ImageView img = new ImageView(new Image(getClass()
                        .getResource("/image/foreignKey.png").toExternalForm()));
                img.setFitWidth(15);
                img.setFitHeight(15);
                img.getStyleClass().add("primaryKey");
                hBox.getChildren().add(img);
            }
            Label heading = new Label(columnsList.getHeading().get(i));
            //for columnName suggestions
            whereQuery.getStrings().add(columnsList.getHeading().get(i).toLowerCase());
            Label dataType = new Label(columnsList.getType().get(i));
            VBox textContainer = new VBox(heading, dataType);
            hBox.getChildren().addAll(textContainer);
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setMaxWidth(HBox.USE_PREF_SIZE);
            hBox.setMinWidth(HBox.USE_PREF_SIZE);
            tableColumn.setGraphic(hBox);
            hBox.widthProperty().addListener((observableValue, number, t1) ->
                    tableColumn.setPrefWidth(hBox.prefWidth(-1) + 10));
            tableColumn.setCellFactory(updateItem());
            tableColumn.setOnEditCommit(t -> {
                updateData.setOnSucceeded(workerStateEvent2 -> {
                    TablePosition<ObservableList<Object>, String> tablePosition = t.getTablePosition();
//                System.out.println(tablePosition.getColumn());
                    t.getRowValue().set(tablePosition.getColumn(), t.getNewValue());
                    dataView.requestFocus();
                });
                updateData(t.getNewValue(), tableColumnName.get(t.getTableColumn()));
            });
            dataView.getColumns().add(tableColumn);
            tableColumnName.put(tableColumn, columnsList.getHeading().get(i));
            nameTableColumn.put(columnsList.getHeading().get(i), tableColumn);

        }
        dataView.setItems(columnsList.getColumn());
        dataView.getSelectionModel().select(0, dataView.getColumns().get(0));
    }

    @FXML
    public void handleKeyPressedOnSuggestion(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            whereClause();
        }
    }

    public void whereClause() {
        filterQueryProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
        filterQueryProvider.setWhereQuery(whereQuery.getText());
        startService(filterQueryProvider);
    }

    public void changeTableNameFunction(String newTableName) {
        changeTableName.setOldTableName(tableView.getSelectionModel().getSelectedItem());
        changeTableName.setNewTableName(newTableName);
        changeTableName.setOnSucceeded(workerStateEvent ->
                tableView.getItems().set(tableView.getSelectionModel().getSelectedIndex(),
                        newTableName));
        startService(changeTableName);
    }

    public void saveSnapshotDB() {
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
    }

    public boolean changeSavingPath(String databaseName) {
        File savingFile = fileChooser.showSaveDialog(databaseView.getScene().getWindow());
        if (savingFile != null) {
            initialFolder.set(savingFile.getParentFile());
            fileName.put(databaseName, savingFile);
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
            }
        }
    }

    //    filterQueryProvider.setOnFailed(workerStateEvent ->
//    alertShow(filterQueryProvider.getException()));
//
//        filterQueryProvider.setOnSucceeded(workerStateEvent -> {
//        primaryKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
//
//        primaryKeyProvider.setOnSucceeded(workerStateEvent1 ->{
//            foreignKeyProvider.setOnSucceeded(workerStateEvent2 ->
//                    tableColumnArrangement(filterQueryProvider, primaryKeyProvider.getValue(),foreignKeyProvider.getValue()));
//            startService(foreignKeyProvider);
//        });
//        startService(primaryKeyProvider);
//    });
    private void dataQueryProvider(Service<Database.Column> queryProvider) {
        queryProvider.setOnFailed(workerStateEvent ->
                alertShow(queryProvider.getException()));

        queryProvider.setOnSucceeded(workerStateEvent -> {
            primaryKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());

            primaryKeyProvider.setOnSucceeded(workerStateEvent1 -> {
                foreignKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
                foreignKeyProvider.setOnSucceeded(workerStateEvent2 ->
                        tableColumnArrangement(
                                queryProvider.getValue(), primaryKeyProvider.getValue(), foreignKeyProvider.getValue()
                        )
                );
                startService(foreignKeyProvider);
            });
            startService(primaryKeyProvider);
        });
    }
}