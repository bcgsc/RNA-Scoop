package annotation;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import labelset.Cluster;
import labelset.LabelSet;
import mediator.ControllerMediator;
import util.Util;

import java.util.Collection;
import java.util.HashMap;

public class Gene implements Comparable<Gene> {

    /**
     * Map of all isoforms of this gene
     * Key is transcript ID, value is the isoform
     */
    private HashMap<String, Isoform> isoforms;

    /**
     * Map of this gene's max fold changes for each label set
     */
    private HashMap<LabelSet, GeneMaxFoldChange> maxFoldChangeMap;

    private String name;
    private String id;
    private int startNucleotide;
    private int endNucleotide;
    private String chromosome;
    private boolean onPositiveStrand;
    private SimpleObjectProperty<GeneMaxFoldChange> maxFoldChange;

    public Gene(String id, String chromosome, String strand) {
        isoforms = new HashMap<>();
        maxFoldChangeMap = new HashMap<>();
        // initializes startNucleotide to MAX_VALUE and endNucleotide to 0 in order for
        // main.java.parser to correctly set the right values
        this.id = id;
        name = null;
        startNucleotide = Integer.MAX_VALUE;
        endNucleotide = 0;
        this.chromosome = chromosome;
        onPositiveStrand = strand.equals("+");
        maxFoldChange = new SimpleObjectProperty(new GeneMaxFoldChange(0, 0));
    }

    public void addIsoform(String transcriptID, Isoform isoform) {
        isoforms.put(transcriptID, isoform);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartNucleotide(int startNucleotide) {
        this.startNucleotide = startNucleotide;
    }

    public void setEndNucleotide(int endNucleotide) {
        this.endNucleotide = endNucleotide;
    }

    public ObjectProperty<GeneMaxFoldChange> maxFoldChangeProperty() {
        return maxFoldChange;
    }

    public void updateMaxFoldChange() {
        LabelSet labelSetInUse = ControllerMediator.getInstance().getLabelSetInUse();
        maxFoldChange.set(maxFoldChangeMap.get(labelSetInUse));
    }

    public void calculateAndSaveMaxFoldChange(Collection<LabelSet> labelSets) {
        for (LabelSet labelSet : labelSets) {
            if (!maxFoldChangeMap.containsKey(labelSet)) {
                GeneMaxFoldChange maxFoldChange = getMaxFoldChangeForLabelSet(labelSet);
                maxFoldChangeMap.put(labelSet, maxFoldChange);
            }
        }
    }
    /**
     * Removes given label set from the map of max fold changes
     */
    public void removeLabelSet(LabelSet labelSet) {
        maxFoldChangeMap.remove(labelSet);
    }

    public Boolean hasIsoform(String transcriptID) {
        return isoforms.containsKey(transcriptID);
    }

    public Isoform getIsoform(String transcriptID) {
        return isoforms.get(transcriptID);
    }

    public HashMap<String, Isoform> getIsoformsMap() {
        return isoforms;
    }

    public Collection<Isoform> getIsoforms() {
        return isoforms.values();
    }

    public int getNumIsoforms() {
        return isoforms.size();
    }

    public boolean hasMultiExonIsoforms() {
        for (Isoform isoform : isoforms.values()) {
            if (isoform.isMultiExonic())
                return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getStartNucleotide() {
        return startNucleotide;
    }

    public int getEndNucleotide() {
        return endNucleotide;
    }

    public String getChromosome() {
        return chromosome;
    }

    public boolean isOnPositiveStrand() {
        return onPositiveStrand;
    }

    @Override
    public int compareTo(Gene gene) {
        return id.compareTo(gene.getId());
    }

    private GeneMaxFoldChange getMaxFoldChangeForLabelSet(LabelSet labelSet) {
        Collection<Cluster> clusters = labelSet.getClusters();
        double maxFoldChange = 0;
        double maxPercentExpressed= 0;
        for (Isoform isoform : isoforms.values()) {
            double minExpression = Integer.MAX_VALUE;
            double maxExpression = Integer.MIN_VALUE;
            for (Cluster cluster : clusters) {
                double expression = isoform.getAverageExpressionInCluster(cluster, false, true);
                if (expression < minExpression)
                    minExpression = expression;
                if (expression > maxExpression)
                    maxExpression = expression;

                int numExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoform.getId(), cluster, false);
                int numCells = cluster.getCells().size();
                double percentExpressed = (double) numExpressingCells / numCells;
                if (percentExpressed > maxPercentExpressed)
                    maxPercentExpressed = percentExpressed;
            }
            double foldChange = maxExpression / minExpression;
            if (foldChange > maxFoldChange)
                maxFoldChange = foldChange;
        }
        return new GeneMaxFoldChange(Util.roundToOneDecimal(maxFoldChange), maxPercentExpressed);
    }
}
