package parser.data;

import java.util.ArrayList;

public class Isoform {
    ArrayList<Exon> exons;

    public Isoform() {
        exons = new ArrayList<>();
    }

    public void addExon(Exon exon) {
        exons.add(exon);
        exons.sort(Exon::compareTo);
    }

    public ArrayList<Exon> getExons() {
        return exons;
    }
}