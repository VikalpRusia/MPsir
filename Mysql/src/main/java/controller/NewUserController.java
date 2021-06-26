package controller;


import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

public class NewUserController {

    @FXML
    private DialogPane dialogPane;

    public void initialize(){
        dialogPane.getButtonTypes().addAll(
                ButtonType.OK,ButtonType.CANCEL
        );

    }
}
