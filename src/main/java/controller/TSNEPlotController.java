package controller;

import com.jujutsu.tsne.*;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.utils.MatrixOps;
import com.jujutsu.utils.TSneUtils;
import exceptions.RNAScoopException;
import exceptions.TSNEInvalidPerplexityException;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import labelset.Cluster;
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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.extension.DatasetSelectionExtension;
import org.jfree.data.extension.impl.DatasetExtensionManager;
import org.jfree.data.extension.impl.XYCursor;
import org.jfree.data.extension.impl.XYDatasetSelectionExtension;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ui.LegendMaker;
import util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;
import static jdk.nashorn.internal.objects.Global.Infinity;

public class TSNEPlotController implements Initializable, InteractiveElementController{
    private static final double LEGEND_DOT_SIZE = 18;
    private static final double GRAPHIC_SPACING = 2;
    private static final double LEGEND_DOT_CANVAS_WIDTH = LEGEND_DOT_SIZE + GRAPHIC_SPACING;
    private static final double LEGEND_DOT_CANVAS_HEIGHT = LEGEND_DOT_SIZE + GRAPHIC_SPACING;
    private static final double LEGEND_ELEMENT_SPACING  = 5;
    private static final boolean INCLUDE_LEGEND_LABELS = true;
    private static final boolean LEGEND_SHOW_ONLY_SELECTED = false;
    private static final boolean LEGEND_SHOW_BACKGROUND = true;
    private static final boolean LEGEND_IS_VERTICAL = true;

    @FXML private VBox tSNEPlotPanel;
    @FXML private Button changeClusterLabelsButton;
    @FXML private Button drawTSNEButton;
    @FXML private TextField perplexity;
    @FXML private SwingNode swingNode;
    @FXML private StackPane tSNEPlotHolder;

    private HashMap<String, Integer> isoformIndexMap;
    private double[][] cellIsoformExpressionMatrix;
    private ChartPanel tSNEPlot;
    private ScrollPane tSNEPlotLegend;
    private CellSelectionManager cellSelectionManager;
    private HashMap<Integer, CellDataItem> cellNumberCellMap;
    private XYSeriesCollection cellsInTSNEPlot;

    /**
     * Makes t-SNE plot repaint every time its holder resizes (otherwise doesn't on Windows),
     * initializes cellsInTSNEPlot
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tSNEPlotHolder.heightProperty().addListener((ov, oldValue, newValue) -> {
            if (tSNEPlot != null) {
                // wrapping revalidate and repaint with runlater() helps
                runLater(() -> {
                    tSNEPlot.revalidate();
                    tSNEPlot.repaint();
                });
            }
        });
        cellsInTSNEPlot = new XYSeriesCollection();
        cellNumberCellMap = new HashMap<>();
    }

    public Node getTSNEPlot() {
        return tSNEPlotPanel;
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        changeClusterLabelsButton.setDisable(true);
        drawTSNEButton.setDisable(true);
        perplexity.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        changeClusterLabelsButton.setDisable(false);
        drawTSNEButton.setDisable(false);
        perplexity.setDisable(false);
    }

    public void setCellIsoformExpressionMatrix(double[][] cellIsoformExpressionMatrix) {
        if (!isTSNEPlotCleared())
            clearTSNEPlot();
        this.cellIsoformExpressionMatrix = cellIsoformExpressionMatrix;
    }

    public void setIsoformIndexMap(HashMap<String, Integer> isoformIndexMap) {
        if (!isTSNEPlotCleared())
            clearTSNEPlot();
        this.isoformIndexMap = isoformIndexMap;
    }

    public void clearTSNEPlot() {
        if (!isTSNEPlotCleared()) {
            JPanel whiteBackground = new JPanel();
            whiteBackground.setBackground(Color.WHITE);
            swingNode.setContent(whiteBackground);
            tSNEPlotHolder.getChildren().remove(tSNEPlotLegend);
            tSNEPlot = null;
            tSNEPlotLegend = null;
            cellSelectionManager = null;
            cellsInTSNEPlot.removeAllSeries();
            ControllerMediator.getInstance().clearLabelSetClusterCells();
        }
    }

    /*
    The below three functions should be called when changes happen to the label set in use
    They all redraw the t-SNE plot and notify the cell selection manager about the changes
    */

    public void handleClusterAddedFromSelectedCells() {
        if (!isTSNEPlotCleared()) {
            cellSelectionManager.handleClusterAddedFromSelectedCells();
            redrawTSNEPlot();
        }
    }

