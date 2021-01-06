package controller;

import annotation.Gene;
import annotation.Isoform;
import exceptions.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import labelset.Cluster;
import labelset.LabelSet;
import mediator.ControllerMediator;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.json.JSONObject;
import persistence.SessionMaker;
import ui.Main;


import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class GeneFiltererController extends PopUpController implements Initializable, InteractiveElementController{
    private static final double GENE_FILTERER_WIDTH = 550;
    private static final double GENE_FILTERER_HEIGHT = 600;
    // Dominant isoform switching (DIS)
    private static final int DEFAULT_DIS_MIN = 10;
    private static final int DEFAULT_DIS_MIN_PERCENT_EXPRESSED = 50;
    // Differential Expression (DE)
    private static final int DEFAULT_DE_MIN_FOLD_CHANGE = 2;
    private static final int DEFAULT_DE_MIN = 10;
    private static final int DEFAULT_DE_MIN_PERCENT_EXPRESSED = 50;
    // Category-specific expression (CSE)
    private static final int DEFAULT_CSE_MIN = 10;
    private static final int DEFAULT_CSE_MIN_PERCENT_EXPRESSED = 75;
    private static final int DEFAULT_CSE_MAX = 25;
    private static final int DEFAULT_CSE_MAX_PERCENT_EXPRESSED = 25;

    @FXML private Parent geneFilterer;
    @FXML private ToggleGroup filterToggles;

    @FXML private RadioButton noneFilterOption;
    // Dominant isoform switching (DIS)
    @FXML private RadioButton disFilterOption;
    @FXML private TextField disMinField;
    @FXML private TextField disMinPercentExpressedField;
    // Differential Expression (DE)
    @FXML private RadioButton deFilterOption;
    @FXML private CheckComboBox<Cluster> deCategories;
    @FXML private TextField deMinFoldChangeField;
    @FXML private TextField deMinField;
    @FXML private TextField deMinPercentExpressedField;
    // Category-specific expression (CSE)
    @FXML private RadioButton cseFilterOption;
    @FXML private CheckComboBox<Cluster> cseCategories;
    @FXML private TextField cseMinField;
    @FXML private TextField cseMinPercentExpressedField;
    @FXML private TextField cseMaxField;
    @FXML private TextField cseMaxPercentExpressedField;

    private Toggle optionFilteringBy;
    private LabelSet labelSetFilteringBy;

    private MutableDouble tempDISMin;
    private double savedDISMin;
    private MutableDouble tempDISMinPercentExpressed;
    private double savedDISMinPercentExpressed;

    private MutableDouble tempDEMinFoldChange;
    private double savedDEMinFoldChange;
    private MutableDouble tempDEMin;
    private double savedDEMin;
    private MutableDouble tempDEMinPercentExpressed;
    private double savedDEMinPercentExpressed;
    private Collection<Cluster> savedDECategories;

    private MutableDouble tempCSEMin;
    private double savedCSEMin;
    private MutableDouble tempCSEMinPercentExpressed;
    private double savedCSEMinPercentExpressed;
    private MutableDouble tempCSEMax;
    private double savedCSEMax;
    private MutableDouble tempCSEMaxPercentExpressed;
    private double savedCSEMaxPercentExpressed;
    private Collection<Cluster> savedCSECategories;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        savedDECategories = new HashSet<>();
        savedCSECategories = new HashSet<>();
        setUpFieldUpdating();
        setSettingsToDefault();
        setUpWindow();
        disable();
    }

    public void unfilterGenes() {
        noneFilterOption.setSelected(true);
        optionFilteringBy = noneFilterOption;
        ControllerMediator.getInstance().updateGenesTableFilteringMethod();
    }

    public void enable() {
        if (!ControllerMediator.getInstance().isCellPlotCleared())
            geneFilterer.setDisable(false);
    }

    public void disable() {
        geneFilterer.setDisable(true);
    }

    public boolean geneHasIsoformSwitches(Gene gene) {
        Map<Cluster, Set<Isoform>> dominantIsoformsPerCluster = new HashMap<>();
        Collection<Cluster> clusters = labelSetFilteringBy.getClusters();
        for (Cluster cluster : clusters) {
            for (Isoform isoform : gene.getIsoforms()) {
                if (dominantIsoformsPerCluster.containsKey(cluster)) {
                    Set<Isoform> dominantIsoforms = dominantIsoformsPerCluster.get(cluster);
                    updateClusterDominantIsoforms(cluster, isoform, dominantIsoforms);
                } else {
                    Set<Isoform> dominantIsoforms = new HashSet<>();
                    updateClusterDominantIsoforms(cluster, isoform, dominantIsoforms);
                    if (dominantIsoforms.size() > 0)
                        dominantIsoformsPerCluster.put(cluster, dominantIsoforms);
                }
            }
        }

        Collection<Set<Isoform>> dominantIsoformSets = dominantIsoformsPerCluster.values();
        Iterator<Set<Isoform>> dominantIsoformSetsIterator = dominantIsoformSets.iterator();
        if (dominantIsoformSetsIterator.hasNext()) {
            Set<Isoform> clusterDominantIsoforms = dominantIsoformSetsIterator.next();
            while (dominantIsoformSetsIterator.hasNext()) {
                Set<Isoform> otherClusterDominantIsoforms = dominantIsoformSetsIterator.next();
                if (!clusterDominantIsoforms.equals(otherClusterDominantIsoforms)) {
                    return true;
                }
            }
        }
       return false;
    }
    
    public boolean geneIsDifferentiallyExpressed(Gene gene) {
        for (Isoform isoform : gene.getIsoforms()) {
            if (isoformIsDifferentiallyExpressed(isoform))
                return true;
        }
        return false;
    }

    public boolean geneHasCategorySpecificExpression(Gene gene) {
        for (Isoform isoform : gene.getIsoforms()) {
            if (isoformHasCategorySpecificExpression(isoform))
                return true;
        }
        return false;
    }

    public void updateFilterCellCategories() {
        LabelSet labelSet = ControllerMediator.getInstance().getLabelSetInUse();
        if (labelSetFilteringBy != labelSet) {
            deCategories.getCheckModel().clearChecks();
            savedDECategories.clear();
            cseCategories.getCheckModel().clearChecks();
            savedCSECategories.clear();
            ObservableList deCategoryItems = deCategories.getItems();
            ObservableList cseCategoryItems = cseCategories.getItems();
            deCategoryItems.clear();
            cseCategoryItems.clear();

            if (labelSet != null) {
                deCategoryItems.addAll(labelSet.getClusters());
                cseCategoryItems.addAll(labelSet.getClusters());
            }
            labelSetFilteringBy = labelSet;
        }
    }

    public void handleCellClearedPlot() {
        disable();
    }

    public void setSettingsToDefault() {
        noneFilterOption.setSelected(true);

        disMinField.setText(Integer.toString(DEFAULT_DIS_MIN));
        tempDISMin = new MutableDouble(DEFAULT_DIS_MIN);
        disMinPercentExpressedField.setText(Integer.toString(DEFAULT_DIS_MIN_PERCENT_EXPRESSED));
        tempDISMinPercentExpressed = new MutableDouble(DEFAULT_DIS_MIN_PERCENT_EXPRESSED);

        deMinFoldChangeField.setText(Integer.toString(DEFAULT_DE_MIN_FOLD_CHANGE));
        tempDEMinFoldChange = new MutableDouble(DEFAULT_DE_MIN_FOLD_CHANGE);
        deMinField.setText(Integer.toString(DEFAULT_DE_MIN));
        tempDEMin = new MutableDouble(DEFAULT_DE_MIN);
        deMinPercentExpressedField.setText(Integer.toString(DEFAULT_DE_MIN_PERCENT_EXPRESSED));
        tempDEMinPercentExpressed = new MutableDouble(DEFAULT_DIS_MIN_PERCENT_EXPRESSED);

        cseMinField.setText(Integer.toString(DEFAULT_CSE_MIN));
        tempCSEMin = new MutableDouble(DEFAULT_CSE_MIN);
        cseMinPercentExpressedField.setText(Integer.toString(DEFAULT_CSE_MIN_PERCENT_EXPRESSED));
        tempCSEMinPercentExpressed = new MutableDouble(DEFAULT_CSE_MIN_PERCENT_EXPRESSED);
        cseMaxField.setText(Integer.toString(DEFAULT_CSE_MAX));
        tempCSEMax = new MutableDouble(DEFAULT_CSE_MAX);
        cseMaxPercentExpressedField.setText(Integer.toString(DEFAULT_CSE_MAX_PERCENT_EXPRESSED));
        tempCSEMaxPercentExpressed = new MutableDouble(DEFAULT_CSE_MAX_PERCENT_EXPRESSED);

        saveSettings();
    }


    public void restoreGeneFiltererFromPrevSession(JSONObject prevSession) {
        AtomicBoolean restoredSettings = new AtomicBoolean(false);
        Platform.runLater(() -> {
            restoreDISSettingsFromPrevSession(prevSession);
            restoreDESettingsFromPrevSession(prevSession);
            restoreCSESettingsFromPrevSession(prevSession);
            saveSettings();
            restoredSettings.set(true);
        });
        while (!restoredSettings.get());
        filterGenesAsInPreviousSession(prevSession);
    }

    public FilterOption getOptionFilteringBy() {
        if (optionFilteringBy == noneFilterOption)
            return FilterOption.NONE;
        else if (optionFilteringBy == disFilterOption)
            return FilterOption.DIS;
        else if (optionFilteringBy == deFilterOption)
            return FilterOption.DE;
        else
            return FilterOption.CSE;
    }

    public Collection<String> getDESelectedCategories() {
        return savedDECategories.stream().map(Cluster::getName).collect(Collectors.toList());
    }

    public Collection<String> getCSESelectedCategories() {
        return savedCSECategories.stream().map(Cluster::getName).collect(Collectors.toList());
    }

    public boolean isFilteringByDominantIsoformSwitching() {
        return disFilterOption == optionFilteringBy;
    }

    public boolean isFilteringByDifferentialExpression() {
        return deFilterOption == optionFilteringBy;
    }

    public boolean isFilteringByCategorySpecificExpression() {
        return cseFilterOption == optionFilteringBy;
    }

    public double getDISMin() {
        return savedDISMin;
    }

    public double getDISMinPercentExpressed() {
        return savedDISMinPercentExpressed;
    }

    public double getDEMinFoldChange() {
        return savedDEMinFoldChange;
    }

    public double getDEMin() {
        return savedDEMin;
    }

    public double getDEMinPercentExpressed() {
        return savedDEMinPercentExpressed;
    }

    public double getCSEMin() {
        return savedCSEMin;
    }

    public double getCSEMinPercentExpressed() {
        return savedCSEMinPercentExpressed;
    }

    public double getCSEMax() {
        return savedCSEMax;
    }

    public double getCSEMaxPercentExpressed() {
        return savedCSEMaxPercentExpressed;
    }

    @FXML
    protected void handleFilterButton() {
        Toggle optionToFilterBy = filterToggles.getSelectedToggle();
        if (optionToFilterBy == deFilterOption && deCategories.getCheckModel().getCheckedItems().size() < 2) {
            ControllerMediator.getInstance().addConsoleErrorMessage("Must select at least two categories to filter by differential isoform expression");
        } else if (optionToFilterBy == cseFilterOption && cseCategories.getCheckModel().getCheckedItems().isEmpty()) {
            ControllerMediator.getInstance().addConsoleErrorMessage("Must select at least one category to filter by category-specific isoform expression");
        } else {
            disableAssociatedFunctionality();
            try {
                Thread filterGenesThread = new Thread(new FilterGenesThread());
                filterGenesThread.start();
            } catch (Exception e) {
                enableAssociatedFunctionality();
                ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
            }
        }
    }

    private void saveSettings() {
        savedDISMin = tempDISMin.doubleValue();
        savedDISMinPercentExpressed = tempDISMinPercentExpressed.doubleValue();

        savedDEMinFoldChange = tempDEMinFoldChange.doubleValue();
        savedDEMin = tempDEMin.doubleValue();
        savedDEMinPercentExpressed = tempDEMinPercentExpressed.doubleValue();
        savedDECategories.clear();
        savedDECategories.addAll(deCategories.getCheckModel().getCheckedItems());

        savedCSEMin = tempCSEMin.doubleValue();
        savedCSEMinPercentExpressed = tempCSEMinPercentExpressed.doubleValue();
        savedCSEMax = tempCSEMax.doubleValue();
        savedCSEMaxPercentExpressed = tempCSEMaxPercentExpressed.doubleValue();
        savedCSECategories.clear();
        savedCSECategories.addAll(cseCategories.getCheckModel().getCheckedItems());

        optionFilteringBy = filterToggles.getSelectedToggle();
    }

    private void restoreSettingsToSaved() {
        tempDISMin.setValue(savedDISMin);
        disMinField.setText(getStringRepresentationOfNum(savedDISMin));
        tempDISMinPercentExpressed.setValue(savedDISMinPercentExpressed);
        disMinPercentExpressedField.setText(getStringRepresentationOfNum(savedDISMinPercentExpressed));

        tempDEMinFoldChange.setValue(savedDEMinFoldChange);
        deMinFoldChangeField.setText(getStringRepresentationOfNum(savedDEMinFoldChange));
        tempDEMin.setValue(savedDEMin);
        deMinField.setText(getStringRepresentationOfNum(savedDEMin));
        tempDEMinPercentExpressed.setValue(savedDEMinPercentExpressed);
        deMinPercentExpressedField.setText(getStringRepresentationOfNum(savedDEMinPercentExpressed));
        IndexedCheckModel<Cluster> deCheckModel = deCategories.getCheckModel();
        deCheckModel.clearChecks();
        for (Cluster cluster : savedDECategories)
            deCheckModel.check(cluster);

        tempCSEMin.setValue(savedCSEMin);
        cseMinField.setText(getStringRepresentationOfNum(savedCSEMin));
        tempCSEMinPercentExpressed.setValue(savedCSEMinPercentExpressed);
        cseMinPercentExpressedField.setText(getStringRepresentationOfNum(savedCSEMinPercentExpressed));
        tempCSEMax.setValue(savedCSEMax);
        cseMaxField.setText(getStringRepresentationOfNum(savedCSEMax));
        tempCSEMaxPercentExpressed.setValue(savedCSEMaxPercentExpressed);
        cseMaxPercentExpressedField.setText(getStringRepresentationOfNum(savedCSEMaxPercentExpressed));
        IndexedCheckModel<Cluster> cseCheckModel = cseCategories.getCheckModel();
        cseCheckModel.clearChecks();
        for (Cluster cluster : savedCSECategories)
            cseCheckModel.check(cluster);

        optionFilteringBy.setSelected(true);
    }

    private void updateClusterDominantIsoforms(Cluster cluster, Isoform isoform, Set<Isoform> dominantIsoforms) {
        double isoformExpression = isoform.getAverageExpressionInCluster(cluster, false, false);
        int isoformNumExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoform.getId(), cluster, false);
        double isoformPercentExpressed = (double) isoformNumExpressingCells / cluster.getCells().size();
        if (isoformExpression >= savedDISMin && isoformPercentExpressed * 100 >= savedDISMinPercentExpressed) {
            for (Iterator<Isoform> iterator = dominantIsoforms.iterator(); iterator.hasNext();) {
                Isoform dominantIsoform = iterator.next();
                double dominantIsoformExpression = dominantIsoform.getAverageExpressionInCluster(cluster, false, false);
                double expressionRatio = isoformExpression / dominantIsoformExpression;
                if (expressionRatio < (double) 1/1.1) {
                    return;
                } else if (expressionRatio > 1) {
                    if (expressionRatio > 1.1) {
                        iterator.remove();
                    } else {
                        int dominantIsoformNumExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(dominantIsoform.getId(), cluster, false);
                        double dominantIsoformPercentExpressed = (double) dominantIsoformNumExpressingCells / cluster.getCells().size();

                        if (dominantIsoformPercentExpressed < isoformPercentExpressed)
                            iterator.remove();
                    }
                } else {
                    int dominantIsoformNumExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(dominantIsoform.getId(), cluster, false);
                    double dominantIsoformPercentExpressed = (double) dominantIsoformNumExpressingCells / cluster.getCells().size();

                    if (isoformPercentExpressed < dominantIsoformPercentExpressed)
                        return;
                }
            }
            dominantIsoforms.add(isoform);
        }
    }

    private boolean isoformIsDifferentiallyExpressed(Isoform isoform) {
        double minExpression = Double.MAX_VALUE;
        double maxExpression = 0;
        for (Cluster cluster : savedDECategories) {
            double expression = isoform.getAverageExpressionInCluster(cluster, false, false);
            int numExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoform.getId(), cluster, false);
            double percentExpressed = (double) numExpressingCells / cluster.getCells().size();
            if (expression > maxExpression && expression >= savedDEMin && percentExpressed * 100 >= savedDEMinPercentExpressed)
                maxExpression = expression;
            if (expression < minExpression)
                minExpression = expression;

        }
        return (maxExpression / minExpression) >= savedDEMinFoldChange;
    }

    private boolean isoformHasCategorySpecificExpression(Isoform isoform) {
        for (Cluster cluster : labelSetFilteringBy.getClusters()) {
            double expression = isoform.getAverageExpressionInCluster(cluster, false, false);
            int numExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoform.getId(), cluster, false);
            double percentExpressed = (double) numExpressingCells / cluster.getCells().size();

            if (savedCSECategories.contains(cluster)) {
                if (expression < savedCSEMin || percentExpressed * 100 < savedCSEMinPercentExpressed)
                    return false;
            } else {
                if (expression > savedCSEMax && percentExpressed * 100 > savedCSEMaxPercentExpressed)
                    return false;
            }
        }
        return true;
    }

    private void restoreDISSettingsFromPrevSession(JSONObject prevSession) {
        tempDISMin.setValue(prevSession.getDouble(SessionMaker.DIS_MIN_KEY));
        disMinField.setText(getStringRepresentationOfNum(tempDISMin.doubleValue()));

        tempDISMinPercentExpressed.setValue(prevSession.getDouble(SessionMaker.DIS_MIN_PERCENT_EXPRESSED_KEY));
        disMinPercentExpressedField.setText(getStringRepresentationOfNum(tempDISMinPercentExpressed.doubleValue()));
    }

    private void restoreDESettingsFromPrevSession(JSONObject prevSession) {
        Collection<String> catagoriesToSelect = (List<String>)(List<?>) prevSession.getJSONArray(SessionMaker.DE_SELECTED_CATEGORIES_KEY).toList();

        for (Cluster category : deCategories.getItems()) {
            for (String categoryToSelect : catagoriesToSelect) {
                if (category.getName().equals(categoryToSelect)) {
                    deCategories.getCheckModel().check(category);
                    break;
                }
            }
        }

        tempDEMinFoldChange.setValue(prevSession.getDouble(SessionMaker.DE_MIN_FOLD_CHANGE_KEY));
        deMinFoldChangeField.setText(getStringRepresentationOfNum(tempDEMinFoldChange.doubleValue()));

        tempDEMin.setValue(prevSession.getDouble(SessionMaker.DE_MIN_KEY));
        deMinField.setText(getStringRepresentationOfNum(tempDEMin.doubleValue()));

        tempDEMinPercentExpressed.setValue(prevSession.getDouble(SessionMaker.DE_MIN_PERCENT_EXPRESSED_KEY));
        deMinPercentExpressedField.setText(getStringRepresentationOfNum(tempDEMinPercentExpressed.doubleValue()));
    }

    private void restoreCSESettingsFromPrevSession(JSONObject prevSession) {
        Collection<String> catagoriesToSelect = (List<String>)(List<?>) prevSession.getJSONArray(SessionMaker.CSE_SELECTED_CATEGORIES_KEY).toList();
        for (Cluster category : cseCategories.getItems()) {
            for (String categoryToSelect : catagoriesToSelect) {
                if (category.getName().equals(categoryToSelect)) {
                    cseCategories.getCheckModel().check(category);
                    break;
                }
            }
        }

        tempCSEMin.setValue(prevSession.getDouble(SessionMaker.CSE_MIN_KEY));
        cseMinField.setText(getStringRepresentationOfNum(tempCSEMin.doubleValue()));

        tempCSEMinPercentExpressed.setValue(prevSession.getDouble(SessionMaker.CSE_MIN_PERCENT_EXPRESSED_KEY));
        cseMinPercentExpressedField.setText(getStringRepresentationOfNum(tempCSEMinPercentExpressed.doubleValue()));

        tempCSEMax.setValue(prevSession.getDouble(SessionMaker.CSE_MAX_KEY));
        cseMaxField.setText(getStringRepresentationOfNum(tempCSEMax.doubleValue()));

        tempCSEMaxPercentExpressed.setValue(prevSession.getDouble(SessionMaker.CSE_MAX_PERCENT_EXPRESSED_KEY));
        cseMaxPercentExpressedField.setText(getStringRepresentationOfNum(tempCSEMaxPercentExpressed.doubleValue()));
    }
    private void filterGenesAsInPreviousSession(JSONObject prevSession) {
        String optionToFilterBy = prevSession.getString(SessionMaker.OPTION_FILTERING_BY_KEY);
        if (optionToFilterBy.equals(FilterOption.NONE.toString())) {
            noneFilterOption.setSelected(true);
            optionFilteringBy = noneFilterOption;
        } else if (optionToFilterBy.equals(FilterOption.DIS.toString())) {
            disFilterOption.setSelected(true);
            optionFilteringBy = disFilterOption;
        } else if (optionToFilterBy.equals(FilterOption.DE.toString())) {
            deFilterOption.setSelected(true);
            optionFilteringBy = deFilterOption;
        } else {
            cseFilterOption.setSelected(true);
            optionFilteringBy = cseFilterOption;
        }
        if (optionFilteringBy != noneFilterOption)
            Platform.runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Filtering genes as did in previous session..."));
        ControllerMediator.getInstance().updateGenesTableFilteringMethod();
        if (optionFilteringBy != noneFilterOption)
            Platform.runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Finished filtering genes"));
    }

    /**
     * If num has no decimal part, returns string version of num with no decimal (no .0),
     * else returns string num string with decimal
     */
    private String getStringRepresentationOfNum(double num) {
        if (num % 1 == 0)
            return Integer.toString((int) num);
        return Double.toString(num);
    }

    private void enableAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableClusterView();
        ControllerMediator.getInstance().enableClusterViewSettings();
        ControllerMediator.getInstance().enableLabelSetManager();
        ControllerMediator.getInstance().enableGeneSelector();
   }

    private void disableAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableClusterView();
        ControllerMediator.getInstance().disableClusterViewSettings();
        ControllerMediator.getInstance().disableLabelSetManager();
        ControllerMediator.getInstance().disableGeneSelector();
    }

    private void setUpFieldUpdating(){
        disMinField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, disMinField, tempDISMin, ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF));
        disMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, disMinPercentExpressedField, tempDISMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

        deMinFoldChangeField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, deMinFoldChangeField, tempDEMinFoldChange, ChangedCutOffEvent.CHANGED_MIN_FOLD_CHANGE_CUTOFF));
        deMinField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, deMinField, tempDEMin, ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF));
        deMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, deMinPercentExpressedField, tempDEMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

        cseMinField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, cseMinField, tempCSEMin, ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF));
        cseMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, cseMinPercentExpressedField, tempCSEMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));
        cseMaxField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, cseMaxField, tempCSEMax, ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF));
        cseMaxPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringSetting(newValue, cseMaxPercentExpressedField, tempCSEMaxPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

    }

    private void handleChangedFilteringSetting(Boolean newValue, TextField field, MutableDouble originalCutOff, ChangedCutOffEvent event) {
        if (!newValue) { //when focus lost
            String newCutOff = field.getText();
            try {
                if (event.equals(ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF))
                    checkExpressionCutOff(newCutOff);
                else if (event.equals(ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF))
                    checkPercentExpressedCutOff(newCutOff);
                else
                    checkMinFoldChange(newCutOff);
                originalCutOff.setValue(Double.parseDouble(newCutOff));
            } catch (Exception e){
                field.setText(getStringRepresentationOfNum(originalCutOff.doubleValue()));
                ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
            }
        }
    }

    /**
     * Sets up gene selector window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Gene Filterer");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            restoreSettingsToSaved();
            window.hide();
        });
    }

    private void checkExpressionCutOff(String cutOff) throws InvalidExpressionCutOffException {
        double doubleCutOff;
        try {
            doubleCutOff = Double.parseDouble(cutOff);
        } catch (Exception e) {
            throw new InvalidExpressionCutOffException();
        }
        if (doubleCutOff < 0)
            throw new InvalidExpressionCutOffException();
    }

    private void checkPercentExpressedCutOff(String percentExpressedCutOff) throws InvalidPercentCutOff {
        double doublePercentExpressedCutOff;
        try {
            doublePercentExpressedCutOff = Double.parseDouble(percentExpressedCutOff);
        } catch (Exception e) {
            throw new InvalidPercentCutOff();
        }
        if (doublePercentExpressedCutOff < 0 || doublePercentExpressedCutOff >100)
            throw new InvalidPercentCutOff();
    }

    private void checkMinFoldChange(String minFoldChange) throws InvalidMinFoldChangeCutOff {
        double doubleMinFoldChange;
        try {
            doubleMinFoldChange = Double.parseDouble(minFoldChange);
        } catch (Exception e) {
            throw new InvalidMinFoldChangeCutOff();
        }

        if (doubleMinFoldChange < 1)
            throw new InvalidMinFoldChangeCutOff();
    }

    private void setWindowSizeAndDisplay() {
        window.setScene(new Scene(geneFilterer, GENE_FILTERER_WIDTH, GENE_FILTERER_HEIGHT));
    }

    public enum FilterOption {
        NONE, DIS, DE, CSE
    }

    private class FilterGenesThread implements Runnable {

        @Override
        public void run() {
            saveSettings();

            boolean filteringGenes = (optionFilteringBy != noneFilterOption);
            if (filteringGenes)
                Platform.runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Filtering genes..."));

            ControllerMediator.getInstance().updateGenesTableFilteringMethod();

            if (filteringGenes)
                Platform.runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Successfully filtered genes"));
            else
                Platform.runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Stopped filtering genes"));

            enableAssociatedFunctionality();
        }
    }

    private class MutableDouble {
        double value;

        public MutableDouble(double value) {
            this.value = value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public double doubleValue() {
            return value;
        }
    }

    private enum ChangedCutOffEvent {
        CHANGED_EXPRESSION_CUTOFF,
        CHANGED_PERCENT_CUTOFF,
        CHANGED_MIN_FOLD_CHANGE_CUTOFF
    }
}
