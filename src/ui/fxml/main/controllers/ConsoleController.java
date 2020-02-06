package ui.fxml.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConsoleController {
    @FXML private Text consoleMessage;
    @FXML private ScrollPane console;

    public void addConsoleMessage(String message) {
       consoleMessage.setText("> " + message + "\n\n" + consoleMessage.getText().replaceFirst("> ", ""));
    }

    public void addConsoleErrorMessage(String message) {
        consoleMessage.setText("> ERROR: " + message + "\n\n" + consoleMessage.getText().replaceFirst("> ", ""));
    }

    public ScrollPane getConsole() {
        return console;
    }
}
