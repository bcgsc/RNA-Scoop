package controller;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.utils.MatrixUtils;
import com.jujutsu.utils.TSneUtils;
import exceptions.RNAScoopException;
import exceptions.TSNEInvalidPerplexityException;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import mediator.ControllerMediator;
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
import org.jfree.data.extension.DatasetIterator;
import org.jfree.data.extension.DatasetSelectionExtension;
import org.jfree.data.extension.impl.DatasetExtensionManager;
import org.jfree.data.extension.impl.XYCursor;
import org.jfree.data.extension.impl.XYDatasetSelectionExtension;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.SelectionChangeEvent;
import org.jfree.data.general.SelectionChangeListener;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ui.PointColor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.*;

import static javafx.application.Platform.runLater;

public class TSNEPlotController implements Initializable, InteractiveElementController, SelectionChangeListener<XYCursor> {
    @FXML private VBox tSNEPlotPanel;
    @FXML private Button drawTSNEButton;
    @FXML private TextField perplexity;
    @FXML private SwingNode swingNode;

    private JPanel tSNEPlot;
    private XYSeriesCollection cellsInTSNEPlot;
    private ArrayList<CellDataItem> selectedCells;

    /**
     * Sets up pane in which t-SNE plot is displayed
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tSNEPlot = new JPanel(new GridLayout(1, 2));
        tSNEPlot.setPreferredSize(new Dimension(500, 1000));
        swingNode.setContent(tSNEPlot);
        tSNEPlot.setBackground(Color.WHITE);
        tSNEPlot.setBorder(BorderFactory.createLineBorder(Color.getHSBColor(0,0,0.68f)));
        cellsInTSNEPlot = new XYSeriesCollection();
        selectedCells = new ArrayList<>();
    }

    public Node getTSNEPlot() {
        return tSNEPlotPanel;
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
        cellsInTSNEPlot.removeAllSeries();
        selectedCells.clear();
        tSNEPlot.removeAll();
        tSNEPlot.validate();
        tSNEPlot.repaint();
    }

    public boolean isTSNEPlotCleared() {
        return cellsInTSNEPlot.getSeriesCount() == 0;
    }

    public boolean areCellsSelected() {
        return selectedCells.size() > 0;
    }

    public double getIsoformExpressionLevel(String isoformID) {
        double isoformExpressionSum = 0;
        int numSelected = selectedCells.size();
        for (CellDataItem selectedCell : selectedCells)
            isoformExpressionSum += selectedCell.getIsoformExpressionLevel(isoformID);
        return isoformExpressionSum / numSelected;
    }

    @Override
    public void selectionChanged(SelectionChangeEvent<XYCursor> selectionChangeEvent) {
        if (!isTSNEPlotCleared()) {
            XYDatasetSelectionExtension ext = (XYDatasetSelectionExtension)
                    selectionChangeEvent.getSelectionExtension();
            DatasetIterator<XYCursor> selectionIterator = ext.getSelectionIterator(true);
            selectedCells.clear();
            while (selectionIterator.hasNext()) {
                XYCursor dc = selectionIterator.next();

                selectedCells.add((CellDataItem) cellsInTSNEPlot.getSeries(dc.series).getDataItem(dc.item));
            }
            ControllerMediator.getInstance().drawGenes(ControllerMediator.getInstance().getShownGenes());
        }
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
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableGeneSelector();
        ControllerMediator.getInstance().disableTPMGradientAdjuster();
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableTPMGradientAdjuster();
    }

    private static class CellDataItem extends XYDataItem {
        private double[] isoformExpressionLevels;
        HashMap<String, Integer> isoformIndexMap;

        public CellDataItem(Number x, Number y, double[] isoformExpressionLevels, HashMap<String, Integer> isoformIndexMap) {
            super(x, y);
            this.isoformExpressionLevels = isoformExpressionLevels;
            this.isoformIndexMap = isoformIndexMap;
        }

        public double getIsoformExpressionLevel(String isoformID) {
            Integer isoformIndex = isoformIndexMap.get(isoformID);
            if (isoformIndex != null) {
                return isoformExpressionLevels[isoformIndex];
            }
            else
                return 0;
        }
    }

    private class TSNEPlotMaker implements Runnable {
        private File dataFile;
        private File colorsFile;
        private File isoformLabelsFile;

        @Override
        public void run() {
            clearTSNEPlot();
            runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Drawing t-SNE plot..."));
            try {
                loadFiles();
                double [][] cellIsoformExpressionMatrix = MatrixUtils.simpleRead2DMatrix(dataFile, "\t");
                setTPMMaxAndMinValues(cellIsoformExpressionMatrix);
                double[][] tSNEMatrix = generateTSNEMatrix(cellIsoformExpressionMatrix);
                drawTsne(cellIsoformExpressionMatrix, tSNEMatrix);
                runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Finished drawing t-SNE plot"));
            } catch(RNAScoopException e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage()));
            } catch (Exception e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("drawing the t-SNE plot"));
                e.printStackTrace();
            } finally {
                runLater(TSNEPlotController.this::enableAssociatedFunctionality);
            }
        }

        private void setTPMMaxAndMinValues(double[][] cellIsoformExpressionMatrix) {
            double[] expressionArray = Arrays.stream(cellIsoformExpressionMatrix).flatMapToDouble(Arrays::stream).toArray();
            Arrays.sort(expressionArray);
            int expressionArraySize = expressionArray.length;
            double minTPM = expressionArray[0];
            double maxTPM = expressionArray[expressionArraySize - 1];

            double[] filteredExpressionArray = Arrays.stream(expressionArray).filter(tpm -> tpm >= 1).toArray();
            int filteredExpressionArraySize = filteredExpressionArray.length;
            double filteredMinTPM = filteredExpressionArray[0];
            double filteredMaxTPM = filteredExpressionArray[filteredExpressionArraySize - 1];
            double q1 = filteredExpressionArray[filteredExpressionArraySize / 4];
            double q3 = filteredExpressionArray[filteredExpressionArraySize * 3/4];
            double iqr = q3 - q1;
            int recommendedMinTPM = (int) Double.max(q1 - 1.5 * iqr, filteredMinTPM);
            int recommendedMaxTPM = (int) Double.min(q3 + 1.5 * iqr, filteredMaxTPM);

            ControllerMediator.getInstance().setRecommendedMinTPM(recommendedMinTPM);
            ControllerMediator.getInstance().setRecommendedMaxTPM(recommendedMaxTPM);
            ControllerMediator.getInstance().addMinTPMToGradientMinTPMLabel(minTPM);
            ControllerMediator.getInstance().addMaxTPMToGradientMaxTPMLabel(maxTPM);
        }

        /**
         * Load t-SNE data and labels files
         */
        private void loadFiles() {
            dataFile = new File("matrix.txt");
            isoformLabelsFile = new File("isoform_labels.txt");
            colorsFile = new File("mnist200_labels_int.txt");
        }

