package createdNodes;

import controller.serviceProvider.Services;
import javafx.concurrent.Worker;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class SuggestingTextField extends TextField {
    private final SortedSet<String> strings;
    private final ContextMenu popupMenu;
    private final Services.Suggestions suggestionService = new Services.Suggestions();
    private final int count = 5;
    private static boolean textChangedByUser = true;

    public SuggestingTextField() {
        super();
        strings = new TreeSet<>();
        suggestionService.setStrings(strings);
        popupMenu = new ContextMenu();
        textProperty().addListener((observableValue, s, t1) -> {
            int lastIndexOf = t1.lastIndexOf(' ');
            if (t1.length() == 0 || lastIndexOf + 1 == t1.length() || !textChangedByUser) {
                popupMenu.hide();
                textChangedByUser = true;
            } else {
                suggestionService.setBasedOn(t1);
                suggestionService.setSpaceIndex(lastIndexOf);
                suggestionService.setOnSucceeded(workerStateEvent -> {
                    popupMenu.getItems().clear();
                    List<String> suggestion = suggestionService.getValue();
                    for (int i = 0; i < Math.min(count, suggestion.size()); i++) {
                        String result = suggestion.get(i);

                        MenuItem menuItem = new MenuItem();
                        menuItem.getStyleClass().add("filter-menuitem");
                        menuItem.setGraphic(color(result, suggestionService.getTitle()));
                        menuItem.setOnAction(actionEvent -> {
                            String text = suggestionService.getMessage() + result;
                            textChangedByUser = false;
                            setText(text);
                            positionCaret(text.length());
                        });
                        popupMenu.getItems().addAll(menuItem);
                    }
                    Text text = new Text(suggestionService.getMessage());
                    text.applyCss();
                    final double width = text.getLayoutBounds().getWidth();

                    popupMenu.show(SuggestingTextField.this, Side.BOTTOM,
                            width + 3, 0);
                });
                if (suggestionService.getState() == Worker.State.READY) {
                    suggestionService.start();
                } else if (suggestionService.getState() == Worker.State.SUCCEEDED) {
                    suggestionService.restart();
                }
            }
        });
        popupMenu.setOnShown(windowEvent -> popupMenu.getSkin().getNode().lookup(".menu-item").requestFocus());
    }

    private TextFlow color(String result, String t1) {
        TextFlow textFlow = new TextFlow();
        int y = 0;
        StringBuilder normal = new StringBuilder();
        for (int i = 0; i < result.length(); i++) {
            if (y < t1.length() &&
                    (result.charAt(i) == t1.charAt(y) || result.charAt(i) == t1.charAt(y) + 32 ||
                            result.charAt(i) == t1.charAt(y) - 32)
            ) {
                if (normal.length() != 0) {
                    Text text = new Text(normal.toString());
                    textFlow.getChildren().add(text);
                }
                Text highlighted = new Text(String.valueOf(result.charAt(i)));
                highlighted.setFill(Color.RED);
                textFlow.getChildren().add(highlighted);
                y++;
                normal.delete(0, normal.length());
            } else {
                normal.append(result.charAt(i));
            }
        }
        if (normal.length() != 0) {
            textFlow.getChildren().add(new Text(normal.toString()));
        }
        return textFlow;
    }

    public SortedSet<String> getStrings() {
        return strings;
    }
}