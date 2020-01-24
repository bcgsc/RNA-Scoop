package ui.fxml.main.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;
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

    private VBox isoformPlot;
    private VBox console;
    private VBox tSNEPlot;
    private Label consoleMessage;
    private CheckComboBox geneSelector;
    private boolean tSNEPlotOpen;
    private boolean consoleOpen;
    private boolean isoformPlotOpen;

    public void initData(ConsoleController consoleController, IsoformPlotController isoformPlotController, tSNEPlotController tSNEPlotController) {
        isoformPlot = isoformPlotController.getIsoformPlot();
        console = consoleController.getConsole();
        tSNEPlot = tSNEPlotController.getTSNEPlot();
        consoleMessage = consoleController.getConsoleMessage();
        geneSelector = isoformPlotController.getGeneSelector();
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
            consoleMessage.setText("An error occurred when loading the About window" +
                                   "\nError message: " + e.getMessage());
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
            window.setRight(tSNEPlot);
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
            window.setCenter(isoformPlot);
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
            window.setBottom(console);
            consoleToggle.setText("Close Console");
            consoleOpen = true;
        }
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
        consoleMessage.setText(consoleText);
    }

    private void addLoadedPaths() {
        ObservableList<String> addedPaths = path.getItems();
        if (!addedPaths.contains(path.getValue())) {
            addedPaths.add((String) path.getValue());
        }
    }

    private void addLoadedGenes() {
        HashMap<String, Gene> genes = Parser.getParsedGenes();
        ObservableList<String> addedGenes= geneSelector.getItems();
        for(String gene : genes.keySet()) {
            if (!addedGenes.contains(gene))
                addedGenes.add(gene);
        }
        addedGenes.sort(String::compareTo);
    }
}
