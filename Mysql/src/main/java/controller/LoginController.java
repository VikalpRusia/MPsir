package controller;

import extra.ImagesLink;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logger.ProjectLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    private final Logger logger = LoggerFactory.getLogger(ProjectLogger.class);
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private DialogPane dialogPane;
    @FXML
    private CheckBox isPasswordVisible;
    @FXML
    private TextField visiblePassword;

    public void initialize() {
        password.textProperty().bindBidirectional(visiblePassword.textProperty());
        logger.atDebug().log("Initialisation started for {}", getClass().getName());
        ButtonType login = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(
                login,
                ButtonType.CANCEL
        );
        Button okBtn = (Button) dialogPane.lookupButton(dialogPane.getButtonTypes().get(0));
        okBtn.setStyle("-fx-background-color: royalblue;-fx-text-fill: white");
        Platform.runLater(() -> username.requestFocus());
        Node loginButton = dialogPane.lookupButton(login);
        loginButton.setDisable(true);
        username.textProperty().addListener((observableValue, s, t1) -> verify(loginButton));
        password.textProperty().addListener((observableValue, s, t1) -> verify(loginButton));
        logger.atInfo().log("Initialized successfully {}", getClass().getName());
        password.visibleProperty().bind(isPasswordVisible.selectedProperty().not());
        visiblePassword.visibleProperty().bind(isPasswordVisible.selectedProperty());
    }

    private void verify(Node loginButton) {
        loginButton.setDisable(username.getText().trim().isEmpty() || password.getText().isEmpty());
    }

    public String getUsername() {
        return username.getText();
    }

    public String getPassword() {
        return password.getText();
    }

    public void forgetPassword() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forgotPassword.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Forgot password?");
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(
                new Image(ImagesLink.icon)
        );
        try {
            dialog.setDialogPane(loader.load());
            ForgotPassword forgotPassword = loader.getController();
            dialog.initOwner(dialogPane.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int choice = forgotPassword.selectedAccount();
                switch (choice) {
                    case 0:
                        FXMLLoader loader1 = new FXMLLoader(getClass().getResource(
                                "/fxml/admin-forgot-password.fxml"
                        ));
                        Stage stage = new Stage();
                        stage.setScene(new Scene(loader1.load()));
                        stage.getIcons().add(
                                new Image(ImagesLink.icon)
                        );
                        stage.setTitle("admin account: reset password");
                        stage.initOwner(dialogPane.getScene().getWindow());
                        stage.initModality(Modality.APPLICATION_MODAL);
                        AdminForgotPassword adminForgotPassword = loader1.getController();
                        adminForgotPassword.setCurrentStage(stage);
                        stage.showAndWait();
                        break;
                    case 1:
                        FXMLLoader loader2 = new FXMLLoader(getClass().getResource(
                                "/fxml/non-admin-forgot-password.fxml"
                        ));
                        Stage stage1 = new Stage();
                        stage1.setScene(new Scene(loader2.load()));
                        stage1.getIcons().add(
                                new Image(ImagesLink.icon)
                        );
                        stage1.setTitle("non-admin account: reset password");
                        stage1.initOwner(dialogPane.getScene().getWindow());
                        stage1.initModality(Modality.APPLICATION_MODAL);
                        NonAdminForgotPassword nonAdminForgotPassword = loader2.getController();
                        nonAdminForgotPassword.setCurrentStage(stage1);
                        stage1.showAndWait();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
