package controller;

import exceptions.RNAScoopException;
import exceptions.InvalidExpressionCutOffException;
import exceptions.GradientMinGreaterEqualMaxException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import org.json.JSONObject;
import persistence.SessionMaker;
import ui.Main;

import java.net.URL;
import java.util.ResourceBundle;

import static util.Util.roundToOneDecimal;

public class GradientAdjusterController extends PopUpController implements Initializable, InteractiveElementController {
    public static final String LINEAR_SCALE_OPTION = "Linear";
    public static final String EXPONENTIAL_SCALE_OPTION = "Logarithmic";
    private static final float GRADIENT_ADJUSTER_SCALE_HEIGHT_FACTOR = 0.25f;
    private static final float GRADIENT_ADJUSTER_SCALE_WIDTH_FACTOR = 0.4f;
    private static final Color DEFAULT_MIN_COLOR = Color.color(1.000, 1.000,1.000);
    private static final Color DEFAULT_MID_COLOR = Color.color(0.659, 0.867, 0.710);
    private static final Color DEFAULT_MAX_COLOR = Color.color(0.263, 0.635,0.792);
    private static final int DEFAULT_RECOMMENDED_MIN = 1;
    private static final int DEFAULT_RECOMMENDED_MAX = 1000;
    private static final double GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT = 40;
    private static final double GRADIENT_MIN_WIDTH = 250;
    private static final double GRADIENT_MIN_HEIGHT = 60;

    @FXML private ScrollPane gradientAdjuster;
    @FXML private GridPane gridPane;
    @FXML private Rectangle gradient;
    @FXML private Text minGradientLabel;
    @FXML private Text maxGradientLabel;
    // Gradient controls
    @FXML private TextField gradientMinField;
    @FXML private TextField gradientMaxField;
    @FXML private Button useAutoMinMaxButton;
    @FXML private ColorPicker minColorPicker;
    @FXML private ColorPicker midColorPicker;
    @FXML private ColorPicker maxColorPicker;
    @FXML private ComboBox<String> scaleChooser;

    private int recommendedMin;
    private int recommendedMax;
    private double gradientMin;
    private double gradientMax;

    /**
     * Sets up grid pane, gradient and controls, and the window
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpGridPane();
        setUpGradientAndControls();
        setUpWindow();
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        gradientMinField.setDisable(true);
        gradientMaxField.setDisable(true);
        useAutoMinMaxButton.setDisable(true);
        scaleChooser.setDisable(true);
        minColorPicker.setDisable(true);
        minColorPicker.hide();
        midColorPicker.setDisable(true);
        midColorPicker.hide();
        maxColorPicker.setDisable(true);
        maxColorPicker.hide();
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        gradientMinField.setDisable(false);
        gradientMaxField.setDisable(false);
        useAutoMinMaxButton.setDisable(false);
        scaleChooser.setDisable(false);
        minColorPicker.setDisable(false);
        midColorPicker.setDisable(false);
        maxColorPicker.setDisable(false);
    }

    public void addMinExpressionToGradientMinLabel(double min) {
        minGradientLabel.setText("Min (" + roundToOneDecimal(min) + ")" + ": ");
    }

    public void addMaxExpressionToGradientMaxLabel(double max) {
        maxGradientLabel.setText("Max (" + roundToOneDecimal(max)+ ")" + ": ");
    }

    public void setGradientToDefault() {
        scaleChooser.setValue(LINEAR_SCALE_OPTION);
        minColorPicker.setValue(DEFAULT_MIN_COLOR);
        midColorPicker.setValue(DEFAULT_MID_COLOR);
        maxColorPicker.setValue(DEFAULT_MAX_COLOR);
        recommendedMin = DEFAULT_RECOMMENDED_MIN;
        recommendedMax = DEFAULT_RECOMMENDED_MAX;
        setGradientMinMaxToRecommended();
        drawGradient();
    }

    public void setRecommendedMin(int recommendedMin) {
        this.recommendedMin = recommendedMin;
    }

    public void setRecommendedMax(int recommendedMax) {
        this.recommendedMax = recommendedMax;
    }

    public void setGradientMinMaxToRecommended() {
        gradientMinField.setText(Integer.toString(recommendedMin));
        gradientMaxField.setText(Integer.toString(recommendedMax));
        gradientMin = recommendedMin;
        gradientMax = recommendedMax;
    }

    public void restoreGradientFromPrevSession(JSONObject prevSession) {
        restoreGradientMinFromPrevSession(prevSession);
        restoreGradientMaxFromPrevSession(prevSession);
        restoreGradientColorsFromPrevSession(prevSession);
        restoreGradientScaleFromPrevSession(prevSession);
    }

    private void restoreGradientMinFromPrevSession(JSONObject prevSession) {
        gradientMin = prevSession.getDouble(SessionMaker.MIN_GRADIENT_KEY);
        gradientMinField.setText(Double.toString(gradientMin));
    }

    private void restoreGradientMaxFromPrevSession(JSONObject prevSession) {
        gradientMax = prevSession.getDouble(SessionMaker.MAX_GRADIENT_KEY);
        gradientMaxField.setText(Double.toString(gradientMax));
    }

    private void restoreGradientColorsFromPrevSession(JSONObject prevSession) {
        minColorPicker.setValue(Color.web(prevSession.getString(SessionMaker.MIN_COLOR_KEY)));
        midColorPicker.setValue(Color.web(prevSession.getString(SessionMaker.MID_COLOR_KEY)));
        maxColorPicker.setValue(Color.web(prevSession.getString(SessionMaker.MAX_COLOR_KEY)));
        drawGradient();
    }


    private void restoreGradientScaleFromPrevSession(JSONObject prevSession) {
        scaleChooser.getSelectionModel().select(prevSession.getString(SessionMaker.GRADIENT_SCALE_KEY));
    }

    public double getGradientMin() {
        return gradientMin;
    }

    public double getGradientMax() {
        return gradientMax;
    }

    public double getGradientMid() {
        if (getScaleOptionInUse().equals(GradientAdjusterController.LINEAR_SCALE_OPTION)) {
           return gradientMin + 0.5 * (gradientMax - gradientMin);
        } else {
            double logMin = Math.log10(gradientMin + Double.MIN_VALUE);
            double logMax = Math.log10(gradientMax + Double.MIN_VALUE);
            return Math.pow(10, logMin + 0.5 * (logMax - logMin));
        }
    }

    public String getScaleOptionInUse() {
        return scaleChooser.getValue();
    }

    /**
     * Returns the color on the gradient associated with the given expression
     * */
    public Color getColorFromGradient(double expression) {
        Color minColor = minColorPicker.getValue();
        Color midColor = midColorPicker.getValue();
        Color maxColor = maxColorPicker.getValue();
        if (expression <= gradientMin) {
            return minColor;
        } else if (expression >= gradientMax) {
            return maxColor;
        } else {
            double t = getTForExpressionBetweenMaxMin(expression);
            if (t <= 0.5)
                return minColor.interpolate(midColor, t/0.5);
            else
                return midColor.interpolate(maxColor, (t - 0.5)/0.5);
        }
    }

