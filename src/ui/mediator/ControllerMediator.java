package ui.mediator;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import parser.data.Gene;
import ui.controllers.GeneSelectorController;
import ui.controllers.ConsoleController;
import ui.controllers.IsoformPlotController;
import ui.controllers.MainController;
import ui.controllers.TSNEPlotController;

import java.util.Collection;

public class ControllerMediator implements Mediator{
    private MainController mainController;
    private ConsoleController consoleController;
    private IsoformPlotController isoformPlotController;
    private TSNEPlotController tsnePlotController;
    private GeneSelectorController geneSelectorController;

    // Register controllers
    @Override
    public void registerMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void registerConsoleController(ConsoleController consoleController) {
        this.consoleController = consoleController;
    }

    @Override
    public void registerIsoformPlotController(IsoformPlotController isoformPlotController) {
        this.isoformPlotController = isoformPlotController;
    }

    @Override
    public void registerTSNEPlotController(TSNEPlotController tsnePlotController) {
        this.tsnePlotController = tsnePlotController;
    }

    @Override
    public void registerGeneSelectorController(GeneSelectorController geneSelectorController) {
        this.geneSelectorController = geneSelectorController;
    }

    // Change Main Display
    public void initializeMain(Stage window, Parent console, Parent isoformPlot, Parent tSNEPlot) {
        mainController.initializeMain(window, console, isoformPlot, tSNEPlot);
    }

    public void openIsoformPlot() {
        mainController.openIsoformPlot();
    }

    public void closeIsoformPlot() {
        mainController.closeIsoformPlot();
    }

    public void openTSNEPlot() {
        mainController.openTSNEPlot();
    }

    public void closeTSNEPlot() {
        mainController.closeTSNEPlot();
    }

    public void openConsole() {
        mainController.openConsole();
    }

    public void closeConsole() {
        mainController.closeConsole();
    }

    // Console Functions
    public void addConsoleErrorMessage(String message) {
        consoleController.addConsoleErrorMessage(message);
    }

    public void addConsoleUnexpectedErrorMessage(String action) {
        consoleController.addConsoleUnexpectedErrorMessage(action);
    }

    public void clearConsole() {
        consoleController.clearConsole();
    }

    public void addConsoleMessage(String message) {
        consoleController.addConsoleMessage(message);
    }

    // Display Genes
    public void displayGeneSelector() {
        geneSelectorController.display();
    }

    public void toggleReverseComplement() {
        isoformPlotController.toggleReverseComplement();
    }

    public void updateGenes() {
        geneSelectorController.updateGenes();
    }

    public void clearIsoformPlot() {
        isoformPlotController.clearCanvas();
    }

    public void drawGenes(Collection<Gene> genes) {
        isoformPlotController.drawGenes(genes);
    }

    public void clearShownGenes() {
        geneSelectorController.clearShownGenes();
    }

    //Display t-SNE
    public void clearTSNEPlot() {
        tsnePlotController.clearTSNEPlot();
    }

    // Getters
    public Node getTSNEPlot() {
        return tsnePlotController.getTSNEPlot();
    }

    public Node getConsole() {
        return consoleController.getConsole();
    }

    public Node getIsoformPlot() {
        return isoformPlotController.getIsoformPlot();
    }

    public Collection<Gene> getShownGenes() {
        return geneSelectorController.getShownGenes();
    }

    public boolean isConsoleOpen() {
        return mainController.isConsoleOpen();
    }

    public boolean isIsoformPlotOpen() {
        return mainController.isIsoformPlotOpen();
    }

    public boolean isTSNEPlotOpen() {
        return mainController.isTSNEPlotOpen();
    }

    public String getCurrentLoadedPath() {
        return mainController.getCurrentLoadedPath();
    }

    //Setters
    public void setPathComboBoxValue(String path) {
        mainController.setPathComboBoxValue(path);
    }

    //Disable Functionality
    public void disableMain() {
        mainController.disable();
    }

    public void disableIsoformPlot() {
        isoformPlotController.disable();
    }

    public void disableTSNEPlot() {
        tsnePlotController.disable();
    }

    public void disableGeneSelector() {
        geneSelectorController.disable();
    }

    //Enable Functionality
    public void enableMain() {
        mainController.enable();
    }

    public void enableTSNEPlot() {
        tsnePlotController.enable();
    }

    public void enableIsoformPlot() {
        isoformPlotController.enable();
    }

    public void enableGeneSelector() {
        geneSelectorController.enable();
    }

    // Everything below here is in support of Singleton pattern
    private ControllerMediator() {}

    public static ControllerMediator getInstance() {
        return ControllerMediatorHolder.INSTANCE;
    }

    private static class ControllerMediatorHolder {
        private static final ControllerMediator INSTANCE = new ControllerMediator();
    }
}
