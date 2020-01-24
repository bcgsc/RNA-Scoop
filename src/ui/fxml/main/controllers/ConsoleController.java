package ui.fxml.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ConsoleController {
    @FXML private Label consoleMessage;
    @FXML private VBox console;

    public void setConsoleMessage(String message) {
        consoleMessage.setText(message);
    }

    public VBox getConsole() {
        return console;
    }
}
