package controller;

import exceptions.RNAScoopException;
import javafx.application.Platform;
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
import java.io.IOException;
import java.util.Map;

import static javafx.application.Platform.runLater;

public class MainController implements InteractiveElementController {
    private static final float MAIN_SCALE_FACTOR = 0.7f;

    @FXML private BorderPane borderPane;
    @FXML private ComboBox pathComboBox;
    @FXML private Button openFileChooserButton;
    @FXML private Menu fileMenu;
    @FXML private Menu viewMenu;
    @FXML private MenuItem clusterViewToggle;
    @FXML private MenuItem isoformPlotToggle;
    @FXML private MenuItem consoleToggle;
    @FXML private SplitPane verticalSplitPane;
    @FXML private SplitPane horizontalSplitPane;
    @FXML private CheckMenuItem hideSingleExonIsoformsToggle;
    @FXML private CheckMenuItem revComplementToggle;
    @FXML private CheckMenuItem hideDotPlotToggle;
    // expression toggles
    @FXML private RadioMenuItem showMedianToggle;
    @FXML private RadioMenuItem showAverageToggle;
    @FXML private CheckMenuItem includeZerosToggle;
    // gene label toggles
    @FXML private RadioMenuItem showGeneNameAndIDToggle;
    @FXML private RadioMenuItem showGeneNameToggle;
    @FXML private RadioMenuItem showGeneIDToggle;
    // isoform label toggles
    @FXML private CheckMenuItem showIsoformNameToggle;
    @FXML private CheckMenuItem showIsoformIDToggle;

    private Stage window;
    private FileChooser fileChooser;
    private boolean clusterViewIsOpen;
    private boolean consoleIsOpen;
    private boolean isoformPlotIsOpen;
    private String currentLoadedPath;

    public void initializeMain(Parent console, Parent isoformPlot, Parent clusterView) {
        fileChooser = new FileChooser();
        setUpWindow();
        addPanels(console, isoformPlot, clusterView);
        setIsoformPlotSettingsToDefault();
        setUpPathComboBox();
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        fileMenu.setDisable(true);
        viewMenu.setDisable(true);
        openFileChooserButton.setDisable(true);
        pathComboBox.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        fileMenu.setDisable(false);
        viewMenu.setDisable(false);
        openFileChooserButton.setDisable(false);
        pathComboBox.setDisable(false);
    }

    public void openIsoformPlot() {
        if (!isoformPlotIsOpen) {
            horizontalSplitPane.getItems().add(0, ControllerMediator.getInstance().getIsoformPlot());
            isoformPlotToggle.setText("Close Isoform plot");
            isoformPlotIsOpen = true;
        }
    }

