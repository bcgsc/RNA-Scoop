package controller;

import com.jujutsu.tsne.*;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.utils.MatrixOps;
import com.jujutsu.utils.TSneUtils;
import exceptions.RNAScoopException;
import exceptions.TSNEInvalidPerplexityException;
import javafx.application.Platform;
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
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.DataItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.panel.selectionhandler.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.item.IRSUtilities;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.extension.DatasetCursor;
import org.jfree.data.extension.DatasetSelectionExtension;
import org.jfree.data.extension.impl.DatasetExtensionManager;
import org.jfree.data.extension.impl.XYCursor;
import org.jfree.data.extension.impl.XYDatasetSelectionExtension;
import org.jfree.data.general.SelectionChangeEvent;
import org.jfree.data.general.SelectionChangeListener;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ui.PointColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.net.URL;
import java.util.List;
import java.util.*;

import static javafx.application.Platform.runLater;

public class TSNEPlotController implements Initializable, InteractiveElementController, SelectionChangeListener<XYCursor> {
    @FXML private VBox tSNEPlotPanel;
    @FXML private Button drawTSNEButton;
    @FXML private TextField perplexity;
    @FXML private SwingNode swingNode;

    private TSNEPlotInfo tSNEPlotInfo;
    private JPanel tSNEPlotHolder;
    private ChartPanel tSNEPlot;
    private CellSelectionManager cellSelectionManager;
    private XYSeriesCollection cellsInTSNEPlot;

    /**
     * Sets up pane in which t-SNE plot is displayed
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpTSNEPlotHolder();
        swingNode.setContent(tSNEPlotHolder);
        cellsInTSNEPlot = new XYSeriesCollection();
    }

    public Node getTSNEPlot() {
        return tSNEPlotPanel;
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        drawTSNEButton.setDisable(true);
        perplexity.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        drawTSNEButton.setDisable(false);
        perplexity.setDisable(false);
    }

    public void setTSNEPlotInfo(double[][] cellIsoformMatrix, HashMap<String, Integer> isoformIndexMap,
                                ArrayList<String> cellLabels) {
        tSNEPlotInfo = new TSNEPlotInfo(cellIsoformMatrix, isoformIndexMap, cellLabels);
    }

    public void clearTSNEPlot() {
        if (!isTSNEPlotCleared()) {
            tSNEPlotHolder.remove(tSNEPlot);
            tSNEPlot = null;
            cellSelectionManager = null;
            cellsInTSNEPlot.removeAllSeries();
            tSNEPlotHolder.revalidate();
            tSNEPlotHolder.repaint();
        }
    }

    public boolean isTSNEPlotCleared() {
        return tSNEPlot == null;
    }

    public boolean areCellsSelected() {
        if (cellSelectionManager != null)
            return cellSelectionManager.getSelectedCells().size() > 0;
        return false;
    }

    /**
     * Returns the average expression level of the isoform with the given
     * ID in the cells selected in the t-SNE plot
     */
    public double getIsoformExpressionLevel(String isoformID) {
        double isoformExpressionSum = 0;
        int numSelected = 0;
        HashMap<Integer, ArrayList<CellDataItem>> selectedCells = cellSelectionManager.getSelectedCells();
        for (ArrayList<CellDataItem> selectedCellsInCluster : selectedCells.values()) {
            numSelected += selectedCellsInCluster.size();
            for (CellDataItem selectedCell : selectedCellsInCluster)
                isoformExpressionSum += selectedCell.getIsoformExpressionLevel(isoformID);
        }
        return isoformExpressionSum / numSelected;
    }

    public double getIsoformExpressionLevelInCluster(String isoformID, int clusterNumber) {
        double isoformExpressionSum = 0;
        HashMap<Integer, ArrayList<CellDataItem>> selectedCells = cellSelectionManager.getSelectedCells();
        ArrayList<CellDataItem> selectedCellsInCluster = selectedCells.get(clusterNumber);
        int numSelected = selectedCellsInCluster.size();
        for (CellDataItem selectedCell : selectedCellsInCluster)
            isoformExpressionSum += selectedCell.getIsoformExpressionLevel(isoformID);
        return isoformExpressionSum / numSelected;
    }

