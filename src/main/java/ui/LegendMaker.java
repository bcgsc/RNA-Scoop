package ui;

import controller.TSNEPlotController;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import javafx.scene.text.Text;
import mediator.ControllerMediator;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class LegendMaker {
    public static HBox createLegend(boolean includeLabels, boolean onlySelected, double dotSize, double dotCanvasWidth,
                                    double dotCanvasHeight, double elementSpacing) {
        HBox dotPlotLegend = new HBox();
        double dotX = dotCanvasWidth / 2;
        double dotY = dotCanvasHeight / 2;
        Collection<TSNEPlotController.Cluster> clusters = ControllerMediator.getInstance().getClusters(onlySelected);

        Iterator<TSNEPlotController.Cluster> iterator = clusters.iterator();
        while(iterator.hasNext()) {
            TSNEPlotController.Cluster cluster= iterator.next();
            Canvas dotPlotLegendItem = new Canvas(dotCanvasWidth, dotCanvasHeight);
            if (iterator.hasNext() && !includeLabels)
                HBox.setMargin(dotPlotLegendItem, new Insets(0, elementSpacing, 0, 0));
            else if (iterator.hasNext())
                    HBox.setMargin(dotPlotLegendItem, new Insets(0, 1, 0, 0));
            GraphicsContext graphicsContext = dotPlotLegendItem.getGraphicsContext2D();
            Color dotColor = cluster.getColor();
            graphicsContext.setFill(dotColor);
            graphicsContext.fillOval(dotX - dotSize / 2, dotY - dotSize / 2, dotSize, dotSize);
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.strokeOval(dotX - dotSize / 2, dotY - dotSize / 2, dotSize, dotSize);
            graphicsContext.setFill(getLegendLabelColor(dotColor));
            graphicsContext.fillText(String.valueOf(cluster.getNumber()), dotX - 3, dotY + 4);
            dotPlotLegend.getChildren().add(dotPlotLegendItem);
            if (includeLabels) {
                Text label = new Text(cluster.getLabel());
                HBox.setMargin(label, new Insets(0, elementSpacing, 0, 0));
                dotPlotLegend.getChildren().add(label);
            }
        }
        return dotPlotLegend;
    }

    private static Paint getLegendLabelColor(Color dotColor) {
        if (getLuminence(dotColor) > 60)
            return Color.BLACK;
        else
            return Color.WHITE;
    }

    private static double getLuminence(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();

        //	Minimum and Maximum RGB values are used in the HSL calculations
        double min = Math.min(r, Math.min(g, b));
        double max = Math.max(r, Math.max(g, b));

        return (max + min) / 2;
    }
}
