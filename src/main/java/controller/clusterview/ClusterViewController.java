package controller.clusterview;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.utils.TSneUtils;
import controller.InteractiveElementController;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
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
import tagbio.umap.Umap;
import ui.Legend;
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

public class ClusterViewController implements Initializable, InteractiveElementController {
    private static final double LEGEND_DOT_SIZE = 18;
    private static final double GRAPHIC_SPACING = 2;
    private static final double LEGEND_DOT_CANVAS_WIDTH = LEGEND_DOT_SIZE + GRAPHIC_SPACING;
    private static final double LEGEND_DOT_CANVAS_HEIGHT = LEGEND_DOT_SIZE + GRAPHIC_SPACING;
    private static final double LEGEND_ELEMENT_SPACING  = 5;
    private static final boolean INCLUDE_LEGEND_LABELS = true;
    private static final boolean LEGEND_SELECTABLE = true;
    private static final boolean LEGEND_SHOW_ONLY_SELECTED = false;
    private static final boolean LEGEND_SHOW_BACKGROUND = true;
    private static final boolean LEGEND_IS_VERTICAL = true;

    @FXML private VBox clusterView;
    @FXML private Button drawPlotButton;
    @FXML private Button changeClusterLabelsButton;
    @FXML private Button clusterViewSettingsButton;
    @FXML private SwingNode swingNode;
    @FXML private StackPane plotHolder;

    private HashMap<String, Integer> isoformIndexMap;
    private double[][] cellIsoformExpressionMatrix;
    private double[][] embedding; // optional embedding user can load
    private ChartPanel plot;
    private PlotRenderer plotRenderer;
    private ScrollPane legendHolder;
    private Legend legend;
    private CellSelectionManager cellSelectionManager;
    private HashMap<Integer, CellDataItem> cellNumberCellMap;
    private XYSeriesCollection cellsInPlot;

    /**
     * Makes plot repaint every time its holder resizes (otherwise doesn't on Windows),
     * initializes cellsInPlot
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        plotHolder.heightProperty().addListener((ov, oldValue, newValue) -> {
            if (plot != null) {
                // wrapping revalidate and repaint with runlater() helps
                runLater(() -> {
                    plot.revalidate();
                    plot.repaint();
                });
            }
        });
        cellsInPlot = new XYSeriesCollection();
        cellNumberCellMap = new HashMap<>();
    }

    public Node getClusterView() {
        return clusterView;
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        drawPlotButton.setDisable(true);
        changeClusterLabelsButton.setDisable(true);
        clusterViewSettingsButton.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        drawPlotButton.setDisable(false);
        changeClusterLabelsButton.setDisable(false);
        clusterViewSettingsButton.setDisable(false);
    }

    public void setCellIsoformExpressionMatrix(double[][] cellIsoformExpressionMatrix) {
        if (!isPlotCleared())
            clearPlot();
        this.cellIsoformExpressionMatrix = cellIsoformExpressionMatrix;
    }

    public void setIsoformIndexMap(HashMap<String, Integer> isoformIndexMap) {
        if (!isPlotCleared())
            clearPlot();
        this.isoformIndexMap = isoformIndexMap;
    }

    public void setEmbedding(double[][] embedding) {
        if (!isPlotCleared())
            clearPlot();
        this.embedding = embedding;
    }

    public void clearPlot() {
        if (!isPlotCleared()) {
            JPanel whiteBackground = new JPanel();
            whiteBackground.setBackground(Color.WHITE);
            swingNode.setContent(whiteBackground);
            plotHolder.getChildren().remove(legendHolder);
            plot = null;
            plotRenderer = null;
            legendHolder = null;
            legend = null;
            cellSelectionManager = null;
            cellsInPlot.removeAllSeries();
            ControllerMediator.getInstance().clearLabelSetClusterCells();
        }
    }

    /*
    The below three functions should be called when changes happen to the label set in use
    They all redraw the plot and notify the cell selection manager about the changes
    */

    public void handleClusterAddedFromSelectedCells() {
        if (!isPlotCleared()) {
            cellSelectionManager.handleClusterAddedFromSelectedCells();
            redrawPlot();
        }
    }

