package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class DescController {
    @FXML
    private ToggleGroup view;

    @FXML
    private TextFlow textFlow;

    public void initialize(){
        view.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            if (t1 == null) {
                toggle.setSelected(true);
                return;
            }
            ToggleButton t = (ToggleButton) observableValue.getValue();
            t.getOnAction().handle(new ActionEvent());
        });
        Platform.runLater(this::about);
    }
    public void about(){
        textFlow.getChildren().clear();
        Text text = new Text("This is project is architected,designed and implemented by ");
        text.setFont(Font.font("Comic Sans MS",18));
        Text author = new Text("Vikalp Rusia");
        author.setFont(Font.font("Comic Sans MS", FontWeight.BOLD,20));
        textFlow.getChildren().addAll(text,author);
    }
    public void contact(){
        textFlow.getChildren().clear();
        Text text = new Text("Mail your valuable Suggestions at: ");
        text.setFont(Font.font("Comic Sans MS",18));
        Text mail = new Text("18bcs163@ietdavv.edu.in");
        mail.setFont(Font.font("Comic Sans MS",FontWeight.BOLD,20));
        textFlow.getChildren().addAll(text,mail);
    }
}
