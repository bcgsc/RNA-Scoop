package ui.fxml.main.controllers;

import exceptions.RNAScoopException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    @FXML private Button loadButton;
    @FXML private Menu viewMenu;
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

    public void disable() {
        loadButton.setDisable(true);
        viewMenu.setDisable(true);
    }

    public void enable() {
        loadButton.setDisable(false);
        viewMenu.setDisable(false);
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
     * Clears isoform plot and shown genes in genes selector
     * Updates genes selectors genes
     * Adds file path to path's list of previous file paths
     * Displays error (and successful completion) messages in console
     */
    @FXML
    protected void handleLoadButtonAction() {
        isoformPlotController.clearCanvas();
        geneSelectorController.clearShownGenes();
        disable();
        isoformPlotController.disable();
        tSNEPlotController.disable();
        geneSelectorController.disable();
        try {
            Thread fileLoaderThread = new Thread(new FileLoaderThread());
            fileLoaderThread.start();
        } catch (Exception e) {
            enableAssociatedFunctionality();
            consoleController.addConsoleErrorMessage("An unexpected error occurred while reading file from path: " + path.getValue());
        }
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

        tSNEPlotController.initControllers(consoleController, geneSelectorController, isoformPlotController,this);
        isoformPlotController.initControllers(consoleController, geneSelectorController);
        geneSelectorController.initControllers(isoformPlotController, tSNEPlotController, consoleController, this);
    }

    private void enableAssociatedFunctionality() {
        Platform.runLater(() -> enable());
        Platform.runLater(() -> isoformPlotController.enable());
        Platform.runLater(() -> tSNEPlotController.enable());
        Platform.runLater(() -> geneSelectorController.enable());
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

    /**
     * Thread that loads files from paths in path combo box
     */
    private class FileLoaderThread implements Runnable {

        @Override
        public void run() {
            try {
                Platform.runLater(() -> consoleController.addConsoleMessage("Loading file from path: " + path.getValue()));
                Parser.readFile((String) path.getValue());
                Platform.runLater(() -> consoleController.addConsoleMessage("Successfully loaded file from path: " + path.getValue()));
                Platform.runLater(() -> addLoadedPaths());
            } catch (RNAScoopException e){
                Parser.removeParsedGenes();
                Platform.runLater(() -> consoleController.addConsoleErrorMessage(e.getMessage()));
            } catch (FileNotFoundException e) {
                Platform.runLater(() -> consoleController.addConsoleErrorMessage("Could not find file at path: " + path.getValue()));
            } catch (Exception e) {
                Parser.removeParsedGenes();
                Platform.runLater(() -> consoleController.addConsoleErrorMessage("An unexpected error occurred while reading file from path: " + path.getValue()));
            } finally {
                Platform.runLater(() -> geneSelectorController.updateGenes());
                enableAssociatedFunctionality();
            }
        }
    }
}