    public void handleRemovedCluster(Cluster removedCluster, Cluster clusterMergedInto) {
        if (!isPlotCleared()) {
            cellSelectionManager.handleRemovedCluster(removedCluster, clusterMergedInto);
            redrawPlot();
        }
    }

    public void handleChangedLabelSetInUse(){
        if (!isPlotCleared()) {
            cellSelectionManager.handleChangedLabelSet();
            redrawPlot();
        }
    }

    public void redrawPlot() {
        redrawPlotSansLegend();
        redrawLegend();
    }

    public void drawPlot() {
        clearPlot();
        ControllerMediator.getInstance().deselectAllIsoforms();
        ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
        disableAssociatedFunctionality();
        try {
            Thread plotMaker = new Thread(new PlotMaker());
            plotMaker.start();
        } catch (Exception e) {
            enableAssociatedFunctionality();
            ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("drawing the cell plot");
            e.printStackTrace();
        }
    }

    public void redrawLegend() {
        if (!isPlotCleared()) {
            legend = new Legend(INCLUDE_LEGEND_LABELS, LEGEND_SELECTABLE, LEGEND_SHOW_ONLY_SELECTED, LEGEND_SHOW_BACKGROUND,
                    LEGEND_IS_VERTICAL, LEGEND_DOT_SIZE, LEGEND_DOT_CANVAS_WIDTH, LEGEND_DOT_CANVAS_HEIGHT, LEGEND_ELEMENT_SPACING);
            legendHolder.setContent(legend.getLegendGraphic());
        }
    }

    public void selectCellsIsoformsExpressedIn(Collection<String> isoformIDs) {
        cellSelectionManager.selectCellsIsoformsExpressedIn(isoformIDs);
    }

    public void selectCluster(Cluster cluster, boolean unselectRest) {
        cellSelectionManager.selectCluster(cluster, unselectRest);
    }

    public void unselectCluster(Cluster cluster) {
        cellSelectionManager.unselectCluster(cluster);
    }

    public void clearSelectedCellsAndRedrawPlot() {
        cellSelectionManager.clearSelection();
        ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
    }

    public void redrawPlotSansLegend() {
        if (!isPlotCleared())
            plotRenderer.updateOutlineAndRedraw();
    }

    public boolean isPlotCleared() {
        return plot == null;
    }

    public Map<Integer, CellDataItem> getCellNumberCellMap() {
        return cellNumberCellMap;
    }

    public boolean areCellsSelected() {
        if (!isPlotCleared())
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
        Collection<ClusterViewController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();

        for (ClusterViewController.CellDataItem selectedCell : cellsInCluster) {
            if (selectedCell.getIsoformExpressionLevel(isoformID) > 0)
                numExpressingCells++;
        }
        return numExpressingCells;
    }

    public Collection<CellDataItem> getCells(boolean onlySelected) {
        if (isPlotCleared())
            return new HashSet<>();
        else if (onlySelected)
            return cellSelectionManager.getSelectedCells().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        else
            return cellNumberCellMap.values();
    }

    public List<Cluster> getClusters(boolean onlySelected) {
        if (isPlotCleared())
            return new ArrayList<>();
        else if (onlySelected)
            return cellSelectionManager.getSelectedClusters();
        else
            return ControllerMediator.getInstance().getLabelSetInUse().getClusters();
    }

    public Collection<CellDataItem> getSelectedCellsInCluster(Cluster cluster) {
        if (isPlotCleared())
            return new ArrayList<>();
        return cellSelectionManager.getSelectedCellsInCluster(cluster);
    }

    public void handleColoringChange() {
        if (!isPlotCleared() && plotRenderer.isColoringByIsoform())
            redrawPlotSansLegend();
    }

    /**
     * When "Draw cell plot" button is pressed, draws plot and deselects any
     * selected isoforms
     */
    @FXML
    protected void handleDrawPlotButton() {
        drawPlot();
    }

    /**
     * Opens up label set manager window when handle change cluster labels button is pressed
     */
    @FXML
    protected void handleChangeClusterLabelsButton() {
        ControllerMediator.getInstance().displayLabelSetManager();
    }

