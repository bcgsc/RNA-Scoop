package ui.mediator;

import javafx.scene.Node;
import javafx.scene.Parent;
import ui.controllers.GeneSelectorController;
import ui.controllers.ConsoleController;
import ui.controllers.IsoformPlotController;
import ui.controllers.MainController;
import ui.controllers.TSNEPlotController;

import java.util.List;

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

    public void initializeMain(Parent console, Parent isoformPlot, Parent tSNEPlot) {
        mainController.initializeMain(console, isoformPlot, tSNEPlot);
    }

    // Add console messages
    public void addConsoleErrorMessage(String message) {
        consoleController.addConsoleErrorMessage(message);
    }

    public void addConsoleUnexpectedErrorMessage(String action) {
        consoleController.addConsoleUnexpectedErrorMessage(action);
    }

    public void addConsoleMessage(String message) {
        consoleController.addConsoleMessage(message);
    }

    // Display Genes
    public void toggleReverseComplement() {
        isoformPlotController.toggleReverseComplement();
    }

    public void updateGenes() {
        geneSelectorController.updateGenes();
    }

    public void clearShownGenes() {
        geneSelectorController.clearShownGenes();
    }

    public void setIsoformPlotShownGenes(List<String> shownGenes) {
        isoformPlotController.setShownGenes(shownGenes);
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

    public void displayGeneSelector() {
        geneSelectorController.display();
    }

    private static class ControllerMediatorHolder {
        private static final ControllerMediator INSTANCE = new ControllerMediator();
    }
}
