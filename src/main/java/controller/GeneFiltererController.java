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
import org.json.JSONObject;
import persistance.SessionMaker;
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

    private MutableDouble disMin;
    private MutableDouble disMinPercentExpressed;

    private MutableDouble deMinFoldChange;
    private MutableDouble deMin;
    private MutableDouble deMinPercentExpressed;

    private MutableDouble cseMin;
    private MutableDouble cseMinPercentExpressed;
    private MutableDouble cseMax;
    private MutableDouble cseMaxPercentExpressed;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpFieldUpdating();
        setGeneFilteringParamsToDefault();
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
            cseCategories.getCheckModel().clearChecks();
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

    public void setGeneFilteringParamsToDefault() {
        noneFilterOption.setSelected(true);
        optionFilteringBy = noneFilterOption;

        disMinField.setText(Integer.toString(DEFAULT_DIS_MIN));
        disMin = new MutableDouble(DEFAULT_DIS_MIN);
        disMinPercentExpressedField.setText(Integer.toString(DEFAULT_DIS_MIN_PERCENT_EXPRESSED));
        disMinPercentExpressed = new MutableDouble(DEFAULT_DIS_MIN_PERCENT_EXPRESSED);

        deMinFoldChangeField.setText(Integer.toString(DEFAULT_DE_MIN_FOLD_CHANGE));
        deMinFoldChange = new MutableDouble(DEFAULT_DE_MIN_FOLD_CHANGE);
        deMinField.setText(Integer.toString(DEFAULT_DE_MIN));
        deMin = new MutableDouble(DEFAULT_DE_MIN);
        deMinPercentExpressedField.setText(Integer.toString(DEFAULT_DE_MIN_PERCENT_EXPRESSED));
        deMinPercentExpressed = new MutableDouble(DEFAULT_DIS_MIN_PERCENT_EXPRESSED);

        cseMinField.setText(Integer.toString(DEFAULT_CSE_MIN));
        cseMin = new MutableDouble(DEFAULT_CSE_MIN);
        cseMinPercentExpressedField.setText(Integer.toString(DEFAULT_CSE_MIN_PERCENT_EXPRESSED));
        cseMinPercentExpressed = new MutableDouble(DEFAULT_CSE_MIN_PERCENT_EXPRESSED);
        cseMaxField.setText(Integer.toString(DEFAULT_CSE_MAX));
        cseMax = new MutableDouble(DEFAULT_CSE_MAX);
        cseMaxPercentExpressedField.setText(Integer.toString(DEFAULT_CSE_MAX_PERCENT_EXPRESSED));
        cseMaxPercentExpressed = new MutableDouble(DEFAULT_CSE_MAX_PERCENT_EXPRESSED);
    }


    public void restoreGeneFiltererFromPrevSession(JSONObject prevSession) {
        AtomicBoolean restoredFilteringParams = new AtomicBoolean(false);
        Platform.runLater(() -> {
            restoreDISParamsFromPrevSession(prevSession);
            restoreDEParamsFromPrevSession(prevSession);
            restoreCSEParamsFromPrevSession(prevSession);
            restoredFilteringParams.set(true);
        });
        while (!restoredFilteringParams.get());
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
        return deCategories.getCheckModel().getCheckedItems().stream().map(Cluster::getName).collect(Collectors.toList());
    }

    public Collection<String> getCSESelectedCategories() {
        return cseCategories.getCheckModel().getCheckedItems().stream().map(Cluster::getName).collect(Collectors.toList());
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
        return disMin.doubleValue();
    }

    public double getDISMinPercentExpressed() {
        return disMinPercentExpressed.doubleValue();
    }

    public double getDEMinFoldChange() {
        return deMinFoldChange.doubleValue();
    }

    public double getDEMin() {
        return deMin.doubleValue();
    }

    public double getDEMinPercentExpressed() {
        return deMinPercentExpressed.doubleValue();
    }

    public double getCSEMin() {
        return cseMin.doubleValue();
    }

    public double getCSEMinPercentExpressed() {
        return cseMinPercentExpressed.doubleValue();
    }

    public double getCSEMax() {
        return cseMax.doubleValue();
    }

    public double getCSEMaxPercentExpressed() {
        return cseMaxPercentExpressed.doubleValue();
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

    private void updateClusterDominantIsoforms(Cluster cluster, Isoform isoform, Set<Isoform> dominantIsoforms) {
        double isoformExpression = isoform.getAverageExpressionInCluster(cluster, false, false);
        int isoformNumExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoform.getId(), cluster, false);
        double isoformPercentExpressed = (double) isoformNumExpressingCells / cluster.getCells().size();
        if (isoformExpression >= disMin.doubleValue() && isoformPercentExpressed * 100 >= disMinPercentExpressed.doubleValue()) {
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

        for (Cluster cluster : deCategories.getCheckModel().getCheckedItems()) {
            double expression = isoform.getAverageExpressionInCluster(cluster, false, false);
            int numExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoform.getId(), cluster, false);
            double percentExpressed = (double) numExpressingCells / cluster.getCells().size();
            if (expression > maxExpression && expression > deMin.doubleValue() && percentExpressed * 100 > deMinPercentExpressed.doubleValue())
                maxExpression = expression;
            else if (expression < minExpression)
                minExpression = expression;

        }
        return (maxExpression / minExpression) >= deMinFoldChange.doubleValue();
    }

    private boolean isoformHasCategorySpecificExpression(Isoform isoform) {
        for (Cluster cluster : labelSetFilteringBy.getClusters()) {
            double expression = isoform.getAverageExpressionInCluster(cluster, false, false);
            int numExpressingCells = ControllerMediator.getInstance().getNumExpressingCells(isoform.getId(), cluster, false);
            double percentExpressed = (double) numExpressingCells / cluster.getCells().size();

            if (cseCategories.getCheckModel().isChecked(cluster)) {
                if (expression < cseMin.doubleValue() || percentExpressed * 100 < cseMinPercentExpressed.doubleValue())
                    return false;
            } else {
                if (expression > cseMax.doubleValue() && percentExpressed * 100 > cseMaxPercentExpressed.doubleValue())
                    return false;
            }
        }
        return true;
    }

    private void restoreDISParamsFromPrevSession(JSONObject prevSession) {
        disMin.setValue(prevSession.getDouble(SessionMaker.DIS_MIN_KEY));
        disMinField.setText(Double.toString(disMin.doubleValue()));

        disMinPercentExpressed.setValue(prevSession.getDouble(SessionMaker.DIS_MIN_PERCENT_EXPRESSED_KEY));
        disMinPercentExpressedField.setText(Double.toString(disMinPercentExpressed.doubleValue()));
    }

    private void restoreDEParamsFromPrevSession(JSONObject prevSession) {
        Collection<String> catagoriesToSelect = (List<String>)(List<?>) prevSession.getJSONArray(SessionMaker.DE_SELECTED_CATEGORIES_KEY).toList();

        for (Cluster category : deCategories.getItems()) {
            for (String categoryToSelect : catagoriesToSelect) {
                if (category.getName().equals(categoryToSelect)) {
                    deCategories.getCheckModel().check(category);
                    break;
                }
            }
        }

        deMinFoldChange.setValue(prevSession.getDouble(SessionMaker.DE_MIN_FOLD_CHANGE_KEY));
        deMinFoldChangeField.setText(Double.toString(deMinFoldChange.doubleValue()));

        deMin.setValue(prevSession.getDouble(SessionMaker.DE_MIN_KEY));
        deMinField.setText(Double.toString(deMin.doubleValue()));

        deMinPercentExpressed.setValue(prevSession.getDouble(SessionMaker.DE_MIN_PERCENT_EXPRESSED_KEY));
        deMinPercentExpressedField.setText(Double.toString(deMinPercentExpressed.doubleValue()));
    }

    private void restoreCSEParamsFromPrevSession(JSONObject prevSession) {
        Collection<String> catagoriesToSelect = (List<String>)(List<?>) prevSession.getJSONArray(SessionMaker.CSE_SELECTED_CATEGORIES_KEY).toList();
        for (Cluster category : cseCategories.getItems()) {
            for (String categoryToSelect : catagoriesToSelect) {
                if (category.getName().equals(categoryToSelect)) {
                    cseCategories.getCheckModel().check(category);
                    break;
                }
            }
        }

        cseMin.setValue(prevSession.getDouble(SessionMaker.CSE_MIN_KEY));
        cseMinField.setText(Double.toString(cseMin.doubleValue()));

        cseMinPercentExpressed.setValue(prevSession.getDouble(SessionMaker.CSE_MIN_PERCENT_EXPRESSED_KEY));
        cseMinPercentExpressedField.setText(Double.toString(cseMinPercentExpressed.doubleValue()));

        cseMax.setValue(prevSession.getDouble(SessionMaker.CSE_MAX_KEY));
        cseMaxField.setText(Double.toString(cseMax.doubleValue()));

        cseMaxPercentExpressed.setValue(prevSession.getDouble(SessionMaker.CSE_MAX_PERCENT_EXPRESSED_KEY));
        cseMaxPercentExpressedField.setText(Double.toString(cseMaxPercentExpressed.doubleValue()));
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
                handleChangedFilteringParam(newValue, disMinField, disMin, ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF));
        disMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, disMinPercentExpressedField, disMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

        deMinFoldChangeField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, deMinFoldChangeField, deMinFoldChange, ChangedCutOffEvent.CHANGED_MIN_FOLD_CHANGE_CUTOFF));
        deMinField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, deMinField, deMin, ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF));
        deMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, deMinPercentExpressedField, deMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

        cseMinField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, cseMinField, cseMin, ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF));
        cseMinPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, cseMinPercentExpressedField, cseMinPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));
        cseMaxField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, cseMaxField, cseMax, ChangedCutOffEvent.CHANGED_EXPRESSION_CUTOFF));
        cseMaxPercentExpressedField.focusedProperty().addListener((arg0, oldValue, newValue) ->
                handleChangedFilteringParam(newValue, cseMaxPercentExpressedField, cseMaxPercentExpressed, ChangedCutOffEvent.CHANGED_PERCENT_CUTOFF));

    }

    private void handleChangedFilteringParam(Boolean newValue, TextField field, MutableDouble originalCutOff, ChangedCutOffEvent event) {
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
                field.setText(Double.toString(originalCutOff.doubleValue()));
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
            if (!optionFilteringBy.isSelected())
                optionFilteringBy.setSelected(true);
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
            optionFilteringBy = filterToggles.getSelectedToggle();

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
