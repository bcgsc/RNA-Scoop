package json;

import org.json.JSONObject;
import ui.mediator.ControllerMediator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class responsible for writing and reading to a JSON file in which the user's
 * previous session information is saved
 */
public class SessionIO {
    private static final String PREV_SESSION_PATH = "./src/json/prevsession.json";
    private static final File PREV_SESSION_FILE = new File(PREV_SESSION_PATH);

    /**
     * Saves information about the user's current session to a JSON file
     */
    public static void saveSession() throws IOException{
        JSONObject session = SessionMaker.makeSession();
        FileWriter fileWriter = new FileWriter(PREV_SESSION_PATH);
        fileWriter.write(session.toString());
        fileWriter.flush();
    }

    /**
     * Loads information about the user's previous session from a JSON file
     * and restores the saved settings
     */
    public static void loadSession() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(PREV_SESSION_FILE.getPath()));
        String prevSessionString = new String(encoded, Charset.defaultCharset());
        JSONObject prevSession = new JSONObject(prevSessionString);
        SessionParser.setSavedSettings(prevSession);
    }

    private static class SessionMaker {
        private static final String PATH_KEY = "path";
        private static final String ISOFORM_PLOT_OPEN_KEY = "isoform plot open";
        private static final String TSNE_PLOT_OPEN_KEY = "t-sne open";
        private static final String CONSOLE_OPEN_KEY = "console open";

        /**
         * Creates a session containing:
         *   - the current loaded path
         *   - whether the isoform plot, t-sne plot and console are opened or closed
         */
        private static JSONObject makeSession() {
            JSONObject session = new JSONObject();
            session.put(PATH_KEY, ControllerMediator.getInstance().getCurrentLoadedPath());
            session.put(ISOFORM_PLOT_OPEN_KEY, ControllerMediator.getInstance().isIsoformPlotOpen());
            session.put(TSNE_PLOT_OPEN_KEY, ControllerMediator.getInstance().isTSNEPlotOpen());
            session.put(CONSOLE_OPEN_KEY, ControllerMediator.getInstance().isConsoleOpen());
            return session;
        }
    }

    private static class SessionParser {
        private static void setSavedSettings(JSONObject prevSession) {
            restoreComboBoxPath(prevSession);
            restoreIsoformPlotSettings(prevSession);
            restoreTSNEPlotSettings(prevSession);
            restoreConsoleSettings(prevSession);
        }

        /**
         * If the loaded path from the previous session has been saved, sets the path combo box's value
         * to it
         */
        private static void restoreComboBoxPath(JSONObject prevSession) {
            if(prevSession.has(SessionMaker.PATH_KEY))
                ControllerMediator.getInstance().setPathComboBoxValue(prevSession.getString(SessionMaker.PATH_KEY));
        }

        /**
         * If the isoform plot was closed in the previous session, closes the isoform plot if it's not
         * already closed
         *
         * If the isoform plot was open in the previous session, opens the isoform plot if it's not
         * already open
         */
        private static void restoreIsoformPlotSettings(JSONObject prevSession) {
            boolean prevIsoformPlotOpen = prevSession.getBoolean(SessionMaker.ISOFORM_PLOT_OPEN_KEY);
            if(!prevIsoformPlotOpen && ControllerMediator.getInstance().isIsoformPlotOpen())
                ControllerMediator.getInstance().closeIsoformPlot();
            else if(prevIsoformPlotOpen && !ControllerMediator.getInstance().isIsoformPlotOpen())
                ControllerMediator.getInstance().openIsoformPlot();
        }

        /**
         * If the t-SNE plot was closed in the previous session, closes the t-SNE plot if it's not
         * already closed
         *
         * If the t-SNE plot was open in the previous session, opens the t-SNE plot if it's not
         * already open
         */
        private static void restoreTSNEPlotSettings(JSONObject prevSession) {
            boolean prevTSNEPlotOpen = prevSession.getBoolean(SessionMaker.TSNE_PLOT_OPEN_KEY);
            if(!prevTSNEPlotOpen && ControllerMediator.getInstance().isTSNEPlotOpen())
                ControllerMediator.getInstance().closeTSNEPlot();
            else if(prevTSNEPlotOpen && !ControllerMediator.getInstance().isTSNEPlotOpen())
                ControllerMediator.getInstance().openTSNEPlot();
        }

        /**
         * If the console was closed in the previous session, closes the console if it's not
         * already closed
         *
         * If the console was open in the previous session, opens the console if it's not
         * already open
         */
        private static void restoreConsoleSettings(JSONObject prevSession) {
            boolean prevConsoleOpen = prevSession.getBoolean(SessionMaker.CONSOLE_OPEN_KEY);
            if(!prevConsoleOpen && ControllerMediator.getInstance().isConsoleOpen())
                ControllerMediator.getInstance().closeConsole();
            else if(prevConsoleOpen && !ControllerMediator.getInstance().isConsoleOpen())
                ControllerMediator.getInstance().openConsole();
        }

    }
}
