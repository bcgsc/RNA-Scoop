package mediator;

import annotation.Gene;
import controller.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

import java.util.*;

public class ControllerMediator implements Mediator{
    private MainController mainController;
    private ConsoleController consoleController;
    private IsoformPlotController isoformPlotController;
    private TSNEPlotController tsnePlotController;
    private GeneSelectorController geneSelectorController;
    private TPMGradientAdjusterController tpmGradientAdjusterController;

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

    @Override
    public void registerTPMGradientController(TPMGradientAdjusterController tpmGradientAdjusterController) {
        this.tpmGradientAdjusterController = tpmGradientAdjusterController;
    }

    // Change Main Display
    public void initializeMain(Parent console, Parent isoformPlot, Parent tSNEPlot) {
        mainController.initializeMain(console, isoformPlot, tSNEPlot);
    }

    public void clearPathComboBox() {
        mainController.clearPathComboBox();
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

    public void displayTPMGradientAdjuster() {
        tpmGradientAdjusterController.display();
    }

    public void addGenesToIsoformPlot(Collection<Gene> genes) {
        isoformPlotController.addGenes(genes);
    }

    public void removeGenesFromIsoformPlot(Collection<Gene> genes) {
        isoformPlotController.removeGenes(genes);
    }

    public void redrawIsoformGraphics() {
        isoformPlotController.redrawGraphics();
    }

    public void updateGeneReverseComplementStatus() {
        isoformPlotController.updateGeneReverseComplementStatus();
    }

    public void updateHideIsoformsNoJunctionsStatus() {
        isoformPlotController.updateHideIsoformsNoJunctionsStatus();
    }

    public void updateGeneLabels() {
        isoformPlotController.updateGeneLabels();
    }

    public void updateIsoformLabels() {
        isoformPlotController.updateIsoformLabels();
    }

    public void updateGenes() {
        geneSelectorController.updateGenes();
    }

    public void clearShownGenes() {
        geneSelectorController.clearShownGenes();
    }

    public void clearAllGenes() {
        geneSelectorController.clearAllGenes();
    }

    public void deselectAllIsoforms() {
        isoformPlotController.deselectAllIsoforms();
    }

    //Display t-SNE
    public void clearTSNEPlot() {
        tsnePlotController.clearTSNEPlot();
    }

    public void selectCellsIsoformsExpressedIn(Collection<String> isoformIDs) {
        tsnePlotController.selectCellsIsoformsExpressedIn(isoformIDs);
    }

    //Load from JSON

    public void restoreMainFromJSON(Map settings) {
        mainController.restoreMainFromJSON(settings);
    }

    public void restoreConsoleFromJSON(Map settings) {
        consoleController.restoreConsoleFromJSON(settings);
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

    public String getCurrentLoadedPath() {
        return mainController.getCurrentLoadedPath();
    }

    public ArrayList<ConsoleController.Message> getConsoleMessages() {
        return consoleController.getConsoleMessages();
    }

    public Collection<Gene> getShownGenes() {
        return geneSelectorController.getShownGenes();
    }

    public boolean areCellsSelected () {
        return tsnePlotController.areCellsSelected();
    }

    public double getIsoformExpressionLevel(String isoformID) {
        return tsnePlotController.getIsoformExpressionLevel(isoformID);
    }

    public double getIsoformExpressionLevelInCluster(String isoformID, int clusterNumber) {
        return  tsnePlotController.getIsoformExpressionLevelInCluster(isoformID, clusterNumber);
    }

    public Set<Integer> getSelectedClusterNumbers() {
        return tsnePlotController.getSelectedClusterNumbers();
    }

    public HashMap<Integer, Color> getSelectedClusterColors() {
        return tsnePlotController.getSelectedClusterColors();
    }

    public double getFractionOfExpressingCells(String isoformID, int clusterNumber) {
        return tsnePlotController.getFractionOfExpressingCells(isoformID, clusterNumber);
    }

    public double getGradientMinTPM() {
        return tpmGradientAdjusterController.getGradientMinTPM();
    }

    public double getGradientMaxTPM() {
        return tpmGradientAdjusterController.getGradientMaxTPM();
    }

    public Color getMinTPMColor() {
        return tpmGradientAdjusterController.getMinTPMColor();
    }

    public Color getMaxTPMColor() {
        return tpmGradientAdjusterController.getMaxTPMColor();
    }

    public String getScale() {
        return tpmGradientAdjusterController.getScale();
    }

    public boolean isReverseComplementing() {
        return  mainController.isReverseComplementing();
    }

    public boolean isHidingIsoformsWithNoJunctions() {
        return  mainController.isHidingIsoformsWithNoJunctions();
    }

    public boolean isShowingGeneNameAndID() {
        return  mainController.isShowingGeneAndIDName();
    }

    public boolean isShowingGeneName() {
        return  mainController.isShowingGeneName();
    }

    public boolean isShowingGeneID() {
        return  mainController.isShowingGeneID();
    }

    public boolean isShowingIsoformName() {
        return  mainController.isShowingIsoformName();
    }

    public boolean isShowingIsoformID() {
        return  mainController.isShowingIsoformID();
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

    public boolean isTSNEPlotCleared() {
        return tsnePlotController.isTSNEPlotCleared();
    }

    //Setters
    public void setTSNEPlotInfo(double[][] cellIsoformMatrix, HashMap<String, Integer> isoformIndexMap,
                                ArrayList<String> cellLabels) {
        tsnePlotController.setTSNEPlotInfo(cellIsoformMatrix, isoformIndexMap, cellLabels);
    }

    public void setRecommendedMinTPM(int recommendedMinTPM) {
        tpmGradientAdjusterController.setRecommendedMinTPM(recommendedMinTPM);
    }

    public void setRecommendedMaxTPM(int recommendedMaxTPM) {
        tpmGradientAdjusterController.setRecommendedMaxTPM(recommendedMaxTPM);
    }

    public void setGradientMaxMinToRecommended() {
        tpmGradientAdjusterController.setGradientMaxMinToRecommended();
    }

    public void addMinTPMToGradientMinTPMLabel(double realMinTPM) {
        tpmGradientAdjusterController.addMinTPMToGradientMinTPMLabel(realMinTPM);
    }

    public void addMaxTPMToGradientMaxTPMLabel(double realMaxTPM) {
        tpmGradientAdjusterController.addMaxTPMToGradientMaxTPMLabel(realMaxTPM);
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

    public void disableTPMGradientAdjuster() {
        tpmGradientAdjusterController.disable();
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

    public void enableTPMGradientAdjuster() {
        tpmGradientAdjusterController.enable();
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
