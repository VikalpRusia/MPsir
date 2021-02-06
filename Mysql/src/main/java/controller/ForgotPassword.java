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

    public void initialize() {
        dialogPane.getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL
        );
    }

    protected int selectedAccount() {
        return (int) accountType.getUserData();
    }
}
