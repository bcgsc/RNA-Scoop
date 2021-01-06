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
    public static final String FIRST_MESSAGE_INDICATOR = "> ";
    public static final String ERROR_INDICATOR = "ERROR: ";
    public static final String ERROR_INDICATOR_COLOR = "red";

    @FXML private TextFlow consoleTextFlow;
    @FXML private ScrollPane console;

    //List of messages that have been added to the console
    private boolean lastMessageIsError;
    private boolean consoleIsCleared;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        consoleTextFlow.setStyle("-fx-line-spacing: 0.85em;");
        lastMessageIsError = false;
        consoleIsCleared = true;
        addConsoleMessage("Welcome to RNA-Scoop!");
    }

    public void addConsoleMessage(String message) {
        addFirstMessageIndicator();
        consoleTextFlow.getChildren().add(new Text(message));
        scrollToBottom();
        lastMessageIsError = false;
        consoleIsCleared = false;
    }

    public void addConsoleErrorMessage(String message) {
        addFirstAndErrorMessageIndicators();
        consoleTextFlow.getChildren().add(new Text(message));
        scrollToBottom();
        lastMessageIsError = true;
        consoleIsCleared = false;
    }

    /**
     * Adds an error message to the console describing the unexpected
     * exception thrown and why it was thrown. Also prints stack trace
     * @param e the exception that occurred
     */
    public void addConsoleUnexpectedExceptionMessage(Exception e) {
        String message = e.toString();
        addFirstAndErrorMessageIndicators();
        consoleTextFlow.getChildren().add(new Text(message));
        scrollToBottom();
        lastMessageIsError = true;
        consoleIsCleared = false;
        e.printStackTrace();
    }

    public void clearConsole() {
        consoleTextFlow.getChildren().clear();
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
            consoleTextFlow.getChildren().add(new Text("\n" + FIRST_MESSAGE_INDICATOR));
        } else {
            consoleTextFlow.getChildren().add(new Text(FIRST_MESSAGE_INDICATOR));
        }
    }

    /**
     * Removes first message indicator from first console message
     */
    private void removeFirstMessageIndicator() {
        int numMessages = consoleTextFlow.getChildren().size();
        Text firstMessage;
        if (lastMessageIsError)
            firstMessage = (Text) consoleTextFlow.getChildren().get(numMessages - 3);
        else
            firstMessage = (Text) consoleTextFlow.getChildren().get(numMessages - 2);
        firstMessage.setText(firstMessage.getText().replaceFirst(FIRST_MESSAGE_INDICATOR, ""));
    }

    /**
     * Adds first message and error message indicators to console, indicates that latest message is an error
     */
    private void addFirstAndErrorMessageIndicators() {
        addFirstMessageIndicator();
        Text errorIndicator = new Text(ERROR_INDICATOR);
        errorIndicator.setStyle("-fx-fill: " + ERROR_INDICATOR_COLOR + ";");
        consoleTextFlow.getChildren().add(errorIndicator);
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
