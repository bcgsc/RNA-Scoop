package annotation;

import java.util.ArrayList;

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