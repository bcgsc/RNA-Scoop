package annotation;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import labelset.Cluster;
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

    private String name;
    private String id;
    private int startNucleotide;
    private int endNucleotide;
    private String chromosome;
    private boolean onPositiveStrand;
    private DoubleProperty maxFoldChange;

    public Gene(String id, String chromosome, String strand) {
        isoforms = new HashMap<>();
        // initializes startNucleotide to MAX_VALUE and endNucleotide to 0 in order for
        // main.java.parser to correctly set the right values
        this.id = id;
        name = null;
        startNucleotide = Integer.MAX_VALUE;
        endNucleotide = 0;
        this.chromosome = chromosome;
        onPositiveStrand = strand.equals("+");
        maxFoldChange = new SimpleDoubleProperty();
        maxFoldChange.setValue(0);
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

    public DoubleProperty maxFoldChangeProperty() {
        return maxFoldChange;
    }

    public void updateMaxFoldChange() {
        Collection<Cluster> clusters = ControllerMediator.getInstance().getClusters(false);
        double newMaxFoldChange = 0;
        for (Isoform isoform : isoforms.values()) {
            double minExpression = Integer.MAX_VALUE;
            double maxExpression = Integer.MIN_VALUE;
            for (Cluster cluster : clusters) {
                double expression = isoform.getExpressionLevelInCluster(cluster, false, true);
                if (expression < minExpression)
                    minExpression = expression;
                if (expression > maxExpression)
                    maxExpression = expression;
            }
            double foldChange = maxExpression / minExpression;
            if (foldChange > newMaxFoldChange)
                newMaxFoldChange = foldChange;
        }
        maxFoldChange.setValue(Util.roundToOneDecimal(newMaxFoldChange));
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
}
