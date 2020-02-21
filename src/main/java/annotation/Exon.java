package annotation;

import java.util.Objects;

public class Exon implements Comparable<Exon>{
    private int startNucleotide;
    private int endNucleotide;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exon exon = (Exon) o;
        return startNucleotide == exon.startNucleotide &&
                endNucleotide == exon.endNucleotide;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startNucleotide, endNucleotide);
    }
}