        private double [][] generateTSNEMatrix(double[][] cellIsoformExpressionMatrix) throws TSNEInvalidPerplexityException {
            double perplexityValue;
            try {
                perplexityValue = Double.parseDouble(perplexity.getText());
            } catch (NumberFormatException e) {
                throw new TSNEInvalidPerplexityException();
            }
            if(perplexityValue < 0)
                throw new TSNEInvalidPerplexityException();

            int initial_dims = 55;
            BarnesHutTSne tsne = new BHTSne();
            TSneConfiguration config = TSneUtils.buildConfig(cellIsoformExpressionMatrix, 2, initial_dims, perplexityValue, 1000);
            return tsne.tsne(config);
        }


        /**
         * Plots given t-SNE matrix
         */
        private void drawTsne(double[][] cellIsoformExpressionMatrix, double[][] tSNEMatrix) throws FileNotFoundException {
            createDataSet(cellIsoformExpressionMatrix, tSNEMatrix);

            DatasetSelectionExtension<XYCursor> datasetExtension
                    = new XYDatasetSelectionExtension(cellsInTSNEPlot);
            datasetExtension.addChangeListener(TSNEPlotController.this);

            JFreeChart chart = createPlot(datasetExtension);
            ChartPanel panel = new ChartPanel(chart);
            panel.setMouseWheelEnabled(true);

            addSelectionHandler(panel);
            addSelectionManager(cellsInTSNEPlot, datasetExtension, panel);

            tSNEPlot.add(panel);
            tSNEPlot.validate();
        }

        /**
         * Given a t-SNE matrix, creates a collection of series of x, y coordinates that
         * can be plotted. A new series is created for every label
         *
         * NOTE: if labels file does not have a label on a separate line for every cell,
         * cells will be left out of the data set!
         */
        private void createDataSet(double[][] cellIsoformExpressionMatrix, double[][] tSNEMatrix) throws FileNotFoundException {
            Scanner colorScanner = new Scanner(colorsFile);
            HashMap<String, Integer> isoformIndexMap = getIsoformIndexMap();
            int cellIndex = 0;
            while (colorScanner.hasNextLine()) {
                String label = colorScanner.nextLine();
                XYSeries series = new XYSeries(label);
                if (!dataSetHasSeries(series)) {
                    cellsInTSNEPlot.addSeries(series);
                } else {
                    series = cellsInTSNEPlot.getSeries(label);
                }

                double cellX = tSNEMatrix[cellIndex][0];
                double cellY = tSNEMatrix[cellIndex][1];

                CellDataItem cellDataItem = new CellDataItem(cellX, cellY, cellIsoformExpressionMatrix[cellIndex], isoformIndexMap);
                series.add(cellDataItem);
                cellIndex++;
            }
            colorScanner.close();
        }

        private HashMap<String, Integer> getIsoformIndexMap() throws FileNotFoundException {
            Scanner isoformLabelsScanner = new Scanner(isoformLabelsFile);
            HashMap<String, Integer> isoformIndexMap = new HashMap<>();
            int index = 0;
            while (isoformLabelsScanner.hasNextLine()) {
                String isoformLabel = isoformLabelsScanner.nextLine();
                isoformIndexMap.put(isoformLabel, index);
                index++;
            }
            return isoformIndexMap;
        }

        /**
         * @return true if dataset has a series with the same key, false otherwise
         */
        private boolean dataSetHasSeries(Series series) {
            List<XYSeries> dataSetSeries = cellsInTSNEPlot.getSeries();
            for(Series otherSeries : dataSetSeries) {
                if (otherSeries.getKey().equals(series.getKey())) {
                    return true;
                }
            }
            return false;
        }

        private JFreeChart createPlot(DatasetSelectionExtension<XYCursor> ext) {
            JFreeChart chart = ChartFactory.createScatterPlot("", " ", " ", cellsInTSNEPlot);

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
            for(int i = 0; i < cellsInTSNEPlot.getSeriesCount(); i++) {
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
