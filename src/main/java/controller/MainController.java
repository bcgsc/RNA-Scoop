package controller;

import annotation.Gene;
import exceptions.RNAScoopException;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import parser.Parser;
import persistance.SessionIO;
import persistance.SessionMaker;
import ui.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static javafx.application.Platform.runLater;

public class MainController implements InteractiveElementController {
    private static final float MAIN_SCALE_FACTOR = 0.7f;

    @FXML private BorderPane borderPane;
    @FXML private ComboBox pathComboBox;
    @FXML private Button openFileChooserButton;
    @FXML private Menu fileMenu;
    @FXML private Menu viewMenu;
    @FXML private MenuItem tSNEToggle;
    @FXML private MenuItem isoformPlotToggle;
    @FXML private MenuItem consoleToggle;
    @FXML private SplitPane verticalSplitPane;
    @FXML private SplitPane horizontalSplitPane;
    //isoform plot setting toggles
    @FXML private CheckMenuItem revComplementToggle;
    @FXML private RadioMenuItem showGeneNameAndIDToggle;
    @FXML private RadioMenuItem showGeneNameToggle;
    @FXML private RadioMenuItem showGeneIDToggle;
    @FXML private CheckMenuItem showIsoformNameToggle;
    @FXML private CheckMenuItem showIsoformIDToggle;

    private Stage window;
    private FileChooser fileChooser;
    private boolean tSNEPlotIsOpen;
    private boolean consoleIsOpen;
    private boolean isoformPlotIsOpen;
    private String currentLoadedPath;

    public void initializeMain(Parent console, Parent isoformPlot, Parent tSNEPlot) {
        fileChooser = new FileChooser();
        setUpWindow();
        addPanels(console, isoformPlot, tSNEPlot);
        setUpPathComboBox();
    }

    public void disable() {
        fileMenu.setDisable(true);
        viewMenu.setDisable(true);
        openFileChooserButton.setDisable(true);
        pathComboBox.setDisable(true);
    }

    public void enable() {
        fileMenu.setDisable(false);
        viewMenu.setDisable(false);
        openFileChooserButton.setDisable(false);
        pathComboBox.setDisable(false);
    }

    public void openIsoformPlot() {
        if (!isoformPlotIsOpen) {
            horizontalSplitPane.getItems().add(0, ControllerMediator.getInstance().getIsoformPlot());
            isoformPlotToggle.setText("Close Isoform Plot");
            isoformPlotIsOpen = true;
        }
    }

