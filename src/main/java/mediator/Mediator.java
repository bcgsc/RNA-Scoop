package mediator;

import controller.*;
import controller.labelsetmanager.AddLabelSetViewController;
import controller.labelsetmanager.LabelSetManagerController;

public interface Mediator {
    void registerMainController(MainController mainController);
    void registerConsoleController(ConsoleController consoleController);
    void registerIsoformPlotController(IsoformPlotController isoformPlotController);
    void registerTSNEPlotController(TSNEPlotController tsnePlotController);
    void registerGeneSelectorController(GeneSelectorController geneSelectorController);
    void registerTPMGradientController(TPMGradientAdjusterController tpmGradientAdjusterController);
    void registerLabelSetManagerController(LabelSetManagerController labelSetManagerController);
    void registerAddLabelSetViewController(AddLabelSetViewController addLabelSetViewController);
}
