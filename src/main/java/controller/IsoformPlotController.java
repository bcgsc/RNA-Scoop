package controller;

import annotation.Exon;
import annotation.Gene;
import annotation.Isoform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import mediator.ControllerMediator;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class IsoformPlotController implements Initializable, InteractiveElementController {
    private static final int SCROLLBAR_WIDTH = 16;

    @FXML private VBox isoformPlotPanel;
    @FXML private Button selectGenesButton;
    @FXML private Button setTPMGradientButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Pane isoformPlot;
    @FXML private VBox geneGroups;

    private SelectionModel selectionModel;
    private RectangularSelection rectangularSelection;
    private HashMap<Gene, GeneGroup> geneGeneGroupMap;
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
     * (doesn't add genes without isoforms with junctions if hiding isoforms without junctions)
     */
    public void addGenes(Collection<Gene> genes) {
        boolean showGeneNameAndID = ControllerMediator.getInstance().isShowingGeneNameAndID();
        boolean showGeneName = ControllerMediator.getInstance().isShowingGeneName();
        boolean showIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        boolean showIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        boolean hideIsoformsWithNoJunctions = ControllerMediator.getInstance().isHidingIsoformsWithNoJunctions();
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean shouldGetCustomIsoformColor = ControllerMediator.getInstance().areCellsSelected();
        for (Gene gene : genes) {
            if (!geneGeneGroupMap.containsKey(gene) && (!hideIsoformsWithNoJunctions || gene.hasIsoformWithJunctions())) {
                GeneGroup geneGroup = new GeneGroup(gene, showGeneNameAndID, showGeneName, showIsoformID, showIsoformName,
                                                    hideIsoformsWithNoJunctions, reverseComplement, shouldGetCustomIsoformColor);
                geneGroups.getChildren().add(geneGroup);
                geneGeneGroupMap.put(gene, geneGroup);
            }
        }
    }

    /**
     * Removes all given genes that are in the isoform plot
     */
    public void removeGenes(Collection<Gene> genes) {
        for (Gene gene : genes) {
            if (geneGeneGroupMap.containsKey(gene)) {
                GeneGroup geneGroup = geneGeneGroupMap.get(gene);
                geneGroup.makeUnselectable();
                geneGroups.getChildren().remove(geneGroup);
                geneGeneGroupMap.remove(gene, geneGroup);
            }
        }
    }

    public void redrawIsoformGraphics() {
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean shouldGetIsoformColor = ControllerMediator.getInstance().areCellsSelected();
        for(GeneGroup geneGroup : geneGeneGroupMap.values())
            geneGroup.redrawIsoformGraphics(reverseComplement, shouldGetIsoformColor);
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
        boolean shouldGetCustomIsoformColor = ControllerMediator.getInstance().areCellsSelected();
        boolean showGeneName = ControllerMediator.getInstance().isShowingGeneName();
        for (Gene gene : geneGeneGroupMap.keySet()) {
            GeneGroup geneGroup = geneGeneGroupMap.get(gene);
            geneGroup.updateLabel(showGeneNameAndID, showGeneName, reverseComplement);
            if (!gene.isOnPositiveStrand())
                geneGroup.redrawIsoformGraphics(reverseComplement, shouldGetCustomIsoformColor);
        }
    }

    /**
     * Checks if are currently hiding isoforms without junctions, and hides/shows
     * isoforms accordingly
     */
    public void updateHideIsoformsNoJunctionsStatus() {
        boolean hideIsoformsWithNoJunctions = ControllerMediator.getInstance().isHidingIsoformsWithNoJunctions();
        if (hideIsoformsWithNoJunctions)
            hideIsoformsNoJunctions();
        else
            showIsoformsNoJunctions();
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

    private void hideIsoformsNoJunctions() {
        Collection<Gene> genesToRemove = new ArrayList<>();
        for (Gene gene : geneGeneGroupMap.keySet()) {
            if (!gene.hasIsoformWithJunctions()) {
                genesToRemove.add(gene);
            } else {
                GeneGroup geneGroup = geneGeneGroupMap.get(gene);
                geneGroup.removeIsoformsNoJunctions();
            }
        }
        if (genesToRemove.size() > 0)
            removeGenes(genesToRemove);
    }

    private void showIsoformsNoJunctions() {
        Collection<Gene> genesToAdd = new ArrayList<>();
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean showIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        boolean showIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        boolean shouldGetCustomIsoformColor = ControllerMediator.getInstance().areCellsSelected();
        for (Gene gene : ControllerMediator.getInstance().getShownGenes()) {
            if (!geneGeneGroupMap.containsKey(gene)) {
                genesToAdd.add(gene);
            } else {
                GeneGroup geneGroup = geneGeneGroupMap.get(gene);
                geneGroup.addIsoformsNoJunctions(showIsoformName, showIsoformID, reverseComplement, shouldGetCustomIsoformColor);
            }
        }
        if (genesToAdd.size() > 0)
            addGenes(genesToAdd);
    }

    private void setScrollPaneWidthSpacing() {
        Insets geneGroupsMargin = VBox.getMargin(geneGroups);
        scrollPaneWidthSpacing = geneGroupsMargin.getLeft() + geneGroupsMargin.getRight();
    }

    private void setUpRedrawIsoformsToMatchScrollPaneWidth() {
        scrollPane.widthProperty().addListener((ov, oldValue, newValue) -> redrawIsoformGraphics());
    }

    /**
     * Adds a 10px of space between the groups of genes in the isoform plot
     */
    private void setGeneGroupsStyling() {
        geneGroups.setSpacing(10);
    }

    /**
     * Puts given collection into sorted list
     */
    private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }

    private class GeneGroup extends VBox {
        private final Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);

        private Gene gene;
        private SelectableText label;
        private HashMap<Isoform, IsoformGroup> isoformsIsoformGroupMap;

        public GeneGroup(Gene gene, boolean showGeneNameAndID, boolean showGeneName, boolean showIsoformID,
                         boolean showIsoformName, boolean hideIsoformsWithNoJunctions, boolean reverseComplement,
                         boolean shouldGetCustomIsoformColor) {
            this.gene = gene;
            isoformsIsoformGroupMap = new HashMap<>();
            addLabel(showGeneNameAndID, showGeneName, reverseComplement);
            addIsoformsGroups(showIsoformName, showIsoformID, hideIsoformsWithNoJunctions, reverseComplement, shouldGetCustomIsoformColor);
            setSpacing(7.5);
        }

        public void addIsoform(Isoform isoform, IsoformGroup isoformGroup) {
            isoformsIsoformGroupMap.put(isoform, isoformGroup);
            getChildren().add(isoformGroup);
        }

        public void removeIsoform(Isoform isoform) {
            IsoformGroup isoformGroup = isoformsIsoformGroupMap.get(isoform);
            isoformGroup.makeIsoformGraphicNonSelectable();
            getChildren().remove(isoformGroup);
            isoformsIsoformGroupMap.remove(isoform, isoformGroup);
        }

        public void updateLabel(boolean showGeneNameAndID, boolean showGeneName, boolean reverseComplement) {
            label.setTextAndFitWidthToText(getGeneLabelText(showGeneNameAndID, showGeneName, reverseComplement));
        }

        public void makeUnselectable() {
            for (IsoformGroup isoformGroup : isoformsIsoformGroupMap.values())
                isoformGroup.makeIsoformGraphicNonSelectable();
        }

        public void redrawIsoformGraphics(boolean reverseComplement, boolean shouldGetCustomIsoformColor) {
            int geneStart = gene.getStartNucleotide();
            int geneEnd = gene.getEndNucleotide();
            double pixelsPerNucleotide = getPixelsPerNucleotide(geneStart, geneEnd);
            for (IsoformGroup isoformGroup : isoformsIsoformGroupMap.values())
                isoformGroup.redrawIsoformGraphic(pixelsPerNucleotide, reverseComplement, shouldGetCustomIsoformColor);
        }

        public void updateIsoformLabels(boolean showIsoformName, boolean showIsoformID) {
            for (Isoform isoform : isoformsIsoformGroupMap.keySet()) {
                IsoformGroup isoformGroup = isoformsIsoformGroupMap.get(isoform);
                isoformGroup.updateLabel(showIsoformName, showIsoformID);
            }
        }

        public void removeIsoformsNoJunctions() {
            for (Isoform isoform :  gene.getIsoforms())
                if (!isoform.hasExonJunctions())
                    removeIsoform(isoform);
        }

        public void addIsoformsNoJunctions(boolean showIsoformName, boolean showIsoformID, boolean reverseComplement,
                                           boolean shouldGetCustomIsoformColor) {
            int geneStart = gene.getStartNucleotide();
            int geneEnd = gene.getEndNucleotide();
            double pixelsPerNucleotide = getPixelsPerNucleotide(geneStart, geneEnd);

            for (Isoform isoform : gene.getIsoforms()) {
                if (!isoform.hasExonJunctions()) {
                    IsoformGroup isoformGroup = new IsoformGroup(isoform, pixelsPerNucleotide, showIsoformName, showIsoformID,
                            reverseComplement, shouldGetCustomIsoformColor);
                    addIsoform(isoform, isoformGroup);
                }
            }
        }

        private void addLabel(boolean showGeneNameAndID, boolean showGeneName, boolean reverseComplement) {
            label = new SelectableText();
            label.setFont(GENE_FONT);
            label.setTextAndFitWidthToText(getGeneLabelText(showGeneNameAndID, showGeneName, reverseComplement));
            getChildren().add(label);
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

        private void addIsoformsGroups(boolean showIsoformName, boolean showIsoformID, boolean hideIsoformsWithNoJunctions,
                                       boolean reverseComplement, boolean shoudGetCustomIsoformColor) {
            int geneStart = gene.getStartNucleotide();
            int geneEnd = gene.getEndNucleotide();
            double pixelsPerNucleotide = getPixelsPerNucleotide(geneStart, geneEnd);
            Collection<String> isoformsID = gene.getIsoformsMap().keySet();
            List<String> sortedIsoformsIDs = asSortedList(isoformsID);
            for (String isoformID : sortedIsoformsIDs) {
                Isoform isoform = gene.getIsoform(isoformID);
                if (!hideIsoformsWithNoJunctions || isoform.hasExonJunctions()) {
                    IsoformGroup isoformGroup = new IsoformGroup(isoform, pixelsPerNucleotide, showIsoformName, showIsoformID,
                                                                 reverseComplement, shoudGetCustomIsoformColor);
                    addIsoform(isoform, isoformGroup);
                }
            }
        }

        private double getPixelsPerNucleotide(int geneStart, int geneEnd) {
            return (scrollPane.getWidth() - IsoformGroup.ISOFORM_OFFSET - scrollPaneWidthSpacing - SCROLLBAR_WIDTH - IsoformGroup.ISOFORM_GRAPHIC_SPACING) /
                    (geneEnd - geneStart + 1);
        }
    }

    private class IsoformGroup extends VBox {
        public final Color DEFAULT_EXON_COLOR = Color.color(0.929, 0.929, 0.929);
        public static final int ISOFORM_OFFSET = 10;
        public static final int EXON_HEIGHT = 10;
        public final Color OUTLINE_COLOUR = Color.BLACK;
        // object on which isoform graphic is drawn has a padding of ISOFORM_GRAPHIC_SPACING / 2
        // all around
        public static final int ISOFORM_GRAPHIC_SPACING = 2;
        private final Font ISOFORM_FONT = Font.font("Verdana",12);

        private Isoform isoform;
        private SelectableText label;
        private Canvas isoformGraphic;

        public IsoformGroup(Isoform isoform, double pixelsPerNucleotide, boolean showIsoformName, boolean showIsoformID,
                            boolean reverseComplement, boolean shoudGetCustomIsoformColor) {
            this.isoform = isoform;
            double isoformGraphicWidth = getIsoformGraphicWidth(pixelsPerNucleotide, isoform);
            isoformGraphic = new Canvas(isoformGraphicWidth, EXON_HEIGHT + ISOFORM_GRAPHIC_SPACING);
            getChildren().add(isoformGraphic);
            rectangularSelection.addSelectableIsoformGraphic(isoformGraphic, isoform.getId());
            drawIsoformGraphic(pixelsPerNucleotide, reverseComplement, shoudGetCustomIsoformColor);
            if (shouldHaveLabel(showIsoformName, showIsoformID))
                createLabel(getIsoformLabelText(showIsoformName, showIsoformID));
            setSpacing(5);
        }

        public void updateLabel(boolean showIsoformName, boolean showIsoformID) {
            if (shouldHaveLabel(showIsoformName, showIsoformID)) {
                String isoformLabelText = getIsoformLabelText(showIsoformName, showIsoformID);
                if (label == null)
                    createLabel(isoformLabelText);
                else
                    label.setTextAndFitWidthToText(isoformLabelText);
            } else {
                removeLabel();
            }
        }

        public void removeLabel() {
            if (label != null) {
                label = null;
                getChildren().remove(0);
            }
        }

        public void makeIsoformGraphicNonSelectable() {
            selectionModel.removeSelectedIsoformGraphic(isoformGraphic);
            rectangularSelection.removeSelectableIsoformGraphic(isoformGraphic);
        }

        public void redrawIsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean shouldGetCustomIsoformColor) {
            setIsoformGraphicWidth(pixelsPerNucleotide);
            clearIsoformGraphic(isoformGraphic);
            drawIsoformGraphic(pixelsPerNucleotide, reverseComplement, shouldGetCustomIsoformColor);
        }

        private boolean shouldHaveLabel(boolean showIsoformName, boolean showIsoformID) {
            return (showIsoformName && isoform.getName() != null)|| showIsoformID;
        }

        private void createLabel(String newLabel) {
            label = new SelectableText();
            label.setFont(ISOFORM_FONT);
            VBox.setMargin(label, new Insets(0, 0, 0, ISOFORM_OFFSET));
            getChildren().add(0, label);
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

        private void setIsoformGraphicWidth(double pixelsPerNucleotide) {
            double isoformGraphicWidth = getIsoformGraphicWidth(pixelsPerNucleotide, isoform);
            isoformGraphic.setWidth(isoformGraphicWidth);
        }

        private void clearIsoformGraphic(Canvas isoformGraphic) {
            GraphicsContext graphicsContext = isoformGraphic.getGraphicsContext2D();
            graphicsContext.clearRect(0, 0, isoformGraphic.getWidth(), isoformGraphic.getHeight());
        }

        private double getIsoformGraphicWidth(double pixelsPerNucleotide, Isoform isoform) {
            double isoformWidth = (isoform.getEndNucleotide() - isoform.getStartNucleotide() + 1) * pixelsPerNucleotide;
            return isoformWidth + ISOFORM_GRAPHIC_SPACING;
        }


        private void drawIsoformGraphic(double pixelsPerNucleotide, boolean reverseComplement, boolean shouldGetCustomIsoformColor) {
            Color isoformColor = getIsoformColor(shouldGetCustomIsoformColor);
            ArrayList<Exon> exons = isoform.getExons();
            Gene gene = isoform.getGene();
            GraphicsContext graphicsContext = isoformGraphic.getGraphicsContext2D();

            if(gene.isOnPositiveStrand() || !reverseComplement) {
                int isoformStart = isoform.getStartNucleotide();
                int geneStart = gene.getStartNucleotide();

                double isoformExtraOffset = (isoformStart - geneStart) * pixelsPerNucleotide;
                VBox.setMargin(isoformGraphic, new Insets(0, 0, 0, ISOFORM_OFFSET + isoformExtraOffset));
                drawIsoform(pixelsPerNucleotide, isoformColor, exons, isoformStart, graphicsContext);
            } else {
                int isoformEnd = isoform.getEndNucleotide();
                int geneEnd = gene.getEndNucleotide();

                double isoformExtraOffset = (geneEnd - isoformEnd) * pixelsPerNucleotide;
                VBox.setMargin(isoformGraphic, new Insets(0, 0, 0, ISOFORM_OFFSET + isoformExtraOffset));
                drawIsoformReverseComplement(pixelsPerNucleotide, isoformColor, exons, isoformEnd, graphicsContext);
            }
        }

        private Color getIsoformColor(boolean shouldGetCustomIsoformColor) {
            Color isoformColor = DEFAULT_EXON_COLOR;
            if (shouldGetCustomIsoformColor)
                isoformColor = getCustomIsoformColor();
            return isoformColor;
        }

        /**
         * Returns custom isoform color for isoform associated with this isoform group based on the isoform's
         * expression level and where is lies in the TPM gradient
         */
        private Color getCustomIsoformColor() {
            double isoformExpression = ControllerMediator.getInstance().getIsoformExpressionLevel(isoform.getId());
            double minTPM = ControllerMediator.getInstance().getGradientMinTPM();
            double maxTPM = ControllerMediator.getInstance().getGradientMaxTPM();
            Color minTPMColor = ControllerMediator.getInstance().getMinTPMColor();
            Color maxTPMColor = ControllerMediator.getInstance().getMaxTPMColor();
            String scale = ControllerMediator.getInstance().getScale();

            if (scale.equals(TPMGradientAdjusterController.SCALE_CHOOSER_LINEAR_OPTION))
                return getLinearScaleColor(isoformExpression, minTPM, maxTPM, minTPMColor, maxTPMColor);
            else
                return getLogarithmicScaleColor(isoformExpression, minTPM, maxTPM, minTPMColor, maxTPMColor);
        }

        /**
         * Gets color for isoform with given expression based on TPM gradient using a linear scale
         */
        private Color getLinearScaleColor(double isoformExpression, double minTPM, double maxTPM, Color minTPMColor, Color maxTPMColor) {
            if (isoformExpression <= minTPM)
                return minTPMColor;
            else if (isoformExpression >= maxTPM)
                return maxTPMColor;
            else {
                double t = (isoformExpression - minTPM) / (maxTPM - minTPM);
                return minTPMColor.interpolate(maxTPMColor, t);
            }
        }

        /**
         * Gets color for isoform with given expression based on TPM gradient using a logarithmic scale
         */
        private Color getLogarithmicScaleColor(double isoformExpression, double minTPM, double maxTPM, Color minTPMColor, Color maxTPMColor) {
            if (isoformExpression <= minTPM)
                return minTPMColor;
            else if (isoformExpression >= maxTPM)
                return  maxTPMColor;
            else {
                double logIsoformExpression = Math.log10(isoformExpression + Double.MIN_VALUE);
                double logMinTPM = Math.log10(minTPM + Double.MIN_VALUE);
                double logMaxTPM = Math.log10(maxTPM + Double.MIN_VALUE);
                double t = (logIsoformExpression- logMinTPM)/(logMaxTPM - logMinTPM);
                return minTPMColor.interpolate(maxTPMColor, t);
            }
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

        /**
         * Draws the given exon, without reverse complementing
         */
        private void drawExon(int isoformStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i, Color isoformColor,
                              GraphicsContext graphicsContext) {
            int exonStart = exons.get(i).getStartNucleotide();
            int exonEnd = exons.get(i).getEndNucleotide();
            double startX = (exonStart - isoformStart) * pixelsPerNucleotide + ISOFORM_GRAPHIC_SPACING / 2;
            double width = (exonEnd - exonStart + 1) * pixelsPerNucleotide;
            drawExonGraphic(startX, width, isoformColor, graphicsContext);
        }

        /**
         * Draws the given intron, without reverse complementing
         */
        private void drawIntron(int isoformStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i,
                                GraphicsContext graphicsContext) {
            int exonStart = exons.get(i).getStartNucleotide() ;
            int prevExonEnd = exons.get(i - 1).getEndNucleotide();
            double startX = (prevExonEnd - isoformStart + 1) * pixelsPerNucleotide + ISOFORM_GRAPHIC_SPACING / 2;
            double endX = (exonStart - isoformStart) * pixelsPerNucleotide + ISOFORM_GRAPHIC_SPACING / 2;
            drawIntronGraphic(startX, endX, graphicsContext);
        }

        private void drawExonReverseComplement(int isoformEnd, double pixelsPerNucleotide, ArrayList<Exon> exons, int i, Color isoformColor,
                                               GraphicsContext graphicsContext) {
            int exonStart = exons.get(i).getStartNucleotide();
            int exonEnd = exons.get(i).getEndNucleotide();
            double startX = (isoformEnd - exonEnd) * pixelsPerNucleotide + ISOFORM_GRAPHIC_SPACING / 2;
            double width = (exonEnd - exonStart + 1) * pixelsPerNucleotide;
            drawExonGraphic(startX, width, isoformColor, graphicsContext);
        }

        /**
         * Draws the given intron, without reverse complementing
         */
        private void drawIntronReverseComplement(int isoformEnd, double pixelsPerNucleotide, ArrayList<Exon> exons, int i,
                                                 GraphicsContext graphicsContext) {
            int exonStart = exons.get(i).getStartNucleotide() ;
            int prevExonEnd = exons.get(i - 1).getEndNucleotide();
            double startX = (isoformEnd - exonStart + 1) * pixelsPerNucleotide + ISOFORM_GRAPHIC_SPACING / 2;
            double endX = (isoformEnd - prevExonEnd) * pixelsPerNucleotide + ISOFORM_GRAPHIC_SPACING / 2;
            drawIntronGraphic(startX, endX, graphicsContext);
        }

        private void drawExonGraphic(double startX, double width, Color isoformColor, GraphicsContext graphicsContext) {
            graphicsContext.setFill(isoformColor);
            graphicsContext.fillRect(startX, ISOFORM_GRAPHIC_SPACING / 2, width, EXON_HEIGHT);
            graphicsContext.setFill(OUTLINE_COLOUR);
            graphicsContext.strokeRect(startX, ISOFORM_GRAPHIC_SPACING / 2, width, EXON_HEIGHT);
        }

        private void drawIntronGraphic(double startX, double endX, GraphicsContext graphicsContext) {
            graphicsContext.setFill(OUTLINE_COLOUR);
            graphicsContext.strokeLine(startX, EXON_HEIGHT / 2, endX, EXON_HEIGHT / 2);
        }
    }

    private class SelectableText extends TextField {

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
