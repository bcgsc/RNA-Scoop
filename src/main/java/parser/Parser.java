package parser;

import annotation.Exon;
import annotation.Gene;
import annotation.Isoform;
import exceptions.*;
import labelset.Cluster;
import labelset.LabelSet;
import mediator.ControllerMediator;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static javafx.application.Platform.runLater;

public class Parser {
    private static final String GTF_PATH_KEY = "gtf";
    private static final String MATRIX_PATH_KEY = "matrix";
    private static final String ISOFORM_LABELS_PATH_KEY = "isoform_ids";
    private static final String CELL_LABELS_PATH_KEY = "cell_labels";
    private static final String EMBEDDING_PATH_KEY = "embedding";
    private static final String GZIP_EXTENSION = ".gz";

    /**
     * Reads in JSON file at given path. File specifies paths to the
     * GTF and cell plot info. Loads both from it
     */
    public static boolean loadJSONFile(String pathToPaths)  {
        runLater(Parser::clearProgram);
        try {
            runLater(() ->  ControllerMediator.getInstance().addConsoleMessage("Loading file from path: " + pathToPaths));
            byte[] encoded = Files.readAllBytes(Paths.get(pathToPaths));
            String pathsString = new String(encoded, Charset.defaultCharset());
            JSONObject paths = new JSONObject(pathsString);
            runLater(() ->  ControllerMediator.getInstance().addConsoleMessage("Parsing GTF file..."));
            GTFLoader.loadGTF((String) paths.get(GTF_PATH_KEY));
            runLater(() ->  ControllerMediator.getInstance().addConsoleMessage("Parsing matrix files..."));
            CellPlotInfoLoader.loadCellPlotInfo((String) paths.get(MATRIX_PATH_KEY),
                    (String) paths.get(ISOFORM_LABELS_PATH_KEY),
                    (String) paths.get(CELL_LABELS_PATH_KEY),
                    paths.has(EMBEDDING_PATH_KEY) ? (String) paths.get(EMBEDDING_PATH_KEY) : null);
            runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Successfully loaded file from path: " + pathToPaths));
            return true;
        } catch (RNAScoopException e){
            runLater(Parser::clearLoadedData);
            runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage()));
            return false;
        } catch (Exception e) {
            runLater(Parser::clearLoadedData);
            runLater(() -> ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e));
            return false;
        }
    }

    public static Set<String> loadGeneSelectionFile(File geneSelectionFile) {
        Set<String> genesToSelect = new HashSet<>();
        String currentLabel;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(geneSelectionFile));
            while ((currentLabel = reader.readLine()) != null) {
                genesToSelect.add(currentLabel);
            }
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
        }

        return genesToSelect;
    }

    /**
     * Clears gene selector, cell plot, clears t-SNE plot data, and label sets
     * (basically clears everything that should be cleared when loading a new dataset)
     */
    private static void clearProgram() {
        ControllerMediator.getInstance().clearGeneSelector();
        ControllerMediator.getInstance().clearCellPlot();
        ControllerMediator.getInstance().setIsoformIndexMap(null);
        ControllerMediator.getInstance().setCellIsoformExpressionMatrix(null);
        ControllerMediator.getInstance().setEmbedding(null);
        ControllerMediator.getInstance().clearLabelSets();
    }

    private static void clearLoadedData() {
        GTFLoader.removeParsedGenes();
        ControllerMediator.getInstance().updateGenesTable(new ArrayList<>());
        ControllerMediator.getInstance().setCellIsoformExpressionMatrix(null);
        ControllerMediator.getInstance().setIsoformIndexMap(null);
        ControllerMediator.getInstance().setEmbedding(null);
        ControllerMediator.getInstance().clearLabelSets();
    }

    private static class GTFLoader {
        /**
         * Map of all genes parser has parsed so far
         * Key is the gene's ID, value is the gene
         */
        private static HashMap<String, Gene> parsedGenes = new HashMap<>();

        public static void loadGTF(String pathToGTF) throws IOException, RNAScoopException {
            BufferedReader reader;

            if (pathToGTF.toLowerCase().endsWith(GZIP_EXTENSION)) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pathToGTF))));
            }
            else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(pathToGTF)));
            }

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
            ArrayList<Gene> geneList = new ArrayList<>(parsedGenes.values());
            runLater(() -> ControllerMediator.getInstance().updateGenesTable(geneList));
            removeParsedGenes();
            reader.close();
        }

        /**
         * Removes data from previously parsed GTF file
         */
        public static void removeParsedGenes() {
            parsedGenes.clear();
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

    private static class CellPlotInfoLoader {

        public static void loadCellPlotInfo(String pathToMatrix, String pathToIsoformLabels, String pathToCellLabels, String pathToEmbedding) throws IOException, RNAScoopException {
            double[][] cellIsoformExpressionMatrix = getCellIsoformExpressionMatrix(pathToMatrix);
            HashMap<String, Integer> isoformIndexMap = getIsoformIndexMap(pathToIsoformLabels);

            if (isoformIndexMap.size() != cellIsoformExpressionMatrix[0].length)
                throw new ColumnLabelsLengthException();

            LabelSet cellLabels = getCellLabels(pathToCellLabels);

            if (cellLabels.getNumCellsInLabelSet() != cellIsoformExpressionMatrix.length)
                throw new RowLabelsLengthException();

            if (pathToEmbedding != null) {
                double[][] embedding = getEmbedding(pathToEmbedding);


                if (embedding.length != cellIsoformExpressionMatrix.length)
                    throw new EmbeddingLengthException();
                ControllerMediator.getInstance().setEmbedding(embedding);
            }

            ControllerMediator.getInstance().setCellIsoformExpressionMatrix(cellIsoformExpressionMatrix);
            ControllerMediator.getInstance().setIsoformIndexMap(isoformIndexMap);
            ControllerMediator.getInstance().addLabelSet(cellLabels);
        }

        /**
         * Creates a cell isoform expression matrix by reading the given data file
         * Throws exceptions if size of the matrix is 0, or if the matrix contains negative
         * expression values
         */
        private static double[][] getCellIsoformExpressionMatrix(String pathToMatrix) throws MatrixSizeZeroException, NegativeExpressionInMatrixException {
            double[][] cellIsoformExpressionMatrix = parse2DMatrix(pathToMatrix, "\t");
            if (cellIsoformExpressionMatrix.length == 0)
                throw new MatrixSizeZeroException();

            return cellIsoformExpressionMatrix;
        }

        /**
         * Parse a 2D matrix text file. Code was partially derived from TSNE-Java.
         * @param pathToMatrix input matrix path
         * @param columnDelimiter column delimiter string
         * @return 2D double array
         * @throws NegativeExpressionInMatrixException a negative expression value is found
         */
        public static double[][] parse2DMatrix(String pathToMatrix, String columnDelimiter) throws NegativeExpressionInMatrixException {
            List<double[]> rows = new ArrayList<>();

            try {
                BufferedReader reader;

                if (pathToMatrix.toLowerCase().endsWith(GZIP_EXTENSION)) {
                    reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pathToMatrix))));
                }
                else {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(pathToMatrix)));
                }

                String line;
                double val;
                while ((line = reader.readLine()) != null && !line.matches("\\s*")) {
                    String[] cols = line.trim().split(columnDelimiter);
                    double [] row = new double[cols.length];
                    for (int j = 0; j < cols.length; j++) {
                        if(!(cols[j].length()==0)) {
                            val = Double.parseDouble(cols[j].trim());
                            // @TODO: handle non-numerical entries

                            if (val < 0) {
                                throw new NegativeExpressionInMatrixException();
                            }

                            row[j] = val;
                        }
                    }
                    rows.add(row);
                }
                reader.close();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }

            double[][] array = new double[rows.size()][];
            int currentRow = 0;
            for (double[] ds : rows) {
                array[currentRow++] = ds;
            }

            return array;
        }

        /**
         * Creates map that maps each isoform ID to its column number in the matrix (e.g. if the
         * first column represents IsoformA, IsoformA's ID will be mapped to 0)
         */
        private static HashMap<String, Integer> getIsoformIndexMap(String pathToIsoformLabels) throws IOException, DuplicateColumnLabelException {
            File isoformLabelsFile = new File(pathToIsoformLabels);
            BufferedReader reader= new BufferedReader(new FileReader(isoformLabelsFile));
            HashMap<String, Integer> isoformIndexMap = new HashMap<>();
            String currentLabel;
            int index = 0;
            while ((currentLabel = reader.readLine()) != null) {
                if (isoformIndexMap.containsKey(currentLabel))
                    throw new DuplicateColumnLabelException(currentLabel);
                isoformIndexMap.put(currentLabel, index);
                index++;
            }
            return isoformIndexMap;
        }

        /**
         * Creates a label set from given cell labels file. Label set is made based on map of cells
         * (represented by their numbers) and the clusters they belong to.
         * If the first line of the cell labels file says "T Cells", the cell represented by the first
         * row of the matrix should be in the cluster labelled "T Cells". The map from which the
         * label set is produced will map 0 to a cluster with label "T Cells"
         */
        private static LabelSet getCellLabels(String pathToCellLabels) throws IOException {
            Map<Integer, Cluster> cellNumberClusterMap = new LinkedHashMap<>();
            Map<String, Cluster> clusterMap = new HashMap<>();

            String currentLabel;
            Cluster cluster;
            int cellNumber = 0;

            File cellLabelsFile = new File(pathToCellLabels);
            BufferedReader reader= new BufferedReader(new FileReader(cellLabelsFile));
            while ((currentLabel = reader.readLine()) != null) {
                if (clusterMap.containsKey(currentLabel)) {
                    cluster = clusterMap.get(currentLabel);
                } else {
                    cluster = new Cluster(currentLabel);
                    clusterMap.put(currentLabel, cluster);
                }
                cellNumberClusterMap.put(cellNumber, cluster);
                cellNumber++;
            }
            return new LabelSet(cellNumberClusterMap);
        }

        private static double[][] getEmbedding(String pathToEmbedding) throws IOException, EmbeddingColumnsException, EmbeddingNotNumberException {
            String currentLabel;
            int lineNumber = 1;
            ArrayList<double[]> embeddingArrayList = new ArrayList<>();

            File embeddingFile = new File(pathToEmbedding);
            BufferedReader reader= new BufferedReader(new FileReader(embeddingFile));
            while ((currentLabel = reader.readLine()) != null) {
                String[] cellCoordsString = currentLabel.split("\t");

                if (cellCoordsString.length != 2)
                    throw new EmbeddingColumnsException(lineNumber);

                try {
                    double[] cellCoords = Arrays.stream(cellCoordsString).mapToDouble(Double::parseDouble).toArray();
                    embeddingArrayList.add(cellCoords);
                } catch (NumberFormatException e) {
                    throw new EmbeddingNotNumberException("(" + cellCoordsString[0] + ", " + cellCoordsString[1] + ")", lineNumber);
                }
                lineNumber++;
            }

            double[][] embedding = new double[embeddingArrayList.size()][];
            for (int i = 0; i < embeddingArrayList.size(); i++)
                embedding[i] = embeddingArrayList.get(i);
            return embedding;
        }

    }
}
