package controller;

import exceptions.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import parser.Parser;
import ui.Main;
import util.Util;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;

public class DatasetLoaderController extends PopUpController implements Initializable {
    private static final float DATASET_LOADER_HEIGHT = 780;
    private static final float DATASET_LOADER_WIDTH = 500;

    @FXML private Toggle jsonLoadOption;
    @FXML private Toggle filesLoadOption;
    @FXML private TextField jsonField;
    @FXML private Parent datasetLoader;
    @FXML private TextField gtfField;
    @FXML private TextField matrixField;
    @FXML private TextField isoformIDsField;
    @FXML private VBox labelSets;
    @FXML private TextField embeddingField;
    @FXML private TextField expressionUnitField;

    private Toggle optionLoadedBy;
    private String loadedJSONPath;
    private String loadedGTFPath;
    private String loadedMatrixPath;
    private String loadedIsoformIDsPath;
    private Collection<LabelSetLoaderSection> loadedLabelSets;
    private String loadedEmbeddingPath;
    private String loadedExpressionUnit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addLabelSetLoaderSection();
        setUpWindow();
        setLoadByOptionToDefault();
        runLater(() -> datasetLoader.requestFocus());
    }

    public void disable() {
        datasetLoader.setDisable(true);
    }

    public void enable() {
        datasetLoader.setDisable(false);
    }

    public void clearLoadedDatasetDataAndScreen() {
        clearLoadedDatasetData();
        resetDatasetLoaderFieldsToSaved();
    }

    @FXML
    protected void getJSONFileFromFileChooser() {
        chooseFile(jsonField);
    }

    @FXML
    protected void getGTFFileFromFileChooser() {
        chooseFile(gtfField);
    }

    @FXML
    protected void getMatrixFileFromFileChooser() {
        chooseFile(matrixField);
    }

    @FXML
    protected void getIsoformIDsFileFromFileChooser() {
        chooseFile(isoformIDsField);
    }

    @FXML
    protected void getEmbeddingFileFromFileChooser() {
        chooseFile(embeddingField);
    }

    @FXML
    protected void addLabelSetLoaderSection() {
        labelSets.getChildren().add(new LabelSetLoaderSection());
    }

    @FXML
    protected void removeLabelSetLoaderSection() {
        ObservableList<Node> labelSetLoaderSections = labelSets.getChildren();
        int numLabelSetLoaderSections = labelSetLoaderSections.size();
        if (numLabelSetLoaderSections > 1) {
            labelSetLoaderSections.remove(numLabelSetLoaderSections - 1);
        } else {
            ControllerMediator.getInstance().addConsoleErrorMessage("Dataset must include at least one label set");
        }
    }

    @FXML
    protected void loadDataset() {
        disableLoadingDatasetAssociatedFunctionality();
        try {
            Thread fileLoaderThread = new Thread(new FileLoaderThread());
            fileLoaderThread.start();
        } catch (Exception e) {
            enableLoadingDatasetAssociatedFunctionality();
            ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
        }
    }

    private void chooseFile(TextField field) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            field.setText(file.getAbsolutePath());
        }
    }

    private void resetDatasetLoaderFieldsToSaved() {
        optionLoadedBy.setSelected(true);
        jsonField.setText(loadedJSONPath);
        gtfField.setText(loadedGTFPath);
        matrixField.setText(loadedMatrixPath);
        isoformIDsField.setText(loadedIsoformIDsPath);
        labelSets.getChildren().clear();
        labelSets.getChildren().addAll(loadedLabelSets);
        embeddingField.setText(loadedEmbeddingPath);
        expressionUnitField.setText(loadedExpressionUnit);
    }

    private void saveLoadedDatasetData() {
        optionLoadedBy = (jsonLoadOption.isSelected())? jsonLoadOption : filesLoadOption;
        loadedJSONPath = jsonField.getText();
        loadedGTFPath = gtfField.getText();
        loadedMatrixPath = matrixField.getText();
        loadedIsoformIDsPath = isoformIDsField.getText();
        saveLoadedLabelSets();
        loadedEmbeddingPath = embeddingField.getText();
        loadedExpressionUnit = expressionUnitField.getText();
    }

    private void clearLoadedDatasetData() {
        setLoadByOptionToDefault();
        loadedJSONPath = "";
        loadedGTFPath = "";
        loadedLabelSets = new ArrayList<>();
        loadedLabelSets.add(new LabelSetLoaderSection());
        loadedMatrixPath = "";
        loadedIsoformIDsPath = "";
        loadedEmbeddingPath = "";
        loadedExpressionUnit = "";
    }

    private void saveLoadedLabelSets() {
        List<LabelSetLoaderSection> newLabelSets = (List<LabelSetLoaderSection>) (List<?>) labelSets.getChildren();
        List<LabelSetLoaderSection> newLabelSetsCopy = newLabelSets.stream().map(LabelSetLoaderSection::new).collect(Collectors.toList());
        loadedLabelSets = newLabelSetsCopy;
    }
    public void disableLoadingDatasetAssociatedFunctionality() {
        disable();
        ControllerMediator.getInstance().disableDatasetLoader();
        ControllerMediator.getInstance().disableIsoformPlot();
        ControllerMediator.getInstance().disableGeneSelector();
        ControllerMediator.getInstance().disableClusterView();
        ControllerMediator.getInstance().disableClusterViewSettings();
        ControllerMediator.getInstance().disableGradientAdjuster();
        ControllerMediator.getInstance().disableLabelSetManager();
        ControllerMediator.getInstance().disableGeneFilterer();
        // doesn't disable add label set view, because main should be disabled when
        // that view is active
    }
    public void enableLoadingDatasetAssociatedFunctionality() {
        enable();
        ControllerMediator.getInstance().enableIsoformPlot();
        ControllerMediator.getInstance().enableDatasetLoader();
        ControllerMediator.getInstance().enableGeneSelector();
        ControllerMediator.getInstance().enableClusterView();
        ControllerMediator.getInstance().enableClusterViewSettings();
        ControllerMediator.getInstance().enableGradientAdjuster();
        ControllerMediator.getInstance().enableLabelSetManager();
        ControllerMediator.getInstance().enableGeneFilterer();
    }

    /**
     * Sets up Image Exporter window
     * Makes it so window is hidden when X button is pressed, enables
     * associated functionality (because is disabled when window is displayed)
     */
    private void setUpWindow() {
        window = new Stage();
        window.setTitle("RNA-Scoop - Load Dataset");
        window.getIcons().add(Main.RNA_SCOOP_LOGO);
        setWindowSizeAndDisplay();
        Util.setFieldDragNDrop(jsonField);
        Util.setFieldDragNDrop(gtfField);
        Util.setFieldDragNDrop(matrixField);
        Util.setFieldDragNDrop(isoformIDsField);
        Util.setFieldDragNDrop(embeddingField);
        Util.setFieldDragNDrop(expressionUnitField);
        window.setOnCloseRequest(event -> {
            event.consume();
            resetDatasetLoaderFieldsToSaved();
            window.hide();
        });
    }

    private void setLoadByOptionToDefault() {
        jsonLoadOption.setSelected(true);
        optionLoadedBy = jsonLoadOption;
    }

    private void setWindowSizeAndDisplay() {
        window.setScene(new Scene(datasetLoader, DATASET_LOADER_WIDTH, DATASET_LOADER_HEIGHT));
    }

    private class LabelSetLoaderSection extends HBox {
        private TextField nameField;
        private TextField pathField;

        public LabelSetLoaderSection() {
            setUpLabelSetLoaderSection();
        }

        public LabelSetLoaderSection(LabelSetLoaderSection sectionToCopy) {
            setUpLabelSetLoaderSection();
            nameField.setText(sectionToCopy.getName());
            pathField.setText(sectionToCopy.getPath());
        }

        public String getName() {
            return nameField.getText();
        }

        public String getPath() {
            return pathField.getText();
        }

        private void setUpLabelSetLoaderSection() {
            setAlignment(Pos.CENTER);

            Text nameText = new Text("Name: ");
            nameText.setStyle("-fx-font-size: 12.5;");
            nameField = new TextField();
            nameField.setPrefWidth(100);

            Text pathText = new Text("Path: ");
            pathText.setStyle("-fx-font-size: 12.5;");
            HBox.setMargin(pathText, new Insets(0,0,0,35));

            pathField = new TextField();
            pathField.setPrefWidth(150);
            Util.setFieldDragNDrop(pathField);
            HBox.setHgrow(pathField, Priority.ALWAYS);
            HBox.setMargin(pathField, new Insets(0,10,0,0));

            Button fileChooser = new Button("Find...");
            fileChooser.setOnAction((event) -> chooseFile(pathField));

            getChildren().addAll(nameText, nameField, pathText, pathField, fileChooser);
        }
    }

    private class FileLoaderThread implements Runnable {

        @Override
        public void run() {
            try {
                if (jsonLoadOption.isSelected())
                    loadByJSON();
                else
                    loadByFiles();
            } catch (RNAScoopException e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage()));
            } catch (Exception e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e));
            } finally {
                runLater(DatasetLoaderController.this::enableLoadingDatasetAssociatedFunctionality);
            }
        }

        private void loadByJSON() throws RNAScoopException {
            String jsonPath = getNecessaryPath(jsonField, "JSON file");
            boolean success = Parser.loadJSONFile(jsonPath);
            if (success) {
                saveLoadedDatasetData();
            } else {
                Platform.runLater(DatasetLoaderController.this::clearLoadedDatasetData);
            }
        }

        private void loadByFiles() throws RNAScoopException {
            String gtfPath = getNecessaryPath(gtfField, "GTF file");
            String matrixPath = getNecessaryPath(matrixField, "matrix file");
            String isoformIDsPath = getNecessaryPath(isoformIDsField, "matrix column labels file");
            Map<String, String> labelSets = getLabelSets();
            String embedding = embeddingField.getText();
            String expressionUnit = expressionUnitField.getText();
            boolean success = Parser.loadDatasetFromIndividualPaths(gtfPath, matrixPath, isoformIDsPath, embedding, labelSets, expressionUnit);
            if (success) {
                saveLoadedDatasetData();
            } else {
                Platform.runLater(DatasetLoaderController.this::clearLoadedDatasetData);
            }
        }

        private String getNecessaryPath(TextField field, String fieldFileName) throws RNAScoopException {
            String filePath = field.getText();
            if (filePath.equals("")) {
                throw new DatasetMissingPathException(fieldFileName);
            }
            return filePath;
        }

        private Map<String, String> getLabelSets() throws RNAScoopException{
            Map<String, String> labelSetMap = new HashMap<>();
            for (Node child : labelSets.getChildren()) {
                LabelSetLoaderSection labelSetLoaderSection = (LabelSetLoaderSection) child;
                String name = labelSetLoaderSection.getName();
                if (name.equals(""))
                    throw new LabelSetMissingNameException();
                if (labelSetMap.containsKey(name))
                    throw new LabelSetDuplicateNameException();
                String path = labelSetLoaderSection.getPath();
                if (path.equals(""))
                    throw new LabelSetMissingPathException();
                labelSetMap.put(name, path);
            }
            return labelSetMap;
        }
    }
}
