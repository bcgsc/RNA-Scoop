package controller;

import javafx.stage.Stage;

public abstract class PopUpController {
    protected Stage window;

    /**
     * Displays the pop up window
     */
    public void display() {
        window.hide(); // so if behind another window, comes to front
        if (window.isIconified()) window.setIconified(false);
        window.show();
    }
}
