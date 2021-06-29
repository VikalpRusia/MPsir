package main;

import controller.LoginController;
import controller.MainController;
import extra.HostServicesProvider;
import extra.ImagesLink;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import logger.ProjectLogger;
import model.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class Main extends Application {
    private Database database;
    private Logger logger;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        System.setProperty("java.util.logging.config.file",
                getClass().getResource("/logging.properties").getPath());
        logger = LoggerFactory.getLogger(ProjectLogger.class);
        logger.atTrace().log("Logger created ", logger);
    }

    @Override
    public void start(Stage primaryStage) {
        logger.atInfo().log("Starting Application");
        primaryStage.getIcons().add(new Image(ImagesLink.icon));
        logger.atTrace().addArgument(ImagesLink.icon).log("Icon loaded Successfully in main window {}");
        login(primaryStage);
    }

    @Override
    public void stop() {
        logger.atInfo().log("Stopping application");
        try {
            super.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (database != null) {
                database.close();
                database=null;
                logger.atInfo().log("Closing database");
            }
        } catch (SQLException e) {
            logger.atError().log("Database is not closing");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database down");
            alert.setHeaderText("Oops! Database connectivity failed");
            alert.setContentText("Error logged! please try again later\nData lost!");
            alert.showAndWait();
        }
        logger.atDebug().log("Application stopped");
        System.exit(0);
    }

    public void login(Stage primaryStage) {
        HostServicesProvider.INSTANCE.init(getHostServices());
        logger.atTrace().log("Host Service initialise");
        logger.atInfo().log("Attempt login");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(
                new Image(ImagesLink.icon)
        );
        logger.atTrace().log("Icon loaded in login window");
        dialog.setTitle("Login Page!");
        try {
            dialog.setDialogPane(loader.load());
            logger.atDebug().log("Loaded login FXML");
            LoginController controller = loader.getController();
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                initialiseDatabase(controller.getUsername(), controller.getPassword());
                logger.atInfo().log("Connected Successfully");
                display(primaryStage);
            }
        } catch (IOException e) {
            logger.atError().addArgument(e).log("Cannot find Login Page {}");
            e.printStackTrace();
        }
    }

    public void display(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainView.fxml"));
        primaryStage.setTitle("MY WORKBENCH");
        try {
            primaryStage.setScene(new Scene(loader.load()));
            logger.atTrace().log("Loaded display fxml");
            MainController controller = loader.getController();
            controller.setDatabase(database);
            controller.initData();
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());
            logger.atTrace().log("Size increased");
            primaryStage.setMaximized(true);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialiseDatabase(String username, String password) {
        logger.atInfo().log("Attempting to connect to database");
        try {
            database = new Database(username, password);
        } catch (SQLException e) {
            logger.atError().addArgument(e).log("Password or username incorrect {}");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database not responsive");
            alert.setHeaderText("Error logged! please try again later");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            stop();
        }
    }
}
