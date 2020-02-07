package parser.data;

import java.util.ArrayList;

public class Isoform {
    private ArrayList<Exon> exons;

    public Isoform() {
        exons = new ArrayList<>();
    }

    public void addExon(Exon exon) {
        if (!exons.contains(exon)) {
            exons.add(exon);
            exons.sort(Exon::compareTo);
        }
    }

    public ArrayList<Exon> getExons() {
        return exons;
    }
}