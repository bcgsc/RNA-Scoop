package persistance;

import controller.ConsoleController;
import mediator.ControllerMediator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SessionMaker {
    public static final String PATH_KEY = "path";
    public static final String ISOFORM_PLOT_OPEN_KEY = "isoform open";
    public static final String TSNE_PLOT_OPEN_KEY = "t-sne open";
    public static final String CONSOLE_OPEN_KEY = "console open";
    public static final String REVERSE_COMPLEMENT_KEY = "rev complement";
    public static final String SHOW_NAMES_KEY = "show names";
    public static final String CONSOLE_MESSAGES_KEY = "console messages";
    public static final String MESSAGE_TEXT_KEY = "message text";
    public static final String MESSAGE_IS_ERROR_KEY = "message is error";

    /**
     * Creates a session containing:
     *   - the current loaded path
     *   - whether the isoform plot, t-sne plot and console are opened or closed
     *   - whether the reverse complement option is selected
     *   - whether the show names option is selected
     *   - the console messages
     */
    public static JSONObject makeSession() {
        JSONObject session = new JSONObject();
        session.put(PATH_KEY, ControllerMediator.getInstance().getCurrentLoadedPath());
        session.put(ISOFORM_PLOT_OPEN_KEY, ControllerMediator.getInstance().isIsoformPlotOpen());
        session.put(TSNE_PLOT_OPEN_KEY, ControllerMediator.getInstance().isTSNEPlotOpen());
        session.put(CONSOLE_OPEN_KEY, ControllerMediator.getInstance().isConsoleOpen());
        session.put(REVERSE_COMPLEMENT_KEY, ControllerMediator.getInstance().isReverseComplementing());
        session.put(SHOW_NAMES_KEY, ControllerMediator.getInstance().isShowingNames());
        addConsoleMessagesToSession(session);
        return session;
    }

    /**
     * Saves the current console messages to the given session
     */
    private static void addConsoleMessagesToSession(JSONObject session) {
        ArrayList<ConsoleController.Message> consoleMessages = ControllerMediator.getInstance().getConsoleMessages();
        JSONArray messagesArray = new JSONArray();
        for(ConsoleController.Message message : consoleMessages) {
            JSONObject messageJSON = new JSONObject();
            messageJSON.put(MESSAGE_TEXT_KEY, message.getMessageText());
            messageJSON.put(MESSAGE_IS_ERROR_KEY, message.getMessageType());
            messagesArray.put(messageJSON);
        }
        session.put(CONSOLE_MESSAGES_KEY, messagesArray);
    }
}
