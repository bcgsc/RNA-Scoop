package ui.fxml.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class ConsoleController implements Initializable{
    private static final String FIRST_MESSAGE_INDICATOR = "> ";
    private static final String ERROR_INDICATOR = "ERROR: ";
    private static final String ERROR_INDICATOR_COLOR = "red";

    @FXML private TextFlow consoleMessage;
    @FXML private ScrollPane console;

    private boolean lastMessageIsError;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        consoleMessage.setStyle("-fx-line-spacing: 0.85em;");
        consoleMessage.getChildren().add(new Text(FIRST_MESSAGE_INDICATOR + "Welcome to RNA-Scoop!"));
        lastMessageIsError = false;
    }

    public void addConsoleMessage(String message) {
        removeFirstMessageIndicator();
        consoleMessage.getChildren().add(new Text("\n" + FIRST_MESSAGE_INDICATOR + message));
        lastMessageIsError = false;
        scrollToBottom();
    }

    public void addConsoleErrorMessage(String message) {
        removeFirstMessageIndicator();
        Text errorIndicator = new Text(ERROR_INDICATOR);
        errorIndicator.setStyle("-fx-fill: " + ERROR_INDICATOR_COLOR + ";");
        consoleMessage.getChildren().add(new Text("\n" + FIRST_MESSAGE_INDICATOR));
        consoleMessage.getChildren().add(errorIndicator);
        consoleMessage.getChildren().add(new Text(message));
        lastMessageIsError = true;
        scrollToBottom();
    }

    public ScrollPane getConsole() {
        return console;
    }

    /**
     * Removes first message indicator from first console message
     */
    private void removeFirstMessageIndicator() {
        int numMessages = consoleMessage.getChildren().size();
        Text firstMessage;
        if (lastMessageIsError)
            firstMessage = (Text) consoleMessage.getChildren().get(numMessages - 3);
        else
            firstMessage = (Text) consoleMessage.getChildren().get(numMessages - 1);
        firstMessage.setText(firstMessage.getText().replaceFirst(FIRST_MESSAGE_INDICATOR, ""));
    }

    /**
     * Scroll to bottom of console
     */
    private void scrollToBottom() {
        // manually layout console
        console.applyCss();
        console.layout();

        console.setVvalue(1);
    }
}
