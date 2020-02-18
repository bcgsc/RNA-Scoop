package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    private static final float GENE_SELECTOR_SCALE_FACTOR = 0.40f;
    private static final Image logo = new Image("ui/resources/icons/RNA-ScoopIcon2.png");

    @FXML private VBox geneSelector;
    @FXML private GridPane gridPane;
    @FXML private TableView genesTable;
    @FXML private TableView shownGenesTable;
    @FXML private Button addAllButton;
    @FXML private Button addSelectedButton;
    @FXML private Button removeSelectedButton;
    @FXML private Button clearAllButton;

    private Stage window;
    private List<Gene> genes;
    private List<Gene> selectedGenes;

    /**
     * Sets up grid pane, window, gene and shown gene tables
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpGridPane();
        setUpGenesTableView();
        setUpShownGenesTableView();
        setUpWindow();
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        genesTable.setDisable(true);
        shownGenesTable.setDisable(true);
        addAllButton.setDisable(true);
        addSelectedButton.setDisable(true);
        removeSelectedButton.setDisable(true);
        clearAllButton.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        genesTable.setDisable(false);
        shownGenesTable.setDisable(false);
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
     * Clears all genes in shown genes table, and tells the isoform plot
     * about the changes
     */
    public void clearShownGenes() {
        selectedGenes.clear();
        ControllerMediator.getInstance().setIsoformPlotShownGenes(selectedGenes);
    }

    /**
     * Retrieves parsed genes and sets as genes in genes table
     */
    public void updateGenes() {
        genes.clear();
        genes.addAll(Parser.getParsedGenes().values());
        genes.sort(Gene::compareTo);
    }

    /**
     * Adds genes selected in genes table to shown genes table, and tells the isoform plot
     * about the changes
     */
    @FXML
    protected void handleAddSelectedButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<Gene> genesToAdd = genesTable.getSelectionModel().getSelectedItems();
            for (Gene gene : genesToAdd) {
                if (!selectedGenes.contains(gene))
                    selectedGenes.add(gene);
            }
            selectedGenes.sort(Gene::compareTo);
            ControllerMediator.getInstance().setIsoformPlotShownGenes(selectedGenes);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("adding selected genes");
            e.printStackTrace();
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Removes genes selected in shown genes table from shown genes table,
     * and tells the isoform plot about the changes
     */
    @FXML
    protected void handleRemoveSelectedButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<Gene> genesToRemove = shownGenesTable.getSelectionModel().getSelectedItems();
            selectedGenes.removeAll(genesToRemove);
            ControllerMediator.getInstance().setIsoformPlotShownGenes(selectedGenes);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("removing selected genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Removes all genes in shown genes table from shown genes table,
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
     * Adds all genes to shown genes table, and tells the isoform plot
     * about the changes
     */
    @FXML
    protected void handleAddAllButtonAction() {
        disableAssociatedFunctionality();
        try {
            selectedGenes.clear();
            selectedGenes.addAll(genes);
            ControllerMediator.getInstance().setIsoformPlotShownGenes(selectedGenes);
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
     * Gives genes table two columns (one for gene ID, one for gene name)
     * Genes table is populated with all parsed genes
     */
    private void setUpGenesTableView() {
        genesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        genes = FXCollections.observableArrayList();
        genesTable.setItems((ObservableList) genes);
        TableColumn<Gene,String> geneIDCol = new TableColumn("Gene ID");
        geneIDCol .setCellValueFactory(new PropertyValueFactory("id"));
        TableColumn<Gene,String> geneName = new TableColumn("Gene Name");
        geneName.setCellValueFactory(new PropertyValueFactory("name"));

        genesTable.getColumns().setAll(geneIDCol , geneName);
        genesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Gives shown genes table two columns (one for gene ID, one for gene name)
     * Shown genes table is populated with all genes users has selected to display
     */
    private void setUpShownGenesTableView() {
        shownGenesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedGenes = FXCollections.observableArrayList();
        shownGenesTable.setItems((ObservableList) selectedGenes);
        TableColumn<Gene,String> geneIDCol = new TableColumn("Gene ID");
        geneIDCol .setCellValueFactory(new PropertyValueFactory("id"));
        TableColumn<Gene,String> geneName = new TableColumn("Gene Name");
        geneName.setCellValueFactory(new PropertyValueFactory("name"));

        shownGenesTable.getColumns().setAll(geneIDCol , geneName);
        shownGenesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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