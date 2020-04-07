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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
    private static final Color DEFAULT_EXON_COLOR = Color.color(0.929, 0.929, 0.929);
    private static final Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);
    private static final Font ISOFORM_FONT = Font.font("Verdana",12);
    private static final int ISOFORM_OFFSET = 10;
    private static final int EXON_HEIGHT = 10;
    private static final Color OUTLINE_COLOUR = Color.BLACK;
    // object on which isoform graphic is drawn has a padding of ISOFORM_GRAPHIC_SPACING / 2
    // all around
    private static final int ISOFORM_GRAPHIC_SPACING = 2;
    private static final int SCROLLBAR_WIDTH = 16;

    @FXML private VBox isoformPlotPanel;
    @FXML private Button selectGenesButton;
    @FXML private Button setTPMGradientButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Pane isoformPlot;
    @FXML private VBox geneGroups;

    private SelectionModel selectionModel;
    private RectangularSelection rectangularSelection;
    private HashMap<Gene, GeneGroup> genesGeneGroupMap;
    private double scrollPaneWidthSpacing;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectionModel = new SelectionModel();
        genesGeneGroupMap = new HashMap<>();
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
     * (doesn't add genes without isoforms with junctions if hiding isoforms without jiunctions)
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
            if (!genesGeneGroupMap.containsKey(gene) && (!hideIsoformsWithNoJunctions || gene.hasIsoformWithJunctions())) {
                GeneGroup geneGroup = makeGeneGroup(gene, showGeneNameAndID, showGeneName, showIsoformID, showIsoformName,
                                                    hideIsoformsWithNoJunctions, reverseComplement, shouldGetCustomIsoformColor);
                geneGroups.getChildren().add(geneGroup);
                genesGeneGroupMap.put(gene, geneGroup);
            }
        }
    }

    /**
     * Removes all given genes that are in the isoform plot
     */
    public void removeGenes(Collection<Gene> genes) {
        for (Gene gene : genes) {
            if (genesGeneGroupMap.containsKey(gene)) {
                GeneGroup geneGroup = genesGeneGroupMap.get(gene);
                for (IsoformGroup isoformGroup : geneGroup.getIsoformGroups()) {
                    makeIsoformGraphicNonSelectable(isoformGroup.getIsoformGraphic());
                }
                geneGroups.getChildren().remove(geneGroup);
                genesGeneGroupMap.remove(gene, geneGroup);
            }
        }
    }

    public void redrawIsoformGraphics() {
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean shouldGetIsoformColor = ControllerMediator.getInstance().areCellsSelected();
        for(Gene gene : genesGeneGroupMap.keySet()) {
            GeneGroup geneGroup = genesGeneGroupMap.get(gene);
            redrawGeneIsoformGraphics(reverseComplement, shouldGetIsoformColor, gene, geneGroup);
        }
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
        for (Gene gene : genesGeneGroupMap.keySet()) {
            GeneGroup geneGroup = genesGeneGroupMap.get(gene);
            geneGroup.changeLabel(getGeneLabelText(gene, showGeneNameAndID, showGeneName, reverseComplement));
            if (!gene.isOnPositiveStrand()) {
                redrawGeneIsoformGraphics(reverseComplement, shouldGetCustomIsoformColor, gene, geneGroup);
            }
        }
    }

    /**
     * Checks if are currently hiding isoforms without junctions, and removes/adds
     * isoforms accordingly
     */
    public void updateHideIsoformsNoJunctionsStatus() {
        boolean hideIsoformsWithNoJunctions = ControllerMediator.getInstance().isHidingIsoformsWithNoJunctions();
        if (hideIsoformsWithNoJunctions)
            removeIsoformsNoJunctions();
        else
            addIsoformsNoJunctions();
    }

    /**
     * Updates gene labels according to current gene label settings
     */
    public void updateGeneLabels() {
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean showGeneNameAndID = ControllerMediator.getInstance().isShowingGeneNameAndID();
        boolean showGeneName = ControllerMediator.getInstance().isShowingGeneName();
        for (Gene gene : genesGeneGroupMap.keySet()) {
            GeneGroup geneGroup = genesGeneGroupMap.get(gene);
            geneGroup.changeLabel(getGeneLabelText(gene, showGeneNameAndID, showGeneName, reverseComplement));
        }
    }

    /**
     * Updates isoform labels according to current isoform label settings
     */
    public void updateIsoformLabels() {
        boolean showIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        boolean showIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        for (Gene gene : genesGeneGroupMap.keySet()) {
            GeneGroup geneGroup = genesGeneGroupMap.get(gene);
            for (Isoform isoform : geneGroup.getIsoforms()) {
                IsoformGroup isoformGroup = geneGroup.getIsoformGroup(isoform);
                if ((showIsoformName && isoform.getName() != null) || showIsoformID) {
                    isoformGroup.changeLabel(getIsoformLabelText(isoform, showIsoformName, showIsoformID));
                } else {
                    isoformGroup.removeLabel();
                }
            }
        }
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

    private GeneGroup makeGeneGroup(Gene gene, boolean showGeneNameAndID, boolean showGeneName, boolean showIsoformID,
                                    boolean showIsoformName, boolean hideIsoformsWithNoJunctions, boolean reverseComplement,
                                    boolean shouldGetCustomIsoformColor) {
        GeneGroup geneGroup = new GeneGroup(getGeneLabelText(gene, showGeneNameAndID, showGeneName, reverseComplement));
        addIsoformsGroups(gene, geneGroup, showIsoformName, showIsoformID, hideIsoformsWithNoJunctions, reverseComplement, shouldGetCustomIsoformColor);
        geneGroup.setSpacing(7.5);
        return geneGroup;
    }

    private void removeIsoformsNoJunctions() {
        Collection<Gene> genesToRemove = new ArrayList<>();
        for (Gene gene : genesGeneGroupMap.keySet()) {
            if (!gene.hasIsoformWithJunctions()) {
                genesToRemove.add(gene);
            } else {
                GeneGroup geneGroup = genesGeneGroupMap.get(gene);
                for (Isoform isoform : gene.getIsoforms()) {
                    if (!isoform.hasExonJunctions()) {
                        IsoformGroup isoformGroup = geneGroup.getIsoformGroup(isoform);
                        makeIsoformGraphicNonSelectable(isoformGroup.getIsoformGraphic());
                        geneGroup.removeIsoform(isoform);
                    }
                }
            }
        }
        if (genesToRemove.size() > 0)
            removeGenes(genesToRemove);
    }

    private void addIsoformsNoJunctions() {
        Collection<Gene> genesToAdd = new ArrayList<>();
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean showIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        boolean showIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        boolean shouldGetCustomIsoformColor = ControllerMediator.getInstance().areCellsSelected();
        for (Gene gene : ControllerMediator.getInstance().getShownGenes()) {
            if (!genesGeneGroupMap.containsKey(gene)) {
                genesToAdd.add(gene);
            } else {
                int geneStart = gene.getStartNucleotide();
                boolean isOnPositiveStrand = gene.isOnPositiveStrand();
                int geneEnd = gene.getEndNucleotide();
                double pixelsPerNucleotide = getPixelsPerNucleotide(geneStart, geneEnd);
                GeneGroup geneGroup = genesGeneGroupMap.get(gene);
                for (Isoform isoform : gene.getIsoforms()) {
                    if (!isoform.hasExonJunctions()) {
                        Color isoformColor = DEFAULT_EXON_COLOR;
                        if (shouldGetCustomIsoformColor)
                            isoformColor = getCustomIsoformColor(isoform.getId());
                        IsoformGroup isoformGroup = makeIsoformGroup(showIsoformName, showIsoformID, reverseComplement,
                                geneStart, isOnPositiveStrand, geneEnd, pixelsPerNucleotide, isoformColor, isoform);
                        geneGroup.addIsoform(isoform, isoformGroup);
                    }
                }
            }
        }
        if (genesToAdd.size() > 0)
            addGenes(genesToAdd);
    }

    private String getGeneLabelText(Gene gene, boolean showGeneNameAndID, boolean showGeneName, boolean reverseComplement) {
        String labelText = "";
        if (showGeneNameAndID)
            labelText += gene.getId() + " (" + gene.getName() + ")";
        else if (showGeneName)
            labelText +=gene.getName();
        else
            labelText += gene.getId();

        if (reverseComplement) {
            if (gene.isOnPositiveStrand())
                labelText += " (+)";
            else
                labelText += " (-)";
        }

        return labelText;
    }

    private void addIsoformsGroups(Gene gene, GeneGroup geneGroup, boolean showIsoformName, boolean showIsoformID,
                                   boolean hideIsoformsWithNoJunctions, boolean reverseComplement, boolean shoudGetCustomIsoformColor) {
        int geneStart = gene.getStartNucleotide();
        boolean isOnPositiveStrand = gene.isOnPositiveStrand();
        int geneEnd = gene.getEndNucleotide();
        double pixelsPerNucleotide = getPixelsPerNucleotide(geneStart, geneEnd);
        Collection<String> isoformsID = gene.getIsoformsMap().keySet();
        List<String> sortedIsoformsIDs = asSortedList(isoformsID);
        for (String isoformID : sortedIsoformsIDs) {
            Isoform isoform = gene.getIsoform(isoformID);
            Color isoformColor = DEFAULT_EXON_COLOR;
            if (shoudGetCustomIsoformColor)
                isoformColor = getCustomIsoformColor(isoformID);
            if (!hideIsoformsWithNoJunctions || (hideIsoformsWithNoJunctions && isoform.hasExonJunctions())) {
                IsoformGroup isoformGroup = makeIsoformGroup(showIsoformName, showIsoformID, reverseComplement, geneStart,
                                                             isOnPositiveStrand, geneEnd, pixelsPerNucleotide, isoformColor, isoform);
                geneGroup.addIsoform(isoform, isoformGroup);
            }
        }
    }

    private void makeIsoformGraphicNonSelectable(Canvas isoformGraphic) {
        selectionModel.removeSelectedIsoformGraphic(isoformGraphic);
        rectangularSelection.removeSelectableIsoformGraphic(isoformGraphic);
    }

    /**
     * Returns custom isoform color for isoform with given ID based on the isoform's expression
     * level and where is lies in the TPM gradient
     */
    private Color getCustomIsoformColor(String isoformID) {
        double isoformExpression = ControllerMediator.getInstance().getIsoformExpressionLevel(isoformID);
        double minTPM = ControllerMediator.getInstance().getGradientMinTPM();
        double maxTPM = ControllerMediator.getInstance().getGradientMaxTPM();
        Color minTPMColor = ControllerMediator.getInstance().getMinTPMColor();
        Color maxTPMColor = ControllerMediator.getInstance().getMaxTPMColor();
        String scale = ControllerMediator.getInstance().getScale();

        if (scale.equals(TPMGradientAdjusterController.SCALE_CHOOSER_LINEAR_OPTION))
            return getLinearScaleColor(isoformExpression, minTPM, maxTPM, minTPMColor, maxTPMColor);
        else
            return getExponentialScaleColor(isoformExpression, minTPM, maxTPM, minTPMColor, maxTPMColor);
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
     * Gets color for isoform with given expression based on TPM gradient using an exponential scale
     */
    private Color getExponentialScaleColor(double isoformExpression, double minTPM, double maxTPM, Color minTPMColor, Color maxTPMColor) {
        if (isoformExpression <= minTPM)
            return minTPMColor;
        else if (isoformExpression >= maxTPM)
            return  maxTPMColor;
        else {
            double linearT = (isoformExpression - minTPM) / (maxTPM - minTPM);
            double t = Math.log10(linearT * 100D + 1D - linearT)/Math.log10(100D);
            return minTPMColor.interpolate(maxTPMColor, t);
        }
    }

    private double getPixelsPerNucleotide(int geneStart, int geneEnd) {
        return (scrollPane.getWidth() - ISOFORM_OFFSET - scrollPaneWidthSpacing - SCROLLBAR_WIDTH - ISOFORM_GRAPHIC_SPACING) /
                (geneEnd - geneStart + 1);
    }

    private IsoformGroup makeIsoformGroup(boolean showIsoformName, boolean showIsoformID, boolean reverseComplement, int geneStart,
                                          boolean isOnPositiveStrand, int geneEnd, double pixelsPerNucleotide, Color isoformColor, Isoform isoform) {
        double isoformGraphicWidth = getIsoformGraphicWidth(pixelsPerNucleotide, isoform);
        Canvas isoformGraphic = new Canvas(isoformGraphicWidth, EXON_HEIGHT + ISOFORM_GRAPHIC_SPACING);
        drawIsoformGraphic(isoform, geneStart, geneEnd, isOnPositiveStrand, pixelsPerNucleotide, isoformColor, reverseComplement, isoformGraphic);
        IsoformGroup isoformGroup = new IsoformGroup(isoformGraphic);
        if ((showIsoformName && isoform.getName() != null)|| showIsoformID)
            isoformGroup.changeLabel(getIsoformLabelText(isoform, showIsoformName, showIsoformID));
        isoformGroup.setSpacing(5);
        return isoformGroup;
    }

    private String getIsoformLabelText(Isoform isoform, boolean showIsoformName, boolean showIsoformID) {
        if (showIsoformName && showIsoformID)
            return isoform.getId() + " (" + isoform.getName() + ")";
        else if (showIsoformName)
            return isoform.getName();
        else if (showIsoformID)
            return isoform.getId();
        else
            return "";
    }

    private void redrawGeneIsoformGraphics(boolean reverseComplement, boolean shouldGetCustomIsoformColor, Gene gene, GeneGroup geneGroup) {
        int geneStart = gene.getStartNucleotide();
        int geneEnd = gene.getEndNucleotide();
        double pixelsPerNucleotide = getPixelsPerNucleotide(geneStart, geneEnd);
        for (Isoform isoform : geneGroup.getIsoforms()) {
            Canvas isoformGraphic = geneGroup.getIsoformGroup(isoform).getIsoformGraphic();
            double isoformGraphicWidth = getIsoformGraphicWidth(pixelsPerNucleotide, isoform);
            isoformGraphic.setWidth(isoformGraphicWidth);
            clearIsoformGraphic(isoformGraphic);
            Color isoformColor = DEFAULT_EXON_COLOR;
            if (shouldGetCustomIsoformColor)
                isoformColor = getCustomIsoformColor(isoform.getId());
            drawIsoformGraphic(isoform, geneStart, geneEnd, gene.isOnPositiveStrand(), pixelsPerNucleotide, isoformColor, reverseComplement, isoformGraphic);
        }
    }

    private Canvas drawIsoformGraphic(Isoform isoform, int geneStart, int geneEnd, boolean isOnPositiveStrand, double pixelsPerNucleotide, Color isoformColor,
                                      boolean reverseComplement, Canvas isoformGraphic) {
        ArrayList<Exon> exons = isoform.getExons();
        int isoformStart = isoform.getStartNucleotide();
        int isoformEnd = isoform.getEndNucleotide();
        rectangularSelection.addSelectableIsoformGraphic(isoformGraphic, isoform.getId());
        GraphicsContext graphicsContext = isoformGraphic.getGraphicsContext2D();
        if(isOnPositiveStrand || !reverseComplement) {
            double isoformExtraOffset = (isoformStart - geneStart) * pixelsPerNucleotide;
            VBox.setMargin(isoformGraphic, new Insets(0, 0, 0, ISOFORM_OFFSET + isoformExtraOffset));
            drawIsoform(pixelsPerNucleotide, isoformColor, exons, isoformStart, graphicsContext);
        } else {
            double isoformExtraOffset = (geneEnd - isoformEnd) * pixelsPerNucleotide;
            VBox.setMargin(isoformGraphic, new Insets(0, 0, 0, ISOFORM_OFFSET + isoformExtraOffset));
            drawIsoformReverseComplement(pixelsPerNucleotide, isoformColor, exons, isoformEnd, graphicsContext);
        }
        return isoformGraphic;
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

    private double getIsoformGraphicWidth(double pixelsPerNucleotide, Isoform isoform) {
        double isoformWidth = (isoform.getEndNucleotide() - isoform.getStartNucleotide() + 1) * pixelsPerNucleotide;
        return isoformWidth + ISOFORM_GRAPHIC_SPACING;
    }

    private void clearIsoformGraphic(Canvas isoformGraphic) {
        GraphicsContext graphicsContext = isoformGraphic.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, isoformGraphic.getWidth(), isoformGraphic.getHeight());
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
        private Text label;
        private HashMap<Isoform, IsoformGroup> isoformsIsoformGroupMap;

        public GeneGroup(String labelText) {
            label = new Text();
            label.setFont(GENE_FONT);
            label.setText(labelText);
            getChildren().add(label);
            isoformsIsoformGroupMap = new HashMap<>();
        }

        public void addIsoform(Isoform isoform, IsoformGroup isoformGroup) {
            isoformsIsoformGroupMap.put(isoform, isoformGroup);
            getChildren().add(isoformGroup);
        }

        public void removeIsoform(Isoform isoform) {
            IsoformGroup isoformGroup = isoformsIsoformGroupMap.get(isoform);
            getChildren().remove(isoformGroup);
            isoformsIsoformGroupMap.remove(isoform, isoformGroup);
        }

        public void changeLabel(String newLabel) {
            label.setText(newLabel);
        }

        public Collection<Isoform> getIsoforms() {
            return isoformsIsoformGroupMap.keySet();
        }

        public Collection<IsoformGroup> getIsoformGroups() {
            return isoformsIsoformGroupMap.values();
        }

        public IsoformGroup getIsoformGroup(Isoform isoform) {
            return isoformsIsoformGroupMap.get(isoform);
        }
    }

    private class IsoformGroup extends VBox {
        private Text label;
        private Canvas isoformGraphic;

        public IsoformGroup(Canvas isoformGraphic) {
            this.label = null;
            this.isoformGraphic = isoformGraphic;
            getChildren().add(isoformGraphic);
        }

        public void changeLabel(String newLabelText) {
            if (label == null)
                createLabel(newLabelText);
            else
                label.setText(newLabelText);
        }

        public void removeLabel() {
            if (label != null) {
                label = null;
                getChildren().remove(0);
            }
        }

        public Canvas getIsoformGraphic() {
            return isoformGraphic;
        }

        private void createLabel(String newLabel) {
            label = new Text();
            label.setFont(ISOFORM_FONT);
            VBox.setMargin(label, new Insets(0, 0, 0, ISOFORM_OFFSET));
            getChildren().add(0, label);
            label.setText(newLabel);
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
