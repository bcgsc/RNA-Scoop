package controller;

import annotation.Gene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import parser.Parser;
import ui.Main;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import static javafx.application.Platform.runLater;

public class GeneSelectorController implements Initializable, InteractiveElementController {
    private static final float GENE_SELECTOR_SCALE_FACTOR = 0.45f;

    @FXML private ScrollPane geneSelector;
    @FXML private GridPane gridPane;
    @FXML private TextField filterField;
    @FXML private TableView genesTable;
    @FXML private TableView shownGenesTable;
    @FXML private Button addAllButton;
    @FXML private Button addSelectedButton;
    @FXML private Button removeSelectedButton;
    @FXML private Button clearAllButton;

    private Stage window;
    private ObservableList<Gene> genes;
    private ObservableList<Gene> shownGenes;

    /**
     * Sets up grid pane, window, genes and shown genes tables
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpGridPane();
        setUpGenesTable();
        setUpShownGenesTable();
        setUpWindow();
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        genesTable.setDisable(true);
        filterField.setDisable(true);
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
        filterField.setDisable(false);
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
        runLater(() -> geneSelector.requestFocus());
    }

    /**
     * Clears all genes in genes table, clears genes being shown and filter field
     */
    public void clearGeneSelector() {
        clearShownGenes();
        genes.clear();
        filterField.setText(null);
    }

    /**
     * Clears all genes in shown genes table, and clears the isoform plot
     */
    public void clearShownGenes() {
        ControllerMediator.getInstance().removeGenesFromIsoformPlot(shownGenes);
        shownGenes.clear();
    }

    public Collection<Gene> getShownGenes() {
        return shownGenes;
    }

    /**
     * Retrieves parsed genes and sets as genes in genes table
     */
    public void updateGenes() {
        genes.clear();
        genes.addAll(Parser.getParsedGenesMap().values());
        genes.sort(Gene::compareTo);
    }

    /**
     * Adds genes selected in genes table to shown genes table and draws them
     */
    @FXML
    protected void handleAddSelectedButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<Gene> selectedGenes = genesTable.getSelectionModel().getSelectedItems();
            Collection<Gene> genesToAdd = new ArrayList<>();
            for (Gene gene : selectedGenes) {
                if (!shownGenes.contains(gene)) {
                    shownGenes.add(gene);
                    genesToAdd.add(gene);
                }
            }
            if (genesToAdd.size() > 0)
                ControllerMediator.getInstance().addGenesToIsoformPlot(genesToAdd);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("adding selected genes");
            e.printStackTrace();
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Removes genes selected in shown genes table from shown genes table and
     * from isoform plot
     */
    @FXML
    protected void handleRemoveSelectedButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<Gene> genesToRemove = shownGenesTable.getSelectionModel().getSelectedItems();
            ControllerMediator.getInstance().removeGenesFromIsoformPlot(genesToRemove);
            shownGenes.removeAll(genesToRemove);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("removing selected genes");
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
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("clearing shown genes");
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Adds all genes to shown genes table and isoform plot
     */
    @FXML
    protected void handleAddAllButtonAction() {
        disableAssociatedFunctionality();
        try {
            shownGenes.clear();
            shownGenes.addAll(genes);
            ControllerMediator.getInstance().addGenesToIsoformPlot(shownGenes);
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
        RowConstraints row2 = new RowConstraints();
        gridPane.getRowConstraints().addAll(row1, row2);
    }

    /**
     * Sets up genes table's columns, items, and makes table searchable
     */
    private void setUpGenesTable() {
        genesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setUpGenesTableColumns();
        setGenesTableItemsAndMakeSearchable();
    }

    /**
     * Sets up shown genes table's columns
     * Shown genes table is populated with all genes users has selected to display
     */
    private void setUpShownGenesTable() {
        shownGenesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setUpShownGenesTableColumns();
        shownGenes = FXCollections.observableArrayList();
        shownGenesTable.setItems(shownGenes);
    }

    /**
     * Sets up gene selector window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Gene Selector");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    /**
     * Gives genes table 3 columns (one for gene ID, one for gene name and one for
     * number of isoforms)
     */
    private void setUpGenesTableColumns() {
        TableColumn<Gene,String> geneIDCol = new TableColumn("Gene ID");
        geneIDCol .setCellValueFactory(new PropertyValueFactory("id"));
        TableColumn<Gene,String> geneName = new TableColumn("Gene Name");
        geneName.setCellValueFactory(new PropertyValueFactory("name"));
        TableColumn<Gene,String> numIsoforms = new TableColumn("# Isoforms");
        numIsoforms.setCellValueFactory(new PropertyValueFactory("numIsoforms"));
        genesTable.getColumns().setAll(geneIDCol , geneName, numIsoforms);
        genesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Sets genes table to contain all parsed genes. When something is typed into the
     * filter field, filters gene table's list of genes to only contain genes whose names
     * or IDs start with the filter field's text
     */
    private void setGenesTableItemsAndMakeSearchable() {
        genes = FXCollections.observableArrayList();
        FilteredList<Gene> filteredGenes = new FilteredList<>(genes, gene -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> filteredGenes.setPredicate(gene -> {
            // If filter text is empty, display all genes
            if (newValue == null || newValue.isEmpty())
                return true;
            // Compare gene name and id of every gene with filter text.
            String lowerCaseFilter = newValue.toLowerCase();
            if (gene.getName().toLowerCase().startsWith(lowerCaseFilter))
                return true;
            else return gene.getId().toLowerCase().startsWith(lowerCaseFilter);
        }));
        // allows genes in genes table to be sortable
        SortedList<Gene> sortedGenes = new SortedList<>(filteredGenes);
        sortedGenes.comparatorProperty().bind(genesTable.comparatorProperty());
        genesTable.setItems(sortedGenes);
    }

    /**
     * Gives shown genes table two columns (one for gene ID, one for gene name)
     */
    private void setUpShownGenesTableColumns() {
        TableColumn<Gene,String> geneIDCol = new TableColumn("Gene ID");
        geneIDCol .setCellValueFactory(new PropertyValueFactory("id"));
        TableColumn<Gene,String> geneName = new TableColumn("Gene Name");
        geneName.setCellValueFactory(new PropertyValueFactory("name"));
        shownGenesTable.getColumns().setAll(geneIDCol , geneName);
        shownGenesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void disableAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableTSNEPlot();
        ControllerMediator.getInstance().disableTPMGradientAdjuster();
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableTSNEPlot();
        ControllerMediator.getInstance().enableTPMGradientAdjuster();
    }

    private void setWindowSizeAndDisplay() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(geneSelector, screen.getWidth() * GENE_SELECTOR_SCALE_FACTOR, screen.getHeight() * GENE_SELECTOR_SCALE_FACTOR));
    }
}