package controller.labelsetmanager;

import exceptions.RNAScoopException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.converter.DefaultStringConverter;
import labelset.Cluster;
import labelset.LabelSet;
import mediator.ControllerMediator;
import ui.LabelSetManagerWindow;

import java.awt.*;
import java.util.Collections;

public class AddLabelSetViewController {
    @FXML private VBox addLabelSetView;
    @FXML private VBox clustersTableColumn;
    @FXML private TableView clustersTable;
    @FXML private TextField labelSetNameTextField;
    @FXML private Text savingAlert;

    private LabelSetManagerWindow window;
    private LabelSet labelSet;


    public void initializeAddLabelSetView(LabelSetManagerWindow window) {
        this.window = window;
        setUpClustersTable();
        setUpLabelSetNameTextField();
        removeSavingAlert();
    }

    public boolean isDisplayed() {
        return window.isDisplayingAddLabelSetView();
    }

    /**
     * To be run before before "Add Label Set" view is displayed in the Label Set Manager
     * window
     * Gets the label set currently in use and sets it as the label set the user is customizing
     * Sets the clusters in the cluster table to be this label set's clusters
     * Sets the label set name text field to be the name of this label set
     */
    public void prepareForDisplay() {
        labelSet = ControllerMediator.getInstance().getLabelSetInUse();
        clustersTable.setItems(labelSet.getClusters());
        labelSetNameTextField.setText(labelSet.getName());
        disableAssociatedFunctionality();
    }

    public void disable() {
        addLabelSetView.setDisable(true);
    }

    public void enable() {
        addLabelSetView.setDisable(false);
    }

    /**
     * Switches back to the main view, discards the label set the user was working on, and updates
     * the genes' max fold change
     */
    public void closeWithoutSaving() {
        ControllerMediator.getInstance().removeLabelSet(labelSet);
        enableAssociatedFunctionality();
        ControllerMediator.getInstance().updateGenesMaxFoldChange();
        window.displayMainView();
        window.hide();
    }

