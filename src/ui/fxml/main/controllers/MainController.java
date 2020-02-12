package ui.fxml.main.controllers;

import exceptions.RNAScoopException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import parser.Parser;
import ui.fxml.GeneSelectorController;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainController {

    private static final float ABOUT_SCALE_FACTOR = 0.33f;

    @FXML private BorderPane window;
    @FXML private ComboBox path;
    @FXML private MenuItem tSNEToggle;
    @FXML private MenuItem isoformPlotToggle;
    @FXML private MenuItem consoleToggle;
    @FXML private SplitPane verticalSplitPane;
    @FXML private SplitPane horizontalSplitPane;

    private IsoformPlotController isoformPlotController;
    private ConsoleController consoleController;
    private TSNEPlotController tSNEPlotController;
    private GeneSelectorController geneSelectorController;
    private boolean tSNEPlotOpen;
    private boolean consoleOpen;
    private boolean isoformPlotOpen;

    public void initializeMain(FXMLLoader consoleLoader, FXMLLoader isoformPlotLoader,
                               FXMLLoader tSNEPlotLoader, FXMLLoader geneSelectorLoader) throws IOException {
        addPanels(consoleLoader, isoformPlotLoader, tSNEPlotLoader);
        setUpControllers(consoleLoader, isoformPlotLoader, tSNEPlotLoader, geneSelectorLoader);
        setUpPathComboBox();
    }

    @FXML
    protected void handleAboutButtonAction() {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("ui/fxml/about.fxml"));
            Stage stage = new Stage();
            stage.setTitle("About");
            Rectangle2D screen = Screen.getPrimary().getBounds();
            stage.setScene(new Scene(root, screen.getWidth() * ABOUT_SCALE_FACTOR, screen.getHeight() * ABOUT_SCALE_FACTOR));
            stage.show();
        }
        catch (IOException e) {
            consoleController.addConsoleErrorMessage("Could not load about window");
        }
    }

    /**
     * When t-SNE toggle is pressed, toggles visibility of the t-SNE plot
     */
    @FXML
    protected void handleTSNEViewToggle() {
        if (tSNEPlotOpen) {
            horizontalSplitPane.getItems().remove(tSNEPlotController.getTSNEPlot());
            tSNEToggle.setText("Open t-SNE Plot");
            tSNEPlotOpen = false;
        } else {
            horizontalSplitPane.getItems().add(tSNEPlotController.getTSNEPlot());
            tSNEToggle.setText("Close t-SNE Plot");
            tSNEPlotOpen = true;
        }
    }

    /**
     * When isoform plot toggle is pressed, toggles visibility of the isoform plot
     */
    @FXML
    protected void handleIsoformViewToggle() {
        if (isoformPlotOpen) {
            horizontalSplitPane.getItems().remove(isoformPlotController.getIsoformPlot());
            isoformPlotToggle.setText("Open Isoform Plot");
            isoformPlotOpen = false;
        } else {
            horizontalSplitPane.getItems().add(0, isoformPlotController.getIsoformPlot());
            isoformPlotToggle.setText("Close Isoform Plot");
            isoformPlotOpen = true;
        }
    }

    /**
     * When console toggle is pressed, toggles visibility of the console
     */
    @FXML
    protected void handleConsoleViewToggle() {
        if (consoleOpen) {
            verticalSplitPane.getItems().remove(consoleController.getConsole());
            consoleToggle.setText("Open Console");
            consoleOpen = false;
        } else {
            verticalSplitPane.getItems().add(1, consoleController.getConsole());
            verticalSplitPane.setDividerPosition(0, 1);
            consoleToggle.setText("Close Console");
            consoleOpen = true;
        }
    }

    /**
     * When reverse complement toggle is selected, (-) strands will be reverse complemented in
     * the isoform plot; when it is unselected, they will not
     */
    @FXML
    protected void handleRevComplementToggle() {
        isoformPlotController.toggleReverseComplement();
    }

    /**
     * When load button is pressed, parses path to file and retrieves parsed genes
     * Adds parsed genes to gene selector's items
     * Adds file path to path's list of previous file paths
     * Displays error (and successful completion) messages in console
     */
    @FXML
    protected void handleLoadButtonAction() {
        try {
            Parser.readFile((String) path.getValue());
            consoleController.addConsoleMessage("Successfully loaded file from path: " + path.getValue());
            addLoadedPaths();
        } catch (RNAScoopException e){
            Parser.removeParsedGenes();
            consoleController.addConsoleErrorMessage(e.getMessage());
        } catch (FileNotFoundException e) {
            consoleController.addConsoleErrorMessage("Could not find file at path: " + path.getValue());
        } catch (Exception e) {
            Parser.removeParsedGenes();
            consoleController.addConsoleErrorMessage("An unexpected error occurred while reading file from path: " + path.getValue());
        }
        isoformPlotController.clearCanvas();
        geneSelectorController.clearShownGenes();
        geneSelectorController.updateGenes();
    }

    /**
     * Add's the console, isoform plot and t-sne plot panels to the main
     * window.
     */
    private void addPanels(FXMLLoader consoleLoader, FXMLLoader isoformPlotLoader, FXMLLoader tSNEPlotLoader) throws IOException {
        horizontalSplitPane.getItems().add(isoformPlotLoader.load());
        horizontalSplitPane.getItems().add(tSNEPlotLoader.load());
        verticalSplitPane.getItems().add(consoleLoader.load());
        tSNEPlotOpen = true;
        consoleOpen = true;
        isoformPlotOpen = true;
    }

    private void setUpControllers(FXMLLoader consoleLoader, FXMLLoader isoformPlotLoader, FXMLLoader tSNEPlotLoader, FXMLLoader geneSelectorLoader) {
        isoformPlotController = isoformPlotLoader.getController();
        consoleController = consoleLoader.getController();
        tSNEPlotController = tSNEPlotLoader.getController();
        geneSelectorController = geneSelectorLoader.getController();

        tSNEPlotController.initConsoleController(consoleController);
        isoformPlotController.initControllers(consoleController, geneSelectorController);
        geneSelectorController.initIsoformPlotController(isoformPlotController);
    }

    /**
     * Automatically resizes path combo box when window is resized; removes
     * initial focus from path combo box
     */
    private void setUpPathComboBox() {
        window.widthProperty().addListener((observable, oldValue, newValue) -> path.setPrefWidth(window.getWidth() - 85));
        Platform.runLater(() -> window.requestFocus());
    }

    private void addLoadedPaths() {
        ObservableList<String> addedPaths = path.getItems();
        if (!addedPaths.contains(path.getValue())) {
            addedPaths.add((String) path.getValue());
        }
    }
}
