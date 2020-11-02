package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.Database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Considered normal database i.e. it is basic not customisable.
 * Refer point 1 in addDatabase method
 */
public class Controller {

    Database database;
    ObservableList<String> databaseList;
    ObservableList<String> tableList;
    Database.Column columnsList;

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
        dataView.setEditable(true);
        contextMenu_Data_Database = new ContextMenu();
        contextMenuDatabase = new ContextMenu();
        MenuItem addDatabase = new MenuItem("Add database");
        addDatabase.setOnAction(actionEvent -> {
            try {
                addDatabase(databaseList);
            } catch (SQLException e) {
                alertShow(e);
            }
        });
        MenuItem deleteDatabase = new MenuItem("Delete database");
        deleteDatabase.setOnAction(actionEvent -> {
            Consumer<String> s = s1 -> {
                try {
                    database.dropDatabase(s1);
                } catch (SQLException e) {
                    alertShow(e);
                }
            };
            deletionHandling(databaseView, s);
            if (databaseList.size() == 0) {
                tableList.clear();
                dataView.getColumns().clear();
            }
        });
        contextMenu_Data_Database.getItems().addAll(addDatabase, deleteDatabase);
        MenuItem addDatabaseEmpty = new MenuItem("Add database");
        addDatabaseEmpty.setOnAction(actionEvent -> {
            try {
                addDatabase(databaseList);
            } catch (SQLException e) {
                alertShow(e);
            }
        });
        contextMenuDatabase.getItems().addAll(addDatabaseEmpty);

        //table Context
        contextMenu_Data_Table = new ContextMenu();
        contextMenuTable = new ContextMenu();
        MenuItem addTable = new MenuItem("Add table");
        addTable.setOnAction(tr -> {
            try {
                inputTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        MenuItem deleteTable = new MenuItem("Delete table");
        deleteTable.setOnAction(tr -> {
            Consumer<String> consumer = s -> {
                try {
                    database.dropTable(s);
                } catch (SQLException e) {
                    alertShow(e);
                }
            };
            deletionHandling(tableView, consumer);
            if (tableList.size() == 0) {
                dataView.getColumns().clear();
            }
        });
        MenuItem primaryKey = new MenuItem("Primary Key");
        primaryKey.setOnAction(s -> getPrimaryKey());
        contextMenu_Data_Table.getItems().addAll(addTable, deleteTable, primaryKey);

        MenuItem addTableEmpty = new MenuItem("Add table");
        addTableEmpty.setOnAction(tr -> {
            try {
                inputTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        contextMenuTable.getItems().addAll(addTableEmpty);

        //table row Context
        contextMenuRow = new ContextMenu();
        MenuItem add_row = new MenuItem("Add row");
        add_row.setOnAction(e -> inputDataInTable());
        contextMenuRow.getItems().addAll(add_row);

        //table data context
        contextMenuDataRow = new ContextMenu();
        MenuItem add_row_Empty = new MenuItem("Add row");
        add_row_Empty.setOnAction(e -> inputDataInTable());
        MenuItem delete_row = new MenuItem("Delete row");
        delete_row.setOnAction(s -> deleteData());
        contextMenuDataRow.getItems().addAll(add_row_Empty, delete_row);

        databaseView.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            if (t1 != null) {
                changedDatabase(t1);
            }
        });
        databaseView.setCellFactory(stringListView -> contextFunction(contextMenu_Data_Database, contextMenuDatabase));
        tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            if (t1 != null) {
                changedTable(t1);
            }
        });
        tableView.setCellFactory(stringListView -> contextFunction(contextMenu_Data_Table, contextMenuTable));
        tableView.setContextMenu(contextMenuTable);

        dataView.setRowFactory(new Callback<>() {
            @Override
            public TableRow<ObservableList<Object>> call(TableView<ObservableList<Object>> observableListTableView) {
                return new TableRow<>() {
                    @Override
                    protected void updateItem(ObservableList<Object> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setItem(null);
                            setContextMenu(contextMenuRow);
                        } else {
                            setItem(item);
                            setContextMenu(contextMenuDataRow);
                        }
                    }
                };
            }
        });
        dataView.setContextMenu(contextMenuRow);
    }

    public void initData(Database database) {
        this.database = database;
        try {
            databaseList = database.showDatabase();
        } catch (SQLException e) {
            alertShow(e);
        }
        databaseView.setItems(databaseList);

        databaseView.getSelectionModel().select(0);

    }


    public void changedDatabase(String databaseName) {
        try {
            tableList = database.showTables(databaseName);
        } catch (SQLException e) {
            alertShow(e);
        }
        tableView.setItems(tableList);
        dataView.getColumns().clear();//when table changes it should be reflected in data
        tableView.getSelectionModel().select(0);
    }

