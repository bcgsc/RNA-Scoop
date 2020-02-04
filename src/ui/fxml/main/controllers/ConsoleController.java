package ui.fxml.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConsoleController {
    @FXML private Text consoleMessage;
    @FXML private ScrollPane console;

    private Lock lock = new ReentrantLock();

    public void addConsoleMessage(String message) {
        lock.lock();
        try {
            consoleMessage.setText("> " + message + "\n\n" + consoleMessage.getText().replaceFirst("> ", ""));
        } finally {
            lock.unlock();
        }
    }

    public ScrollPane getConsole() {
        return console;
    }
}