    /**
     * Adds new cluster from the selected cells to the label set user is customizing,
     * alerts the cluster view of the change, and updates the isoform plot.
     */
    @FXML
    protected void handleMakeClusterButton() {
        try {
            labelSet.addClusterFromSelectedCells();
            ControllerMediator.getInstance().clusterViewHandleClusterAddedFromSelectedCells();
            ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
        } catch (RNAScoopException e) {
            ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage());
        }
    }

    /**
     * If label set only has one cluster adds error message to console saying label set
     * must have at least one cluster
     * Else, removes cluster currently selected from label set, alerts the cluster view
     * of the change and updates the isoform plot
     */
    @FXML
    protected void handleRemoveClusterButton() {
        if (labelSet.getClusters().size() > 1) {
            Cluster clusterToRemove = (Cluster) clustersTable.getSelectionModel().getSelectedItem();
            Cluster clusterCombiningWith = labelSet.getClusterToCombineWith(clusterToRemove);
            labelSet.removeCluster(clusterToRemove);
            ControllerMediator.getInstance().clusterViewHandleRemovedCluster(clusterToRemove, clusterCombiningWith);
            ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("Label set must have at least one cluster");
        }
    }

    /**
     * Saves the label set the user has been customizing. Calculates the genes' fold changes
     * for this label set on another thread
     */
    @FXML
    protected void handleSaveLabelSetButton() {
        ControllerMediator.getInstance().unfilterGenes();
        ControllerMediator.getInstance().updateFilterCellCategories();
        ControllerMediator.getInstance().addConsoleMessage("Saving label set...");
        disableUpdatingFoldChangeAssociatedFunctionality();
        addSavingAlert();
        try {
            Thread foldChangeUpdaterThread = new Thread(new CalculateAndUpdateFoldChangeThread());
            foldChangeUpdaterThread.start();
        } catch (Exception e) {
            enableAssociatedFunctionality();
            enableUpdatingFoldChangeAssociatedFunctionality();
            removeSavingAlert();
            window.displayMainView();
            ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
        }
    }

    @FXML
    protected void handleCancelButton() {
        closeWithoutSaving();
    }

    /**
     * Disables all functionality that should be disabled when the user is customizing the
     * label set
     */
    private void disableAssociatedFunctionality() {
        ControllerMediator.getInstance().disableMain();
        ControllerMediator.getInstance().disableClusterView();
        ControllerMediator.getInstance().disableClusterViewSettings();
        ControllerMediator.getInstance().disableGeneFilterer();
    }

    /**
     * Disables all functionality (additional to that which should be disabled when the user is customizing
     * the label set) that should be disabled when the fold change values are being updated
     */
    private void disableUpdatingFoldChangeAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableGeneSelector();
    }

    /**
     * Enables all functionality that should be disabled when the user is customizing the
     * label set
     */
    private void enableAssociatedFunctionality() {
        ControllerMediator.getInstance().enableMain();
        ControllerMediator.getInstance().enableClusterView();
        ControllerMediator.getInstance().enableClusterViewSettings();
        ControllerMediator.getInstance().enableGeneFilterer();
    }

    /**
     * Enables all functionality (additional to that which should be disabled when the user is customizing
     * the label set) that should be disabled when the fold change values are being updated
     */
    private void enableUpdatingFoldChangeAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableGeneSelector();
    }

    private void addSavingAlert() {
        clustersTableColumn.getChildren().add(savingAlert);
    }

    private void removeSavingAlert() {
        clustersTableColumn.getChildren().remove(savingAlert);
    }

    /**
     * Creates editable clusters table with two columns, one for cluster name and one
     * for cluster color
     */
    private void setUpClustersTable() {
        clustersTable.setEditable(true);
        TableColumn<Cluster, String> clusterNameCol = getClusterNameColumn();
        TableColumn<Cluster, Color> colorCol = getClusterColorColumn();
        clustersTable.getColumns().setAll(clusterNameCol , colorCol);
        clustersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setUpLabelSetNameTextField() {
        labelSetNameTextField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                handleChangedLabelSetName();
            }
        });
        labelSetNameTextField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER))
                handleChangedLabelSetName();
        });
    }

    private void handleChangedLabelSetName() {
        String newName = labelSetNameTextField.getText();
        if (!ControllerMediator.getInstance().hasLabelSetWithName(newName)) {
            ControllerMediator.getInstance().getLabelSetInUse().setName(newName);
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("There is already a label set named " + newName);
            labelSetNameTextField.setText(labelSet.getName());
        }
    }

    /**
     * Returns column that displays the names of clusters in the table. Cells are editable.
     * When the value of a cell changes, the name of the cluster is changed to be the
     * name in the cell and the cell plot legend is redrawn.
     */
    private TableColumn<Cluster, String> getClusterNameColumn() {
        TableColumn<Cluster,String> clusterNameCol = new TableColumn("Name");
        clusterNameCol.setCellValueFactory(new PropertyValueFactory("name"));
        clusterNameCol.setCellFactory(col -> new TextFieldTableCell<Cluster, String>(new DefaultStringConverter()) {
            @Override
            public void commitEdit(String newName) {
                if (labelSet.getClusterWithName(newName) == null) {
                    super.commitEdit(newName);
                    Cluster cluster = (Cluster) getTableRow().getItem();
                    cluster.setName(newName);
                    ControllerMediator.getInstance().redrawLegend();
                } else {
                    super.cancelEdit();
                    if (!super.getItem().equals(newName))
                        ControllerMediator.getInstance().addConsoleErrorMessage("Label set already has a cluster named " + newName);
                }
            }
        });
        return clusterNameCol;
    }

    /**
     * Returns column that displays the colors of clusters in the table. Cells are editable
     * (have color pickers in them).
     * When the value of a cell changes, the color of the cluster is changed to be the
     * color in the cell and the cell plot is redrawn, and the dot plot legend is updated
     */
    private TableColumn<Cluster, Color> getClusterColorColumn() {
        TableColumn<Cluster, Color> colorCol = new TableColumn("Color");
        colorCol.setCellValueFactory(new PropertyValueFactory("color"));
        colorCol.setCellFactory(tableView -> new ColorTableCell<>(colorCol));
        colorCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Cluster, Color> t) -> {
                    Cluster cluster = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    cluster.setColor(t.getNewValue());
                    ControllerMediator.getInstance().redrawCellPlot();
                    ControllerMediator.getInstance().updateDotPlotLegend();
                });
        return colorCol;
    }

    /**
     * Thread which calculates all gene fold change values for this label set, and
     * updates each gene's fold change value
     */
    private class CalculateAndUpdateFoldChangeThread implements Runnable {

        @Override
        public void run() {
            ControllerMediator.getInstance().calculateAndSaveMaxFoldChange(Collections.singletonList(labelSet));
            ControllerMediator.getInstance().updateGenesMaxFoldChange();
            Platform.runLater(() -> {
                enableAssociatedFunctionality();
                enableUpdatingFoldChangeAssociatedFunctionality();
                removeSavingAlert();
                window.displayMainView();
                ControllerMediator.getInstance().addConsoleMessage("Successfully saved label set");
            });
        }
    }

    /**
     * A table cell which has a color picker in it
     */
    private class ColorTableCell<T> extends TableCell<T, Color> {
        private final ColorPicker colorPicker;

        public ColorTableCell(TableColumn<T, Color> column) {
            colorPicker = new ColorPicker();
            colorPicker.setStyle("-fx-color-label-visible: false");
            colorPicker.editableProperty().bind(column.editableProperty());
            colorPicker.disableProperty().bind(column.editableProperty().not());
            colorPicker.setOnShowing(event -> {
                final TableView<T> tableView = getTableView();
                tableView.getSelectionModel().select(getTableRow().getIndex());
                tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);
            });
            colorPicker.setOnAction(event -> {
                javafx.scene.paint.Color color = colorPicker.getValue();
                Color awtColor = new Color((int) Math.round(color.getRed() * 255),
                        (int) Math.round(color.getGreen() * 255),
                        (int) Math.round(color.getBlue() * 255));
                commitEdit(awtColor);
            });
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(Color item, boolean empty) {
            super.updateItem(item, empty);

            setText(null);
            if(empty) {
                setGraphic(null);
            } else {
                javafx.scene.paint.Color javaFXColor = javafx.scene.paint.Color.color((double) item.getRed() / 255,
                                                                                    (double) item.getGreen() / 255,
                                                                                     (double) item.getBlue() / 255);
                colorPicker.setValue(javaFXColor);
                setGraphic(colorPicker);
            }
        }
    }
}
