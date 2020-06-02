package controller.labelsetmanager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import labelset.Cluster;
import labelset.LabelSet;
import mediator.ControllerMediator;
import ui.LabelSetManagerWindow;

import java.awt.*;

public class AddLabelSetViewController {
    @FXML private TableView clustersTable;
    @FXML private TextField labelSetNameTextField;

    private LabelSetManagerWindow window;
    private LabelSet labelSet;


    public void initializeAddLabelSetView(LabelSetManagerWindow window) {
        this.window = window;
        setUpClustersTable();
        setUpLabelSetNameTextField();
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
    }

    /**
     * Adds new cluster from the select cells to the label set user is customizing,
     * alerts the t-SNE plot of the change, and updates the isoform plot
     */
    @FXML
    protected void handleMakeClusterButton() {
        labelSet.addClusterFromSelectedCells();
        ControllerMediator.getInstance().TSNEPlotHandleClusterAddedFromSelectedCells();
        ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
    }

    /**
     * If label set only has one cluster adds error message to console saying label set
     * must have at least one cluster
     * Else, removes cluster currently selected from label set, alerts the t-SNE plot
     * of the change and updates the isoform plot
     */
    @FXML
    protected void handleRemoveClusterButton() {
        if (labelSet.getClusters().size() > 1) {
            Cluster clusterToRemove = (Cluster) clustersTable.getSelectionModel().getSelectedItem();
            Cluster clusterCombiningWith = labelSet.getClusterToCombineWith(clusterToRemove);
            labelSet.removeCluster(clusterToRemove);
            ControllerMediator.getInstance().TSNEPlotHandleRemovedCluster(clusterToRemove, clusterCombiningWith);
            ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("Label set must have at least one cluster");
        }
    }

    /**
     * Changes display back to main view
     */
    @FXML
    protected void handleSaveLabelSetButton() {
        window.displayMainView();
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
                ControllerMediator.getInstance().getLabelSetInUse().setName(labelSetNameTextField.getText());
            }
        });
    }

    /**
     * Returns column that displays the names of clusters in the table. Cells are editable.
     * When the value of a cell changes, the name of the cluster is changed to be the
     * name in the cell and the t-SNE plot legend is redrawn.
     */
    private TableColumn<Cluster, String> getClusterNameColumn() {
        TableColumn<Cluster,String> clusterNameCol = new TableColumn("Name");
        clusterNameCol.setCellValueFactory(new PropertyValueFactory("name"));
        clusterNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        clusterNameCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Cluster, String> t) -> {
                    Cluster cluster = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    cluster.setName(t.getNewValue());
                    ControllerMediator.getInstance().redrawTSNEPlotLegend();
                });
        return clusterNameCol;
    }

    /**
     * Returns column that displays the colors of clusters in the table. Cells are editable
     * (have color pickers in them).
     * When the value of a cell changes, the color of the cluster is changed to be the
     * color in the cell and the t-SNE plot is redrawn, and the dot plot legend is updated
     */
    private TableColumn<Cluster, Color> getClusterColorColumn() {
        TableColumn<Cluster, Color> colorCol = new TableColumn("Color");
        colorCol.setCellValueFactory(new PropertyValueFactory("color"));
        colorCol.setCellFactory(tableView -> new ColorTableCell<>(colorCol));
        colorCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Cluster, Color> t) -> {
                    Cluster cluster = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    cluster.setColor(t.getNewValue());
                    ControllerMediator.getInstance().redrawTSNEPlot();
                    ControllerMediator.getInstance().updateDotPlotLegend();
                });
        return colorCol;
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
