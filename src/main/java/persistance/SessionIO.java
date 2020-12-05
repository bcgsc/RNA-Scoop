package persistance;

import controller.ConsoleController;
import controller.clusterview.ClusterViewController;
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

    public static final String SESSION_FILE_NAME = "session.json";

    /**
     * Saves information about the user's current session to a JSON file at
     * given path
     */
    public static void saveSessionAtPath(String pathToDir) throws IOException {
        JSONObject session = SessionMaker.makeSession(pathToDir);
        FileWriter fileWriter = new FileWriter(pathToDir + File.separator + SESSION_FILE_NAME);
        fileWriter.write(session.toString());
        fileWriter.close();
    }

    /**
     * If file exists, loads information about the user's previous session from a
     * JSON file at given path and restores the saved settings
     */
    public static void loadSessionAtPath(String path) throws IOException {
        if (new File(path + File.separator + SESSION_FILE_NAME).exists()) {
            disableAssociatedFunctionality();
            clearCurrentSessionData();
            ControllerMediator.getInstance().addConsoleMessage("Loading session...");
            byte[] encoded = Files.readAllBytes(Paths.get(path + File.separator + SESSION_FILE_NAME));
            String prevSessionString = new String(encoded, Charset.defaultCharset());
            JSONObject prevSession = new JSONObject(prevSessionString);
            try {
                Thread sessionLoader = new Thread(new SessionLoader(prevSession));
                sessionLoader.start();
            } catch (Exception e) {
                enableAssociatedFunctionality();
                ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
            }
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("Could not find file with name " + SESSION_FILE_NAME + " in selected directory");
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
        private boolean restoredIsoformView;

        public SessionLoader(JSONObject prevSession) {
            this.prevSession = prevSession;
            restoredIsoformView = false;
        }

        @Override
        public void run() {
            loadPreviousSessionData();
            restoreSession();
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
            ControllerMediator.getInstance().restoreMainFromPrevSession(prevSession);
            ControllerMediator.getInstance().restoreTPMGradientFromPrevSession(prevSession);
            ControllerMediator.getInstance().restoreClusterViewSettingsFromPrevSession(prevSession);
            ControllerMediator.getInstance().restoreImageExporterFromPrevSession(prevSession);
            ControllerMediator.getInstance().restoreLabelSetManagerFromPrevSession(prevSession);
            Platform.runLater(() -> {
                ControllerMediator.getInstance().restoreGeneSelectorFromPrevSession(prevSession);
                ControllerMediator.getInstance().restoreIsoformViewFromPrevSession(prevSession);
                restoredIsoformView = true;
            });
            while (!restoredIsoformView)
            ControllerMediator.getInstance().restoreClusterViewFromPrevSession(prevSession);
            ControllerMediator.getInstance().restoreGeneFiltererFromPrevSession(prevSession);
        }
    }
}
