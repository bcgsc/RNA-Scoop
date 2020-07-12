package mediator;

import annotation.Gene;
import controller.*;
import controller.labelsetmanager.AddLabelSetViewController;
import controller.labelsetmanager.LabelSetManagerController;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import labelset.Cluster;
import labelset.LabelSet;
import ui.LabelSetManagerWindow;

import java.util.*;

public class ControllerMediator implements Mediator{
    private MainController mainController;
    private ConsoleController consoleController;
    private IsoformPlotController isoformPlotController;
    private TSNEPlotController tsnePlotController;
    private GeneSelectorController geneSelectorController;
    private TPMGradientAdjusterController tpmGradientAdjusterController;
    private LabelSetManagerController labelSetManagerController;
    private AddLabelSetViewController addLabelSetViewController;

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
    public void registerLabelSetManagerController(LabelSetManagerController labelSetManagerController) {
        this.labelSetManagerController = labelSetManagerController;
    }

    @Override
    public void registerAddLabelSetViewController(AddLabelSetViewController addLabelSetViewController) {
        this.addLabelSetViewController = addLabelSetViewController;
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

    public void updateDotPlotLegend() {
        isoformPlotController.updateDotPlotLegend();
    }

    public void updateGeneReverseComplementStatus() {
        isoformPlotController.updateGeneReverseComplementStatus();
    }

    public void updateHideSingleExonIsoformsStatus() {
        isoformPlotController.updateHideSingleExonIsoformsStatus();
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

    public void updateGenesTable() {
        geneSelectorController.updateGenesTable();
    }

    public void updateGenesMaxFoldChange() {
        geneSelectorController.updateGenesMaxFoldChange();
    }

    public void updateFoldChangeAlert() {
        geneSelectorController.updateFoldChangeAlert();
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

    public void initializeLabelSetManager(LabelSetManagerWindow window) {
        labelSetManagerController.initializeLabelSetManager(window);
    }

    public void initializeAddLabelSetView(LabelSetManagerWindow window) {
        addLabelSetViewController.initializeAddLabelSetView(window);
    }

    public void prepareAddLabelSetViewForDisplay() {
        addLabelSetViewController.prepareForDisplay();
    }

    public void prepareAddLabelSetViewForClose(boolean saveLabelSet) {
        addLabelSetViewController.prepareForClose(saveLabelSet);
    }

    public void addLabelSet(LabelSet labelSet) {
        labelSetManagerController.addLabelSet(labelSet);
    }

    public void removeLabelSet(LabelSet labelSet) {
        labelSetManagerController.removeLabelSet(labelSet);
    }

    public void clearLabelSets() {
        labelSetManagerController.clearLabelSets();
    }

    public void clearLabelSetClusterCells() {
        labelSetManagerController.clearLabelSetClusterCells();
    }

    public void addCellsToLabelSetClusters() {
        labelSetManagerController.addCellsToLabelSetClusters();
    }

    public int getNumLabelSets() {
        return labelSetManagerController.getNumLabelSets();
    }

    // Display t-SNE
    public void displayClusterManager() {
        labelSetManagerController.display();
    }

    public void clearTSNEPlot() {
        tsnePlotController.clearTSNEPlot();
    }

    public void TSNEPlotHandleClusterAddedFromSelectedCells() {
        tsnePlotController.handleClusterAddedFromSelectedCells();
    }

    public void TSNEPlotHandleRemovedCluster(Cluster removedCluster, Cluster clusterMergedInto) {
        tsnePlotController.handleRemovedCluster(removedCluster, clusterMergedInto);
    }

    public void tSNEPlotHandleChangedLabelSetInUse(){
        tsnePlotController.handleChangedLabelSetInUse();
    }

    public void redrawTSNEPlot() {
        tsnePlotController.redrawTSNEPlot();
    }

    public void redrawTSNEPlotLegend() {
        tsnePlotController.redrawTSNEPlotLegend();
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

    public Map<Integer, TSNEPlotController.CellDataItem> getCellNumberCellMap() {
        return tsnePlotController.getCellNumberCellMap();
    }

    public boolean areCellsSelected () {
        return tsnePlotController.areCellsSelected();
    }

    public Collection<TSNEPlotController.CellDataItem> getCells(boolean onlySelected) {
        return tsnePlotController.getCells(onlySelected);
    }

    public Color getColorFromTPMGradient(double expression) {
        return tpmGradientAdjusterController.getColorFromTPMGradient(expression);
    }

    public LabelSet getLabelSetInUse() {
        return labelSetManagerController.getLabelSetInUse();
    }

    public int getNumCellsToPlot() {
        return tsnePlotController.getNumCellsToPlot();
    }

    public List<Cluster> getClusters(boolean onlySelected) {
        return tsnePlotController.getClusters(onlySelected);
    }

    public Collection<TSNEPlotController.CellDataItem> getSelectedCellsInCluster(Cluster cluster) {
        return tsnePlotController.getSelectedCellsInCluster(cluster);
    }

    public int getNumExpressingCells(String isoformID, Cluster cluster, boolean onlySelected) {
        return tsnePlotController.getNumExpressingCells(isoformID, cluster, onlySelected);
    }

    public boolean isReverseComplementing() {
        return  mainController.isReverseComplementing();
    }

    public boolean isHidingSingleExonIsoforms() {
        return  mainController.isHidingSingleExonIsoforms();
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

    public boolean isAddLabelSetViewDisplayed() {
        return addLabelSetViewController.isDisplayed();
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
        tpmGradientAdjusterController.setGradientMinMaxToRecommended();
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

    public void disableLabelSetManager() {
        labelSetManagerController.disable();
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

    public void enableLabelSetManager() {
        labelSetManagerController.enable();
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