    /**
     * Opens up cluster controller window when handle change cluster labels button is pressed
     */
    @FXML
    protected void handleClusterViewSettingsButton() {
        ControllerMediator.getInstance().displayClusterViewSettings();
    }


    private void disableAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableGeneSelector();
        ControllerMediator.getInstance().disableTPMGradientAdjuster();
        ControllerMediator.getInstance().disableLabelSetManager();
        // doesn't disable add label set view because plot should be
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

    /**
     * Represents a cell in the plot
     */
    public class CellDataItem extends XYDataItem {
        /**
         * Each number is the level of expression of some isoform in this cell.
         */
        private double[] isoformExpressionLevels;
        /**
         * The row in the cell isoform expression matrix that this cell represents
         * (first row is represented by cell 0). This is also the index of this cell in the
         * series that is used to plot the plot
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
     * Is the plot lasso selection tool
     */
    private static class PlotFreeRegionSelectionHandler extends FreeRegionSelectionHandler {
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            runLater(() -> {
                ControllerMediator.getInstance().deselectAllIsoforms();
                ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
            });
        }
    }

    private class PlotRenderer extends XYLineAndShapeRenderer {
        private final BasicStroke DEFAULT_BASIC_STROKE = new BasicStroke(2f);
        private final BasicStroke COLORING_BY_ISOFORM_BASIC_STROKE = new BasicStroke(0.5f);
        private final Shape CELL_SHAPE = new Ellipse2D.Double(0, 0, 6.5, 6.5);

        public PlotRenderer() {
            super(false, true);
            setUseOutlinePaint(true);
            setSeriesOutlineStroke(0, DEFAULT_BASIC_STROKE);
        }

        @Override
        public Paint getItemPaint(int series, int cellNumber) {
            CellDataItem cell = cellNumberCellMap.get(cellNumber);

            if (isColoringByIsoform()) {
                Collection<String> selectedIsoformIDs = ControllerMediator.getInstance().getSelectedIsoformIDs();
                String id = selectedIsoformIDs.iterator().next();
                javafx.scene.paint.Color javaFXColor = ControllerMediator.getInstance().getColorFromTPMGradient(cell.getIsoformExpressionLevel(id));
                return new Color((int) Math.round(javaFXColor.getRed() * 255),
                                 (int) Math.round(javaFXColor.getGreen() * 255),
                                 (int) Math.round(javaFXColor.getBlue() * 255));
            } else if (cellSelectionManager.isCellSelected(cell)) {
                return Color.WHITE;
            } else {
                return ControllerMediator.getInstance().getLabelSetInUse().getCellCluster(cellNumber).getColor();
            }
        }

        @Override
        public Paint getItemOutlinePaint(int series, int cellNumber) {
            if (isColoringByIsoform())
                return Color.black;
            else
                return ControllerMediator.getInstance().getLabelSetInUse().getCellCluster(cellNumber).getColor();
        }

        @Override
        public Shape getItemShape(int row, int column) {
            return CELL_SHAPE;
        }

        public void updateOutlineAndRedraw() {
            if (isColoringByIsoform())
                setSeriesOutlineStroke(0, COLORING_BY_ISOFORM_BASIC_STROKE); // triggers redraw
            else
                setSeriesOutlineStroke(0, DEFAULT_BASIC_STROKE); // triggers redraw
        }

        public boolean isColoringByIsoform() {
            Collection<String> selectedIsoformIDs = ControllerMediator.getInstance().getSelectedIsoformIDs();
            boolean coloringBySelectedIsoform = ControllerMediator.getInstance().isColoringCellPlotBySelectedIsoform();

            return coloringBySelectedIsoform && selectedIsoformIDs.size() > 0;
        }
    }

    /**
     * Manages selection/deselection of cells in the plot
     */
    private class CellSelectionManager implements SelectionManager {
        private HashMap<Cluster, Collection<CellDataItem>> selectedCells;
        private boolean redrawOnClear;

