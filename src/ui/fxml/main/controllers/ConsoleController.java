package ui.fxml.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ConsoleController {
    @FXML private Text consoleMessage;
    @FXML private ScrollPane console;

    public void addConsoleMessage(String message) {
        consoleMessage.setText("> " + message + "\n\n" + consoleMessage.getText().replaceFirst("> ", ""));
    }

    public ScrollPane getConsole() {
        return console;
    }
}
