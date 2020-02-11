package parser.data;

import java.util.ArrayList;
import java.util.HashMap;

public class Isoform {
    /**
     * Map of isoform expression level per cell
     * Key is cell isoform is expressed in, value is expression level
     */
    private HashMap<String, Integer> expressionPerCellMap;
    private ArrayList<Exon> exons;

    public Isoform() {
        expressionPerCellMap = new HashMap<>();
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