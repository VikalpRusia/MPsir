package controller;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Pair;

public class NewUserController {

    @FXML
    public PasswordField password;
    @FXML
    public TextField visiblePassword;
    @FXML
    private DialogPane dialogPane;
    @FXML
    private TextField userName;
    @FXML
    private CheckBox isPasswordVisible;

    public void initialize() {
        ButtonType create = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);

        dialogPane.getButtonTypes().addAll(
                create, ButtonType.CANCEL
        );
        Button okBtn = (Button) dialogPane.lookupButton(dialogPane.getButtonTypes().get(0));
        okBtn.setDisable(true);
        okBtn.setStyle("-fx-background-color: royalblue;-fx-text-fill: white");

        Platform.runLater(() -> userName.requestFocus());

        userName.textProperty().addListener((observableValue, s, t1) -> verify(okBtn));
        password.textProperty().addListener((observableValue, s, t1) -> verify(okBtn));
        password.textProperty().bindBidirectional(visiblePassword.textProperty());
        visiblePassword.visibleProperty().bind(isPasswordVisible.selectedProperty());
        password.visibleProperty().bind(isPasswordVisible.selectedProperty().not());
    }

    private void verify(Node loginButton) {
        loginButton.setDisable(userName.getText().trim().isEmpty() || password.getText().isEmpty());
    }

    public Pair<String, String> getUserDetail() {
        return new Pair<>(userName.getText(), password.getText());
    }

}
