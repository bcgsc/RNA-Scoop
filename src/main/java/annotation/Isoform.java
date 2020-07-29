package annotation;

import controller.TSNEPlotController;
import labelset.Cluster;
import mediator.ControllerMediator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public double getAverageExpression(boolean onlySelected, boolean includeZeros) {
        Collection<TSNEPlotController.CellDataItem> cells = ControllerMediator.getInstance().getCells(onlySelected);
        return getAverageExpressionInCells(includeZeros, cells);
    }

    public double getAverageExpressionInCluster(Cluster cluster, boolean onlySelected, boolean includeZeros) {
        Collection<TSNEPlotController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = ControllerMediator.getInstance().getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();
        return getAverageExpressionInCells(includeZeros, cellsInCluster);
    }

    public double getMedianExpression(boolean onlySelected, boolean includeZeros) {
        Collection<TSNEPlotController.CellDataItem> cells = ControllerMediator.getInstance().getCells(onlySelected);
        return getMedianExpressionInCells(includeZeros, cells);
    }

    public double getMedianExpressionInCluster(Cluster cluster, boolean onlySelected, boolean includeZeros) {
        Collection<TSNEPlotController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = ControllerMediator.getInstance().getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();
        return getMedianExpressionInCells(includeZeros, cellsInCluster);
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

    private double getAverageExpressionInCells(boolean includeZeros, Collection<TSNEPlotController.CellDataItem> cells) {
        double expressionSum = 0;
        int numCells = 0;

        for (TSNEPlotController.CellDataItem cell : cells) {
            double expression = cell.getIsoformExpressionLevel(id);
            if (includeZeros || expression > 0) {
                expressionSum += expression;
                numCells++;
            }
        }

        return expressionSum / numCells;
    }

    private double getMedianExpressionInCells(boolean includeZeros, Collection<TSNEPlotController.CellDataItem> cells) {
        List<TSNEPlotController.CellDataItem> cellList;
        if (!includeZeros)
            cellList = cells.stream().filter(cell -> cell.getIsoformExpressionLevel(id) > 0).collect(Collectors.toList());
        else if (!(cells instanceof List))
            cellList = new ArrayList<>(cells);
        else
            cellList = (List) cells;

        int numCells = cellList.size();
        if (numCells == 0) {
            return 0;
        } else {
            cellList.sort((cell, otherCell) -> (int) Math.round(cell.getIsoformExpressionLevel(id) - otherCell.getIsoformExpressionLevel(id)));
            TSNEPlotController.CellDataItem medianCell = cellList.get(numCells / 2);

            if (numCells % 2 != 0) {
                return medianCell.getIsoformExpressionLevel(id);
            } else {
                TSNEPlotController.CellDataItem medianCellTwo = cellList.get(numCells / 2 - 1);
                return (medianCell.getIsoformExpressionLevel(id) + medianCellTwo.getIsoformExpressionLevel(id)) / 2;
            }
        }
    }
}