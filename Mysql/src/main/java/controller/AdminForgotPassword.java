package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
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
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    @FXML
    public void exit() {
        currentStage.close();
    }

    @FXML
    public void search() throws IOException {
        String searchText = searchTextField.getText();
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost request = new HttpPost("http://localhost:8080/Server_war_exploded/search");
            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("search", searchText));
            request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

            try (CloseableHttpResponse response = client.execute(request);
                 Scanner sc = new Scanner(new BufferedInputStream(response.getEntity().getContent()))
            ) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new ConnectException("Page search not found for URL" + request.getURI());
                }
                if (Boolean.parseBoolean(sc.nextLine())){
                    System.out.println("success");
                } else{
                    error.setVisible(true);
                }
            } catch (ConnectException e) {
                error(e);
            }
        }
    }

    private void error(Throwable e) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.initOwner(root.getScene().getWindow());
        error.setTitle("Server problem!");
        error.setHeaderText("error encountered while connecting !");
        error.setContentText(e.getMessage());
        error.showAndWait();
    }
}
