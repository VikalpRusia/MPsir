package controller;

import controller.serviceProvider.Services;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.Map;

public class AddColumnController {
    private Services.GetColumnNameAndType getColumnNameAndType;
    private Map<String, String> sample;
    @FXML
    private HBox hBox;
    @FXML
    private ComboBox<String> tableName;
    @FXML
    private ComboBox<String> tableColumnName;

    @FXML
    private ComboBox<Boolean> primaryKey;

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TextField columnName;

    @FXML
    private ComboBox<String> columnType;

    @FXML
    private ComboBox<Boolean> foreignKey;

    @FXML
    private ComboBox<Boolean> uniqueKey;

    @FXML
    private ComboBox<Boolean> notNull;

    @FXML
    private ComboBox<Boolean> autoIncrement;

    @FXML
    public void initialize() {
        hBox.visibleProperty().bind(foreignKey.valueProperty());
        hBox.managedProperty().bind(foreignKey.valueProperty());

        getColumnNameAndType = new Services.GetColumnNameAndType();
        getColumnNameAndType.setOnFailed(workerStateEvent -> {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.initOwner(dialogPane.getScene().getWindow());
            error.setTitle("Query failed!");
            error.setHeaderText("error !");
            error.setContentText(getColumnNameAndType.getException().getMessage());
            error.showAndWait();
        });
        getColumnNameAndType.setOnSucceeded(workerStateEvent -> {
            sample = getColumnNameAndType.getValue();
            tableColumnName.getItems().clear();
            for (String entry : sample.keySet()) {
                tableColumnName.getItems().add(entry);
            }
            tableColumnName.getSelectionModel().select(0);
        });

        tableName.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, s, t1) -> changeColumnName(t1));

        ButtonType submit = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(
                submit, ButtonType.CANCEL
        );
        Node submitButton = dialogPane.lookupButton(submit);
        submitButton.setDisable(true);
        columnName.textProperty().addListener((observableValue, s, t1) -> verify(submitButton));
        columnType.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, number, t1) -> {
                    int value = t1.intValue();
                    if (value >= 0) {
                        columnType.getEditor().positionCaret(
                                columnType.getItems().get(value).length());
                    }
                    verify(submitButton);
                });
    }

    private void verify(Node submitButton) {
        submitButton.setDisable(columnName.getText().trim().isEmpty() || columnType.getValue() == null
                || columnType.getValue().isEmpty());
    }

    public String getColumnName() {
        return columnName.getText();
    }

    public String getColumnType() {
        return columnType.getValue();
    }

    public Boolean getPrimaryKey() {
        return primaryKey.getValue();
    }

    public Boolean getUniqueKey() {
        return uniqueKey.getValue();
    }

    public Boolean getNotNull() {
        return notNull.getValue();
    }

    public Boolean getAutoIncrement() {
        return autoIncrement.getValue();
    }

    public void setTable(ObservableList<String> stringObservableList) {
        tableName.setItems(stringObservableList);
        tableName.getSelectionModel().select(0);
    }

    public void changeColumnName(String tableName) {
        getColumnNameAndType.setTableName(tableName);
        MainController.startService(getColumnNameAndType);
    }

    public String getForeignKey() {
        if (foreignKey.getValue()) {
            return tableName.getValue() + " (" + sample.get(tableColumnName.getValue()) + ")";
        }
        return "false";
    }
}
