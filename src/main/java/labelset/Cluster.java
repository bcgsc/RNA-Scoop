package labelset;

import controller.clusterview.ClusterViewController;
import ui.PointColor;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class Cluster implements Comparable<Cluster> {
    private String name;
    private Color color;
    private LabelSet labelSet;
    private Set<ClusterViewController.CellDataItem> cells;

    public Cluster(String name) {
        this.name = name;
        cells = new HashSet<>();
        color = PointColor.getColor();
    }

    public Cluster(String name, LabelSet labelSet) {
        this(name);
        this.labelSet = labelSet;
    }

    public Cluster(String name, LabelSet labelSet, Set<ClusterViewController.CellDataItem> cells) {
        this.name = name;
        this.labelSet = labelSet;
        this.cells = cells;
        color = PointColor.getColor();
    }

    public void addCell(ClusterViewController.CellDataItem cell) {
        cells.add(cell);
    }

    public void clearCells(){
        cells.clear();
    }

    public void setLabelSet(LabelSet labelSet) {
        this.labelSet = labelSet;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Cluster otherCluster) {
        return getNumber() - otherCluster.getNumber();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns this cluster's number
     * A cluster's number refers to its position in its label set's list of
     * clusters
     */
    public int getNumber() {
        return labelSet.getClusterNumber(this);
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public javafx.scene.paint.Color getJavaFXColor() {
        return javafx.scene.paint.Color.color((double) color.getRed() / 255,
                                            (double) color.getGreen() / 255,
                                             (double) color.getBlue() / 255);
    }

    public Set<ClusterViewController.CellDataItem> getCells() {
        return cells;
    }
}
