package controller.labelsetmanager;

import controller.PopUpController;
import controller.clusterview.ClusterViewController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import labelset.Cluster;
import labelset.LabelSet;
import mediator.ControllerMediator;
import ui.LabelSetManagerWindow;

import java.util.*;

import static javafx.application.Platform.runLater;

public class LabelSetManagerController extends PopUpController {
    @FXML private ScrollPane labelSetManager;
    @FXML private ListView labelSetsListView;
    @FXML private Button addLabelSetButton;
    @FXML private Button removeLabelSetButton;

    private ObservableList<LabelSet> labelSets;
    private LabelSet labelSetInUse;

    public void initializeLabelSetManager(LabelSetManagerWindow window) {
        this.window = window;
        setUpLabelSetsListView();
        addLabelSetButton.setDisable(true);
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        labelSetsListView.setDisable(true);
        removeLabelSetButton.setDisable(true);
        addLabelSetButton.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        labelSetsListView.setDisable(false);
        removeLabelSetButton.setDisable(false);
        if (!ControllerMediator.getInstance().isCellPlotCleared())
            addLabelSetButton.setDisable(false);
    }

    /**
     * Displays the label set manager window
     */
    @Override
    public void display() {
        super.display();
        runLater(() -> labelSetManager.requestFocus());
    }

    public void handleClearedCellPlot() {
        addLabelSetButton.setDisable(true);
    }

    /**
     * Checks if method is running on the JavaFX Application Thread (necessary because Parser calls this
     * method on a different thread). If running on JavaFX Application thread, adds label set right away,
     * else makes JavaFX Application Thread to do it later
     */
    public void addLabelSet(LabelSet labelSet) {
        if (!Platform.isFxApplicationThread())
            Platform.runLater(() -> addLabelSetHelper(labelSet));
        else
            addLabelSetHelper(labelSet);
    }

    public void clearLabelSets() {
        labelSets.clear();
        labelSetInUse = null;
        ControllerMediator.getInstance().unfilterGenes();
        ControllerMediator.getInstance().updateFilterCellCategories();
    }

    /**
     * If there is more than one label set, removes given label set and selects label set above/below it
     * If there is only one label set adds error message to console saying that there must be at least
     * one label set
     */
    public void removeLabelSet(LabelSet labelSet) {
        if (labelSets.size() > 1) {
            int labelSetToRemoveIndex = labelSets.lastIndexOf(labelSet);
            labelSets.remove(labelSetToRemoveIndex);
            ControllerMediator.getInstance().geneSelectorHandleRemovedLabelSet(labelSet);
            labelSetsListView.getSelectionModel().select((labelSetToRemoveIndex == 0) ? labelSets.get(0) : labelSets.get(labelSetToRemoveIndex - 1));
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("There must be at least one label set");
        }
    }

    /**
     * Clears cells in each cluster in each label set
     */
    public void clearLabelSetClusterCells() {
        for (LabelSet labelSet : labelSets) {
            for (Cluster cluster : labelSet.getClusters())
                cluster.clearCells();
        }
    }

    /**
     * Adds all cells in cell plot to the cluster they belong to in each
     * label set
     */
    public void addCellsToLabelSetClusters() {
        Map<Integer, ClusterViewController.CellDataItem> cellNumberCellMap = ControllerMediator.getInstance().getCellNumberCellMap();
        for (ClusterViewController.CellDataItem cellDataItem : cellNumberCellMap.values()) {
            for (LabelSet labelSet : labelSets)
                labelSet.addCell(cellDataItem);
        }
    }

    public LabelSet getLabelSetInUse() {
        return labelSetInUse;
    }

    public int getNumLabelSets() {
        return labelSets.size();
    }

    /**
     * When Add Label Set button is pressed, creates a new label set, and adds it to the list of label
     * sets. Switches the window's display to the "Add Label Set" view, so that users can customize the
     * new label set
     */
    @FXML
    protected void handleAddLabelSetButton() {
        LabelSet labelSet = new LabelSet();
        labelSetInUse = labelSet;
        LabelSetManagerWindow labelSetManagerWindow = (LabelSetManagerWindow) window;
        labelSetManagerWindow.displayAddLabelSetView();
        addLabelSetHelper(labelSet);
    }

    /**
     * When Remove Label Set button is pressed, removes selected label set (unless
     * there is only one label set)
     */
    @FXML
    protected void handleRemoveLabelSetButton() {
        removeLabelSet(labelSetInUse);
    }

    /**
     * Adds given label set to the list of label sets and selects it (making it the
     * label set in use)
     */
    private void addLabelSetHelper(LabelSet labelSet) {
        labelSets.add(labelSet);
        labelSetsListView.getSelectionModel().select(labelSet);
    }

    private void setUpLabelSetsListView() {
        makeLabelSetInUseBeSelected();
        makeLabelSetsListViewShowName();
        labelSets = FXCollections.observableArrayList();
        labelSetsListView.setItems(labelSets);
    }

    /**
     * Makes label set in use be the selected label set in the label set list view
     */
    private void makeLabelSetInUseBeSelected() {
        labelSetsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            labelSetInUse = (LabelSet) newValue;
            ControllerMediator.getInstance().clusterViewHandleChangedLabelSetInUse();
            ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
            boolean stillEditingLabelSet = ControllerMediator.getInstance().isAddLabelSetViewDisplayed();
            boolean cellPlotCleared = ControllerMediator.getInstance().isCellPlotCleared();
            if (!stillEditingLabelSet && !cellPlotCleared) {
                ControllerMediator.getInstance().unfilterGenes();
                ControllerMediator.getInstance().updateFilterCellCategories();
                ControllerMediator.getInstance().updateGenesMaxFoldChange();
            }
        });
    }

    /**
     * Makes the label set list view show the names of the label sets
     */
    private void makeLabelSetsListViewShowName() {
        labelSetsListView.setCellFactory(
                listview -> new ListCell<LabelSet>() {
                    @Override
                    public void updateItem(LabelSet labelSet, boolean empty) {
                        super.updateItem(labelSet, empty);
                        textProperty().unbind();
                        if(labelSet != null)
                            textProperty().bind(new SimpleStringProperty(labelSet.getName()));
                        else
                            setText(null);
                    }
                }
        );
    }
}
