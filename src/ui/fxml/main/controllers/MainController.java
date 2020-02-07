package ui.fxml.main.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import parser.Parser;
import parser.data.Gene;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class MainController {

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
    private boolean tSNEPlotOpen;
    private boolean consoleOpen;
    private boolean isoformPlotOpen;

    public void initializeMain(FXMLLoader consoleLoader, FXMLLoader isoformPlotLoader, FXMLLoader tSNEPlotLoader) {
        addPanels(consoleLoader, isoformPlotLoader, tSNEPlotLoader);
        setUpPathComboBox();
    }

    @FXML
    protected void handleAboutButtonAction() {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("ui/fxml/about.fxml"));
            Stage stage = new Stage();
            stage.setTitle("About");
            stage.setScene(new Scene(root, 600, 350));
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
    protected void handleTSNEViewToggle(ActionEvent e) {
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
    protected void handleIsoformViewToggle(ActionEvent e) {
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
    protected void handleConsoleViewToggle(ActionEvent e) {
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
    protected void handleRevComplementToggle(ActionEvent event) {
        isoformPlotController.toggleReverseComplement();
    }

    /**
     * When load button is pressed, parses path to file and retrieves parsed genes
     * Adds parsed genes to gene selector's items
     * Adds file path to path's list of previous file paths
     * Displays error (and successful completion) messages in console
     */
    @FXML
    protected void handleLoadButtonAction(ActionEvent event) {
        try {
            Parser.readFile((String) path.getValue());
            consoleController.addConsoleMessage("Successfully loaded file from path: " + path.getValue());
        } catch (FileNotFoundException e) {
            consoleController.addConsoleErrorMessage("Could not find file at path: " + path.getValue());
        } catch (Exception e) {
            consoleController.addConsoleErrorMessage("An unexpected error occurred while reading file from path: " + path.getValue());
        }
        addLoadedPaths();
        addLoadedGenes();
        isoformPlotController.clearCanvas();
        isoformPlotController.clearCheckedGenes();
    }

    /**
     * Add's the console, isoform plot and t-sne plot panels to the main
     * window.
     */
    private void addPanels(FXMLLoader consoleLoader, FXMLLoader isoformPlotLoader, FXMLLoader tSNEPlotLoader) {
        try {
            horizontalSplitPane.getItems().add(isoformPlotLoader.load());
            horizontalSplitPane.getItems().add(tSNEPlotLoader.load());
            verticalSplitPane.getItems().add(consoleLoader.load());
            this.isoformPlotController = isoformPlotLoader.getController();
            this.consoleController = consoleLoader.getController();
            this.tSNEPlotController = tSNEPlotLoader.getController();
            tSNEPlotOpen = true;
            consoleOpen = true;
            isoformPlotOpen = true;
        } catch (IOException e) {
            consoleController.addConsoleErrorMessage("An unexpected error occurred while setting up the isoform plot, " +
                                                     "t-SNE plot, and console panels");
        }
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

    private void addLoadedGenes() {
        HashMap<String, Gene> genes = Parser.getParsedGenes();
        isoformPlotController.addGenes(genes);
    }
}
