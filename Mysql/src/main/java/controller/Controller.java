package controller;

import controller.serviceProvider.Services;
import javafx.application.Platform;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.Database;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Considered normal database i.e. it is basic not customisable.
 * Refer point 1 in addDatabase method
 */
public class Controller {

    //    Database database;

    Services.DatabaseListProvider databaseListProvider;
    Services.TableListProvider tableListProvider;
    Services.ColumnDetailsProvider columnDetailsProvider;
    Services.PrimarykeyService primaryKeyProvider;
    Services.DeleteDatabaseService deleteDatabaseProvider;
    Services.DeleteTableService deleteTableProvider;
    Services.AddDatabase createDatabase;
    Services.CreateTable createTable;
    Services.InsertData insertIntoTable;
    Services.DeleteData deleteData;
    Services.UpdateData updateData;
    Services.DescAll descAll;
    Services.PrimaryKeyValueProvider primaryKeyValueProvider;

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

    public void initialize() {
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
        primaryKeyProvider = new Services.PrimarykeyService();
        primaryKeyProvider.setOnFailed(workerStateEvent ->
                alertShow(primaryKeyProvider.getException()));

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
        //columnRelated
        columnDetailsProvider = new Services.ColumnDetailsProvider();
        columnDetailsProvider.setOnFailed(workerStateEvent ->
                alertShow(columnDetailsProvider.getException()));
        columnDetailsProvider.setOnSucceeded(workerStateEvent -> {
            primaryKeyProvider.setTableName(tableView.getSelectionModel().getSelectedItem());
            primaryKeyProvider.setOnSucceeded(workerStateEvent1 -> {
                tableColumnName = new HashMap<>();
                nameTableColumn = new HashMap<>();
                int z = 0;
                List<String> key = primaryKeyProvider.getValue();

                Database.Column columnsList = columnDetailsProvider.getValue();
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
                    if (key != null &&
                            z < key.size() &&
                            columnsList.getHeading().get(i).equals(key.get(z))) {
                        z++;
                        ImageView img = new ImageView(new Image(getClass()
                                .getResource("/image/primarkey.png").toExternalForm()));
                        img.setFitWidth(15);
                        img.setFitHeight(15);
                        img.getStyleClass().add("primaryKey");
                        hBox.getChildren().add(img);
                    }
                    Label heading = new Label(columnsList.getHeading().get(i));
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
            });
            startService(primaryKeyProvider);

        });
        //Table related
        dataView.setEditable(true);
//        dataView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dataView.getSelectionModel().

                setCellSelectionEnabled(true);

        contextMenu_Data_Database = new

                ContextMenu();

        contextMenuDatabase = new

                ContextMenu();

        MenuItem addDatabase = new MenuItem("Add database");
        addDatabase.setOnAction(actionEvent -> addDatabase());
        MenuItem deleteDatabase = new MenuItem("Delete database");
        deleteDatabase.setOnAction(actionEvent ->

        {
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
        });
        contextMenu_Data_Database.getItems().
                addAll(addDatabase, deleteDatabase);

        MenuItem addDatabaseEmpty = new MenuItem("Add database");
        addDatabaseEmpty.setOnAction(actionEvent -> addDatabase());
        contextMenuDatabase.getItems().
                addAll(addDatabaseEmpty);

        //table Context
        contextMenu_Data_Table = new

                ContextMenu();

        contextMenuTable = new

                ContextMenu();

        MenuItem addTable = new MenuItem("Add table");
        addTable.setOnAction(tr ->

        {
            try {
                inputTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        MenuItem deleteTable = new MenuItem("Delete table");
        deleteTable.setOnAction(tr ->

        {
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
        });
        MenuItem primaryKey = new MenuItem("Primary Key");
        primaryKey.setOnAction(s ->

                getPrimaryKey());
        MenuItem description = new MenuItem("Description");
        description.setOnAction(s ->

        {
            try {
                descriptionTable(tableView.getSelectionModel().getSelectedItem());
            } catch (IOException e) {
                alertShow(e);
            }
        });
        contextMenu_Data_Table.getItems().

                addAll(addTable, deleteTable, primaryKey, description);

        MenuItem addTableEmpty = new MenuItem("Add table");
        addTableEmpty.setOnAction(tr ->

        {
            try {
                inputTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        contextMenuTable.getItems().

                addAll(addTableEmpty);

        //table row Context
        contextMenuRow = new

                ContextMenu();

        MenuItem add_row = new MenuItem("Add row");
        add_row.setOnAction(e ->

                inputDataInTable());
        contextMenuRow.getItems().

                addAll(add_row);

        //table data context
        contextMenuDataRow = new

                ContextMenu();

        MenuItem add_row_Empty = new MenuItem("Add row");
        add_row_Empty.setOnAction(e ->

                inputDataInTable());
        MenuItem add_null = new MenuItem("Update to null");
        add_null.setOnAction(z ->

        {
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
        delete_row.setOnAction(s ->

                deleteData());
        contextMenuDataRow.getItems().

                addAll(add_row_Empty, add_null, delete_row);

        databaseView.getSelectionModel().

                selectedItemProperty().

                addListener((observableValue, s, t1) ->

                {
                    if (t1 != null) {
                        changedDatabase();
                    }
                });
        databaseView.setCellFactory(stringListView ->

                contextFunction(contextMenu_Data_Database, contextMenuDatabase));
        tableView.getSelectionModel().

                selectedItemProperty().

                addListener((observableValue, s, t1) ->
                        changedTable(t1));
        tableView.setCellFactory(stringListView ->

                contextFunction(contextMenu_Data_Table, contextMenuTable));
        tableView.setContextMenu(contextMenuTable);
        dataView.setContextMenu(contextMenuRow);


    }

    public void setDatabase(Database database) {
        Services.setDatabase(database);
    }

    public void initData() {
        startService(databaseListProvider);
    }


    public void changedDatabase() {
        tableListProvider.setDatabaseName(
                databaseView.getSelectionModel().getSelectedItem());
        startService(tableListProvider);
    }

    public void changedTable(String tableName) {
        dataView.getColumns().clear();
        if (tableName == null) {
            return;
        }
        columnDetailsProvider.setTableName(tableName);
        startService(columnDetailsProvider);

    }

    private ListCell<String> contextFunction(ContextMenu contextMenu, ContextMenu contextMenuEmpty) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setContextMenu(contextMenuEmpty);
                } else {
                    setText(s);
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
                    //1
                    s = matcher.group(1).toLowerCase();
                    databaseList.addAll(s);
                }
                databaseView.getSelectionModel().select(s);
            });
            startService(createDatabase);
        }
    }

    public void inputTable() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTable.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Table creation");
        dialog.initOwner(databaseView.getScene().getWindow());
        dialog.setDialogPane(loader.load());
        CreateTableController controller = loader.getController();
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
                StringConverter<String> stringConverter = new StringConverter<>() {
                    @Override
                    public String toString(String s) {
                        return s;
                    }

                    @Override
                    public String fromString(String s) {
                        return s;
                    }
                };
                return new TextFieldTableCell<>(stringConverter) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setFont(Font.font(15));
                        if (empty) {
                            setText(null);
                            setEditable(false);
                            setContextMenu(contextMenuRow);
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

    public void description() throws IOException {
        Stage stage = new Stage();
        stage.initOwner(databaseView.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/about.fxml"));
        stage.setWidth(600);
        stage.setHeight(600);
        stage.setTitle("Descriptions !");
        stage.setScene(new Scene(root));
        stage.showAndWait();

    }

    public void descriptionTable(String tableName) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(databaseView.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/descriptionTable.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.sizeToScene();
        stage.setTitle("Description of " + tableName);
        DescriptionTableController descController = fxmlLoader.getController();
        descAll.setOnSucceeded(workerStateEvent -> {
            descController.setData(descAll.getValue());
            stage.showAndWait();
        });
        descAll.setTableName(tableName);
        startService(descAll);


//        stage.setResizable(false);

    }

    public void updateToNull(String columnModified) {
        updateData(null, columnModified);
    }

    private void startService(Service<?> service) {
        if (service.getState() == Worker.State.READY) {
            service.start();
        } else if (service.getState() == Worker.State.SUCCEEDED
                || service.getState() == Worker.State.FAILED
        ) {
            service.restart();
        }
    }


}