    public void changedTable(String tableName) {
        dataView.getColumns().clear();
        List<String> key = null;
        int z = 0;
        try {
            columnsList = database.showData(tableName);
            key = database.primaryKey(tableName);
        } catch (SQLException e) {
            alertShow(e);
        }
        for (int i = 0; i < columnsList.getHsize(); i++) {
            TableColumn<ObservableList<Object>, String> tableColumn = new TableColumn<>(
                    columnsList.getHeading().get(i) + "\n" + columnsList.getType().get(i));
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
            if (key != null &&
                    z < key.size() &&
                    columnsList.getHeading().get(i).equals(key.get(z))) {
                z++;
                ImageView img = new ImageView(new Image(getClass()
                        .getResource("/image/primarkey.png").toExternalForm()));
                img.setFitWidth(15);
                img.setFitHeight(15);
                HBox hBox = new HBox(img);
                hBox.prefHeightProperty().bind(img.fitHeightProperty());
                hBox.prefWidthProperty().bind(img.fitWidthProperty());
                img.getStyleClass().add("primaryKey");
                tableColumn.setGraphic(hBox);
            }
            tableColumn.setCellFactory(updateItem());
            tableColumn.setOnEditCommit(t -> {
                updateData(t.getNewValue(), t.getTableColumn().getText());
                TablePosition<ObservableList<Object>, String> tablePosition = t.getTablePosition();
//                System.out.println(tablePosition.getColumn());
                t.getRowValue().set(tablePosition.getColumn(), t.getNewValue());
            });
            dataView.getColumns().add(tableColumn);
        }
        dataView.setItems(columnsList.getColumn());

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
        initData(database);
    }

    public void deletionHandling(ListView<String> listView, Consumer<String> consumer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(databaseView.getScene().getWindow());
        alert.setHeaderText("You are going to delete " + listView.getSelectionModel().getSelectedItem() + " !");
        alert.setContentText("Are you Sure ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String Name = listView.getSelectionModel().getSelectedItem();
            consumer.accept(Name);
            listView.getItems().removeAll(Name);
        }
    }

    public void alertShow(Exception e) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.initOwner(databaseView.getScene().getWindow());
        error.setTitle("Query failed!");
        error.setHeaderText("error !");
        error.setContentText(e.getMessage());
        error.showAndWait();
    }

    public void addDatabase(ObservableList<String> databaseList) throws SQLException {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setTitle("Database Name");
        textInputDialog.setHeaderText("Enter database Name: ");
        textInputDialog.setContentText("database cannot be empty");
        textInputDialog.initOwner(databaseView.getScene().getWindow());
        Optional<String> result = textInputDialog.showAndWait();
        if (result.isPresent() && !result.get().equals("")) {
            String x = result.get();
            database.createDatabase(x);
            Pattern pattern = Pattern.compile("^(.*?);*$");
            Matcher matcher = pattern.matcher(x);
            String s = null;
            while (matcher.find()) {
                //1
                s = matcher.group(1).toLowerCase();
                databaseList.addAll(s);
            }
            databaseView.getSelectionModel().select(s);
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
            try {
                database.createTable(
                        controller.getTableName(),
                        controller.getColumnsName(),
                        controller.getColumnsType(),
                        controller.getPrimaryKeys()
                );
                tableList.add(controller.getTableName());
                tableView.getSelectionModel().select(controller.getTableName());
            } catch (SQLException e) {
                alertShow(e);
            }

        }

    }

    protected static Callback<TableColumn<ObservableList<Object>, String>, TableCell<ObservableList<Object>, String>> updateItem() {
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
                        } else if (item == null) {
                            setText("<Null>");
                            setTextFill(Color.PURPLE);
                            setEditable(true);
                        } else {
                            setText(item);
                            setTextFill(Color.BLACK);
                            setEditable(true);
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
        controller.setColumnsList(columnsList);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                database.insertIntoTable(tableView.getSelectionModel().getSelectedItem(),
                        controller.values()
                );
                columnsList.getColumn().add(controller.values());
                dataView.getSelectionModel().select(controller.values());
            } catch (SQLException e) {
                alertShow(e);
            }
        }
    }

    public void getPrimaryKey() {
        String tableName = tableView.getSelectionModel().getSelectedItem();
        try {
            List<String> keys = database.primaryKey(tableName);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            StringBuilder s = new StringBuilder();
            keys.forEach(s1 -> s.append(s1).append("\n"));
            alert.setHeaderText("Primary keys are !");
            alert.setContentText(s.toString());
            alert.showAndWait();
        } catch (SQLException e) {
            alertShow(e);
        }
    }

    public void deleteData() {
        try {
            List<String> values = primaryKeyValues();
            database.deleteData(tableView.getSelectionModel().getSelectedItem(), values);
            columnsList.getColumn().remove(dataView.getSelectionModel().getSelectedItem());
        } catch (SQLException e) {
            alertShow(e);
        }

    }

    public void updateData(String newValue, String columnModified) {
        try {
            List<String> values = primaryKeyValues();
            database.updateData(tableView.getSelectionModel().getSelectedItem(),
                    columnModified, newValue, values);

        } catch (SQLException e) {
            alertShow(e);
        }
    }

    private List<String> primaryKeyValues() throws SQLException {
        List<Integer> primaryKeyColumns = database.positionPrimaryKey(
                tableView.getSelectionModel().getSelectedItem()
        );
        List<String> values = new ArrayList<>();
        for (Integer key : primaryKeyColumns) {
            values.add(
                    dataView.getSelectionModel().getSelectedItem().get(key - 1).toString()
            );
        }
//        System.out.println(values);
        return values;
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
}