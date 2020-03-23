package controller;

import annotation.Exon;
import annotation.Gene;
import annotation.Isoform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import mediator.ControllerMediator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class IsoformPlotController implements Initializable, InteractiveElementController {
    private static final Color FONT_COLOUR = Color.BLACK;
    private static final Color DEFAULT_EXON_COLOUR = Color.color(0.929, 0.929, 0.929);
    private static final Color OUTLINE_COLOUR = Color.BLACK;
    private static final Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);
    private static final Font TRANSCRIPT_FONT = Font.font("Verdana",12);
    private static final float GENE_FONT_HEIGHT = getFontHeight(GENE_FONT);
    private static final float ISOFORM_FONT_HEIGHT = getFontHeight(TRANSCRIPT_FONT);
    private static final int CANVAS_MIN_WIDTH = 400;
    private static final int CANVAS_INIT_Y = 0;
    private static final int GENE_ID_X_OFFSET = 0;
    private static final int ISOFORM_X_OFFSET = 13;
    private static final int SCROLLBAR_WIDTH = 20;
    private static final int CANVAS_MARGIN = 15;
    private static final int EXON_HEIGHT = 10;
    private static final int GENE_GENE_SPACING = 5;
    private static final int GENE_ISOFORM_SPACING = 8;
    private static final int ISOFORM_SPACING = 8;

    @FXML private Canvas canvas;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox isoformPlotPanel;
    @FXML private Button selectGenesButton;
    @FXML private Button setTPMGradientButton;

    private GraphicsContext gc;
    private float canvasCurrY;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setUpGraphics();
        setUpScrollPane();
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

    public void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvasCurrY = CANVAS_INIT_Y;
        canvas.setHeight(CANVAS_INIT_Y);
    }

    /**
     * Draws given genes, except genes that don't have isoforms with exon
     * junctions, if supposed to be hiding those
     */
    public void drawGenes(Collection<Gene> genes) {
        boolean isShowingGeneNameAndID = ControllerMediator.getInstance().isShowingGeneNameAndID();
        boolean isShowingGeneName = ControllerMediator.getInstance().isShowingGeneName();
        boolean isShowingIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        boolean isShowingIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        boolean isHidingIsoformsWithNoJunctions = ControllerMediator.getInstance().isHidingIsoformsWithNoJunctions();
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean shouldGetCustomIsoformColor = ControllerMediator.getInstance().areCellsSelected();

        Collection<Gene> genesToDraw = genes;
        if (isHidingIsoformsWithNoJunctions)
            genesToDraw = genesToDraw.stream().filter(Gene::hasIsoformWithJunctions).collect(Collectors.toList());

        clearCanvas();
        incrementCanvasHeight(genesToDraw, isShowingIsoformID, isShowingIsoformName, isHidingIsoformsWithNoJunctions);
        for (Gene gene : genesToDraw) {
            drawGene(gene, isShowingGeneNameAndID, isShowingGeneName, isShowingIsoformID, isShowingIsoformName,
                    isHidingIsoformsWithNoJunctions, reverseComplement, shouldGetCustomIsoformColor);
            canvasCurrY += GENE_GENE_SPACING;
        }
    }

    public Node getIsoformPlot() {
        return isoformPlotPanel;
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

    private void setUpGraphics() {
        gc = canvas.getGraphicsContext2D();
        canvasCurrY = CANVAS_INIT_Y;
        canvas.setHeight(CANVAS_INIT_Y);
    }

    /**
     * Adds listener to resize canvas width when scroll pane width changes
     * (unless scroll pane width < MIN_CANVAS_WIDTH)
     * Redraws canvas when resize occurs and genes are being displayed
     */
    private void setUpScrollPane() {
        scrollPane.widthProperty().addListener((ov, oldValue, newValue) -> {
            double newCanvasWidth = newValue.doubleValue() - (2 * CANVAS_MARGIN + SCROLLBAR_WIDTH);
            if (newCanvasWidth >= CANVAS_MIN_WIDTH) {
                canvas.setWidth(newValue.doubleValue() - (2 * CANVAS_MARGIN + SCROLLBAR_WIDTH));
                drawGenes(ControllerMediator.getInstance().getShownGenes());
            }
        });
    }

    /**
     * Sets canvas height to height necessary to display given genes
     */
    private void incrementCanvasHeight(Collection<Gene> genes, boolean isShowingIsoformID, boolean isShowingIsoformName,
                                       boolean isHidingIsoformsWithNoJunctions) {
        int numGenes = genes.size();
        if (numGenes > 0) {
            double newHeight = canvas.getHeight();
            newHeight += (numGenes - 1) * GENE_GENE_SPACING;
            newHeight += numGenes * (GENE_FONT_HEIGHT + GENE_ISOFORM_SPACING);
            for (Gene gene : genes) {
                Collection<Isoform> isoforms = getIsoformsToDraw(gene, isHidingIsoformsWithNoJunctions);

                int numIsoforms = isoforms.size();
                newHeight += numIsoforms * (2 * ISOFORM_SPACING + EXON_HEIGHT);
                newHeight += getHeightForIsoformLabels(isoforms, isShowingIsoformID, isShowingIsoformName);
            }
            // there is no label below the last isoform
            newHeight -= ISOFORM_SPACING;
            canvas.setHeight(newHeight);
        } else {
            canvas.setHeight(0);
        }
    }

    /**
     * Draws and labels all isoforms of the given gene
     * @param gene gene to draw
     */
    private void drawGene(Gene gene, boolean isShowingGeneNameAndID, boolean isShowingGeneName, boolean isShowingIsoformID,
                          boolean isShowingIsoformName, boolean isHidingIsoformsWithJunctions, boolean reverseComplement, boolean shouldGetCustomIsoformColor) {
        drawGeneLabel(gene, isShowingGeneNameAndID, isShowingGeneName, reverseComplement);
        canvasCurrY += GENE_ISOFORM_SPACING;
        drawAllIsoforms(gene, isShowingIsoformID, isShowingIsoformName, isHidingIsoformsWithJunctions, reverseComplement, shouldGetCustomIsoformColor);
    }

    /**
     * Returns isoforms of given gene that should be drawn (isoforms with no junctions are excluded
     * if are hiding isoforms with no junctions
     */
    private Collection<Isoform> getIsoformsToDraw(Gene gene, boolean isHidingIsoformsWithNoJunctions) {
        Collection<Isoform> isoforms;
        if (isHidingIsoformsWithNoJunctions)
            isoforms = gene.getIsoformsWithJunctions();
        else
            isoforms = gene.getIsoforms();
        return isoforms;
    }

    /**
     * Returns combined height of all the labels of the given isoforms
     */
    private double getHeightForIsoformLabels(Collection<Isoform> isoforms, boolean isShowingIsoformID, boolean isShowingIsoformName) {
        int numIsoforms = isoforms.size();
        double height = 0;

        if (isShowingIsoformID) {
            height += numIsoforms * ISOFORM_FONT_HEIGHT;
        }
        else {
            if (isShowingIsoformName) {
                for (Isoform isoform: isoforms) {
                    if (isoform.getName() != null)
                        height += ISOFORM_FONT_HEIGHT;
                }
            }
        }
        return height;
    }

    /**
     * Draws label for gene
     */
    private void drawGeneLabel(Gene gene, boolean isShowingGeneNameAndID, boolean isShowingGeneName, boolean reverseComplement) {
        canvasCurrY += GENE_FONT_HEIGHT;
        gc.setFill(FONT_COLOUR);
        gc.setFont(GENE_FONT);

        String label = makeGeneLabelText(gene, isShowingGeneNameAndID, isShowingGeneName, reverseComplement);

        gc.fillText(label, GENE_ID_X_OFFSET, canvasCurrY);
    }

    /**
     * Makes gene label text:
     *   - if are showing gene name, label includes gene name
     *   - if are showing gene ID, label includes gene ID
     *   - if are showing gene name and ID, label includes gene ID first, followed
     *     by gene name in brackets
     *   - if are reverse complementing genes on (-) strand, label includes strand gene is on
     */
    private String makeGeneLabelText(Gene gene, boolean isShowingGeneNameAndID, boolean isShowingGeneName, boolean reverseComplement) {
        String label = "";
        String geneID = gene.getId();
        String geneName = gene.getName();

        if (isShowingGeneNameAndID && geneName != null)
            label += geneID + " (" + geneName + ")";
        else if (isShowingGeneName && geneName != null)
            label += geneName;
        else
            label += geneID;

        if (gene.isOnPositiveStrand() && reverseComplement)
            label += " (+)";
        else if (reverseComplement)
            label += " (-)";
        return label;
    }

    /**
     * Labels and draws each isoform of given gene
     */
    private void drawAllIsoforms(Gene gene, boolean isShowingIsoformID, boolean isShowingIsoformName, boolean isHidingIsoformsWithNoJunctions,
                                 boolean reverseComplement, boolean shouldGetCustomIsoformColor) {
        int geneStart = gene.getStartNucleotide();
        int geneEnd = gene.getEndNucleotide();
        double pixelsPerNucleotide = (canvas.getWidth() - ISOFORM_X_OFFSET)/(geneEnd- geneStart + 1);
        Collection<String> isoformsID = gene.getIsoformsMap().keySet();
        List<String> sortedIsoformsIDs = asSortedList(isoformsID);

        for (String isoformID : sortedIsoformsIDs) {
            Isoform isoform = gene.getIsoform(isoformID);
            boolean shouldDrawIsoform = !(isHidingIsoformsWithNoJunctions && !isoform.hasExonJunctions());
            if (shouldDrawIsoform) {
                boolean shouldDrawIsoformLabel = (isShowingIsoformName && isoform.getName() != null) || isShowingIsoformID;
                if (shouldDrawIsoformLabel)
                    drawIsoformLabel(isoform, isoformID, isShowingIsoformID, isShowingIsoformName);
                canvasCurrY += ISOFORM_SPACING;
                Color isoformColor = DEFAULT_EXON_COLOUR;
                if (shouldGetCustomIsoformColor)
                    isoformColor = getCustomIsoformColor(isoformID);
                if(gene.isOnPositiveStrand() || !reverseComplement)
                    drawIsoform(isoform, geneStart, pixelsPerNucleotide, isoformColor);
                else
                    drawIsoformReverseComplement(isoform, geneEnd, pixelsPerNucleotide, isoformColor);
                canvasCurrY += EXON_HEIGHT;
                canvasCurrY += ISOFORM_SPACING;
            }
        }
    }

    /**
     * Draws label for isoforms
     */
    private void drawIsoformLabel(Isoform isoform, String isoformID, boolean isShowingIsoformID, boolean isShowingIsoformName) {
        canvasCurrY += ISOFORM_FONT_HEIGHT;
        String label = makeIsoformLabelText(isoform, isoformID, isShowingIsoformID, isShowingIsoformName);
        gc.setFont(TRANSCRIPT_FONT);
        gc.setFill(FONT_COLOUR);
        gc.fillText(label, ISOFORM_X_OFFSET, canvasCurrY);
    }

    /**
     * Makes isoform label text:
     *   - if are showing isoform name, label includes gene name
     *   - if are showing isoform ID, label includes isoform ID
     *   - if are showing isoform name and isoform ID, label includes isoform ID
     *     first, followed by isoform name in brackets
     *   - if are showing neither, returns an empty string
     */
    private String makeIsoformLabelText(Isoform isoform, String isoformID, boolean isShowingIsoformID, boolean isShowingIsoformName) {
        String label = "";
        String isoformName = isoform.getName();

        if (isShowingIsoformID && isShowingIsoformName && isoformName != null)
            label += isoformID + " (" +isoformName + ")";
        else if (isShowingIsoformName && isoformName != null)
            label += isoformName;
        else
            label += isoformID;

        return label;
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

    /**
     * Draws the exons and introns of the given isoform, without reverse complementing
     */
    private void drawIsoform(Isoform isoform, int geneStart, double pixelsPerNucleotide, Color isoformColor) {
        ArrayList<Exon> exons = isoform.getExons();
        for (int i = 0; i < exons.size(); ++i) {
            drawExon(geneStart, pixelsPerNucleotide, exons, i, isoformColor);
            if (i != 0) {
                drawIntron(geneStart, pixelsPerNucleotide, exons, i);
            }
        }
    }


    /**
     * Draws the exons and introns of the reverse complement of the given isoform
     */
    private void drawIsoformReverseComplement(Isoform isoform, int geneEnd, double pixelsPerNucleotide, Color isoformColor) {
        ArrayList<Exon> exons = isoform.getExons();
        for (int i = 0; i < exons.size(); ++i) {
            drawExonReverseComplement(geneEnd, pixelsPerNucleotide, exons, i, isoformColor);
            if (i != 0) {
                drawIntronReverseComplement(geneEnd, pixelsPerNucleotide, exons, i);
            }
        }
    }

    /**
     * Draws the given exon, without reverse complementing
     */
    private void drawExon(int geneStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i, Color isoformColor) {
        int exonStart = exons.get(i).getStartNucleotide();
        int exonEnd = exons.get(i).getEndNucleotide();
        double startX = (exonStart - geneStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double width = (exonEnd - exonStart + 1) * pixelsPerNucleotide;
        drawExonGraphic(startX, width, isoformColor);
    }

    /**
     * Draws the given intron, without reverse complementing
     */
    private void drawIntron(int geneStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int prevExonEnd = exons.get(i - 1).getEndNucleotide();
        double startX = (prevExonEnd - geneStart + 1) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double endX = (exonStart - geneStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        drawIntronGraphic(startX, endX);
    }

    /**
     * Draws the reverse complement of the given exon
     */
    private void drawExonReverseComplement(int geneEnd, double pixelsPerNucleotide, ArrayList<Exon> exons, int i, Color isoformColor) {
        int exonStart = exons.get(i).getStartNucleotide();
        int exonEnd = exons.get(i).getEndNucleotide();
        double startX = (geneEnd - exonEnd) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double width = (exonEnd - exonStart + 1) * pixelsPerNucleotide;
        drawExonGraphic(startX, width, isoformColor);
    }

    /**
     * Draws the reverse complement of the given intron
     */
    private void drawIntronReverseComplement(int geneEnd, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int prevExonEnd= exons.get(i - 1).getEndNucleotide();
        double startX = (geneEnd - exonStart + 1) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double endX = (geneEnd - prevExonEnd) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        drawIntronGraphic(startX, endX);
    }

    private void drawExonGraphic(double startX, double width, Color isoformColor) {
        gc.setFill(isoformColor);
        gc.fillRect(startX, canvasCurrY, width, EXON_HEIGHT);
        gc.setFill(OUTLINE_COLOUR);
        gc.strokeRect(startX, canvasCurrY, width, EXON_HEIGHT);
    }

    private void drawIntronGraphic(double startX, double endX) {
        double y = canvasCurrY + (double) EXON_HEIGHT / 2;
        gc.setFill(OUTLINE_COLOUR);
        gc.strokeLine(startX, y, endX, y);
    }

    private static float getFontHeight(Font font) {
        Text text = new Text("ABC");
        text.setFont(font);
        return (float) text.getLayoutBounds().getHeight();
    }

    /**
     * Puts given collection into sorted list
     */
    private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }
}
