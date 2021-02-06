package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
        Button okBtn = (Button) dialogPane.lookupButton(dialogPane.getButtonTypes().get(0));
        okBtn.setStyle("-fx-background-color: royalblue;-fx-text-fill: white");
        accountType.selectedToggleProperty().addListener((observableValue, toggle, t1) ->
                selectedToggleValue = Integer.parseInt((String)t1.getUserData()));
    }

    protected int selectedAccount() {
        return selectedToggleValue;
    }
}
