package parser;

import annotation.Exon;
import annotation.Gene;
import annotation.Isoform;
import exceptions.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    /**
     * Map of all genes main.java.parser has parsed so far
     * Key is the gene's ID, value is the gene
     */
    private static HashMap<String, Gene> parsedGenes = new HashMap<>();

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
                throw new GTFFileMissingColumnsException(lineNumber);
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

    public static HashMap<String, Gene> getParsedGenesMap() {
        return parsedGenes;
    }

    /**
     * Remove all characters following "#" (GTF comment symbol)
     */
    private static String removeComments(String exonDataString) {
        return exonDataString.split("#")[0];
    }


    /**
     * Returns true if feature field (in the line of the GTF file data represents) is "exon"
     * @param data represents a line in a GTF file, each element is the element of a column
     */
    private static boolean isExonData(String[] data) {
        return data[2].equals("exon");
    }

    private static class ExonDataParser {

        private static Pattern attributePattern = Pattern.compile("\\s*(\\S+)\\s*\"(\\S+)\"\\s*");

        private static String[] exonData;
        private static int lineNumber;
        private static String chromosome;
        private static int endNucleotide;
        private static int startNucleotide;
        private static String strand;
        // attribute values
        private static String geneID;
        private static String transcriptID;
        private static String geneName;
        private static String transcriptName;

        private static void clearData() {
            exonData = null;
            chromosome = null;
            endNucleotide = 0;
            startNucleotide = 0;
            strand = null;
            geneID = null;
            transcriptID = null;
            geneName = null;
            transcriptName = null;
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
            setStartNucleotide();
            setEndNucleotide();
            setStrand();
            setAttributeValues();

            storeExonInformation();
            clearData();
        }

        private static void setChromosome() {
            chromosome = exonData[0];
        }

        private static void setStartNucleotide() throws GTFInvalidStartNucleotideException {
            try {
                startNucleotide = Integer.parseInt(exonData[3]);
            } catch (NumberFormatException e) {
                throw new GTFInvalidStartNucleotideException(lineNumber);
            }
        }

        private static void setEndNucleotide() throws GTFInvalidEndNucleotideException {
            try {
                endNucleotide = Integer.parseInt(exonData[4]);
            } catch (NumberFormatException e) {
                throw new GTFInvalidEndNucleotideException(lineNumber);
            }
        }

        private static void setStrand() {
            strand = exonData[6];
        }

        private static void setAttributeValues() throws GTFMissingAttributesInfoException {
            for (String p : exonData[8].split(";")) {
                Matcher m = attributePattern.matcher(p);
                if (m.matches()) {
                    String attributeName = m.group(1);
                    String attributeValue = m.group(2);
                    switch (attributeName) {
                        case "gene_id":
                            geneID = attributeValue;
                            break;
                        case "transcript_id":
                            transcriptID = attributeValue;
                            break;
                        case "gene_name":
                            geneName = attributeValue;
                            break;
                        case "transcript_name":
                            transcriptName = attributeValue;
                            break;
                    }
                }
            }
            if (geneID == null || transcriptID == null)
                throw new GTFMissingAttributesInfoException(lineNumber);
        }

        private static void storeExonInformation() {
            Gene gene;
            Isoform isoform;
            Exon exon;
            if(parsedGenes.containsKey(geneID)) {
                gene = parsedGenes.get(geneID);
            } else {
                gene = new Gene(geneID, chromosome, strand);
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

            if (geneName != null && gene.getName() == null)
                gene.setName(geneName);
            if (transcriptName != null && isoform.getName() == null)
                isoform.setName(transcriptName);
            if (gene.getStartNucleotide() > exon.getStartNucleotide())
                gene.setStartNucleotide(exon.getStartNucleotide());
            if (gene.getEndNucleotide() < exon.getEndNucleotide())
                gene.setEndNucleotide(exon.getEndNucleotide());
        }
    }
}
