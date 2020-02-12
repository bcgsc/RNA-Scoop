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
import parser.Parser;
import parser.data.Gene;
import ui.fxml.main.controllers.IsoformPlotController;
import ui.resources.Util;

import java.net.URL;
import java.util.*;

public class GeneSelectorController implements Initializable {

    @FXML GridPane gridPane;
    @FXML ListView genes;
    @FXML ListView shownGenes;

    private IsoformPlotController isoformPlotController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpGridPane();
        setUpGenesListView();
        setUpShownGenesListView();
        Platform.runLater(() -> gridPane.requestFocus());
    }

    public void initIsoformPlotController(IsoformPlotController isoformPlotController) {
        this.isoformPlotController = isoformPlotController;
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

    /**
     * Clears all genes in shown genes list view, and tells the isoform plot controller
     * about the changes
     */
    @FXML
    protected void handleClearAllButtonAction() {
        shownGenes.getItems().clear();
        isoformPlotController.setShownGenes(shownGenes.getItems());
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

    /**
     * Adds sorted parsed genes to genes list view, allows user to select multiple genes
     * at once
     */
    private void setUpGenesListView() {
        HashMap<String, Gene> parsedGenes = Parser.getParsedGenes();
        if (parsedGenes != null)
            genes.getItems().addAll(Util.asSortedList(parsedGenes.keySet()));
        genes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Adds currently drawn genes to shown genes list, allows user to select multiple genes
     * at once
     */
    private void setUpShownGenesListView() {
        shownGenes.getItems().addAll(IsoformPlotController.getShownGenes());
        shownGenes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
