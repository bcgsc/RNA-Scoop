package ui.fxml.main.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import parser.Parser;
import parser.data.Gene;

import java.io.IOException;
import java.util.HashMap;

public class MainController {

    @FXML private BorderPane window;
    @FXML private ComboBox path;
    @FXML private MenuItem tSNEToggle;
    @FXML private MenuItem isoformPlotToggle;
    @FXML private MenuItem consoleToggle;

    private IsoformPlotController isoformPlotController;
    private ConsoleController consoleController;
    private tSNEPlotController tSNEPlotController;
    private boolean tSNEPlotOpen;
    private boolean consoleOpen;
    private boolean isoformPlotOpen;

    public void initData(ConsoleController consoleController, IsoformPlotController isoformPlotController, tSNEPlotController tSNEPlotController) {
        this.isoformPlotController = isoformPlotController;
        this.consoleController = consoleController;
        this.tSNEPlotController = tSNEPlotController;
        tSNEPlotOpen = true;
        consoleOpen = true;
        isoformPlotOpen = true;
    }

    @FXML
    protected void handleAboutButton() {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("ui/fxml/about.fxml"));
            Stage stage = new Stage();
            stage.setTitle("About");
            stage.setScene(new Scene(root, 600, 350));
            stage.show();
        }
        catch (IOException e) {
            consoleController.setConsoleMessage("An error occurred when loading the About window" +
                                                "\nError message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * When t-SNE toggle is pressed, toggles visibility of the t-SNE plot
     */
    @FXML
    protected void handleTSNEViewToggle(ActionEvent e) {
        if (tSNEPlotOpen) {
            window.setRight(null);
            tSNEToggle.setText("Open t-SNE Plot");
            tSNEPlotOpen = false;
        } else {
            window.setRight(tSNEPlotController.getTSNEPlot());
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
            window.setCenter(null);
            isoformPlotToggle.setText("Open Isoform Plot");
            isoformPlotOpen = false;
        } else {
            window.setCenter(isoformPlotController.getIsoformPlot());
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
            window.setBottom(null);
            consoleToggle.setText("Open Console");
            consoleOpen = false;
        } else {
            window.setBottom(consoleController.getConsole());
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
        String consoleText = Parser.readFile((String) path.getValue());
        addLoadedPaths();
        addLoadedGenes();
        consoleController.setConsoleMessage(consoleText);
        isoformPlotController.clearCanvas();
        isoformPlotController.clearCheckedGenes();
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
