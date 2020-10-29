package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.Callback;
import model.Database;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLOutput;
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
        addTable.setOnAction(tr ->{
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
        });
        contextMenu_Data_Table.getItems().addAll(addTable, deleteTable);

        MenuItem addTableEmpty = new MenuItem("Add table");
        contextMenuTable.getItems().addAll(addTableEmpty);

        //table row Context
        contextMenuRow = new ContextMenu();
        MenuItem add_row = new MenuItem("Add row");
        contextMenuRow.getItems().addAll(add_row);

        //table data context
        contextMenuDataRow = new ContextMenu();
        MenuItem add_row_Empty = new MenuItem("Add row");
        MenuItem edit_row = new MenuItem("Edit row");
        MenuItem delete_row = new MenuItem("Delete row");
        contextMenuDataRow.getItems().addAll(add_row_Empty, edit_row, delete_row);

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

        tableView.getSelectionModel().select(0);
    }

    public void changedTable(String tableName) {
        dataView.getColumns().clear();
        try {
            columnsList = database.showData(tableName);
        } catch (SQLException e) {
            alertShow(e);
        }
        for (int i = 0; i < columnsList.getHsize(); i++) {
            TableColumn<ObservableList<Object>, String> tableColumn = new TableColumn<>(columnsList.getHeading().get(i));
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
        alert.setHeaderText("You are going to delete " + listView.getSelectionModel().getSelectedItem() + " !");
        alert.setContentText("Are you Sure ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String Name = listView.getSelectionModel().getSelectedItem();
            consumer.accept(Name);
            listView.getItems().removeAll(Name);
        }
    }

    public void alertShow(SQLException e) {
        Alert error = new Alert(Alert.AlertType.ERROR);
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
        Optional<String> result = textInputDialog.showAndWait();
        if (result.isPresent() && !result.get().equals("")) {
            String x = result.get();
            database.createDatabase(x);
            Pattern pattern = Pattern.compile("^(.*?);*$");
            Matcher matcher = pattern.matcher(x);
            while (matcher.find()) {
                //1
                databaseList.addAll(matcher.group(1).toLowerCase());
            }
        }
    }

    public void inputTable() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTable.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        CreateTableController controller = loader.getController();
        dialog.setDialogPane(loader.load());
        Optional<ButtonType> result =dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){

        }

    }
}