    public Set<Integer> getSelectedClusterNumbers() {
        if (cellSelectionManager != null)
            return cellSelectionManager.getSelectedCells().keySet();
        else
            return new HashSet<>();
    }

    public HashMap<Integer, javafx.scene.paint.Color> getSelectedClusterColors() {
        HashMap<Integer, javafx.scene.paint.Color> clusterColorMap = new HashMap<>();
        XYPlot plot = (XYPlot) tSNEPlot.getChart().getPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        for (Integer clusterNumber : getSelectedClusterNumbers()) {
            Color color = (Color) renderer.getSeriesFillPaint(clusterNumber - 1);
            clusterColorMap.put(clusterNumber, new javafx.scene.paint.Color((double) color.getRed() / 255,
                                                                          (double) color.getGreen() / 255,
                                                                           (double) color.getBlue() / 255,
                                                                         (double) color.getAlpha() / 255));
        }
        return clusterColorMap;
    }

    public double getFractionOfExpressingCells(String isoformID, int clusterNumber) {
        double numExpressingCells = 0;
        HashMap<Integer, ArrayList<CellDataItem>> selectedCells = cellSelectionManager.getSelectedCells();
        ArrayList<CellDataItem> selectedCellsInCluster = selectedCells.get(clusterNumber);
        int numSelected = selectedCellsInCluster.size();
        for (CellDataItem selectedCell : selectedCellsInCluster) {
            if (selectedCell.getIsoformExpressionLevel(isoformID) > 0)
                numExpressingCells++;
        }
        return numExpressingCells / numSelected;
    }

    public void selectCellsIsoformsExpressedIn(Collection<String> isoformIDs) {
        cellSelectionManager.selectCellsIsoformsExpressedIn(isoformIDs);
    }

    /**
     * When the cells selected in the t-SNE plot changes, redraws the genes in isoform plot
     */
    @Override
    public void selectionChanged(SelectionChangeEvent<XYCursor> selectionChangeEvent) {
        ControllerMediator.getInstance().redrawIsoformGraphics();
    }

    /**
     * When "Draw t-SNE" button is pressed, draws t-SNE plot and deselects any
     * selected isoforms
     */
    @FXML
    protected void handleDrawTSNEButtonAction() {
        clearTSNEPlot();
        ControllerMediator.getInstance().deselectAllIsoforms();
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

    /**
     * Creates box where t-SNE plot is drawn (box is white with grey border)
     * Makes sure t-SNE plot resizes when t-SNE plot panel resizes (height doesn't
     * change on Windows when t-SNE plot panel height changes, so added code to fix
     * this)
     */
    private void setUpTSNEPlotHolder() {
        tSNEPlotHolder = new JPanel(new GridLayout(1, 2));
        tSNEPlotHolder.setBackground(Color.WHITE);
        // the preferred width does not matter (will automatically resize to right width), but
        // if preferred height is too small will not take up all of t-SNE plot panel, which is why
        // the preferred height is set to a large number
        tSNEPlotHolder.setPreferredSize(new Dimension(500, Integer.MAX_VALUE));
        tSNEPlotHolder.setBorder(BorderFactory.createLineBorder(Color.getHSBColor(0,0,0.68f)));
        tSNEPlotPanel.heightProperty().addListener((ov, oldValue, newValue) -> {
            // resetting preferred size and repainting and revalidating t-SNE plot (after short delay)
            // results in t-SNE plot resizing on Windows
            Platform.runLater(() -> {
                tSNEPlotHolder.revalidate();
                tSNEPlotHolder.repaint();
            });
        });
    }

    /**
     * Holds all the information to generate the t-SNE plot
     */
    private static class TSNEPlotInfo {
        /**
         * Matrix in which each row represents a cell, each column an isoform
         * and each number the level of expression of that isoform in the particular
         * cell
         */
        private double[][] cellIsoformMatrix;
        /**
         * Maps each isoform ID to its column number in the matrix (e.g. if the
         * first column represents IsoformA, IsoformA's ID will be mapped to 0)
         */
        private HashMap<String, Integer> isoformIndexMap;
        /**
         * Group each cell in matrix belongs to. If cell represented by the first
         * row in the matrix is in group "T Cells", cellLabels[0] = "T Cells"
         */
        private ArrayList<String> cellLabels;

        public TSNEPlotInfo(double[][] cellIsoformMatrix, HashMap<String, Integer> isoformIndexMap,
                            ArrayList<String> cellLabels) {
            this.cellIsoformMatrix = cellIsoformMatrix;
            this.isoformIndexMap = isoformIndexMap;
            this.cellLabels = cellLabels;
        }

        public double[][] getCellIsoformMatrix() {
            return cellIsoformMatrix;
        }

        public HashMap<String, Integer> getIsoformIndexMap() {
            return isoformIndexMap;
        }

        public ArrayList<String> getCellLabels() {
            return cellLabels;
        }
    }

    private static class Cluster extends XYSeries {
        private int clusterNumber;

        public Cluster(String label, int clusterNumber) {
            super(getClusterKey(clusterNumber, label));
            this.clusterNumber = clusterNumber;
        }

        public static Comparable getClusterKey(int clusterNumber, String label) {
                return "(" + clusterNumber + ") " + label;
        }

        public int getClusterNumber() {
            return clusterNumber;
        }
    }

    /**
     * Represents a cell in the t-SNE plot
     */
    private class CellDataItem extends XYDataItem {
        /**
         * Each number is the level of expression of some isoform in this cell.
         */
        private double[] isoformExpressionLevels;

        public CellDataItem(Number x, Number y, double[] isoformExpressionLevels) {
            super(x, y);
            this.isoformExpressionLevels = isoformExpressionLevels;
        }

        /**
         * Returns the level of expression of the isoform with the given ID
         * in this cell. Returns 0 if that information isn't stored
         */
        public double getIsoformExpressionLevel(String isoformID) {
            Integer isoformIndex = tSNEPlotInfo.getIsoformIndexMap().get(isoformID);
            if (isoformIndex != null) {
                return isoformExpressionLevels[isoformIndex];
            }
            else
                return 0;
        }
    }

    /**
     * Is the t-SNE plot lasso selection tool
     */
    private static class TSNEPlotFreeRegionSelectionHandler extends FreeRegionSelectionHandler {
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            ControllerMediator.getInstance().deselectAllIsoforms();
            ControllerMediator.getInstance().redrawIsoformGraphics();
        }
    }

