package controller;

import extra.HostServicesProvider;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import logger.ProjectLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AboutController {

    private final ObjectProperty<Font> importantPart = new SimpleObjectProperty<>(
            Font.font("Comic Sans MS", FontWeight.BOLD, 20));
    private final ObjectProperty<Font> heading = new SimpleObjectProperty<>(
            Font.font("Comic Sans MS", 18));

    private final Logger logger = LoggerFactory.getLogger(ProjectLogger.class);

    @FXML
    private ToggleGroup view;
    @FXML
    private TextFlow textFlow;

    public void initialize() {
        view.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            if (t1 == null) {
                toggle.setSelected(true);
                return;
            }
            ToggleButton t = (ToggleButton) observableValue.getValue();
            t.getOnAction().handle(new ActionEvent());
        });
        Platform.runLater(this::about);
        logger.atInfo().log("Initialisation completed of {}", getClass().getName());
    }

    public void about() {
        textFlow.getChildren().clear();

        Text text = new Text("This project is architected,designed and implemented by ");
        text.fontProperty().bind(heading);

        Text author = new Text("Vikalp Rusia");
        author.fontProperty().bind(importantPart);

        textFlow.getChildren().addAll(text, author);
    }

    public void contact() {
        logger.atInfo().log("Called contact toggle");
        textFlow.getChildren().clear();

        Text text = new Text("Mail your valuable Suggestions at: ");
        text.fontProperty().bind(heading);

        Hyperlink mail = new Hyperlink("18bcs163@ietdavv.edu.in");
        mail.fontProperty().bind(importantPart);
        mail.setOnAction(actionEvent -> HostServicesProvider.INSTANCE.getHostServices()
                .showDocument("mailto:18bcs163@ietdavv.edu.in?subject=Feedback"));

        textFlow.getChildren().addAll(text, mail);
    }

    public void development() {
        logger.atInfo().log("Called development toggle");
        textFlow.getChildren().clear();

        Text text = new Text("GitHub link of this Project: ");
        text.fontProperty().bind(heading);
        logger.atTrace().log("Bound property of text to heading font");

        Hyperlink gitLink = new Hyperlink("https://github.com/VikalpRusia/MPsir");
        gitLink.fontProperty().bind(importantPart);
        logger.atTrace().log("Bound property of gitLink to important font");
        gitLink.setOnAction(actionEvent -> {
            logger.atInfo().log("Called gitLink hyperlink");
            HostServicesProvider.INSTANCE.getHostServices()
                    .showDocument("https://github.com/VikalpRusia/MPsir");
        });
        logger.atTrace().log("HyperLink set to open gitLink in browser");

        Text endingText = new Text("\n\nHelp in the further development of this project :)");
        endingText.fontProperty().bind(importantPart);
        logger.atTrace().log("Bound ending text to important Font");

        textFlow.getChildren().addAll(text, gitLink, endingText);
        logger.atDebug().log("Child's added to text flow");
    }
}
