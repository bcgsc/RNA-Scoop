package ui.mediator;

import ui.controllers.GeneSelectorController;
import ui.controllers.ConsoleController;
import ui.controllers.IsoformPlotController;
import ui.controllers.MainController;
import ui.controllers.TSNEPlotController;

public interface Mediator {
    void registerMainController(MainController mainController);
    void registerConsoleController(ConsoleController consoleController);
    void registerIsoformPlotController(IsoformPlotController isoformPlotController);
    void registerTSNEPlotController(TSNEPlotController tsnePlotController);
    void registerGeneSelectorController(GeneSelectorController geneSelectorController);
}
