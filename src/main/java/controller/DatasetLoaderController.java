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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import parser.Parser;
import ui.Main;
import util.Util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static javafx.application.Platform.runLater;

// TODO: clear???
// TODO: figure out current loaded path thing
public class DatasetLoaderController extends PopUpController implements Initializable {
    private static final float DATASET_LOADER_HEIGHT = 640;
    private static final float DATASET_LOADER_WIDTH = 500;

    @FXML private Parent datasetLoader;
    @FXML private TextField gtfField;
    @FXML private TextField matrixField;
    @FXML private TextField isoformIDsField;
    @FXML private VBox labelSets;
    @FXML private TextField embeddingField;
    @FXML private TextField expressionUnitField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addLabelSetLoaderSection();
        setUpWindow();
        runLater(() -> datasetLoader.requestFocus());
    }

    public void disable() {
        datasetLoader.setDisable(true);
    }

    public void enable() {
        datasetLoader.setDisable(false);
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
        Util.setFieldDragNDrop(gtfField);
        Util.setFieldDragNDrop(matrixField);
        Util.setFieldDragNDrop(isoformIDsField);
        Util.setFieldDragNDrop(embeddingField);
        Util.setFieldDragNDrop(expressionUnitField);
        window.setOnCloseRequest(event -> {
            event.consume();
            window.hide();
        });
    }

    private void setWindowSizeAndDisplay() {
        window.setScene(new Scene(datasetLoader, DATASET_LOADER_WIDTH, DATASET_LOADER_HEIGHT));
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
        ControllerMediator.getInstance().disableLoadingDatasetAssociatedFunctionality();
        try {
            Thread fileLoaderThread = new Thread(new FileLoaderThread());
            fileLoaderThread.start();
        } catch (Exception e) {
            ControllerMediator.getInstance().enableLoadingDatasetAssociatedFunctionality();
            ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e);
        }
    }

    private class LabelSetLoaderSection extends HBox {
        private TextField nameField;
        private TextField pathField;

        public LabelSetLoaderSection() {
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

            getChildren().addAll(nameText, nameField, pathText, pathField);
        }

        public String getName() {
            return nameField.getText();
        }

        public String getPath() {
            return pathField.getText();
        }
    }

    private class FileLoaderThread implements Runnable {

        @Override
        public void run() {
            try {
                String gtfPath = getNecessaryPath(gtfField, "GTF file");
                String matrixPath = getNecessaryPath(matrixField, "matrix file");
                String isoformIDsPath = getNecessaryPath(isoformIDsField, "matrix column labels file");
                Map<String, String> labelSets = getLabelSets();
                String embedding = embeddingField.getText();
                String expressionUnit = expressionUnitField.getText();
                Parser.loadDatasetFromIndividualPaths(gtfPath, matrixPath, isoformIDsPath, embedding, labelSets, expressionUnit);
                ControllerMediator.getInstance().enableLoadingDatasetAssociatedFunctionality();
            } catch (RNAScoopException e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleErrorMessage(e.getMessage()));
            } catch (Exception e) {
                runLater(() -> ControllerMediator.getInstance().addConsoleUnexpectedExceptionMessage(e));
            } finally {
                runLater(() -> ControllerMediator.getInstance().enableLoadingDatasetAssociatedFunctionality());
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
