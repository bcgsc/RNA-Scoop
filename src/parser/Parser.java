package parser;

import exceptions.*;
import parser.data.Exon;
import parser.data.Gene;
import parser.data.Isoform;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    /**
     * Map of all genes parser has parsed so far
     * Key is the gene's ID, value is the gene
     */
    private static HashMap<String, Gene> parsedGenes;

    /**
     * Reads in file at given path and parses each line
     * @param path path to the file
     */
    public static void readFile(String path) throws IOException, RNAScoopException {
        removeParsedGenes();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String currentLine;
        int lineNumber = 0;
        while ((currentLine = reader.readLine()) != null) {
            ++lineNumber;
            String dataString = removeComments(currentLine);
            String[] data = dataString.split("\t");
            if (data.length < 9)
                throw new GTFFileMissingColumnsException();
            if (isExonData(data)) {
                ExonDataParser.parse(data, lineNumber);
            }
        }
        reader.close();
    }

    /**
     * Removes data from previously parsed GTF file
     */
    public static void removeParsedGenes() {
        parsedGenes = new HashMap<>();
    }

    /**
     * Returns true if feature field (in the line of the GTF file data represents) is "exon"
     * @param data represents a line in a GTF file, each element is the element of a column
     */
    private static boolean isExonData(String[] data) {
        return data[2].equals("exon");
    }

    /**
     * Remove all characters following "#" (GTF comment symbol)
     */
    private static String removeComments(String exonDataString) {
        return exonDataString.split("#")[0];
    }

    private static class ExonDataParser {

        private static Pattern p = Pattern.compile("\\s*(\\S+)\\s*\"(\\S+)\"\\s*;\\s*");

        private static String[] exonData;
        private static int lineNumber;
        private static String chromosome;
        private static int endNucleotide;
        private static int startNucleotide;
        private static String strand;
        // attribute values
        private static String geneID;
        private static String transcriptID;

        private static void clearData() {
            exonData = null;
            chromosome = null;
            endNucleotide = 0;
            startNucleotide = 0;
            strand = null;
            geneID = null;
            transcriptID = null;
        }

        /**
         * Parses array of exon information
         * Adds exon to its correct isoform which is associated with its gene
         * @param exonData array of exon information
         */
        private static void parse(String[] exonData, int lineNumber) throws RNAScoopException {
            ExonDataParser.exonData = exonData;
            ExonDataParser.lineNumber = lineNumber;

            setChromosome();
            setEndNucleotide();
            setStartNucleotide();
            setStrand();
            setAttributeValues();

            storeExonInformation();
            clearData();
        }

        private static void storeExonInformation() {
            Gene gene;
            Isoform isoform;
            Exon exon;
            if(parsedGenes.containsKey(geneID)) {
                gene = parsedGenes.get(geneID);
            } else {
                gene = new Gene(chromosome, strand);
                parsedGenes.put(geneID, gene);
            }
            if(gene.hasIsoform(transcriptID)) {
                isoform = gene.getIsoform(transcriptID);
            } else {
                isoform = new Isoform();
                gene.addIsoform(transcriptID, isoform);
            }
            exon = new Exon(startNucleotide, endNucleotide);
            isoform.addExon(exon);

            if (gene.getStartNucleotide() > exon.getStartNucleotide())
                gene.setStartNucleotide(exon.getStartNucleotide());
            if (gene.getEndNucleotide() < exon.getEndNucleotide())
                gene.setEndNucleotide(exon.getEndNucleotide());
        }

        private static void setAttributeValues() throws GTFMissingInfoException {
            Matcher m = p.matcher(exonData[8]);
            while(m.find()) {
                if (m.group(1).equals("gene_id"))
                    geneID = m.group(2);
                else if (m.group(1).equals("transcript_id"))
                    transcriptID = m.group(2);
            }
            if (geneID == null || transcriptID == null)
                throw new GTFMissingInfoException(lineNumber);
        }

        private static void setStrand() {
            strand = exonData[6];
        }

        private static void setChromosome() {
            chromosome = exonData[0];
        }

        private static void setStartNucleotide() throws GTFInvalidStartNucleotideException {
            try {
                startNucleotide = Integer.parseInt(exonData[3]);
            } catch (NumberFormatException e) {
                removeParsedGenes();
                throw new GTFInvalidStartNucleotideException(lineNumber);
            }
        }

        private static void setEndNucleotide() throws GTFInvalidEndNucleotideException {
            try {
                endNucleotide = Integer.parseInt(exonData[4]);
            } catch (NumberFormatException e) {
                removeParsedGenes();
                throw new GTFInvalidEndNucleotideException(lineNumber);
            }
        }
    }

    public static HashMap<String, Gene> getParsedGenes() {
        return parsedGenes;
    }
}