    public void handleRemovedCluster(Cluster removedCluster, Cluster clusterMergedInto) {
        if (!isTSNEPlotCleared()) {
            cellSelectionManager.handleRemovedCluster(removedCluster, clusterMergedInto);
            redrawTSNEPlot();
        }
    }

    public void handleChangedLabelSetInUse(){
        if (!isTSNEPlotCleared()) {
            cellSelectionManager.handleChangedLabelSet();
            redrawTSNEPlot();
        }
    }

    public void redrawTSNEPlot() {
        redrawTSNEPlotSansLegend();
        redrawTSNEPlotLegend();
    }

    public void redrawTSNEPlotLegend() {
        if (!isTSNEPlotCleared())
            tSNEPlotLegend.setContent(LegendMaker.createLegend(INCLUDE_LEGEND_LABELS, LEGEND_SHOW_ONLY_SELECTED, LEGEND_SHOW_BACKGROUND,
                    LEGEND_IS_VERTICAL, LEGEND_DOT_SIZE, LEGEND_DOT_CANVAS_WIDTH, LEGEND_DOT_CANVAS_HEIGHT, LEGEND_ELEMENT_SPACING));
    }

    public void selectCellsIsoformsExpressedIn(Collection<String> isoformIDs) {
        cellSelectionManager.selectCellsIsoformsExpressedIn(isoformIDs);
    }


    public boolean isTSNEPlotCleared() {
        return tSNEPlot == null;
    }

    public Map<Integer, CellDataItem> getCellNumberCellMap() {
        return cellNumberCellMap;
    }

    public boolean areCellsSelected() {
        if (!isTSNEPlotCleared())
            return cellSelectionManager.getSelectedCells().size() > 0;
        return false;
    }

    public int getNumCellsToPlot() {
        if (cellIsoformExpressionMatrix != null)
            return cellIsoformExpressionMatrix.length;
        return 0;
    }

    public int getNumExpressingCells(String isoformID, Cluster cluster, boolean onlySelected) {
        int numExpressingCells = 0;
        Collection<TSNEPlotController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();

        for (TSNEPlotController.CellDataItem selectedCell : cellsInCluster) {
            if (selectedCell.getIsoformExpressionLevel(isoformID) > 0)
                numExpressingCells++;
        }
        return numExpressingCells;
    }

    public Collection<CellDataItem> getCells(boolean onlySelected) {
        if (isTSNEPlotCleared())
            return new HashSet<>();
        else if (onlySelected)
            return cellSelectionManager.getSelectedCells().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        else
            return cellNumberCellMap.values();
    }

    public List<Cluster> getClusters(boolean onlySelected) {
        if (isTSNEPlotCleared())
            return new ArrayList<>();
        else if (onlySelected)
            return cellSelectionManager.getSelectedClusters();
        else
            return ControllerMediator.getInstance().getLabelSetInUse().getClusters();
    }

    public Collection<CellDataItem> getSelectedCellsInCluster(Cluster cluster) {
        if (isTSNEPlotCleared())
            return new ArrayList<>();
        return cellSelectionManager.getSelectedCellsInCluster(cluster);
    }

    /**
     * When "Draw t-SNE" button is pressed, draws t-SNE plot and deselects any
     * selected isoforms
     */
    @FXML
    protected void handleDrawTSNEButton() {
        clearTSNEPlot();
        ControllerMediator.getInstance().deselectAllIsoforms();
        ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
        disableAssociatedFunctionality();
        try {
            Thread tSNEPlotMaker = new Thread(new TSNEPlotMaker());
            tSNEPlotMaker.start();
        } catch (Exception e) {
            enableAssociatedFunctionality();
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("drawing the t-SNE plot");
        }
    }

    /**
     * Opens up cluster controller window when handle change cluster labels button is pressed
     */
    @FXML
    protected void handleChangeClusterLabelsButton() {
        ControllerMediator.getInstance().displayClusterManager();
    }


