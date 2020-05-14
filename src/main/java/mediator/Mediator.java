package mediator;

import controller.*;

public interface Mediator {
    void registerMainController(MainController mainController);
    void registerConsoleController(ConsoleController consoleController);
    void registerIsoformPlotController(IsoformPlotController isoformPlotController);
    void registerTSNEPlotController(TSNEPlotController tsnePlotController);
    void registerGeneSelectorController(GeneSelectorController geneSelectorController);
    void registerTPMGradientController(TPMGradientAdjusterController tpmGradientAdjusterController);
    void registerClusterManagerController(ClusterManagerController clusterManagerController);
}
