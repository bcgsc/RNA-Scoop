package controller.clusterview;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.Main;

public class ClusterViewSettingsController {
    private static final float CLUSTER_VIEW_SETTINGS_HEIGHT = 150;
    private static final float CLUSTER_VIEW_SETTINGS_WIDTH = 450;

    @FXML private ScrollPane clusterViewSettings;
    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private VBox holder;

    private boolean usingUMAPSettings;

    private Parent tsneSettings;
    private Parent umapSettings;
    private Stage window;

    public void initializeClusterViewSettings(Parent tsneSettings, Parent umapSettings) {
        this.tsneSettings = tsneSettings;
        this.umapSettings = umapSettings;
        useUMAPSettings();
        setUpAlgorithmComboBox();
        setUpWindow();
    }

    /**
     * Displays the cluster view settings window
     */
    public void display() {
        window.hide();
        window.show();
    }

    public boolean usingUMAPSettings() {
        return usingUMAPSettings;
    }

    @FXML
    protected void handleClusterViewAlgorithmChange() {
        if (usingUMAPSettings) {
            useTSNESettings();
        } else {
            useUMAPSettings();
        }
    }

    private void useUMAPSettings() {
        holder.getChildren().clear();
        holder.getChildren().add(umapSettings);
        usingUMAPSettings = true;
    }

    private void useTSNESettings() {
        holder.getChildren().clear();
        holder.getChildren().add(tsneSettings);
        usingUMAPSettings = false;
    }

    private void setUpAlgorithmComboBox() {
        algorithmComboBox.getItems().addAll("UMAP", "t-SNE");
        algorithmComboBox.setValue("UMAP");
    }

    /**
     * Sets up cluster view settings window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Cluster View Settings");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void setWindowSizeAndDisplay() {
        window.setScene(new Scene(clusterViewSettings, CLUSTER_VIEW_SETTINGS_WIDTH, CLUSTER_VIEW_SETTINGS_HEIGHT));
    }
}
