package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
    private boolean consoleIsCleared;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        consoleMessage.setStyle("-fx-line-spacing: 0.85em;");
        lastMessageIsError = false;
        consoleIsCleared = true;
        addConsoleMessage("Welcome to RNA-Scoop!");
    }

    public void addConsoleMessage(String message) {
        addFirstMessageIndicator();
        consoleMessage.getChildren().add(new Text(message));
        scrollToBottom();
        lastMessageIsError = false;
        consoleIsCleared = false;
    }

    public void addConsoleErrorMessage(String message) {
        addFirstAndErrorMessageIndicators();
        consoleMessage.getChildren().add(new Text(message));
        scrollToBottom();
        lastMessageIsError = true;
        consoleIsCleared = false;
    }

    /**
     * Adds an error message to the console telling the user an unexpected
     * error occurred when doing some action
     * @param action the action happening when the error occurred
     */
    public void addConsoleUnexpectedErrorMessage(String action) {
        addFirstAndErrorMessageIndicators();
        consoleMessage.getChildren().add(new Text("An unexpected error occurred while " + action));
        scrollToBottom();
        lastMessageIsError = true;
        consoleIsCleared = false;
    }

    public void clearConsole() {
        consoleMessage.getChildren().clear();
        lastMessageIsError = false;
        consoleIsCleared = true;
    }

    public Node getConsole() {
        return console;
    }

    /**
     * Adds first message indicator to blank line (where new message will go) in console
     */
    private void addFirstMessageIndicator() {
        if (!consoleIsCleared) {
            removeFirstMessageIndicator();
            consoleMessage.getChildren().add(new Text("\n" + FIRST_MESSAGE_INDICATOR));
        } else {
            consoleMessage.getChildren().add(new Text(FIRST_MESSAGE_INDICATOR));
        }
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
            firstMessage = (Text) consoleMessage.getChildren().get(numMessages - 2);
        firstMessage.setText(firstMessage.getText().replaceFirst(FIRST_MESSAGE_INDICATOR, ""));
    }

    /**
     * Adds first message and error message indicators to console, indicates that latest message is an error
     */
    private void addFirstAndErrorMessageIndicators() {
        addFirstMessageIndicator();
        Text errorIndicator = new Text(ERROR_INDICATOR);
        errorIndicator.setStyle("-fx-fill: " + ERROR_INDICATOR_COLOR + ";");
        consoleMessage.getChildren().add(errorIndicator);
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
