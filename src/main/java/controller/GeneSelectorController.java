package controller;

import annotation.Gene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import parser.Parser;
import ui.Main;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class GeneSelectorController implements Initializable, InteractiveElementController {

    private static final float GENE_SELECTOR_SCALE_FACTOR = 0.40f;

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
    private List<Gene> shownGenes;

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
     * Clears all genes in shown genes table, and clears the isoform plot
     */
    public void clearShownGenes() {
        shownGenes.clear();
        ControllerMediator.getInstance().clearIsoformPlot();
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
            ObservableList<Gene> genesToAdd = genesTable.getSelectionModel().getSelectedItems();
            for (Gene gene : genesToAdd) {
                if (!shownGenes.contains(gene))
                    shownGenes.add(gene);
            }
            shownGenes.sort(Gene::compareTo);
            ControllerMediator.getInstance().drawGenes(shownGenes);
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("adding selected genes");
            e.printStackTrace();
        } finally {
            enableAssociatedFunctionality();
        }
    }

    /**
     * Removes genes selected in shown genes table from shown genes table,
     * and redraws the isoform plot
     */
    @FXML
    protected void handleRemoveSelectedButtonAction() {
        disableAssociatedFunctionality();
        try {
            ObservableList<Gene> genesToRemove = shownGenesTable.getSelectionModel().getSelectedItems();
            shownGenes.removeAll(genesToRemove);
            ControllerMediator.getInstance().drawGenes(shownGenes);
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
     * Adds all genes to shown genes table and draws them
     */
    @FXML
    protected void handleAddAllButtonAction() {
        disableAssociatedFunctionality();
        try {
            shownGenes.clear();
            shownGenes.addAll(genes);
            ControllerMediator.getInstance().drawGenes(shownGenes);
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
        TableColumn<Gene,String> numIsoforms = new TableColumn("# Isoforms");
        numIsoforms.setCellValueFactory(new PropertyValueFactory("numIsoforms"));

        genesTable.getColumns().setAll(geneIDCol , geneName, numIsoforms);
        genesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Gives shown genes table two columns (one for gene ID, one for gene name)
     * Shown genes table is populated with all genes users has selected to display
     */
    private void setUpShownGenesTableView() {
        shownGenesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        shownGenes = FXCollections.observableArrayList();
        shownGenesTable.setItems((ObservableList) shownGenes);
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
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSize();
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

    private void setWindowSize() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(geneSelector, screen.getWidth() * GENE_SELECTOR_SCALE_FACTOR, screen.getHeight() * GENE_SELECTOR_SCALE_FACTOR));
    }
}