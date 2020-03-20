package controller;

import annotation.Gene;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import ui.Main;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class TPMGradientAdjusterController implements Initializable, InteractiveElementController {

    private static final float TPM_GRADIENT_ADJUSTER_SCALE_HEIGHT_FACTOR = 0.25f;
    private static final float TPM_GRADIENT_ADJUSTER_SCALE_WIDTH_FACTOR = 0.4f;
    private static final Color DEFAULT_MIN_TPM_COLOR = Color.color(1.000, 1.000,1.000);
    private static final Color DEFAULT_MAX_TPM_COLOR = Color.color(0.000, 0.608, 0.969);
    private static final int DEFAULT_RECOMMENDED_MIN_TPM = 1;
    private static final int DEFAULT_RECOMMENDED_MAX_TPM = 10000;
    public static final String SCALE_CHOOSER_LINEAR_OPTION = "Linear";
    public static final String SCALE_CHOOSER_EXPONENTIAL_OPTION = "Exponential";

    @FXML private VBox tpmGradientAdjuster;
    @FXML private GridPane gridPane;
    @FXML private Rectangle tpmGradient;
    @FXML private TextField gradientMinTPM;
    @FXML private TextField gradientMaxTPM;
    @FXML private Button useRecommendedMaxMinButton;
    @FXML private Text minGradientTPMLabel;
    @FXML private Text maxGradientTPMLabel;
    @FXML private ColorPicker minTPMColorPicker;
    @FXML private ColorPicker maxTPMColorPicker;
    @FXML private ComboBox scaleChooser;

    private Stage window;
    private int recommendedMinTPM;
    private int recommendedMaxTPM;

    /**
     * Sets up grid pane, TPM gradient, scale chooser and the window
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpGridPane();
        setUpTPMGradient();
        setUpScaleChooser();
        setUpWindow();
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        gradientMinTPM.setDisable(true);
        gradientMaxTPM.setDisable(true);
        useRecommendedMaxMinButton.setDisable(true);
        minTPMColorPicker.setDisable(true);
        minTPMColorPicker.hide();
        maxTPMColorPicker.setDisable(true);
        maxTPMColorPicker.hide();
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        gradientMinTPM.setDisable(false);
        gradientMaxTPM.setDisable(false);
        useRecommendedMaxMinButton.setDisable(false);
        minTPMColorPicker.setDisable(false);
        maxTPMColorPicker.setDisable(false);
    }

    /**
     * Displays the gene selector window
     */
    public void display() {
        window.hide();
        window.show();
    }

    public void addMinTPMToGradientMinTPMLabel(double minTPM) {
        minGradientTPMLabel.setText("Min TPM (" + roundToOneDecimal(minTPM) + ")" + ": ");
    }

    public void addMaxTPMToGradientMaxTPMLabel(double maxTPM) {
        maxGradientTPMLabel.setText("Max TPM (" + roundToOneDecimal(maxTPM)+ ")" + ": ");
    }

    public void setRecommendedMinTPM(int recommendedMinTPM) {
        this.recommendedMinTPM = recommendedMinTPM;
    }

    public void setRecommendedMaxTPM(int recommendedMaxTPM) {
        this.recommendedMaxTPM = recommendedMaxTPM;
    }

    public void setGradientMaxMinToRecommended() {
        gradientMinTPM.setText(Integer.toString(recommendedMinTPM));
        gradientMaxTPM.setText(Integer.toString(recommendedMaxTPM));
    }

    public double getGradientMinTPM() {
        return Double.parseDouble(gradientMinTPM.getText());
    }

    public double getGradientMaxTPM() {
        return Double.parseDouble(gradientMaxTPM.getText());
    }

    public Color getMinTPMColor() {
        return minTPMColorPicker.getValue();
    }

    public Color getMaxTPMColor() {
        return maxTPMColorPicker.getValue();
    }

    public String getScale() {
        return (String) scaleChooser.getValue();
    }

    /**
     * When TPM gradient max/min values or scale being used changes, redraws
     * the genes being shown
     */
    @FXML
    protected void handleChangedTPMGradientScale() {
        redrawShownGenes();
    }

    /**
     * When a new color for the gradient is picked, redraws the gradient
     * and the shown genes
     */
    @FXML
    protected void handleTPMColorPicker() {
        drawTPMGradient();
        redrawShownGenes();
    }

    /**
     * Sets the gradient max and min to the recommended values and redraws the
     * shown genes
     */
    @FXML
    protected void handleUseRecommendedMaxMinButton() {
        setGradientMaxMinToRecommended();
        redrawShownGenes();
    }

    private void drawTPMGradient() {
        Color minTPMColor = minTPMColorPicker.getValue();
        Color maxTPMColor = maxTPMColorPicker.getValue();
        Stop[] stops = new Stop[] { new Stop(0, minTPMColor), new Stop(1, maxTPMColor)};
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        tpmGradient.setFill(gradient);
    }

    private void redrawShownGenes() {
        Collection<Gene> shownGenes = ControllerMediator.getInstance().getShownGenes();
        ControllerMediator.getInstance().drawGenes(shownGenes);
    }

    /**
     * Sets up grid pane's columns and rows
     */
    private void setUpGridPane() {
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(33);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(33);
        ColumnConstraints column3 = new ColumnConstraints();
        column2.setPercentWidth(33);
        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(30);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(40);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight(30);
        gridPane.getRowConstraints().addAll(row1, row2, row3);
    }

    /**
     * Makes TPM gradient resize when window resizes
     * Sets TPM gradient to default colors
     * Sets recommended TPM gradient max and min to defaault values, and sets
     * the gradient's max and min to those values
     * Draws the TPM gradient
     */
    private void setUpTPMGradient() {
        gridPane.heightProperty().addListener((ov, oldValue, newValue) -> {
            tpmGradient.setHeight(newValue.doubleValue() * .3);
            drawTPMGradient();
        });
        gridPane.widthProperty().addListener((ov, oldValue, newValue) -> {
            tpmGradient.setWidth(newValue.doubleValue() - 25);
            drawTPMGradient();
        });
        minTPMColorPicker.setValue(DEFAULT_MIN_TPM_COLOR);
        maxTPMColorPicker.setValue(DEFAULT_MAX_TPM_COLOR);
        recommendedMinTPM = DEFAULT_RECOMMENDED_MIN_TPM;
        recommendedMaxTPM = DEFAULT_RECOMMENDED_MAX_TPM;
        setGradientMaxMinToRecommended();
        drawTPMGradient();
    }

    private void setUpScaleChooser() {
        scaleChooser.setValue(SCALE_CHOOSER_LINEAR_OPTION);
        scaleChooser.getItems().addAll(SCALE_CHOOSER_LINEAR_OPTION, SCALE_CHOOSER_EXPONENTIAL_OPTION);
    }

    /**
     * Sets up TPM gradient adjuster window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - TPM Gradient Adjuster");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSize();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void setWindowSize() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(tpmGradientAdjuster, screen.getWidth() * TPM_GRADIENT_ADJUSTER_SCALE_WIDTH_FACTOR, screen.getHeight() * TPM_GRADIENT_ADJUSTER_SCALE_HEIGHT_FACTOR));
    }

    private double roundToOneDecimal (double value) {
        int scale = (int) Math.pow(10, 1);
        return (double) Math.round(value * scale) / scale;
    }
}