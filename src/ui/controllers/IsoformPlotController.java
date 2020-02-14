package ui.controllers;

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
import parser.Parser;
import parser.data.Exon;
import parser.data.Gene;
import parser.data.Isoform;
import ui.mediator.ControllerMediator;
import ui.resources.Util;

import java.net.URL;
import java.util.*;

public class IsoformPlotController implements Initializable, InteractiveElementController {
    private static final int CANVAS_MIN_WIDTH = 250;
    private static final int CANVAS_INIT_Y = 13;
    private static final int GENE_ID_X_OFFSET = 0;
    private static final int ISOFORM_X_OFFSET = 13;
    private static final int SPACING = 25;
    private static final int SCROLLBAR_WIDTH = 20;
    private static final int CANVAS_MARGIN = 15;
    private static final Color FONT_COLOUR = Color.BLACK;
    private static final Color EXON_COLOUR = Color.color(0.600, 0.851, 1);
    private static final Color OUTLINE_COLOUR = Color.BLACK;
    private static final Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);
    private static final Font TRANSCRIPT_FONT = Font.font("Verdana",12);
    private static final int EXON_HEIGHT = 10;

    @FXML private Canvas canvas;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox isoformPlot;
    @FXML private Button selectGenesButton;

    private GraphicsContext gc;
    private List<String> shownGenes;
    private boolean reverseComplement;
    private int canvasCurrY;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        shownGenes = new ArrayList<>();
        initializeGraphics();
        initializeScrollPane();
    }

    /**
     * Changes whether genes on the (-) strand should be reverse complemented or not,
     * and redraws genes
     */
    public void toggleReverseComplement() {
        reverseComplement = !reverseComplement;
        drawGenes();
    }

    public void disable() {
        selectGenesButton.setDisable(true);
    }

    public void enable() {
        selectGenesButton.setDisable(false);
    }

    public void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setHeight(0);
        canvasCurrY = CANVAS_INIT_Y;
    }

    /**
     * Sets shownGenes and redraws all genes
     */
    public void setShownGenes(List<String> shownGenes) {
        this.shownGenes = shownGenes;
        drawGenes();
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
        reverseComplement = false;
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
     * Draws all genes meant to be shown
     */
    private void drawGenes() {
        clearCanvas();
        incrementCanvasHeight();
        HashMap<String, Gene> parsedGenes = Parser.getParsedGenes();
        for (String geneID : shownGenes) {
            drawGene(parsedGenes.get(geneID), geneID);
        }
    }

    /**
     * Sets canvas height to height necessary to display genes
     */
    private void incrementCanvasHeight() {
        double newHeight = canvas.getHeight();
        HashMap<String, Gene> parsedGenes = Parser.getParsedGenes();
        for (String geneID : shownGenes) {
            Gene gene = parsedGenes.get(geneID);
            int numIsoforms = gene.getIsoforms().size();
            newHeight += numIsoforms * SPACING * 2 + SPACING;
        }
        canvas.setHeight(newHeight);
    }

    /**
     * Clears canvas, draws and labels all isoforms of the given gene
     * @param gene gene to draw
     * @param geneID ID of gene to draw
     */
    private void drawGene(Gene gene, String geneID) {
        drawGeneID(gene, geneID);
        drawAllIsoforms(gene);
    }

    /**
     * Draws label for gene
     */
    private void drawGeneID(Gene gene, String geneID) {
        gc.setFill(FONT_COLOUR);
        gc.setFont(GENE_FONT);
        if(gene.isPositiveSense() && reverseComplement)
            gc.fillText(geneID + " (+)", GENE_ID_X_OFFSET, canvasCurrY);
        else if(reverseComplement)
            gc.fillText(geneID + " (-)", GENE_ID_X_OFFSET, canvasCurrY);
        else
            gc.fillText(geneID, GENE_ID_X_OFFSET, canvasCurrY);
        canvasCurrY += SPACING;
    }

    /**
     * Labels and draws each isoform of given gene
     */
    private void drawAllIsoforms(Gene gene) {
        gc.setFont(TRANSCRIPT_FONT);
        int geneStart = gene.getStartNucleotide();
        int geneEnd = gene.getEndNucleotide();
        double pixelsPerNucleotide = (canvas.getWidth() - ISOFORM_X_OFFSET)/(geneEnd- geneStart + 1);
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

}