    public void closeIsoformPlot() {
        if (isoformPlotIsOpen) {
            horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getIsoformPlot());
            isoformPlotToggle.setText("Open Isoform Plot");
            isoformPlotIsOpen = false;
        }
    }

    public void openTSNEPlot() {
        if (!tSNEPlotIsOpen) {
            horizontalSplitPane.getItems().add(ControllerMediator.getInstance().getTSNEPlot());
            tSNEToggle.setText("Close t-SNE Plot");
            tSNEPlotIsOpen = true;
        }
    }

    public void closeTSNEPlot() {
        if (tSNEPlotIsOpen) {
            horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getTSNEPlot());
            tSNEToggle.setText("Open t-SNE Plot");
            tSNEPlotIsOpen = false;
        }
    }

    public void openConsole() {
        if(!consoleIsOpen) {
            verticalSplitPane.getItems().add(1, ControllerMediator.getInstance().getConsole());
            verticalSplitPane.setDividerPosition(0, 1);
            consoleToggle.setText("Close Console");
            consoleIsOpen = true;
        }
    }

    public void closeConsole() {
        if (consoleIsOpen) {
            verticalSplitPane.getItems().remove(ControllerMediator.getInstance().getConsole());
            consoleToggle.setText("Open Console");
            consoleIsOpen = false;
        }
    }

    public void setPathComboBoxValue(String path) {
        pathComboBox.setValue(path);
    }

    public boolean isTSNEPlotOpen() {
        return tSNEPlotIsOpen;
    }

    public boolean isConsoleOpen() {
        return consoleIsOpen;
    }

    public boolean isIsoformPlotOpen() {
        return isoformPlotIsOpen;
    }

    public String getCurrentLoadedPath() {
        return currentLoadedPath;
    }

    public boolean isReverseComplementing() {
        return revComplementToggle.isSelected();
    }

    public boolean isShowingGeneAndIDName() {
        return showGeneNameAndIDToggle.isSelected();
    }

    public boolean isShowingGeneName() {
        return showGeneNameToggle.isSelected();
    }

    public boolean isShowingGeneID() {
        return showGeneIDToggle.isSelected();
    }

    public boolean isShowingIsoformName() {
        return showIsoformNameToggle.isSelected();
    }

    public boolean isShowingIsoformID() {
        return showIsoformIDToggle.isSelected();
    }

    public void restoreMainFromJSON(Map settings) {
        restoreIsoformPlotFromJSON(settings);
        restoreTSNEPlotFromJSON(settings);
        restoreConsoleFromJSON(settings);
        restorePathComboBoxFromJSON(settings);
        restoreIsoformPlotSettingsTogglesFromJSON(settings);
    }

    @FXML
    protected void handleSaveSessionButton() {
        File file = getSavedFileFromFileChooser();
        if (file != null) {
            try {
                SessionIO.saveSessionAtPath(file.getPath());
            } catch (Exception e) {
                ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("saving session at path: " + file.getPath());
            }
        }
    }

    @FXML
    protected void handleLoadSessionButton() {
        File file = getFileFromFileChooser();
        if (file != null) {
            try {
                SessionIO.loadSessionAtPath(file.getPath());
            } catch (Exception e) {
                ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("loading session from path: " + file.getPath());
            }
        }
    }

    /**
     * Redraws genes when one of the isoform plot setting toggles (reverse complement,
     * show gene name, show gene ID, show isoform name, show isoform ID) changes state
     */
    @FXML
    protected void handleIsoformPlotSettingToggle() {
        Collection<Gene> genes = ControllerMediator.getInstance().getShownGenes();
        ControllerMediator.getInstance().drawGenes(genes);
    }

    /**
     * When isoform plot toggle is pressed, toggles visibility of the isoform plot
     */
    @FXML
    protected void handleIsoformViewToggle() {
        if (isoformPlotIsOpen) {
            closeIsoformPlot();
        } else {
            openIsoformPlot();
        }
    }

    /**
     * When t-SNE toggle is pressed, toggles visibility of the t-SNE plot
     */
    @FXML
    protected void handleTSNEViewToggle() {
        if (tSNEPlotIsOpen) {
            closeTSNEPlot();
        } else {
            openTSNEPlot();
        }
    }

    /**
     * When clear console button is pressed clears console
     */
    @FXML
    protected void handleClearConsoleButton() {
        ControllerMediator.getInstance().clearConsole();
    }

    /**
     * When console toggle is pressed, toggles visibility of the console
     */
    @FXML
    protected void handleConsoleViewToggle() {
        if (consoleIsOpen) {
            closeConsole();
        } else {
            openConsole();
        }
    }

    /**
     * Loads About window when About button is pressed
     */
    @FXML
    protected void handleAboutButtonAction() {
        try {
           FXMLLoader.load(getClass().getResource("/fxml/about.fxml"));
        }
        catch (IOException e) {
            ControllerMediator.getInstance().addConsoleErrorMessage("Could not load about window");
        }
    }

    /**
     * When open file chooser button is pressed, opens file chooser
     * Gene selector is disabled when user is choosing file
     * The chosen file's path given to the path combo box, and the file is loaded
     */
    @FXML
    protected void handleOpenFileChooserButton() {
        File file = getFileFromFileChooser();
        if (file != null) {
            pathComboBox.setValue(file.getAbsolutePath());
            loadFile();
        }
    }

    /**
     * If the isoform plot was closed in the previous session, closes the isoform plot,
     * otherwise opens it
     */
    private void restoreIsoformPlotFromJSON(Map settings) {
        boolean prevIsoformPlotOpen = (boolean) settings.get(SessionMaker.ISOFORM_PLOT_OPEN_KEY);
        if (prevIsoformPlotOpen)
            openIsoformPlot();
        else
            closeIsoformPlot();
    }

    /**
     * If the t-SNE plot was closed in the previous session, closes the t-SNE plot,
     * otherwise opens it
     */
    private void restoreTSNEPlotFromJSON(Map settings) {
        boolean prevTSNEPlotOpen = (boolean) settings.get(SessionMaker.TSNE_PLOT_OPEN_KEY);
        if (prevTSNEPlotOpen)
            openTSNEPlot();
        else
            closeTSNEPlot();
    }

    /**
     * If the console was closed in the previous session, closes the console,
     * otherwise opens it
     */
    private void restoreConsoleFromJSON(Map settings) {
        boolean prevConsolePlotOpen = (boolean) settings.get(SessionMaker.CONSOLE_OPEN_KEY);
        if (prevConsolePlotOpen)
            openConsole();
        else
            closeConsole();
    }

    /**
     * Restores isoform plot setting toggles to what they were in the previous session
     */
    private void restoreIsoformPlotSettingsTogglesFromJSON(Map settings) {
        restoreReverseComplementToggle(settings);
        restoreShowGeneNameIDToggles(settings);
        restoreShowIsoformNameToggle(settings);
        restoreShowIsoformIDToggle(settings);
    }

    /**
     * If the loaded path from the previous session has been saved, sets the path combo box's value
     * to it
     */
    private void restorePathComboBoxFromJSON(Map settings) {
        if(settings.containsKey(SessionMaker.PATH_KEY))
            ControllerMediator.getInstance().setPathComboBoxValue((String) settings.get(SessionMaker.PATH_KEY));
    }

    /**
     * If the reverse complement toggle was selected in the previous session, selects it, else
     * deselects it
     */
    private void restoreReverseComplementToggle(Map settings) {
        boolean wasReverseComplementing = (boolean) settings.get(SessionMaker.REVERSE_COMPLEMENT_KEY);
        if (wasReverseComplementing)
            revComplementToggle.setSelected(true);
        else
            revComplementToggle.setSelected(false);
    }

    /**
     * Selects the show gene name/ID toggle that was selected in the previous session, deselects the
     * rest
     */
    private void restoreShowGeneNameIDToggles(Map settings) {
        boolean wasShowingGeneNameAndID = (boolean) settings.get(SessionMaker.SHOW_GENE_NAME_AND_ID_KEY);
        boolean wasShowingGeneName = (boolean) settings.get(SessionMaker.SHOW_GENE_NAME_KEY);
        if (wasShowingGeneNameAndID)
            showGeneNameAndIDToggle.setSelected(true);
        else if (wasShowingGeneName)
            showGeneNameToggle.setSelected(true);
        else
            showGeneIDToggle.setSelected(true);
    }

    /**
     * If the show isoform name toggle was selected in the previous session, selects it, else
     * deselects it
     */
    private void restoreShowIsoformNameToggle(Map settings) {
        boolean wasShowingIsoformName = (boolean) settings.get(SessionMaker.SHOW_ISOFORM_NAME_KEY);
        if (wasShowingIsoformName)
            showIsoformNameToggle.setSelected(true);
        else
            showIsoformNameToggle.setSelected(false);
    }

    /**
     * If the show isoform ID toggle was selected in the previous session, selects it, else
     * deselects it
     */
    private void restoreShowIsoformIDToggle(Map settings) {
        boolean wasShowingIsoformID = (boolean) settings.get(SessionMaker.SHOW_ISOFORM_ID_KEY);
        if (wasShowingIsoformID)
            showIsoformIDToggle.setSelected(true);
        else
            showIsoformIDToggle.setSelected(false);
    }

    private File getFileFromFileChooser() {
        ControllerMediator.getInstance().disableGeneSelector();
        File file = fileChooser.showOpenDialog(window);
        ControllerMediator.getInstance().enableGeneSelector();
        return file;
    }

    private File getSavedFileFromFileChooser() {
        ControllerMediator.getInstance().disableGeneSelector();
        File file = fileChooser.showSaveDialog(window);
        ControllerMediator.getInstance().enableGeneSelector();
        return file;
    }

    /**
     * Clears t-SNE plot, all genes being shown in isoform plot, and disables associated functionality
     * Loads file from path in path combo box on different thread
     * Displays error (and successful completion) messages in console
     */
    private void loadFile() {
        ControllerMediator.getInstance().clearShownGenes();
        ControllerMediator.getInstance().clearTSNEPlot();
        disableAssociatedFunctionality();
        try {
            Thread fileLoaderThread = new Thread(new FileLoaderThread());
            fileLoaderThread.start();
        } catch (Exception e) {
            enableAssociatedFunctionality();
            ControllerMediator.getInstance().addConsoleErrorMessage("reading file from path: " + pathComboBox.getValue());
        }
    }

    private void disableAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableGeneSelector();
        ControllerMediator.getInstance().disableTSNEPlot();
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableTSNEPlot();
    }

    /**
     * Thread that loads files from paths in path combo box
     */
    private class FileLoaderThread implements Runnable {

        /**
         * Loads file from path in path combo box
         * If the file is successfully loaded, sets the path as the current loaded path and adds it to
         * the path combo box's list of loaded paths
         * Writes messages to the console
         */
        @Override
        public void run() {
            try {
                String filePath = (String) pathComboBox.getValue();
                runLater(() ->  ControllerMediator.getInstance().addConsoleMessage("Loading file from path: " + filePath));
                Parser.readFile(filePath);
                runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Successfully loaded file from path: " + filePath));
                currentLoadedPath = filePath;
                runLater(MainController.this::addLoadedPath);
            } catch (RNAScoopException e){
                Parser.removeParsedGenes();
                runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage()));
                currentLoadedPath = null;
            } catch (FileNotFoundException e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage("Could not find file at path: " + pathComboBox.getValue()));
                currentLoadedPath = null;
            } catch (Exception e) {
                Parser.removeParsedGenes();
                runLater(() -> ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("reading file from path: " + pathComboBox.getValue()));
                currentLoadedPath = null;
            } finally {
                runLater(() -> ControllerMediator.getInstance().updateGenes());
                runLater(MainController.this::enableAssociatedFunctionality);
            }
        }
    }

    /**
     * Sets up main window
     * Makes it so current session is saved after user clicks X button
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSize();
        window.setOnCloseRequest(event -> {
            try {
                SessionIO.saveSession();
            } catch (IOException e) {
                System.err.println("An error occurred while saving the current session");
                e.printStackTrace();
            }
        });
        window.show();
    }

    private void setWindowSize() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        window.setScene(new Scene(borderPane, screen.getWidth() * MAIN_SCALE_FACTOR, screen.getHeight() * MAIN_SCALE_FACTOR));
    }

    /**
     * Add's the console, isoform plot and t-sne plot panels to the main
     * window (by default all panels are open)
     */

    private void addPanels(Parent console, Parent isoformPlot, Parent tSNEPlot) {
        horizontalSplitPane.getItems().add(isoformPlot);
        horizontalSplitPane.getItems().add(tSNEPlot);
        verticalSplitPane.getItems().add(console);
        tSNEPlotIsOpen = true;
        consoleIsOpen = true;
        isoformPlotIsOpen = true;
    }

    /**
     * Automatically resizes path combo box when window is resized
     * Allows dragging and dropping of files
     * Makes it so files are loaded from path in combo box when ENTER is pressed
     * Removes initial focus from path combo box
     */
    private void setUpPathComboBox() {
        borderPane.widthProperty().addListener((observable, oldValue, newValue) -> pathComboBox.setPrefWidth(borderPane.getWidth() - 95));
        setUpPathComboBoxDragNDrop();
        // adds listener to border pane so that focus can be on any or no elements and
        // the key press is still registered
        borderPane.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER))
                loadFile();
        });
        runLater(() -> borderPane.requestFocus());
        currentLoadedPath = null;
    }

    /**
     * Puts current loaded path at the first position in the path combo box's
     * list of paths, and selects it
     */
    private void addLoadedPath() {
        ObservableList<String> addedPaths = pathComboBox.getItems();
        addedPaths.remove(currentLoadedPath);
        addedPaths.add(0, currentLoadedPath);
        pathComboBox.getSelectionModel().select(0);
    }

    /**
     * Allows user to drag a file into path combo box and set its value to
     * the file's path
     *
     * If user drags multiple files, prints an error message to the console
     */
    private void setUpPathComboBoxDragNDrop() {
        pathComboBox.setOnDragOver(event -> {
            if (event.getGestureSource() != pathComboBox && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        pathComboBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.getFiles().size() > 1)
                ControllerMediator.getInstance().addConsoleErrorMessage("You cannot load more than one file at a time");
            else
                pathComboBox.setValue(db.getFiles().get(0).getAbsolutePath());
            event.consume();
        });
    }
}
