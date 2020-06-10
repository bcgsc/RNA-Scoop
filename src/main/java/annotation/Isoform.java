package annotation;

import controller.TSNEPlotController;
import labelset.Cluster;
import mediator.ControllerMediator;

import java.util.ArrayList;
import java.util.Collection;

public class Isoform {

    private Gene gene;
    private ArrayList<Exon> exons;
    private String id;
    private String name;

    public Isoform(String id, Gene gene) {
        this.gene = gene;
        this.id = id;
        exons = new ArrayList<>();
        name = null;
    }

    public void addExon(Exon exon) {
        if (!exons.contains(exon)) {
            exons.add(exon);
            exons.sort(Exon::compareTo);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the average expression level of the isoform with the given
     * ID in either all cells in t-SNE plot, or only those selected
     */
    public double getIsoformExpressionLevel(boolean onlySelected) {
        double isoformExpressionSum = 0;
        Collection<TSNEPlotController.CellDataItem> cells;
        cells = ControllerMediator.getInstance().getCells(onlySelected);
        int numCells = cells.size();

        for (TSNEPlotController.CellDataItem cell : cells)
            isoformExpressionSum += cell.getIsoformExpressionLevel(id);

        return isoformExpressionSum / numCells;
    }

    public double getIsoformExpressionLevelInCluster(Cluster cluster, boolean onlySelected) {
        double isoformExpressionSum = 0;
        Collection<TSNEPlotController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = ControllerMediator.getInstance().getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();

        int numSelected = cellsInCluster.size();
        for (TSNEPlotController.CellDataItem selectedCell : cellsInCluster)
            isoformExpressionSum += selectedCell.getIsoformExpressionLevel(id);
        return isoformExpressionSum / numSelected;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Exon> getExons() {
        return exons;
    }

    public Gene getGene() {
        return gene;
    }

    public boolean isMultiExonic() {
        return exons.size() > 1;
    }

    /**
     * NOTE: assumes exons are sorted on increasing start coordinate
     */
    public int getStartNucleotide(){
        return exons.get(0).getStartNucleotide();
    }

    /**
     * NOTE: assumes exons are sorted on increasing start coordinate
     */
    public int getEndNucleotide(){
        return exons.get(exons.size() - 1).getEndNucleotide();
    }
}