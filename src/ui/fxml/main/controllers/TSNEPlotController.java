package ui.fxml.main.controllers;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.utils.MatrixUtils;
import com.jujutsu.utils.TSneUtils;
import exceptions.InvalidPerplexityException;
import exceptions.TSNELabelsFileNotFoundException;
import exceptions.TSNEPlotException;
import exceptions.TSNeDataFileNotFoundException;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.panel.selectionhandler.EntitySelectionManager;
import org.jfree.chart.panel.selectionhandler.FreeRegionSelectionHandler;
import org.jfree.chart.panel.selectionhandler.MouseClickSelectionHandler;
import org.jfree.chart.panel.selectionhandler.RegionSelectionHandler;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.item.IRSUtilities;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.extension.DatasetSelectionExtension;
import org.jfree.data.extension.impl.DatasetExtensionManager;
import org.jfree.data.extension.impl.XYCursor;
import org.jfree.data.extension.impl.XYDatasetSelectionExtension;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ui.resources.PointColor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class TSNEPlotController implements Initializable {
    @FXML private VBox tSNEPlot;
    @FXML private Button drawTSNEButton;
    @FXML private TextField perplexity;
    @FXML private SwingNode canvas;

    private ConsoleController consoleController;
    private JPanel pane;
    private XYSeriesCollection dataset;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pane = new JPanel(new GridLayout(1, 2));
        pane.setPreferredSize(new Dimension(500, 1000));
        canvas.setContent(pane);
        pane.setBackground(Color.WHITE);
        pane.setBorder(BorderFactory.createLineBorder(Color.getHSBColor(0,0,0.68f)));
    }

    public void initConsoleController(ConsoleController consoleController) {
        this.consoleController = consoleController;
    }

    public VBox getTSNEPlot() {
        return tSNEPlot;
    }

    /**
     * When "Draw t-SNE" button is pressed, draws t-SNE plot
     */
    @FXML
    protected void handleDrawTSNEButtonAction(ActionEvent e) {
        Thread plotTSNE = new Thread(new TSNEPlotMaker());
        plotTSNE.start();
    }

    private class TSNEPlotMaker implements Runnable {

        @Override
        public void run() {
            Platform.runLater(new TogglePlotButtonsThread(true));
            Platform.runLater(new WriteToConsoleThread("Drawing t-SNE plot..."));
            try {
                double[][] tSNEMatrix = generateTSNEMatrix();
                drawTsne(tSNEMatrix);
                Platform.runLater(new WriteToConsoleThread("Finished drawing t-SNE plot..."));
            } catch(TSNEPlotException e) {
                Platform.runLater(new WriteToConsoleThread(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(new WriteToConsoleThread("An unexpected error occurred"));
            } finally {
                Platform.runLater(new TogglePlotButtonsThread(false));
            }
        }

        private double [][] generateTSNEMatrix() throws InvalidPerplexityException, TSNeDataFileNotFoundException {
            double perplexityValue;
            try {
                perplexityValue = Double.parseDouble(perplexity.getText());
            } catch (NumberFormatException e) {
                throw new InvalidPerplexityException();
            }
            if(perplexityValue < 0)
                throw new InvalidPerplexityException();

            URL path = getClass().getResource("/resources/mnist2500_X.txt");
            if(path == null)
                throw new TSNeDataFileNotFoundException();

            int initial_dims = 55;
            double [][] X = MatrixUtils.simpleRead2DMatrix(new File(path.getFile()), "   ");
            BarnesHutTSne tsne = new BHTSne();
            TSneConfiguration config = TSneUtils.buildConfig(X, 2, initial_dims, perplexityValue, 1000);
            return tsne.tsne(config);
        }

        /**
         * Plots given t-SNE matrix
         */
        private void drawTsne(double[][] tSNEMatrix) throws FileNotFoundException, TSNELabelsFileNotFoundException {
            createDataSet(tSNEMatrix);

            DatasetSelectionExtension<XYCursor> datasetExtension
                    = new XYDatasetSelectionExtension(dataset);

            JFreeChart chart = createPlot(datasetExtension);
            ChartPanel panel = new ChartPanel(chart);
            panel.setMouseWheelEnabled(true);

            addSelectionHandler(panel);
            addSelectionManager(dataset, datasetExtension, panel);

            pane.removeAll();
            pane.add(panel);
            pane.validate();
        }

        /**
         * Given a t-SNE matrix, creates a collection of series of x, y coordinates that
         * can be plotted. A new series is created for every label
         */
        private void createDataSet(double[][] tSNEMatrix) throws FileNotFoundException, TSNELabelsFileNotFoundException {
            URL path = getClass().getResource("/ui/resources/mnist2500_labels_int.txt");
            if (path == null)
                throw new TSNELabelsFileNotFoundException();
            dataset = new XYSeriesCollection();
            File file = new File(path.getFile());
            Scanner scanner = new Scanner(file);
            int cellIndex = 0;
            while (scanner.hasNextLine()) {
                String label = scanner.nextLine();
                XYSeries series = new XYSeries(label);
                if (!dataSetHasSeries(series)) {
                    dataset.addSeries(series);
                } else {
                    series = dataset.getSeries(label);
                }
                double cellX = tSNEMatrix[cellIndex][0];
                double cellY = tSNEMatrix[cellIndex][1];
                series.add(cellX, cellY);
                cellIndex++;
            }
            scanner.close();
        }

        /**
         * @return true if dataset has a series with the same key, false otherwise
         */
        private boolean dataSetHasSeries(Series series) {
            List<XYSeries> dataSetSeries = dataset.getSeries();
            for(Series otherSeries : dataSetSeries) {
                if (otherSeries.getKey().equals(series.getKey())) {
                    return true;
                }
            }
            return false;
        }

        private JFreeChart createPlot(DatasetSelectionExtension<XYCursor> ext) {
            JFreeChart chart = ChartFactory.createScatterPlot("", " ", " ", dataset);

            XYPlot plot = (XYPlot) chart.getPlot();
            setPlotViewProperties(plot);
            adjustPlotPointRendering(ext, plot);

            //register plot as selection change listener
            ext.addChangeListener(plot);

            return chart;
        }

        private void addSelectionManager(XYSeriesCollection dataset, DatasetSelectionExtension<XYCursor> datasetExtension, ChartPanel panel) {
            DatasetExtensionManager dExManager = new DatasetExtensionManager();
            dExManager.registerDatasetExtension(datasetExtension);
            panel.setSelectionManager(new EntitySelectionManager(panel,
                    new Dataset[] { dataset }, dExManager));
        }

        private void addSelectionHandler(ChartPanel panel) {
            RegionSelectionHandler selectionHandler = new FreeRegionSelectionHandler();
            panel.addMouseHandler(selectionHandler);
            panel.addMouseHandler(new MouseClickSelectionHandler());
            panel.removeMouseHandler(panel.getZoomHandler());
        }

        private void setPlotViewProperties(XYPlot plot) {
            plot.setDomainPannable(true);
            plot.setRangePannable(true);
            plot.setDomainCrosshairVisible(true);
            plot.setRangeCrosshairVisible(true);
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinesVisible(false);
            plot.getDomainAxis().setTickMarksVisible(false);
            plot.getDomainAxis().setTickLabelsVisible(false);
            plot.getRangeAxis().setTickMarksVisible(false);
            plot.getRangeAxis().setTickLabelsVisible(false);
        }

        private void adjustPlotPointRendering(DatasetSelectionExtension<XYCursor> ext, XYPlot plot) {
            XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();
            r.setBaseShapesVisible(true);
            r.setBaseShapesFilled(true);
            r.setUseFillPaint(true);
            Shape shape  = new Ellipse2D.Double(0,0,5,5);
            for(int i = 0; i < dataset.getSeriesCount(); i++) {
                r.setSeriesShape(i, shape);
                Color seriesColor = PointColor.getColor();
                r.setSeriesFillPaint(i, seriesColor);
                r.setSeriesPaint(i, seriesColor);
                r.setSeriesOutlinePaint(i, seriesColor);
            }
            //add selection specific rendering
            IRSUtilities.setSelectedItemFillPaint(r, ext, Color.white);
        }

        private class TogglePlotButtonsThread implements Runnable {
            private boolean disable;

            private TogglePlotButtonsThread(boolean disable) {
                this.disable = disable;
            }

            @Override
            public void run() {
                perplexity.setDisable(disable);
                drawTSNEButton.setDisable(disable);
            }
        }

        private class WriteToConsoleThread implements Runnable {
            private String message;

            private WriteToConsoleThread(String message) {
                this.message = message;
            }

            @Override
            public void run() {
                consoleController.addConsoleMessage(message);
            }
        }

    }
}
