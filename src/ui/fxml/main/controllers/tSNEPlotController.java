package ui.fxml.main.controllers;

import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.apache.commons.csv.CSVFormat;
import smile.data.DataFrame;
import smile.io.Read;
import smile.manifold.TSNE;
import smile.math.MathEx;
import smile.plot.swing.Palette;
import smile.plot.swing.PlotCanvas;
import smile.projection.PCA;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class tSNEPlotController implements Runnable{
    @FXML private VBox tSNEPlot;
    @FXML private Button drawTSNEButton;
    @FXML private TextField perplexity;
    @FXML private SwingNode canvas;

    private JPanel panel;
    private double[][] data;
    private int[] labels;
    private char pointLegend = '@';

    public VBox getTSNEPlot() {
        return tSNEPlot;
    }

    /**
     * When isoform plot toggle is pressed, toggles visibility of the isoform plot
     */
    @FXML
    protected void drawTSNE(ActionEvent e) {
        try {
            CSVFormat format = CSVFormat.DEFAULT.withDelimiter(' ').withIgnoreSurroundingSpaces(true);
            URL url = getClass().getResource("../../../../sampledata/mnist2500_X.txt");
            System.out.println(url);
            DataFrame dataset = Read.csv(java.nio.file.Paths.get(url.toURI()), format);
            this.data = dataset.toArray();
            URL url2 = getClass().getResource("../../../../sampledata/mnist2500_labels.txt");
            dataset = Read.csv(java.nio.file.Paths.get(url2.toURI()));
            this.labels = dataset.column(0).toIntArray();
        } catch (Exception var4) {
            JOptionPane.showMessageDialog(null, "Failed to load dataset.", "ERROR", JOptionPane.ERROR_MESSAGE);
            System.err.println(var4.getMessage());
        }

        try {
            int perplexityValue = Integer.parseInt(perplexity.getText().trim());
            if (perplexityValue < 10 || perplexityValue > 300) {
                System.out.println("TODO");
                return;
            }
        } catch (Exception var5) {
            System.out.println("TODO");
            return;
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    public JComponent learn() {
        JPanel pane = new JPanel(new GridLayout(1, 2));
        pane.setPreferredSize(new Dimension(500, 500));
        PCA pca = PCA.fit(this.data);
        pca.setProjection(50);
        double[][] X = pca.project(this.data);
        long clock = System.currentTimeMillis();
        TSNE tsne = new TSNE(X, 2, Double.parseDouble(perplexity.getText()), 200.0D, 1000);
        System.out.format("Learn t-SNE from %d samples in %dms\n", this.data.length, System.currentTimeMillis() - clock);
        double[][] y = tsne.coordinates;
        PlotCanvas plot = new PlotCanvas(MathEx.colMin(y), MathEx.colMax(y));

        for(int i = 0; i < y.length; ++i) {
            plot.point(this.pointLegend, Palette.COLORS[this.labels[i]], y[i]);
        }

        plot.setTitle("t-SNE");
        pane.add(plot);
        return pane;
    }

    public void run() {
        drawTSNEButton.setDisable(true);
        perplexity.setDisable(true);

        try {
            JComponent plot = this.learn();
            if (plot != null) {
                canvas.setContent(plot);
            }
        } catch (Exception var2) {
            System.err.println(var2.getMessage());
        }

        drawTSNEButton.setDisable(false);
        perplexity.setDisable(false);
    }
}
