package ui;

import controller.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mediator.ControllerMediator;
import persistance.SessionIO;

import java.io.IOException;
import java.nio.file.NoSuchFileException;


public class Main extends Application {
    public static final Image RNA_SCOOP_LOGO = new Image("/icons/RNA-ScoopIcon.png");

    @Override
    public void start(Stage window) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/main/main.fxml"));
        FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("/fxml/main/console.fxml"));
        FXMLLoader isoformPlotLoader = new FXMLLoader(getClass().getResource("/fxml/main/isoformplot.fxml"));
        FXMLLoader tSNEPlotLoader = new FXMLLoader(getClass().getResource("/fxml/main/tsneplot.fxml"));
        FXMLLoader geneSelectorLoader = new FXMLLoader(getClass().getResource("/fxml/geneselector.fxml"));
        FXMLLoader tpmGradientLoader = new FXMLLoader(getClass().getResource("/fxml/tpmgradient.fxml"));

        mainLoader.load();
        Parent console = consoleLoader.load();
        Parent isoformPlot = isoformPlotLoader.load();
        Parent tSNEPlot = tSNEPlotLoader.load();
        geneSelectorLoader.load();
        tpmGradientLoader.load();

        registerControllers(mainLoader.getController(), consoleLoader.getController(), isoformPlotLoader.getController(),
                            tSNEPlotLoader.getController(), geneSelectorLoader.getController(), tpmGradientLoader.getController());
        ControllerMediator.getInstance().initializeMain(console, isoformPlot, tSNEPlot);
        loadPreviousSession();
    }

    /**
     * When program ends, current session is saved
     */
    @Override
    public void stop() throws Exception {
        SessionIO.saveSession();
    }

    /**
     * Registers controllers with mediator
     */
    private void registerControllers(MainController mainController, ConsoleController consoleController, IsoformPlotController isoformPlotController,
                                     TSNEPlotController tSNEPlotController, GeneSelectorController geneSelectorController, TPMGradientAdjusterController tpmGradientAdjusterController) {
        ControllerMediator.getInstance().registerMainController(mainController);
        ControllerMediator.getInstance().registerConsoleController(consoleController);
        ControllerMediator.getInstance().registerIsoformPlotController(isoformPlotController);
        ControllerMediator.getInstance().registerTSNEPlotController(tSNEPlotController);
        ControllerMediator.getInstance().registerGeneSelectorController(geneSelectorController);
        ControllerMediator.getInstance().registerTPMGradientController(tpmGradientAdjusterController);
    }

    /**
     * Attempts to load a saved previous session; if can't the default view is displayed
     */
    private void loadPreviousSession() {
        try {
            SessionIO.loadSession();
        } catch (NoSuchFileException e) {
            System.out.println("No saved sessions found. Opening up default view");
        } catch (IOException e) {
            System.err.println("An error occurred while loading a saved session");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
