package controller;

import annotation.Exon;
import annotation.Gene;
import annotation.Isoform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import labelset.Cluster;
import mediator.ControllerMediator;
import org.json.JSONObject;
import persistance.SessionMaker;
import ui.CategoryLabelsLegend;
import util.Util;
import java.net.URL;
import java.util.*;

import static util.Util.roundToOneDecimal;

public class IsoformPlotController implements Initializable, InteractiveElementController {
    private static final int SCROLLBAR_WIDTH = 18;

    @FXML private VBox isoformPlot;
    @FXML private VBox isoformPlotPanel;
    @FXML private ScrollPane scrollPane;
    @FXML private Pane isoformPlotPane;
    @FXML private VBox geneGroups;
    private static GeneGroup firstGeneGroup;
    private static IsoformPlotLegend isoformPlotLegend;

    private static SelectionModel selectionModel;
    private static RectangularSelection rectangularSelection;
    private static HashMap<Gene, GeneGroup> geneGeneGroupMap;
    private double scrollPaneWidthSpacing;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectionModel = new SelectionModel();
        geneGeneGroupMap = new HashMap<>();
        rectangularSelection = new RectangularSelection(isoformPlotPane);
        setScrollPaneWidthSpacing();
        setUpRedrawIsoformsToMatchScrollPaneWidth();
        setGeneGroupsStyling();
    }


    /**
     * Disables all functionality
     */
    public void disable() {
        isoformPlotPanel.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        isoformPlotPanel.setDisable(false);
    }

    /**
     * Adds given genes to isoform plot as long as aren't already added and should be shown
     * (doesn't add genes with only single-exon isoforms if hiding single-exon isoforms)
     */
    public void addGenes(Collection<Gene> genes) {
        boolean showGeneNameAndID = ControllerMediator.getInstance().isShowingGeneNameAndID();
        boolean showGeneName = ControllerMediator.getInstance().isShowingGeneName();
        boolean showIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        boolean showIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        boolean hideSingleExonIsoforms = ControllerMediator.getInstance().isHidingSingleExonIsoforms();
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean cellPlotCleared = ControllerMediator.getInstance().isCellPlotCleared();
        boolean cellsSelected = ControllerMediator.getInstance().areCellsSelected();
        for (Gene gene : genes) {
            if (!geneGeneGroupMap.containsKey(gene) && (!hideSingleExonIsoforms || gene.hasMultiExonIsoforms())) {
                GeneGroup geneGroup = new GeneGroup(gene, showGeneNameAndID, showGeneName, showIsoformID, showIsoformName,
                                                    hideSingleExonIsoforms, reverseComplement, cellPlotCleared, cellsSelected);
                geneGroups.getChildren().add(geneGroup);
                geneGeneGroupMap.put(gene, geneGroup);
                DotPlot.addDotPlotRowsIfShould(geneGroup);
                if (firstGeneGroup == null) {
                    updateFirstGeneGroup();
                    updateIsoformPlotLegend(false);
                }
            }
        }
        DotPlot.updateDotPlotLegend(false);
    }

    /**
     * Removes all given genes that are in the isoform plot
     */
    public void removeGenes(Collection<Gene> genes) {
        boolean selectedIsoformsChanged = false;

        for (Gene gene : genes) {
            if (geneGeneGroupMap.containsKey(gene)) {
                GeneGroup geneGroup = geneGeneGroupMap.get(gene);

                if (selectionModel.areIsoformGraphicsSelected() && !selectedIsoformsChanged)
                    selectedIsoformsChanged = geneGroup.hasSelectedIsoforms();

                // gene group must be made unselectable to remove references to its isoform
                // graphics
                geneGroup.makeUnselectable();
                geneGroups.getChildren().remove(geneGroup);
                geneGeneGroupMap.remove(gene, geneGroup);
                if (firstGeneGroup == geneGroup)
                    updateFirstGeneGroup();
            }
        }
        if (selectedIsoformsChanged)
            ControllerMediator.getInstance().clusterViewHandleChangedIsoformSelection();

        updateIsoformPlotLegend(false);
        DotPlot.updateDotPlotLegend(false);
    }

    public void updateIsoformGraphicsAndDotPlot() {
        redrawIsoforms();
        DotPlot.updateDotPlot();
    }

    /**
     * Updates isoform graphics, dot plot and isoform plot legend
     */
    public void updateIsoformPlot(boolean redrawIsoformPlotLegend) {
        updateIsoformGraphicsAndDotPlot();
        updateIsoformPlotLegend(redrawIsoformPlotLegend);
    }

    public void updateDotPlotLegend() {
        DotPlot.updateDotPlotLegend(true);
    }

    public void deselectAllIsoforms() {
        selectionModel.clearSelectedIsoformGraphics();
    }

    /**
     * Checks if are currently reverse complementing genes or not, and updates gene labels and
     * redraws isoform graphics accordingly
     */
    public void updateGeneReverseComplementStatus() {
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean showGeneNameAndID = ControllerMediator.getInstance().isShowingGeneNameAndID();
        boolean cellPlotCleared = ControllerMediator.getInstance().isCellPlotCleared();
        boolean cellsSelected = ControllerMediator.getInstance().areCellsSelected();
        boolean showGeneName = ControllerMediator.getInstance().isShowingGeneName();
        for (Gene gene : geneGeneGroupMap.keySet()) {
            GeneGroup geneGroup = geneGeneGroupMap.get(gene);
            geneGroup.updateLabel(showGeneNameAndID, showGeneName, reverseComplement);
            if (!gene.isOnPositiveStrand())
                geneGroup.redrawIsoforms(reverseComplement, cellPlotCleared, cellsSelected);
        }
    }

    /**
     * Checks if are currently hiding single-exon isoforms, and hides/shows
     * isoforms accordingly
     */
    public void updateHideSingleExonIsoformsStatus() {
        boolean hidingSingleExonIsoforms = ControllerMediator.getInstance().isHidingSingleExonIsoforms();
        if (hidingSingleExonIsoforms)
            hideSingeExonIsoforms();
        else
            showSingleExonIsoforms();
    }

    /**
     * Should be called when how expression is calculated (median, average...) changes
     * Updates isoform graphics and dots if necessary (i.e. if cell plot isn't cleared)
     */
    public void handleExpressionTypeChange() {
        if (!ControllerMediator.getInstance().isCellPlotCleared())
            updateIsoformGraphicsAndDotPlot();
    }

    /**
     * Called when gradient changes
     * Updates isoform plot if necessary (i.e. if cell plot isn't cleared)
     */
    public void handleGradientChange() {
        if (!ControllerMediator.getInstance().isCellPlotCleared())
            updateIsoformPlot(true);
    }

    /**
     * Should be called when hide dot plot status changes
     * Updates isoform plot if necessary (i.e. if cell plot isn't cleared)
     */
    public void handleDotPlotChange() {
        if (!ControllerMediator.getInstance().isCellPlotCleared())
            updateIsoformPlot(false);
    }

    /**
     * Updates gene labels according to current gene label settings
     */
    public void updateGeneLabels() {
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean showGeneNameAndID = ControllerMediator.getInstance().isShowingGeneNameAndID();
        boolean showGeneName = ControllerMediator.getInstance().isShowingGeneName();
        for (GeneGroup geneGroup : geneGeneGroupMap.values())
            geneGroup.updateLabel(showGeneNameAndID, showGeneName, reverseComplement);
    }

    /**
     * Updates isoform labels according to current isoform label settings
     */
    public void updateIsoformLabels() {
        boolean showIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        boolean showIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        for (GeneGroup geneGroup : geneGeneGroupMap.values())
            geneGroup.updateIsoformLabels(showIsoformName, showIsoformID);

    }

    /**
     * If legend isn't displayed and should be, adds it
     * If legend is displayed and should be, updates it
     * If legend is displayed and shouldn't be, removes it
     * @param redraw whether or not the gradient in the legend should be
     *               redrawn
     */
    public void updateIsoformPlotLegend(boolean redraw) {
        boolean shouldDisplayIsoformPlotLegend = shouldDisplayIsoformPlotLegend();
        if (shouldDisplayIsoformPlotLegend && isoformPlotLegend == null) {
            isoformPlotLegend = new IsoformPlotLegend();
            VBox.setMargin(isoformPlotLegend, new Insets(0, 10, 10, 10));
            isoformPlot.getChildren().add(isoformPlotLegend);
        } else if (shouldDisplayIsoformPlotLegend) {
            isoformPlotLegend.updateIsoformPlotLegend(redraw);
        } else if (isoformPlotLegend != null) {
            isoformPlot.getChildren().remove(isoformPlotLegend);
            isoformPlotLegend = null;
        }
    }

    /**
     * Only re-selects selected isoforms, assumes isoforms from previous session are already in
     * the view.
     */
    public void restoreIsoformPlotFromPrevSession(JSONObject prevSession) {
        Collection<String> isoformsToSelectIDs = (List<String>)(List<?>) prevSession.getJSONArray(SessionMaker.ISOFORMS_SELECTED_KEY).toList();
        selectionModel.selectIsoformsWithGivenIDs(isoformsToSelectIDs);
    }

    public void setExpressionUnit(String expressionUnit) {
        IsoformPlotLegend.setExpressionUnit(expressionUnit);
    }

    public String getExpressionUnit() {
        return IsoformPlotLegend.getExpressionUnit();
    }

    public static Collection<GeneGroup> getGeneGroups() {
        return geneGeneGroupMap.values();
    }

    public Collection<String> getSelectedIsoformIDs() {
        return selectionModel.getSelectedIsoformIDs();
    }

    public boolean areIsoformGraphicsSelected() {
        return selectionModel.areIsoformGraphicsSelected();
    }

    public Node getIsoformPlotPanel() {
        return isoformPlotPanel;
    }

    public Pane getIsoformPlot() {
        return isoformPlot;
    }

    /**
     * Opens up gene selector window when handle select genes button is pressed
     */
    @FXML
    protected void handleSelectGenesButton() {
        ControllerMediator.getInstance().displayGeneSelector();
    }

    /**
     * Opens up gradient adjuster window when set expression gradient button is pressed
     */
    @FXML
    protected void handleSetExpressionGradientButton() {
        ControllerMediator.getInstance().displayGradientAdjuster();
    }

    private void hideSingeExonIsoforms() {
        Collection<Gene> genesToRemove = new ArrayList<>();
        for (Gene gene : geneGeneGroupMap.keySet()) {
            if (!gene.hasMultiExonIsoforms()) {
                genesToRemove.add(gene);
            } else {
                GeneGroup geneGroup = geneGeneGroupMap.get(gene);
                geneGroup.removeSingleExonIsoforms();
            }
        }
        if (genesToRemove.size() > 0)
            removeGenes(genesToRemove);
        DotPlot.updateDotPlotLegend( false);
    }

    private void showSingleExonIsoforms() {
        Collection<Gene> genesToAdd = new ArrayList<>();
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean showIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        boolean showIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        boolean cellPlotCleared = ControllerMediator.getInstance().isCellPlotCleared();
        boolean cellsSelected = ControllerMediator.getInstance().areCellsSelected();
        for (Gene gene : ControllerMediator.getInstance().getShownGenes()) {
            if (!geneGeneGroupMap.containsKey(gene)) {
                genesToAdd.add(gene);
            } else {
                GeneGroup geneGroup = geneGeneGroupMap.get(gene);
                geneGroup.addSingleExonIsoforms(showIsoformName, showIsoformID, reverseComplement, cellPlotCleared, cellsSelected);
            }
        }
        if (genesToAdd.size() > 0)
            addGenes(genesToAdd);
    }

    private void redrawIsoforms() {
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean cellPlotCleared = ControllerMediator.getInstance().isCellPlotCleared();
        boolean cellsSelected = ControllerMediator.getInstance().areCellsSelected();
        for(GeneGroup geneGroup : geneGeneGroupMap.values())
            geneGroup.redrawIsoforms(reverseComplement, cellPlotCleared, cellsSelected);
    }

    private void updateFirstGeneGroup() {
        ObservableList<Node> geneGroupsList = geneGroups.getChildren();
        if (geneGroupsList.size() > 0)
            firstGeneGroup = (GeneGroup) geneGroupsList.get(0);
        else
            firstGeneGroup = null;
    }

    private void setScrollPaneWidthSpacing() {
        Insets geneGroupsMargin = VBox.getMargin(geneGroups);
        scrollPaneWidthSpacing = geneGroupsMargin.getLeft() + geneGroupsMargin.getRight();
    }

    private void setUpRedrawIsoformsToMatchScrollPaneWidth() {
        scrollPane.widthProperty().addListener((ov, oldValue, newValue) -> redrawIsoforms());
    }

    /**
     * Adds a 10px of space between the groups of genes in the isoform plot
     */
    private void setGeneGroupsStyling() {
        geneGroups.setSpacing(10);
    }

    /**
     * Returns text describing the type of expression that should be displayed
     * (ex. median, average...). Used for tooltips in the isoform plot
     */
    private static String getToolTipExpressionText(double expression, boolean showMedian, boolean includingZeros) {
        if (showMedian && includingZeros)
            return "Median expression: " + roundToOneDecimal(expression);
        else if (showMedian)
            return "Median non-zero expression: " + roundToOneDecimal(expression);
        else if (includingZeros)
            return "Average expression: " + roundToOneDecimal(expression);
        else
            return "Average non-zero expression: " + roundToOneDecimal(expression);

    }

    private boolean shouldDisplayIsoformPlotLegend() {
        return !ControllerMediator.getInstance().isCellPlotCleared() &&
                ControllerMediator.getInstance().isShowingIsoformPlotLegend() &&
                firstGeneGroup != null;
    }

    /**
     * A gene in the isoform plot with all its isoforms
     */
    private class GeneGroup extends VBox {
        private final Font GENE_FONT = Font.loadFont(getClass().getResource("/fonts/OpenSans-Bold.ttf").toExternalForm(), 16);

        private Gene gene;
        private SelectableText label;
        private HashMap<Isoform, IsoformGroup> isoformsIsoformGroupMap;
        private IsoformGroup firstIsoformGroup;

        public GeneGroup(Gene gene, boolean showGeneNameAndID, boolean showGeneName, boolean showIsoformID,
                         boolean showIsoformName, boolean hideSingleExonIsoforms, boolean reverseComplement,
                         boolean cellPlotCleared, boolean cellsSelected) {
            this.gene = gene;
            isoformsIsoformGroupMap = new HashMap<>();
            addLabel(showGeneNameAndID, showGeneName, reverseComplement);
            addIsoforms(showIsoformName, showIsoformID, hideSingleExonIsoforms, reverseComplement, cellPlotCleared, cellsSelected);
            // spacing is spacing between isoforms
            setSpacing(7.5);
        }

        public void removeIsoform(Isoform isoform) {
            IsoformGroup isoformGroup = isoformsIsoformGroupMap.get(isoform);
            // necessary to remove all references to isoform group so can be cleaned up by
            // garbage collector
            isoformGroup.makeIsoformGraphicNonSelectable();
            getChildren().remove(isoformGroup);
            isoformsIsoformGroupMap.remove(isoform, isoformGroup);
            if (firstIsoformGroup == isoformGroup)
                updateFirstIsoformGroup();
        }

        public void updateLabel(boolean showGeneNameAndID, boolean showGeneName, boolean reverseComplement) {
            label.setTextAndFitWidthToText(getGeneLabelText(showGeneNameAndID, showGeneName, reverseComplement));
        }

        public void makeUnselectable() {
            for (IsoformGroup isoformGroup : isoformsIsoformGroupMap.values())
                isoformGroup.makeIsoformGraphicNonSelectable();
        }

        public void redrawIsoforms(boolean reverseComplement, boolean cellPlotCleared, boolean cellsSelected) {
            double pixelsPerNucleotide = getPixelsPerNucleotide();
            for (IsoformGroup isoformGroup : isoformsIsoformGroupMap.values())
                isoformGroup.redrawIsoformGraphic(pixelsPerNucleotide, reverseComplement, cellPlotCleared, cellsSelected);

        }

        public void updateIsoformLabels(boolean showIsoformName, boolean showIsoformID) {
            for (Isoform isoform : isoformsIsoformGroupMap.keySet()) {
                IsoformGroup isoformGroup = isoformsIsoformGroupMap.get(isoform);
                isoformGroup.updateLabel(showIsoformName, showIsoformID);
            }
        }

        public void removeSingleExonIsoforms() {
            for (Isoform isoform :  gene.getIsoforms())
                if (!isoform.isMultiExonic())
                    removeIsoform(isoform);
        }

        public void addSingleExonIsoforms(boolean showIsoformName, boolean showIsoformID, boolean reverseComplement,
                                          boolean cellPlotCleared, boolean cellsSelected) {
            double pixelsPerNucleotide = getPixelsPerNucleotide();

            for (Isoform isoform : gene.getIsoforms()) {
                if (!isoform.isMultiExonic()) {
                    IsoformGroup isoformGroup = new IsoformGroup(isoform, pixelsPerNucleotide, showIsoformName, showIsoformID,
                                                                 reverseComplement, cellPlotCleared, cellsSelected);
                    addIsoform(isoformGroup);
                }
            }
        }

        public boolean hasSelectedIsoforms() {
            for (IsoformGroup isoformGroup : getIsoformGroups()) {
                if (isoformGroup.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        public Collection<IsoformGroup> getIsoformGroups() {
            return isoformsIsoformGroupMap.values();
        }

        private IsoformGroup getFirstIsoformGroup() {
            return firstIsoformGroup;
        }

        private void addLabel(boolean showGeneNameAndID, boolean showGeneName, boolean reverseComplement) {
            label = new SelectableText();
            label.setFont(GENE_FONT);
            VBox holder = new VBox();
            holder.setFillWidth(false);
            holder.getChildren().add(label);
            label.setTextAndFitWidthToText(getGeneLabelText(showGeneNameAndID, showGeneName, reverseComplement));
            getChildren().add(holder);
        }

        private void addIsoforms(boolean showIsoformName, boolean showIsoformID, boolean hideSingleExonIsoforms,
                                 boolean reverseComplement, boolean cellPlotCleared, boolean cellsSelected) {
            double pixelsPerNucleotide = getPixelsPerNucleotide();
            Collection<String> isoformsID = gene.getIsoformsMap().keySet();
            List<String> sortedIsoformsIDs = Util.asSortedList(isoformsID);
            for (String isoformID : sortedIsoformsIDs) {
                Isoform isoform = gene.getIsoform(isoformID);
                if (!hideSingleExonIsoforms || isoform.isMultiExonic()) {
                    IsoformGroup isoformGroup = new IsoformGroup(isoform, pixelsPerNucleotide, showIsoformName,
                            showIsoformID, reverseComplement, cellPlotCleared, cellsSelected);
                    addIsoform(isoformGroup);
                }
            }
        }

        private String getGeneLabelText(boolean showGeneNameAndID, boolean showGeneName, boolean reverseComplement) {
            String labelText;
            String geneName = gene.getName();
            String geneID = gene.getId();

            if (showGeneNameAndID && geneName != null)
                labelText = geneID + " (" + geneName + ")";
            else if (showGeneName && geneName != null)
                labelText = geneName;
            else
                labelText = geneID;

            if (reverseComplement) {
                if (gene.isOnPositiveStrand())
                    labelText += " (+)";
                else
                    labelText += " (-)";
            }

            return labelText;
        }

        private void addIsoform(IsoformGroup isoformGroup) {
            isoformsIsoformGroupMap.put(isoformGroup.getIsoform(), isoformGroup);
            getChildren().add(isoformGroup);
            if (firstIsoformGroup == null)
                updateFirstIsoformGroup();
        }


        private void updateFirstIsoformGroup() {
            ObservableList<Node> isoformGroupsList = getChildren();
            if (isoformGroupsList.size() > 1)
                firstIsoformGroup = (IsoformGroup) isoformGroupsList.get(1);
            else
                firstIsoformGroup = null;
        }

        private double getPixelsPerNucleotide() {
            return (scrollPane.getWidth() - IsoformGroup.ISOFORM_GROUP_OFFSET - scrollPaneWidthSpacing - SCROLLBAR_WIDTH -
                    IsoformGroup.IsoformGraphic.ISOFORM_GRAPHIC_SPACING - IsoformGroup.getDotPlotSpacing()) /
                    (gene.getEndNucleotide() - gene.getStartNucleotide() + 1);
        }
    }

    /**
     * An isoform in the isoform plot. If showing dot plot, group also contains dot plot row
     * for the isoform
     */
    private static class IsoformGroup extends VBox {
        public static final int ISOFORM_GROUP_OFFSET = 10;
        private static final Font ISOFORM_FONT = Font.loadFont(IsoformGroup.class.getResource("/fonts/OpenSans-Regular.ttf").toExternalForm(), 12);
        private static final int ISOFORM_GRAPHIC_DOT_PLOT_SPACING = 15;

        private Isoform isoform;
        private SelectableText label;
        private BorderPane labelAndLegendHolder;
        private BorderPane graphicsHolder;
        private IsoformGraphic isoformGraphic;
        private HBox dotPlotRow;
        private Pane dotPlotLegend;

        public IsoformGroup(Isoform isoform, double pixelsPerNucleotide, boolean showIsoformName,
                            boolean showIsoformID, boolean reverseComplement, boolean cellPlotCleared, boolean cellsSelected) {
            this.isoform = isoform;
            graphicsHolder = new BorderPane();
            labelAndLegendHolder = new BorderPane();
            Insets isoformGroupMargins = new Insets(0, 0, 0, ISOFORM_GROUP_OFFSET);
            VBox.setMargin(labelAndLegendHolder, isoformGroupMargins);
            VBox.setMargin(graphicsHolder, isoformGroupMargins);
            getChildren().addAll(labelAndLegendHolder, graphicsHolder);
            if (shouldHaveLabel(showIsoformName, showIsoformID))
                addLabel(getIsoformLabelText(showIsoformName, showIsoformID));
            addIsoformGraphic(pixelsPerNucleotide, reverseComplement, cellPlotCleared, cellsSelected);
            // spacing between these isoform and those above/below it
            setSpacing(5);
        }

        public void updateLabel(boolean showIsoformName, boolean showIsoformID) {
            if (shouldHaveLabel(showIsoformName, showIsoformID)) {
                String isoformLabelText = getIsoformLabelText(showIsoformName, showIsoformID);
                if (label == null)
                    addLabel(isoformLabelText);
                else
                    label.setTextAndFitWidthToText(isoformLabelText);
            } else {
                removeLabel();
            }
        }

        public void redrawIsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean cellPlotCleared, boolean cellsSelected) {
            isoformGraphic.redraw(pixelsPerNucleotide, reverseComplement, cellPlotCleared, cellsSelected);
        }

        public void removeLabel() {
            if (label != null) {
                label = null;
                labelAndLegendHolder.setLeft(null);
            }
        }

        public void makeIsoformGraphicNonSelectable() {
            selectionModel.removeSelectedIsoformGraphic(isoformGraphic);
            rectangularSelection.removeSelectableIsoformGraphic(isoformGraphic);
        }

        public void setDotPlotRow(HBox dotPlotRow) {
            this.dotPlotRow = dotPlotRow;
            if (dotPlotRow != null)
                BorderPane.setMargin(dotPlotRow, new Insets(0,0, 0,  ISOFORM_GRAPHIC_DOT_PLOT_SPACING));
            graphicsHolder.setRight(dotPlotRow);
        }

        public void setDotPlotLegend(Pane dotPlotLegend) {
            if (dotPlotLegend != null)
                BorderPane.setAlignment(dotPlotLegend, Pos.CENTER_RIGHT);
            this.dotPlotLegend = dotPlotLegend;
            labelAndLegendHolder.setRight(dotPlotLegend);
        }

        public boolean isSelected() {
            return selectionModel.isIsoformGraphicSelected(isoformGraphic);
        }

        public Isoform getIsoform() {
            return isoform;
        }

        public HBox getDotPlotRow() {
            return dotPlotRow;
        }

        public Pane getDotPlotLegend() {
            return dotPlotLegend;
        }

        /**
         * Returns space required for dot plot row
         */
        public static double getDotPlotSpacing() {
            double dotPlotWidth = DotPlot.getDotPlotWidth();
            if (dotPlotWidth == 0)
                return 0;
            else
                return dotPlotWidth + ISOFORM_GRAPHIC_DOT_PLOT_SPACING;
        }

        private void addIsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean cellPlotCleared, boolean cellsSelected) {
            isoformGraphic = new IsoformGraphic(pixelsPerNucleotide, reverseComplement, cellPlotCleared, cellsSelected);
            graphicsHolder.setLeft(isoformGraphic);
            rectangularSelection.addSelectableIsoformGraphic(isoformGraphic, isoform.getId());
        }


        private boolean shouldHaveLabel(boolean showIsoformName, boolean showIsoformID) {
            return (showIsoformName && isoform.getName() != null)|| showIsoformID;
        }

        private void addLabel(String newLabel) {
            label = new SelectableText();
            label.setFont(ISOFORM_FONT);
            VBox holder = new VBox();
            holder.setFillWidth(false);
            holder.getChildren().add(label);
            labelAndLegendHolder.setLeft(holder);
            label.setTextAndFitWidthToText(newLabel);
        }

        private String getIsoformLabelText(boolean showIsoformName, boolean showIsoformID) {
            String isoformName = isoform.getName();
            String isoformID = isoform.getId();

            if (showIsoformName && showIsoformID && isoformName != null)
                return isoformID + " (" + isoformName + ")";
            else if (showIsoformName && isoformName != null)
                return isoformName;
            else if (showIsoformID)
                return isoformID;
            else
                return "";
        }

        private class IsoformGraphic extends Canvas {
            public final Color DEFAULT_EXON_COLOR = Color.color(0.929, 0.929, 0.929);
            public final Color OUTLINE_COLOUR = Color.BLACK;
            public static final int EXON_HEIGHT = 10;
            // objects on which isoform graphic is drawn have a padding of
            // GRAPHIC_SPACING / 2 all around
            public static final double ISOFORM_GRAPHIC_SPACING = 2;

            private Tooltip toolTip;
            boolean toolTipShowing;

            public IsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean cellPlotCleared, boolean cellsSelected) {
                setWidth(getIsoformGraphicWidth(pixelsPerNucleotide));
                setHeight(EXON_HEIGHT + IsoformGraphic.ISOFORM_GRAPHIC_SPACING);
                toolTip = new Tooltip();
                toolTipShowing = false;
                double expression = getIsoformExpression(cellsSelected);
                drawIsoformGraphic(pixelsPerNucleotide, reverseComplement, cellPlotCleared, expression);
                setToolTip(!cellPlotCleared, expression);
            }

            /**
             * Redraws the isoform graphic and updates the tool tip text (or removes it if it should not be shown)
             */
            public void redraw(double pixelsPerNucleotide, boolean reverseComplement, boolean cellPlotCleared, boolean cellsSelected) {
                setIsoformGraphicWidth(pixelsPerNucleotide);
                clear();
                double expression = getIsoformExpression(cellsSelected);
                drawIsoformGraphic(pixelsPerNucleotide, reverseComplement, cellPlotCleared, expression);
                setToolTip(!cellPlotCleared, expression);
            }

            private double getIsoformExpression(boolean onlySelected) {
                boolean showMedian = ControllerMediator.getInstance().isShowingMedian();
                boolean includeZeros = ControllerMediator.getInstance().isIncludingZeros();
                if (showMedian)
                    return isoform.getMedianExpression(onlySelected, includeZeros);
                else
                    return  isoform.getAverageExpression(onlySelected, includeZeros);
            }

            /**
             * Sets this isoform graphic to its correct width
             */
            private void setIsoformGraphicWidth(double pixelsPerNucleotide) {
                double isoformGraphicWidth = getIsoformGraphicWidth(pixelsPerNucleotide);
                setWidth(isoformGraphicWidth);
            }

            private void clear() {
                GraphicsContext graphicsContext = getGraphicsContext2D();
                graphicsContext.clearRect(0, 0, getWidth(), getHeight());
            }

            /**
             * Gets the width this isoform graphic should be
             */
            private double getIsoformGraphicWidth(double pixelsPerNucleotide) {
                double isoformWidth = (isoform.getEndNucleotide() - isoform.getStartNucleotide() + 1) * pixelsPerNucleotide;
                return isoformWidth + IsoformGraphic.ISOFORM_GRAPHIC_SPACING;
            }

            /**
             * Either removes the tooltip for this isoform graphic (if shouldn't be shown)
             * or sets its text to display the given expression (rounded to one decimal)
             */
            private void setToolTip(boolean shouldShowToolTip, double expression) {
                if (shouldShowToolTip) {
                    boolean showMedian = ControllerMediator.getInstance().isShowingMedian();
                    boolean includeZeros = ControllerMediator.getInstance().isIncludingZeros();
                    toolTip.setText(getToolTipExpressionText(expression, showMedian, includeZeros));
                    if (!toolTipShowing) {
                        Tooltip.install(this, toolTip);
                        toolTipShowing = true;
                    }
                } else {
                    Tooltip.uninstall(this, toolTip);
                    toolTipShowing = false;
                }
            }

            private void drawIsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean cellPlotCleared, double expression) {
                Color isoformColor = getIsoformColor(!cellPlotCleared, expression);
                ArrayList<Exon> exons = isoform.getExons();
                Gene gene = isoform.getGene();
                GraphicsContext graphicsContext = getGraphicsContext2D();

                if(gene.isOnPositiveStrand() || !reverseComplement) {
                    int isoformStart = isoform.getStartNucleotide();
                    int geneStart = gene.getStartNucleotide();

                    double isoformExtraOffset = (isoformStart - geneStart) * pixelsPerNucleotide;
                    BorderPane.setMargin(this, new Insets(0, 0, 0, isoformExtraOffset));
                    drawIsoform(pixelsPerNucleotide, isoformColor, exons, isoformStart, graphicsContext);
                } else {
                    int isoformEnd = isoform.getEndNucleotide();
                    int geneEnd = gene.getEndNucleotide();

                    double isoformExtraOffset = (geneEnd - isoformEnd) * pixelsPerNucleotide;
                    BorderPane.setMargin(this, new Insets(0, 0, 0, isoformExtraOffset));
                    drawIsoformReverseComplement(pixelsPerNucleotide, isoformColor, exons, isoformEnd, graphicsContext);
                }
            }

            private Color getIsoformColor(boolean shouldGetCustomIsoformColor, double expression) {
                Color isoformColor = DEFAULT_EXON_COLOR;
                if (shouldGetCustomIsoformColor)
                    isoformColor = ControllerMediator.getInstance().getColorFromGradient(expression);
                return isoformColor;
            }

            private void drawIsoform(double pixelsPerNucleotide, Color isoformColor, ArrayList<Exon> exons, int isoformStart,
                                     GraphicsContext graphicsContext) {
                for (int i = 0; i < exons.size(); ++i) {
                    drawExon(isoformStart, pixelsPerNucleotide, exons, i, isoformColor, graphicsContext);
                    if (i != 0)
                        drawIntron(isoformStart, pixelsPerNucleotide, exons, i, graphicsContext);
                }
            }

            private void drawIsoformReverseComplement(double pixelsPerNucleotide, Color isoformColor, ArrayList<Exon> exons, int isoformEnd,
                                                      GraphicsContext graphicsContext) {
                for (int i = 0; i < exons.size(); ++i) {
                    drawExonReverseComplement(isoformEnd, pixelsPerNucleotide, exons, i, isoformColor, graphicsContext);
                    if (i != 0) {
                        drawIntronReverseComplement(isoformEnd, pixelsPerNucleotide, exons, i, graphicsContext);
                    }
                }
            }

            private void drawExon(int isoformStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i, Color isoformColor,
                                  GraphicsContext graphicsContext) {
                int exonStart = exons.get(i).getStartNucleotide();
                int exonEnd = exons.get(i).getEndNucleotide();
                double startX = (exonStart - isoformStart) * pixelsPerNucleotide + IsoformGraphic.ISOFORM_GRAPHIC_SPACING / 2;
                double width = (exonEnd - exonStart + 1) * pixelsPerNucleotide;
                drawExonGraphic(startX, width, isoformColor, graphicsContext);
            }

            private void drawIntron(int isoformStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i,
                                    GraphicsContext graphicsContext) {
                int exonStart = exons.get(i).getStartNucleotide() ;
                int prevExonEnd = exons.get(i - 1).getEndNucleotide();
                double startX = (prevExonEnd - isoformStart + 1) * pixelsPerNucleotide + IsoformGraphic.ISOFORM_GRAPHIC_SPACING / 2;
                double endX = (exonStart - isoformStart) * pixelsPerNucleotide + IsoformGraphic.ISOFORM_GRAPHIC_SPACING / 2;
                drawIntronGraphic(startX, endX, graphicsContext);
            }

            private void drawExonReverseComplement(int isoformEnd, double pixelsPerNucleotide, ArrayList<Exon> exons, int i, Color isoformColor,
                                                   GraphicsContext graphicsContext) {
                int exonStart = exons.get(i).getStartNucleotide();
                int exonEnd = exons.get(i).getEndNucleotide();
                double startX = (isoformEnd - exonEnd) * pixelsPerNucleotide + IsoformGraphic.ISOFORM_GRAPHIC_SPACING / 2;
                double width = (exonEnd - exonStart + 1) * pixelsPerNucleotide;
                drawExonGraphic(startX, width, isoformColor, graphicsContext);
            }

            private void drawIntronReverseComplement(int isoformEnd, double pixelsPerNucleotide, ArrayList<Exon> exons, int i,
                                                     GraphicsContext graphicsContext) {
                int exonStart = exons.get(i).getStartNucleotide() ;
                int prevExonEnd = exons.get(i - 1).getEndNucleotide();
                double startX = (isoformEnd - exonStart + 1) * pixelsPerNucleotide + IsoformGraphic.ISOFORM_GRAPHIC_SPACING / 2;
                double endX = (isoformEnd - prevExonEnd) * pixelsPerNucleotide + IsoformGraphic.ISOFORM_GRAPHIC_SPACING / 2;
                drawIntronGraphic(startX, endX, graphicsContext);
            }

            private void drawExonGraphic(double startX, double width, Color isoformColor, GraphicsContext graphicsContext) {
                graphicsContext.setFill(isoformColor);
                graphicsContext.fillRect(startX, IsoformGraphic.ISOFORM_GRAPHIC_SPACING / 2, width, EXON_HEIGHT);
                graphicsContext.setFill(OUTLINE_COLOUR);
                graphicsContext.strokeRect(startX, IsoformGraphic.ISOFORM_GRAPHIC_SPACING / 2, width, EXON_HEIGHT);
            }

            private void drawIntronGraphic(double startX, double endX, GraphicsContext graphicsContext) {
                graphicsContext.setFill(OUTLINE_COLOUR);
                graphicsContext.strokeLine(startX, EXON_HEIGHT / 2, endX, EXON_HEIGHT / 2);
            }
        }
    }

    private static class DotPlot {
        public static final double GRAPHIC_SPACING = 2;
        private static final double QUARTER_EXPRESS_DOT_SIZE = 4.5;
        private static final double HALF_EXPRESS_DOT_SIZE = 9.75;
        private static final double THREE_QUARTERS_EXPRESS_DOT_SIZE = 14.5;
        private static final double ALL_EXPRESS_DOT_SIZE = 18;
        private static final double DOT_PLOT_COLUMN_WIDTH = ALL_EXPRESS_DOT_SIZE + GRAPHIC_SPACING;
        private static final double DOT_PLOT_ROW_HEIGHT = ALL_EXPRESS_DOT_SIZE + GRAPHIC_SPACING;
        private static final int DOT_PLOT_COLUMN_SPACING = 1;
        private static final double DOT_LEGEND_COLUMN_WIDTH = 42;
        private static final double DOT_LEGEND_TEXT_HEIGHT = 9;

        public static void addDotPlotRowsIfShould(GeneGroup geneGroup) {
            if (shouldDrawDotPlot()) {
                for (IsoformGroup isoformGroup : geneGroup.getIsoformGroups())
                    isoformGroup.setDotPlotRow(createDotPlotRow(isoformGroup));
            }
        }

        public static void updateDotPlotLegend(boolean redraw){
            if (firstGeneGroup != null) {
                IsoformGroup firstIsoformGroup = firstGeneGroup.getFirstIsoformGroup();
                Pane dotPlotLegend = firstIsoformGroup.getDotPlotLegend();
                boolean shouldDrawDotPlot = shouldDrawDotPlot();
                if (shouldDrawDotPlot && (dotPlotLegend == null || redraw)) {
                    boolean onlySelectedClusters = ControllerMediator.getInstance().areCellsSelected();
                    CategoryLabelsLegend legend = new CategoryLabelsLegend(false, false, onlySelectedClusters,
                            false, false, ALL_EXPRESS_DOT_SIZE, DOT_PLOT_COLUMN_WIDTH, DOT_PLOT_ROW_HEIGHT, DOT_PLOT_COLUMN_SPACING);
                    firstIsoformGroup.setDotPlotLegend(legend.getLegendGraphic());
                }  else if (dotPlotLegend != null && !shouldDrawDotPlot) {
                    firstIsoformGroup.setDotPlotLegend(null);
                }
            }
        }

        public static Node getDotLegend() {
            VBox dotLegend = new VBox();
            dotLegend.setAlignment(Pos.CENTER);

            Canvas dotLegendCanvas = getDotLegendNoCaption();
            VBox.setMargin(dotLegendCanvas, new Insets(0, 0, 5, 0));
            Text caption = new Text("Cell Proportions");
            caption.setFont(IsoformPlotLegend.LEGEND_FONT);
            dotLegend.getChildren().addAll(dotLegendCanvas, caption);

            return dotLegend;
        }

        public static void updateDotPlot() {
            for (GeneGroup geneGroup : getGeneGroups()) {
                for (IsoformGroup isoformGroup : geneGroup.getIsoformGroups())
                    updateDotPlotRow(isoformGroup);
            }
            updateDotPlotLegend( true);
        }

        public static double getDotPlotWidth() {
            if (shouldDrawDotPlot()) {
                boolean onlySelected = ControllerMediator.getInstance().areCellsSelected();
                int numSelectedClusters = ControllerMediator.getInstance().getClusters(onlySelected).size();
                return numSelectedClusters * DOT_PLOT_COLUMN_WIDTH + (numSelectedClusters - 1) * DOT_PLOT_COLUMN_SPACING + GRAPHIC_SPACING;
            }
            return 0;
        }

        private static void updateDotPlotRow(IsoformGroup isoformGroup) {
            HBox dotPlotRow = isoformGroup.getDotPlotRow();
            boolean shouldDrawDotPlot = shouldDrawDotPlot();
            if (shouldDrawDotPlot) {
                isoformGroup.setDotPlotRow(createDotPlotRow(isoformGroup));
            } else if (dotPlotRow != null){
                isoformGroup.setDotPlotRow(null);
            }
        }

        private static boolean shouldDrawDotPlot() {
            return !(ControllerMediator.getInstance().isHidingDotPlot() || ControllerMediator.getInstance().isCellPlotCleared());
        }

        private static HBox createDotPlotRow(IsoformGroup isoformGroup) {
            HBox dotPlotRow = new HBox();
            boolean onlySelected = ControllerMediator.getInstance().areCellsSelected();
            Collection<Cluster> clusters = ControllerMediator.getInstance().getClusters(onlySelected);
            Iterator<Cluster> iterator = clusters.iterator();

            while(iterator.hasNext()) {
                Cluster cluster = iterator.next();
                double expression = getIsoformExpressionInCluster(cluster, isoformGroup.getIsoform(), onlySelected);
                int numExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoformGroup.getIsoform().getId(), cluster, onlySelected);
                int numCells = onlySelected? ControllerMediator.getInstance().getSelectedCellsInCluster(cluster).size() : cluster.getCells().size();

                Canvas dotPlotRowCircle = getDotPlotRowCircle(expression, numExpressingCells, numCells);
                addExpressionLevelToolTip(expression, numExpressingCells, numCells, dotPlotRowCircle);
                if (iterator.hasNext())
                    HBox.setMargin(dotPlotRowCircle, new Insets(0, DOT_PLOT_COLUMN_SPACING, 0, 0));
                dotPlotRow.getChildren().add(dotPlotRowCircle);
            }
            return dotPlotRow;
        }

        private static Canvas getDotLegendNoCaption() {
            double width = DOT_LEGEND_COLUMN_WIDTH * 4 + DOT_PLOT_COLUMN_SPACING * 3;
            double height = DOT_PLOT_ROW_HEIGHT + DOT_LEGEND_TEXT_HEIGHT;
            Canvas dotLegendCanvas = new Canvas(width, height);
            GraphicsContext graphicsContext = dotLegendCanvas.getGraphicsContext2D();
            double[] dotSizesToDraw = new double[]{QUARTER_EXPRESS_DOT_SIZE, HALF_EXPRESS_DOT_SIZE,
                    THREE_QUARTERS_EXPRESS_DOT_SIZE, ALL_EXPRESS_DOT_SIZE};

            double dotX = DOT_LEGEND_COLUMN_WIDTH / 2;
            double dotY = DOT_PLOT_ROW_HEIGHT / 2;
            for (double dotSize : dotSizesToDraw) {
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.strokeOval(dotX - dotSize / 2, dotY - dotSize / 2, dotSize, dotSize);

                graphicsContext.setFont(IsoformPlotLegend.LEGEND_FONT);
                String text;
                if (dotSize == QUARTER_EXPRESS_DOT_SIZE) text = "≤ 25%";
                else if (dotSize == HALF_EXPRESS_DOT_SIZE) text = "≤ 50%";
                else if (dotSize == THREE_QUARTERS_EXPRESS_DOT_SIZE) text = "≤ 75%";
                else text = "≤ 100%";

                double offset = new Text(text).getLayoutBounds().getWidth() / 2;
                graphicsContext.fillText(text, dotX -offset, DOT_PLOT_ROW_HEIGHT + DOT_LEGEND_TEXT_HEIGHT);
                dotX += DOT_LEGEND_COLUMN_WIDTH + DOT_PLOT_COLUMN_SPACING;
            }
            return dotLegendCanvas;
        }

        private static double getIsoformExpressionInCluster(Cluster cluster, Isoform isoform, boolean onlySelected) {
            boolean showMedian = ControllerMediator.getInstance().isShowingMedian();
            if (showMedian)
                return isoform.getMedianExpressionInCluster(cluster, onlySelected, false);
            else
                return  isoform.getAverageExpressionInCluster(cluster, onlySelected, false);
        }

        private static double getDotSize(double fractionExpressingCells) {
            if (fractionExpressingCells <= 0.25)
                return QUARTER_EXPRESS_DOT_SIZE;
            else if (fractionExpressingCells <= 0.5)
                return HALF_EXPRESS_DOT_SIZE;
            else if (fractionExpressingCells <= 0.75)
                return THREE_QUARTERS_EXPRESS_DOT_SIZE;
            else
                return ALL_EXPRESS_DOT_SIZE;
        }

        private static Canvas getDotPlotRowCircle(double expression, int numExpressingCells, int numCells) {
            Canvas dotPlotRowItem = new Canvas(DOT_PLOT_COLUMN_WIDTH, DOT_PLOT_ROW_HEIGHT);

            if (expression >= ControllerMediator.getInstance().getGradientMin()) {
                double dotX = DOT_PLOT_COLUMN_WIDTH / 2;
                double dotY = DOT_PLOT_ROW_HEIGHT / 2;
                double dotSize = getDotSize((double) numExpressingCells/numCells);
                Color dotColor = ControllerMediator.getInstance().getColorFromGradient(expression);

                GraphicsContext graphicsContext = dotPlotRowItem.getGraphicsContext2D();
                graphicsContext.setFill(dotColor);
                graphicsContext.fillOval(dotX - dotSize / 2, dotY - dotSize / 2, dotSize, dotSize);
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.strokeOval(dotX - dotSize / 2, dotY - dotSize / 2, dotSize, dotSize);
            }
            return dotPlotRowItem;
        }

        private static void addExpressionLevelToolTip(double expression, int numExpressingCells, int numCells, Node node) {
            double percentExpressed = roundToOneDecimal(((double) numExpressingCells / numCells) * 100);
            boolean showMedian = ControllerMediator.getInstance().isShowingMedian();
            Tooltip tooltip = new Tooltip(getToolTipExpressionText(expression, showMedian, false) + "\n" +
                                                "Cells: " + numExpressingCells + "/" + numCells + " (" + percentExpressed + "%)");
            Tooltip.install(node, tooltip);
        }
    }

    private static class IsoformPlotLegend extends HBox {
        public static final Font LEGEND_FONT = Font.loadFont(IsoformPlotLegend.class.getResource("/fonts/OpenSans-Regular.ttf").toExternalForm(), 11);

        private GradientLegend gradientLegend;
        private static String expressionUnit;
        private Node dotLegend;

        public IsoformPlotLegend() {
            setAlignment(Pos.BOTTOM_CENTER);
            setFillHeight(false);
            addGradientLegend();
            if (DotPlot.shouldDrawDotPlot())
                addDotLegend();
        }

        public void updateIsoformPlotLegend(boolean redraw) {
            if (redraw)
                gradientLegend.redraw();
            if (DotPlot.shouldDrawDotPlot() && dotLegend == null)
                addDotLegend();
            else if (!DotPlot.shouldDrawDotPlot() && dotLegend != null)
                removeDotLegend();
        }

        public static void setExpressionUnit(String expressionUnit) {
            IsoformPlotLegend.expressionUnit = expressionUnit;
        }

        public static String getExpressionUnit() {
            return expressionUnit;
        }

        private void addGradientLegend() {
            gradientLegend = new GradientLegend();
            getChildren().add(gradientLegend);
        }

        private void addDotLegend() {
            dotLegend = DotPlot.getDotLegend();
            HBox.setMargin(dotLegend, new Insets(0, 0, 0, 10));
            getChildren().add(dotLegend);
        }

        private void removeDotLegend() {
            getChildren().remove(dotLegend);
            dotLegend = null;
        }

        private class GradientLegend extends VBox{
            private static final double GRADIENT_HEIGHT = DotPlot.ALL_EXPRESS_DOT_SIZE;
            private static final double GRADIENT_WIDTH = 200;

            private Rectangle gradient;
            private Text minText;
            private Text midText;
            private Text maxText;
            private Text scale;

            public GradientLegend() {
                setAlignment(Pos.CENTER);

                initializeGradient();
                initializeGradientExpressionLabels();
                initializeGradientScale();

                StackPane expressionLabels = new StackPane();
                expressionLabels.getChildren().addAll(minText, midText, maxText);
                StackPane.setAlignment(minText, Pos.CENTER_LEFT);
                StackPane.setAlignment(midText, Pos.CENTER);
                StackPane.setAlignment(maxText, Pos.CENTER_RIGHT);

                getChildren().addAll(expressionLabels, gradient, scale);
            }

            private void initializeGradient() {
                gradient = new Rectangle(GRADIENT_WIDTH, GRADIENT_HEIGHT);
                gradient.setFill(ControllerMediator.getInstance().getGradientFill());
                VBox.setMargin(gradient, new Insets(5, 0, 5, 0));
                gradient.setStyle("-fx-stroke: black; -fx-stroke-width: 1");
            }

            private void initializeGradientExpressionLabels() {
                minText = new Text(getMinText());
                minText.setFont(LEGEND_FONT);

                midText = new Text(getMidText());
                midText.setFont(LEGEND_FONT);

                maxText = new Text(getMaxText());
                maxText.setFont(LEGEND_FONT);
            }

            private void initializeGradientScale() {
                scale = new Text(getScaleText());
                scale.setFont(LEGEND_FONT);
            }

            public void redraw() {
                gradient.setFill(ControllerMediator.getInstance().getGradientFill());
                minText.setText(getMinText());
                midText.setText(getMidText());
                maxText.setText(getMaxText());
                scale.setText(getScaleText());
            }

            private String getMinText() {
                return roundAndConvertToString(ControllerMediator.getInstance().getGradientMin());
            }

            private String getMidText() {
                return roundAndConvertToString(ControllerMediator.getInstance().getGradientMid());
            }

            private String getMaxText() {
                return roundAndConvertToString(ControllerMediator.getInstance().getGradientMax());
            }

            private String getScaleText() {
                String scaleText = "Expression Level";
                boolean usingLinearScale = ControllerMediator.getInstance().getScaleOptionInUse().equals(GradientAdjusterController.LINEAR_SCALE_OPTION);
                String scaleOption = usingLinearScale ? "Linear" : "Log";
                if (expressionUnit != null)
                        return scaleText + " (" + expressionUnit + ", " + scaleOption + ")";
                else
                    return scaleText + " (" + scaleOption + ")";
            }

            /**
             * Rounds number to closest whole number if >= 1 or = 0, else rounds number to
             * one decimal. Converts result to string
             */
            public String roundAndConvertToString(double num) {
                if (num >= 1)
                    return Integer.toString((int) Math.round(num));
                else if (num == 0)
                    return Integer.toString((int) num);
                else
                    return Double.toString(Util.roundToOneDecimal(num));
            }
        }
    }

    private static class SelectableText extends TextField {

        public SelectableText() {
            super();
            setEditable(false);
            setStyle("-fx-background-color: transparent;" +
                     "-fx-background-insets: 0;" +
                     "-fx-padding: 1 3 1 0;");
        }

        public void setTextAndFitWidthToText(String text) {
            setText(text);
            setPrefWidth(getTextWidth());
        }

        private float getTextWidth() {
            Text text = new Text(getText());
            text.setFont(getFont());
            return (float) text.getLayoutBounds().getWidth() + 5;
        }

    }

    public class SelectionModel {
        private Map<IsoformGroup.IsoformGraphic, String> selection = new HashMap<>();

        public void addSelectedIsoformGraphic(IsoformGroup.IsoformGraphic isoformGraphic, String id) {
            if (!selection.containsKey(isoformGraphic)) {
                isoformGraphic.setStyle("-fx-effect: dropshadow(one-pass-box, #ffafff, 7, 7, 0, 0);");
                selection.put(isoformGraphic, id);
            }
        }

        public void removeSelectedIsoformGraphic(IsoformGroup.IsoformGraphic isoformGraphic) {
            if (selection.containsKey(isoformGraphic)) {
                isoformGraphic.setStyle("-fx-effect: null");
                selection.remove(isoformGraphic);
            }
        }

        public void clearSelectedIsoformGraphics() {
            while (!selection.isEmpty())
                removeSelectedIsoformGraphic(selection.keySet().iterator().next());
        }

        public void selectIsoformsWithGivenIDs(Collection<String> isoformIDs) {
            for (String isoformID : isoformIDs) {
                IsoformGroup.IsoformGraphic isoformGraphic = rectangularSelection.getSelectableIsoformGraphic(isoformID);
                addSelectedIsoformGraphic(isoformGraphic, isoformID);
            }
        }

        public boolean isIsoformGraphicSelected(IsoformGroup.IsoformGraphic isoformGraphic) {
            return selection.containsKey(isoformGraphic);
        }

        public boolean areIsoformGraphicsSelected() {
            return selection.size() > 0;
        }

        public Collection<String> getSelectedIsoformIDs() {
            return selection.values();
        }
    }

    private class RectangularSelection {
        private final DragContext dragContext = new DragContext();
        private Rectangle selectionBox;
        private Pane group;
        private HashMap<IsoformGroup.IsoformGraphic, String> selectableIsoformGraphics;

        public RectangularSelection(Pane group) {
            this.group = group;
            selectableIsoformGraphics = new HashMap<>();

            setUpSelectionBox();

            group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
        }

        public void addSelectableIsoformGraphic(IsoformGroup.IsoformGraphic isoformGraphic, String isoformID) {
            selectableIsoformGraphics.put(isoformGraphic, isoformID);
        }

        public void removeSelectableIsoformGraphic(IsoformGroup.IsoformGraphic isoformGraphic) {
            selectableIsoformGraphics.remove(isoformGraphic);
        }

        public IsoformGroup.IsoformGraphic getSelectableIsoformGraphic(String isoformID) {
            for (Map.Entry<IsoformGroup.IsoformGraphic, String> entry : selectableIsoformGraphics.entrySet()) {
                if (entry.getValue().equals(isoformID))
                    return entry.getKey();
            }
            return null;
        }

        private void setUpSelectionBox() {
            selectionBox = new Rectangle( 0,0,0,0);
            selectionBox.setStyle("-fx-stroke: orange; " +
                    "-fx-stroke-width: 0.25; " +
                    "-fx-stroke-dash-array: 2; " +
                    "-fx-stroke-dash-offset: 6;");
            selectionBox.setFill(Color.color(1, 0, 1, 0.2));
        }

        EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragContext.mouseAnchorX = event.getX();
                dragContext.mouseAnchorY = event.getY();
                selectionBox.setX(dragContext.mouseAnchorX);
                selectionBox.setY(dragContext.mouseAnchorY);
                selectionBox.setWidth(0);
                selectionBox.setHeight(0);

                group.getChildren().add(selectionBox);
                event.consume();
            }
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!ControllerMediator.getInstance().isColoringCellPlotBySelectedIsoform()) {
                    double offsetX = event.getX() - dragContext.mouseAnchorX;
                    double offsetY = event.getY() - dragContext.mouseAnchorY;

                    if (offsetX > 0)
                        selectionBox.setWidth(offsetX);
                    else {
                        selectionBox.setX(event.getX());
                        selectionBox.setWidth(dragContext.mouseAnchorX - selectionBox.getX());
                    }

                    if (offsetY > 0) {
                        selectionBox.setHeight(offsetY);
                    } else {
                        selectionBox.setY(event.getY());
                        selectionBox.setHeight(dragContext.mouseAnchorY - selectionBox.getY());
                    }
                    event.consume();
                }
            }
        };

        EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean changedSelection = updateSelection(event);

                clearRectangularSelection();

                if (changedSelection)
                    ControllerMediator.getInstance().clusterViewHandleChangedIsoformSelection();

                event.consume();
            }

            private boolean updateSelection(MouseEvent event) {
                boolean changedSelectionOnClear = clearSelectionIfShould(event);
                boolean changedSelectionOnAddingRemoving = addOrRemoveFromSelection(event);

                return changedSelectionOnClear || changedSelectionOnAddingRemoving;
            }

            private boolean clearSelectionIfShould(MouseEvent event) {
                boolean coloringCellPlotByIsoform = ControllerMediator.getInstance().isColoringCellPlotBySelectedIsoform();
                boolean isoformGraphicsAreSelected = selectionModel.areIsoformGraphicsSelected();

                if(!event.isControlDown() && (coloringCellPlotByIsoform || !event.isShiftDown()) && isoformGraphicsAreSelected) {
                    selectionModel.clearSelectedIsoformGraphics();
                    return true;
                }
                return false;
            }

            private boolean addOrRemoveFromSelection(MouseEvent event) {
                boolean changedSelection = false;
                for(IsoformGroup.IsoformGraphic isoformGraphic: selectableIsoformGraphics.keySet()) {
                    boolean graphicInSelectionBox = isoformGraphic.localToScene(isoformGraphic.getBoundsInLocal()).intersects(selectionBox.localToScene(selectionBox.getBoundsInLocal()));
                    if(graphicInSelectionBox) {
                        boolean graphicSelected = selectionModel.isIsoformGraphicSelected(isoformGraphic);
                        if(graphicSelected && event.isControlDown()) {
                            selectionModel.removeSelectedIsoformGraphic(isoformGraphic);
                            if (!changedSelection)
                                changedSelection = true;
                        } else if (!graphicSelected && !event.isControlDown()) {
                            selectionModel.addSelectedIsoformGraphic(isoformGraphic, selectableIsoformGraphics.get(isoformGraphic));
                            if (!changedSelection)
                                changedSelection = true;
                        }
                    }
                }
                return changedSelection;
            }
        };

        private void clearRectangularSelection() {
            selectionBox.setX(0);
            selectionBox.setY(0);
            selectionBox.setWidth(0);
            selectionBox.setHeight(0);
            group.getChildren().remove(selectionBox);
        }

        private final class DragContext {
            public double mouseAnchorX;
            public double mouseAnchorY;
        }
    }
}
