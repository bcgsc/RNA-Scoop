package controller;

import exceptions.RNAScoopException;
import exceptions.TPMGradientInvalidMaxMinException;
import exceptions.TPMGradientMinGreaterEqualMaxException;
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
import java.util.ResourceBundle;

public class TPMGradientAdjusterController implements Initializable, InteractiveElementController {
    private static final float TPM_GRADIENT_ADJUSTER_SCALE_HEIGHT_FACTOR = 0.25f;
    private static final float TPM_GRADIENT_ADJUSTER_SCALE_WIDTH_FACTOR = 0.4f;
    private static final Color DEFAULT_MIN_TPM_COLOR = Color.color(1.000, 1.000,1.000);
    private static final Color DEFAULT_MAX_TPM_COLOR = Color.color(0.000, 0.608, 0.969);
    private static final int DEFAULT_RECOMMENDED_MIN_TPM = 1;
    private static final int DEFAULT_RECOMMENDED_MAX_TPM = 10000;
    public static final String SCALE_CHOOSER_LINEAR_OPTION = "Linear";
    public static final String SCALE_CHOOSER_EXPONENTIAL_OPTION = "Logarithmic";

    @FXML private VBox tpmGradientAdjuster;
    @FXML private GridPane gridPane;
    @FXML private Rectangle tpmGradient;
    @FXML private TextField gradientMinTPMField;
    @FXML private TextField gradientMaxTPMField;
    @FXML private Button useRecommendedMaxMinButton;
    @FXML private Text minGradientTPMLabel;
    @FXML private Text maxGradientTPMLabel;
    @FXML private ColorPicker minTPMColorPicker;
    @FXML private ColorPicker maxTPMColorPicker;
    @FXML private ComboBox<String> scaleChooser;

