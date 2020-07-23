package ui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import javafx.scene.text.Text;
import labelset.Cluster;
import mediator.ControllerMediator;

import java.util.Iterator;
import java.util.List;

public class LegendMaker {
    public static final double LIGHT_COLOR_LUMINENCE_LIMIT = 0.69;
    public static final int LEGEND_CIRCLE_LABEL_SPACING = 2;

    public static Pane createLegend(boolean includeLabels, boolean selectable, boolean onlySelected, boolean includeBackground, boolean vertical,
                                    double dotSize, double circleCanvasWidth, double circleCanvasHeight, double elementSpacing) {
        Pane legend;
        if (vertical)
            legend = new VBox();
        else
            legend = new HBox();

        List<Cluster> clusters;
        clusters = ControllerMediator.getInstance().getClusters(onlySelected);

        Iterator<Cluster> iterator = clusters.iterator();
        while(iterator.hasNext()) {
            Cluster cluster= iterator.next();
            Canvas legendCircle = createLegendCircle(selectable, dotSize, circleCanvasWidth, circleCanvasHeight, cluster);
            HBox legendElement = new HBox();
            legendElement.getChildren().add(legendCircle);
            if (includeLabels) {
                Text label = createLabel(selectable, cluster);
                legendElement.getChildren().add(label);
                HBox.setMargin(legendCircle, new Insets(0, LEGEND_CIRCLE_LABEL_SPACING, 0, 0));
            }
            if (iterator.hasNext() && vertical)
                VBox.setMargin(legendElement, new Insets(0, 0, elementSpacing, 0));
            else if (iterator.hasNext())
                HBox.setMargin(legendElement, new Insets(0,  elementSpacing, 0, 0));
            legend.getChildren().add(legendElement);
        }

        if (includeBackground)
            legend = addBackgroundToLegend(legend);

        return legend;
    }

    private static Canvas createLegendCircle(boolean selectable, double dotSize, double circleCanvasWidth,
                                             double circleCanvasHeight, Cluster cluster) {
        Canvas legendCircle = drawLegendCircleShape(dotSize, circleCanvasWidth, circleCanvasHeight, cluster);
        if (selectable)
            legendCircle.setOnMouseClicked(new LegendMouseHandler(cluster, legendCircle));
        return legendCircle;
    }

    private static Text createLabel(boolean selectable, Cluster cluster) {
        Text label = new Text(cluster.getName());
        if (selectable)
            label.setOnMouseClicked(new LegendMouseHandler(cluster, label));
        return label;
    }

    private static Pane addBackgroundToLegend(Pane legend) {
        Pane legendElements = legend;
        legend = new HBox();
        HBox.setMargin(legendElements, new Insets(5));
        legend.getChildren().add(legendElements);
        legend.setStyle("-fx-background-color: rgba(255, 255, 255, 0.65);" +
                "-fx-border-color: transparent transparent rgba(173, 173, 173, 0.65) rgba(173, 173, 173, 0.65);");
        return legend;
    }

    private static Canvas drawLegendCircleShape(double dotSize, double circleCanvasWidth, double circleCanvasHeight, Cluster cluster) {
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
        int number = cluster.getNumber();
        if (number < 10)
            graphicsContext.fillText(String.valueOf(number), circleX - 3, circleY + 4);
        else
            graphicsContext.fillText(String.valueOf(number), circleX - 6, circleY + 4);
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

    private static class LegendMouseHandler implements EventHandler<MouseEvent> {
        Cluster cluster;
        Node node;

        public LegendMouseHandler(Cluster cluster, Node node) {
            this.cluster = cluster;
            this.node = node;
        }

        @Override
        public void handle(MouseEvent event) {
            ControllerMediator.getInstance().deselectAllIsoforms();
            ControllerMediator.getInstance().selectCluster(cluster);
            node.setStyle("-fx-effect: dropshadow(one-pass-box, #ffafff, 7, 7, 0, 0);");
        }
    }
}
