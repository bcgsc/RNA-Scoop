package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ui.Main;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController extends PopUpController implements Initializable {
    private static final float ABOUT_SCALE_FACTOR = 0.33f;

    @FXML VBox vbox;
    @FXML ScrollPane scrollPane;
    @FXML Text javaVersion;
    @FXML Text os;

    /**
     * Sets up About window
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setJavaVersionText();
        setOSText();
        makeResizeToWindowSize();
        setUpWindow();
    }

    private void makeResizeToWindowSize() {
        scrollPane.widthProperty().addListener((ov, oldValue, newValue) -> vbox.setPrefWidth(scrollPane.getWidth() - 10)
        );
    }

    private void setJavaVersionText() {
        javaVersion.setText(System.getProperty("java.version") + " " +
                System.getProperty("java.vm.name") + " " +
                System.getProperty("java.vm.version"));
    }

    private void setOSText() {
        os.setText(System.getProperty("os.name") + " " +
                   System.getProperty("os.version") + " " +
                   System.getProperty("os.arch"));
    }

    private void setUpWindow() {
        window = new Stage();
        window.setTitle("About");
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(scrollPane, screen.getWidth() * ABOUT_SCALE_FACTOR, screen.getHeight() * ABOUT_SCALE_FACTOR));
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }
}
