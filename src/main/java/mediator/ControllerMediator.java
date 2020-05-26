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
    private ClusterManagerController clusterManagerController;

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

    @Override
    public void registerClusterManagerController(ClusterManagerController clusterManagerController) {
        this.clusterManagerController = clusterManagerController;
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

    public void updateIsoformGraphicsAndDotPlot() {
            isoformPlotController.updateIsoformGraphicsAndDotPlot();
    }

    public void updateGeneReverseComplementStatus() {
        isoformPlotController.updateGeneReverseComplementStatus();
    }

    public void updateHideIsoformsNoJunctionsStatus() {
        isoformPlotController.updateHideIsoformsNoJunctionsStatus();
    }

    public void updateHideDotPlotStatus() {
        isoformPlotController.updateHideDotPlotStatus();
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

    public void clearGeneSelector() {
        geneSelectorController.clearGeneSelector();
    }

    public void deselectAllIsoforms() {
        isoformPlotController.deselectAllIsoforms();
    }

    // Label Sets Functions
    public void clearLabelSets() {
        clusterManagerController.clearLabelSets();
    }

    public void addLabelSet(Map<Integer, ClusterManagerController.Cluster> cellMap) {
        clusterManagerController.addLabelSet(cellMap);
    }

    public void clearLabelSetClusterCells() {
        clusterManagerController.clearLabelSetClusterCells();
    }

    public void updateLabelSetClusterCells() {
        clusterManagerController.updateLabelSetClusterCells();
    }

    // Display t-SNE
    public void displayClusterManager() {
        clusterManagerController.display();
    }

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

    public Map<Integer, TSNEPlotController.CellDataItem> getCellMap() {
        return tsnePlotController.getCellMap();
    }

    public boolean areCellsSelected () {
        return tsnePlotController.areCellsSelected();
    }

    public double getIsoformExpressionLevel(String isoformID, boolean selectedOnly) {
        return tsnePlotController.getIsoformExpressionLevel(isoformID, selectedOnly);
    }

    public Color getColorFromTPMGradient(double expression) {
        return tpmGradientAdjusterController.getColorFromTPMGradient(expression);
    }

    public ClusterManagerController.LabelSet getLabelSetInUse() {
        return clusterManagerController.getLabelSetInUse();
    }

    public int getNumCellsToPlot() {
        return tsnePlotController.getNumCellsToPlot();
    }

    public double getIsoformExpressionLevelInCluster(String isoformID, ClusterManagerController.Cluster cluster, boolean onlySelected) {
        return  clusterManagerController.getIsoformExpressionLevelInCluster(isoformID, cluster, onlySelected);
    }

    public List<ClusterManagerController.Cluster> getAllClusters() {
        return clusterManagerController.getAllClusters();
    }

    public List<ClusterManagerController.Cluster> getSelectedClusters() {
        return tsnePlotController.getSelectedClusters();
    }

    public Collection<TSNEPlotController.CellDataItem> getSelectedCellsInCluster(ClusterManagerController.Cluster cluster) {
        return tsnePlotController.getSelectedCellsInCluster(cluster);
    }

    public double getFractionOfExpressingCells(String isoformID, ClusterManagerController.Cluster cluster, boolean onlySelected) {
        return clusterManagerController.getFractionOfExpressingCells(isoformID, cluster, onlySelected);
    }

    public boolean isReverseComplementing() {
        return  mainController.isReverseComplementing();
    }

    public boolean isHidingIsoformsWithNoJunctions() {
        return  mainController.isHidingIsoformsWithNoJunctions();
    }

    public boolean isHidingDotPlot() {
        return mainController.isHidingDotPlot();
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
    public void setCellIsoformExpressionMatrix(double[][] cellIsoformExpressionMatrix) {
        tsnePlotController.setCellIsoformExpressionMatrix(cellIsoformExpressionMatrix);
    }

    public void setIsoformIndexMap(HashMap<String, Integer> isoformIndexMap) {
        tsnePlotController.setIsoformIndexMap(isoformIndexMap);
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
