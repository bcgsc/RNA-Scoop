package ui.fxml.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ConsoleController {
    @FXML private Label consoleMessage;
    @FXML private VBox console;

    public VBox getConsole() {
        return console;
    }

    public Label getConsoleMessage() {
        return consoleMessage;
    }
}
