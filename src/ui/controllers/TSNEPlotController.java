package ui.controllers;

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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import ui.mediator.ControllerMediator;
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

import static javafx.application.Platform.*;

public class TSNEPlotController implements Initializable, InteractiveElementController {
    @FXML private VBox tSNEPlot;
    @FXML private Button drawTSNEButton;
    @FXML private TextField perplexity;
    @FXML private SwingNode canvas;

    private JPanel pane;
    private XYSeriesCollection dataSet;

    /**
     * Sets up pane in which t-SNE plot is displayed
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pane = new JPanel(new GridLayout(1, 2));
        pane.setPreferredSize(new Dimension(500, 1000));
        canvas.setContent(pane);
        pane.setBackground(Color.WHITE);
        pane.setBorder(BorderFactory.createLineBorder(Color.getHSBColor(0,0,0.68f)));
    }

    public Node getTSNEPlot() {
        return tSNEPlot;
    }

    public void disable() {
        drawTSNEButton.setDisable(true);
        perplexity.setDisable(true);
    }

    public void enable() {
        drawTSNEButton.setDisable(false);
        perplexity.setDisable(false);
    }

    public void clearTSNEPlot() {
        pane.removeAll();
        pane.validate();
        pane.repaint();
    }

    /**
     * When "Draw t-SNE" button is pressed, draws t-SNE plot
     */
    @FXML
    protected void handleDrawTSNEButtonAction() {
        disableAssociatedFunctionality();
        try {
            Thread tSNEPlotMaker = new Thread(new TSNEPlotMaker());
            tSNEPlotMaker.start();
        } catch (Exception e) {
            enableAssociatedFunctionality();
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("drawing the t-SNE plot");
        }
    }

    private void disableAssociatedFunctionality() {
        disable();
        clearTSNEPlot();
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableGeneSelector();
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableGeneSelector();
    }

    private class TSNEPlotMaker implements Runnable {
        private File dataFile;
        private File labelsFile;
        @Override
        public void run() {
            runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Drawing t-SNE plot..."));
            try {
                loadFiles();
                double[][] tSNEMatrix = generateTSNEMatrix();
                drawTsne(tSNEMatrix);
                runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Finished drawing t-SNE plot"));
            } catch(RNAScoopException e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage()));
            } catch (Exception e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("drawing the t-SNE plot"));
            } finally {
                runLater(TSNEPlotController.this::enableAssociatedFunctionality);
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
            dataFile = new File("mnist2500_X.txt");
            labelsFile = new File("mnist2500_labels_int.txt");
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
    }
}
