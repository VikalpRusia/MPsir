package controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
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

public class AdminForgotPassword {

    private final Searching searching = new Searching();
    private Stage currentStage;
    @FXML
    private AnchorPane root;
    @FXML
    private TextField searchTextField;
    @FXML
    private VBox error;

    public void initialize() {
        Platform.runLater(() -> root.requestFocus());
        error.managedProperty().bind(error.visibleProperty());
        searching.setOnFailed(workerStateEvent ->
                error(searching.getException()));
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    @FXML
    public void exit() {
        currentStage.close();
    }

    @FXML
    public void search() {
        String searchText = searchTextField.getText();
        searching.setSearchText(searchText);
        searching.setOnSucceeded(workerStateEvent -> {
            try (CloseableHttpResponse response = searching.getValue();
                 Scanner sc = new Scanner(new BufferedInputStream(response.getEntity().getContent()))
            ) {
                if (Boolean.parseBoolean(sc.nextLine())) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/authAdminShowPassword.fxml"));
                    try {
                        currentStage.setScene(new Scene(fxmlLoader.load()));
                    } catch (IOException e) {
                        error(e);
                    }
                    AuthAdminShowPassword resetPassword = fxmlLoader.getController();
                    resetPassword.setUserNameStr(sc.nextLine());
                    resetPassword.setSearchDataStr(searchText);
                    resetPassword.setCurrentStage(currentStage);
                } else {
                    error.setVisible(true);
                }
            } catch (IOException e){
                error(e);
            }
        });
        if (searching.getState() == Worker.State.READY) {
            searching.start();
        } else if (searching.getState() == Worker.State.SUCCEEDED
                || searching.getState() == Worker.State.FAILED
        ) {
            searching.restart();
        }
    }

    private void error(Throwable e) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.initOwner(currentStage);
        error.setTitle("Server problem!");
        error.setHeaderText("error encountered while connecting !");
        error.setContentText(e.getMessage());
        error.showAndWait();
    }

    private static class Searching extends Service<CloseableHttpResponse> {
        private String searchText;

        public void setSearchText(String searchText) {
            this.searchText = searchText;
        }

        @Override
        protected Task<CloseableHttpResponse> createTask() {
            return new Task<>() {
                @Override
                protected CloseableHttpResponse call() throws Exception {
                    try (CloseableHttpClient client = HttpClients.createDefault()) {

                        HttpPost request = new HttpPost("http://localhost:8080/Server_war_exploded/search");
                        ArrayList<NameValuePair> postParameters = new ArrayList<>();
                        postParameters.add(new BasicNameValuePair("search", searchText));
                        request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

                        CloseableHttpResponse response = client.execute(request);
                        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                            throw new ConnectException("Page search not found for URL" + request.getURI());
                        }
                        return response;
                    }
                }
            };
        }
    }
}
