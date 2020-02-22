package annotation;


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
    private boolean isPositiveSense;

    public Gene(String id, String chromosome, String strand) {
        isoforms = new HashMap<>();
        // initializes startNucleotide to MAX_VALUE and endNucleotide to 0 in order for
        // main.java.parser to correctly set the right values
        this.id = id;
        name = null;
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

    public void setName(String name) {
        this.name = name;
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
        return isPositiveSense;
    }

    @Override
    public int compareTo(Gene gene) {
        return id.compareTo(gene.getId());
    }
}
