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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.stage.Window;
import labelset.Cluster;
import labelset.LabelSet;
import org.json.JSONObject;
import ui.LabelSetManagerWindow;

import java.io.IOException;
import java.util.*;

public class ControllerMediator implements Mediator{
    private MainController mainController;
    private AboutController aboutController;
    private ConsoleController consoleController;
    private IsoformPlotController isoformPlotController;
    private ClusterViewController clusterViewController;
    private GeneSelectorController geneSelectorController;
    private GeneFiltererController geneFiltererController;
    private GradientAdjusterController gradientAdjusterController;
    private LabelSetManagerController labelSetManagerController;
    private AddLabelSetViewController addLabelSetViewController;
    private ClusterViewSettingsController clusterViewSettingsController;
    private UMAPSettingsController umapSettingsController;
    private TSNESettingsController tsneSettingsController;
    private ImageExporterController imageExporterController;
    private DatasetLoaderController datasetLoaderController;

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
    public void registerGradientAdjusterController(GradientAdjusterController gradientAdjusterController) {
        this.gradientAdjusterController = gradientAdjusterController;
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

    @Override
    public void registerDatasetLoaderController(DatasetLoaderController datasetLoaderController) {
        this.datasetLoaderController = datasetLoaderController;
    }

    // Change Main Display
    public void initializeMain(Parent console, Parent isoformPlot, Parent clusterView) {
        mainController.initializeMain(console, isoformPlot, clusterView);
    }

    // About window

    public void displayAboutWindow() {
        aboutController.display();
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

    public void displayGradientAdjuster() {
        gradientAdjusterController.display();
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

    public void setGeneFilteringParamsToDefault() {
        geneFiltererController.setSettingsToDefault();
    }

    public void setGradientToDefault() {
        gradientAdjusterController.setGradientToDefault();
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

    public void exportLabelSetsToFiles(String pathToDir) throws IOException {
        labelSetManagerController.exportLabelSetsToFiles(pathToDir);
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

    public int getNumLabelSetsExported() {
        return labelSetManagerController.getNumLabelSetsExported();
    }

    public boolean hasLabelSetWithName(String name) {
        return labelSetManagerController.hasLabelSetWithName(name);
    }

    public String getUniqueLabelSetName(String name) {
        return labelSetManagerController.getUniqueLabelSetName(name);
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

    public void setClusterViewSettingsToDefault() {
        clusterViewSettingsController.setSettingsToDefault();
    }

    public void setUMAPSettingsToDefault() {
        umapSettingsController.setSettingsToDefault();
    }

    public void setTSNESettingsToDefault() {
        tsneSettingsController.setSettingsToDefault();
    }

    public void restoreUMAPSettingsToSaved() {
        umapSettingsController.restoreSettingsToSaved();
    }

    public void restoreTSNESettingsToSaved() {
        tsneSettingsController.restoreSettingsToSaved();
    }

    public void exportEmbeddingToFile(String pathToDir) throws IOException {
        clusterViewController.exportEmbeddingToFile(pathToDir);
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

    public void selectCluster(Cluster cluster, boolean unselectRest, boolean updateIsoformView) {
        clusterViewController.selectCluster(cluster, unselectRest, updateIsoformView);
    }

    public void unselectCluster(Cluster cluster) {
        clusterViewController.unselectCluster(cluster);
    }

    // Load dataset

    public void displayDatasetLoader() {
        datasetLoaderController.display();
    }

    //Load from JSON

    public void restoreMainFromPrevSession(JSONObject prevSession) {
        mainController.restoreMainFromPrevSession(prevSession);
    }

    public void restoreGradientFromPrevSession(JSONObject prevSession) {
        gradientAdjusterController.restoreGradientFromPrevSession(prevSession);
    }

    public void restoreUMAPSettingsFromPrevSession(JSONObject prevSession) {
        umapSettingsController.restoreSettingsFromPrevSession(prevSession);
    }

    public void restoreTSNESettingsFromPrevSession(JSONObject prevSession) {
        tsneSettingsController.restoreSettingsFromPrevSession(prevSession);
    }

    public void restoreClusterViewSettingsFromPrevSession(JSONObject prevSession) {
        clusterViewSettingsController.restoreClusterViewSettingsFromPrevSession(prevSession);
    }

    public void restoreLabelSetManagerFromPrevSession(JSONObject prevSession) {
        labelSetManagerController.restoreLabelSetManagerFromPrevSession(prevSession);
    }

    public void restoreClusterViewFromPrevSession(JSONObject prevSession) {
        clusterViewController.restoreClusterViewFromPrevSession(prevSession);
    }

    public void restoreGeneFiltererFromPrevSession(JSONObject prevSession) {
        geneFiltererController.restoreGeneFiltererFromPrevSession(prevSession);
    }

    public void restoreGeneSelectorFromPrevSession(JSONObject prevSession) {
        geneSelectorController.restoreGeneSelectorFromPrevSession(prevSession);
    }

    public void restoreIsoformViewFromPrevSession(JSONObject prevSession) {
        isoformPlotController.restoreIsoformPlotFromPrevSession(prevSession);
    }

    // Export figures

    public void displayImageExporter() {
        imageExporterController.display();
    }

    public void setImageExporterSettingsToDefault() {
        imageExporterController.setSettingsToDefault();
    }

    public void restoreImageExporterFromPrevSession(JSONObject prevSession) {
        imageExporterController.restoreImageExporterFromPrevSession(prevSession);
    }

    // Getters
    public Node getClusterView() {
        return clusterViewController.getClusterView();
    }

    public Pane getCellClusterPlot() {
        return clusterViewController.getCellClusterPlot();
    }

    public Node getConsole() {
        return consoleController.getConsole();
    }

    public Node getIsoformPlotPanel() {
        return isoformPlotController.getIsoformPlotPanel();
    }

    public Pane getIsoformPlot() {
        return isoformPlotController.getIsoformPlot();
    }

    public Collection<Gene> getShownGenes() {
        return geneSelectorController.getShownGenes();
    }

    public Collection<String> getShownGeneIDs() {
        return geneSelectorController.getShownGeneIDs();
    }

    public GeneFiltererController.FilterOption getOptionFilteringBy() {
        return geneFiltererController.getOptionFilteringBy();
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

    public double getDISMin() {
        return geneFiltererController.getDISMin();
    }

    public double getDISMinPercentExpressed() {
        return geneFiltererController.getDISMinPercentExpressed();
    }

    public Collection<String> getDESelectedCategories() {
        return geneFiltererController.getDESelectedCategories();
    }

    public Collection<String> getCSESelectedCategories() {
        return geneFiltererController.getCSESelectedCategories();
    }

    public double getDEMinFoldChange() {
        return geneFiltererController.getDEMinFoldChange();
    }

    public double getDEMin() {
        return geneFiltererController.getDEMin();
    }

    public double getDEMinPercentExpressed() {
        return geneFiltererController.getDEMinPercentExpressed();
    }

    public double getCSEMin() {
        return geneFiltererController.getCSEMin();
    }

    public double getCSEMinPercentExpressed() {
        return geneFiltererController.getCSEMinPercentExpressed();
    }

    public double getCSEMax() {
        return geneFiltererController.getCSEMax();
    }

    public double getCSEMaxPercentExpressed() {
        return geneFiltererController.getCSEMaxPercentExpressed();
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

    public String getExpressionUnit() {
        return isoformPlotController.getExpressionUnit();
    }

    public double getGradientMin() {
        return gradientAdjusterController.getGradientMin();
    }

    public double getGradientMax() {
        return gradientAdjusterController.getGradientMax();
    }

    public double getGradientMid() {
        return gradientAdjusterController.getGradientMid();
    }

    public int getRecommendedGradientMin() {
        return gradientAdjusterController.getRecommendedGradientMin();
    }

    public int getRecommendedGradientMax() {
        return gradientAdjusterController.getRecommendedGradientMax();
    }

    public String getScaleOptionInUse() {
        return gradientAdjusterController.getScaleOptionInUse();
    }

    public Color getColorFromGradient(double expression) {
        return gradientAdjusterController.getColorFromGradient(expression);
    }

    public String getGradientMinColorCode() {
        return gradientAdjusterController.getGradientMinColorCode();
    }

    public String getGradientMidColorCode() {
        return gradientAdjusterController.getGradientMidColorCode();
    }

    public String getGradientMaxColorCode() {
        return gradientAdjusterController.getGradientMaxColorCode();
    }

    public LinearGradient getGradientFill() {
        return gradientAdjusterController.getGradientFill();
    }

    public LabelSet getLabelSetInUse() {
        return labelSetManagerController.getLabelSetInUse();
    }

    public int getNumCellsToPlot() {
        return clusterViewController.getNumCellsToPlot();
    }

    public Collection<Integer> getSelectedCellNumbers() {
        return clusterViewController.getSelectedCellNumbers();
    }

    public Collection<String> getSelectedCellCategoryNames() {
        return clusterViewController.getSelectedCellCategoryNames();
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

    public float getFigureScale() {
        return imageExporterController.getFigureScale();
    }

    public String getFigureTypeExporting() {
        return imageExporterController.getFigureTypeExporting();
    }

    public String getCellPlotFigureXAxisLabel() {
        return imageExporterController.getCellPlotFigureXAxisLabel();
    }

    public String getCellPlotFigureYAxisLabel() {
        return imageExporterController.getCellPlotFigureYAxisLabel();
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

    public void setExpressionUnit(String expressionUnit) {
        isoformPlotController.setExpressionUnit(expressionUnit);
    }

    public void setRecommendedGradientMin(int recommendedMin) {
        gradientAdjusterController.setRecommendedMin(recommendedMin);
    }

    public void setRecommendedGradientMax(int recommendedMax) {
        gradientAdjusterController.setRecommendedMax(recommendedMax);
    }

    public void setGradientMaxMinToRecommended() {
        gradientAdjusterController.setGradientMinMaxToRecommended();
    }

    public void addMinExpressionToGradientMinLabel(double min) {
        gradientAdjusterController.addMinExpressionToGradientMinLabel(min);
    }

    public void addMaxExpressionToGradientMaxLabel(double max) {
        gradientAdjusterController.addMaxExpressionToGradientMaxLabel(max);
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

    public void disableClusterView(boolean disableCellSelection) {
        clusterViewController.disable(disableCellSelection);
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

    public void disableGradientAdjuster() {
        gradientAdjusterController.disable();
    }

    public void disableLabelSetManager() {
        labelSetManagerController.disable();
    }

    public void disableDatasetLoader() {
        datasetLoaderController.disable();
    }

    public void disableLoadingDatasetAssociatedFunctionality() {
        mainController.disableLoadingDatasetAssociatedFunctionality();
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

    public void enableGradientAdjuster() {
        gradientAdjusterController.enable();
    }

    public void enableLabelSetManager() {
        labelSetManagerController.enable();
    }

    public void enableDatasetLoader() {
        datasetLoaderController.enable();
    }

    public void enableLoadingDatasetAssociatedFunctionality() {
        mainController.enableLoadingDatasetAssociatedFunctionality();
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
