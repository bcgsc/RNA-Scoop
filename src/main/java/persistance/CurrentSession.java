package persistance;

import labelset.LabelSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CurrentSession {
    private static String gtfPath;
    private static String matrixPath;
    private static String isoformIDsPath;
    private static Map<LabelSet, String> labelSetPaths;
    private static String embeddingPath;

    /**
     * Saves paths of loaded dataset files. Should be called when new input JSON file is loaded
     */
    public static void saveLoadedPaths(String gtfPath, String matrixPath, String isoformIDsPath,
                                       Map<LabelSet, String> labelSetPaths, String embeddingPath) {
        CurrentSession.gtfPath = gtfPath;
        CurrentSession.matrixPath = matrixPath;
        CurrentSession.isoformIDsPath = isoformIDsPath;
        CurrentSession.labelSetPaths = labelSetPaths;
        CurrentSession.embeddingPath = embeddingPath;
    }

    public static void clearSavedPaths() {
        CurrentSession.gtfPath = null;
        CurrentSession.matrixPath = null;
        CurrentSession.isoformIDsPath = null;
        CurrentSession.labelSetPaths = null;
        CurrentSession.embeddingPath = null;
    }

    /**
     * Saves path to embedding file currently in use
     */
    public static void saveEmbeddingPath(String embeddingPath) {
        CurrentSession.embeddingPath = embeddingPath;
    }

    /**
     * Saves path to label set file loaded/generated in RNA-Scoop
     */
    public static void saveLabelSetPath(LabelSet labelSet, String labelSetPath) {
        labelSetPaths.put(labelSet, labelSetPath);
    }

    public static void clearEmbeddingPath() {
        embeddingPath = null;
    }

    public static void removeLabelSetPath(LabelSet labelSet) {
        labelSetPaths.remove(labelSet);
    }

    public static boolean isLabelSetPathSaved(LabelSet labelSet) {
        return labelSetPaths.containsKey(labelSet);
    }

    public static boolean isEmbeddingSaved() {
        return embeddingPath != null;
    }

    public static String getGTFPath() {
        return gtfPath;
    }

    public static String getMatrixPath() {
        return matrixPath;
    }

    public static String getIsoformIDsPath() {
        return isoformIDsPath;
    }

    public static Map<String, String> getLabelSetPaths() {
        if (labelSetPaths != null) {
            Map<String, String> labelSetNamePathMap = new HashMap<>();
            for (LabelSet labelSet : labelSetPaths.keySet())
                labelSetNamePathMap.put(labelSet.getName(), labelSetPaths.get(labelSet));
            return labelSetNamePathMap;
        }
        return null;
    }

    public static String getEmbeddingPath() {
        return embeddingPath;
    }
}
