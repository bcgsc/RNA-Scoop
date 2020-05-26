package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import mediator.ControllerMediator;
import ui.Main;
import ui.PointColor;
import util.Util;

import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import static javafx.application.Platform.runLater;

public class ClusterManagerController implements Initializable {
    private static final float CLUSTER_CONTROLLER_SCALE_FACTOR = 0.33f;

    @FXML private ScrollPane clusterController;
    @FXML private ListView labelSetsListView;

    private ObservableList<LabelSet> labelSets;
    private LabelSet labelSetInUse;
    private Stage window;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpLabelSetsListView();
        setUpWindow();
    }

    private void setUpLabelSetsListView() {
        labelSets = FXCollections.observableArrayList();
        labelSetsListView.setCellFactory(new Callback<ListView<LabelSet>, ListCell<LabelSet>>() {
            @Override
            public ListCell<LabelSet> call(ListView<LabelSet> lv) {
                return new ListCell<LabelSet>() {
                    @Override
                    public void updateItem(LabelSet labelSet, boolean empty) {
                        super.updateItem(labelSet, empty);
                        textProperty().unbind();
                        if (labelSet != null)
                            textProperty().bind(new SimpleStringProperty(labelSet.getName()));
                    }
                };
            }
        });
        labelSetsListView.setItems(labelSets);
    }

    /**
     * Displays the cluster controller window
     */
    public void display() {
        window.hide();
        window.show();
        runLater(() -> clusterController.requestFocus());
    }

    public void clearLabelSets() {
        labelSets.clear();
    }

    public void clearLabelSetClusterCells() {
        for (LabelSet labelSet : labelSets) {
            for (Cluster cluster : labelSet.getClusters())
                cluster.clearCells();
        }
    }

    public void updateLabelSetClusterCells() {
        Map<Integer, TSNEPlotController.CellDataItem> cellMap = ControllerMediator.getInstance().getCellMap();
        for (Map.Entry<Integer, TSNEPlotController.CellDataItem> entry : cellMap.entrySet()) {
            for (LabelSet labelSet : labelSets)
                labelSet.addCell(entry.getKey(), entry.getValue());
        }
    }

    public void addLabelSet(Map<Integer, Cluster> cellMap) {
        LabelSet labelSet = new LabelSet(cellMap);
        labelSets.add(labelSet);
        if (labelSets.size() == 1)
            labelSetInUse = labelSet;
    }

    public LabelSet getLabelSetInUse() {
        return labelSetInUse;
    }

    public List<Cluster> getAllClusters() {
       return labelSetInUse.getClusters();
    }

    public double getIsoformExpressionLevelInCluster(String isoformID, Cluster cluster, boolean onlySelected) {
        double isoformExpressionSum = 0;
        Collection<TSNEPlotController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = ControllerMediator.getInstance().getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();

        int numSelected = cellsInCluster.size();
        for (TSNEPlotController.CellDataItem selectedCell : cellsInCluster)
            isoformExpressionSum += selectedCell.getIsoformExpressionLevel(isoformID);
        return isoformExpressionSum / numSelected;
    }

    public double getFractionOfExpressingCells(String isoformID, Cluster cluster, boolean onlySelected) {
        double numExpressingCells = 0;
        Collection<TSNEPlotController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = ControllerMediator.getInstance().getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();

        int numSelected = cellsInCluster.size();
        for (TSNEPlotController.CellDataItem selectedCell : cellsInCluster) {
            if (selectedCell.getIsoformExpressionLevel(isoformID) > 0)
                numExpressingCells++;
        }
        return numExpressingCells / numSelected;
    }

    public class LabelSet {
        private Map<Integer, Cluster> cellMap;
        private ArrayList<Cluster> clusters;
        private String name;

        private LabelSet() {
            clusters = new ArrayList<>();
            cellMap = new HashMap<>();
            name = "Label Set " + (labelSets.size() + 1);
            Cluster cluster = new Cluster("Cluster 1", 1);
            for (int i = 0; i < ControllerMediator.getInstance().getNumCellsToPlot(); i++) {
                cellMap.put(i, cluster);
            }
        }

        private LabelSet(Map<Integer, Cluster> cellMap) {
            clusters = new ArrayList<>();
            name = "Label Set " + (labelSets.size() + 1);
            this.cellMap = cellMap;
            clusters.addAll(Util.asSortedList(new HashSet<>(cellMap.values())));
        }

        private void addCell(Integer cellNumber, TSNEPlotController.CellDataItem cell) {
            Cluster cluster = cellMap.get(cellNumber);
            cluster.addCell(cell);
        }

        public ArrayList<Cluster> getClusters() {
            return clusters;
        }

        public Cluster getCellCluster(Integer cellNumber) {
            return cellMap.get(cellNumber);
        }

        public String getName() {
            return name;
        }
    }


    public static class Cluster implements Comparable<Cluster> {
        private int number;
        private String label;
        private Color color;
        private HashSet<TSNEPlotController.CellDataItem> cells;

        public Cluster(String label, int number) {
            this.number = number;
            this.label = label;
            cells = new HashSet<>();
            color = PointColor.getColor();
        }

        @Override
        public int compareTo(Cluster otherCluster) {
            return number - otherCluster.getNumber();
        }

        public int getNumber() {
            return number;
        }

        public String getLabel() {
            return label;
        }

        public Color getColor() {
            return color;
        }

        public javafx.scene.paint.Color getJavaFXColor() {
            return javafx.scene.paint.Color.color((double) color.getRed() / 255,
                                                (double) color.getGreen() / 255,
                                                 (double) color.getBlue() / 255);
        }

        private HashSet<TSNEPlotController.CellDataItem> getCells() {
            return cells;
        }

        private void setColor(javafx.scene.paint.Color color) {
            this.color = new Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue());
        }

        private void addCell(TSNEPlotController.CellDataItem cell) {
            cells.add(cell);
        }

        private void clearCells(){
            cells.clear();
        }

    }

    /**
     * Sets up gene selector window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Cluster Manager");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void setWindowSizeAndDisplay() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(clusterController, screen.getWidth() * CLUSTER_CONTROLLER_SCALE_FACTOR, screen.getHeight() *  CLUSTER_CONTROLLER_SCALE_FACTOR));
    }
}
