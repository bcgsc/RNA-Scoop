package ui.resources;

import smile.plot.swing.PlotCanvas;

public class TSNEPlotCanvas extends PlotCanvas {

    public TSNEPlotCanvas(double[] lowerBound, double[] upperBound) {
        super(lowerBound, upperBound);
    }
}