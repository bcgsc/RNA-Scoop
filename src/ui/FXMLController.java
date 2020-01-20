package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private final static int START_Y_COORD = 20;
    private final static int SPACING = 25;
    private final static int START_X_COORD = 5;
    private final static Font GENE_FONT = Font.font("Verdana", FontWeight.BOLD, 15);
    private final static Font TRANSCRIPT_FONT = Font.font("Verdana",15);

    @FXML private ComboBox path;
    @FXML private Label console;
    @FXML private Canvas canvas;
    @FXML private ScrollPane scrollPane;
    @FXML private ComboBox geneSelector;

    private GraphicsContext gc;

    final private static int MIN_CANVAS_WIDTH = 400;


    @FXML
    protected void handleLoadButtonAction(ActionEvent event) {
        String consoleText = Parser.readFile(path.getPromptText());
        HashMap<String, Gene> genes = Parser.getParsedGenes();
        ObservableList<String> addedGenes= geneSelector.getItems();
        for(String gene : genes.keySet()) {
            if (!addedGenes.contains(gene))
                addedGenes.add(gene);
        }
        console.setText(consoleText);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = canvas.getGraphicsContext2D();
        scrollPane.widthProperty().addListener((ov, oldValue, newValue) -> {
            if(newValue.doubleValue() - 20 >= MIN_CANVAS_WIDTH) {
                canvas.setWidth(newValue.doubleValue() - 20);
                String currGene = geneSelector.getPromptText();
                if (!currGene.equals("No Gene Selected"))
                    drawGene(Parser.getParsedGenes().get(currGene), currGene);
            }
        });
        geneSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                geneSelector.setPromptText(t1);
                drawGene(Parser.getParsedGenes().get(t1), t1);
            }
        });
        path.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                path.setPromptText(t1);
            }
        });
    }

    private void drawGene(Gene gene, String geneID) {
        // clear canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int currYCoord = START_Y_COORD;

        // add geneID
        gc.setFill(Color.BLACK);
        gc.setFont(GENE_FONT);
        gc.fillText(geneID, START_X_COORD, currYCoord);
        currYCoord += SPACING;

        //draw isoforms
        gc.setFont(TRANSCRIPT_FONT);
        int geneStart = gene.getStartNucleotide();
        int geneEnd = gene.getEndNucleotide();
        double pixelsPerNucleotide = canvas.getWidth()/(geneEnd- geneStart);
        for(String transcriptID : gene.getIsoforms().keySet()) {
            gc.fillText(transcriptID, START_X_COORD, currYCoord);
            currYCoord += SPACING/2;
            drawIsoform(gene.getIsoform(transcriptID), geneStart, currYCoord, pixelsPerNucleotide);
            currYCoord += SPACING * 2;
        }
    }

    private void drawIsoform(Isoform isoform, int geneStart, int currYCoord, double pixelsPerNucleotide) {
        ArrayList<Exon> exons = isoform.getExons();
        for (int i = 0; i < exons.size(); i++) {
            int exonStart = exons.get(i).getStartNucleotide();
            int exonEnd = exons.get(i).getEndNucleotide();
            double startX = (exonStart - geneStart)*pixelsPerNucleotide;
            double widthX = (exonEnd - exonStart)*pixelsPerNucleotide;
            gc.fillRect(startX, currYCoord, widthX, 10);
        }
    }
}
