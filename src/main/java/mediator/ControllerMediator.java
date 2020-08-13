package mediator;

import annotation.Gene;
import controller.*;
import controller.clusterview.ClusterViewController;
import controller.clusterview.ClusterViewSettingsController;
import controller.clusterview.TSNESettingsController;
import controller.clusterview.UMAPSettingsController;
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
    private ClusterViewController clusterViewController;
    private GeneSelectorController geneSelectorController;
    private TPMGradientAdjusterController tpmGradientAdjusterController;
    private LabelSetManagerController labelSetManagerController;
    private AddLabelSetViewController addLabelSetViewController;
    private ClusterViewSettingsController clusterViewSettingsController;
    private UMAPSettingsController umapSettingsController;
    private TSNESettingsController tsneSettingsController;

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
    public void registerClusterViewController(ClusterViewController clusterViewController) {
        this.clusterViewController = clusterViewController;
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

    @Override
    public void registerClusterViewSettingsController(ClusterViewSettingsController clusterViewSettingsController) {
        this.clusterViewSettingsController = clusterViewSettingsController;
    }

    @Override
    public void registerUMAPSettingsController(UMAPSettingsController umapSettingsController) {
        this.umapSettingsController = umapSettingsController;
    }

    @Override
    public void registerTSNESettingsController(TSNESettingsController tsneSettingsController) {
        this.tsneSettingsController = tsneSettingsController;
    }

    // Change Main Display
    public void initializeMain(Parent console, Parent isoformPlot, Parent clusterView) {
        mainController.initializeMain(console, isoformPlot, clusterView);
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

    public void isoformPlotHandleColoringOrDotPlotChange() {
        isoformPlotController.handleColoringOrDotPlotChange();
    }

    public void clusterViewHandleColoringChange() {
        clusterViewController.handleColoringChange();
    }

    public void updateGeneLabels() {
        isoformPlotController.updateGeneLabels();
    }

    public void updateIsoformLabels() {
        isoformPlotController.updateIsoformLabels();
    }

    public void updateGenesTable(List<Gene> geneList) {
        geneSelectorController.updateGenesTable(geneList);
    }

    public void updateGenesMaxFoldChange() {
        geneSelectorController.updateGenesMaxFoldChange();
    }

    public void updateFoldChangeAlert() {
        geneSelectorController.updateFoldChangeAlert();
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

    public void handleCloseAddLabelSetView() {
        addLabelSetViewController.handleClose();
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
    public void initializeClusterViewSettings(Parent tsneSettings, Parent umapSettings) {
        clusterViewSettingsController.initializeClusterViewSettings(tsneSettings, umapSettings);
    }

    public void displayLabelSetManager() {
        labelSetManagerController.display();
    }

    public void displayClusterViewSettings() {
        clusterViewSettingsController.display();
    }

    public boolean usingUMAPSettings() {
        return clusterViewSettingsController.usingUMAPSettings();
    }

    public void saveUMAPSettings() {
        umapSettingsController.saveSettings();
    }

    public void saveTSNESettings() {
        tsneSettingsController.saveSettings();
    }

    public void restoreUMAPSettingsToSaved() {
        umapSettingsController.restoreSettingsToSaved();
    }

    public void restoreTSNESettingsToSaved() {
        tsneSettingsController.restoreSettingsToSaved();
    }

    public float getMinDist() {
        return umapSettingsController.getMinDist();
    }

    public int getNearestNeighbors() {
        return umapSettingsController.getNearestNeighbors();
    }

    public double getPerplexity() {
        return tsneSettingsController.getPerplexity();
    }

    public int getMaxIterations() {
        return tsneSettingsController.getMaxIterations();
    }

    public void clearCellPlot() {
        clusterViewController.clearPlot();
    }

    public void clearSelectedCellsAndRedrawPlot() {
        clusterViewController.clearSelectedCellsAndRedrawPlot();
    }

    public void redrawCellPlotSansLegend() {
        clusterViewController.redrawPlotSansLegend();
    }

    public void clusterViewHandleClusterAddedFromSelectedCells() {
        clusterViewController.handleClusterAddedFromSelectedCells();
    }

    public void clusterViewHandleRemovedCluster(Cluster removedCluster, Cluster clusterMergedInto) {
        clusterViewController.handleRemovedCluster(removedCluster, clusterMergedInto);
    }

    public void clusterViewHandleChangedLabelSetInUse(){
        clusterViewController.handleChangedLabelSetInUse();
    }

    public void redrawCellPlot() {
        clusterViewController.redrawPlot();
    }

    public void drawCellPlot() {
        clusterViewController.drawPlot();
    }

    public void redrawLegend() {
        clusterViewController.redrawLegend();
    }

    public void selectCellsIsoformsExpressedIn(Collection<String> isoformIDs) {
        clusterViewController.selectCellsIsoformsExpressedIn(isoformIDs);
    }

    public void selectCluster(Cluster cluster, boolean unselectRest) {
        clusterViewController.selectCluster(cluster, unselectRest);
    }

    public void unselectCluster(Cluster cluster) {
        clusterViewController.unselectCluster(cluster);
    }

    //Load from JSON

    public void restoreMainFromJSON(Map settings) {
        mainController.restoreMainFromJSON(settings);
    }

    public void restoreConsoleFromJSON(Map settings) {
        consoleController.restoreConsoleFromJSON(settings);
    }

    // Getters
    public Node getClusterView() {
        return clusterViewController.getClusterView();
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

    public Map<Integer, ClusterViewController.CellDataItem> getCellNumberCellMap() {
        return clusterViewController.getCellNumberCellMap();
    }

    public Collection<String> getSelectedIsoformIDs() {
        return isoformPlotController.getSelectedIsoformIDs();
    }

    public boolean areCellsSelected () {
        return clusterViewController.areCellsSelected();
    }

    public Collection<ClusterViewController.CellDataItem> getCells(boolean onlySelected) {
        return clusterViewController.getCells(onlySelected);
    }

    public Color getColorFromTPMGradient(double expression) {
        return tpmGradientAdjusterController.getColorFromTPMGradient(expression);
    }

    public LabelSet getLabelSetInUse() {
        return labelSetManagerController.getLabelSetInUse();
    }

    public int getNumCellsToPlot() {
        return clusterViewController.getNumCellsToPlot();
    }

    public List<Cluster> getClusters(boolean onlySelected) {
        return clusterViewController.getClusters(onlySelected);
    }

    public Collection<ClusterViewController.CellDataItem> getSelectedCellsInCluster(Cluster cluster) {
        return clusterViewController.getSelectedCellsInCluster(cluster);
    }

    public int getNumExpressingCells(String isoformID, Cluster cluster, boolean onlySelected) {
        return clusterViewController.getNumExpressingCells(isoformID, cluster, onlySelected);
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

    public boolean isShowingMedian() {
        return mainController.isShowingMedian();
    }

    public boolean isShowingAverage() {
        return mainController.isShowingAverage();
    }

    public boolean isIncludingZeros() {
        return mainController.isIncludingZeros();
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

    public boolean isColoringCellPlotBySelectedIsoform() {
        return mainController.isColoringCellPlotBySelectedIsoform();
    }

    public boolean isConsoleOpen() {
        return mainController.isConsoleOpen();
    }

    public boolean isIsoformPlotOpen() {
        return mainController.isIsoformPlotOpen();
    }

    public boolean isClusterViewOpen() {
        return mainController.isClusterViewOpen();
    }

    public boolean isCellPlotCleared() {
        return clusterViewController.isPlotCleared();
    }

    public boolean isAddLabelSetViewDisplayed() {
        return addLabelSetViewController.isDisplayed();
    }

    //Setters
    public void setCellIsoformExpressionMatrix(double[][] cellIsoformExpressionMatrix) {
        clusterViewController.setCellIsoformExpressionMatrix(cellIsoformExpressionMatrix);
    }

    public void setIsoformIndexMap(HashMap<String, Integer> isoformIndexMap) {
        clusterViewController.setIsoformIndexMap(isoformIndexMap);
    }

    public void setEmbedding(double[][] embedding) {
        clusterViewController.setEmbedding(embedding);
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

    public void disableClusterView() {
        clusterViewController.disable();
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

    public void enableClusterView() {
        clusterViewController.enable();
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
