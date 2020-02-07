package parser.data;


import java.util.HashMap;

public class Gene {

    /**
     * Map of all isoforms of this gene
     * Key is transcript ID, value is the isoform
     */
    private HashMap<String, Isoform> isoforms;

    private int startNucleotide;
    private int endNucleotide;
    private int chromosome;
    private boolean isPositiveSense;

    public Gene(int chromosome, String strand) {
        isoforms = new HashMap<>();
        // initializes startNucleotide to MAX_VALUE and endNucleotide to 0 in order for
        // parser to correctly set the right values
        startNucleotide = Integer.MAX_VALUE;
        endNucleotide = 0;
        this.chromosome = chromosome;
        isPositiveSense = strand.equals("+");
    }

    public void addIsoform(String transcriptID, Isoform isoform) {
        isoforms.put(transcriptID, isoform);
    }

    public Boolean hasIsoform(String transcriptID) {
        return isoforms.containsKey(transcriptID);
    }

    public void setStartNucleotide(int startNucleotide) {
        this.startNucleotide = startNucleotide;
    }

    public void setEndNucleotide(int endNucleotide) {
        this.endNucleotide = endNucleotide;
    }

    public Isoform getIsoform(String transcriptID) {
        return isoforms.get(transcriptID);
    }

    public HashMap<String, Isoform> getIsoforms() {
        return isoforms;
    }

    public int getStartNucleotide() {
        return startNucleotide;
    }

    public int getEndNucleotide() {
        return endNucleotide;
    }

    public int getChromosome() {
        return chromosome;
    }

    public boolean isPositiveSense() {
        return isPositiveSense;
    }

}
