package ui.fxml.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.controlsfx.control.CheckComboBox;
import parser.Parser;
import parser.data.Gene;

import java.util.HashMap;

public class LoadPathController {

    @FXML private ComboBox path;

    private Label console;
    private CheckComboBox geneSelector;

    public void initData(Label console, CheckComboBox geneSelector) {
        this.console = console;
        this.geneSelector = geneSelector;
    }

    /**
     * When load button is pressed, parses path to file and retrieves parsed genes
     * Adds parsed genes to gene selector's items
     * Displays error (and successful completion) messages in console
     */
    @FXML
    protected void handleLoadButtonAction(ActionEvent event) {
        String consoleText = Parser.readFile((String) path.getValue());
        addLoadedPaths();
        addLoadedGenes();
        console.setText(consoleText);
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
