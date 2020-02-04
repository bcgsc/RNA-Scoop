package ui.fxml.main.controllers;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.CheckComboBox;
import parser.Parser;
import parser.data.Exon;
import parser.data.Gene;
import parser.data.Isoform;
import ui.resources.Util;

import java.net.URL;
import java.util.*;

public class IsoformPlotController implements Initializable {
    private static final int CANVAS_MIN_WIDTH = 200;
    private static final int CANVAS_INIT_Y = 13;
    private static final int GENE_ID_X_OFFSET = 0;
    private static final int ISOFORM_X_OFFSET = 13;
    private static final int SPACING = 25;
    private static final int SCROLLBAR_WIDTH = 20;
    private static final int CANVAS_MARGIN = 15;
    private static final Color FONT_COLOUR = Color.BLACK;
    private static final Color EXON_COLOUR = Color.color(0.457, 0.816, 0.555);
    private static final Color OUTLINE_COLOUR = Color.BLACK;
    private static final Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);
    private static final Font TRANSCRIPT_FONT = Font.font("Verdana",12);
    private static final int EXON_HEIGHT = 10;

    @FXML private Canvas canvas;
    @FXML private ScrollPane scrollPane;
    @FXML private CheckComboBox geneSelector;
    @FXML private VBox isoformPlot;
    private boolean reverseComplement;

    private GraphicsContext gc;
    private int canvasCurrY;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeGraphics();
        initializeScrollPane();
        initializeGeneSelector();
    }

    /**
     * Adds given genes to gene selector (if not already added)
     */
    public void addGenes(Map<String, Gene> genes) {
        ObservableList<String> addedGenes= geneSelector.getItems();
        for(String gene : genes.keySet()) {
            if (!addedGenes.contains(gene))
                addedGenes.add(gene);
        }
        addedGenes.sort(String::compareTo);
    }

    /**
     * Changes whether genes on the (-) strand should be reverse complemented or not,
     * and redraws genes
     */
    public void toggleReverseComplement() {
        reverseComplement = !reverseComplement;
        drawGenes();
    }

    public void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setHeight(0);
        canvasCurrY = CANVAS_INIT_Y;
    }

    /**
     * Deselects all gene selector's genes
     */
    public void clearCheckedGenes() {
        for(int i = 0; i < geneSelector.getItems().size(); i++)
            geneSelector.getCheckModel().clearCheck(i);
    }

    public VBox getIsoformPlot() {
        return isoformPlot;
    }

    private void initializeGraphics() {
        gc = canvas.getGraphicsContext2D();
        canvasCurrY = CANVAS_INIT_Y;
        reverseComplement = false;
    }

    /**
     * Draws all genes selected in gene selector combo box
     */
    private void drawGenes() {
        clearCanvas();
        List<String> selectedGenes = geneSelector.getCheckModel().getCheckedItems();
        for (String gene : selectedGenes) {
            drawGene(Parser.getParsedGenes().get(gene), gene);
        }
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
                drawGenes();
            }
        });
    }

    /**
     * Makes gene selector draw new gene each time user selects one
     */
    private void initializeGeneSelector() {
        geneSelector.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> drawGenes());
    }

    /**
     * Clears canvas, draws and labels all isoforms of the given gene
     * @param gene gene to draw
     * @param geneID ID of gene to draw
     */
    private void drawGene(Gene gene, String geneID) {
        incrementCanvasHeight(gene);
        drawGeneID(gene, geneID);
        drawAllIsoforms(gene);
    }

    /**
     * Sets canvas height to height necessary to display given gene
     */
    private void incrementCanvasHeight(Gene gene) {
        int numIsoforms = gene.getIsoforms().size();
        canvas.setHeight(canvas.getHeight() + numIsoforms * SPACING * 2 + SPACING);
    }

    /**
     * Draws label for gene
     */
    private void drawGeneID(Gene gene, String geneID) {
        gc.setFill(FONT_COLOUR);
        gc.setFont(GENE_FONT);
        if(gene.isPositiveSense())
            gc.fillText(geneID + " (+)", GENE_ID_X_OFFSET, canvasCurrY);
        else
            gc.fillText(geneID + " (-)", GENE_ID_X_OFFSET, canvasCurrY);
        canvasCurrY += SPACING;
    }

    /**
     * Labels and draws each isoform of given gene
     */
    private void drawAllIsoforms(Gene gene) {
        gc.setFont(TRANSCRIPT_FONT);
        int geneStart = gene.getStartNucleotide();
        int geneEnd = gene.getEndNucleotide();
        double pixelsPerNucleotide = (canvas.getWidth() - ISOFORM_X_OFFSET)/(geneEnd- geneStart);
        Collection<String> isoforms = gene.getIsoforms().keySet();
        List<String> sortedIsoforms = Util.asSortedList(isoforms);
        for(String transcriptID : sortedIsoforms) {
            gc.setFill(FONT_COLOUR);
            gc.fillText(transcriptID, ISOFORM_X_OFFSET, canvasCurrY);
            canvasCurrY += SPACING / 3;
            if(gene.isPositiveSense() || !reverseComplement)
                drawIsoform(gene.getIsoform(transcriptID), geneStart, pixelsPerNucleotide);
            else
                drawIsoformReverseComplement(gene.getIsoform(transcriptID), geneEnd, pixelsPerNucleotide);
            canvasCurrY += SPACING * 5/3;
        }
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
        double width = (exonEnd - exonStart) * pixelsPerNucleotide;
        drawExonGraphic(startX, width);
    }

    /**
     * Draws the given intron, without reverse complementing
     */
    private void drawIntron(int geneStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int prevExonEnd = exons.get(i - 1).getEndNucleotide();
        double startX = (prevExonEnd - geneStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
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
        double width = (exonEnd - exonStart) * pixelsPerNucleotide;
        drawExonGraphic(startX, width);
    }

    /**
     * Draws the reverse complement of the given intron
     */
    private void drawIntronReverseComplement(int geneEnd, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int prevExonEnd= exons.get(i - 1).getEndNucleotide();
        double startX = (geneEnd - exonStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
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
}
