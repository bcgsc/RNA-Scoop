package controller;

import exceptions.InvalidFigureScaleException;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import org.json.JSONObject;
import persistance.SessionMaker;
import ui.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ImageExporterController extends PopUpController implements Initializable {
    private static final float IMAGE_EXPORTER_HEIGHT = 120;
    private static final float IMAGE_EXPORTER_WIDTH = 440;
    private static final float DEFAULT_SCALE = 1f;
    private static final String JUST_ISOFORM_VIEW_OPTION = "Isoform view";
    private static final String JUST_CELL_CLUSTER_PLOT_OPTION = "Cell cluster plot";
    private static final String BOTH_OPTION = "Isoform view and cell cluster plot";

    @FXML private Parent imageExporter;
    @FXML private ComboBox<String> exportOptions;
    @FXML private TextField scaleField;
    private float scale;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpWindow();
        setUpExportOptions();
        setUpScale();
    }

    @Override
    public void display() {
        super.display();
        disableAssociatedFunctionality();
    }

    public void setSettingsToDefault() {
        exportOptions.getSelectionModel().select(BOTH_OPTION);
        setScale(DEFAULT_SCALE);
    }

    public void restoreImageExporterFromPrevSession(JSONObject prevSession) {
        exportOptions.getSelectionModel().select(prevSession.getString(SessionMaker.FIGURE_TYPE_EXPORTING_KEY));
        setScale(prevSession.getFloat(SessionMaker.FIGURE_SCALE_KEY));
    }

    public float getFigureScale() {
        return scale;
    }

    public String getFigureTypeExporting() {
        return exportOptions.getSelectionModel().getSelectedItem();
    }

    /**
     * When export button is pressed, exports appropriate figure (either just isoform view,
     * cell cluster plot or both depending on option selected) to a png image file
     */
    @FXML
    protected void handleExportButton() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png");
        fileChooser.getExtensionFilters().add(imageFilter);
        fileChooser.setInitialFileName("figure.png");
        File imageFile = fileChooser.showSaveDialog(ControllerMediator.getInstance().getMainWindow());
        if (imageFile != null) {
            ControllerMediator.getInstance().addConsoleMessage("Exporting figure...");
            try {
                BufferedImage image;
                String selectedOption = exportOptions.getSelectionModel().getSelectedItem();
                if (selectedOption.equals(JUST_ISOFORM_VIEW_OPTION))
                    image = exportToImage(ControllerMediator.getInstance().getIsoformPlot());
                else if (selectedOption.equals(JUST_CELL_CLUSTER_PLOT_OPTION))
                    image = exportToImage(ControllerMediator.getInstance().getCellClusterPlot());
                else
                    image = getIsoformViewAndCellClusterPlotImage();
                ImageIO.write(image, "png", imageFile);
                ControllerMediator.getInstance().addConsoleMessage("Successfully exported figure");
            } catch (Exception e) {
                ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
            }
        }
        enableAssociatedFunctionality();
        window.hide();
    }

    private void setScale(float scale) {
        this.scale = scale;
        scaleField.setText(Float.toString(scale));
    }

    private void enableAssociatedFunctionality() {
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableTPMGradientAdjuster();
        ControllerMediator.getInstance().enableClusterView();
        ControllerMediator.getInstance().enableClusterViewSettings();
        ControllerMediator.getInstance().enableLabelSetManager();
    }

    private void disableAssociatedFunctionality() {
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableGeneSelector();
        ControllerMediator.getInstance().disableTPMGradientAdjuster();
        ControllerMediator.getInstance().disableClusterView();
        ControllerMediator.getInstance().disableClusterViewSettings();
        ControllerMediator.getInstance().disableLabelSetManager();
    }

    private BufferedImage exportToImage(Node node) {
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(scale, scale));
        WritableImage image = node.snapshot(spa, null);
        return SwingFXUtils.fromFXImage(image, null);
    }

    private BufferedImage getIsoformViewAndCellClusterPlotImage() {
        BufferedImage image;
        BufferedImage isoformView = exportToImage(ControllerMediator.getInstance().getIsoformPlot());
        BufferedImage cellClusterPlot = exportToImage(ControllerMediator.getInstance().getCellClusterPlot());
        image = new BufferedImage(isoformView.getWidth() + cellClusterPlot.getWidth(),
                Math.max(isoformView.getHeight(), cellClusterPlot.getHeight()), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.drawImage(isoformView, 0, 0, null);
        graphics.drawImage(cellClusterPlot, isoformView.getWidth(), 0, null);
        return image;
    }

    /**
     * Sets up Image Exporter window
     * Makes it so window is hidden when X button is pressed, enables
     * associated functionality (because is disabled when window is displayed)
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Export Figures");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
            enableAssociatedFunctionality();
        });
    }

    private void setUpExportOptions() {
        exportOptions.getItems().addAll(BOTH_OPTION, JUST_ISOFORM_VIEW_OPTION, JUST_CELL_CLUSTER_PLOT_OPTION);
        exportOptions.getSelectionModel().select(0);
    }

    /**
     * Sets scale to default value and makes it so scale value is updated
     * when new values are entered into the scale field
     */
    private void setUpScale() {
        setScale(DEFAULT_SCALE);
        scaleField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                try {
                   updateScale();
                } catch (Exception e) {
                    ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
                    scaleField.setText(Float.toString(scale));
                }
            }
        });
    }

    /**
     * Checks if value in scale field is valid (is number >= 0), and if so,
     * updates the scale
     */
    private void updateScale() throws InvalidFigureScaleException {
        float newScale;
        try {
            newScale = Float.parseFloat(scaleField.getText());
        } catch (Exception e) {
            throw new InvalidFigureScaleException();
        }

        if (newScale <= 0)
            throw new InvalidFigureScaleException();

        scale = newScale;
    }

    private void setWindowSizeAndDisplay() {
        window.setScene(new Scene(imageExporter, IMAGE_EXPORTER_WIDTH, IMAGE_EXPORTER_HEIGHT));
    }
}
