package controller.labelsetmanager;

import controller.PopUpController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import labelset.Cluster;
import labelset.LabelSet;
import mediator.ControllerMediator;
import org.json.JSONObject;
import parser.Parser;
import persistance.CurrentSession;
import persistance.SessionMaker;
import ui.LabelSetManagerWindow;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static javafx.application.Platform.runLater;

public class LabelSetManagerController extends PopUpController {
    @FXML private ScrollPane labelSetManager;
    @FXML private ListView labelSetsListView;
    @FXML private MenuButton addLabelSetButton;
    @FXML private MenuItem addFromCellSelectionOption;
    @FXML private Button removeLabelSetButton;
    @FXML private Button exportLabelSetButton;

    private ObservableList<LabelSet> labelSets;
    private LabelSet labelSetInUse;
    private boolean calculatingLabelSetInUseFoldChanges;

    public void initializeLabelSetManager(LabelSetManagerWindow window) {
        this.window = window;
        setUpLabelSetsListView();
        addFromCellSelectionOption.setDisable(true);
        calculatingLabelSetInUseFoldChanges = false;
    }

    /**
     * Disables all functionality
     */
    public void disable() {
        labelSetsListView.setDisable(true);
        removeLabelSetButton.setDisable(true);
        addLabelSetButton.setDisable(true);
        exportLabelSetButton.setDisable(true);
    }

    /**
     * Enables all functionality
     */
    public void enable() {
        labelSetsListView.setDisable(false);
        removeLabelSetButton.setDisable(false);
        exportLabelSetButton.setDisable(false);
        addLabelSetButton.setDisable(false);
        if (!ControllerMediator.getInstance().isCellPlotCleared() && addFromCellSelectionOption.isDisable())
            addFromCellSelectionOption.setDisable(false);

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
        addFromCellSelectionOption.setDisable(true);
    }

    public void addLabelSets(Collection<LabelSet> labelSets) {
        this.labelSets.addAll(labelSets);
        labelSetInUse = this.labelSets.get(0);
        labelSetsListView.getSelectionModel().select(labelSetInUse);
    }

    /**
     * Adds given label set to the list of label sets and selects it (making it the
     * label set in use)
     */
    public void addLabelSet(LabelSet labelSet) {
        labelSets.add(labelSet);
        labelSetInUse = labelSet;
        labelSetsListView.getSelectionModel().select(labelSet);
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

    public void exportLabelSetsToFiles(String pathToDir) {
        for (LabelSet labelSet : labelSets) {
            if (!CurrentSession.isLabelSetPathSaved(labelSet)) {
                String name = labelSet.getName();
                String extension = (name.lastIndexOf(".") != -1) ? ".txt" : "";
                File labelSetFile = new File(pathToDir + File.separator + name + extension);
                exportLabelSetToFile(labelSetFile, labelSet);
            }
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

    public void restoreLabelSetManagerFromPrevSession(JSONObject prevSession) {
        String prevLabelSetInUseName = prevSession.getString(SessionMaker.LABEL_SET_IN_USE_KEY);
        for (LabelSet labelSet : labelSets) {
            if (labelSet.getName().equals(prevLabelSetInUseName)) {
                labelSetsListView.getSelectionModel().select(labelSet);
                labelSetInUse = labelSet;
                break;
            }
        }
    }

    /**
     * Adds all cells in cell plot to the cluster they belong to in each
     * label set
     */
    public void addCellsToLabelSetClusters() {
        for (LabelSet labelSet : labelSets)
                labelSet.addCellsToClusters();
    }

    public LabelSet getLabelSetInUse() {
        return labelSetInUse;
    }

    public Collection<LabelSet> getLabelSets() {
        return labelSets;
    }

    public int getNumLabelSets() {
        return labelSets.size();
    }

    /**
     * When "Add label set from cell selection" option is pressed, creates a new label set, and adds it to the list of
     * label sets. Switches the window's display to the "Add Label Set" view, so that users can customize the
     * new label set
     */
    @FXML
    protected void handleAddFromCellSelectionOption() {
        LabelSet labelSet = new LabelSet();
        labelSetInUse = labelSet;
        LabelSetManagerWindow labelSetManagerWindow = (LabelSetManagerWindow) window;
        labelSetManagerWindow.displayAddLabelSetView();
        addLabelSet(labelSet);
    }

    @FXML
    protected void handleAddFromFileOption() {
        disableCalculatingFoldChangeAssociatedFunctionality();

        FileChooser fileChooser = new FileChooser();
        File labelSetFile = fileChooser.showOpenDialog(window);
        if (labelSetFile != null) {
            calculatingLabelSetInUseFoldChanges = !ControllerMediator.getInstance().isCellPlotCleared();
            ControllerMediator.getInstance().addConsoleMessage("Loading label set...");
            boolean successfullyAdded = Parser.loadLabelSet(labelSetFile);
            if (calculatingLabelSetInUseFoldChanges && successfullyAdded) {
                try {
                    Thread foldChangeUpdaterThread = new Thread(new CalculateAndUpdateFoldChangeThread());
                    foldChangeUpdaterThread.start();
                } catch (Exception e) {
                    enableCalculatingFoldChangeAssociatedFunctionality();
                    ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
                }
            } else {
                ControllerMediator.getInstance().addConsoleMessage("Successfully loaded label set");
                enableCalculatingFoldChangeAssociatedFunctionality();
            }
        } else {
            enableCalculatingFoldChangeAssociatedFunctionality();
        }
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
     * If a label set is selected, exports it to a file
     */
    @FXML
    protected void handleExportLabelSetButton() {
        if (labelSetInUse != null) {
            ControllerMediator.getInstance().disableMain();

            FileChooser fileChooser = new FileChooser();
            File labelSetFile = fileChooser.showSaveDialog(window);
            if (labelSetFile != null)
                exportLabelSetToFile(labelSetFile, labelSetInUse);

            ControllerMediator.getInstance().enableMain();
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("No label set to export");
        }
    }

    private void enableCalculatingFoldChangeAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableClusterView();
        ControllerMediator.getInstance().enableClusterViewSettings();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableGeneFilterer();
    }

    private void disableCalculatingFoldChangeAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableClusterView();
        ControllerMediator.getInstance().disableClusterViewSettings();
        ControllerMediator.getInstance().disableGeneSelector();
        ControllerMediator.getInstance().disableGeneFilterer();
    }

    private void exportLabelSetToFile(File labelSetFile, LabelSet labelSet) {
        try {
            FileWriter fileWriter = new FileWriter(labelSetFile);
            fileWriter.write(labelSet.toString());
            fileWriter.close();
            ControllerMediator.getInstance().addConsoleMessage("Exported label set to: " + labelSetFile.getPath());
            CurrentSession.saveLabelSetPath(labelSet, labelSetFile.getAbsolutePath());
        } catch (Exception e) {
            ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
        }
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

    /**
     * Thread which calculates and updates all gene max fold change values for current label set in use
     * (should be used to update fold change values when a label set is uploaded from a file, since
     * calculating fold change values can take a while)
     */
    private class CalculateAndUpdateFoldChangeThread implements Runnable {

        @Override
        public void run() {
            ControllerMediator.getInstance().calculateAndSaveMaxFoldChange(Collections.singletonList(labelSetInUse));
            ControllerMediator.getInstance().updateGenesMaxFoldChange();
            Platform.runLater(() -> ControllerMediator.getInstance().addConsoleMessage("Successfully loaded label set"));
            Platform.runLater(LabelSetManagerController.this::enableCalculatingFoldChangeAssociatedFunctionality);
        }
    }
}
