package controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class AdminForgotPassword {

    private Stage currentStage;

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    @FXML
    public void exit(){
        currentStage.close();
    }
}
