package ui.controllers;

import exceptions.RNAScoopException;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import parser.Parser;
import ui.mediator.ControllerMediator;

import java.io.FileNotFoundException;
import java.io.IOException;

import static javafx.application.Platform.*;

public class MainController implements InteractiveElementController {

    private static final float ABOUT_SCALE_FACTOR = 0.33f;

    @FXML private BorderPane window;
    @FXML private ComboBox path;
    @FXML private Button loadButton;
    @FXML private Menu viewMenu;
    @FXML private MenuItem tSNEToggle;
    @FXML private MenuItem isoformPlotToggle;
    @FXML private MenuItem consoleToggle;
    @FXML private SplitPane verticalSplitPane;
    @FXML private SplitPane horizontalSplitPane;

    private boolean tSNEPlotOpen;
    private boolean consoleOpen;
    private boolean isoformPlotOpen;

    public void initializeMain(Parent console, Parent isoformPlot, Parent tSNEPlot) {
        addPanels(console, isoformPlot, tSNEPlot);
        setUpPathComboBox();
    }

    public void disable() {
        loadButton.setDisable(true);
        viewMenu.setDisable(true);
    }

    public void enable() {
        loadButton.setDisable(false);
        viewMenu.setDisable(false);
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
            stage.show();
        }
        catch (IOException e) {
            ControllerMediator.getInstance().addConsoleErrorMessage("Could not load about window");
        }
    }

    /**
     * When t-SNE toggle is pressed, toggles visibility of the t-SNE plot
     */
    @FXML
    protected void handleTSNEViewToggle() {
        if (tSNEPlotOpen) {
            horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getTSNEPlot());
            tSNEToggle.setText("Open t-SNE Plot");
            tSNEPlotOpen = false;
        } else {
            horizontalSplitPane.getItems().add(ControllerMediator.getInstance().getTSNEPlot());
            tSNEToggle.setText("Close t-SNE Plot");
            tSNEPlotOpen = true;
        }
    }

    /**
     * When isoform plot toggle is pressed, toggles visibility of the isoform plot
     */
    @FXML
    protected void handleIsoformViewToggle() {
        if (isoformPlotOpen) {
            horizontalSplitPane.getItems().remove(ControllerMediator.getInstance().getIsoformPlot());
            isoformPlotToggle.setText("Open Isoform Plot");
            isoformPlotOpen = false;
        } else {
            horizontalSplitPane.getItems().add(0, ControllerMediator.getInstance().getIsoformPlot());
            isoformPlotToggle.setText("Close Isoform Plot");
            isoformPlotOpen = true;
        }
    }

    /**
     * When console toggle is pressed, toggles visibility of the console
     */
    @FXML
    protected void handleConsoleViewToggle() {
        if (consoleOpen) {
            verticalSplitPane.getItems().remove(ControllerMediator.getInstance().getConsole());
            consoleToggle.setText("Open Console");
            consoleOpen = false;
        } else {
            verticalSplitPane.getItems().add(1, ControllerMediator.getInstance().getConsole());
            verticalSplitPane.setDividerPosition(0, 1);
            consoleToggle.setText("Close Console");
            consoleOpen = true;
        }
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
     * When load button is pressed, parses path to file and retrieves parsed genes
     * Clears isoform plot and shown genes in genes selector
     * Updates genes selector's genes
     * Adds file path to path's list of previous file paths
     * Displays error (and successful completion) messages in console
     */
    @FXML
    protected void handleLoadButtonAction() {
        ControllerMediator.getInstance().clearShownGenes();
        disableAssociatedFunctionality();
        try {
            Thread fileLoaderThread = new Thread(new FileLoaderThread());
            fileLoaderThread.start();
        } catch (Exception e) {
            enableAssociatedFunctionality();
            ControllerMediator.getInstance().addConsoleErrorMessage("reading file from path: " + path.getValue());
        }
    }

    private void disableAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().clearTSNEPlot();
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

        @Override
        public void run() {
            try {
                runLater(() ->  ControllerMediator.getInstance().addConsoleMessage("Loading file from path: " + path.getValue()));
                Parser.readFile((String) path.getValue());
                runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Successfully loaded file from path: " + path.getValue()));
                runLater(MainController.this::addLoadedPaths);
            } catch (RNAScoopException e){
                Parser.removeParsedGenes();
                runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage()));
            } catch (FileNotFoundException e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage("Could not find file at path: " + path.getValue()));
            } catch (Exception e) {
                Parser.removeParsedGenes();
                e.printStackTrace();
                runLater(() -> ControllerMediator.getInstance().addConsoleUnexpectedErrorMessage("reading file from path: " + path.getValue()));
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
        tSNEPlotOpen = true;
        consoleOpen = true;
        isoformPlotOpen = true;
    }

    /**
     * Automatically resizes path combo box when window is resized; removes
     * initial focus from path combo box
     */
    private void setUpPathComboBox() {
        window.widthProperty().addListener((observable, oldValue, newValue) -> path.setPrefWidth(window.getWidth() - 85));
        runLater(() -> window.requestFocus());
    }

    private void addLoadedPaths() {
        ObservableList<String> addedPaths = path.getItems();
        if (!addedPaths.contains(path.getValue())) {
            addedPaths.add((String) path.getValue());
        }
    }
}
