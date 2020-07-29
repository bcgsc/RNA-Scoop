package controller;

import exceptions.RNAScoopException;
import exceptions.TPMGradientInvalidMaxMinException;
import exceptions.TPMGradientMinGreaterEqualMaxException;
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
import ui.Main;

import java.net.URL;
import java.util.ResourceBundle;

import static util.Util.roundToOneDecimal;

public class TPMGradientAdjusterController implements Initializable, InteractiveElementController {
    private static final float TPM_GRADIENT_ADJUSTER_SCALE_HEIGHT_FACTOR = 0.25f;
    private static final float TPM_GRADIENT_ADJUSTER_SCALE_WIDTH_FACTOR = 0.4f;
    private static final Color DEFAULT_MIN_TPM_COLOR = Color.color(1.000, 1.000,1.000);
    private static final Color DEFAULT_MID_TPM_COLOR = Color.color(0.659, 0.867, 0.710);
    private static final Color DEFAULT_MAX_TPM_COLOR = Color.color(0.263, 0.635,0.792);
    private static final int DEFAULT_RECOMMENDED_MIN_TPM = 1;
    private static final int DEFAULT_RECOMMENDED_MAX_TPM = 10000;
    private static final String SCALE_CHOOSER_LINEAR_OPTION = "Linear";
    private static final String SCALE_CHOOSER_EXPONENTIAL_OPTION = "Logarithmic";
    private static final double TPM_GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT = 40;
    private static final double TPM_GRADIENT_MIN_WIDTH = 250;
    private static final double TPM_GRADIENT_MIN_HEIGHT  = 60;

    @FXML private ScrollPane tpmGradientAdjuster;
    @FXML private GridPane gridPane;
    @FXML private Rectangle tpmGradient;
    @FXML private Text minGradientTPMLabel;
    @FXML private Text maxGradientTPMLabel;
    // TPM gradient controls
    @FXML private TextField gradientMinTPMField;
    @FXML private TextField gradientMaxTPMField;
    @FXML private Button useAutoMinMaxButton;
    @FXML private ColorPicker minTPMColorPicker;
    @FXML private ColorPicker midTPMColorPicker;
    @FXML private ColorPicker maxTPMColorPicker;
    @FXML private ComboBox<String> scaleChooser;

    private Stage window;
    private int recommendedMinTPM;
    private int recommendedMaxTPM;
    private double gradientMinTPM;
    private double gradientMaxTPM;

    /**
     * Sets up grid pane, TPM gradient and controls, and the window
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpGridPane();
        setUpTPMGradientAndControls();
        setUpWindow();
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        gradientMinTPMField.setDisable(true);
        gradientMaxTPMField.setDisable(true);
        useAutoMinMaxButton.setDisable(true);
        scaleChooser.setDisable(true);
        minTPMColorPicker.setDisable(true);
        minTPMColorPicker.hide();
        midTPMColorPicker.setDisable(true);
        midTPMColorPicker.hide();
        maxTPMColorPicker.setDisable(true);
        maxTPMColorPicker.hide();
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        gradientMinTPMField.setDisable(false);
        gradientMaxTPMField.setDisable(false);
        useAutoMinMaxButton.setDisable(false);
        scaleChooser.setDisable(false);
        minTPMColorPicker.setDisable(false);
        midTPMColorPicker.setDisable(false);
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

    public void setGradientMinMaxToRecommended() {
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
        Color midTPMColor = midTPMColorPicker.getValue();
        Color maxTPMColor = maxTPMColorPicker.getValue();
        if (expression <= gradientMinTPM) {
            return minTPMColor;
        } else if (expression >= gradientMaxTPM) {
            return maxTPMColor;
        } else {
            double t = getTForExpressionBetweenMaxMin(expression);
            if (t <= 0.5)
                return minTPMColor.interpolate(midTPMColor, t/0.5);
            else
                return midTPMColor.interpolate(maxTPMColor, (t - 0.5)/0.5);
        }
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
            ControllerMediator.getInstance().handleColoringOrDotPlotChange();
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
        ControllerMediator.getInstance().handleColoringOrDotPlotChange();
    }

    /**
     * When TPM gradient scale changes (ex. from linear to exponential) redraws
     * the genes in the isoform plot
     */
    @FXML
    protected void handleChangedTPMGradientScale() {
        ControllerMediator.getInstance().handleColoringOrDotPlotChange();
    }

    /**
     * Sets the gradient min and max to the recommended values and redraws the
     * genes in the isoform plot
     */
    @FXML
    protected void handleAutoMinMaxButton() {
        setGradientMinMaxToRecommended();
        ControllerMediator.getInstance().handleColoringOrDotPlotChange();
    }

