package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
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

public class AuthAdminShowPassword {

    private final SimpleStringProperty userNameStr = new SimpleStringProperty();
    private final SimpleStringProperty searchDataStr = new SimpleStringProperty();
    private final Mailing mailing = new AuthAdminShowPassword.Mailing();
    private Stage currentStage;
    @FXML
    private Text userName;
    @FXML
    private Text searchData;

    public void initialize() {
        userName.textProperty().bind(userNameStr);
        searchData.textProperty().bind(searchDataStr);
        mailing.setOnFailed(workerStateEvent ->
                error(mailing.getException()));
    }


    public void setUserNameStr(String userNameStr) {
        this.userNameStr.setValue(userNameStr + "\n");
    }

    public void setSearchDataStr(String searchDataStr) {
        this.searchDataStr.setValue(searchDataStr);
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void resetPassword() {
        mailing.setSearchDataStr(searchDataStr.getValue().strip());
        if (mailing.getState() == Worker.State.READY) {
            mailing.start();
        } else if (mailing.getState() == Worker.State.SUCCEEDED
                || mailing.getState() == Worker.State.FAILED
        ) {
            mailing.restart();
        }
        currentStage.close();
    }

    public void exit() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-forgot-password.fxml"));
        currentStage.setScene(new Scene(loader.load()));
        AdminForgotPassword forgotPassword = loader.getController();
        forgotPassword.setCurrentStage(currentStage);
    }

    private void error(Throwable e) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.initOwner(currentStage);
        error.setTitle("Server problem!");
        error.setHeaderText("error encountered while connecting !");
        error.setContentText(e.getMessage());
        error.showAndWait();
    }

    private static class Mailing extends Service<Void> {
        private String searchDataStr;

        public void setSearchDataStr(String searchDataStr) {
            this.searchDataStr = searchDataStr;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost request = new HttpPost("http://localhost:8080/Server_war_exploded/showPassword");
                        ArrayList<NameValuePair> postParameters = new ArrayList<>();
                        postParameters.add(new BasicNameValuePair("search", searchDataStr));
                        request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
                        try (CloseableHttpResponse response = client.execute(request);
                             Scanner sc = new Scanner(new BufferedInputStream(response.getEntity().getContent()))
                        ) {
                            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                                throw new ConnectException("Page search not found for URL" + request.getURI());
                            }
                            while (sc.hasNextLine()) {
                                System.out.println(sc.nextLine());
                            }
                        }
                    }
                    return null;
                }
            };
        }
    }
}
