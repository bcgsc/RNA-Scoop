package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import json.SessionIO;
import ui.controllers.*;
import ui.mediator.ControllerMediator;

import java.io.IOException;
import java.nio.file.NoSuchFileException;


public class Main extends Application {
    private static final float SCALE_FACTOR = 0.7f;
    private static final Image logo = new Image("ui/resources/icons/RNA-ScoopIcon.png");

    @Override
    public void start(Stage window) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("fxml/main/main.fxml"));
        FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("fxml/main/console.fxml"));
        FXMLLoader isoformPlotLoader = new FXMLLoader(getClass().getResource("fxml/main/isoformplot.fxml"));
        FXMLLoader tSNEPlotLoader = new FXMLLoader(getClass().getResource("fxml/main/tsneplot.fxml"));
        FXMLLoader geneSelectorLoader = new FXMLLoader(getClass().getResource("/ui/fxml/geneselector.fxml"));

        BorderPane root = mainLoader.load();
        Parent console = consoleLoader.load();
        Parent isoformPlot = isoformPlotLoader.load();
        Parent tSNEPlot = tSNEPlotLoader.load();
        geneSelectorLoader.load();

        registerControllers(mainLoader.getController(), consoleLoader.getController(), isoformPlotLoader.getController(),
                            tSNEPlotLoader.getController(), geneSelectorLoader.getController());
        ControllerMediator.getInstance().initializeMain(window, console, isoformPlot, tSNEPlot);
        loadPreviousSession();
        setUpWindow(window, root);
    }

    /**
     * Registers controllers with mediator
     */
    private void registerControllers(MainController mainController, ConsoleController consoleController, IsoformPlotController isoformPlotController,
                                     TSNEPlotController tSNEPlotController, GeneSelectorController geneSelectorController) {
        ControllerMediator.getInstance().registerMainController(mainController);
        ControllerMediator.getInstance().registerConsoleController(consoleController);
        ControllerMediator.getInstance().registerIsoformPlotController(isoformPlotController);
        ControllerMediator.getInstance().registerTSNEPlotController(tSNEPlotController);
        ControllerMediator.getInstance().registerGeneSelectorController(geneSelectorController);
    }

    /**
     * Sets up main window
     * Makes it so current session is saved after user clicks X button
     */
    private void setUpWindow(Stage window, BorderPane root) {
        window.setTitle("RNA-Scoop");
        window.getIcons().add(logo);
        setWindowSize(window, root);
        window.setOnCloseRequest(event -> {
            try {
                SessionIO.saveSession();
            } catch (IOException e) {
                System.err.println("An error occurred while saving the current session");
                e.printStackTrace();
            }
        });
        window.show();
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

    private void setWindowSize(Stage primaryStage, BorderPane root) {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        primaryStage.setScene(new Scene(root, screen.getWidth() * SCALE_FACTOR, screen.getHeight() * SCALE_FACTOR));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
