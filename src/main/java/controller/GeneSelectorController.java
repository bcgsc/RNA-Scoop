package controller;

import annotation.Gene;
import annotation.GeneMaxFoldChange;
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
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import labelset.LabelSet;
import mediator.ControllerMediator;
import parser.Parser;
import ui.Main;

import java.io.File;
import java.net.URL;
import java.util.*;

import static javafx.application.Platform.runLater;

public class GeneSelectorController extends PopUpController implements Initializable, InteractiveElementController{
    private static final float GENE_SELECTOR_WIDTH_SCALE_FACTOR = 0.52f;
    private static final float GENE_SELECTOR_HEIGHT_SCALE_FACTOR = 0.45f;

    @FXML private ScrollPane geneSelector;
    @FXML private GridPane gridPane;
    @FXML private TextField filterField;
    @FXML private TableView genesTable;
    @FXML private TableView shownGenesTable;
    @FXML private Button selectFromFileButton;
    @FXML private Button addSelectedButton;
    @FXML private Button removeSelectedButton;
    @FXML private Button clearAllButton;

    private FileChooser fileChooser;
    private ObservableList<Gene> genes;
    private ObservableList<Gene> shownGenes;

    /**
     * Sets up grid pane, window, genes and shown genes tables
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
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
        selectFromFileButton.setDisable(true);
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
        selectFromFileButton.setDisable(false);
        addSelectedButton.setDisable(false);
        removeSelectedButton.setDisable(false);
        clearAllButton.setDisable(false);
    }


    /**
     * Displays the gene selector window
     */
    @Override
    public void display() {
        super.display();
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
     * Sets genes in genes table to be genes in given list
     */
    public void updateGenesTable(List<Gene> genesList) {
        genes.clear();
        genes.addAll(genesList);
        genes.sort(Gene::compareTo);
    }

    public void updateGenesMaxFoldChange() {
        boolean cellPlotCleared = ControllerMediator.getInstance().isCellPlotCleared();

        if (!cellPlotCleared) {
            for (Gene gene : genes)
                gene.updateMaxFoldChange();
        }
    }

    public void handleRemovedLabelSet(LabelSet labelSet) {
        for (Gene gene : genes)
            gene.removeLabelSet(labelSet);
    }

    /**
     * Adds genes selected in genes table to shown genes table and draws them
     */
    @FXML
    protected void handleAddSelectedButtonAction() {
        try {
            ObservableList<Gene> selectedGenes = genesTable.getSelectionModel().getSelectedItems();
            addGenesToShownGenes(selectedGenes);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("adding selected genes");
            e.printStackTrace();
        }
    }

    /**
     * Removes genes selected in shown genes table from shown genes table and
     * from isoform plot
     */
    @FXML
    protected void handleRemoveSelectedButtonAction() {
        try {
            ObservableList<Gene> genesToRemove = shownGenesTable.getSelectionModel().getSelectedItems();
            ControllerMediator.getInstance().removeGenesFromIsoformPlot(genesToRemove);
            shownGenes.removeAll(genesToRemove);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("removing selected genes");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleClearAllButtonAction() {
        try {
            clearShownGenes();
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("clearing shown genes");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleSelectGenesFromFileButton() {
        File file = fileChooser.showOpenDialog(window);
        clearShownGenes();
        if (file != null) {
            Set<String> genesToSelect = Parser.loadGeneSelectionFile(file);
            List<Gene> genesToAdd = new ArrayList<>();
            for (Gene gene : genes) {
                if (genesToSelect.contains(gene.getId()) || genesToSelect.contains(gene.getName()))
                    genesToAdd.add(gene);
            }
            addGenesToShownGenes(genesToAdd);
        }
    }

    private void addGenesToShownGenes(List<Gene> genesToAdd) {
        Collection<Gene> genesAdded = new ArrayList<>();
        for (Gene gene : genesToAdd) {
            if (!shownGenes.contains(gene)) {
                shownGenes.add(gene);
                genesAdded.add(gene);
            }
        }
        if (genesAdded.size() > 0)
            ControllerMediator.getInstance().addGenesToIsoformPlot(genesAdded);
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
        TableColumn<Gene,String> geneIDCol = new TableColumn("ID");
        geneIDCol .setCellValueFactory(new PropertyValueFactory("id"));
        TableColumn<Gene,String> geneName = new TableColumn("Name");
        geneName.setCellValueFactory(new PropertyValueFactory("name"));
        TableColumn<Gene,Double> numIsoforms = new TableColumn("# Isoforms");
        numIsoforms.setCellValueFactory(new PropertyValueFactory("numIsoforms"));
        TableColumn<Gene,GeneMaxFoldChange> maxFoldChange =  new TableColumn("Change");
        maxFoldChange.setCellValueFactory(new PropertyValueFactory("maxFoldChange"));
        genesTable.getColumns().setAll(geneIDCol , geneName, numIsoforms, maxFoldChange);
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
            String name = gene.getName();
            if (name != null && name.toLowerCase().startsWith(lowerCaseFilter))
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

    private void setWindowSizeAndDisplay() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(geneSelector, screen.getWidth() * GENE_SELECTOR_WIDTH_SCALE_FACTOR, screen.getHeight() * GENE_SELECTOR_HEIGHT_SCALE_FACTOR));
    }
}