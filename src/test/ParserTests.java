package test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import parser.Parser;
import parser.data.Exon;
import parser.data.Gene;
import parser.data.Isoform;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ParserTests {
    final private static String FILE_PATH = "/projects/kmnip_prj/work/mstephenson/npm1_gapdh_pml_exon_coords.tsv";
    final private static int NUM_GENES = 3;
    final private static String[] ENSG00000181163_ISOFORMS = {"ENST00000517671", "ENST00000296930", "ENST00000521672",
                                                              "ENST00000351986", "ENST00000393820", "ENST00000523339",
                                                              "ENST00000523622", "ENST00000518587", "ENST00000521260",
                                                              "ENST00000521710", "ENST00000519955", "ENST00000524204"};
    final private static String[] ENSG00000111640_ISOFORMS = {"ENST00000229239", "ENST00000496049", "ENST00000396856",
                                                              "ENST00000492719", "ENST00000396861", "ENST00000474249",
                                                              "ENST00000466588", "ENST00000396859", "ENST00000466525",
                                                              "ENST00000396858", "ENST00000619601"};
    final private static int[][] ENST00000523622_EXONS = {{171387881, 171388006}, {171390075, 171390130},
                                                          {171391305, 171391369}};
    final private static int[][] ENST00000563500_EXONS = {{73994803, 73994941}, {73998004, 73998476},
                                                          {74022828, 74023408}, {74024857, 74024927},
                                                          {74033156, 74036393}};
    private static HashMap<String, Gene> genes;

    @BeforeAll
    public static void setUp() {
        Parser.readFile(FILE_PATH);
        genes = Parser.getParsedGenes();
    }

    @Test
    public void testParserGenes() {
        int timesRan = 0;
        Boolean parsedENSG00000181163 = false;
        Boolean parsedENSG00000111640= false;
        Boolean parsedENSG00000140464 = false;

        for (String key : genes.keySet()) {
            Gene gene = genes.get(key);
            switch(key) {
                case "ENSG00000181163":
                    assertEquals(5, gene.getChromosome());
                    assertEquals("+", gene.getStrand());
                    assertEquals(171387116, gene.getStartNucleotide());
                    assertEquals(171411137, gene.getEndNucleotide());
                    parsedENSG00000181163 = true;
                    break;
                case "ENSG00000111640":
                    assertEquals(12, gene.getChromosome());
                    assertEquals("+", gene.getStrand());
                    parsedENSG00000111640 = true;
                    break;
                case "ENSG00000140464":
                    assertEquals(15, gene.getChromosome());
                    assertEquals("+", gene.getStrand());
                    parsedENSG00000140464 = true;
                    break;
            }
            timesRan++;
        }

        assertTrue(parsedENSG00000181163);
        assertTrue(parsedENSG00000111640);
        assertTrue(parsedENSG00000140464);
        assertEquals(NUM_GENES, timesRan);
    }

    @Test
    public void testParserENSG00000181163Isoforms() {
        Gene gene = genes.get("ENSG00000181163");
        for (String isoform : ENSG00000181163_ISOFORMS) {
            assertTrue(gene.hasIsoform(isoform));
        }
        HashMap<String, Isoform> isoforms = gene.getIsoforms();
        assertEquals(ENSG00000181163_ISOFORMS.length, countIsoforms(isoforms));
    }

    @Test
    public void testParserENST00000619601Isoforms() {
        Gene gene = genes.get("ENSG00000111640");
        for (String isoform : ENSG00000111640_ISOFORMS) {
            assertTrue(gene.hasIsoform(isoform));
        }
        HashMap<String, Isoform> isoforms = gene.getIsoforms();
        assertEquals(ENSG00000111640_ISOFORMS.length, countIsoforms(isoforms));
    }

    @Test
    public void testENST00000523622Exons() {
        Gene gene = genes.get("ENSG00000181163");
        Isoform ENST00000523622 = gene.getIsoform("ENST00000523622");
        ArrayList<Exon> exons = ENST00000523622.getExons();
        int timesRan = 0;
        for(Exon exon : exons) {
            int startNucleotide = exon.getStartNucleotide();
            int endNucleotide = exon.getEndNucleotide();
            boolean compared = false;
            for (int i = 0; i < ENST00000523622_EXONS.length; i++) {
                if (startNucleotide == ENST00000523622_EXONS[i][0]) {
                    assertEquals(ENST00000523622_EXONS[i][1], endNucleotide);
                    compared = true;
                }
            }
            assertTrue(compared);
            timesRan++;
        }
        assertEquals(ENST00000523622_EXONS.length, timesRan);
    }
    @Test
    public void testENST00000563500Exons() {
        Gene gene = genes.get("ENSG00000140464");
        Isoform ENST00000563500  = gene.getIsoform("ENST00000563500");
        ArrayList<Exon> exons = ENST00000563500 .getExons();
        int timesRan = 0;
        for(Exon exon : exons) {
            int startNucleotide = exon.getStartNucleotide();
            int endNucleotide = exon.getEndNucleotide();
            boolean compared = false;
            for (int i = 0; i < ENST00000563500_EXONS.length; i++) {
                if (startNucleotide == ENST00000563500_EXONS[i][0]) {
                    assertEquals(ENST00000563500_EXONS[i][1], endNucleotide);
                    compared = true;
                }
            }
            assertTrue(compared);
            timesRan++;
        }
        assertEquals(ENST00000563500_EXONS.length, timesRan);
    }

    private int countIsoforms(HashMap<String, Isoform> isoforms) {
        int num = 0;
        for (String key : isoforms.keySet())
            num++;
        return num;
    }
}
