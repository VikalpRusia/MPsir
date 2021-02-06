package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ToggleGroup;

public class ForgotPassword {

    @FXML
    private DialogPane dialogPane;
    @FXML
    private ToggleGroup accountType;
    private int selectedToggleValue;

    public void initialize() {
        dialogPane.getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL
        );
        accountType.selectedToggleProperty().addListener((observableValue, toggle, t1) ->
                selectedToggleValue = (int)t1.getUserData());
    }

    protected int selectedAccount() {
        return selectedToggleValue;
    }
}
