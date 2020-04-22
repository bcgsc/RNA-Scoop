package parser;

import annotation.Exon;
import annotation.Gene;
import annotation.Isoform;
import com.jujutsu.utils.MatrixUtils;
import exceptions.*;
import mediator.ControllerMediator;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final String GTF_PATH_KEY = "gtf";
    private static final String MATRIX_PATH_KEY = "matrix";
    private static final String ISOFORM_LABELS_PATH_KEY = "isoform ids";
    private static final String CELL_LABELS_PATH_KEY = "cell labels";
    /**
     * Map of all genes parser has parsed so far
     * Key is the gene's ID, value is the gene
     */
    private static HashMap<String, Gene> parsedGenes = new HashMap<>();

    /**
     * Reads in JSON file at given path. File specifies paths to the
     * GTF and t-SNE info. Loads both from it
     */
    public static void loadFiles(String pathToPaths) throws IOException, RNAScoopException {
        byte[] encoded = Files.readAllBytes(Paths.get(pathToPaths));
        String pathsString = new String(encoded, Charset.defaultCharset());
        JSONObject paths = new JSONObject(pathsString);
        GTFLoader.loadGTF((String) paths.get(GTF_PATH_KEY));
        TSNEInfoLoader.loadTSNEInfo((String) paths.get(MATRIX_PATH_KEY),
                                    (String) paths.get(ISOFORM_LABELS_PATH_KEY),
                                    (String) paths.get(CELL_LABELS_PATH_KEY));
    }

    /**
     * Removes data from previously parsed GTF file
     */
    public static void removeParsedGenes() {
        parsedGenes.clear();
    }

    public static HashMap<String, Gene> getParsedGenesMap() {
        return parsedGenes;
    }

    private static class GTFLoader {

        public static void loadGTF(String pathToGTF) throws IOException, RNAScoopException {
            removeParsedGenes();
            BufferedReader reader = new BufferedReader(new FileReader(pathToGTF));
            String currentLine;
            int lineNumber = 0;
            while ((currentLine = reader.readLine()) != null) {
                ++lineNumber;
                String dataString = removeComments(currentLine);
                String[] data = dataString.split("\t");
                if (data.length == 9 && isExonData(data)) {
                    ExonDataParser.parse(data, lineNumber);
                }
            }
            reader.close();
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
            private static String isoformID;
            private static String geneName;
            private static String isoformName;

            private static void clearData() {
                exonData = null;
                chromosome = null;
                endNucleotide = 0;
                startNucleotide = 0;
                strand = null;
                geneID = null;
                isoformID = null;
                geneName = null;
                isoformName = null;
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
                                isoformID = attributeValue;
                                break;
                            case "gene_name":
                                geneName = attributeValue;
                                break;
                            case "transcript_name":
                                isoformName = attributeValue;
                                break;
                        }
                    }
                }
                if (geneID == null || isoformID == null)
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
                if(gene.hasIsoform(isoformID)) {
                    isoform = gene.getIsoform(isoformID);
                } else {
                    isoform = new Isoform(isoformID, gene);
                    gene.addIsoform(isoformID, isoform);
                }
                exon = new Exon(startNucleotide, endNucleotide);
                isoform.addExon(exon);

                if (geneName != null && gene.getName() == null)
                    gene.setName(geneName);
                if (isoformName != null && isoform.getName() == null)
                    isoform.setName(isoformName);
                if (gene.getStartNucleotide() > exon.getStartNucleotide())
                    gene.setStartNucleotide(exon.getStartNucleotide());
                if (gene.getEndNucleotide() < exon.getEndNucleotide())
                    gene.setEndNucleotide(exon.getEndNucleotide());
            }
        }
    }

    private static class TSNEInfoLoader {

        public static void loadTSNEInfo(String pathToMatrix, String pathToIsoformLabels, String pathToCellLabels) throws IOException, RNAScoopException {
            ControllerMediator.getInstance().setTSNEPlotInfo(getCellIsoformExpressionMatrix(pathToMatrix),
                                                             getIsoformIndexMap(pathToIsoformLabels),
                                                             getCellLabels(pathToCellLabels));
        }

        /**
         * Creates a cell isoform expression matrix by reading the given data file
         * Throws exceptions if size of the matrix is 0, or if the matrix contains negative
         * expression values
         */
        private static double[][] getCellIsoformExpressionMatrix(String pathToMatrix) throws TSNEMatrixSizeZeroException, TSNENegativeExpressionInMatrixException {
            File matrixFile = new File(pathToMatrix);
            double[][] cellIsoformExpressionMatrix = MatrixUtils.simpleRead2DMatrix(matrixFile, "\t");
            if (cellIsoformExpressionMatrix.length == 0)
                throw new TSNEMatrixSizeZeroException();

            double[] negativeExpressions = Arrays.stream(cellIsoformExpressionMatrix).flatMapToDouble(Arrays::stream).filter(expression -> expression < 0).toArray();
            if (negativeExpressions.length > 0)
                throw new TSNENegativeExpressionInMatrixException();

            return cellIsoformExpressionMatrix;
        }

        /**
         * Creates map that maps each isoform ID to its column number in the matrix (e.g. if the
         * first column represents IsoformA, IsoformA's ID will be mapped to 0)
         */
        private static HashMap<String, Integer> getIsoformIndexMap(String pathToIsoformLabels) throws IOException {
            File isoformLabelsFile = new File(pathToIsoformLabels);
            BufferedReader reader= new BufferedReader(new FileReader(isoformLabelsFile));
            HashMap<String, Integer> isoformIndexMap = new HashMap<>();
            String currentLabel;
            int index = 0;
            while ((currentLabel = reader.readLine()) != null) {
                isoformIndexMap.put(currentLabel, index);
                index++;
            }
            return isoformIndexMap;
        }

        /**
         * Creates list of groups each cell in matrix belongs to. If cell represented by the first
         * row in the matrix is in group "T Cells", cellLabels[0] = "T Cells"
         */
        private static ArrayList<String> getCellLabels(String pathToCellLabels) throws IOException {
            File cellLabelsFile = new File(pathToCellLabels);
            BufferedReader reader= new BufferedReader(new FileReader(cellLabelsFile));
            ArrayList<String> cellLabels = new ArrayList<>();
            String currentLabel;
            while ((currentLabel = reader.readLine()) != null) {
                cellLabels.add(currentLabel);
            }
            return cellLabels;
        }
    }
}
