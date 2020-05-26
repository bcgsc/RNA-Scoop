package ui;

import controller.ClusterManagerController;
import controller.TSNEPlotController;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import javafx.scene.text.Text;
import mediator.ControllerMediator;

import java.util.Iterator;
import java.util.List;

public class LegendMaker {
    public static final double LIGHT_COLOR_LUMINENCE_LIMIT = 0.69;
    public static final int LEGEND_CIRCLE_LABEL_SPACING = 1;

    public static Pane createLegend(boolean includeLabels, boolean onlySelected, boolean includeBackground, boolean vertical,
                                    double dotSize, double circleCanvasWidth, double circleCanvasHeight, double elementSpacing) {
        Pane legend;
        if (vertical)
            legend = new VBox();
        else
            legend = new HBox();

        List<ClusterManagerController.Cluster> clusters;
        if (onlySelected)
            clusters = ControllerMediator.getInstance().getSelectedClusters();
        else
            clusters = ControllerMediator.getInstance().getAllClusters();

        Iterator<ClusterManagerController.Cluster> iterator = clusters.iterator();
        while(iterator.hasNext()) {
            ClusterManagerController.Cluster cluster= iterator.next();
            Canvas legendCircle = createLegendCircle(dotSize, circleCanvasWidth, circleCanvasHeight, cluster);
            legend.getChildren().add(legendCircle);
            HBox legendElement = new HBox();
            legendElement.getChildren().add(legendCircle);
            if (includeLabels) {
                Text label = new Text(cluster.getLabel());
                legendElement.getChildren().add(label);
                HBox.setMargin(legendCircle, new Insets(0, LEGEND_CIRCLE_LABEL_SPACING, 0, 0));
            }
            if (iterator.hasNext() && vertical)
                VBox.setMargin(legendElement, new Insets(0, 0, elementSpacing, 0));
            else if (iterator.hasNext())
                HBox.setMargin(legendElement, new Insets(0,  elementSpacing, 0, 0));
            legend.getChildren().add(legendElement);
        }

        if (includeBackground) {
            Pane legendElements = legend;
            legend = new HBox();
            HBox.setMargin(legendElements, new Insets(5));
            legend.getChildren().add(legendElements);
            legend.setStyle("-fx-background-color: rgba(255, 255, 255, 0.65);" +
                            "-fx-border-color: transparent transparent rgba(173, 173, 173, 0.65) rgba(173, 173, 173, 0.65);");
        }
        return legend;
    }

    private static Canvas createLegendCircle(double dotSize, double circleCanvasWidth, double circleCanvasHeight,
                                             ClusterManagerController.Cluster cluster) {
        Canvas legendCircle = new Canvas(circleCanvasWidth, circleCanvasHeight);
        double circleX = circleCanvasWidth / 2;
        double circleY = circleCanvasHeight / 2;
        GraphicsContext graphicsContext = legendCircle.getGraphicsContext2D();
        Color circleColor = cluster.getJavaFXColor();
        // draw circle
        graphicsContext.setFill(circleColor);
        graphicsContext.fillOval(circleX - dotSize / 2, circleY - dotSize / 2, dotSize, dotSize);
        // draw outline around circle
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.strokeOval(circleX - dotSize / 2, circleY - dotSize / 2, dotSize, dotSize);
        // add circle label
        graphicsContext.setFill(getLegendCircleLabelColor(circleColor));
        graphicsContext.fillText(String.valueOf(cluster.getNumber()), circleX - 3, circleY + 4);
        return legendCircle;
    }

    private static Paint getLegendCircleLabelColor(Color circleColor) {
        if (getLuminence(circleColor) >= LIGHT_COLOR_LUMINENCE_LIMIT)
            return Color.BLACK;
        else
            return Color.WHITE;
    }

    private static double getLuminence(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();

        // Minimum and Maximum RGB values are used in the HSL calculations
        double min = Math.min(r, Math.min(g, b));
        double max = Math.max(r, Math.max(g, b));
        return (max + min) / 2;
    }
}
