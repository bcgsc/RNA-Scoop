package controller.clusterview;

import controller.PopUpController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import org.json.JSONObject;
import persistence.SessionMaker;
import ui.Main;

public class ClusterViewSettingsController extends PopUpController {
    private static final float CLUSTER_VIEW_SETTINGS_HEIGHT = 160;
    private static final float CLUSTER_VIEW_SETTINGS_WIDTH = 440;
    private static final String UMAP_OPTION = "UMAP";
    private static final String T_SNE_OPTION = "t-SNE";

    @FXML private ScrollPane clusterViewSettings;
    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private Button okButton;
    @FXML private VBox holder;

    // whether or not using UMAP settings, only updated when "OK" button is pressed
    private boolean savedUsingUMAPSettings;

    private Parent tsneSettings;
    private Parent umapSettings;

    public void initializeClusterViewSettings(Parent tsneSettings, Parent umapSettings) {
        this.tsneSettings = tsneSettings;
        this.umapSettings = umapSettings;
        setUpAlgorithmComboBox();
        setUpWindow();
    }

    public void disable() {
        algorithmComboBox.setDisable(true);
        holder.setDisable(true);
        okButton.setDisable(true);
    }

    public void enable() {
        algorithmComboBox.setDisable(false);
        holder.setDisable(false);
        okButton.setDisable(false);
    }

    public boolean usingUMAPSettings() {
        return savedUsingUMAPSettings;
    }

    public void setSettingsToDefault() {
        useUMAPSettings();
        ControllerMediator.getInstance().setUMAPSettingsToDefault();
        ControllerMediator.getInstance().setTSNESettingsToDefault();
    }

    public void restoreClusterViewSettingsFromPrevSession(JSONObject prevSession) {
        if (prevSession.getBoolean(SessionMaker.USING_UMAP_FOR_EMBEDDING_KEY))
            useUMAPSettings();
        else
            useTSNESettings();

        ControllerMediator.getInstance().restoreUMAPSettingsFromPrevSession(prevSession);
        ControllerMediator.getInstance().restoreTSNESettingsFromPrevSession(prevSession);
    }

    @FXML
    protected void handleClusterViewAlgorithmChange() {
        if (algorithmComboBox.getSelectionModel().getSelectedItem().equals(T_SNE_OPTION)) {
            useTSNESettings();
        } else {
            useUMAPSettings();
        }
    }

    @FXML
    protected void handleOKButton() {
        saveAlgorithmInUseSetting();
        ControllerMediator.getInstance().saveUMAPSettings();
        ControllerMediator.getInstance().saveTSNESettings();
        ControllerMediator.getInstance().drawCellPlot();
        window.hide();
    }

    private void useUMAPSettings() {
        SingleSelectionModel<String> selectionModel = algorithmComboBox.getSelectionModel();
        if (!selectionModel.getSelectedItem().equals(UMAP_OPTION))
            selectionModel.select(UMAP_OPTION);
        holder.getChildren().clear();
        holder.getChildren().add(umapSettings);
    }

    private void useTSNESettings() {
        SingleSelectionModel<String> selectionModel = algorithmComboBox.getSelectionModel();
        if (!selectionModel.getSelectedItem().equals(T_SNE_OPTION))
            selectionModel.select(T_SNE_OPTION);
        holder.getChildren().clear();
        holder.getChildren().add(tsneSettings);
    }

    private void saveAlgorithmInUseSetting() {
        savedUsingUMAPSettings = algorithmComboBox.getSelectionModel().getSelectedItem().equals(UMAP_OPTION);
    }

    private void restoreSettingsToSaved() {
        if (savedUsingUMAPSettings)
            algorithmComboBox.getSelectionModel().select(UMAP_OPTION);
        else
            algorithmComboBox.getSelectionModel().select(T_SNE_OPTION);

        ControllerMediator.getInstance().restoreUMAPSettingsToSaved();
        ControllerMediator.getInstance().restoreTSNESettingsToSaved();
    }

    private void setUpAlgorithmComboBox() {
        algorithmComboBox.getItems().addAll(UMAP_OPTION, T_SNE_OPTION);
        algorithmComboBox.setValue(UMAP_OPTION);
        useUMAPSettings();
        saveAlgorithmInUseSetting();
    }

    /**
     * Sets up cluster view settings window
     * Makes it so window is hidden when X button is pressed, UMAP
     * and t-sne settings are restored to saved values
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Cluster View Settings");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            restoreSettingsToSaved();
            window.hide();
        });
    }

    private void setWindowSizeAndDisplay() {
        window.setScene(new Scene(clusterViewSettings, CLUSTER_VIEW_SETTINGS_WIDTH, CLUSTER_VIEW_SETTINGS_HEIGHT));
    }
}
