package main;

import controller.Controller;
import controller.DialogController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class Main extends Application {
    Database database;

    @Override
    public void start(Stage primaryStage) throws Exception {
        login(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        try {
            if (database != null) {
                database.close();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database down");
            alert.setHeaderText("Oops! Database connectivity failed");
            alert.setContentText("Error logged! please try again later\nData lost!");
            alert.showAndWait();
        }
        Platform.exit();
        System.exit(0);
    }
    public void login(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(loader.load());
        DialogController controller = loader.getController();
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get()==ButtonType.OK){
            initialiseDatabase(controller.getUsername(), controller.getPassword());
            display(primaryStage);
        }

    }
    public void display(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sample.fxml"));
        primaryStage.setTitle("MYSQL WORKBENCH");
        primaryStage.setScene(new Scene(loader.load()));
        Controller controller = loader.getController();
        controller.initData(database);
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMaximized(true);

        primaryStage.show();

    }
    public void initialiseDatabase(String username,String password) throws Exception {
        try {
            database = new Database(username,password);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database not responsive");
            alert.setHeaderText("Error logged! please try again later");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
