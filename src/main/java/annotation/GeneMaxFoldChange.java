package annotation;

public class GeneMaxFoldChange implements Comparable<GeneMaxFoldChange>{
    private double maxFoldChange;
    private double maxPercentExpressing;

    public GeneMaxFoldChange(double maxFoldChange, double maxPercentExpressing) {
        this.maxFoldChange = maxFoldChange;
        this.maxPercentExpressing = maxPercentExpressing;
    }

    public void setMaxFoldChange(double maxFoldChange) {
        this.maxFoldChange = maxFoldChange;
    }

    public void setMaxPercentExpressing(double maxPercentExpressing) {
        this.maxPercentExpressing = maxPercentExpressing;
    }

    public double getMaxFoldChange() {
        return maxFoldChange;
    }

    public double getMaxPercentExpressing() {
        return maxPercentExpressing;
    }

    @Override
    public String toString() {
        return String.valueOf(maxFoldChange);
    }

    @Override
    public int compareTo(GeneMaxFoldChange other) {
        int maxFoldChangeCompare = Double.compare(this.getMaxFoldChange(), other.getMaxFoldChange());
        if (maxFoldChangeCompare == 0)
            return Double.compare(this.getMaxPercentExpressing(), other.getMaxPercentExpressing());
        else
            return maxFoldChangeCompare;
    }
}