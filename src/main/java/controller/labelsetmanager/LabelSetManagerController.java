package controller.labelsetmanager;

import controller.TSNEPlotController;
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
import java.util.List;

import static javafx.application.Platform.runLater;

public class LabelSetManagerController {
    @FXML private ScrollPane labelSetManager;
    @FXML private ListView labelSetsListView;

    private ObservableList<LabelSet> labelSets;
    private LabelSet labelSetInUse;
    private LabelSetManagerWindow window;

    public void initializeLabelSetManager(LabelSetManagerWindow window) {
        this.window = window;
        setUpLabelSetsListView();
    }
    /**
     * Displays the label set manager window
     */
    public void display() {
        window.hide();
        window.show();
        runLater(() -> labelSetManager.requestFocus());
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
     * Adds all cells in t-SNE plot to the cluster they belong to in each
     * label set
     */
    public void addCellsToLabelSetClusters() {
        Map<Integer, TSNEPlotController.CellDataItem> cellNumberCellMap = ControllerMediator.getInstance().getCellNumberCellMap();
        for (TSNEPlotController.CellDataItem cellDataItem : cellNumberCellMap.values()) {
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
        addLabelSetHelper(labelSet);
        window.displayAddLabelSetView();
    }

    /**
     * When Remove Label Set button is pressed, removes selected label set (unless
     * there is only one label set)
     */
    @FXML
    protected void handleRemoveLabelSetButton() {
        removeLabelSet((LabelSet) labelSetsListView.getSelectionModel().getSelectedItem());
    }

    /**
     * Adds given label set to the list of label sets and selects it (making it the
     * label set in use)
     */
    private void addLabelSetHelper(LabelSet labelSet) {
        labelSetInUse = labelSet;
        labelSets.add(labelSet);
        labelSetsListView.getSelectionModel().select(labelSetInUse);
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
            ControllerMediator.getInstance().TSNEPlotHandleChangedLabelSetInUse();
            ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
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
