package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreateTableController {

    private ObservableList<AddColumnInTable> tableViewData;

    @FXML
    private TextField tableName;

    @FXML
    private TableColumn<AddColumnInTable, String> primaryKey;

    @FXML
    private TableColumn<AddColumnInTable, String> columnNameColumn;

    @FXML
    private TableColumn<AddColumnInTable, String> columnTypeColumn;

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TableView<AddColumnInTable> mainTableView;

    @FXML
    private ImageView additionImage;

    @FXML
    private ImageView minusImage;

    public void initialize() {
        tableViewData = FXCollections.observableArrayList();
        dialogPane.getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );
        Tooltip tooltipAdd = new Tooltip("Add column");
        Tooltip.install(additionImage, tooltipAdd);
        Tooltip tooltipSubtract = new Tooltip("Delete column");
        Tooltip.install(minusImage, tooltipSubtract);

        columnNameColumn.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getColumnName()));
        columnTypeColumn.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getColumnType()));
        primaryKey.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().isPrimaryKey().toString()));
        mainTableView.setItems(tableViewData);

    }

    @FXML
    public void addImageClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTableAddColumn.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(loader.load());
        AddColumnController controller = loader.getController();
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = controller.getColumnName();
            String type = controller.getColumnType();
            boolean isPrimary = controller.getPrimaryKey();
            if (name.equals("") || type.equals("")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("All Fields were necessary");
                alert.setContentText("No fields modified");
                alert.showAndWait();
                return;
            }
            tableViewData.add(new AddColumnInTable(name, type, isPrimary));
            mainTableView.getSelectionModel().select(0);
        }
    }

    @FXML
    public void minusImageClick() {
        if (mainTableView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No Entry!");
            alert.setContentText("Enter table column before deleting them!");
            alert.showAndWait();
        }
        tableViewData.remove(
                mainTableView.getSelectionModel().getSelectedItem()
        );
    }

    public String getTableName() {
        return tableName.getText();
    }

    public List<String> getColumnsName() {
        return tableViewData.stream().map(AddColumnInTable::getColumnName).collect(Collectors.toList());
    }

    public List<String> getColumnsType() {
        return tableViewData.stream().map(AddColumnInTable::getColumnType).collect(Collectors.toList());
    }

    public List<String> getPrimaryKeys() {
        return tableViewData
                .stream()
                .filter(AddColumnInTable::isPrimaryKey)
                .map(AddColumnInTable::getColumnName)
                .collect(Collectors.toList());
    }

    public static class AddColumnInTable {
        private final String columnName;
        private final String columnType;
        private final Boolean primaryKey;

        public AddColumnInTable(String columnName, String columnType, boolean primaryKey) {
            this.columnName = columnName;
            this.columnType = columnType;
            this.primaryKey = primaryKey;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getColumnType() {
            return columnType;
        }

        public Boolean isPrimaryKey() {
            return primaryKey;
        }
    }
}
