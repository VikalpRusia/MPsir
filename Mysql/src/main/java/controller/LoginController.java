package controller;

import extra.ImagesLink;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
                        break;
                    case 1:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
