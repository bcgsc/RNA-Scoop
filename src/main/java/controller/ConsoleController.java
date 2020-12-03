package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.json.JSONArray;
import org.json.JSONObject;
import persistance.SessionMaker;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ConsoleController implements Initializable{
    public static final String FIRST_MESSAGE_INDICATOR = "> ";
    public static final String ERROR_INDICATOR = "ERROR: ";
    public static final String ERROR_INDICATOR_COLOR = "red";

    @FXML private TextFlow consoleTextFlow;
    @FXML private ScrollPane console;

    //List of messages that have been added to the console
    private ArrayList<Message> consoleMessages;
    private boolean lastMessageIsError;
    private boolean consoleIsCleared;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        consoleTextFlow.setStyle("-fx-line-spacing: 0.85em;");
        lastMessageIsError = false;
        consoleIsCleared = true;
        consoleMessages = new ArrayList<>();
        addConsoleMessage("Welcome to RNA-Scoop!");
    }

    public void addConsoleMessage(String message) {
        addFirstMessageIndicator();
        consoleTextFlow.getChildren().add(new Text(message));
        scrollToBottom();
        lastMessageIsError = false;
        consoleIsCleared = false;
        consoleMessages.add(new Message(message, MessageType.NORMAL));
    }

    public void addConsoleErrorMessage(String message) {
        addFirstAndErrorMessageIndicators();
        consoleTextFlow.getChildren().add(new Text(message));
        scrollToBottom();
        lastMessageIsError = true;
        consoleIsCleared = false;
        consoleMessages.add(new Message(message, MessageType.ERROR));
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
        consoleMessages.add(new Message(message, MessageType.ERROR));
        e.printStackTrace();
    }

    public void clearConsole() {
        consoleTextFlow.getChildren().clear();
        lastMessageIsError = false;
        consoleIsCleared = true;
        consoleMessages.clear();
    }

    public Node getConsole() {
        return console;
    }

    public ArrayList<Message> getConsoleMessages() {
        return consoleMessages;
    }

    public void restoreConsoleFromPrevSession(JSONObject prevSession) {
        restoreConsoleMessagesFromPrevSession(prevSession);
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

    /**
     * Restore all console messages from a previous session
     */
    private void restoreConsoleMessagesFromPrevSession(JSONObject prevSession) {
        clearConsole();
        JSONArray messages = prevSession.getJSONArray(SessionMaker.CONSOLE_MESSAGES_KEY);
        for (Object message : messages) {
            JSONObject messageJSON = (JSONObject) message;
            String messageType = messageJSON.getString(SessionMaker.MESSAGE_IS_ERROR_KEY);
            String messageText = messageJSON.getString(SessionMaker.MESSAGE_TEXT_KEY);
            if (messageType.equals(MessageType.ERROR.toString()))
                addConsoleErrorMessage(messageText);
            else
                addConsoleMessage(messageText);
        }
    }

    /**
     * Messages that have been added to the console
     */
    public class Message {
        String messageText;
        MessageType messageType;

        private Message(String messageText, MessageType messageType) {
            this.messageText = messageText;
            this.messageType = messageType;
        }

        public String getMessageText() {
            return messageText;
        }

        public MessageType getMessageType() {
            return messageType;
        }
    }

    /**
     * Types of messages that can be added to the console
     */
    public enum MessageType {
        NORMAL, ERROR, WARNING
    }
}
