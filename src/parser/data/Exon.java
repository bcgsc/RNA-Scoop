package parser.data;

public class Exon implements Comparable<Exon>{
    int startNucleotide;
    int endNucleotide;

    public Exon(int startNucleotide, int endNucleotide) {
        this.startNucleotide = startNucleotide;
        this.endNucleotide = endNucleotide;
    }

    public int getStartNucleotide() {
        return startNucleotide;
    }

    public int getEndNucleotide() {
        return endNucleotide;
    }

    @Override
    public int compareTo(Exon exon) {
        return getStartNucleotide() - exon.getStartNucleotide();
    }
}
