package ui.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import parser.Parser;
import parser.data.Gene;
import ui.mediator.ControllerMediator;

import java.net.URL;
import java.util.*;

public class GeneSelectorController implements Initializable, InteractiveElementController {

    private static final float GENE_SELECTOR_SCALE_FACTOR = 0.35f;
    private static final Image logo = new Image("ui/resources/icons/RNA-ScoopIcon2.png");

    @FXML private VBox geneSelector;
    @FXML private GridPane gridPane;
    @FXML private ListView genes;
    @FXML private ListView shownGenes;
    @FXML private Button addAllButton;
    @FXML private Button addSelectedButton;
    @FXML private Button removeSelectedButton;
    @FXML private Button clearAllButton;

    private Stage window;

    /**
     * Sets up grid pane, window, allows users to select multiple genes in both list views
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpGridPane();
        genes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        shownGenes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setUpWindow();
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        genes.setDisable(true);
        shownGenes.setDisable(true);
        addAllButton.setDisable(true);
        addSelectedButton.setDisable(true);
        removeSelectedButton.setDisable(true);
        clearAllButton.setDisable(true);
    }

    /**
     * Enables all functionality
     */
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
     * Clears all genes in shown genes list view, and tells the isoform plot
     * about the changes
     */
    public void clearShownGenes() {
        shownGenes.getItems().clear();
        ControllerMediator.getInstance().setIsoformPlotShownGenes(shownGenes.getItems());
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
     * about the changes
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
            ControllerMediator.getInstance().setIsoformPlotShownGenes(genesToBeShown);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("adding selected genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Removes genes selected in shown genes list view from shown genes list view,
     * and tells the isoform plot about the changes
     */
    @FXML
    protected void handleRemoveSelectedButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<String> genesToRemove = shownGenes.getSelectionModel().getSelectedItems();
            ObservableList<String> genesToBeShown = shownGenes.getItems();
            genesToBeShown.removeAll(genesToRemove);
            ControllerMediator.getInstance().setIsoformPlotShownGenes(genesToBeShown);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("removing selected genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Removes all genes in shown genes list view from shown genes list view,
     * and tells the isoform plot about the changes
     */
    @FXML
    protected void handleClearAllButtonAction() {
        disableAssociatedFunctionality();
        try {
            clearShownGenes();
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("clearing shown genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Adds all genes to shown genes list view, and tells the isoform plot
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
            ControllerMediator.getInstance().setIsoformPlotShownGenes(genesToShow);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("adding all genes");
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

    /**
     * Sets up gene selector window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Gene Selector");
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(geneSelector, screen.getWidth() * GENE_SELECTOR_SCALE_FACTOR, screen.getHeight() * GENE_SELECTOR_SCALE_FACTOR));
        window.getIcons().add(logo);
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void disableAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableTSNEPlot();
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableTSNEPlot();
    }
}
