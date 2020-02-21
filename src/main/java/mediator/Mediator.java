package mediator;

import controller.GeneSelectorController;
import controller.ConsoleController;
import controller.IsoformPlotController;
import controller.MainController;
import controller.TSNEPlotController;

public interface Mediator {
    void registerMainController(MainController mainController);
    void registerConsoleController(ConsoleController consoleController);
    void registerIsoformPlotController(IsoformPlotController isoformPlotController);
    void registerTSNEPlotController(TSNEPlotController tsnePlotController);
    void registerGeneSelectorController(GeneSelectorController geneSelectorController);
}