        public CellSelectionManager() {
            selectedCells = new HashMap<>();
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
         * Clears selected cells in plot, then selects cells in which isoforms with
         * the given IDs are expressed
         */
        public void selectCellsIsoformsExpressedIn(Collection<String> isoformIDs) {
            List<XYSeries> cellGroups = cellsInPlot.getSeries();
            redrawOnClear = false;
            clearSelection();
            for (XYSeries cellGroup : cellGroups) {
                for (XYDataItem dataItem : cellGroup.getItems()) {
                    CellDataItem cell = (CellDataItem) dataItem;
                    if (shouldSelectCell(cell, isoformIDs))
                        select(cell);

                }
            }
            redrawPlotSansLegend();
            runLater(() -> ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot());
            redrawOnClear = true;
        }

        public void selectCluster(Cluster cluster, boolean unselectRest) {
            if (unselectRest)
                selectedCells.clear();
            for (CellDataItem cell : cluster.getCells())
                select(cell);
            redrawPlotSansLegend();
            runLater(() -> ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot());
        }

        public void unselectCluster(Cluster cluster) {
            if (selectedCells.containsKey(cluster)) {
                selectedCells.remove(cluster);
                redrawPlotSansLegend();
            }
        }


        /**
         * Selects cell at given (x, y) coordinates (if cell exists there)
         */
        @Override
        public void select(double x, double y) {
            double scaleX = plot.getScaleX();
            double scaleY = plot.getScaleY();
            if (scaleX != 1.0D || scaleY != 1.0D) {
                x /= scaleX;
                y /= scaleY;
            }
            boolean shouldRedraw = false;
            EntityCollection entities = plot.getChartRenderingInfo().getEntityCollection();
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
                redrawPlotSansLegend();
            }
        }

        /**
         * Selects any cells contained in given lasso selection
         */
        @Override
        public void select(GeneralPath pathSelection) {
            double scaleX = plot.getScaleX();
            double scaleY = plot.getScaleY();
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
            EntityCollection entities = plot.getChartRenderingInfo().getEntityCollection();
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
                redrawPlotSansLegend();
            }
        }

        /**
         * Unnecessary therefore not implemented
         */
        @Override
        public void select(Rectangle2D rectangle2D) {}

