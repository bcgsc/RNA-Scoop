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
import ui.fxml.main.controllers.IsoformPlotController;
import ui.resources.Util;

import java.net.URL;
import java.util.*;

public class GeneSelectorController implements Initializable {

    @FXML private GridPane gridPane;
    @FXML private ListView genes;
    @FXML private ListView shownGenes;

    private Stage window;
    private IsoformPlotController isoformPlotController;

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

    public void initIsoformPlotController(IsoformPlotController isoformPlotController) {
        this.isoformPlotController = isoformPlotController;
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
        ObservableList<String> genesToAdd = genes.getSelectionModel().getSelectedItems();
        ObservableList<String> genesToBeShown = shownGenes.getItems();
        for (String gene : genesToAdd) {
            if (!genesToBeShown.contains(gene))
                genesToBeShown.add(gene);
        }
        genesToBeShown.sort(String::compareTo);
        isoformPlotController.setShownGenes(genesToBeShown);
    }

    /**
     * Removes genes selected in shown genes list view from shown genes list view,
     * and tells the isoform plot controller about the changes
     */
    @FXML
    protected void handleRemoveSelectedButtonAction() {
        ObservableList<String> genesToRemove = shownGenes.getSelectionModel().getSelectedItems();
        ObservableList<String> genesToBeShown = shownGenes.getItems();
        genesToBeShown.removeAll(genesToRemove);
        isoformPlotController.setShownGenes(genesToBeShown);
    }

    @FXML
    protected void handleClearAllButtonAction() {
        clearShownGenes();
    }

    /**
     * Adds all genes to shown genes list view, and tells the isoform plot controller
     * about the changes
     */
    @FXML
    protected void handleAddAllButtonAction() {
        ObservableList<String> genesToAdd = genes.getItems();
        ObservableList<String> genesToShow = shownGenes.getItems();
        genesToShow.clear();
        genesToShow.addAll(genesToAdd);
        isoformPlotController.setShownGenes(genesToShow);
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
}