    private Stage window;
    private int recommendedMinTPM;
    private int recommendedMaxTPM;
    private double gradientMinTPM;
    private double gradientMaxTPM;

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
        gradientMinTPMField.setDisable(true);
        gradientMaxTPMField.setDisable(true);
        useRecommendedMaxMinButton.setDisable(true);
        scaleChooser.setDisable(true);
        minTPMColorPicker.setDisable(true);
        minTPMColorPicker.hide();
        maxTPMColorPicker.setDisable(true);
        maxTPMColorPicker.hide();
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        gradientMinTPMField.setDisable(false);
        gradientMaxTPMField.setDisable(false);
        useRecommendedMaxMinButton.setDisable(false);
        scaleChooser.setDisable(false);
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
        gradientMinTPMField.setText(Integer.toString(recommendedMinTPM));
        gradientMaxTPMField.setText(Integer.toString(recommendedMaxTPM));
        gradientMinTPM = recommendedMinTPM;
        gradientMaxTPM = recommendedMaxTPM;
    }

    /**
     * Returns the color on the TPM gradient associated with the given expression
     * */
    public Color getColorFromTPMGradient(double expression) {
        Color minTPMColor = minTPMColorPicker.getValue();
        Color maxTPMColor = maxTPMColorPicker.getValue();
        String scale = scaleChooser.getValue();

        if (scale.equals(TPMGradientAdjusterController.SCALE_CHOOSER_LINEAR_OPTION))
            return getLinearScaleColor(expression, gradientMinTPM, gradientMaxTPM, minTPMColor, maxTPMColor);
        else
            return getLogarithmicScaleColor(expression, gradientMinTPM, gradientMaxTPM, minTPMColor, maxTPMColor);
    }

    /**
     * When TPM gradient max/min changes updates the stored max/min values
     * (if new values are valid). Redraws the genes
     *
     * If user inputted values are invalid, adds a message to the console saying so,
     * and changes max/min values in the fields to the last valid ones stored
     */
    @FXML
    protected void handleChangedGradientMaxMinTPM() {
        try {
            updateTPMGradientMaxMin();
            ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
        } catch (RNAScoopException e) {
            gradientMinTPMField.setText(String.valueOf(gradientMinTPM));
            gradientMaxTPMField.setText(String.valueOf(gradientMaxTPM));
            e.addToMessage(". Changed TPM gradient max and min back to previous values");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    /**
     * When a new color for the gradient is picked, redraws the gradient
     * and the shown genes
     */
    @FXML
    protected void handleTPMColorPicker() {
        drawTPMGradient();
        ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
    }

    /**
     * When TPM gradient scale changes (ex. from linear to exponential) redraws
     * the genes in the isoform plot
     */
    @FXML
    protected void handleChangedTPMGradientScale() {
        ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
    }

    /**
     * Sets the gradient max and min to the recommended values and redraws the
     * genes in the isoform plot
     */
    @FXML
    protected void handleUseRecommendedMaxMinButton() {
        setGradientMaxMinToRecommended();
        ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
    }

    /**
     * Gets color for isoform with given expression based on TPM gradient using a linear scale
     */
    private Color getLinearScaleColor(double isoformExpression, double minTPM, double maxTPM, Color minTPMColor, Color maxTPMColor) {
        if (isoformExpression <= minTPM)
            return minTPMColor;
        else if (isoformExpression >= maxTPM)
            return maxTPMColor;
        else {
            double t = (isoformExpression - minTPM) / (maxTPM - minTPM);
            return minTPMColor.interpolate(maxTPMColor, t);
        }
    }

    /**
     * Gets color for isoform with given expression based on TPM gradient using a logarithmic scale
     */
    private Color getLogarithmicScaleColor(double isoformExpression, double minTPM, double maxTPM, Color minTPMColor, Color maxTPMColor) {
        if (isoformExpression <= minTPM)
            return minTPMColor;
        else if (isoformExpression >= maxTPM)
            return  maxTPMColor;
        else {
            double logIsoformExpression = Math.log10(isoformExpression + Double.MIN_VALUE);
            double logMinTPM = Math.log10(minTPM + Double.MIN_VALUE);
            double logMaxTPM = Math.log10(maxTPM + Double.MIN_VALUE);
            double t = (logIsoformExpression- logMinTPM)/(logMaxTPM - logMinTPM);
            return minTPMColor.interpolate(maxTPMColor, t);
        }
    }

    private void drawTPMGradient() {
        Color minTPMColor = minTPMColorPicker.getValue();
        Color maxTPMColor = maxTPMColorPicker.getValue();
        Stop[] stops = new Stop[] { new Stop(0, minTPMColor), new Stop(1, maxTPMColor)};
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        tpmGradient.setFill(gradient);
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
     * Makes TPM gradient check if user's inputted max/min values are valid
     * Sets TPM gradient to default colors
     * Sets recommended TPM gradient max and min to defaalt values, and sets
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
        gradientMinTPMField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedGradientMaxMinTPM();
            }
        });
        gradientMaxTPMField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedGradientMaxMinTPM();
            }
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

    /**
     * Attempts to update TPM gradient min and max values by setting them to values in TPM gradient max and
     * min fields
     *
     * If there is a problem with the values in the TPM gradient max and min fields (are
     * non-numeric, negative, min >= max), throws the appropriate exception and does not update the
     * stored TPM gradient max and min values
     */
    private void updateTPMGradientMaxMin() throws TPMGradientInvalidMaxMinException, TPMGradientMinGreaterEqualMaxException {
        double newGradientMin;
        double newGradientMax;

        try {
            newGradientMin = Double.parseDouble(gradientMinTPMField.getText());
            newGradientMax = Double.parseDouble(gradientMaxTPMField.getText());
        } catch (NumberFormatException e) {
            throw new TPMGradientInvalidMaxMinException();
        }

        if (newGradientMin >= newGradientMax)
            throw new TPMGradientMinGreaterEqualMaxException();
        if (newGradientMin < 0)
            throw new TPMGradientInvalidMaxMinException();

        gradientMinTPM = newGradientMin;
        gradientMaxTPM = newGradientMax;
    }

    private double roundToOneDecimal (double value) {
        int scale = (int) Math.pow(10, 1);
        return (double) Math.round(value * scale) / scale;
    }
}