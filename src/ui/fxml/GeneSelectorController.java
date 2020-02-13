package ui.fxml;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import parser.Parser;
import parser.data.Gene;
import ui.fxml.main.controllers.ConsoleController;
import ui.fxml.main.controllers.IsoformPlotController;
import ui.fxml.main.controllers.MainController;
import ui.fxml.main.controllers.TSNEPlotController;
import ui.resources.Util;

import java.net.URL;
import java.util.*;

public class GeneSelectorController implements Initializable {

    @FXML private GridPane gridPane;
    @FXML private ListView genes;
    @FXML private ListView shownGenes;
    @FXML private Button addAllButton;
    @FXML private Button addSelectedButton;
    @FXML private Button removeSelectedButton;
    @FXML private Button clearAllButton;

    private Stage window;
    private ConsoleController consoleController;
    private IsoformPlotController isoformPlotController;
    private TSNEPlotController tsnePlotController;
    private MainController mainController;

    /**
     * Sets up grid pane, users to select multiple genes in both list views
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpGridPane();
        genes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        shownGenes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void initWindow(Stage window) {
        this.window = window;
    }

    public void initControllers(IsoformPlotController isoformPlotController, TSNEPlotController tsnePlotController, ConsoleController consoleController, MainController mainController) {
        this.isoformPlotController = isoformPlotController;
        this.tsnePlotController = tsnePlotController;
        this.mainController = mainController;
        this.consoleController = consoleController;
    }

    public void disable() {
        genes.setDisable(true);
        shownGenes.setDisable(true);
        addAllButton.setDisable(true);
        addSelectedButton.setDisable(true);
        removeSelectedButton.setDisable(true);
        clearAllButton.setDisable(true);
    }

    public void enable() {
        genes.setDisable(false);
        shownGenes.setDisable(false);
        addAllButton.setDisable(false);
        addSelectedButton.setDisable(false);
        removeSelectedButton.setDisable(false);
        clearAllButton.setDisable(false);
    }


    /**
     * Displays the gene selector window
     */
    public void display() {
        window.hide();
        window.show();
    }

    /**
     * Clears all genes in shown genes list view, and tells the isoform plot controller
     * about the changes
     */
    public void clearShownGenes() {
        shownGenes.getItems().clear();
        isoformPlotController.setShownGenes(shownGenes.getItems());
    }

    /**
     * Retrieves parsed genes and sets as genes in genes list view
     */
    public void updateGenes() {
        ObservableList<String> genesItems = genes.getItems();
        genesItems.clear();
        HashMap<String, Gene> parsedGenes = Parser.getParsedGenes();
        if (parsedGenes != null)
            genesItems.addAll(parsedGenes.keySet());
        genesItems.sort(String::compareTo);
    }

    /**
     * Adds genes selected in genes list view to shown genes list view, and tells the isoform plot
     * controller about the changes
     */
    @FXML
    protected void handleAddSelectedButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<String> genesToAdd = genes.getSelectionModel().getSelectedItems();
            ObservableList<String> genesToBeShown = shownGenes.getItems();
            for (String gene : genesToAdd) {
                if (!genesToBeShown.contains(gene))
                    genesToBeShown.add(gene);
            }
            genesToBeShown.sort(String::compareTo);
            isoformPlotController.setShownGenes(genesToBeShown);
        } catch (Exception e) {
            consoleController.addConsoleErrorMessage("An unexpected error occurred while adding selected genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Removes genes selected in shown genes list view from shown genes list view,
     * and tells the isoform plot controller about the changes
     */
    @FXML
    protected void handleRemoveSelectedButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<String> genesToRemove = shownGenes.getSelectionModel().getSelectedItems();
            ObservableList<String> genesToBeShown = shownGenes.getItems();
            genesToBeShown.removeAll(genesToRemove);
            isoformPlotController.setShownGenes(genesToBeShown);
        } catch (Exception e) {
            consoleController.addConsoleErrorMessage("An unexpected error occurred while removing selected genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    @FXML
    protected void handleClearAllButtonAction() {
        disableAssociatedFunctionality();
        try {
            clearShownGenes();
        } catch (Exception e) {
            consoleController.addConsoleErrorMessage("An unexpected error occurred while clearing shown genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Adds all genes to shown genes list view, and tells the isoform plot controller
     * about the changes
     */
    @FXML
    protected void handleAddAllButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<String> genesToAdd = genes.getItems();
            ObservableList<String> genesToShow = shownGenes.getItems();
            genesToShow.clear();
            genesToShow.addAll(genesToAdd);
            isoformPlotController.setShownGenes(genesToShow);
        } catch (Exception e) {
            consoleController.addConsoleErrorMessage("An unexpected error occurred while adding all genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Sets up grid pane's columns and rows
     */
    private void setUpGridPane() {
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(40);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(20);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(40);
        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(100);
        gridPane.getRowConstraints().add(row1);
    }

    private void disableAssociatedFunctionality() {
        disable();
        tsnePlotController.disable();
        mainController.disable();
        isoformPlotController.disable();
    }

    private void enableAssociatedFunctionality() {
        enable();
        tsnePlotController.enable();
        mainController.enable();
        isoformPlotController.enable();
    }
}
