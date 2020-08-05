package mediator;

import controller.*;
import controller.clusterview.ClusterViewController;
import controller.clusterview.ClusterViewSettingsController;
import controller.clusterview.TSNESettingsController;
import controller.clusterview.UMAPSettingsController;
import controller.labelsetmanager.AddLabelSetViewController;
import controller.labelsetmanager.LabelSetManagerController;

public interface Mediator {
    void registerMainController(MainController mainController);
    void registerConsoleController(ConsoleController consoleController);
    void registerIsoformPlotController(IsoformPlotController isoformPlotController);
    void registerClusterViewController(ClusterViewController clusterViewController);
    void registerGeneSelectorController(GeneSelectorController geneSelectorController);
    void registerTPMGradientController(TPMGradientAdjusterController tpmGradientAdjusterController);
    void registerLabelSetManagerController(LabelSetManagerController labelSetManagerController);
    void registerAddLabelSetViewController(AddLabelSetViewController addLabelSetViewController);
    void registerClusterViewSettingsController(ClusterViewSettingsController clusterViewSettingsController);
    void registerUMAPSettingsController(UMAPSettingsController umapSettingsController);
    void registerTSNESettingsController(TSNESettingsController tsneSettingsController);
}
