package annotation;

import java.util.ArrayList;
import java.util.HashMap;

public class Isoform {
    /**
     * Map of isoform expression level per cell
     * Key is cell isoform is expressed in, value is expression level
     */
    private HashMap<String, Integer> expressionPerCellMap;
    private ArrayList<Exon> exons;
    private String name;
    private boolean hasExonJunctions;

    public Isoform() {
        expressionPerCellMap = new HashMap<>();
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