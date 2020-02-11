package ui.fxml.main.controllers;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.utils.MatrixUtils;
import com.jujutsu.utils.TSneUtils;
import exceptions.TSNEInvalidPerplexityException;
import exceptions.TSNELabelsFileNotFoundException;
import exceptions.RNAScoopException;
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
    private XYSeriesCollection dataSet;

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
    protected void handleDrawTSNEButtonAction() {
        Thread plotTSNE = new Thread(new TSNEPlotMaker());
        plotTSNE.start();
    }

    private class TSNEPlotMaker implements Runnable {
        private File dataFile;
        private File labelsFile;
        @Override
        public void run() {
            Platform.runLater(new TogglePlotButtonsThread(true));
            Platform.runLater(new WriteToConsoleThread("Drawing t-SNE plot...", false));
            try {
                loadFiles();
                double[][] tSNEMatrix = generateTSNEMatrix();
                drawTsne(tSNEMatrix);
                Platform.runLater(new WriteToConsoleThread("Finished drawing t-SNE plot", false));
            } catch(RNAScoopException e) {
                Platform.runLater(new WriteToConsoleThread(e.getMessage(), true));
            } catch (Exception e) {
                Platform.runLater(new WriteToConsoleThread("An unexpected error occurred", true));
            } finally {
                Platform.runLater(new TogglePlotButtonsThread(false));
            }
        }

        /**
         * Load t-SNE data and labels files
         */
        private void loadFiles() throws TSNeDataFileNotFoundException, TSNELabelsFileNotFoundException {
            URL urlToDataFile = getClass().getResource("/ui/resources/mnist2500_X.txt");
            URL urlToLabelsFile = getClass().getResource("/ui/resources/mnist2500_labels_int.txt");
            if(urlToDataFile == null)
                throw new TSNeDataFileNotFoundException();
            if (urlToLabelsFile == null)
                throw new TSNELabelsFileNotFoundException();
            dataFile = new File(urlToDataFile.getFile());
            labelsFile = new File(urlToLabelsFile.getFile());
        }

        private double [][] generateTSNEMatrix() throws TSNEInvalidPerplexityException {
            double perplexityValue;
            try {
                perplexityValue = Double.parseDouble(perplexity.getText());
            } catch (NumberFormatException e) {
                throw new TSNEInvalidPerplexityException();
            }
            if(perplexityValue < 0)
                throw new TSNEInvalidPerplexityException();

            int initial_dims = 55;
            double [][] X = MatrixUtils.simpleRead2DMatrix(dataFile, "   ");
            BarnesHutTSne tsne = new BHTSne();
            TSneConfiguration config = TSneUtils.buildConfig(X, 2, initial_dims, perplexityValue, 1000);
            return tsne.tsne(config);
        }

        /**
         * Plots given t-SNE matrix
         */
        private void drawTsne(double[][] tSNEMatrix) throws FileNotFoundException {
            createDataSet(tSNEMatrix);

            DatasetSelectionExtension<XYCursor> datasetExtension
                    = new XYDatasetSelectionExtension(dataSet);

            JFreeChart chart = createPlot(datasetExtension);
            ChartPanel panel = new ChartPanel(chart);
            panel.setMouseWheelEnabled(true);

            addSelectionHandler(panel);
            addSelectionManager(dataSet, datasetExtension, panel);

            pane.removeAll();
            pane.add(panel);
            pane.validate();
        }

        /**
         * Given a t-SNE matrix, creates a collection of series of x, y coordinates that
         * can be plotted. A new series is created for every label
         *
         * NOTE: if labels file does not have a label on a separate line for every cell,
         * cells will be left out of the data set!
         */
        private void createDataSet(double[][] tSNEMatrix) throws FileNotFoundException {
            dataSet = new XYSeriesCollection();
            Scanner scanner = new Scanner(labelsFile);
            int cellIndex = 0;
            while (scanner.hasNextLine()) {
                String label = scanner.nextLine();
                XYSeries series = new XYSeries(label);
                if (!dataSetHasSeries(series)) {
                    dataSet.addSeries(series);
                } else {
                    series = dataSet.getSeries(label);
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
            List<XYSeries> dataSetSeries = dataSet.getSeries();
            for(Series otherSeries : dataSetSeries) {
                if (otherSeries.getKey().equals(series.getKey())) {
                    return true;
                }
            }
            return false;
        }

        private JFreeChart createPlot(DatasetSelectionExtension<XYCursor> ext) {
            JFreeChart chart = ChartFactory.createScatterPlot("", " ", " ", dataSet);

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
            for(int i = 0; i < dataSet.getSeriesCount(); i++) {
                r.setSeriesShape(i, shape);
                Color seriesColor = PointColor.getColor();
                r.setSeriesFillPaint(i, seriesColor);
                r.setSeriesPaint(i, seriesColor);
                r.setSeriesOutlinePaint(i, seriesColor);
            }
            //add selection specific rendering
            IRSUtilities.setSelectedItemFillPaint(r, ext, Color.white);
        }

        /**
         * Thread which disables/enables the "Draw t-SNE" button and the Perplexity field
         */
        private class TogglePlotButtonsThread implements Runnable {
            private boolean disableState;

            private TogglePlotButtonsThread(boolean disableState) {
                this.disableState = disableState;
            }

            @Override
            public void run() {
                perplexity.setDisable(disableState);
                drawTSNEButton.setDisable(disableState);
            }
        }

        /**
         * Thread which writes messages to the console
         */
        private class WriteToConsoleThread implements Runnable {
            private String message;
            private boolean messageIsError;

            private WriteToConsoleThread(String message, boolean messageIsError) {
                this.message = message;
                this.messageIsError = messageIsError;
            }

            @Override
            public void run() {
                if(messageIsError)
                    consoleController.addConsoleErrorMessage(message);
                else
                    consoleController.addConsoleMessage(message);
            }
        }

    }
}
