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
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import labelset.Cluster;
import mediator.ControllerMediator;
import ui.LegendMaker;
import util.Util;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static util.Util.roundToOneDecimal;

public class IsoformPlotController implements Initializable, InteractiveElementController {
    private static final int SCROLLBAR_WIDTH = 16;

    @FXML private VBox isoformPlotPanel;
    @FXML private Button selectGenesButton;
    @FXML private Button setTPMGradientButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Pane isoformPlot;
    @FXML private VBox geneGroups;
    private static GeneGroup firstGeneGroup;

    private static SelectionModel selectionModel;
    private static RectangularSelection rectangularSelection;
    private static HashMap<Gene, GeneGroup> geneGeneGroupMap;
    private double scrollPaneWidthSpacing;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectionModel = new SelectionModel();
        geneGeneGroupMap = new HashMap<>();
        rectangularSelection = new RectangularSelection(isoformPlot);
        setScrollPaneWidthSpacing();
        setUpRedrawIsoformsToMatchScrollPaneWidth();
        setGeneGroupsStyling();
    }

    public Node getIsoformPlot() {
        return isoformPlotPanel;
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        selectGenesButton.setDisable(true);
        setTPMGradientButton.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        selectGenesButton.setDisable(false);
        setTPMGradientButton.setDisable(false);
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
        boolean tSNEPlotCleared = ControllerMediator.getInstance().isTSNEPlotCleared();
        boolean cellsSelected = ControllerMediator.getInstance().areCellsSelected();
        for (Gene gene : genes) {
            if (!geneGeneGroupMap.containsKey(gene) && (!hideSingleExonIsoforms || gene.hasMultiExonIsoforms())) {
                GeneGroup geneGroup = new GeneGroup(gene, showGeneNameAndID, showGeneName, showIsoformID, showIsoformName,
                                                    hideSingleExonIsoforms, reverseComplement, tSNEPlotCleared, cellsSelected);
                geneGroups.getChildren().add(geneGroup);
                geneGeneGroupMap.put(gene, geneGroup);
                DotPlot.addDotPlotRowsIfShould(geneGroup);
                if (firstGeneGroup == null)
                    updateFirstGeneGroup();
            }
        }
        DotPlot.updateDotPlotLegend(false);
    }

    /**
     * Removes all given genes that are in the isoform plot
     */
    public void removeGenes(Collection<Gene> genes) {
        for (Gene gene : genes) {
            if (geneGeneGroupMap.containsKey(gene)) {
                GeneGroup geneGroup = geneGeneGroupMap.get(gene);
                // gene group must be made unselectable to remove references to its isoform
                // graphics
                geneGroup.makeUnselectable();
                geneGroups.getChildren().remove(geneGroup);
                geneGeneGroupMap.remove(gene, geneGroup);
                if (firstGeneGroup == geneGroup)
                    updateFirstGeneGroup();
            }
        }
        DotPlot.updateDotPlotLegend(false);
    }

    public void updateIsoformGraphicsAndDotPlot() {
        redrawIsoforms();
        DotPlot.updateDotPlot();
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
        boolean tSNEPlotCleared = ControllerMediator.getInstance().isTSNEPlotCleared();
        boolean cellsSelected = ControllerMediator.getInstance().areCellsSelected();
        boolean showGeneName = ControllerMediator.getInstance().isShowingGeneName();
        for (Gene gene : geneGeneGroupMap.keySet()) {
            GeneGroup geneGroup = geneGeneGroupMap.get(gene);
            geneGroup.updateLabel(showGeneNameAndID, showGeneName, reverseComplement);
            if (!gene.isOnPositiveStrand())
                geneGroup.redrawIsoforms(reverseComplement, tSNEPlotCleared, cellsSelected);
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
     * Should be called when hide dot plot status changes.
     * Updates isoform graphics and dot plot if necessary (i.e. if t-SNE plot isn't cleared)
     */
    public void updateHideDotPlotStatus() {
        if (!ControllerMediator.getInstance().isTSNEPlotCleared())
            updateIsoformGraphicsAndDotPlot();
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

    public static Collection<GeneGroup> getGeneGroups() {
        return geneGeneGroupMap.values();
    }

    /**
     * Opens up gene selector window when handle select genes button is pressed
     */
    @FXML
    protected void handleSelectGenesButton() {
        ControllerMediator.getInstance().displayGeneSelector();
    }

    /**
     * Opens up TPM gradient adjuster window when handle set TPM gradient button is pressed
     */
    @FXML
    protected void handleSetTPMGradientButton() {
        ControllerMediator.getInstance().displayTPMGradientAdjuster();
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
        boolean tSNEPlotCleared = ControllerMediator.getInstance().isTSNEPlotCleared();
        boolean cellsSelected = ControllerMediator.getInstance().areCellsSelected();
        for (Gene gene : ControllerMediator.getInstance().getShownGenes()) {
            if (!geneGeneGroupMap.containsKey(gene)) {
                genesToAdd.add(gene);
            } else {
                GeneGroup geneGroup = geneGeneGroupMap.get(gene);
                geneGroup.addSingleExonIsoforms(showIsoformName, showIsoformID, reverseComplement, tSNEPlotCleared, cellsSelected);
            }
        }
        if (genesToAdd.size() > 0)
            addGenes(genesToAdd);
    }

    private void redrawIsoforms() {
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean tSNEPlotCleared = ControllerMediator.getInstance().isTSNEPlotCleared();
        boolean cellsSelected = ControllerMediator.getInstance().areCellsSelected();
        for(GeneGroup geneGroup : geneGeneGroupMap.values())
            geneGroup.redrawIsoforms(reverseComplement, tSNEPlotCleared, cellsSelected);
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
     * A gene in the isoform plot with all its isoforms
     */
    private class GeneGroup extends VBox {
        private final Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);

        private Gene gene;
        private SelectableText label;
        private HashMap<Isoform, IsoformGroup> isoformsIsoformGroupMap;
        private IsoformGroup firstIsoformGroup;

        public GeneGroup(Gene gene, boolean showGeneNameAndID, boolean showGeneName, boolean showIsoformID,
                         boolean showIsoformName, boolean hideSingleExonIsoforms, boolean reverseComplement,
                         boolean tSNEPlotCleared, boolean cellsSelected) {
            this.gene = gene;
            isoformsIsoformGroupMap = new HashMap<>();
            addLabel(showGeneNameAndID, showGeneName, reverseComplement);
            addIsoforms(showIsoformName, showIsoformID, hideSingleExonIsoforms, reverseComplement, tSNEPlotCleared, cellsSelected);
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

        public Collection<IsoformGroup> getIsoformGroups() {
            return isoformsIsoformGroupMap.values();
        }

        private IsoformGroup getFirstIsoformGroup() {
            return firstIsoformGroup;
        }

        public void redrawIsoforms(boolean reverseComplement, boolean tSNEPlotCleared, boolean cellsSelected) {
            double pixelsPerNucleotide = getPixelsPerNucleotide();
            for (IsoformGroup isoformGroup : isoformsIsoformGroupMap.values())
                isoformGroup.redrawIsoformGraphic(pixelsPerNucleotide, reverseComplement, tSNEPlotCleared, cellsSelected);

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
                                          boolean tSNEPlotCleared, boolean cellsSelected) {
            double pixelsPerNucleotide = getPixelsPerNucleotide();

            for (Isoform isoform : gene.getIsoforms()) {
                if (!isoform.isMultiExonic()) {
                    IsoformGroup isoformGroup = new IsoformGroup(isoform, pixelsPerNucleotide, showIsoformName, showIsoformID,
                                                                 reverseComplement, tSNEPlotCleared, cellsSelected);
                    addIsoform(isoformGroup);
                }
            }
        }

        private void addLabel(boolean showGeneNameAndID, boolean showGeneName, boolean reverseComplement) {
            label = new SelectableText();
            label.setFont(GENE_FONT);
            label.setTextAndFitWidthToText(getGeneLabelText(showGeneNameAndID, showGeneName, reverseComplement));
            getChildren().add(label);
        }

        private void addIsoforms(boolean showIsoformName, boolean showIsoformID, boolean hideSingleExonIsoforms,
                                 boolean reverseComplement, boolean tSNEPlotCleared, boolean cellsSelected) {
            double pixelsPerNucleotide = getPixelsPerNucleotide();
            Collection<String> isoformsID = gene.getIsoformsMap().keySet();
            List<String> sortedIsoformsIDs = Util.asSortedList(isoformsID);
            for (String isoformID : sortedIsoformsIDs) {
                Isoform isoform = gene.getIsoform(isoformID);
                if (!hideSingleExonIsoforms || isoform.isMultiExonic()) {
                    IsoformGroup isoformGroup = new IsoformGroup(isoform, pixelsPerNucleotide, showIsoformName,
                            showIsoformID, reverseComplement, tSNEPlotCleared, cellsSelected);
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
        private static final Font ISOFORM_FONT = Font.font("Verdana",12);
        private static final int ISOFORM_GRAPHIC_DOT_PLOT_SPACING = 5;

        private Isoform isoform;
        private SelectableText label;
        private BorderPane labelAndLegendHolder;
        private BorderPane graphicsHolder;
        private IsoformGraphic isoformGraphic;
        private HBox dotPlotRow;
        private Pane dotPlotLegend;

        public IsoformGroup(Isoform isoform, double pixelsPerNucleotide, boolean showIsoformName,
                            boolean showIsoformID, boolean reverseComplement, boolean tSNEPlotCleared, boolean cellsSelected) {
            this.isoform = isoform;
            graphicsHolder = new BorderPane();
            labelAndLegendHolder = new BorderPane();
            Insets isoformGroupMargins = new Insets(0, 0, 0, ISOFORM_GROUP_OFFSET);
            VBox.setMargin(labelAndLegendHolder, isoformGroupMargins);
            VBox.setMargin(graphicsHolder, isoformGroupMargins);
            getChildren().addAll(labelAndLegendHolder, graphicsHolder);
            if (shouldHaveLabel(showIsoformName, showIsoformID))
                addLabel(getIsoformLabelText(showIsoformName, showIsoformID));
            addIsoformGraphic(pixelsPerNucleotide, reverseComplement, tSNEPlotCleared, cellsSelected);
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

        public void redrawIsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean tSNEPlotCleared, boolean cellsSelected) {
            isoformGraphic.redraw(pixelsPerNucleotide, reverseComplement, tSNEPlotCleared, cellsSelected);
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
            graphicsHolder.setRight(dotPlotRow);
        }

        public void setDotPlotLegend(Pane dotPlotLegend) {
            if (dotPlotLegend != null)
                BorderPane.setAlignment(dotPlotLegend, Pos.CENTER_RIGHT);
            this.dotPlotLegend = dotPlotLegend;
            labelAndLegendHolder.setRight(dotPlotLegend);
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

        private void addIsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean tSNEPlotCleared, boolean cellsSelected) {
            isoformGraphic = new IsoformGraphic(pixelsPerNucleotide, reverseComplement, tSNEPlotCleared, cellsSelected);
            graphicsHolder.setLeft(isoformGraphic);
            rectangularSelection.addSelectableIsoformGraphic(isoformGraphic, isoform.getId());
        }


        private boolean shouldHaveLabel(boolean showIsoformName, boolean showIsoformID) {
            return (showIsoformName && isoform.getName() != null)|| showIsoformID;
        }

        private void addLabel(String newLabel) {
            label = new SelectableText();
            label.setFont(ISOFORM_FONT);
            labelAndLegendHolder.setLeft(label);
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

            public IsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean tSNEPlotCleared, boolean cellsSelected) {
                setWidth(getIsoformGraphicWidth(pixelsPerNucleotide));
                setHeight(EXON_HEIGHT + IsoformGraphic.ISOFORM_GRAPHIC_SPACING);

                toolTip = new Tooltip();
                toolTipShowing = false;
                double expression = isoform.getIsoformExpressionLevel(cellsSelected);
                drawIsoformGraphic(pixelsPerNucleotide, reverseComplement, tSNEPlotCleared, expression);
                setToolTip(!tSNEPlotCleared, expression);
            }

            /**
             * Redraws the isoform graphic and updates the tool tip text (or removes it if it should not be shown)
             */
            public void redraw(double pixelsPerNucleotide, boolean reverseComplement, boolean tSNEPlotCleared, boolean cellsSelected) {
                setIsoformGraphicWidth(pixelsPerNucleotide);
                clear();
                double expression = isoform.getIsoformExpressionLevel(cellsSelected);
                drawIsoformGraphic(pixelsPerNucleotide, reverseComplement, tSNEPlotCleared, expression);
                setToolTip(!tSNEPlotCleared, expression);
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
                    toolTip.setText("TPM: " + roundToOneDecimal(expression));
                    if (!toolTipShowing) {
                        Tooltip.install(this, toolTip);
                        toolTipShowing = true;
                    }
                } else {
                    Tooltip.uninstall(this, toolTip);
                    toolTipShowing = false;
                }
            }

            private void drawIsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean tSNEPlotCleared, double expression) {
                Color isoformColor = getIsoformColor(!tSNEPlotCleared, expression);
                ArrayList<Exon> exons = isoform.getExons();
                Gene gene = isoform.getGene();
                GraphicsContext graphicsContext = getGraphicsContext2D();

                if(gene.isOnPositiveStrand() || !reverseComplement) {
                    int isoformStart = isoform.getStartNucleotide();
                    int geneStart = gene.getStartNucleotide();

                    double isoformExtraOffset = (isoformStart - geneStart) * pixelsPerNucleotide;
                    BorderPane.setMargin(this, new Insets(0, ISOFORM_GRAPHIC_DOT_PLOT_SPACING, 0, isoformExtraOffset));
                    drawIsoform(pixelsPerNucleotide, isoformColor, exons, isoformStart, graphicsContext);
                } else {
                    int isoformEnd = isoform.getEndNucleotide();
                    int geneEnd = gene.getEndNucleotide();

                    double isoformExtraOffset = (geneEnd - isoformEnd) * pixelsPerNucleotide;
                    BorderPane.setMargin(this, new Insets(0, 10, 0, ISOFORM_GROUP_OFFSET + isoformExtraOffset));
                    drawIsoformReverseComplement(pixelsPerNucleotide, isoformColor, exons, isoformEnd, graphicsContext);
                }
            }

            private Color getIsoformColor(boolean shouldGetCustomIsoformColor, double expression) {
                Color isoformColor = DEFAULT_EXON_COLOR;
                if (shouldGetCustomIsoformColor)
                    isoformColor = ControllerMediator.getInstance().getColorFromTPMGradient(expression);
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
        private static final double QUARTER_EXPRESS_DOT_SIZE = 4;
        private static final double HALF_EXPRESS_DOT_SIZE = 7.5;
        private static final double THREE_QUARTERS_EXPRESS_DOT_SIZE = 11;
        private static final double ALL_EXPRESS_DOT_SIZE = 14.5;
        private static final double DOT_PLOT_COLUMN_WIDTH = ALL_EXPRESS_DOT_SIZE + GRAPHIC_SPACING;
        private static final double DOT_PLOT_ROW_HEIGHT = ALL_EXPRESS_DOT_SIZE + GRAPHIC_SPACING;
        private static final int DOT_PLOT_COLUMN_SPACING = 1;

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
                    firstIsoformGroup.setDotPlotLegend(LegendMaker.createLegend(false, onlySelectedClusters, false, false,
                            ALL_EXPRESS_DOT_SIZE, DOT_PLOT_COLUMN_WIDTH, DOT_PLOT_ROW_HEIGHT, DOT_PLOT_COLUMN_SPACING));
                }  else if (dotPlotLegend != null && !shouldDrawDotPlot) {
                    firstIsoformGroup.setDotPlotLegend(null);
                }
            }
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
            return !(ControllerMediator.getInstance().isHidingDotPlot() || ControllerMediator.getInstance().isTSNEPlotCleared());
        }

        private static HBox createDotPlotRow(IsoformGroup isoformGroup) {
            HBox dotPlotRow = new HBox();
            double dotX = DOT_PLOT_COLUMN_WIDTH / 2;
            double dotY = DOT_PLOT_ROW_HEIGHT / 2;
            boolean onlySelected = ControllerMediator.getInstance().areCellsSelected();
            Collection<Cluster> clusters = ControllerMediator.getInstance().getClusters(onlySelected);
            Iterator<Cluster> iterator = clusters.iterator();

            while(iterator.hasNext()) {
                Cluster cluster = iterator.next();
                double expression = isoformGroup.getIsoform().getIsoformExpressionLevelInCluster(cluster, onlySelected);
                int numExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoformGroup.getIsoform().getId(), cluster, onlySelected);
                int numCells = onlySelected? ControllerMediator.getInstance().getSelectedCellsInCluster(cluster).size() : cluster.getCells().size();
                double dotSize = getDotSize((double) numExpressingCells/numCells);
                Canvas dotPlotRowCircle = getDotPlotRowCircle(dotX, dotY, expression, dotSize);
                addExpressionLevelToolTip(expression, numExpressingCells, numCells, dotPlotRowCircle);
                if (iterator.hasNext())
                    HBox.setMargin(dotPlotRowCircle, new Insets(0, DOT_PLOT_COLUMN_SPACING, 0, 0));
                dotPlotRow.getChildren().add(dotPlotRowCircle);
            }
            return dotPlotRow;
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

        private static Canvas getDotPlotRowCircle(double dotX, double dotY, double expression, double dotSize) {
            Canvas dotPlotRowItem = new Canvas(DOT_PLOT_COLUMN_WIDTH, DOT_PLOT_ROW_HEIGHT);
            GraphicsContext graphicsContext = dotPlotRowItem.getGraphicsContext2D();
            Color dotColor = ControllerMediator.getInstance().getColorFromTPMGradient(expression);
            graphicsContext.setFill(dotColor);
            graphicsContext.fillOval(dotX - dotSize / 2, dotY -dotSize / 2, dotSize, dotSize);
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.strokeOval(dotX - dotSize / 2, dotY - dotSize / 2, dotSize, dotSize);
            return dotPlotRowItem;
        }

        private static void addExpressionLevelToolTip(double expression, int numExpressingCells, int numCells, Node node) {
            double percentExpressed = roundToOneDecimal(((double) numExpressingCells / numCells) * 100);
            Tooltip tooltip = new Tooltip("TPM: " + roundToOneDecimal(expression) + "\n" +
                                                "Cells: " + numExpressingCells + "/" + numCells + " (" + percentExpressed + "%)");
            Tooltip.install(node, tooltip);
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
        private Set<Canvas> selection = new HashSet<>();

        public void addSelectedIsoformGraphic(Canvas isoformGraphic) {
            if (!selection.contains(isoformGraphic)) {
                isoformGraphic.setStyle("-fx-effect: dropshadow(one-pass-box, #ffafff, 7, 7, 0, 0);");
                selection.add(isoformGraphic);
            }
        }

        public void removeSelectedIsoformGraphic(Canvas isoformGraphic) {
            if (selection.contains(isoformGraphic)) {
                isoformGraphic.setStyle("-fx-effect: null");
                selection.remove(isoformGraphic);
            }
        }

        public void clearSelectedIsoformGraphics() {
            while (!selection.isEmpty())
                removeSelectedIsoformGraphic(selection.iterator().next());
        }

        public Collection<Canvas> getSelectedIsoformGraphics() {
            return selection;
        }
    }

    private class RectangularSelection {
        private final DragContext dragContext = new DragContext();
        private Rectangle selectionBox;
        private Pane group;
        private HashMap<Canvas, String> selectableIsoformGraphics;

        public RectangularSelection(Pane group) {
            this.group = group;
            selectableIsoformGraphics = new HashMap<>();

            setUpSelectionBox();

            group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
        }

        public void addSelectableIsoformGraphic(Canvas isoformGraphic, String isoformID) {
            selectableIsoformGraphics.put(isoformGraphic, isoformID);
        }

        public void removeSelectableIsoformGraphic(Canvas isoformGraphic) {
            selectableIsoformGraphics.remove(isoformGraphic);
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
                double offsetX = event.getX() - dragContext.mouseAnchorX;
                double offsetY = event.getY() - dragContext.mouseAnchorY;

                if(offsetX > 0)
                    selectionBox.setWidth(offsetX);
                else {
                    selectionBox.setX(event.getX());
                    selectionBox.setWidth(dragContext.mouseAnchorX - selectionBox.getX());
                }

                if(offsetY > 0) {
                    selectionBox.setHeight(offsetY);
                } else {
                    selectionBox.setY(event.getY());
                    selectionBox.setHeight(dragContext.mouseAnchorY - selectionBox.getY());
                }
                event.consume();
            }
        };

        EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!event.isShiftDown() && !event.isControlDown())
                    selectionModel.clearSelectedIsoformGraphics();

                for(Canvas isoformGraphic: selectableIsoformGraphics.keySet()) {
                    if(isoformGraphic.localToScene(isoformGraphic.getBoundsInLocal()).intersects(selectionBox.localToScene(selectionBox.getBoundsInLocal()))) {
                        if(event.isControlDown())
                            selectionModel.removeSelectedIsoformGraphic(isoformGraphic);
                        else
                            selectionModel.addSelectedIsoformGraphic(isoformGraphic);
                    }
                }
                clearRectangularSelection();
                if (!ControllerMediator.getInstance().isTSNEPlotCleared()) {
                    List<String> isoformIDs = selectionModel.getSelectedIsoformGraphics().stream().map(isoformGraphic -> selectableIsoformGraphics.get(isoformGraphic)).collect(Collectors.toList());
                    ControllerMediator.getInstance().selectCellsIsoformsExpressedIn(isoformIDs);
                }
                event.consume();
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
