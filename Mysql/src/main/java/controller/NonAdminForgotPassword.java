package controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Scanner;

public class NonAdminForgotPassword {

    private final RequestingAdminViaMail requestingAdminViaMail = new RequestingAdminViaMail();
    private Stage currentStage;

    @FXML
    private Button sendingRequest;
    @FXML
    private PasswordField invisiblePassword;
    @FXML
    private TextField visiblePassword;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private CheckBox isPasswordVisible;
    @FXML
    private VBox error;
    @FXML
    private AnchorPane root;
    @FXML
    private TextField userName;

    public void initialize() {
        invisiblePassword.textProperty().bindBidirectional(visiblePassword.textProperty());
        invisiblePassword.visibleProperty().bind(isPasswordVisible.selectedProperty().not());
        visiblePassword.visibleProperty().bind(isPasswordVisible.selectedProperty());

        error.managedProperty().bind(error.visibleProperty());
        Platform.runLater(() -> root.requestFocus());
        requestingAdminViaMail.setOnFailed(workerStateEvent ->
                error(requestingAdminViaMail.getException()));
        sendingRequest.setDisable(true);
        userName.textProperty().addListener((observableValue, s, t1) -> verify());
        confirmPassword.textProperty().addListener((observableValue, s, t1) -> verify());
        invisiblePassword.textProperty().addListener((observableValue, s, t1) -> verify());
    }

    private void verify() {
        sendingRequest.setDisable(
                userName.getText().equals("") || confirmPassword.getText().equals("")
                        || invisiblePassword.getText().equals(""));
    }

    public void sendRequest() {
        if (!confirmPassword.getText().equals(invisiblePassword.getText())) {
            error.setVisible(true);
            return;
        }
        requestingAdminViaMail.setUserName(userName.getText());
        requestingAdminViaMail.setPassword(confirmPassword.getText());
        if (requestingAdminViaMail.getState() == Worker.State.READY) {
            requestingAdminViaMail.start();
        } else if (requestingAdminViaMail.getState() == Worker.State.SUCCEEDED
                || requestingAdminViaMail.getState() == Worker.State.FAILED
        ) {
            requestingAdminViaMail.restart();
        }
        exit();
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    private void error(Throwable e) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.initOwner(currentStage);
        error.setTitle("Server problem!");
        error.setHeaderText("error encountered while connecting !");
        error.setContentText(e.getMessage());
        error.showAndWait();
    }

    public void exit() {
        currentStage.close();
    }

    private static class RequestingAdminViaMail extends Service<Void> {
        private String userName;
        private String password;

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try (CloseableHttpClient client = HttpClients.createDefault()) {

                        HttpPost request = new HttpPost("http://localhost:8080/Server_war_exploded/non-admin");
                        ArrayList<NameValuePair> postParameters = new ArrayList<>();
                        postParameters.add(new BasicNameValuePair("name", userName));
                        postParameters.add(new BasicNameValuePair("UUID", getUUID()));
                        postParameters.add(new BasicNameValuePair("password", password));
                        request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

                        try (CloseableHttpResponse response = client.execute(request)) {
                            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                                throw new ConnectException("Page search not found for URL" + request.getURI());
                            }
                        }
                    }
                    return null;
                }

                private String getUUID() throws IOException {
                    String command = "wmic csproduct get UUID";
                    ProcessBuilder builder = new ProcessBuilder(
                            "powershell.exe", command
                    );
                    Process p = builder.start();
                    p.getOutputStream().close();
                    try (Scanner sc = new Scanner(new BufferedInputStream(p.getInputStream()))) {
                        sc.next();
                        return sc.next();
                    }
                }
            };
        }
    }
}
