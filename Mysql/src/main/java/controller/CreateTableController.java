package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logger.ProjectLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreateTableController {
    private final Logger logger = LoggerFactory.getLogger(ProjectLogger.class);
    private ObservableList<AddColumnInTable> tableViewData;
    private ObservableList<String> notUnique;
    private ObservableList<String> tablePresentName;
    private Map<String, ObservableList<String>> uniqueness;
    //for deletion
    private Map<String, ToggleButton> toggleButtonMap;

    @FXML
    private TextField tableName;

    @FXML
    private TableColumn<AddColumnInTable, String> primaryKey;

    @FXML
    private TableColumn<AddColumnInTable, String> columnNameColumn;

    @FXML
    private TableColumn<AddColumnInTable, String> columnTypeColumn;

    @FXML
    private TableColumn<AddColumnInTable, String> foreignKey;

    @FXML
    private TableColumn<AddColumnInTable, String> uniqueKey;

    @FXML
    private TableColumn<AddColumnInTable, String> notNull;

    @FXML
    private TableColumn<AddColumnInTable, String> autoIncrement;

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TableView<AddColumnInTable> mainTableView;

    @FXML
    private ImageView additionImage;

    @FXML
    private ImageView minusImage;

    @FXML
    private ImageView additionUniqueImage;

    @FXML
    private ImageView minusUniqueImage;

    @FXML
    private VBox toggleButtonVBox;

    @FXML
    private ToggleGroup view;

    @FXML
    private ListView<String> uniquenessTogether;

    @FXML
    private SplitPane splitPane;

    @FXML
    private VBox enterText;

    @FXML
    private ComboBox<String> columnsSelection;

    public void initialize() {
        logger.atDebug().log("Started initialise");
        uniqueness = new HashMap<>();
        toggleButtonMap = new HashMap<>();
        notUnique = FXCollections.observableArrayList();
        tableViewData = FXCollections.observableArrayList();
        ButtonType create = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(
                create,
                ButtonType.CANCEL
        );
        Node createButton = dialogPane.lookupButton(create);
        createButton.setDisable(true);
        tableName.textProperty().addListener((observableValue, s, t1) -> verify(createButton));
        tableViewData.addListener((ListChangeListener<AddColumnInTable>) change -> verify(createButton));
        logger.atTrace().log("Buttons set");

        //binding
        columnsSelection.setItems(notUnique);
        logger.atTrace().log("Not Unique bind to column Selection");

        view.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            if (t1 == null) {
                toggle.setSelected(true);
                return;
            }
            ToggleButton t = (ToggleButton) observableValue.getValue();
            t.getOnAction().handle(new ActionEvent());
        });
        Tooltip tooltipAdd = new Tooltip("Add column");
        Tooltip.install(additionImage, tooltipAdd);
        Tooltip tooltipSubtract = new Tooltip("Delete column");
        Tooltip.install(minusImage, tooltipSubtract);

        Tooltip tooltipAddUnique = new Tooltip("Add Unique Column Name");
        Tooltip.install(additionUniqueImage, tooltipAddUnique);
        Tooltip tooltipSubUnique = new Tooltip("Subtract Unique Column Name");
        Tooltip.install(minusUniqueImage, tooltipSubUnique);
        logger.atDebug().log("Tool tip set");

        columnNameColumn.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getColumnName()));
        columnTypeColumn.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getColumnType()));
        primaryKey.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().isPrimaryKey().toString()));
        foreignKey.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getForeignKey()));
        uniqueKey.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getUnique().toString()));
        notNull.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getNotNull().toString()));
        autoIncrement.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getAutoIncrement().toString()));
        mainTableView.setItems(tableViewData);
        logger.atDebug().log("Configured cell value Factory");

        Platform.runLater(() -> splitPane.setDividerPositions(0.29797979797979796));
        logger.atInfo().log("Completed initialisation");
    }

    private void verify(Node submitButton) {
        submitButton.setDisable(tableName.getText().trim().isEmpty() || tableViewData.size() == 0);
    }

    @FXML
    public void addImageClick() throws IOException {
        logger.atInfo().log("Add column image pressed");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTableAddColumn.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add column in Table");
        dialog.initOwner(dialogPane.getScene().getWindow());
        dialog.setDialogPane(loader.load());
        logger.atDebug().log("Loaded fxml");
        AddColumnController controller = loader.getController();
        controller.setTable(tablePresentName);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            String name = controller.getColumnName();
            String type = controller.getColumnType();
            boolean isPrimary = controller.getPrimaryKey();
            boolean isUnique = controller.getUniqueKey();
            boolean isNotNull = controller.getNotNull();
            boolean autoIncrement = controller.getAutoIncrement();
            String foreignKey = controller.getForeignKey();
            if (type == null || name.equals("") || type.equals("")) {
                logger.atWarn().log("All fields necessary");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("All Fields were necessary");
                alert.setContentText("No fields modified");
                alert.initOwner(dialogPane.getScene().getWindow());
                alert.showAndWait();
                return;
            } else if (isPrimary && isUnique) {
                logger.atWarn().log("Remove uniqueness for primary Key");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Unique is by default for primary");
                alert.setContentText("Remove uniqueness !");
                alert.initOwner(dialogPane.getScene().getWindow());
                alert.showAndWait();
                return;
            }
            if (isUnique) {
                logger.atDebug().log("Unique key");
                ObservableList<String> list = FXCollections.observableArrayList();
                list.add(name);
                uniqueness.put(name, list);
                ToggleButton toggleButton = new ToggleButton(name);
                toggleButton.setToggleGroup(view);
                toggleButton.setOnAction(actionEvent -> toggleChange(name));
                if (toggleButtonVBox.getChildren().size() == 1) {
                    toggleButton.setSelected(true);
                }
                toggleButton.prefWidthProperty().bind(toggleButtonVBox.widthProperty());
                toggleButtonVBox.getChildren().add(toggleButton);
                toggleButtonMap.put(name, toggleButton);
            } else {
                notUnique.add(name);
            }
            tableViewData.add(new AddColumnInTable(name, type, isPrimary, foreignKey, isUnique, isNotNull, autoIncrement));
            mainTableView.getSelectionModel().select(0);
        }
    }

    public void toggleChange(String name) {
        logger.atInfo().addArgument(name).log("{} toggled");
        uniquenessTogether.setItems(uniqueness.get(name));
    }

    @FXML
    public void minusImageClick() {
        logger.atInfo().log("minus column tab pressed");
        if (mainTableView.getSelectionModel().getSelectedItem() == null) {
            logger.atWarn().log("No item selected or no entry present to be selected");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No Entry!");
            alert.setContentText("Enter table column before deleting them!");
            alert.showAndWait();
            return;
        } else if (mainTableView.getSelectionModel().getSelectedItem().getUnique()) {
            String name = mainTableView.getSelectionModel().getSelectedItem().getColumnName();
            uniqueness.get(name).clear();
            uniqueness.remove(name);
            toggleButtonVBox.getChildren().remove(toggleButtonMap.get(name));
            view.getToggles().remove(toggleButtonMap.get(name));
            if (view.getToggles().size() > 1) {
                view.getToggles().get(0).setSelected(true);
            }
        } else {
            String name = mainTableView.getSelectionModel().getSelectedItem().getColumnName();
            for (ObservableList<String> entry : uniqueness.values()) {
                entry.remove(name);
            }
            notUnique.remove(mainTableView.getSelectionModel().getSelectedItem().getColumnName());
        }
        tableViewData.remove(
                mainTableView.getSelectionModel().getSelectedItem()
        );
    }

    public String getTableName() {
        return tableName.getText();
    }

    public void setTableName(ObservableList<String> items) {
        tablePresentName = items;
    }

    public List<String> getColumnsName() {
        return tableViewData.stream().map(AddColumnInTable::getColumnName).collect(Collectors.toList());
    }

    public List<String> getColumnsType() {
        return tableViewData.stream().map(AddColumnInTable::getColumnType).collect(Collectors.toList());
    }

    public List<String> getPrimaryKeys() {
        return tableViewData
                .stream()
                .filter(AddColumnInTable::isPrimaryKey)
                .map(AddColumnInTable::getColumnName)
                .collect(Collectors.toList());
    }

    public List<String> getUniqueKeys() {
        return uniqueness.keySet().stream().
                filter(s -> uniqueness.get(s).size() == 1)
                .collect(Collectors.toList());
    }

    public List<List<String>> getUniqueTogether() {
        return uniqueness.values().stream().
                filter(s -> s.size() != 1)
                .collect(Collectors.toList());
    }

    public Map<String, String> getForeignKeys() {
        Map<String, String> map = new HashMap<>();
        for (AddColumnInTable middle : tableViewData) {
            if (!middle.getForeignKey().equals("false")) {
                map.put(middle.getColumnName(), middle.getForeignKey());
            }
        }
        return map;
    }

    public List<String> getNotNull() {
        return tableViewData
                .stream()
                .filter(AddColumnInTable::getNotNull)
                .map(AddColumnInTable::getColumnName)
                .collect(Collectors.toList());
    }

    public List<String> getAutoIncrement() {
        return tableViewData
                .stream()
                .filter(AddColumnInTable::getAutoIncrement)
                .map(AddColumnInTable::getColumnName)
                .collect(Collectors.toList());
    }

    public void minusUniqueClick() {
        logger.atInfo().log("minus index pressed");
        if (uniqueKeyPossible()) {
            return;
        }
        String toBeRemoved = uniquenessTogether.getSelectionModel().getSelectedItem();
        String removedFrom = ((ToggleButton) view.getSelectedToggle()).getText();
        if (toBeRemoved == null || toBeRemoved.equals(removedFrom)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error !");
            alert.setContentText("Cannot delete main element or focus is somewhere else !");
            alert.initOwner(splitPane.getScene().getWindow());
            alert.showAndWait();
            return;
        }
        uniqueness.get(removedFrom).remove(toBeRemoved);
    }

    private boolean uniqueKeyPossible() {
        if (toggleButtonMap.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error !");
            alert.setContentText("No Unique Key found !");
            alert.initOwner(splitPane.getScene().getWindow());
            alert.showAndWait();
            return true;
        }
        return false;
    }

    public void addUniqueClick() {
        logger.atInfo().log("add unique key");
        if (uniqueKeyPossible()) {
            return;
        }
        if (notUnique.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error !");
            alert.setContentText("Cannot Add element since there are no element!");
            alert.initOwner(splitPane.getScene().getWindow());
            alert.showAndWait();
            return;
        }
        enterText.setVisible(true);
        columnsSelection.getSelectionModel().select(0);
    }

    public void submit() {
        logger.atInfo().log("Submit button pressed");
        String selected = ((ToggleButton) view.getSelectedToggle()).getText();
        enterText.setVisible(false);
        if (uniqueness.get(selected).contains(columnsSelection.getValue())) {
            return;
        }
        uniqueness.get(selected).add(columnsSelection.getValue());
    }

    static class AddColumnInTable {
        private final String columnName;
        private final String columnType;
        private final Boolean primaryKey;
        private final String foreignKey;
        private final Boolean isUnique;
        private final Boolean isNotNull;
        private final Boolean isAutoIncrement;

        public AddColumnInTable(String columnName, String columnType, boolean primaryKey, String foreignKey,
                                boolean isUnique, boolean isNotNull, boolean isAutoIncrement) {
            this.columnName = columnName;
            this.columnType = columnType;
            this.primaryKey = primaryKey;
            this.foreignKey = foreignKey;
            this.isUnique = isUnique;
            this.isNotNull = isNotNull;
            this.isAutoIncrement = isAutoIncrement;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getColumnType() {
            return columnType;
        }

        public Boolean isPrimaryKey() {
            return primaryKey;
        }

        public String getForeignKey() {
            return foreignKey;
        }

        public Boolean getUnique() {
            return isUnique;
        }

        public Boolean getNotNull() {
            return isNotNull;
        }

        public Boolean getAutoIncrement() {
            return isAutoIncrement;
        }
    }
}
