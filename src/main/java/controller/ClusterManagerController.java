package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import ui.Main;
import ui.PointColor;

import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import static javafx.application.Platform.runLater;

public class ClusterManagerController implements Initializable {
    private static final float CLUSTER_CONTROLLER_SCALE_FACTOR = 0.33f;

    @FXML private ScrollPane clusterController;
    // Main View
    @FXML private VBox mainView;
    @FXML private ListView labelSetsListView;
    // Add Cluster View
    @FXML private VBox addClusterView;
    @FXML private TableView clustersTable;
    @FXML private TextField labelSetAddingText;

    private ObservableList<LabelSet> labelSets;
    private LabelSet labelSetInUse;
    private ObservableList<Cluster> clustersInLabelSetAdding;
    private Stage window;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up main view
        setUpLabelSetsListView();
        // Set up add label set view
        setUpClustersTable();
        labelSetAddingText.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                labelSetInUse.setName(labelSetAddingText.getText());
            }
        });

        setUpWindow();
    }

    /**
     * Displays the cluster controller window
     */
    public void display() {
        window.hide();
        window.show();
        runLater(() -> clusterController.requestFocus());
    }

    public void clearLabelSets() {
        labelSets.clear();
        labelSetInUse = null;
    }

    public void clearLabelSetClusterCells() {
        for (LabelSet labelSet : labelSets) {
            for (Cluster cluster : labelSet.getClusters())
                cluster.clearCells();
        }
    }

    public void updateLabelSetClusterCells() {
        Map<Integer, TSNEPlotController.CellDataItem> cellMap = ControllerMediator.getInstance().getCellMap();
        for (Map.Entry<Integer, TSNEPlotController.CellDataItem> entry : cellMap.entrySet()) {
            for (LabelSet labelSet : labelSets)
                labelSet.addCell(entry.getKey(), entry.getValue());
        }
    }

    public void addLabelSet(Map<Integer, Cluster> cellMap) {
        LabelSet labelSet = new LabelSet(cellMap);
        if (!Platform.isFxApplicationThread())
            Platform.runLater(() -> addLabelSet(labelSet));
        else
            addLabelSet(labelSet);
    }

    public LabelSet getLabelSetInUse() {
        return labelSetInUse;
    }

    public List<Cluster> getAllClusters() {
       return labelSetInUse.getClusters();
    }

    public double getIsoformExpressionLevelInCluster(String isoformID, Cluster cluster, boolean onlySelected) {
        double isoformExpressionSum = 0;
        Collection<TSNEPlotController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = ControllerMediator.getInstance().getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();

        int numSelected = cellsInCluster.size();
        for (TSNEPlotController.CellDataItem selectedCell : cellsInCluster)
            isoformExpressionSum += selectedCell.getIsoformExpressionLevel(isoformID);
        return isoformExpressionSum / numSelected;
    }

    public double getFractionOfExpressingCells(String isoformID, Cluster cluster, boolean onlySelected) {
        double numExpressingCells = 0;
        Collection<TSNEPlotController.CellDataItem> cellsInCluster;
        if (onlySelected)
            cellsInCluster = ControllerMediator.getInstance().getSelectedCellsInCluster(cluster);
        else
            cellsInCluster = cluster.getCells();

        int numSelected = cellsInCluster.size();
        for (TSNEPlotController.CellDataItem selectedCell : cellsInCluster) {
            if (selectedCell.getIsoformExpressionLevel(isoformID) > 0)
                numExpressingCells++;
        }
        return numExpressingCells / numSelected;
    }

    // Functionality to handle main view buttons
    @FXML
    protected void handleAddLabelSetButton() {
        LabelSet labelSet = new LabelSet();
        clustersInLabelSetAdding.addAll(labelSet.getClusters());
        addLabelSet(labelSet);
        labelSetAddingText.setText(labelSet.getName());
        clusterController.setContent(addClusterView);
    }

    @FXML
    protected void handleRemoveLabelSetButton() {
        if (labelSets.size() > 1) {
            LabelSet labelSetToRemove = (LabelSet) labelSetsListView.getSelectionModel().getSelectedItem();
            int labelSetToRemoveIndex = labelSets.lastIndexOf(labelSetToRemove);
            labelSets.remove(labelSetToRemoveIndex);
            labelSetsListView.getSelectionModel().select((labelSetToRemoveIndex == 0)? labelSets.get(0) : labelSets.get(labelSetToRemoveIndex - 1));
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("There must be at least one label set");
        }
    }

    // Functionality to handle add label set view buttons
    @FXML
    protected void handleMakeClusterButton() {
        labelSetInUse.addClusterFromSelectedCells();
        ArrayList<Cluster> labelSetAddingClusters = labelSetInUse.getClusters();
        clustersInLabelSetAdding.addAll(labelSetAddingClusters.get(labelSetAddingClusters.size() - 1));
        ControllerMediator.getInstance().handleClusterAddedFromSelectedCells();
        ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
    }

    @FXML
    protected void handleRemoveClusterButton() {
        if (clustersInLabelSetAdding.size() > 1) {
            Cluster clusterToRemove = (Cluster) clustersTable.getSelectionModel().getSelectedItem();
            Cluster clusterCombiningWith = labelSetInUse.getClusterToCombineWith(clusterToRemove);
            labelSetInUse.removeCluster(clusterToRemove);
            clustersInLabelSetAdding.remove(clusterToRemove);
            ControllerMediator.getInstance().handleRemovedCluster(clusterToRemove, clusterCombiningWith);
            ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("Label set must have at least one cluster");
        }
    }

    @FXML
    protected void handleSaveLabelSetButton() {
        clustersInLabelSetAdding.clear();
        clusterController.setContent(mainView);
    }

    private void addLabelSet(LabelSet labelSet) {
        labelSetInUse = labelSet;
        labelSets.add(labelSet);
        labelSetsListView.getSelectionModel().select(labelSetInUse);
    }

    private void setUpLabelSetsListView() {
        labelSets = FXCollections.observableArrayList();
        labelSetsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            labelSetInUse = (LabelSet) newValue;
            ControllerMediator.getInstance().handleChangedLabelSetInUse();
            ControllerMediator.getInstance().updateIsoformGraphicsAndDotPlot();
        });
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
        labelSetsListView.setItems(labelSets);
    }

    private void setUpClustersTable() {
        clustersInLabelSetAdding = FXCollections.observableArrayList();
        clustersTable.setItems(clustersInLabelSetAdding);
        clustersTable.setEditable(true);
        TableColumn<Cluster,String> clusterNameCol = new TableColumn("Name");
        clusterNameCol.setCellValueFactory(new PropertyValueFactory("name"));
        clusterNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        clusterNameCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Cluster, String> t) -> {
                    Cluster cluster = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    cluster.setName(t.getNewValue());
                    ControllerMediator.getInstance().redrawTSNEPlotLegend();
                });
        TableColumn<Cluster, Color> colorCol = new TableColumn("Color");
        colorCol.setCellValueFactory(new PropertyValueFactory("color"));
        colorCol.setCellFactory(tableView -> new ColorTableCell<>(colorCol));
        colorCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Cluster, Color> t) -> {
                    Cluster cluster = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    cluster.setColor(t.getNewValue());
                    ControllerMediator.getInstance().redrawTSNEPlot();
                    ControllerMediator.getInstance().updateDotPlot();
                });
        clustersTable.getColumns().setAll(clusterNameCol , colorCol);
        clustersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Sets up gene selector window
     * Makes it so window is hidden when X button is pressed
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Cluster Label Set Manager");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        window.setAlwaysOnTop(true);
        setWindowSizeAndDisplay();
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void setWindowSizeAndDisplay() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        clusterController.setContent(mainView);
        window.setScene(new Scene(clusterController, screen.getWidth() * CLUSTER_CONTROLLER_SCALE_FACTOR, screen.getHeight() *  CLUSTER_CONTROLLER_SCALE_FACTOR));
    }

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

    public class LabelSet {
        private Map<Integer, Cluster> cellMap;
        private ArrayList<Cluster> clusters;
        private String name;

        private LabelSet() {
            clusters = new ArrayList<>();
            cellMap = new HashMap<>();
            name = "Label Set " + (labelSets.size() + 1);
            Cluster cluster = new Cluster("Cluster 1", this);
            if (!ControllerMediator.getInstance().isTSNEPlotCleared()) {
                for (TSNEPlotController.CellDataItem cell : ControllerMediator.getInstance().getCellMap().values()) {
                    cluster.addCell(cell);
                    cellMap.put(cell.getCellNumber(), cluster);
                }
            } else {
                for (int i = 0; i < ControllerMediator.getInstance().getNumCellsToPlot(); i++)
                    cellMap.put(i, cluster);
            }
            clusters.add(cluster);
        }

        private LabelSet(Map<Integer, Cluster> cellMap) {
            clusters = new ArrayList<>();
            name = "Label Set " + (labelSets.size() + 1);
            this.cellMap = cellMap;
            for (Cluster cluster: new LinkedHashSet<>(cellMap.values())) {
                clusters.add(cluster);
                cluster.setLabelSet(this);
            }
        }

        private void addCell(Integer cellNumber, TSNEPlotController.CellDataItem cell) {
            Cluster cluster = cellMap.get(cellNumber);
            cluster.addCell(cell);
        }

        private void addClusterFromSelectedCells() {
            Set<TSNEPlotController.CellDataItem> selectedCells = ControllerMediator.getInstance().getSelectedCells();
            int clusterNumber = clusters.size() + 1;
            Cluster newCluster = new Cluster("Cluster " + clusterNumber, labelSetInUse, selectedCells);
            for (TSNEPlotController.CellDataItem selectedCell : selectedCells) {
                for (Cluster cluster : clusters) {
                    Set<TSNEPlotController.CellDataItem> clusterCells = cluster.getCells();
                    if (clusterCells.contains(selectedCell)) {
                        clusterCells.remove(selectedCell);
                        break;
                    }
                }
                cellMap.put(selectedCell.getCellNumber(), newCluster);
            }
            clusters.add(newCluster);
        }

        private Cluster getClusterToCombineWith(Cluster cluster) {
            int indexOfClusterToRemove = clusters.lastIndexOf(cluster);
            return clusters.get((indexOfClusterToRemove == 0) ? 1 : indexOfClusterToRemove - 1);
        }

        private void removeCluster(Cluster cluster) {
            Cluster clusterToCombineWith = getClusterToCombineWith(cluster);
            int indexOfClusterToRemove = clusters.lastIndexOf(cluster);
            clusters.remove(indexOfClusterToRemove);
            for (TSNEPlotController.CellDataItem cell : cluster.getCells()) {
                cellMap.put(cell.getCellNumber(), clusterToCombineWith);
                clusterToCombineWith.addCell(cell);
            }
        }

        private void setName(String name) {
            this.name = name;
        }

        private ArrayList<Cluster> getClusters() {
            return clusters;
        }

        public Cluster getCellCluster(Integer cellNumber) {
            return cellMap.get(cellNumber);
        }

        private int getClusterNumber(Cluster cluster){
            return clusters.lastIndexOf(cluster) + 1;
        }

        private String getName() {
            return name;
        }
    }


    public static class Cluster implements Comparable<Cluster> {
        private String name;
        private Color color;
        private LabelSet labelSet;
        private Set<TSNEPlotController.CellDataItem> cells;

        public Cluster(String name) {
            this.name = name;
            cells = new HashSet<>();
            color = PointColor.getColor();
        }

        private Cluster(String name, LabelSet labelSet) {
            this(name);
            this.labelSet = labelSet;
        }

        private Cluster(String name, LabelSet labelSet, Set<TSNEPlotController.CellDataItem> cells) {
            this.name = name;
            this.labelSet = labelSet;
            this.cells = cells;
            color = PointColor.getColor();
        }

        private void addCell(TSNEPlotController.CellDataItem cell) {
            cells.add(cell);
        }

        private void clearCells(){
            cells.clear();
        }

        private void setLabelSet(LabelSet labelSet) {
            this.labelSet = labelSet;
        }

        private void setColor(Color color) {
            this.color = color;
        }

        private void setName(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(Cluster otherCluster) {
            return getNumber() - otherCluster.getNumber();
        }

        public int getNumber() {
            return labelSet.getClusterNumber(this);
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }

        public javafx.scene.paint.Color getJavaFXColor() {
            return javafx.scene.paint.Color.color((double) color.getRed() / 255,
                                                (double) color.getGreen() / 255,
                                                 (double) color.getBlue() / 255);
        }

        public Set<TSNEPlotController.CellDataItem> getCells() {
            return cells;
        }
    }
}
