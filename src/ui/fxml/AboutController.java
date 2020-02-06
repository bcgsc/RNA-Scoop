package ui.fxml;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {
    @FXML VBox vbox;
    @FXML ScrollPane scrollPane;
    @FXML Text javaVersion;
    @FXML Text os;

    /**
     * Sets up About window
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPane.widthProperty().addListener((ov, oldValue, newValue) -> vbox.setPrefWidth(scrollPane.getWidth() - 10)
        );
        javaVersion.setText(System.getProperty("java.version") + " " +
                System.getProperty("java.vm.name") + " " +
                System.getProperty("java.vm.version"));
        os.setText(System.getProperty("os.name") + " " +
                   System.getProperty("os.version") + " " +
                   System.getProperty("os.arch"));
    }
}