    /**
     * Manages selection/deselection of cells in the t-SNE plot
     */
    private class CellSelectionManager implements SelectionManager {
        private DatasetExtensionManager extensionManager;
        private HashMap<Integer, ArrayList<CellDataItem>> selectedCells;

        public CellSelectionManager(DatasetExtensionManager extensionManager) {
            selectedCells = new HashMap<>();
            this.extensionManager = extensionManager;
        }

        /**
         * Clears selected cells in t-SNE plot, then selects cells in which isoforms with
         * the given IDs are expressed
         */
        public void selectCellsIsoformsExpressedIn(Collection<String> isoformIDs) {
            List<XYSeries> cellGroups = cellsInTSNEPlot.getSeries();
            muteAll();
            clearSelection();
            for (XYSeries cellGroup : cellGroups) {
                for (XYDataItem dataItem : cellGroup.getItems()) {
                    CellDataItem cell = (CellDataItem) dataItem;
                    if (shouldSelectCell(cell, isoformIDs))
                        select(cell);
                }
            }
            unmuteAndTrigger();
            tSNEPlot.repaint();
        }

        /**
         * Selects cell at given (x, y) coordinates (if cell exists there)
         */
        @Override
        public void select(double x, double y) {
            double scaleX = tSNEPlot.getScaleX();
            double scaleY = tSNEPlot.getScaleY();
            if (scaleX != 1.0D || scaleY != 1.0D) {
                x /= scaleX;
                y /= scaleY;
            }
            EntityCollection entities = tSNEPlot.getChartRenderingInfo().getEntityCollection();
            for (ChartEntity chartEntity : entities.getEntities())
                if (chartEntity instanceof DataItemEntity) {
                    XYItemEntity xyItemEntity = (XYItemEntity) chartEntity;
                    if (xyItemEntity.getArea().contains(new Point2D.Double(x, y))) {
                        select(xyItemEntity);
                    }
                }
            }

