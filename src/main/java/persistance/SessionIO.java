package persistance;

import javafx.application.Platform;
import mediator.ControllerMediator;
import org.json.JSONObject;
import parser.Parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * Class responsible for writing and reading to a JSON file in which
 * session information is saved
 */
public class SessionIO {

    /**
     * Saves information about the user's current session to a JSON file at
     * given path
     */
    public static void saveSessionAtPath(String path) throws IOException {
        int extensionIndex = path.lastIndexOf(".");
        String pathToDir = path.substring(0, (extensionIndex == -1)? path.length() : extensionIndex) + "_files";
        JSONObject session = SessionMaker.makeSession(pathToDir);
        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(session.toString());
        fileWriter.close();
    }

    /**
     * If file exists, loads information about the user's previous session from a
     * JSON file at given path and restores the saved settings
     */
    public static void loadSessionAtPath(String path) throws IOException {
        disableAssociatedFunctionality();
        clearCurrentSessionData();
        ControllerMediator.getInstance().addConsoleMessage("Loading session...");
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String prevSessionString = new String(encoded, Charset.defaultCharset());
        JSONObject prevSession = new JSONObject(prevSessionString);
        try {
            Thread sessionLoader = new Thread(new SessionLoader(prevSession));
            sessionLoader.start();
        } catch (Exception e) {
            enableAssociatedFunctionality();
            ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
        }
    }

    /**
     * Clears all data from current session (isoforms from GTF, matrix, console messages, path to
     * JSON file...)
     */
    public static void clearCurrentSessionData() {
        ControllerMediator.getInstance().clearGeneSelector();
        ControllerMediator.getInstance().clearCellPlot();
        ControllerMediator.getInstance().clearLabelSets();
        ControllerMediator.getInstance().setIsoformIndexMap(null);
        ControllerMediator.getInstance().setCellIsoformExpressionMatrix(null);
        ControllerMediator.getInstance().setEmbedding(null);
        CurrentSession.clearSavedPaths();
    }

    private static void disableAssociatedFunctionality() {
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableGeneSelector();
        ControllerMediator.getInstance().disableClusterView(true);
        ControllerMediator.getInstance().disableClusterViewSettings();
        ControllerMediator.getInstance().disableTPMGradientAdjuster();
        ControllerMediator.getInstance().disableLabelSetManager();
        ControllerMediator.getInstance().disableGeneFilterer();
        // doesn't disable add label set view, because main should be disabled
        // (and therefore user can't load sessions) when that view is active
    }

    private static void enableAssociatedFunctionality() {
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableClusterView();
        ControllerMediator.getInstance().enableClusterViewSettings();
        ControllerMediator.getInstance().enableTPMGradientAdjuster();
        ControllerMediator.getInstance().enableLabelSetManager();
        ControllerMediator.getInstance().enableGeneFilterer();
    }

    private static class SessionLoader implements Runnable {
        private JSONObject prevSession;
        private boolean restoredAllButCellPlotAndFiltering;

        public SessionLoader(JSONObject prevSession) {
            this.prevSession = prevSession;
            restoredAllButCellPlotAndFiltering = false;
        }

        @Override
        public void run() {
            if (prevSession.has(SessionMaker.GTF_PATH_KEY)) // if there is no loaded gtf file in the prev session, no data was loaded
                loadPreviousSessionData();
            restoreSession();
            Platform.runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Successfully loaded session"));
            enableAssociatedFunctionality();
        }

        private void loadPreviousSessionData() {
            String gtf = prevSession.getString(SessionMaker.GTF_PATH_KEY);
            String matrix = prevSession.getString(SessionMaker.MATRIX_PATH_KEY);
            String isoformLabels = prevSession.getString(SessionMaker.ISOFORM_LABELS_PATH_KEY);
            Collection<String> cellLabels = (List<String>)(List<?>) prevSession.getJSONArray(SessionMaker.CELL_LABELS_PATH_KEY).toList();
            String embedding = (prevSession.has(SessionMaker.EMBEDDING_PATH_KEY))? prevSession.getString(SessionMaker.EMBEDDING_PATH_KEY) : null;
            Parser.loadPreviousSessionData(gtf, matrix, isoformLabels, cellLabels, embedding);
        }

        private void restoreSession() {
            Platform.runLater(() -> {
                ControllerMediator.getInstance().restoreMainFromPrevSession(prevSession);
                ControllerMediator.getInstance().restoreTPMGradientFromPrevSession(prevSession);
                ControllerMediator.getInstance().restoreClusterViewSettingsFromPrevSession(prevSession);
                ControllerMediator.getInstance().restoreImageExporterFromPrevSession(prevSession);
                ControllerMediator.getInstance().restoreLabelSetManagerFromPrevSession(prevSession);
                ControllerMediator.getInstance().restoreGeneSelectorFromPrevSession(prevSession);
                ControllerMediator.getInstance().restoreIsoformViewFromPrevSession(prevSession);
                restoredAllButCellPlotAndFiltering = true;
            });
            while (!restoredAllButCellPlotAndFiltering)
            ControllerMediator.getInstance().restoreClusterViewFromPrevSession(prevSession);
            ControllerMediator.getInstance().restoreGeneFiltererFromPrevSession(prevSession);
        }
    }
}
