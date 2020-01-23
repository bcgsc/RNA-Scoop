package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ui.fxml.controllers.ConsoleController;
import ui.fxml.controllers.IsoformPlotController;
import ui.fxml.controllers.LoadPathController;


public class Main extends Application {
    private static final float SCALE_FACTOR = 0.7f;
    private static final Image logo = new Image("ui/icons/RNA-Scoop_logo.jpg");

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane root = FXMLLoader.load(getClass().getResource("fxml/main.fxml"));
        FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("fxml/isoformplot.fxml"));
        FXMLLoader bottomLoader = new FXMLLoader(getClass().getResource("fxml/console.fxml"));
        FXMLLoader topLoader = new FXMLLoader(getClass().getResource("fxml/menu.fxml"));

        loadTSNE(root);
        loadConsole(root, bottomLoader);
        loadIsoformViewer(root, centerLoader);
        loadMenu(root, bottomLoader, centerLoader, topLoader);

        setUpWindow(primaryStage, root);
    }

    private void loadTSNE(BorderPane root) throws java.io.IOException {
        root.setRight(FXMLLoader.load(getClass().getResource("fxml/tsne.fxml")));
    }

    private void loadConsole(BorderPane root, FXMLLoader bottomLoader) throws java.io.IOException {
        root.setBottom(bottomLoader.load());
    }

    private void loadIsoformViewer(BorderPane root, FXMLLoader centerLoader) throws java.io.IOException {
        root.setCenter(centerLoader.load());
    }

    private void loadMenu(BorderPane root, FXMLLoader bottomLoader, FXMLLoader centerLoader, FXMLLoader topLoader) throws java.io.IOException {
        root.setTop(topLoader.load());
        IsoformPlotController isoformPlotController = centerLoader.getController();
        LoadPathController loadPathController = topLoader.getController();
        ConsoleController consoleController = bottomLoader.getController();
        loadPathController.initData(consoleController.getConsole(), isoformPlotController.getGeneSelector());
    }

    private void setUpWindow(Stage primaryStage, BorderPane root) {
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
