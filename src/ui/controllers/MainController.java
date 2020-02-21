package ui.controllers;

import exceptions.RNAScoopException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import parser.Parser;
import ui.mediator.ControllerMediator;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static javafx.application.Platform.*;

public class MainController implements InteractiveElementController {

    private static final float ABOUT_SCALE_FACTOR = 0.33f;
    private static final Image logo = new Image("ui/resources/icons/RNA-ScoopIcon.png");

    @FXML private BorderPane borderPane;
    @FXML private ComboBox pathComboBox;
    @FXML private Button openFileChooserButton;
    @FXML private Menu viewMenu;
    @FXML private MenuItem tSNEToggle;
    @FXML private MenuItem isoformPlotToggle;
    @FXML private MenuItem consoleToggle;
    @FXML private SplitPane verticalSplitPane;
    @FXML private SplitPane horizontalSplitPane;

    private Stage window;
    private FileChooser fileChooser;
    private boolean tSNEPlotIsOpen;
    private boolean consoleIsOpen;
    private boolean isoformPlotIsOpen;
    private String currentLoadedPath;

    public void initializeMain(Stage window, Parent console, Parent isoformPlot, Parent tSNEPlot) {
        this.window = window;
        fileChooser = new FileChooser();
        addPanels(console, isoformPlot, tSNEPlot);
        setUpPathComboBox();
        currentLoadedPath = null;
    }

    public void disable() {
        openFileChooserButton.setDisable(true);
        viewMenu.setDisable(true);
        pathComboBox.setDisable(true);
    }

    public void enable() {
        openFileChooserButton.setDisable(false);
        viewMenu.setDisable(false);
        pathComboBox.setDisable(false);
    }

    public void openIsoformPlot() {
        horizontalSplitPane.getItems().add(0, ControllerMediator.getInstance().getIsoformPlot());
        isoformPlotToggle.setText("Close Isoform Plot");
        isoformPlotIsOpen = true;
    }

    public void closeIsoformPlot() {
        horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getIsoformPlot());
        isoformPlotToggle.setText("Open Isoform Plot");
        isoformPlotIsOpen = false;
    }

    public void openTSNEPlot() {
        horizontalSplitPane.getItems().add(ControllerMediator.getInstance().getTSNEPlot());
        tSNEToggle.setText("Close t-SNE Plot");
        tSNEPlotIsOpen = true;
    }

    public void closeTSNEPlot() {
        horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getTSNEPlot());
        tSNEToggle.setText("Open t-SNE Plot");
        tSNEPlotIsOpen = false;
    }

    public void openConsole() {
        verticalSplitPane.getItems().add(1, ControllerMediator.getInstance().getConsole());
        verticalSplitPane.setDividerPosition(0, 1);
        consoleToggle.setText("Close Console");
        consoleIsOpen = true;
    }

    public void closeConsole() {
        verticalSplitPane.getItems().remove(ControllerMediator.getInstance().getConsole());
        consoleToggle.setText("Open Console");
        consoleIsOpen = false;
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

    public void setPathComboBoxValue(String path) {
        pathComboBox.setValue(path);
    }

    public String getCurrentLoadedPath() {
        return  currentLoadedPath;
    }

    /**
     * When reverse complement toggle is selected, (-) strands will be reverse complemented in
     * the isoform plot; when it is unselected, they will not
     */
    @FXML
    protected void handleRevComplementToggle() {
        ControllerMediator.getInstance().toggleReverseComplement();
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
     * When open file chooser button is pressed, opens file chooser
     * Gene selector is disabled when user is choosing file
     * The chosen file's path given to the path combo box, and the file is loaded
     */
    @FXML
    protected void handleOpenFileChooserButton() {
        ControllerMediator.getInstance().disableGeneSelector();
        File file = fileChooser.showOpenDialog(window);
        ControllerMediator.getInstance().enableGeneSelector();
        if (file != null) {
            pathComboBox.setValue(file.getAbsolutePath());
            loadFile();
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

    @FXML
    protected void handleAboutButtonAction() {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("ui/fxml/about.fxml"));
            Stage stage = new Stage();
            stage.setTitle("About");
            Rectangle2D screen = Screen.getPrimary().getBounds();
            stage.setScene(new Scene(root, screen.getWidth() * ABOUT_SCALE_FACTOR, screen.getHeight() * ABOUT_SCALE_FACTOR));
            stage.getIcons().add(logo);
            stage.show();
        }
        catch (IOException e) {
            ControllerMediator.getInstance().addConsoleErrorMessage("Could not load about window");
        }
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
     * Add's the console, isoform plot and t-sne plot panels to the main
     * window.
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
        borderPane.widthProperty().addListener((observable, oldValue, newValue) -> pathComboBox.setPrefWidth(window.getWidth() - 95));
        setUpPathComboBoxDragNDrop();
        // adds listener to border pane so that focus can be on any or no elements and
        // the key press is still registered
        borderPane.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER))
                loadFile();
        });
        runLater(() -> borderPane.requestFocus());
    }

    /**
     * Puts current loaded path at the first position in the path combo box's
     * list of paths, and selects it
     */
    private void addLoadedPath() {
        ObservableList<String> addedPaths = pathComboBox.getItems();
        if (addedPaths.contains(currentLoadedPath)) {
            addedPaths.remove(currentLoadedPath);
        }
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
