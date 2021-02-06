package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

public class ModifyTableAddColumn {

    @FXML
    private ComboBox<String> columnType;
    @FXML
    private DialogPane dialogPane;
    @FXML
    private TextField columnName;
    @FXML
    private ComboBox<Boolean> primaryKey;
    @FXML
    private ComboBox<Boolean> notNull;
    @FXML
    private ComboBox<Boolean> autoIncrement;
    @FXML
    private ComboBox<Boolean> unique;

    public void initialize() {
        ButtonType submit = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(
                submit, ButtonType.CANCEL
        );
        Node submitButton = dialogPane.lookupButton(submit);
        submitButton.setDisable(true);
        columnName.textProperty().addListener((observableValue, s, t1) -> validate(submitButton));
        columnType.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, number, t1) -> {
                    int value = t1.intValue();
                    if (value >= 0) {
                        columnType.getEditor().positionCaret(
                                columnType.getItems().get(value).length());
                    }
                    validate(submitButton);
                });
    }

    private void validate(Node submitButton) {
        submitButton.setDisable(columnName.getText().trim().isEmpty() || columnType.getValue() == null
                || columnType.getValue().isEmpty());
    }

    public String getColumnName() {
        return columnName.getText();
    }

    public boolean getPrimaryKey() {
        return primaryKey.getValue();
    }

    public Boolean getNotNull() {
        return notNull.getValue();
    }

    public Boolean getAutoIncrement() {
        return autoIncrement.getValue();
    }

    public Boolean getUnique() {
        return unique.getValue();
    }

    public String getColumnType() {
        return columnType.getValue();
    }
}
