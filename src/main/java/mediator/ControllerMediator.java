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
import javafx.scene.paint.LinearGradient;
import javafx.stage.Window;
import labelset.Cluster;
import labelset.LabelSet;
import ui.LabelSetManagerWindow;

import java.util.*;

public class ControllerMediator implements Mediator{
    private MainController mainController;
    private AboutController aboutController;
    private ConsoleController consoleController;
    private IsoformPlotController isoformPlotController;
    private ClusterViewController clusterViewController;
    private GeneSelectorController geneSelectorController;
    private GeneFiltererController geneFiltererController;
    private TPMGradientAdjusterController tpmGradientAdjusterController;
    private LabelSetManagerController labelSetManagerController;
    private AddLabelSetViewController addLabelSetViewController;
    private ClusterViewSettingsController clusterViewSettingsController;
    private UMAPSettingsController umapSettingsController;
    private TSNESettingsController tsneSettingsController;
    private ImageExporterController imageExporterController;

    // Register controllers
    @Override
    public void registerMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void registerAboutController(AboutController aboutController) {
        this.aboutController = aboutController;
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
    public void registerGeneFiltererController(GeneFiltererController geneFiltererController) {
        this.geneFiltererController = geneFiltererController;
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

    @Override
    public void registerImageExporterController(ImageExporterController imageExporterController) {
        this.imageExporterController = imageExporterController;
    }

    // Change Main Display
    public void initializeMain(Parent console, Parent isoformPlot, Parent clusterView) {
        mainController.initializeMain(console, isoformPlot, clusterView);
    }

    // About window

    public void displayAboutWindow() {
        aboutController.display();
    }

    public void clearPathComboBox() {
        mainController.clearPathComboBox();
    }

    // Console Functions
    public void addConsoleErrorMessage(String message) {
        consoleController.addConsoleErrorMessage(message);
    }

    public void addConsoleUnexpectedExceptionMessage(Exception e) {
        consoleController.addConsoleUnexpectedExceptionMessage(e);
    }

    public void clearConsole() {
        consoleController.clearConsole();
    }

    public void addConsoleMessage(String message) {
        consoleController.addConsoleMessage(message);
    }

    // Display Genes
    public void displayGeneFilterer() {
        geneFiltererController.display();
    }

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

    public void updateIsoformPlot(boolean redrawIsoformPlotLegend) {
        isoformPlotController.updateIsoformPlot(redrawIsoformPlotLegend);
    }

    public void updateIsoformPlotLegend(boolean redraw) {
        isoformPlotController.updateIsoformPlotLegend(redraw);
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

    public void isoformPlotHandleExpressionTypeChange() {
        isoformPlotController.handleExpressionTypeChange();
    }

    public void isoformPlotHandleDotPlotChange() {
        isoformPlotController.handleDotPlotChange();
    }

    public void isoformPlotHandleGradientChange() {
        isoformPlotController.handleGradientChange();
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

    public void geneSelectorHandleRemovedLabelSet(LabelSet labelSet) {
        geneSelectorController.handleRemovedLabelSet(labelSet);
    }

    public void updateFilterCellCategories() {
        geneFiltererController.updateFilterCellCategories();
    }

    public void unfilterGenes() {
        geneFiltererController.unfilterGenes();
    }

    public void updateGenesTableFilteringMethod() {
        geneSelectorController.updateGenesTableFilteringMethod();
    }

    public boolean geneHasIsoformSwitches(Gene gene) {
        return geneFiltererController.geneHasIsoformSwitches(gene);
    }

    public boolean geneIsDifferentiallyExpressed(Gene gene) {
        return geneFiltererController.geneIsDifferentiallyExpressed(gene);
    }

    public boolean geneHasCategorySpecificExpression(Gene gene) {
        return geneFiltererController.geneHasCategorySpecificExpression(gene);
    }
    public void updateGenesMaxFoldChange() {
        geneSelectorController.updateGenesMaxFoldChange();
    }

    public void calculateAndSaveMaxFoldChange(Collection<LabelSet> labelSets) {
        geneSelectorController.calculateAndSaveMaxFoldChange(labelSets);
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

    public void closeAddLabelSetViewWithoutSaving() {
        addLabelSetViewController.closeWithoutSaving();
    }

    public void addLabelSet(LabelSet labelSet) {
        labelSetManagerController.addLabelSet(labelSet);
    }

    public void addLabelSets(Collection<LabelSet> labelSets) {
        labelSetManagerController.addLabelSets(labelSets);
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

    public Collection<LabelSet> getLabelSets() {
        return labelSetManagerController.getLabelSets();
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

    public void labelSetManagerHandleClearedCellPlot() {
        labelSetManagerController.handleClearedCellPlot();
    }

    public void geneFiltererHandleClearedCellPlot() {
        geneFiltererController.handleCellClearedPlot();
    }

    public void redrawCellPlotSansLegend() {
        clusterViewController.redrawPlotSansLegend();
    }

    public void clusterViewHandleChangedIsoformSelection() {
        clusterViewController.handleChangedIsoformSelection();
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

    // Export figures

    public void displayImageExporter() {
        imageExporterController.display();
    }

    // Getters
    public Node getClusterView() {
        return clusterViewController.getClusterView();
    }

    public Node getCellClusterPlot() {
        return clusterViewController.getCellClusterPlot();
    }

    public Node getConsole() {
        return consoleController.getConsole();
    }

    public Node getIsoformPlotPanel() {
        return isoformPlotController.getIsoformPlotPanel();
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

    public boolean notFilteringGenes() {
        return geneFiltererController.notFilteringGenes();
    }

    public boolean isFilteringByDominantIsoformSwitching() {
        return geneFiltererController.isFilteringByDominantIsoformSwitching();
    }

    public boolean isFilteringByDifferentialExpression() {
        return geneFiltererController.isFilteringByDifferentialExpression();
    }

    public boolean isFilteringByCategorySpecificExpression() {
        return geneFiltererController.isFilteringByCategorySpecificExpression();
    }

    public Map<Integer, ClusterViewController.CellDataItem> getCellNumberCellMap() {
        return clusterViewController.getCellNumberCellMap();
    }

    public Collection<String> getSelectedIsoformIDs() {
        return isoformPlotController.getSelectedIsoformIDs();
    }

    public boolean areIsoformGraphicsSelected() {
        return isoformPlotController.areIsoformGraphicsSelected();
    }

    public boolean areCellsSelected () {
        return clusterViewController.areCellsSelected();
    }

    public Collection<ClusterViewController.CellDataItem> getCells(boolean onlySelected) {
        return clusterViewController.getCells(onlySelected);
    }

    public double getGradientMinTPM() {
        return tpmGradientAdjusterController.getGradientMinTPM();
    }

    public double getGradientMaxTPM() {
        return tpmGradientAdjusterController.getGradientMaxTPM();
    }

    public double getGradientMidTPM() {
        return tpmGradientAdjusterController.getGradientMidTPM();
    }

    public String getScaleOptionInUse() {
        return tpmGradientAdjusterController.getScaleOptionInUse();
    }

    public Color getColorFromTPMGradient(double expression) {
        return tpmGradientAdjusterController.getColorFromTPMGradient(expression);
    }

    public LinearGradient getTPMGradientFill() {
        return tpmGradientAdjusterController.getTPMGradientFill();
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

    public boolean isShowingIsoformPlotLegend() {
        return mainController.isShowingIsoformPlotLegend();
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

    public Window getMainWindow() {
        return mainController.getMainWindow();
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

    public void disableClusterViewSettings() {
        clusterViewSettingsController.disable();
    }

    public void disableGeneSelector() {
        geneSelectorController.disable();
    }

    public void disableGeneFilterer() {
        geneFiltererController.disable();
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

    public void enableClusterViewSettings() {
        clusterViewSettingsController.enable();
    }

    public void enableIsoformPlot() {
        isoformPlotController.enable();
    }

    public void enableGeneSelector() {
        geneSelectorController.enable();
    }

    public void enableGeneFilterer() {
        geneFiltererController.enable();
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
