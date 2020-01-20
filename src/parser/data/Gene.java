package parser.data;


import java.util.HashMap;

public class Gene {
    HashMap<String, Isoform> isoforms;
    int startNucleotide;
    int endNucleotide;
    int chromosome;
    String strand;

    public Gene(int chromosome, String strand) {
        isoforms = new HashMap<>();
        startNucleotide = Integer.MAX_VALUE;
        endNucleotide = 0;
        this.chromosome = chromosome;
        this.strand = strand;
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

    public String getStrand() {
        return strand;
    }

}
