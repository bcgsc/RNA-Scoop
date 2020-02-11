package ui.fxml;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import parser.Parser;
import parser.data.Gene;
import ui.fxml.main.controllers.ConsoleController;
import ui.fxml.main.controllers.IsoformPlotController;

import java.net.URL;
import java.util.*;

public class GeneSelectorController implements Initializable {

    @FXML GridPane gridPane;
    @FXML ListView genes;
    @FXML ListView genesViewing;

    private IsoformPlotController isoformPlotController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        gridPane.getColumnConstraints().addAll(column1, column2);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(85);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(15);
        gridPane.getRowConstraints().addAll(row1, row2);
        HashMap<String, Gene> parsedGenes = Parser.getParsedGenes();
        if (parsedGenes != null)
            genes.getItems().addAll(asSortedList(parsedGenes.keySet()));
        genes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        genesViewing.getItems().addAll(IsoformPlotController.getGenesViewing());
        genesViewing.setMouseTransparent(true);
        genesViewing.setFocusTraversable(false);
        Platform.runLater(() -> gridPane.requestFocus());
    }

    public void initIsoformPlotController(IsoformPlotController isoformPlotController) {
        this.isoformPlotController = isoformPlotController;
    }

    @FXML
    protected void handleAddSelectedButtonAction() {
        ObservableList<String> genesToAdd = genes.getSelectionModel().getSelectedItems();
        ObservableList<String> genesAdded = genesViewing.getItems();
        for (String gene : genesToAdd) {
            if (!genesAdded.contains(gene))
                genesAdded.add(gene);
        }
        genesAdded.sort(String::compareTo);
        isoformPlotController.setGenesViewing(genesAdded);
    }

    @FXML
    protected void handleRemoveSelectedButtonAction() {
        ObservableList<String> genesToAdd = genes.getSelectionModel().getSelectedItems();
        ObservableList<String> genesAdded = genesViewing.getItems();
        for (String gene : genesToAdd) {
            if (genesAdded.contains(gene))
                genesAdded.remove(gene);
        }
        genesAdded.sort(String::compareTo);
        isoformPlotController.setGenesViewing(genesAdded);
    }

    private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }
}
