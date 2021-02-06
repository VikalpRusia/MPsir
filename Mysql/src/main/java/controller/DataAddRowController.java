package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.util.Callback;
import javafx.util.StringConverter;
import logger.ProjectLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class DataAddRowController {
    private final Logger logger = LoggerFactory.getLogger(ProjectLogger.class);

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TableView<ObservableList<Object>> dataTable;


    public void initialize() {
        logger.atDebug().log("Initialising data add row controller");
        dialogPane.getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL
        );
        logger.atTrace().log("Created buttons");
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        dialogPane.setPrefWidth(bounds.getWidth() - 100);
        logger.atTrace().log("width set to {}", bounds.getWidth() - 100);
    }

    public void setColumnsList(Set<String> columnsList) {
        ObservableList<ObservableList<Object>> item = FXCollections.observableArrayList();
        ObservableList<Object> sample = FXCollections.observableArrayList();
        item.add(sample);

        for (String name : columnsList) {
            TableColumn<ObservableList<Object>, String> tableColumn = new TableColumn<>(name);
            dataTable.getColumns().add(tableColumn);
            sample.add(null);
            tableColumn.setCellFactory(new Callback<>() {
                @Override
                public TableCell<ObservableList<Object>, String> call(TableColumn<ObservableList<Object>, String> observableListStringTableColumn) {
                    StringConverter<String> stringConverter = new StringConverter<>() {
                        @Override
                        public String toString(String s) {
                            return s;
                        }

                        @Override
                        public String fromString(String s) {
                            return s;
                        }
                    };
                    return new TextFieldTableCell<>(stringConverter) {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setFont(Font.font(15));
                            if (empty) {
                                setText(null);
                                setEditable(false);
                            } else if (item == null) {
                                setText("<Null>");
                                setTextFill(Color.PURPLE);
                                setEditable(true);
                            } else {
                                setText(item);
                                setTextFill(Color.BLACK);
                                setEditable(true);
                            }
                        }
                    };
                }
            });
            tableColumn.setReorderable(false);
            tableColumn.setOnEditCommit(t -> {
                TablePosition<ObservableList<Object>, String> tablePosition = t.getTablePosition();
                t.getRowValue().set(tablePosition.getColumn(), t.getNewValue());
//                System.out.println(t.getRowValue());
            });
        }
        logger.atDebug().log("Column's arranged");
        dataTable.setItems(item);
    }

    public ObservableList<Object> values() {
        return dataTable.getItems().get(0);
    }

}