    public void closeIsoformPlot() {
        if (isoformPlotIsOpen) {
            horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getIsoformPlot());
            isoformPlotToggle.setText("Open Isoform plot");
            isoformPlotIsOpen = false;
        }
    }

    public void openClusterView() {
        if (!clusterViewIsOpen) {
            horizontalSplitPane.getItems().add(ControllerMediator.getInstance().getClusterView());
            clusterViewToggle.setText("Close cluster view");
            clusterViewIsOpen = true;
        }
    }

    public void closeClusterView() {
        if (clusterViewIsOpen) {
            horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getClusterView());
            clusterViewToggle.setText("Open cluster view");
            clusterViewIsOpen = false;
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

    /**
     * Clears path combo box's list of paths, current loaded path, and path
     * combo box value
     */
    public void clearPathComboBox() {
        pathComboBox.getItems().clear();
        currentLoadedPath = null;
        pathComboBox.setValue(null);
    }

    public void restoreMainFromJSON(Map settings) {
        restoreIsoformPlotFromJSON(settings);
        restoreClusterViewFromJSON(settings);
        restoreConsoleFromJSON(settings);
        restorePathComboBoxFromJSON(settings);
        restoreIsoformPlotSettingsTogglesFromJSON(settings);
    }

    public boolean isClusterViewOpen() {
        return clusterViewIsOpen;
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

    public boolean isHidingDotPlot() {
        return hideDotPlotToggle.isSelected();
    }

    public boolean isHidingSingleExonIsoforms() {
        return hideSingleExonIsoformsToggle.isSelected();
    }

    public boolean isShowingMedian() {
        return showMedianToggle.isSelected();
    }

    public boolean isShowingAverage() {
        return showAverageToggle.isSelected();
    }

    public boolean isShowingGeneAndIDName() {
        return showGeneNameAndIDToggle.isSelected();
    }

    /**
     * Returns if should be including zeros when calculating expression or not
     */
    public boolean isIncludingZeros() {
        return includeZerosToggle.isSelected();
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

    /**
     * Saves current session to file chosen by user through file chooser and adds
     * error/success messages to console
     */
    @FXML
    protected void handleSaveSessionButton() {
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try {
                SessionIO.saveSessionAtPath(file.getPath());
                ControllerMediator.getInstance().addConsoleMessage("Successfully saved session");
            } catch (Exception e) {
                ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("saving session at path: " + file.getPath());
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads session user selects from file chooser and adds error/success messages to
     * console
     */
    @FXML
    protected void handleLoadSessionButton() {
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            try {
                SessionIO.loadSessionAtPath(file.getPath());
                ControllerMediator.getInstance().addConsoleMessage("Successfully loaded session");
            } catch (Exception e) {
                ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("loading session from path: " + file.getPath());
                e.printStackTrace();
            }
        }
    }

    /**
     * Resets the current session to have all the default settings. Opens all panels,
     * clears all data, sets isoform view settings to default (basically sets screen so it's as if you're opening
     * RNA-Scoop for the first time)
     */
    @FXML
    protected void handleResetSessionButton() {
        openIsoformPlot();
        openClusterView();
        openConsole();
        setIsoformPlotSettingsToDefault();
        SessionIO.clearCurrentSessionData();
    }

    /**
     * When reverse complement toggle is selected/deselected changes whether genes in
     * isoform plot are reverse complemented, accordingly.
     */
    @FXML
    protected void handleReverseComplementToggle() {
        ControllerMediator.getInstance().updateGeneReverseComplementStatus();
    }

    /**
     * When hide single-exon isoforms toggle is selected/deselected changes whether
     * isoforms with only one exon in isoform plot are hidden, accordingly
     */
    @FXML
    protected void handleHideSingleExonIsoformsToggle() {
        ControllerMediator.getInstance().updateHideSingleExonIsoformsStatus();
    }

    /**
     * When hide dot plot toggle is selected/deselected changes whether dot plot
     * shows up when cells in cell plot are selected
     */
    @FXML
    protected void handleHideDotPlotToggle() {
        ControllerMediator.getInstance().handleColoringOrDotPlotChange();
    }

    /**
     * When one of the expression toggles is selected/deselected alerts
     * the dot plot of the change
     */
    @FXML
    protected void handleExpressionToggle() {
        ControllerMediator.getInstance().handleColoringOrDotPlotChange();
    }


    /**
     * When one of the gene label toggles is selected/deselected updates the gene labels
     * of all the genes in the isoform plot
     */
    @FXML
    protected void handleGeneLabelToggle() {
        ControllerMediator.getInstance().updateGeneLabels();
    }

    /**
     * When one of the isoform label toggles is selected/deselected updates the isoform labels
     * of all the genes in the isoform plot
     */
    @FXML
    protected void handleIsoformLabelToggle() {
        ControllerMediator.getInstance().updateIsoformLabels();
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
     * When cluster view toggle is pressed, toggles visibility of the cluster view
     */
    @FXML
    protected void handleClusterViewToggle() {
        if (clusterViewIsOpen) {
            closeClusterView();
        } else {
            openClusterView();
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
     * The chosen file's path given to the path combo box, and the file is loaded
     */
    @FXML
    protected void handleOpenFileChooserButton() {
        File file = fileChooser.showOpenDialog(window);
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
     * If the cluster view was closed in the previous session, closes the cluster view,
     * otherwise opens it
     */
    private void restoreClusterViewFromJSON(Map settings) {
        boolean prevClusterViewOpen = (boolean) settings.get(SessionMaker.CLUSTER_VIEW_OPEN_KEY);
        if (prevClusterViewOpen)
            openClusterView();
        else
            closeClusterView();
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
        restoreHideSingleExonIsoformsToggle(settings);
        restoreHideDotPlotToggle(settings);
        restoreExpressionToggles(settings);
        restoreShowGeneNameIDToggles(settings);
        restoreShowIsoformNameToggle(settings);
        restoreShowIsoformIDToggle(settings);
    }

    /**
     * Clears path combo box, then if the loaded path from the previous session has been saved,
     * sets the path combo box's value and saved current loaded path to it
     */
    private void restorePathComboBoxFromJSON(Map settings) {
        if(settings.containsKey(SessionMaker.PATH_KEY)) {
            currentLoadedPath = (String) settings.get(SessionMaker.PATH_KEY);
            pathComboBox.setValue(currentLoadedPath);
        }
    }

    /**
     * If the reverse complement toggle was selected in the previous session, selects it, else
     * deselects it
     */
    private void restoreReverseComplementToggle(Map settings) {
        boolean wasReverseComplementing = (boolean) settings.get(SessionMaker.REVERSE_COMPLEMENT_KEY);
        revComplementToggle.setSelected(wasReverseComplementing);
    }

    /**
     * If the hide single-exon isoforms toggle was selected in the previous session, selects it,
     * else deselects it
     */
    private void restoreHideSingleExonIsoformsToggle(Map settings) {
        boolean wasHidingSingleExonIsoforms = (boolean) settings.get(SessionMaker.HIDE_SINGLE_EXON_ISOFORMS_KEY);
        hideSingleExonIsoformsToggle.setSelected(wasHidingSingleExonIsoforms);
    }

    /**
     * If the hide dot plot toggle was selected in the previous session, selects it,
     * else deselects it
     */
    private void restoreHideDotPlotToggle(Map settings) {
        boolean wasHidingDotPlot = (boolean) settings.get(SessionMaker.HIDE_DOT_PLOT_KEY);
        hideDotPlotToggle.setSelected(wasHidingDotPlot);
    }

    /**
     * Selects the expression toggles that were selected in the previous session, deselects the
     * rest
     */
    private void restoreExpressionToggles(Map settings) {
        boolean wasShowingMedian = (boolean) settings.get(SessionMaker.SHOW_MEDIAN_KEY);
        boolean wasIncludingZeros = (boolean) settings.get(SessionMaker.INCLUDE_ZEROS_KEY);

        if (wasShowingMedian)
            showMedianToggle.setSelected(true);
        else
            showAverageToggle.setSelected(true);

        includeZerosToggle.setSelected(wasIncludingZeros);
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
        showIsoformNameToggle.setSelected(wasShowingIsoformName);
    }

    /**
     * If the show isoform ID toggle was selected in the previous session, selects it, else
     * deselects it
     */
    private void restoreShowIsoformIDToggle(Map settings) {
        boolean wasShowingIsoformID = (boolean) settings.get(SessionMaker.SHOW_ISOFORM_ID_KEY);
        showIsoformIDToggle.setSelected(wasShowingIsoformID);
    }

    private void setIsoformPlotSettingsToDefault() {
        revComplementToggle.setSelected(false);
        hideSingleExonIsoformsToggle.setSelected(false);
        hideDotPlotToggle.setSelected(false);
        showMedianToggle.setSelected(true);
        includeZerosToggle.setSelected(true);
        showGeneNameToggle.setSelected(true);
        showIsoformNameToggle.setSelected(false);
        showIsoformIDToggle.setSelected(false);
    }

    /**
     * Clears cell plot (include loaded matrix data), label sets, gene selector window,
     * current loaded path (as will be updated), disables associated functionality
     * Loads file from path in path combo box on different thread
     * Displays error (and successful completion) messages in console
     */
    private void loadFile() {
        currentLoadedPath = null;
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
        ControllerMediator.getInstance().disableClusterView();
        ControllerMediator.getInstance().disableTPMGradientAdjuster();
        ControllerMediator.getInstance().disableLabelSetManager();
        // doesn't disable add label set view, because main should be disabled when
        // that view is active
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableClusterView();
        ControllerMediator.getInstance().enableTPMGradientAdjuster();
        ControllerMediator.getInstance().enableLabelSetManager();
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
            String filePath = (String) pathComboBox.getValue();
            boolean success = Parser.loadFiles(filePath);
            if (success) {
                currentLoadedPath = filePath;
                runLater(MainController.this::addLoadedPath);
            }
            enableAssociatedFunctionality();
        }
    }

    /**
     * Sets up main window, when this window is closed, program shuts down
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> Platform.exit());
        window.show();
    }

    /**
     * Sets size of window and scene it displays, adds important styling for all scroll-panes
     * in the scene
     */
    private void setWindowSizeAndDisplay() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        Scene scene = new Scene(borderPane, screen.getWidth() * MAIN_SCALE_FACTOR, screen.getHeight() * MAIN_SCALE_FACTOR);
        scene.getStylesheets().add("/css/scrollpane.css");
        window.setScene(scene);
    }

    /**
     * Add's the console, isoform plot and cluster view panels to the main
     * window (by default all panels are open)
     */

    private void addPanels(Parent console, Parent isoformPlot, Parent clusterView) {
        horizontalSplitPane.getItems().add(isoformPlot);
        horizontalSplitPane.getItems().add(clusterView);
        verticalSplitPane.getItems().add(console);
        clusterViewIsOpen = true;
        consoleIsOpen = true;
        isoformPlotIsOpen = true;
    }

    /**
     * Automatically resizes path combo box when window is resized
     * Sets path combo box initial width
     * Allows dragging and dropping of files
     * Makes it so files are loaded from path in combo box when ENTER is pressed, and
     * combo box is in focus
     * Removes initial focus from path combo box
     */
    private void setUpPathComboBox() {
        borderPane.widthProperty().addListener((observable, oldValue, newValue) -> pathComboBox.setPrefWidth(borderPane.getWidth() - 95));
        pathComboBox.setPrefWidth(borderPane.getWidth() - 95);
        setUpPathComboBoxDragNDrop();
        // adds listener to border pane so that focus can be on any or no elements and
        // the key press is still registered
        pathComboBox.setOnKeyPressed(event -> {
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
     * the file's path. Path combo box is in focus after
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
            pathComboBox.requestFocus();
            if (db.getFiles().size() > 1)
                ControllerMediator.getInstance().addConsoleErrorMessage("You cannot load more than one file at a time");
            else
                pathComboBox.setValue(db.getFiles().get(0).getAbsolutePath());
            event.consume();
        });
    }
}
