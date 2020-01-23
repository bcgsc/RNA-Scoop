package ui;

import javafx.collections.ListChangeListener;
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
import org.controlsfx.control.CheckComboBox;
import parser.Parser;
import parser.data.Exon;
import parser.data.Gene;
import parser.data.Isoform;
import ui.util.Util;

import java.net.URL;
import java.util.*;

public class FXMLController implements Initializable {
    private static final int CANVAS_MIN_WIDTH = 150;
    private static final int CANVAS_INIT_Y = 13;
    private static final int GENE_ID_X_OFFSET = 0;
    private static final int ISOFORM_X_OFFSET = 13;
    private static final int SPACING = 25;
    private static final int SCROLLBAR_WIDTH = 20;
    private static final int CANVAS_MARGIN = 15;
    private static final Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);
    private static final Font TRANSCRIPT_FONT = Font.font("Verdana",12);
    private static final int EXON_HEIGHT = 10;

    @FXML private ComboBox path;
    @FXML private Label console;
    @FXML private Canvas canvas;
    @FXML private ScrollPane scrollPane;
    @FXML private CheckComboBox geneSelector;

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
        addLoadedPaths();
        addLoadedGenes();
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

    private void addLoadedPaths() {
        ObservableList<String> addedPaths = path.getItems();
        if (!addedPaths.contains(path.getValue())) {
            addedPaths.add((String) path.getValue());
        }
    }

    private void addLoadedGenes() {
        HashMap<String, Gene> genes = Parser.getParsedGenes();
        ObservableList<String> addedGenes= geneSelector.getItems();
        for(String gene : genes.keySet()) {
            if (!addedGenes.contains(gene))
                addedGenes.add(gene);
        }
        addedGenes.sort(String::compareTo);
    }

    /**
     * Draws all genes selected in gene selector combo box
     */
    private void drawGenes() {
        clearCanvas();
        List<String> selectedGenes = geneSelector.getCheckModel().getCheckedItems();
        for (String gene : selectedGenes)
            drawGene(Parser.getParsedGenes().get(gene), gene);
    }

    /**
     * Clears canvas, draws and labels all isoforms of the given gene
     * @param gene gene to draw
     * @param geneID ID of gene to draw
     */
    private void drawGene(Gene gene, String geneID) {
        incrementCanvasHeight(gene);
        drawGeneID(geneID);
        drawAllIsoforms(gene);
    }

    /**
     * Sets canvas height to height necessary to display given gene
     */
    private void incrementCanvasHeight(Gene gene) {
        int numIsoforms = gene.getIsoforms().size();
        canvas.setHeight(canvas.getHeight() + numIsoforms * SPACING * 2 + SPACING);
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setHeight(0);
        canvasCurrY = CANVAS_INIT_Y;
    }

    /**
     * Draws label for gene
     */
    private void drawGeneID(String geneID) {
        gc.setFont(GENE_FONT);
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
        double pixelsPerNucleotide = (canvas.getWidth() - ISOFORM_X_OFFSET)/(geneEnd- geneStart);
        Collection<String>  isoforms = gene.getIsoforms().keySet();
        List<String> sortedIsoforms = Util.asSortedList(isoforms);
        for(String transcriptID : sortedIsoforms) {
            gc.fillText(transcriptID, ISOFORM_X_OFFSET, canvasCurrY);
            canvasCurrY += SPACING / 3;
            drawIsoform(gene.getIsoform(transcriptID), geneStart, pixelsPerNucleotide);
            canvasCurrY += SPACING * 5/3;
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
        double startX = (exonStart - geneStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double width = (exonEnd - exonStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        gc.fillRect(startX, canvasCurrY, width, EXON_HEIGHT);
    }

    private void drawIntron(int geneStart, double pixelsPerNucleotide, ArrayList<Exon> exons, int i) {
        int exonStart = exons.get(i).getStartNucleotide();
        int prevExonEnd = exons.get(i - 1).getEndNucleotide();
        double startX = (prevExonEnd - geneStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double endX = (exonStart - geneStart) * pixelsPerNucleotide + ISOFORM_X_OFFSET;
        double y = canvasCurrY + (double) EXON_HEIGHT / 2;
        gc.strokeLine(startX, y, endX, y);
    }
}