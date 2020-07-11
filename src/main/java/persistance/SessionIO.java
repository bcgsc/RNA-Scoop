package persistance;

import mediator.ControllerMediator;
import org.json.JSONObject;
import parser.Parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Class responsible for writing and reading to a JSON file in which
 * session information is saved
 */
public class SessionIO {
    private static final String USERS_HOME_DIR = System.getProperty("user.home");
    private static final String SESSION_DIR_NAME = ".rnascoop";
    private static final String DEFAULT_SESSION_NAME = "prevsession.json";
    private static final String DEFAULT_SESSION_PATH = USERS_HOME_DIR + File.separator + SESSION_DIR_NAME + File.separator + DEFAULT_SESSION_NAME;

    /**
     * Saves information about the user's current session to a JSON file at
     * default path
     */
    public static void saveSession() throws IOException{
        File prevSession = new File(DEFAULT_SESSION_NAME);
        if (!prevSession.exists()) {
            File sessionDirectory = new File(USERS_HOME_DIR + File.separator + SESSION_DIR_NAME);
            sessionDirectory.mkdir();
            new File(DEFAULT_SESSION_PATH);
        }
        saveSessionAtPath(DEFAULT_SESSION_PATH);
    }

    /**
     * Saves information about the user's current session to a JSON file at
     * given path
     */
    public static void saveSessionAtPath(String path) throws IOException {
        JSONObject session = SessionMaker.makeSession();
        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(session.toString());
        fileWriter.flush();
    }

    /**
     * Loads information about the user's previous session from a JSON file at
     * default path
     */
    public static void loadSession() throws IOException {
        loadSessionAtPath(DEFAULT_SESSION_PATH);
    }

    /**
     * Loads information about the user's previous session from a JSON file
     * at given path and restores the saved settings
     */
    public static void loadSessionAtPath(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String prevSessionString = new String(encoded, Charset.defaultCharset());
        JSONObject prevSession = new JSONObject(prevSessionString);
        setSavedSettings(prevSession);
    }

    /**
     * Clears all data from current session (isoforms from GTF, matrix, console messages, path to
     * JSON file...)
     */
    public static void clearCurrentSessionData() {
        Parser.removeParsedGenes();
        ControllerMediator.getInstance().clearGeneSelector();
        ControllerMediator.getInstance().clearTSNEPlot();
        ControllerMediator.getInstance().clearConsole();
        ControllerMediator.getInstance().clearLabelSets();
        ControllerMediator.getInstance().clearPathComboBox();
        ControllerMediator.getInstance().setIsoformIndexMap(null);
        ControllerMediator.getInstance().setCellIsoformExpressionMatrix(null);
    }

    private static void setSavedSettings(JSONObject prevSession) {
        Map settings = prevSession.toMap();
        clearCurrentSessionData();
        ControllerMediator.getInstance().restoreMainFromJSON(settings);
        ControllerMediator.getInstance().restoreConsoleFromJSON(settings);
    }

}
