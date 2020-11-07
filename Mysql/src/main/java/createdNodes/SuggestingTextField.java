package createdNodes;

import controller.serviceProvider.Services;
import javafx.concurrent.Worker;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

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
            if (t1.length() == 0 || lastIndexOf + 1 == t1.length()||!textChangedByUser) {
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
                        MenuItem menuItem = new MenuItem(result);
                        menuItem.setOnAction(actionEvent -> {
                            String text = suggestionService.getMessage() + result;
                            textChangedByUser=false;
                            setText(text);
                            positionCaret(text.length());
                        });
                        popupMenu.getItems().addAll(menuItem);
                    }
                    Text text = new Text(suggestionService.getMessage());
                    text.applyCss();
                    final double width = text.getLayoutBounds().getWidth();

                    popupMenu.show(SuggestingTextField.this, Side.BOTTOM,
                            width+3, 0);
                });
                if (suggestionService.getState() == Worker.State.READY) {
                    suggestionService.start();
                } else if (suggestionService.getState() == Worker.State.SUCCEEDED) {
                    suggestionService.restart();
                }
            }
        });
    }

    public SortedSet<String> getStrings() {
        return strings;
    }
}
