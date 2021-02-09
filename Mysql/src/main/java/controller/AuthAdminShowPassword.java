package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthAdminShowPassword {

    private Stage currentStage;
    private SimpleStringProperty userNameStr = new SimpleStringProperty();
    private SimpleStringProperty searchDataStr = new SimpleStringProperty();
    @FXML
    private Label userName;
    @FXML
    private Label searchData;

    public void initialize() {
        userName.textProperty().bind(userNameStr);
        searchData.textProperty().bind(searchDataStr);
    }


    public void setUserNameStr(String userNameStr) {
        this.userNameStr.setValue("Username : "+userNameStr);
    }

    public void setSearchDataStr(String searchDataStr) {
        this.searchDataStr.setValue("Identification by : "+searchDataStr);
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void resetPassword() {
    }

    public void exit() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-forgot-password.fxml"));
        currentStage.setScene(new Scene(loader.load()));
        AdminForgotPassword forgotPassword = loader.getController();
        forgotPassword.setCurrentStage(currentStage);
    }
}