    private void disableAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableGeneSelector();
        ControllerMediator.getInstance().disableTPMGradientAdjuster();
        ControllerMediator.getInstance().disableLabelSetManager();
        // doesn't disable add label set view because t-SNE plot should be
        // disabled when that view is active
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableTPMGradientAdjuster();
        ControllerMediator.getInstance().enableLabelSetManager();
    }

    private void redrawTSNEPlotSansLegend() {
        if (!isTSNEPlotCleared())
            tSNEPlot.getChart().setTitle(tSNEPlot.getChart().getTitle());

    }

    /**
     * Represents a cell in the t-SNE plot
     */
    public class CellDataItem extends XYDataItem {
        /**
         * Each number is the level of expression of some isoform in this cell.
         */
        private double[] isoformExpressionLevels;
        /**
         * The row in the cell isoform expression matrix that this cell represents
         * (first row is represented by cell 0). This is also the index of this cell in the
         * series that is used to plot the t-SNE plot
         */
        private int cellNumber;

        private CellDataItem(Number x, Number y, double[] isoformExpressionLevels, int cellNumber) {
            super(x, y);
            this.isoformExpressionLevels = isoformExpressionLevels;
            this.cellNumber = cellNumber;
        }

        /**
         * Returns the level of expression of the isoform with the given ID
         * in this cell. Returns 0 if that information isn't stored
         */
        public double getIsoformExpressionLevel(String isoformID) {
            Integer isoformIndex = isoformIndexMap.get(isoformID);

            if (isoformIndex != null) {
                return isoformExpressionLevels[isoformIndex];
            }
            else
                return 0;
        }

        public int getCellNumber() {
            return cellNumber;
        }
    }

    /**
     * Is the t-SNE plot lasso selection tool
     */
    private static class TSNEPlotFreeRegionSelectionHandler extends FreeRegionSelectionHandler {
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            runLater(() -> {
                ControllerMediator.getInstance().deselectAllIsoforms();
                ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
            });
        }
    }

    private class TSNEPlotRenderer extends XYLineAndShapeRenderer {

        public TSNEPlotRenderer() {
            super(false, true);
            //setDrawOutlines(true);
            setUseOutlinePaint(true);
        }

        @Override
        public Paint getItemPaint(int series, int cellNumber) {
            CellDataItem cell = cellNumberCellMap.get(cellNumber);
            if (cellSelectionManager.isCellSelected(cell))
                return Color.WHITE;
            else
                return ControllerMediator.getInstance().getLabelSetInUse().getCellCluster(cellNumber).getColor();
        }

        @Override
        public Paint getItemOutlinePaint(int series, int cellNumber) {
            return ControllerMediator.getInstance().getLabelSetInUse().getCellCluster(cellNumber).getColor();
        }

        @Override
        public Shape getItemShape(int row, int column) {
            return new Ellipse2D.Double(0,0,5,5);
        }
    }

    /**
     * Manages selection/deselection of cells in the t-SNE plot
     */
    private class CellSelectionManager implements SelectionManager {
        private DatasetExtensionManager extensionManager;
        private HashMap<Cluster, Collection<CellDataItem>> selectedCells;
        private boolean redrawOnClear;

        public CellSelectionManager(DatasetExtensionManager extensionManager) {
            selectedCells = new HashMap<>();
            this.extensionManager = extensionManager;
            redrawOnClear = true;
        }

        public boolean isCellSelected(CellDataItem cellDataItem) {
            for (Collection<CellDataItem> selectedCellsInCluster : selectedCells.values()) {
                if (selectedCellsInCluster.contains(cellDataItem))
                    return true;
            }
            return false;
        }

        public List<Cluster> getSelectedClusters() {
            return Util.asSortedList(selectedCells.keySet());
        }

        public Collection<CellDataItem> getSelectedCellsInCluster(Cluster cluster) {
            if (selectedCells.containsKey(cluster))
                return selectedCells.get(cluster);
            return new ArrayList<>();
        }

        /*
        The below three functions fix selected cells map so that it correctly maps selected
        clusters to the cells in them that are selected, after there are changes to the label set
        in use
         */

        public void handleClusterAddedFromSelectedCells() {
            if (selectedCells.size() > 0) {
                Collection<CellDataItem> selectedCellsSubset = selectedCells.values().iterator().next();
                CellDataItem selectedCell = selectedCellsSubset.iterator().next();
                Cluster addCluster = ControllerMediator.getInstance().getLabelSetInUse().getCellCluster(selectedCell.getCellNumber());
                selectedCells.clear();
                selectedCells.put(addCluster, new ArrayList<>(addCluster.getCells()));
            }
        }

        public void handleRemovedCluster(Cluster removedCluster, Cluster clusterMergedWith) {
            if (selectedCells.containsKey(removedCluster)) {
                Collection<CellDataItem> cellsToMove = selectedCells.get(removedCluster);
                Collection<CellDataItem> cellsInClusterMergedWith;
                if (selectedCells.containsKey(clusterMergedWith)) {
                    cellsInClusterMergedWith = selectedCells.get(clusterMergedWith);
                } else {
                    cellsInClusterMergedWith = new ArrayList<>();
                    selectedCells.put(clusterMergedWith, cellsInClusterMergedWith);
                }
                cellsInClusterMergedWith.addAll(cellsToMove);
                selectedCells.remove(removedCluster);
            }
        }

        public void handleChangedLabelSet() {
            Collection<CellDataItem> cellsToMove = selectedCells.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
            selectedCells.clear();
            for (CellDataItem cell : cellsToMove)
                select(cell);
        }

        /**
         * Clears selected cells in t-SNE plot, then selects cells in which isoforms with
         * the given IDs are expressed
         */
        public void selectCellsIsoformsExpressedIn(Collection<String> isoformIDs) {
            List<XYSeries> cellGroups = cellsInTSNEPlot.getSeries();
            redrawOnClear = false;
            clearSelection();
            for (XYSeries cellGroup : cellGroups) {
                for (XYDataItem dataItem : cellGroup.getItems()) {
                    CellDataItem cell = (CellDataItem) dataItem;
                    if (shouldSelectCell(cell, isoformIDs))
                        select(cell);

                }
            }
            redrawTSNEPlotSansLegend();
            runLater(() -> ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot());
            redrawOnClear = true;
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
            boolean shouldRedraw = false;
            EntityCollection entities = tSNEPlot.getChartRenderingInfo().getEntityCollection();
            for (ChartEntity chartEntity : entities.getEntities()) {
                if (chartEntity instanceof DataItemEntity) {
                    XYItemEntity xyItemEntity = (XYItemEntity) chartEntity;
                    if (xyItemEntity.getArea().contains(new Point2D.Double(x, y))) {
                        select(xyItemEntity);
                        if (!shouldRedraw)
                            shouldRedraw = true;
                    }
                }
            }
            if (shouldRedraw) {
                runLater(() -> ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot());
                redrawTSNEPlotSansLegend();
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
            boolean shouldRedraw = false;
            EntityCollection entities = tSNEPlot.getChartRenderingInfo().getEntityCollection();
            for (ChartEntity chartEntity : entities.getEntities()) {
                if (chartEntity instanceof XYItemEntity) {
                    XYItemEntity xyItemEntity = (XYItemEntity) chartEntity;
                    Area selectionShape = new Area(selection);
                    Area entityShape = new Area(xyItemEntity.getArea());
                    if (selectionShape.contains(entityShape.getBounds())) {
                        select(xyItemEntity);
                        if (!shouldRedraw)
                            shouldRedraw = true;
                    } else {
                        entityShape.subtract(selectionShape);
                        if (entityShape.isEmpty()) {
                            select(xyItemEntity);
                            if (!shouldRedraw)
                                shouldRedraw = true;
                        }
                    }
                }
            }
            if (shouldRedraw) {
                runLater(() -> ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot());
                redrawTSNEPlotSansLegend();
            }
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
            selectedCells.clear();
            if (redrawOnClear)
                redrawTSNEPlotSansLegend();

        }

        public HashMap<Cluster, Collection<CellDataItem>> getSelectedCells() {
            return selectedCells;
        }

        /**
         * Selects given cell
         */
        private void select(CellDataItem cell) {
            Cluster cluster = ControllerMediator.getInstance().getLabelSetInUse().getCellCluster(cell.getCellNumber());
            Collection<CellDataItem> selectedCellsOfSameCluster;
            if (selectedCells.containsKey(cluster)) {
                selectedCellsOfSameCluster =  selectedCells.get(cluster);
            } else {
                selectedCellsOfSameCluster = new ArrayList<>();
                selectedCells.put(cluster, selectedCellsOfSameCluster);
            }
            selectedCellsOfSameCluster.add(cell);
        }

        /**
         * Selects cell in t-SNE plot given the object that represents its graphic
         * in the t-SNE plot
         */
        private void select(XYItemEntity xyItemEntity) {
            int cellNumber = xyItemEntity.getItem();
            select(cellNumberCellMap.get(cellNumber));
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
                ControllerMediator.getInstance().addCellsToLabelSetClusters();
                ControllerMediator.getInstance().updateGenesMaxFoldChange();
                runLater(() -> {
                    ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
                    ControllerMediator.getInstance().addConsoleMessage("Finished drawing t-SNE plot");
                });
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
            TSneConfiguration config = TSneUtils.buildConfig(cellIsoformExpressionMatrix, 2, initial_dims, perplexityValue, 1000, false, 0.5D, false);
            return tSNE.tsne(config);
        }


        /**
         * Plots given t-SNE matrix
         */
        private void drawTsne(double[][] tSNEMatrix) {
            createDataSet(tSNEMatrix);
            DatasetSelectionExtension<XYCursor> datasetExtension
                    = new XYDatasetSelectionExtension(cellsInNewTSNEPlot);

            JFreeChart chart = createPlot(datasetExtension);
            ChartPanel panel = new ChartPanel(chart);
            panel.setMouseWheelEnabled(true);

            addSelectionHandler(panel);
            addSelectionManager(datasetExtension, panel);
            tSNEPlot = panel;
            tSNEPlot.setPreferredSize(new Dimension(500, Integer.MAX_VALUE));
            chart.removeLegend();
            addLegend();

            cellsInTSNEPlot = cellsInNewTSNEPlot;
            swingNode.setContent(tSNEPlot);
        }

        private void setTPMGradientValues() {
            double[] expressionArray = Arrays.stream(cellIsoformExpressionMatrix).flatMapToDouble(Arrays::stream).toArray();
            Arrays.sort(expressionArray);
            double[] filteredExpressionArray = Arrays.stream(expressionArray).filter(tpm -> tpm >= 1).toArray();

            addMinMaxTPMToTPMGradientLabels(expressionArray);
            setTPMGradientMaxMinToRecommended(filteredExpressionArray);
        }

        /**
         * Given a t-SNE matrix, creates a collection of series of x, y coordinates that
         * can be plotted.
         */
        private void createDataSet(double[][] tSNEMatrix) {
            XYSeries cells = new XYSeries("Cells", false);
            cellsInNewTSNEPlot.addSeries(cells);
            for (int cellNumber = 0; cellNumber < tSNEMatrix.length; cellNumber++) {
                double cellX = tSNEMatrix[cellNumber][0];
                double cellY = tSNEMatrix[cellNumber][1];
                CellDataItem cellDataItem = new CellDataItem(cellX, cellY, cellIsoformExpressionMatrix[cellNumber], cellNumber);
                cells.add(cellDataItem);
                cellNumberCellMap.put(cellDataItem.getCellNumber(), cellDataItem);
            }
        }

        private JFreeChart createPlot(DatasetSelectionExtension<XYCursor> ext) {
            JFreeChart chart = ChartFactory.createScatterPlot("", " ", " ", cellsInNewTSNEPlot);

            XYPlot plot = (XYPlot) chart.getPlot();
            setPlotViewProperties(plot);
            plot.setRenderer(new TSNEPlotRenderer());

            //register plot as selection change listener
            ext.addChangeListener(plot);

            return chart;
        }

        private void addLegend() {
            runLater(() -> {
                tSNEPlotLegend = new ScrollPane();
                tSNEPlotLegend.setContent(LegendMaker.createLegend(INCLUDE_LEGEND_LABELS, LEGEND_SHOW_ONLY_SELECTED,
                        LEGEND_SHOW_BACKGROUND, LEGEND_IS_VERTICAL, LEGEND_DOT_SIZE, LEGEND_DOT_CANVAS_WIDTH,
                        LEGEND_DOT_CANVAS_HEIGHT, LEGEND_ELEMENT_SPACING));
                StackPane.setAlignment(tSNEPlotLegend, Pos.TOP_RIGHT);
                tSNEPlotLegend.setPickOnBounds(false);
                tSNEPlotLegend.setMaxWidth(-Infinity);
                tSNEPlotLegend.setMaxHeight(-Infinity);
                tSNEPlotHolder.getChildren().add(tSNEPlotLegend);
                tSNEPlotLegend.setStyle("-fx-background-color: transparent;");
            });
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
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinesVisible(false);
            plot.getDomainAxis().setTickMarksVisible(false);
            plot.getDomainAxis().setTickLabelsVisible(false);
            plot.getRangeAxis().setTickMarksVisible(false);
            plot.getRangeAxis().setTickLabelsVisible(false);
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
