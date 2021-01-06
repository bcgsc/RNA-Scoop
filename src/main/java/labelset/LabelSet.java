package labelset;

import controller.clusterview.ClusterViewController;
import exceptions.AddClusterWhenNoCellsSelectedException;
import exceptions.AddingClusterMakesEmptyClustersException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mediator.ControllerMediator;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LabelSet {
    private final Map<Integer, Cluster> cellNumberClusterMap;
    private final ObservableList<Cluster> clusters;
    private String name;

    public LabelSet() {
        clusters = FXCollections.observableArrayList();
        cellNumberClusterMap = new HashMap<>();
        name = ControllerMediator.getInstance().getUniqueLabelSetName("Label Set " + (ControllerMediator.getInstance().getNumLabelSets() + 1));
        setUpClusters();
    }

    public LabelSet(Map<Integer, Cluster> cellNumberClusterMap, String name) {
        clusters = FXCollections.observableArrayList();
        this.name = name;
        this.cellNumberClusterMap = cellNumberClusterMap;
        for (Cluster cluster: new LinkedHashSet<>(cellNumberClusterMap.values())) {
            clusters.add(cluster);
            cluster.setLabelSet(this);
        }
        if (!ControllerMediator.getInstance().isCellPlotCleared())
            addCellsToClusters();
    }

    /**
     * Adds all cells in cell plot to appropriate clusters
     */
    public void addCellsToClusters() {
        for (ClusterViewController.CellDataItem cell : ControllerMediator.getInstance().getCells(false))
            addCell(cell);
    }

    /**
     * Adds new cluster to label set containing the selected cells in the t-SNE plot
     */
    public void addClusterFromSelectedCells() throws AddClusterWhenNoCellsSelectedException, AddingClusterMakesEmptyClustersException {
        Set<ClusterViewController.CellDataItem> selectedCells = (Set<ClusterViewController.CellDataItem>) ControllerMediator.getInstance().getCells(true);

        // check if adding new clusters will result in clusters with no cells
        if (selectedCells.size() == 0)
            throw new AddClusterWhenNoCellsSelectedException();
        for (Cluster cluster : clusters) {
            if (selectedCells.containsAll(cluster.getCells()))
                throw new AddingClusterMakesEmptyClustersException(cluster.getName());
        }

        Cluster newCluster = new Cluster(getNewClusterName(), this, selectedCells);
        for (ClusterViewController.CellDataItem selectedCell : selectedCells) {
            for (Cluster cluster : clusters) {
                Set<ClusterViewController.CellDataItem> clusterCells = cluster.getCells();
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
        for (ClusterViewController.CellDataItem cell : cluster.getCells()) {
            cellNumberClusterMap.put(cell.getCellNumber(), clusterToCombineWith);
            clusterToCombineWith.addCell(cell);
        }
    }

    public Cluster getClusterWithName(String name) {
        for (Cluster cluster : clusters) {
            if (cluster.getName().equals(name))
                return cluster;
        }
        return null;
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

    public int getNumCellsInLabelSet() {
        return cellNumberClusterMap.size();
    }

    @Override
    public String toString() {
        StringBuilder labelSetString = new StringBuilder();
        for (int i = 0; i < ControllerMediator.getInstance().getNumCellsToPlot(); i++) {
            Cluster cluster = cellNumberClusterMap.get(i);
            labelSetString.append(cluster.getName()).append("\n");
        }

        return labelSetString.toString();
    }

    /**
     * Adds given cell to the cluster it belongs
     */
    private void addCell(ClusterViewController.CellDataItem cell) {
        Cluster cluster = cellNumberClusterMap.get(cell.getCellNumber());
        if (cluster != null)
            cluster.addCell(cell);
    }

    /**
     * Returns unique name that should be given to next cluster added by cell
     * selection
     */
    private String getNewClusterName() {
        int nextClusterNum = clusters.size() + 1;
        String nextClusterName = "Cluster " + nextClusterNum;
        Set<String> clusterNames = clusters.stream().map(Cluster::getName).collect(Collectors.toSet());

        while (clusterNames.contains(nextClusterName)) {
            nextClusterNum += 1;
            nextClusterName = "Cluster " + nextClusterNum;
        }

        return nextClusterName;
    }

    /**
     * Creates a cluster and adds it to this label set. This cluster has all of the cells
     * in the t-SNE plot.
     */
    private void setUpClusters() {
        Cluster cluster = new Cluster("Cluster 1", this);
        clusters.add(cluster);
        if (!ControllerMediator.getInstance().isCellPlotCleared()) {
            for (ClusterViewController.CellDataItem cell : ControllerMediator.getInstance().getCells(false)) {
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
