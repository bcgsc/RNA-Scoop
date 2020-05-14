package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ui.Main;

import java.net.URL;
import java.util.ResourceBundle;

public class ClusterManagerController implements Initializable {
    private static final float CLUSTER_CONTROLLER_SCALE_FACTOR = 0.33f;

    @FXML private ScrollPane clusterController;

    private Stage window;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpWindow();
    }

    /**
     * Displays the cluster controller window
     */
    public void display() {
        window.hide();
        window.show();
    }

    /**
     * Sets up gene selector window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Cluster Manager");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void setWindowSizeAndDisplay() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(clusterController, screen.getWidth() * CLUSTER_CONTROLLER_SCALE_FACTOR, screen.getHeight() *  CLUSTER_CONTROLLER_SCALE_FACTOR));
    }
}
