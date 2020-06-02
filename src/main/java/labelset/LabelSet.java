package labelset;

import controller.TSNEPlotController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mediator.ControllerMediator;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LabelSet {
    private final Map<Integer, Cluster> cellNumberClusterMap;
    private final ObservableList<Cluster> clusters;
    private String name;

    public LabelSet() {
        clusters = FXCollections.observableArrayList();
        cellNumberClusterMap = new HashMap<>();
        name = "Label Set " + (ControllerMediator.getInstance().getNumLabelSets() + 1);
        setUpClusters();
    }

    public LabelSet(Map<Integer, Cluster> cellNumberClusterMap) {
        clusters = FXCollections.observableArrayList();
        name = "Label Set " + (ControllerMediator.getInstance().getNumLabelSets() + 1);
        this.cellNumberClusterMap = cellNumberClusterMap;
        for (Cluster cluster: new LinkedHashSet<>(cellNumberClusterMap.values())) {
            clusters.add(cluster);
            cluster.setLabelSet(this);
        }
    }

    /**
     * Adds given cell to the cluster it belongs
     */
    public void addCell(TSNEPlotController.CellDataItem cell) {
        Cluster cluster = cellNumberClusterMap.get(cell.getCellNumber());
        cluster.addCell(cell);
    }

    /**
     * Adds new cluster to label set containing the selected cells in the t-SNE plot
     */
    public void addClusterFromSelectedCells() {
        Set<TSNEPlotController.CellDataItem> selectedCells = ControllerMediator.getInstance().getSelectedCells();
        int clusterNumber = clusters.size() + 1;
        Cluster newCluster = new Cluster("Cluster " + clusterNumber, this, selectedCells);
        for (TSNEPlotController.CellDataItem selectedCell : selectedCells) {
            for (Cluster cluster : clusters) {
                Set<TSNEPlotController.CellDataItem> clusterCells = cluster.getCells();
                if (clusterCells.contains(selectedCell)) {
                    clusterCells.remove(selectedCell);
                    break;
                }
            }
            cellNumberClusterMap.put(selectedCell.getCellNumber(), newCluster);
        }
        clusters.add(newCluster);
    }

    /**
     * Returns cluster before given cluster in this label sets' list of clusters
     * or, if given first cluster in list, returns cluster after it
     */
    public Cluster getClusterToCombineWith(Cluster cluster) {
        int indexOfClusterToRemove = clusters.lastIndexOf(cluster);
        return clusters.get((indexOfClusterToRemove == 0) ? 1 : indexOfClusterToRemove - 1);
    }

    /**
     * Removes given cluster and adds its cells to the cluster it should combine with
     */
    public void removeCluster(Cluster cluster) {
        Cluster clusterToCombineWith = getClusterToCombineWith(cluster);
        int indexOfClusterToRemove = clusters.lastIndexOf(cluster);
        clusters.remove(indexOfClusterToRemove);
        for (TSNEPlotController.CellDataItem cell : cluster.getCells()) {
            cellNumberClusterMap.put(cell.getCellNumber(), clusterToCombineWith);
            clusterToCombineWith.addCell(cell);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObservableList<Cluster> getClusters() {
        return clusters;
    }

    /**
     * Returns the cluster a cell with the given number belongs to in this
     * label set
     * NOTE: if cell1 belongs to cluster1, this does not mean it has been added to cluster1's
     * collection of cells. It just means that, according to the cell number -> cluster map,
     * it should be there, and if addCell(cell1) was called, cell1 would be added to the collection.
     */
    public Cluster getCellCluster(Integer cellNumber) {
        return cellNumberClusterMap.get(cellNumber);
    }

    /**
     * Returns position (1 + index) of the given cluster in this label set's
     * list of clusters
     */
    public int getClusterNumber(Cluster cluster){
        return clusters.lastIndexOf(cluster) + 1;
    }

    public String getName() {
        return name;
    }

    /**
     * Creates a cluster and adds it to this label set. This cluster has all of the cells
     * in the t-SNE plot.
     */
    private void setUpClusters() {
        Cluster cluster = new Cluster("Cluster 1", this);
        clusters.add(cluster);
        if (!ControllerMediator.getInstance().isTSNEPlotCleared()) {
            for (TSNEPlotController.CellDataItem cell : ControllerMediator.getInstance().getCellNumberCellMap().values()) {
                cluster.addCell(cell);
                cellNumberClusterMap.put(cell.getCellNumber(), cluster);
            }
        } else {
            // when the t-SNE plot is drawn, all cells should belong to this cluster
            for (int i = 0; i < ControllerMediator.getInstance().getNumCellsToPlot(); i++)
                cellNumberClusterMap.put(i, cluster);
        }
    }
}
