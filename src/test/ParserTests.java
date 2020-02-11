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
    final static private String FILE_PATH = "/projects/kmnip_prj/work/mstephenson/npm1_gapdh_pml_exon_coords.tsv";
    final static private int NUM_GENES = 3;
    private static final String GENE_0 = "ENSG00000181163";
    private static final String GENE_1 = "ENSG00000111640";
    private static final String GENE_2 = "ENSG00000140464";
    private static final String ISOFORM_0 = "ENST00000523622";
    private static final String ISOFORM_1 = "ENST00000563500";
    private static final String[] GENE_0_ISOFORMS = {"ENST00000517671", "ENST00000296930", "ENST00000521672",
                                                     "ENST00000351986", "ENST00000393820", "ENST00000523339",
                                                     ISOFORM_0, "ENST00000518587", "ENST00000521260",
                                                     "ENST00000521710", "ENST00000519955", "ENST00000524204"};
    final private static String[] GENE_1_ISOFORMS = {"ENST00000229239", "ENST00000496049", "ENST00000396856",
                                                     "ENST00000492719", "ENST00000396861", "ENST00000474249",
                                                     "ENST00000466588", "ENST00000396859", "ENST00000466525",
                                                     "ENST00000396858", "ENST00000619601"};
    final private static int[][] ISOFORM_0_EXONS = {{171387881, 171388006}, {171390075, 171390130},
                                                    {171391305, 171391369}};
    final private static int[][] ISOFORM_1_EXONS = {{73994803, 73994941}, {73998004, 73998476},
                                                    {74022828, 74023408}, {74024857, 74024927},
                                                    {74033156, 74036393}};

    private static HashMap<String, Gene> genes;

    @BeforeAll
    public static void setUp() {
        try {
            Parser.readFile(FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        genes = Parser.getParsedGenes();
    }

    @Test
    public void testParserGenes() {
        int timesRan = 0;
        boolean parsedGene0 = false;
        boolean parsedGene1= false;
        boolean parsedGene2 = false;

        for (String key : genes.keySet()) {
            Gene gene = genes.get(key);
            switch(key) {
                case GENE_0:
                    assertEquals("5", gene.getChromosome());
                    assertTrue(gene.isPositiveSense());
                    assertEquals(171387116, gene.getStartNucleotide());
                    assertEquals(171411137, gene.getEndNucleotide());
                    parsedGene0 = true;
                    break;
                case GENE_1:
                    assertEquals("12", gene.getChromosome());
                    assertTrue(gene.isPositiveSense());
                    parsedGene1 = true;
                    break;
                case GENE_2:
                    assertEquals("15", gene.getChromosome());
                    assertTrue(gene.isPositiveSense());
                    parsedGene2 = true;
                    assertEquals(73994673, gene.getStartNucleotide());
                    assertEquals(74047812, gene.getEndNucleotide());
                    break;
            }
            timesRan++;
        }

        assertTrue(parsedGene0);
        assertTrue(parsedGene1);
        assertTrue(parsedGene2);
        assertEquals(NUM_GENES, timesRan);
    }

    @Test
    public void testParserGene0Isoforms() {
        Gene gene = genes.get(GENE_0);
        for (String isoform : GENE_0_ISOFORMS) {
            assertTrue(gene.hasIsoform(isoform));
        }
        HashMap<String, Isoform> isoforms = gene.getIsoforms();
        assertEquals(GENE_0_ISOFORMS.length, countIsoforms(isoforms));
    }

    @Test
    public void testParserGene1Isoforms() {
        Gene gene = genes.get(GENE_1);
        for (String isoform : GENE_1_ISOFORMS) {
            assertTrue(gene.hasIsoform(isoform));
        }
        HashMap<String, Isoform> isoforms = gene.getIsoforms();
        assertEquals(GENE_1_ISOFORMS.length, countIsoforms(isoforms));
    }

    @Test
    public void testIsoform0Exons() {
        Gene gene = genes.get(GENE_0);
        Isoform isoform = gene.getIsoform(ISOFORM_0);
        ArrayList<Exon> exons = isoform.getExons();
        int timesRan = 0;
        for(Exon exon : exons) {
            int startNucleotide = exon.getStartNucleotide();
            int endNucleotide = exon.getEndNucleotide();
            boolean compared = false;
            for (int[] isoform0Exon : ISOFORM_0_EXONS) {
                if (startNucleotide == isoform0Exon[0]) {
                    assertEquals(isoform0Exon[1], endNucleotide);
                    compared = true;
                }
            }
            assertTrue(compared);
            timesRan++;
        }
        assertEquals(ISOFORM_0_EXONS.length, timesRan);
    }
    @Test
    public void testIsoform1Exons() {
        Gene gene = genes.get(GENE_2);
        Isoform isoform  = gene.getIsoform(ISOFORM_1);
        ArrayList<Exon> exons = isoform .getExons();
        int timesRan = 0;
        for(Exon exon : exons) {
            int startNucleotide = exon.getStartNucleotide();
            int endNucleotide = exon.getEndNucleotide();
            boolean compared = false;
            for (int[] isoform1Exon : ISOFORM_1_EXONS) {
                if (startNucleotide == isoform1Exon[0]) {
                    assertEquals(isoform1Exon[1], endNucleotide);
                    compared = true;
                }
            }
            assertTrue(compared);
            timesRan++;
        }
        assertEquals(ISOFORM_1_EXONS.length, timesRan);
    }

    private int countIsoforms(HashMap<String, Isoform> isoforms) {
        int num = 0;
        for (String key : isoforms.keySet())
            num++;
        return num;
    }
}
