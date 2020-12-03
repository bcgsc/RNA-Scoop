package ui;

import controller.*;
import controller.clusterview.ClusterViewController;
import controller.clusterview.ClusterViewSettingsController;
import controller.clusterview.TSNESettingsController;
import controller.clusterview.UMAPSettingsController;
import controller.labelsetmanager.AddLabelSetViewController;
import controller.labelsetmanager.LabelSetManagerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mediator.ControllerMediator;


public class Main extends Application {
    public static final Image RNA_SCOOP_LOGO = new Image("/icons/RNA-ScoopIcon.png");

    @Override
    public void start(Stage window) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/main/main.fxml"));
        FXMLLoader aboutLoader = new FXMLLoader(getClass().getResource("/fxml/about.fxml"));
        FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("/fxml/main/console.fxml"));
        FXMLLoader isoformPlotLoader = new FXMLLoader(getClass().getResource("/fxml/main/isoformplot.fxml"));
        FXMLLoader clusterViewLoader = new FXMLLoader(getClass().getResource("/fxml/main/clusterview.fxml"));
        FXMLLoader geneSelectorLoader = new FXMLLoader(getClass().getResource("/fxml/geneselector.fxml"));
        FXMLLoader geneFiltererLoader = new FXMLLoader(getClass().getResource("/fxml/genefilterer.fxml"));
        FXMLLoader tpmGradientLoader = new FXMLLoader(getClass().getResource("/fxml/tpmgradient.fxml"));
        FXMLLoader labelSetManagerLoader = new FXMLLoader(getClass().getResource("/fxml/labelsetmanager/labelsetmanager.fxml"));
        FXMLLoader addLabelSetViewLoader = new FXMLLoader(getClass().getResource("/fxml/labelsetmanager/addlabelsetview.fxml"));
        FXMLLoader clusterViewSettingsLoader = new FXMLLoader(getClass().getResource("/fxml/clusterviewsettings/clusterviewsettings.fxml"));
        FXMLLoader tSNESettingsLoader = new FXMLLoader(getClass().getResource("/fxml/clusterviewsettings/tsnesettings.fxml"));
        FXMLLoader umapSettingsLoader = new FXMLLoader(getClass().getResource("/fxml/clusterviewsettings/umapsettings.fxml"));
        FXMLLoader imageExporterLoader = new FXMLLoader(getClass().getResource("/fxml/imageexporter.fxml"));

        mainLoader.load();
        aboutLoader.load();
        Parent console = consoleLoader.load();
        Parent isoformPlot = isoformPlotLoader.load();
        Parent clusterView = clusterViewLoader.load();
        geneSelectorLoader.load();
        geneFiltererLoader.load();
        tpmGradientLoader.load();
        Parent labelSetManager = labelSetManagerLoader.load();
        Parent addLabelSetView = addLabelSetViewLoader.load();
        clusterViewSettingsLoader.load();
        Parent tSNESettings = tSNESettingsLoader.load();
        Parent umapSettings = umapSettingsLoader.load();
        imageExporterLoader.load();

        registerControllers(mainLoader.getController(), aboutLoader.getController(), consoleLoader.getController(), isoformPlotLoader.getController(),
                            clusterViewLoader.getController(), geneSelectorLoader.getController(), geneFiltererLoader.getController(),
                            tpmGradientLoader.getController(), labelSetManagerLoader.getController(), addLabelSetViewLoader.getController(),
                            clusterViewSettingsLoader.getController(), tSNESettingsLoader.getController(), umapSettingsLoader.getController(),
                            imageExporterLoader.getController());
        ControllerMediator.getInstance().initializeMain(console, isoformPlot, clusterView);
        ControllerMediator.getInstance().initializeClusterViewSettings(tSNESettings, umapSettings);
        setUpLabelSetManagerPopUp(labelSetManager, addLabelSetView);
    }

    /**
     * Registers controllers with mediator
     */
    private void registerControllers(MainController mainController, AboutController aboutController, ConsoleController consoleController,
                                     IsoformPlotController isoformPlotController, ClusterViewController clusterViewController,
                                     GeneSelectorController geneSelectorController, GeneFiltererController geneFiltererController,
                                     TPMGradientAdjusterController tpmGradientAdjusterController, LabelSetManagerController labelSetManagerController,
                                     AddLabelSetViewController addLabelSetViewController, ClusterViewSettingsController clusterViewSettingsController,
                                     TSNESettingsController tsneSettingsController, UMAPSettingsController umapSettingsController,
                                     ImageExporterController imageExporterController) {
        ControllerMediator.getInstance().registerMainController(mainController);
        ControllerMediator.getInstance().registerAboutController(aboutController);
        ControllerMediator.getInstance().registerConsoleController(consoleController);
        ControllerMediator.getInstance().registerIsoformPlotController(isoformPlotController);
        ControllerMediator.getInstance().registerClusterViewController(clusterViewController);
        ControllerMediator.getInstance().registerGeneSelectorController(geneSelectorController);
        ControllerMediator.getInstance().registerGeneFiltererController(geneFiltererController);
        ControllerMediator.getInstance().registerTPMGradientController(tpmGradientAdjusterController);
        ControllerMediator.getInstance().registerLabelSetManagerController(labelSetManagerController);
        ControllerMediator.getInstance().registerAddLabelSetViewController(addLabelSetViewController);
        ControllerMediator.getInstance().registerClusterViewSettingsController(clusterViewSettingsController);
        ControllerMediator.getInstance().registerTSNESettingsController(tsneSettingsController);
        ControllerMediator.getInstance().registerUMAPSettingsController(umapSettingsController);
        ControllerMediator.getInstance().registerImageExporterController(imageExporterController);
    }

    private void setUpLabelSetManagerPopUp(Parent labelSetManager, Parent addLabelSetView) {
        // label set manager has custom window to allow for changing between views
        LabelSetManagerWindow labelSetManagerWindow = new LabelSetManagerWindow(labelSetManager, addLabelSetView);
        ControllerMediator.getInstance().initializeAddLabelSetView(labelSetManagerWindow);
        ControllerMediator.getInstance().initializeLabelSetManager(labelSetManagerWindow);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