        /**
         * Selects any cells contained in given lasso selection
         */
        @Override
        public void select(GeneralPath pathSelection) {
            double scaleX = tSNEPlot.getScaleX();
            double scaleY = tSNEPlot.getScaleY();
            GeneralPath selection;
            if (scaleX == 1.0D && scaleY == 1.0D) {
                selection = pathSelection;
            } else {
                AffineTransform st = AffineTransform.getScaleInstance(1.0D / scaleX, 1.0D / scaleY);
                Area selectionArea = new Area(pathSelection);
                selectionArea.transform(st);
                selection = new GeneralPath(selectionArea);
            }
            muteAll();
            EntityCollection entities = tSNEPlot.getChartRenderingInfo().getEntityCollection();
            for (ChartEntity chartEntity : entities.getEntities()) {
                if (chartEntity instanceof XYItemEntity) {
                    XYItemEntity xyItemEntity = (XYItemEntity) chartEntity;
                    Area selectionShape = new Area(selection);
                    Area entityShape = new Area(xyItemEntity.getArea());
                    if (selectionShape.contains(entityShape.getBounds())) {
                        select(xyItemEntity);
                    } else {
                        entityShape.subtract(selectionShape);
                        if (entityShape.isEmpty()) {
                            select(xyItemEntity);
                        }
                    }
                }
            }
            unmuteAndTrigger();
        }

        /**
         * Unnecessary therefore not implemented
         */
        @Override
        public void select(Rectangle2D rectangle2D) {}

        /**
         * Clears all selected cells in t-SNE plot
         */
        @Override
        public void clearSelection() {
            if (extensionManager.supports(cellsInTSNEPlot, DatasetSelectionExtension.class)) {
                DatasetSelectionExtension<?> selectionExtension = extensionManager.getExtension(cellsInTSNEPlot, DatasetSelectionExtension.class);
                selectedCells.clear();
                selectionExtension.clearSelection();
            }
        }

        public HashMap<Integer, ArrayList<CellDataItem>> getSelectedCells() {
            return selectedCells;
        }

        private void muteAll() {
            this.setNotifyOnListenerExtensions(false);
        }

        private void unmuteAndTrigger() {
            this.setNotifyOnListenerExtensions(true);
        }

        /**
         * Finds actual cell graphic in t-SNE plot associated with given cell data
         * item and selects it
         */
        private void select(CellDataItem cell) {
            EntityCollection entities = tSNEPlot.getChartRenderingInfo().getEntityCollection();
            for (ChartEntity chartEntity : entities.getEntities()) {
                if (chartEntity instanceof XYItemEntity) {
                    XYItemEntity xyItemEntity = (XYItemEntity) chartEntity;
                    CellDataItem xyItemEntityCell = (CellDataItem) cellsInTSNEPlot.getSeries(xyItemEntity.getSeriesIndex()).getItems().get(xyItemEntity.getItem());
                    if (cell == xyItemEntityCell)
                        select(xyItemEntity);
                }
            }
        }

        /**
         * Selects cell in t-SNE plot given the object that represents its graphic
         * in the t-SNE plot
         */
        private void select(XYItemEntity xyItemEntity) {
            if (cellsInTSNEPlot.equals(xyItemEntity.getGeneralDataset()) && extensionManager.supports(xyItemEntity.getGeneralDataset(), DatasetSelectionExtension.class)) {
                Cluster cluster = (Cluster) cellsInTSNEPlot.getSeries(xyItemEntity.getSeriesIndex());
                CellDataItem cell = (CellDataItem) cluster.getItems().get(xyItemEntity.getItem());
                addCellToSelectedCells(cluster, cell);
                DatasetCursor cursor = xyItemEntity.getItemCursor();
                DatasetSelectionExtension selectionExtension = extensionManager.getExtension(xyItemEntity.getGeneralDataset(), DatasetSelectionExtension.class);
                selectionExtension.setSelected(cursor, true);
            }
        }

        /**
         * Returns true if cell expresses one or more of the isoforms associated with the
         * given isoform IDs
         */
        private boolean shouldSelectCell(CellDataItem cell, Collection<String> isoformIDs) {
            for (String isoformID : isoformIDs) {
                if (cell.getIsoformExpressionLevel(isoformID) > 0)
                    return true;
            }
            return false;
        }

        private void setNotifyOnListenerExtensions(boolean notify) {
            DatasetSelectionExtension<?> selectionExtension = (DatasetSelectionExtension) extensionManager.getExtension(cellsInTSNEPlot, DatasetSelectionExtension.class);
            selectionExtension.setNotify(notify);
        }

