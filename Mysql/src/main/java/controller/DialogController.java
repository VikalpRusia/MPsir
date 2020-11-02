package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class DialogController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private DialogPane dialogPane;

    public void initialize() {
        dialogPane.getButtonTypes().add(
                ButtonType.CLOSE);
        dialogPane.getButtonTypes().add(
                ButtonType.OK
        );
        Platform.runLater(() -> username.requestFocus());
    }

    public String getUsername() {
        return username.getText();
    }

    public String getPassword() {
        return password.getText();
    }
}