    /**
     * Assuming given expression is between TPM  gradient's max and min values, returns t value
     * representing where the expression lies on the TPM gradient (0 being the left, 1 being the
     * right), depending on the type of scale being used. Ex. if gradient's min TPM = 0, max = 10,
     * given expression = 5, and scale used is linear, will return 0.5
     */
    private double getTForExpressionBetweenMaxMin(double expression) {
        double t;
        String scale = scaleChooser.getValue();
        if (scale.equals(TPMGradientAdjusterController.SCALE_CHOOSER_LINEAR_OPTION)) {
            t = (expression - gradientMinTPM) / (gradientMaxTPM - gradientMinTPM);
        } else {
            double logIsoformExpression = Math.log10(expression + Double.MIN_VALUE);
            double logMinTPM = Math.log10(gradientMinTPM + Double.MIN_VALUE);
            double logMaxTPM = Math.log10(gradientMaxTPM+ Double.MIN_VALUE);
            t = (logIsoformExpression- logMinTPM)/(logMaxTPM - logMinTPM);
        }
        return t;
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

    private void drawTPMGradient() {
        Color minTPMColor = minTPMColorPicker.getValue();
        Color midTPMColor = midTPMColorPicker.getValue();
        Color maxTPMColor = maxTPMColorPicker.getValue();
        Stop[] stops = new Stop[] { new Stop(0, minTPMColor), new Stop(0.5, midTPMColor), new Stop(1, maxTPMColor)};
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
        row1.setPercentHeight((100 - TPM_GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT) / 2);
        RowConstraints tpmGradientRow = new RowConstraints();
        tpmGradientRow.setPercentHeight(TPM_GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight((100 - TPM_GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT) / 2);
        gridPane.getRowConstraints().addAll(row1, tpmGradientRow, row3);
    }

    /**
     * Makes TPM gradient resize when window resizes
     * Sets up handling for when TPM gradient max/min field values change
     * Sets TPM gradient to default colors
     * Sets recommended TPM gradient max and min to default values, and sets
     * the gradient's max and min to those values
     * Draws the TPM gradient
     */
    private void setUpTPMGradientAndControls() {
        makeTPMGradientResizeToWindow();
        setUpHandlingChangedMinMaxFields();
        setUpScaleChooser();
        minTPMColorPicker.setValue(DEFAULT_MIN_TPM_COLOR);
        midTPMColorPicker.setValue(DEFAULT_MID_TPM_COLOR);
        maxTPMColorPicker.setValue(DEFAULT_MAX_TPM_COLOR);
        recommendedMinTPM = DEFAULT_RECOMMENDED_MIN_TPM;
        recommendedMaxTPM = DEFAULT_RECOMMENDED_MAX_TPM;
        setGradientMinMaxToRecommended();
        drawTPMGradient();
    }

    /**
     * Assumes gridPane is wrapped in a VBox, gridPane takes up whole tpmGradientAdjuster window (excluding margins)
     * and tpmGradient is a child of gridPane
     */
    private void makeTPMGradientResizeToWindow() {
        Insets gridPaneMargin = VBox.getMargin(gridPane);
        Insets tpmGradientMargin = GridPane.getMargin(tpmGradient);
        // adding 5 prevents horizontal scrollbar from showing up
        double tpmGradientWidthSpacing = gridPaneMargin.getLeft() + gridPaneMargin.getRight() + tpmGradientMargin.getLeft() +
                tpmGradientMargin.getRight() + 5;
        double tpmGradientHeightSpacing = gridPaneMargin.getTop() + gridPaneMargin.getBottom() + tpmGradientMargin.getTop() +
                tpmGradientMargin.getBottom();
        tpmGradientAdjuster.heightProperty().addListener((ov, oldValue, newValue) -> {
            tpmGradient.setHeight(Math.max(newValue.doubleValue() * TPM_GRADIENT_GRID_PANE_ROW_PERCENT_HEIGHT / 100 - tpmGradientHeightSpacing,
                    TPM_GRADIENT_MIN_HEIGHT));
            drawTPMGradient();
        });
        tpmGradientAdjuster.widthProperty().addListener((ov, oldValue, newValue) -> {
            tpmGradient.setWidth(Math.max(newValue.doubleValue() - tpmGradientWidthSpacing, TPM_GRADIENT_MIN_WIDTH));
            drawTPMGradient();
        });
    }

    /**
     * Makes so whenever TPM Gradient min/max field values change, handler method is called
     */
    private void setUpHandlingChangedMinMaxFields() {
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
    }

    /**
     * Sets up TPM gradient adjuster window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - TPM Gradient Adjuster");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void setUpScaleChooser() {
        scaleChooser.setValue(SCALE_CHOOSER_LINEAR_OPTION);
        scaleChooser.getItems().addAll(SCALE_CHOOSER_LINEAR_OPTION, SCALE_CHOOSER_EXPONENTIAL_OPTION);
    }

    private void setWindowSizeAndDisplay() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(tpmGradientAdjuster, screen.getWidth() * TPM_GRADIENT_ADJUSTER_SCALE_WIDTH_FACTOR, screen.getHeight() * TPM_GRADIENT_ADJUSTER_SCALE_HEIGHT_FACTOR));
    }
}