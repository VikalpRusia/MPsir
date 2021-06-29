package controller;

import controller.serviceProvider.Services;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.util.Optional;

public class ShowUsers {
    @FXML
    private TableColumn<String, String> users;
    @FXML
    private TableView<String> tableView;

    private Services.CreateUser createUser;
    private Services.DropUser dropUser;
    private Window window;

    public void initialize() {
        Platform.runLater(() -> window = tableView.getScene().getWindow());

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
            tableView.getItems().add(createUser.getNewUserDetails().getValue());

        });

        //dropUser
        dropUser = new Services.DropUser();
        dropUser.setOnFailed(workerStateEvent ->
                MainController.alertShow(dropUser.getException(), window));
        dropUser.setOnSucceeded(workerStateEvent -> {
            Notifications notificationBuilder = Notifications.create()
                    .title("User Dropped")
                    .text("'" + dropUser.getUser_toBe_dropped() + "' Successfully Dropped")
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.BOTTOM_RIGHT)
                    .darkStyle();
            notificationBuilder.show();
            tableView.getItems().remove(tableView.getSelectionModel().getSelectedIndex());

        });

        users.setCellValueFactory(stringStringCellDataFeatures ->
                new SimpleStringProperty(stringStringCellDataFeatures.getValue()));

        MenuItem addUser = new MenuItem("Add User");
        addUser.setOnAction(e -> createUser());
        MenuItem grantPrivileges = new MenuItem("Grant Privileges");
        grantPrivileges.setOnAction(e -> grantPrivileges());
        MenuItem dropUser = new MenuItem("Drop User");
        dropUser.setOnAction(e -> dropUser());
        ContextMenu completeMenu = new ContextMenu(addUser, grantPrivileges, dropUser);
        addUser = new MenuItem("Add User");
        addUser.setOnAction(e -> createUser());
        ContextMenu incompleteMenu = new ContextMenu(addUser);

        tableView.setContextMenu(completeMenu);
        users.setCellFactory(new Callback<>() {
            @Override
            public TableCell<String, String> call(TableColumn<String, String> stringStringTableColumn) {
                return new TableCell<>() {
                    @Override
                    public void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        if (empty) {
                            setText(null);
                            setContextMenu(incompleteMenu);
                        } else {
                            setText(s);
                            setContextMenu(completeMenu);
                        }
                    }
                };
            }
        });
    }


    public void setUsers(ObservableList<String> strings) {
        tableView.setItems(strings);
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
            MainController.alertShow(e, window);
        }
    }

    public void dropUser() {
        String user_to_drop = tableView.getSelectionModel().getSelectedItem();
        dropUser.setUser_toBe_dropped(user_to_drop);
        MainController.startService(dropUser);
    }

    public void grantPrivileges() {

    }

    public void checkKey(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)){
            window.hide();
        }
    }
}
