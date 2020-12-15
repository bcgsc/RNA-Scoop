package controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;
import mediator.ControllerMediator;
import org.json.JSONObject;
import parser.Parser;
import persistance.SessionIO;
import persistance.SessionMaker;
import ui.Main;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
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
    @FXML private MenuItem clusterViewToggle;
    @FXML private MenuItem isoformPlotToggle;
    @FXML private MenuItem consoleToggle;
    @FXML private SplitPane verticalSplitPane;
    @FXML private SplitPane horizontalSplitPane;
    @FXML private CheckMenuItem revComplementToggle;
    @FXML private CheckMenuItem hideSingleExonIsoformsToggle;
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
    @FXML private CheckMenuItem showIsoformPlotLegendToggle;
    @FXML private CheckMenuItem colorCellPlotByIsoformToggle;

    private Stage window;
    private boolean clusterViewIsOpen;
    private boolean consoleIsOpen;
    private boolean isoformPlotIsOpen;
    private String currentLoadedPath;

    public void initializeMain(Parent console, Parent isoformPlot, Parent clusterView) {
        setUpWindow();
        addPanels(console, isoformPlot, clusterView);
        setViewTogglesToDefault();
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
            horizontalSplitPane.getItems().add(0, ControllerMediator.getInstance().getIsoformPlotPanel());
            isoformPlotToggle.setText("Close Isoform plot");
            isoformPlotIsOpen = true;
        }
    }

    public void closeIsoformPlot() {
        if (isoformPlotIsOpen) {
            horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getIsoformPlotPanel());
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

    public void restoreMainFromPrevSession(JSONObject prevSession) {
        restoreIsoformPlotFromPrevSession(prevSession);
        restoreClusterViewFromPrevSession(prevSession);
        restoreConsoleFromPrevSession(prevSession);
        restoreViewTogglesFromPrevSession(prevSession);
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

    public boolean isReverseComplementing() {
        return revComplementToggle.isSelected();
    }

    public boolean isHidingSingleExonIsoforms() {
        return hideSingleExonIsoformsToggle.isSelected();
    }

    public boolean isHidingDotPlot() {
        return hideDotPlotToggle.isSelected();
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

    public boolean isShowingIsoformPlotLegend() {
        return showIsoformPlotLegendToggle.isSelected();
    }

    public boolean isColoringCellPlotBySelectedIsoform() {
        return colorCellPlotByIsoformToggle.isSelected();
    }

    public Window getMainWindow() {
        return window;
    }

    /**
     * Saves current session to file chosen by user through file chooser and adds
     * error/success messages to console
     */
    @FXML
    protected void handleSaveSessionButton() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON Files", "*.json");
        fileChooser.getExtensionFilters().add(jsonFilter);
        fileChooser.setInitialFileName("session.json");
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try {
                SessionIO.saveSessionAtPath(file.getPath());
                ControllerMediator.getInstance().addConsoleMessage("Successfully saved session");
            } catch (FileAlreadyExistsException e) {
                ControllerMediator.getInstance().addConsoleErrorMessage("Could not create directory named " + e.getFile() +
                        " as a file with that name already exists. Either rename the file, or use another name for the session");
            } catch (Exception e) {
                ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
            }
        }
    }

    /**
     * Loads session user selects from file chooser and adds error/success messages to
     * console
     */
    @FXML
    protected void handleLoadSessionButton() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            try {
                SessionIO.loadSessionAtPath(file.getPath());
            } catch (Exception e) {
                ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
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
        setViewTogglesToDefault();
        ControllerMediator.getInstance().setTPMGradientToDefault();
        ControllerMediator.getInstance().setGeneFilteringParamsToDefault();
        ControllerMediator.getInstance().setClusterViewSettingsToDefault();
        ControllerMediator.getInstance().setImageExporterSettingsToDefault();
        SessionIO.clearCurrentSessionData();
        ControllerMediator.getInstance().clearConsole();
        clearPathComboBox();
    }

    @FXML
    protected void handleExportFiguresButton() {
        ControllerMediator.getInstance().displayImageExporter();
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
     * When hide dot plot toggle is selected/deselected changes tells
     * isoform plot about change
     */
    @FXML
    protected void handleHideDotPlotToggle() {
        ControllerMediator.getInstance().isoformPlotHandleDotPlotChange();
    }

    /**
     * When show isoform plot legend toggle is selected/deselected changes updates the legend
     * (either removes or adds it accordingly)
     */
    @FXML
    protected void handleIsoformPlotLegendToggle() {
        ControllerMediator.getInstance().updateIsoformPlotLegend(false);
    }

    /**
     * When one of the expression toggles is selected/deselected alerts
     * the dot plot of the change
     */
    @FXML
    protected void handleExpressionToggle() {
        ControllerMediator.getInstance().isoformPlotHandleExpressionTypeChange();
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

    @FXML
    protected void handleColorCellPlotByIsoformToggle() {
        Collection<String> selectedIsoformIDs = ControllerMediator.getInstance().getSelectedIsoformIDs();

        if (selectedIsoformIDs.size() > 0) {
            ControllerMediator.getInstance().deselectAllIsoforms();

            if (ControllerMediator.getInstance().areCellsSelected())
                ControllerMediator.getInstance().clearSelectedCellsAndRedrawPlot();
            else
                ControllerMediator.getInstance().redrawCellPlotSansLegend();
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
     * Displays About window when About button is pressed
     */
    @FXML
    protected void handleAboutButtonAction() {
        ControllerMediator.getInstance().displayAboutWindow();
    }

    /**
     * When open file chooser button is pressed, opens file chooser
     * The chosen file's path given to the path combo box, and the file is loaded
     */
    @FXML
    protected void handleOpenFileChooserButton() {
        FileChooser fileChooser = new FileChooser();
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
    private void restoreIsoformPlotFromPrevSession(JSONObject prevSession) {
        boolean prevIsoformPlotOpen = prevSession.getBoolean(SessionMaker.ISOFORM_PLOT_OPEN_KEY);
        if (prevIsoformPlotOpen)
            openIsoformPlot();
        else
            closeIsoformPlot();
    }

    /**
     * If the cluster view was closed in the previous session, closes the cluster view,
     * otherwise opens it
     */
    private void restoreClusterViewFromPrevSession(JSONObject prevSession) {
        boolean prevClusterViewOpen = prevSession.getBoolean(SessionMaker.CLUSTER_VIEW_OPEN_KEY);
        if (prevClusterViewOpen)
            openClusterView();
        else
            closeClusterView();
    }

    /**
     * If the console was closed in the previous session, closes the console,
     * otherwise opens it
     */
    private void restoreConsoleFromPrevSession(JSONObject prevSession) {
        boolean prevConsolePlotOpen = prevSession.getBoolean(SessionMaker.CONSOLE_OPEN_KEY);
        if (prevConsolePlotOpen)
            openConsole();
        else
            closeConsole();
    }

    /**
     * Restores view menu toggles to what they were in the previous session
     */
    private void restoreViewTogglesFromPrevSession(JSONObject prevSession) {
        restoreReverseComplementToggle(prevSession);
        restoreHideSingleExonIsoformsToggle(prevSession);
        restoreHideDotPlotToggle(prevSession);
        restoreExpressionToggles(prevSession);
        restoreShowGeneNameIDToggles(prevSession);
        restoreShowIsoformNameToggle(prevSession);
        restoreShowIsoformIDToggle(prevSession);
        restoreShowIsoformPlotLegendToggle(prevSession);
        restoreColorCellPlotByIsoformToggle(prevSession);
    }

    /**
     * If the reverse complement toggle was selected in the previous session, selects it, else
     * deselects it
     */
    private void restoreReverseComplementToggle(JSONObject prevSession) {
        boolean wasReverseComplementing = prevSession.getBoolean(SessionMaker.REVERSE_COMPLEMENT_KEY);
        revComplementToggle.setSelected(wasReverseComplementing);
    }

    /**
     * If the hide single-exon isoforms toggle was selected in the previous session, selects it,
     * else deselects it
     */
    private void restoreHideSingleExonIsoformsToggle(JSONObject prevSession) {
        boolean wasHidingSingleExonIsoforms = prevSession.getBoolean(SessionMaker.HIDE_SINGLE_EXON_ISOFORMS_KEY);
        hideSingleExonIsoformsToggle.setSelected(wasHidingSingleExonIsoforms);
    }

    /**
     * If the hide dot plot toggle was selected in the previous session, selects it,
     * else deselects it
     */
    private void restoreHideDotPlotToggle(JSONObject prevSession) {
        boolean wasHidingDotPlot = prevSession.getBoolean(SessionMaker.HIDE_DOT_PLOT_KEY);
        hideDotPlotToggle.setSelected(wasHidingDotPlot);
    }

    /**
     * Selects the expression toggles that were selected in the previous session, deselects the
     * rest
     */
    private void restoreExpressionToggles(JSONObject prevSession) {
        boolean wasShowingMedian = prevSession.getBoolean(SessionMaker.SHOW_MEDIAN_KEY);
        boolean wasIncludingZeros = prevSession.getBoolean(SessionMaker.INCLUDE_ZEROS_KEY);

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
    private void restoreShowGeneNameIDToggles(JSONObject prevSession) {
        boolean wasShowingGeneNameAndID = prevSession.getBoolean(SessionMaker.SHOW_GENE_NAME_AND_ID_KEY);
        boolean wasShowingGeneName = prevSession.getBoolean(SessionMaker.SHOW_GENE_NAME_KEY);
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
    private void restoreShowIsoformNameToggle(JSONObject prevSession) {
        boolean wasShowingIsoformName = prevSession.getBoolean(SessionMaker.SHOW_ISOFORM_NAME_KEY);
        showIsoformNameToggle.setSelected(wasShowingIsoformName);
    }

    /**
     * If the show isoform ID toggle was selected in the previous session, selects it, else
     * deselects it
     */
    private void restoreShowIsoformIDToggle(JSONObject prevSession) {
        boolean wasShowingIsoformID = prevSession.getBoolean(SessionMaker.SHOW_ISOFORM_ID_KEY);
        showIsoformIDToggle.setSelected(wasShowingIsoformID);
    }

    /**
     * If the show isoform plot legend toggle was selected in the previous session, selects it,
     * else deselects it
     */
    private void restoreShowIsoformPlotLegendToggle(JSONObject prevSession) {
        boolean wasShowingIsoformPlotLegend = prevSession.getBoolean(SessionMaker.SHOW_ISOFORM_PLOT_LEGEND_KEY);
        showIsoformPlotLegendToggle.setSelected(wasShowingIsoformPlotLegend);
    }

    /**
     * If the color cell plot by isoform toggle was selected in the previous session, selects it, else
     * deselects it
     */
    private void restoreColorCellPlotByIsoformToggle(JSONObject prevSession) {
        boolean wasColoringCellPlotByIsoform = prevSession.getBoolean(SessionMaker.COLOR_CELL_PLOT_BY_ISOFORM_KEY);
        colorCellPlotByIsoformToggle.setSelected(wasColoringCellPlotByIsoform);
    }

    private void setViewTogglesToDefault() {
        revComplementToggle.setSelected(false);
        hideSingleExonIsoformsToggle.setSelected(false);
        hideDotPlotToggle.setSelected(false);
        showMedianToggle.setSelected(true);
        includeZerosToggle.setSelected(true);
        showGeneNameToggle.setSelected(true);
        showIsoformNameToggle.setSelected(false);
        showIsoformIDToggle.setSelected(false);
        showIsoformPlotLegendToggle.setSelected(true);
        colorCellPlotByIsoformToggle.setSelected(false);
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
        ControllerMediator.getInstance().disableClusterViewSettings();
        ControllerMediator.getInstance().disableTPMGradientAdjuster();
        ControllerMediator.getInstance().disableLabelSetManager();
        ControllerMediator.getInstance().disableGeneFilterer();
        // doesn't disable add label set view, because main should be disabled when
        // that view is active
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableClusterView();
        ControllerMediator.getInstance().enableClusterViewSettings();
        ControllerMediator.getInstance().enableTPMGradientAdjuster();
        ControllerMediator.getInstance().enableLabelSetManager();
        ControllerMediator.getInstance().enableGeneFilterer();
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
            boolean success = Parser.loadJSONFile(filePath);
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
