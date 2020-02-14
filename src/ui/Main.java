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
import ui.controllers.*;
import ui.mediator.ControllerMediator;

import java.io.IOException;


public class Main extends Application {
    private static final float SCALE_FACTOR = 0.7f;
    private static final Image logo = new Image("ui/icons/RNA-ScoopIcon.jpg");

    @Override
    public void start(Stage primaryStage) throws Exception{
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
        ControllerMediator.getInstance().initializeMain(console, isoformPlot, tSNEPlot);
        setUpStage(primaryStage, root);
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

    private void setUpStage(Stage primaryStage, BorderPane root) {
        primaryStage.setTitle("RNA-Scoop");
        primaryStage.getIcons().add(logo);
        Rectangle2D screen = Screen.getPrimary().getBounds();
        primaryStage.setScene(new Scene(root, screen.getWidth() * SCALE_FACTOR, screen.getHeight() * SCALE_FACTOR));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
