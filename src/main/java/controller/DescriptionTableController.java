package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DescriptionTableController {

    private ObservableList<ObservableList<String>> data;

    @FXML
    private TableView<ObservableList<String>> tableView;

    @FXML
    private TableColumn<ObservableList<String>, String> field;
    @FXML
    private TableColumn<ObservableList<String>, String> type;
    @FXML
    private TableColumn<ObservableList<String>, String> nullConstraint;
    @FXML
    private TableColumn<ObservableList<String>, String> key;
    @FXML
    private TableColumn<ObservableList<String>, String> defaultConstraint;
    @FXML
    private TableColumn<ObservableList<String>, String> extra;

    public void initialize() {
        field.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().get(0)));
        type.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().get(1)));
        nullConstraint.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().get(2)));
        key.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().get(3)));
        defaultConstraint.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().get(4)));
        extra.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().get(5)));
        tableView.setItems(data);

        tableView.widthProperty().addListener(observable -> {
            if (data.size() < 15) {
                tableView.setPrefWidth(
                        field.getWidth() +
                                type.getWidth() +
                                nullConstraint.getWidth() +
                                key.getWidth() +
                                defaultConstraint.getWidth() +
                                extra.getWidth() + 5
                );
            } else {
                tableView.setPrefWidth(
                        field.getWidth() +
                                type.getWidth() +
                                nullConstraint.getWidth() +
                                key.getWidth() +
                                defaultConstraint.getWidth() +
                                extra.getWidth() + 18
                );
            }
        });

    }

    public void setData(ObservableList<ObservableList<String>> data) {
        this.data = data;
        tableView.setItems(this.data);
    }
}
