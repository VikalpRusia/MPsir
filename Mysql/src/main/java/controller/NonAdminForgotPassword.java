package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class NonAdminForgotPassword {

    private Stage currentStage;

    @FXML
    private AnchorPane root;
    @FXML
    private TextField userName;

    public void initialize() {
        Platform.runLater(() -> root.requestFocus());
    }

    public void sendRequest() {

    }

    public void exit() {
        currentStage.close();
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }
}
