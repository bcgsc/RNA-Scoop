package parser;

import parser.data.Exon;
import parser.data.Gene;
import parser.data.Isoform;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

public class Parser {

    /**
     * Map of all genes parser has parsed so far
     * Key is the gene's ID, value is the gene
     */
    static HashMap<String, Gene> parsedGenes = new HashMap<>();

    /**
     * Reads in file at given path and parses each line
     * @param path path to the file
     */
    public static String readFile(String path) {
        try {
            File myObject = new File(path);
            Scanner myReader = new Scanner(myObject);
            myReader.nextLine();
            while (myReader.hasNextLine()) {
                String exonDataString = myReader.nextLine();
                String[] exonData = exonDataString.split("\t");
                parse(exonData);
            }
            myReader.close();
            return "Successfully loaded file: " + path;
        } catch (Exception e) {
            return "An error occurred while loading file from path: " + path +
                    "\nError message: " + e.getMessage();
        }
    }

    /**
     * Parses array of exon information
     * Adds exon to its correct isoform which is associated with its gene
     * @param exonData array of exon information
     */
    private static void parse(String[] exonData) {
        int chromosome = Integer.parseInt(exonData[0]);
        int start = Integer.parseInt(exonData[1]);
        int stop = Integer.parseInt(exonData[2]);
        String strand = exonData[3];
        String geneId = exonData[4];
        String transcriptID = exonData[5];

        Gene gene;
        Isoform isoform;
        Exon exon;
        if(parsedGenes.containsKey(geneId)) {
            gene = parsedGenes.get(geneId);
        } else {
            gene = new Gene(chromosome, strand);
            parsedGenes.put(geneId, gene);
        }
        if(gene.hasIsoform(transcriptID)) {
            isoform = gene.getIsoform(transcriptID);
        } else {
           isoform = new Isoform();
           gene.addIsoform(transcriptID, isoform);
        }
        exon = new Exon(start, stop);
        isoform.addExon(exon);

        if (gene.getStartNucleotide() > exon.getStartNucleotide())
            gene.setStartNucleotide(exon.getStartNucleotide());
        if (gene.getEndNucleotide() < exon.getEndNucleotide())
            gene.setEndNucleotide(exon.getEndNucleotide());
    }

    public static HashMap<String, Gene> getParsedGenes() {
        return parsedGenes;
    }
}
