package controller;

import controller.serviceProvider.Services;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.util.Optional;

public class ShowUsers {
    @FXML
    private TableColumn<String,String> users;
    @FXML
    private TableView<String> tableView;

    private Services.CreateUser createUser;
    private Window window;

    public void initialize(){
        Platform.runLater(() ->window = tableView.getScene().getWindow());

        //createUser
        createUser = new Services.CreateUser();
        createUser.setOnFailed(workerStateEvent ->
                MainController.alertShow(createUser.getException(), window));
        createUser.setOnSucceeded(workerStateEvent -> {
            Notifications notificationBuilder = Notifications.create()
                    .title("User Created")
                    .text("'" + createUser.getNewUserDetails().getKey() + "' Successfully Created having password '"
                            + createUser.getNewUserDetails().getValue() + "'")
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.BOTTOM_RIGHT)
                    .darkStyle();
            notificationBuilder.show();

        });
    }


    public void setUsers(ObservableList<String> strings) {
        tableView.setItems(strings);
        users.setCellValueFactory(stringStringCellDataFeatures ->
                new SimpleStringProperty(stringStringCellDataFeatures.getValue()));

        MenuItem dropUser = new MenuItem("Drop User");
        MenuItem addUser = new MenuItem("Add User");
        addUser.setOnAction(e -> createUser());
        ContextMenu contextMenu = new ContextMenu(addUser,dropUser);

        tableView.setContextMenu(contextMenu);
    }

    public void createUser() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/fxml/newUser.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(tableView.getScene().getWindow());
        dialog.setTitle("Creating New User");
        try {
            dialog.setDialogPane(loader.load());
            NewUserController newUserController = loader.getController();
            Optional<ButtonType> response = dialog.showAndWait();
            if (response.isPresent() && response.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
                createUser.setNewUserDetails(newUserController.getUserDetail());
                MainController.startService(createUser);
            }
        } catch (IOException e) {
            MainController.alertShow(e,window);
        }
    }

    public void dropUser(){

    }

}