        /**
         * Clears all selected cells in cell plot and selected legend elements,
         * redraws the cell plot
         */
        @Override
        public void clearSelection() {
            selectedCells.clear();
            legend.clearSelectedLegendElements();
            if (redrawOnClear)
                redrawPlotSansLegend();

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
         * Selects cell in plot given the object that represents its graphic
         * in the plot
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

    private class PlotMaker implements Runnable {

        private XYSeriesCollection cellsInNewPlot;

        /**
         * Draws the plot and sets the TPM gradient values
         */
        @Override
        public void run() {
            runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Drawing cell plot..."));
            try {
                cellsInNewPlot = new XYSeriesCollection();
                double[][] matrix = (embedding == null ? generatePlotMatrix() : embedding);
                drawPlot(matrix);
                ControllerMediator.getInstance().addCellsToLabelSetClusters();
                setTPMGradientValues();
                runLater(() -> ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot());
                ControllerMediator.getInstance().updateGenesMaxFoldChange();
                runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Finished drawing cell plot"));
           } catch (Exception e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("drawing the cell plot"));
                e.printStackTrace();
            } finally {
                runLater(ClusterViewController.this::enableAssociatedFunctionality);
            }
        }

        private double[][] generatePlotMatrix() {
            if (ControllerMediator.getInstance().usingUMAPSettings())
                return generateUMAPMatrix();
            else
                return generateTSNEMatrix();
        }


        /**
         * Plots given matrix
         */
        private void drawPlot(double[][] matrix) {
            createDataSet(matrix);
            DatasetSelectionExtension<XYCursor> datasetExtension
                    = new XYDatasetSelectionExtension(cellsInNewPlot);

            JFreeChart chart = createPlot(datasetExtension);
            ChartPanel panel = new ChartPanel(chart);
            panel.setMouseWheelEnabled(true);

            addSelectionHandler(panel);
            addSelectionManager(datasetExtension, panel);
            plot = panel;
            plot.setPreferredSize(new Dimension(500, Integer.MAX_VALUE));
            chart.removeLegend();
            addLegend();

            cellsInPlot = cellsInNewPlot;
            swingNode.setContent(plot);
        }

        private void setTPMGradientValues() {
            double[] expressionArray = Arrays.stream(cellIsoformExpressionMatrix).flatMapToDouble(Arrays::stream).toArray();
            Arrays.sort(expressionArray);
            double[] filteredExpressionArray = Arrays.stream(expressionArray).filter(tpm -> tpm >= 1).toArray();

            addMinMaxTPMToTPMGradientLabels(expressionArray);
            setTPMGradientMaxMinToRecommended(filteredExpressionArray);
        }

        private double[][] generateTSNEMatrix() {
            int initial_dims = 55;
            double perplexity = ControllerMediator.getInstance().getPerplexity();
            int maxIterations = ControllerMediator.getInstance().getMaxIterations();

            BHTSne tSNE = new BHTSne();
            TSneConfiguration config = TSneUtils.buildConfig(cellIsoformExpressionMatrix, 2, initial_dims, perplexity,
                    maxIterations, false, 0.5D, false);
            return tSNE.tsne(config);
        }

        private double[][] generateUMAPMatrix() {
            float minDist = ControllerMediator.getInstance().getMinDist();
            int nearestNeighbors = ControllerMediator.getInstance().getNearestNeighbors();

            final Umap umap = new Umap();
            umap.setNumberComponents(2);         // number of dimensions in result
            umap.setMinDist(minDist);
            umap.setNumberNearestNeighbours(nearestNeighbors);
            return umap.fitTransform(cellIsoformExpressionMatrix);
        }

        /**
         * Given a matrix of 2D coordinates, creates a series of cells that can be plotted.
         */
        private void createDataSet(double[][] matrix) {
            XYSeries cells = new XYSeries("Cells", false);
            cellsInNewPlot.addSeries(cells);
            for (int cellNumber = 0; cellNumber < matrix.length; cellNumber++) {
                double cellX = matrix[cellNumber][0];
                double cellY = matrix[cellNumber][1];
                CellDataItem cellDataItem = new CellDataItem(cellX, cellY, cellIsoformExpressionMatrix[cellNumber], cellNumber);
                cells.add(cellDataItem);
                cellNumberCellMap.put(cellDataItem.getCellNumber(), cellDataItem);
            }
        }

        private JFreeChart createPlot(DatasetSelectionExtension<XYCursor> ext) {
            JFreeChart chart = ChartFactory.createScatterPlot("", " ", " ", cellsInNewPlot);

            XYPlot plot = (XYPlot) chart.getPlot();
            setPlotViewProperties(plot);
            plotRenderer = new PlotRenderer();
            plot.setRenderer(plotRenderer);

            //register plot as selection change listener
            ext.addChangeListener(plot);

            return chart;
        }

        private void addLegend() {
            runLater(() -> {
                legendHolder = new ScrollPane();
                legend = new Legend(INCLUDE_LEGEND_LABELS, LEGEND_SELECTABLE, LEGEND_SHOW_ONLY_SELECTED,
                        LEGEND_SHOW_BACKGROUND, LEGEND_IS_VERTICAL, LEGEND_DOT_SIZE, LEGEND_DOT_CANVAS_WIDTH,
                        LEGEND_DOT_CANVAS_HEIGHT, LEGEND_ELEMENT_SPACING);
                legendHolder.setContent(legend.getLegendGraphic());
                StackPane.setAlignment(legendHolder, Pos.TOP_RIGHT);
                legendHolder.setPickOnBounds(false);
                legendHolder.setMaxWidth(-Infinity);
                legendHolder.setMaxHeight(-Infinity);
                plotHolder.getChildren().add(legendHolder);
                legendHolder.setStyle("-fx-background-color: transparent;");
            });
        }

        private void addSelectionManager(DatasetSelectionExtension<XYCursor> datasetExtension, ChartPanel panel) {
            DatasetExtensionManager dExManager = new DatasetExtensionManager();
            dExManager.registerDatasetExtension(datasetExtension);
            cellSelectionManager = new CellSelectionManager();
            panel.setSelectionManager(cellSelectionManager);
        }

        private void addSelectionHandler(ChartPanel panel) {
            RegionSelectionHandler selectionHandler = new PlotFreeRegionSelectionHandler();
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
         *                        plot
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
         *                                plot
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
