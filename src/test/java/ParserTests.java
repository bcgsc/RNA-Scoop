import annotation.Exon;
import annotation.Gene;
import annotation.Isoform;
import parser.Parser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


public class ParserTests {
    final static private String FILE_PATH = "/home/mstephenson/Downloads/nbt.4259-S_16lines.tsv";
    final static private int NUM_GENES = 9;
    private static final String GENE_0 = "ENSMUSG00000102693.1";
    private static final String GENE_1 = "ENSMUSG00000051951.5";
    private static final String GENE_0_NAME = null;
    private static final String GENE_1_NAME = "Xkr4";
    private static final String ISOFORM_0 = "ENSMUST00000162897.1";
    private static final String ISOFORM_1 = "ENSMUST00000159265.1";
    private static final String ISOFORM_2 = "ENSMUST00000070533.4";
    final private static String[] GENE_1_ISOFORMS = {ISOFORM_0, ISOFORM_1, ISOFORM_2};
    final private static int[][] ISOFORM_0_EXONS = {{3213609, 3216344}, {3205901, 3207317}};
    final private static int[][] ISOFORM_1_EXONS = {{3213439, 3215632}, {3206523, 3207317}};
    final private static int[][] ISOFORM_2_EXONS = {{3670552, 3671498}, {3421702, 3421901},
                                                    {3214482, 3216968}};
    private static final String ISOFORM_0_NAME = "Xkr4-003";
    private static final String ISOFORM_1_NAME = null;
    private static final String ISOFORM_2_NAME = "Xkr4-001";

    private static HashMap<String, Gene> genes;

    @BeforeAll
    public static void setUp() {
        try {
            Parser.readFile(FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        genes = Parser.getParsedGenesMap();
    }

    @Test
    public void testParserGenes() {
        int timesRan = 0;
        boolean parsedGene0 = false;
        boolean parsedGene1= false;

        for (String key : genes.keySet()) {
            Gene gene = genes.get(key);
            switch(key) {
                case GENE_0:
                    assertEquals(GENE_0_NAME, gene.getName());
                    assertEquals("1", gene.getChromosome());
                    assertTrue(gene.isOnPositiveStrand());
                    assertEquals(3073253, gene.getStartNucleotide());
                    assertEquals(3074322, gene.getEndNucleotide());
                    parsedGene0 = true;
                    break;
                case GENE_1:
                    assertEquals(GENE_1_NAME, gene.getName());
                    assertEquals("chr1", gene.getChromosome());
                    assertFalse(gene.isOnPositiveStrand());
                    assertEquals(3205901, gene.getStartNucleotide());
                    assertEquals(3671498, gene.getEndNucleotide());
                    parsedGene1 = true;
                    break;
            }
            timesRan++;
        }

        assertTrue(parsedGene0);
        assertTrue(parsedGene1);
        assertEquals(NUM_GENES, timesRan);
    }


    @Test
    public void testParserGene1Isoforms() {
        Gene gene = genes.get(GENE_1);
        for (String isoform : GENE_1_ISOFORMS) {
            assertTrue(gene.hasIsoform(isoform));
            Isoform geneIsoform = gene.getIsoform(isoform);
            switch(isoform) {
                case ISOFORM_0:
                    assertEquals(ISOFORM_0_NAME, geneIsoform.getName());
                    break;
                case ISOFORM_1:
                    assertEquals(ISOFORM_1_NAME, geneIsoform.getName());
                    break;
                case ISOFORM_2:
                    assertEquals(ISOFORM_2_NAME, geneIsoform.getName());
                    break;
            }

        }
        assertEquals(GENE_1_ISOFORMS.length, gene.getNumIsoforms());
    }

    @Test
    public void testIsoform0Exons() {
        Gene gene = genes.get(GENE_1);
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
        Gene gene = genes.get(GENE_1);
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

    @Test
    public void testIsoform2Exons() {
        Gene gene = genes.get(GENE_1);
        Isoform isoform  = gene.getIsoform(ISOFORM_2);
        ArrayList<Exon> exons = isoform .getExons();
        int timesRan = 0;
        for(Exon exon : exons) {
            int startNucleotide = exon.getStartNucleotide();
            int endNucleotide = exon.getEndNucleotide();
            boolean compared = false;
            for (int[] isoform1Exon : ISOFORM_2_EXONS) {
                if (startNucleotide == isoform1Exon[0]) {
                    assertEquals(isoform1Exon[1], endNucleotide);
                    compared = true;
                }
            }
            assertTrue(compared);
            timesRan++;
        }
        assertEquals(ISOFORM_2_EXONS.length, timesRan);
    }
}