        private void addCellToSelectedCells(Cluster cluster, CellDataItem cell) {
            int clusterNumber = cluster.getClusterNumber();
            if (selectedCells.containsKey(clusterNumber)) {
                ArrayList<CellDataItem> selectedCellsOfSameCluster =  selectedCells.get(clusterNumber);
                selectedCellsOfSameCluster.add(cell);
            } else {
                ArrayList<CellDataItem> selectedCellsOfSameCluster = new ArrayList<>();
                selectedCellsOfSameCluster.add(cell);
                selectedCells.put(clusterNumber, selectedCellsOfSameCluster);
            }
        }
    }

    private class TSNEPlotMaker implements Runnable {
        private XYSeriesCollection cellsInNewTSNEPlot;

        /**
         * Draws the t-SNE plot and sets the TPM gradient values
         */
        @Override
        public void run() {
            runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Drawing t-SNE plot..."));
            try {
                cellsInNewTSNEPlot = new XYSeriesCollection();
                double[][] tSNEMatrix = generateTSNEMatrix();
                drawTsne(tSNEMatrix);
                setTPMGradientValues();
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
            BHTSne tSNE = new BHTSne();
            double[][] matrix = tSNEPlotInfo.getCellIsoformMatrix();
            int numRows = matrix.length;
            int numColumns = matrix[0].length;
            if (numColumns > numRows)
                matrix = MatrixOps.transposeSerial(matrix);
            TSneConfiguration config = TSneUtils.buildConfig(matrix, 2, initial_dims, perplexityValue, 1000);
            return tSNE.tsne(config);
        }


        /**
         * Plots given t-SNE matrix
         */
        private void drawTsne(double[][] tSNEMatrix) {
            createDataSet(tSNEMatrix);
            DatasetSelectionExtension<XYCursor> datasetExtension
                    = new XYDatasetSelectionExtension(cellsInNewTSNEPlot);
            datasetExtension.addChangeListener(TSNEPlotController.this);

            JFreeChart chart = createPlot(datasetExtension);
            ChartPanel panel = new ChartPanel(chart);
            panel.setMouseWheelEnabled(true);

            addSelectionHandler(panel);
            addSelectionManager(datasetExtension, panel);
            tSNEPlot = panel;

            cellsInTSNEPlot = cellsInNewTSNEPlot;
            tSNEPlotHolder.add(panel);
            tSNEPlotHolder.revalidate();
            tSNEPlotHolder.repaint();
        }

        private void setTPMGradientValues() {
            double[] expressionArray = Arrays.stream(tSNEPlotInfo.getCellIsoformMatrix()).flatMapToDouble(Arrays::stream).toArray();
            Arrays.sort(expressionArray);
            double[] filteredExpressionArray = Arrays.stream(expressionArray).filter(tpm -> tpm >= 1).toArray();

            addMinMaxTPMToTPMGradientLabels(expressionArray);
            setTPMGradientMaxMinToRecommended(filteredExpressionArray);
        }

        /**
         * Given a t-SNE matrix, creates a collection of series of x, y coordinates that
         * can be plotted. A new series is created for every label
         *
         * NOTE: if labels file does not have a label on a separate line for every cell,
         * cells will be left out of the data set!
         */
        private void createDataSet(double[][] tSNEMatrix) {
            int cellIndex = 0;
            int clusterNumber = 0;
            HashSet<String> clusterLabels = new HashSet<>();
            for (String label : tSNEPlotInfo.getCellLabels()) {
                Cluster cluster;
                if (!clusterLabels.contains(label)) {
                    clusterNumber++;
                    cluster = new Cluster(label, clusterNumber);
                    cellsInNewTSNEPlot.addSeries(cluster);
                    clusterLabels.add(label);
                } else {
                    cluster = (Cluster) cellsInNewTSNEPlot.getSeries(Cluster.getClusterKey(clusterNumber, label));
                }

                double cellX = tSNEMatrix[cellIndex][0];
                double cellY = tSNEMatrix[cellIndex][1];
                CellDataItem cellDataItem = new CellDataItem(cellX, cellY, tSNEPlotInfo.getCellIsoformMatrix()[cellIndex]);
                cluster.add(cellDataItem);
                cellIndex++;
            }
        }

        private JFreeChart createPlot(DatasetSelectionExtension<XYCursor> ext) {
            JFreeChart chart = ChartFactory.createScatterPlot("", " ", " ", cellsInNewTSNEPlot);

            XYPlot plot = (XYPlot) chart.getPlot();
            setPlotViewProperties(plot);
            adjustPlotPointRendering(ext, plot);

            //register plot as selection change listener
            ext.addChangeListener(plot);

            return chart;
        }

        private void addSelectionManager(DatasetSelectionExtension<XYCursor> datasetExtension, ChartPanel panel) {
            DatasetExtensionManager dExManager = new DatasetExtensionManager();
            dExManager.registerDatasetExtension(datasetExtension);
            cellSelectionManager = new CellSelectionManager(dExManager);
            panel.setSelectionManager(cellSelectionManager);
        }

        private void addSelectionHandler(ChartPanel panel) {
            RegionSelectionHandler selectionHandler = new TSNEPlotFreeRegionSelectionHandler();
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
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setUseFillPaint(true);
            Shape shape  = new Ellipse2D.Double(0,0,5,5);
            for(int i = 0; i < cellsInNewTSNEPlot.getSeriesCount(); i++) {
                renderer.setSeriesShape(i, shape);
                Color seriesColor = PointColor.getColor();
                renderer.setSeriesFillPaint(i, seriesColor);
                renderer.setSeriesPaint(i, seriesColor);
                renderer.setSeriesOutlinePaint(i, seriesColor);
            }
            //add selection specific rendering
            IRSUtilities.setSelectedItemFillPaint(renderer, ext, Color.white);
        }

        /**
         * Adds the absolute min and max expression values in the expressionArray to the min
         * and max labels of the TPM gradient adjuster window
         *
         * @param expressionArray sorted array of all the isoform expression values for all cells in the
         *                        t-SNE plot
         */
        private void addMinMaxTPMToTPMGradientLabels(double[] expressionArray) {
            int expressionArraySize = expressionArray.length;
            double minTPM = expressionArray[0];
            double maxTPM = expressionArray[expressionArraySize - 1];
            ControllerMediator.getInstance().addMinTPMToGradientMinTPMLabel(minTPM);
            ControllerMediator.getInstance().addMaxTPMToGradientMaxTPMLabel(maxTPM);
        }

        /**
         * Calculates the recommended gradient min and max values and sets the TPM gradient's
         * min and max to them
         *
         * If the size of the filteredExpressionArray is 0, sets the recommended min to 0 and the
         * recommended max to 1
         *
         * If, according to the calculations, the recommended max and min are the same, adds 1 to the
         * recommended max
         *
         * @param filteredExpressionArray sorted array of all the isoform expression values > 0 for all cells in the
         *                                t-SNE plot
         */
        private void setTPMGradientMaxMinToRecommended(double[] filteredExpressionArray) {
            if (filteredExpressionArray.length != 0) {
                int filteredExpressionArraySize = filteredExpressionArray.length;
                double filteredMinTPM = filteredExpressionArray[0];
                double filteredMaxTPM = filteredExpressionArray[filteredExpressionArraySize - 1];
                double q1 = filteredExpressionArray[filteredExpressionArraySize / 4];
                double q3 = filteredExpressionArray[filteredExpressionArraySize * 3/4];
                double iqr = q3 - q1;
                int recommendedMinTPM = (int) Double.max(q1 - 1.5 * iqr, filteredMinTPM);
                int recommendedMaxTPM = (int) Double.min(q3 + 1.5 * iqr, filteredMaxTPM);
                if (recommendedMaxTPM == recommendedMinTPM)
                    recommendedMaxTPM += 1;
                ControllerMediator.getInstance().setRecommendedMinTPM(recommendedMinTPM);
                ControllerMediator.getInstance().setRecommendedMaxTPM(recommendedMaxTPM);
                ControllerMediator.getInstance().setGradientMaxMinToRecommended();
            } else {
                ControllerMediator.getInstance().setRecommendedMinTPM(0);
                ControllerMediator.getInstance().setRecommendedMaxTPM(1);
                ControllerMediator.getInstance().setGradientMaxMinToRecommended();
            }
        }
    }
}
