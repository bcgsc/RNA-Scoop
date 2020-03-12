package annotation;

import java.util.ArrayList;

public class Isoform {

    private ArrayList<Exon> exons;
    private String name;
    private boolean hasExonJunctions;

    public Isoform() {
        exons = new ArrayList<>();
        name = null;
        hasExonJunctions = false;
    }

    public void addExon(Exon exon) {
        if (!exons.contains(exon)) {
            exons.add(exon);
            exons.sort(Exon::compareTo);
            if(exons.size() > 1 && !hasExonJunctions)
                hasExonJunctions = true;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Exon> getExons() {
        return exons;
    }

    public boolean hasExonJunctions() {return hasExonJunctions;}
}