package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;

public class AddColumnController {

    @FXML
    private ComboBox<Boolean> primaryKey;

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TextField columnName;

    @FXML
    private ComboBox<String> columnType;

    @FXML
    public void initialize() {
        dialogPane.getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CLOSE
        );
        columnType.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, number, t1) -> {
                    int value = t1.intValue();
                    if (value >= 0) {
                        columnType.getEditor().positionCaret(
                                columnType.getItems().get(value).length());
                    }
                });
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
}
