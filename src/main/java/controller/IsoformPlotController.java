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

public class IsoformPlotController implements Initializable, InteractiveElementController {
    private static final Color FONT_COLOUR = Color.BLACK;
    private static final Color EXON_COLOUR = Color.color(0.600, 0.851, 1);
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
    @FXML private VBox isoformPlot;
    @FXML private Button selectGenesButton;

    private GraphicsContext gc;
    private float canvasCurrY;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeGraphics();
        initializeScrollPane();
    }

    public void disable() {
        selectGenesButton.setDisable(true);
    }

    public void enable() {
        selectGenesButton.setDisable(false);
    }

    public void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvasCurrY = CANVAS_INIT_Y;
        canvas.setHeight(CANVAS_INIT_Y);
    }

    /**
     * Draws given genes
     */
    public void drawGenes(Collection<Gene> genes) {
        clearCanvas();
        incrementCanvasHeight(genes);
        for (Gene gene : genes) {
            drawGene(gene);
            canvasCurrY += GENE_GENE_SPACING;
        }
    }

    public Node getIsoformPlot() {
        return isoformPlot;
    }

    /**
     * Opens up gene selector window
     */
    @FXML
    protected void handleSelectGenesButtonAction() {
        ControllerMediator.getInstance().displayGeneSelector();
    }

    private void initializeGraphics() {
        gc = canvas.getGraphicsContext2D();
        canvasCurrY = CANVAS_INIT_Y;
        canvas.setHeight(CANVAS_INIT_Y);
    }

    /**
     * Adds listener to resize canvas width when scroll pane width changes
     * (unless scroll pane width < MIN_CANVAS_WIDTH)
     * Redraws canvas when resize occurs and gene is being displayed
     */
    private void initializeScrollPane() {
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
    private void incrementCanvasHeight(Collection<Gene> genes) {
        int numGenes = genes.size();
        if (numGenes > 0) {
            double newHeight = canvas.getHeight();
            newHeight += (numGenes - 1) * GENE_GENE_SPACING;
            newHeight += numGenes * (GENE_FONT_HEIGHT + GENE_ISOFORM_SPACING);
            for (Gene gene : genes) {
                Collection<Isoform> isoforms = gene.getIsoforms().values();
                int numIsoforms = isoforms.size();
                newHeight += numIsoforms * (2 * ISOFORM_SPACING + EXON_HEIGHT);
                if (ControllerMediator.getInstance().isShowingIsoformID() || ControllerMediator.getInstance().isShowingGeneNameAndID()) {
                    newHeight += numIsoforms * ISOFORM_FONT_HEIGHT;
                }
                else if (ControllerMediator.getInstance().isShowingIsoformName()) {
                    for (Isoform isoform: isoforms) {
                        if (isoform.getName() != null)
                            newHeight += ISOFORM_FONT_HEIGHT;
                    }
                }
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
    private void drawGene(Gene gene) {
        drawGeneLabel(gene);
        canvasCurrY += GENE_ISOFORM_SPACING;
        drawAllIsoforms(gene);
    }

    /**
     * Draws label for gene
     */
    private void drawGeneLabel(Gene gene) {
        canvasCurrY += GENE_FONT_HEIGHT;
        gc.setFill(FONT_COLOUR);
        gc.setFont(GENE_FONT);

        String label = makeGeneLabelText(gene);

        gc.fillText(label, GENE_ID_X_OFFSET, canvasCurrY);
    }

    /**
     * Makes gene label text:
     *   - if show gene name toggle is selected, label includes gene name
     *   - if show gene ID toggle is selected, label includes gene ID
     *   - if show gene name and ID toggle is selected, label includes gene ID first, followed
     *     by gene name in brackets
     *   - if reverse complement toggle is selected, label includes strand gene is on
     */
    private String makeGeneLabelText(Gene gene) {
        String label = "";
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        boolean showingGeneNameAndID = ControllerMediator.getInstance().isShowingGeneNameAndID();
        boolean showingGeneName = ControllerMediator.getInstance().isShowingGeneName();
        String geneID = gene.getId();
        String geneName = gene.getName();

        if (showingGeneNameAndID && geneName != null)
            label += geneID + " (" + geneName + ")";
        else if (showingGeneName && geneName != null)
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
    private void drawAllIsoforms(Gene gene) {
        int geneStart = gene.getStartNucleotide();
        int geneEnd = gene.getEndNucleotide();
        double pixelsPerNucleotide = (canvas.getWidth() - ISOFORM_X_OFFSET)/(geneEnd- geneStart + 1);
        boolean showingIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        boolean showingIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        Collection<String> isoformsID = gene.getIsoforms().keySet();
        List<String> sortedIsoformsIDs = asSortedList(isoformsID);
        boolean reverseComplement = ControllerMediator.getInstance().isReverseComplementing();
        for (String isoformID : sortedIsoformsIDs) {
            Isoform isoform = gene.getIsoform(isoformID);
            if ((showingIsoformName && isoform.getName() != null)|| showingIsoformID)
                drawIsoformLabel(isoform, isoformID);
            canvasCurrY += ISOFORM_SPACING;
            if(gene.isOnPositiveStrand() || !reverseComplement)
                drawIsoform(isoform, geneStart, pixelsPerNucleotide);
            else
                drawIsoformReverseComplement(isoform, geneEnd, pixelsPerNucleotide);
            canvasCurrY += EXON_HEIGHT;
            canvasCurrY += ISOFORM_SPACING;
        }
    }

    /**
     * Draws label for isoforms
     */
    private void drawIsoformLabel(Isoform isoform, String isoformID) {
        canvasCurrY += ISOFORM_FONT_HEIGHT;
        String label = makeIsoformLabelText(isoform, isoformID);
        gc.setFont(TRANSCRIPT_FONT);
        gc.setFill(FONT_COLOUR);
        gc.fillText(label, ISOFORM_X_OFFSET, canvasCurrY);
    }

    /**
     * Makes isoform label text:
     *   - if show isoform name toggle is selected, label includes gene name
     *   - if show isoform ID toggle is selected, label includes gene ID
     *   - if show isoform name and show isoform ID toggles are selected, label includes isoform ID
     *     first, followed by isoform name in brackets
     *   - if neither toggle is selected, returns an empty string
     */
    private String makeIsoformLabelText(Isoform isoform, String isoformID) {
        String label = "";
        boolean showingIsoformID = ControllerMediator.getInstance().isShowingIsoformID();
        boolean showingIsoformName = ControllerMediator.getInstance().isShowingIsoformName();
        String isoformName = isoform.getName();

        if (showingIsoformID && showingIsoformName && isoformName != null)
            label += isoformID + " (" +isoformName + ")";
        else if (showingIsoformName && isoformName != null)
            label += isoformName;
        else
            label += isoformID;

        return label;
    }

    /**
     * Draws the exons and introns of the given isoform, without reverse complementing
     */
    private void drawIsoform(Isoform isoform, int geneStart, double pixelsPerNucleotide) {
        ArrayList<Exon> exons = isoform.getExons();
        for (int i = 0; i < exons.size(); ++i) {
            drawExon(geneStart, pixelsPerNucleotide, exons, i);
            if (i != 0) {
                drawIntron(geneStart, pixelsPerNucleotide, exons, i);
            }
        }
    }


    /**
     * Draws the exons and introns of the reverse complement of the given isoform
     */
    private void drawIsoformReverseComplement(Isoform isoform, int geneEnd, double pixelsPerNucleotide) {
        ArrayList<Exon> exons = isoform.getExons();
        for (int i = 0; i < exons.size(); ++i) {
            drawExonReverseComplement(geneEnd, pixelsPerNucleotide, exons, i);
            if (i != 0) {
                drawIntronReverseComplement(geneEnd, pixelsPerNucleotide, exons, i);
            }
        }
    }

    /**
     * Draws the given exon, without reverse complementing
     */
    private void drawExon(int geneStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int exonEnd = exons.get(i).getEndNucleotide();
        double startX = (exonStart - geneStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double width = (exonEnd - exonStart + 1) * pixelsPerNucleotide;
        drawExonGraphic(startX, width);
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
    private void drawExonReverseComplement(int geneEnd, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int exonEnd = exons.get(i).getEndNucleotide();
        double startX = (geneEnd - exonEnd) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double width = (exonEnd - exonStart + 1) * pixelsPerNucleotide;
        drawExonGraphic(startX, width);
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

    private void drawExonGraphic(double startX, double width) {
        gc.setFill(EXON_COLOUR);
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
