package ui;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import parser.Parser;
import parser.data.Exon;
import parser.data.Gene;
import parser.data.Isoform;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class FXMLController implements Initializable {
    //private static final float CANVAS_SCALE_FACTOR = 1f;
    private static final int CANVAS_INIT_Y = 13;
    private static final int CANVAS_INIT_X = 0;
    private static final int SPACING = 25;
    private static final int SCROLLBAR_WIDTH = 20;
    private static final int CANVAS_MARGIN = 15;
    private static final Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);
    private static final Font TRANSCRIPT_FONT = Font.font("Verdana",15);
    private static final int EXON_HEIGHT = 10;

    @FXML private ComboBox path;
    @FXML private Label console;
    @FXML private Canvas canvas;
    @FXML private ScrollPane scrollPane;
    @FXML private ComboBox geneSelector;

    private GraphicsContext gc;
    private int canvasCurrY;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeGraphics();
        initializeScrollPane();
        initializeGeneSelector();
    }

    /**
     * When load button is pressed, parses path to file and retrieves parsed genes
     * Adds parsed genes to gene selector's items
     * Displays error (and successful completion) messages in console
     */
    @FXML
    protected void handleLoadButtonAction(ActionEvent event) {
        String consoleText = Parser.readFile((String) path.getValue());
        HashMap<String, Gene> genes = Parser.getParsedGenes();
        ObservableList<String> addedGenes= geneSelector.getItems();
        for(String gene : genes.keySet()) {
            if (!addedGenes.contains(gene))
                addedGenes.add(gene);
        }
        console.setText(consoleText);
    }

    private void initializeGraphics() {
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        canvasCurrY = CANVAS_INIT_Y;
    }

    /**
     * Adds listener to resize canvas width when scroll pane width changes
     * (unless scroll pane width < MIN_CANVAS_WIDTH)
     * Redraws canvas when resize occurs and gene is being displayed
     */
    private void initializeScrollPane() {
        scrollPane.widthProperty().addListener((ov, oldValue, newValue) -> {
            canvas.setWidth(newValue.doubleValue() - (2 * CANVAS_MARGIN + SCROLLBAR_WIDTH));
            String currGene = (String) geneSelector.getValue();
            if (currGene != null)
                drawGene(Parser.getParsedGenes().get(currGene), currGene);

        });
    }

    /**
     * Makes gene selector draw new gene each time user selects one
     */
    private void initializeGeneSelector() {
        geneSelector.valueProperty().addListener((ChangeListener<String>) (ov, oldValue, newValue) -> drawGene(Parser.getParsedGenes().get(newValue), newValue));
    }

    /**
     * Clears canvas, draws and labels all isoforms of the given gene
     * @param gene gene to draw
     * @param geneID ID of gene to draw
     */
    private void drawGene(Gene gene, String geneID) {
        clearCanvas();
        drawGeneID(geneID);
        drawAllIsoforms(gene);
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvasCurrY = CANVAS_INIT_Y;
    }

    /**
     * Draws label for gene
     */
    private void drawGeneID(String geneID) {
        gc.setFont(GENE_FONT);
        gc.fillText(geneID, CANVAS_INIT_X, canvasCurrY);
        canvasCurrY += SPACING;
    }

    /**
     * Labels and draws each isoform of given gene
     */
    private void drawAllIsoforms(Gene gene) {
        gc.setFont(TRANSCRIPT_FONT);
        int geneStart = gene.getStartNucleotide();
        int geneEnd = gene.getEndNucleotide();
        double pixelsPerNucleotide = canvas.getWidth()/(geneEnd- geneStart);
        for(String transcriptID : gene.getIsoforms().keySet()) {
            gc.fillText(transcriptID, CANVAS_INIT_X, canvasCurrY);
            canvasCurrY += SPACING / 2;
            drawIsoform(gene.getIsoform(transcriptID), geneStart, pixelsPerNucleotide);
            canvasCurrY += SPACING * 2;
        }
    }

    /**
     * Draws the exons and introns of the given isoform
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

    private void drawExon(int geneStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int exonEnd = exons.get(i).getEndNucleotide();
        double startX = (exonStart - geneStart) * pixelsPerNucleotide;
        double width = (exonEnd - exonStart) * pixelsPerNucleotide;
        gc.fillRect(startX, canvasCurrY, width, EXON_HEIGHT);
    }

    private void drawIntron(int geneStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int prevExonEnd = exons.get(i - 1).getEndNucleotide();
        double startX = (prevExonEnd - geneStart) * pixelsPerNucleotide;
        double endX = (exonStart - geneStart) * pixelsPerNucleotide;
        double y = canvasCurrY + (double) EXON_HEIGHT / 2;
        gc.strokeLine(startX, y, endX, y);
    }
}