    public LinearGradient getGradientFill() {
        Color minColor = minColorPicker.getValue();
        Color midColor = midColorPicker.getValue();
        Color maxColor = maxColorPicker.getValue();
        Stop[] stops = new Stop[] { new Stop(0, minColor), new Stop(0.5, midColor), new Stop(1, maxColor)};
        return new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
    }

    public String getGradientMinColorCode() {
        return getColorHexString(minColorPicker.getValue());
    }

    public String getGradientMidColorCode() {
        return getColorHexString(midColorPicker.getValue());
    }

    public String getGradientMaxColorCode() {
        return getColorHexString(maxColorPicker.getValue());
    }

    /**
     * When gradient max/min changes updates the stored max/min values
     * (if new values are valid). Redraws the genes
     *
     * If user inputted values are invalid, adds a message to the console saying so,
     * and changes max/min values in the fields to the last valid ones stored
     */
    @FXML
    protected void handleChangedGradientMaxMin() {
        try {
            updateGradientMaxMin();
            ControllerMediator.getInstance().isoformPlotHandleGradientChange();
            ControllerMediator.getInstance().clusterViewHandleColoringChange();
        } catch (RNAScoopException e) {
            gradientMinField.setText(String.valueOf(gradientMin));
            gradientMaxField.setText(String.valueOf(gradientMax));
            e.addToMessage(". Changed the gradient max and min back to previous values");
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    /**
     * When a new color for the gradient is picked, redraws the gradient
     * and the shown genes
     */
    @FXML
    protected void handleColorPicker() {
        drawGradient();
        ControllerMediator.getInstance().isoformPlotHandleGradientChange();
        ControllerMediator.getInstance().clusterViewHandleColoringChange();
    }

    /**
     * When gradient scale changes (ex. from linear to exponential) redraws
     * the genes in the isoform plot
     */
    @FXML
    protected void handleChangedGradientScale() {
        ControllerMediator.getInstance().isoformPlotHandleGradientChange();
        ControllerMediator.getInstance().clusterViewHandleColoringChange();
    }

    /**
     * Sets the gradient min and max to the recommended values and redraws the
     * genes in the isoform plot
     */
    @FXML
    protected void handleAutoMinMaxButton() {
        setGradientMinMaxToRecommended();
        ControllerMediator.getInstance().isoformPlotHandleGradientChange();
        ControllerMediator.getInstance().clusterViewHandleColoringChange();
    }

    /**
     * Assuming given expression is between gradient's max and min values, returns t value
     * representing where the expression lies on the gradient (0 being the left, 1 being the
     * right), depending on the type of scale being used. Ex. if gradient's min = 0, max = 10,
     * given expression = 5, and scale used is linear, will return 0.5
     */
    private double getTForExpressionBetweenMaxMin(double expression) {
        double t;
        if (getScaleOptionInUse().equals(GradientAdjusterController.LINEAR_SCALE_OPTION)) {
            t = (expression - gradientMin) / (gradientMax - gradientMin);
        } else {
            double logIsoformExpression = Math.log10(expression + Double.MIN_VALUE);
            double logMin = Math.log10(gradientMin + Double.MIN_VALUE);
            double logMax = Math.log10(gradientMax + Double.MIN_VALUE);
            t = (logIsoformExpression- logMin)/(logMax - logMin);
        }
        return t;
    }

    public String getColorHexString(Color color) {
        return String.format( "#%02X%02X%02X%02X",
                (int) (Math.round(color.getRed() * 255)),
                (int) (Math.round(color.getGreen() * 255)),
                (int) (Math.round(color.getBlue() * 255)),
                (int) (Math.round(color.getOpacity() * 255)));
    }

    /**
     * Attempts to update gradient min and max values by setting them to values in gradient max and
     * min fields
     *
     * If there is a problem with the values in the gradient max and min fields (are
     * non-numeric, negative, min >= max), throws the appropriate exception and does not update the
     * stored gradient max and min values
     */
    private void updateGradientMaxMin() throws InvalidExpressionCutOffException, GradientMinGreaterEqualMaxException {
        double newGradientMin;
        double newGradientMax;

        try {
            newGradientMin = Double.parseDouble(gradientMinField.getText());
            newGradientMax = Double.parseDouble(gradientMaxField.getText());
        } catch (NumberFormatException e) {
            throw new InvalidExpressionCutOffException();
        }

        if (newGradientMin >= newGradientMax)
            throw new GradientMinGreaterEqualMaxException();
        if (newGradientMin < 0)
            throw new InvalidExpressionCutOffException();

        gradientMin = newGradientMin;
        gradientMax = newGradientMax;
    }

    private void drawGradient() {
        LinearGradient gradient = getGradientFill();
        this.gradient.setFill(gradient);
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
        row1.setPercentHeight((100 - GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT) / 2);
        RowConstraints gradientRow = new RowConstraints();
        gradientRow.setPercentHeight(GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight((100 - GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT) / 2);
        gridPane.getRowConstraints().addAll(row1, gradientRow, row3);
    }

    /**
     * Makes gradient resize when window resizes
     * Sets up handling for when gradient max/min field values change
     * Sets gradient to default colors
     * Sets recommended gradient max and min to default values, and sets
     * the gradient's max and min to those values
     * Draws the gradient
     */
    private void setUpGradientAndControls() {
        makeGradientResizeToWindow();
        setUpHandlingChangedMinMaxFields();
        scaleChooser.getItems().addAll(LINEAR_SCALE_OPTION, EXPONENTIAL_SCALE_OPTION);
        setGradientToDefault();
    }

    /**
     * Assumes gridPane is wrapped in a VBox, gridPane takes up whole gradientAdjuster window (excluding margins)
     * and gradient is a child of gridPane
     */
    private void makeGradientResizeToWindow() {
        Insets gridPaneMargin = VBox.getMargin(gridPane);
        Insets gradientMargin = GridPane.getMargin(gradient);
        // adding 5 prevents horizontal scrollbar from showing up
        double gradientWidthSpacing = gridPaneMargin.getLeft() + gridPaneMargin.getRight() + gradientMargin.getLeft() +
                gradientMargin.getRight() + 5;
        double gradientHeightSpacing = gridPaneMargin.getTop() + gridPaneMargin.getBottom() + gradientMargin.getTop() +
                gradientMargin.getBottom();
        gradientAdjuster.heightProperty().addListener((ov, oldValue, newValue) -> {
            gradient.setHeight(Math.max(newValue.doubleValue() * GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT / 100 - gradientHeightSpacing,
                    GRADIENT_MIN_HEIGHT));
            drawGradient();
        });
        gradientAdjuster.widthProperty().addListener((ov, oldValue, newValue) -> {
            gradient.setWidth(Math.max(newValue.doubleValue() - gradientWidthSpacing, GRADIENT_MIN_WIDTH));
            drawGradient();
        });
    }

    /**
     * Makes so whenever gradient min/max field values change, handler method is called
     */
    private void setUpHandlingChangedMinMaxFields() {
        gradientMinField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedGradientMaxMin();
            }
        });
        gradientMaxField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedGradientMaxMin();
            }
        });
    }

    /**
     * Sets up gradient adjuster window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Expression Gradient Adjuster");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void setWindowSizeAndDisplay() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(gradientAdjuster, screen.getWidth() * GRADIENT_ADJUSTER_SCALE_WIDTH_FACTOR, screen.getHeight() * GRADIENT_ADJUSTER_SCALE_HEIGHT_FACTOR));
    }
}