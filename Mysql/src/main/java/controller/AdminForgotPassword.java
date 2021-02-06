package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminForgotPassword {

    private Stage currentStage;

    @FXML
    private AnchorPane root;
    @FXML
    private TextField searchTextField;
    @FXML
    private VBox error;

    public void initialize(){
        Platform.runLater(()-> root.requestFocus());
        error.managedProperty().bind(error.visibleProperty());
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    @FXML
    public void exit(){
        currentStage.close();
    }

    @FXML
    public void search(){
        String searchText = searchTextField.getText();
        error.setVisible(true);
    }
}
