package ui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import labelset.Cluster;
import mediator.ControllerMediator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Legend {
    public static final double LIGHT_COLOR_LUMINENCE_LIMIT = 0.69;
    public static final int LEGEND_CIRCLE_LABEL_SPACING = 2;

    private Pane legendGraphic;
    private SelectionModel selectionModel;

    public Legend(boolean includeLabels, boolean selectable, boolean onlySelected, boolean includeBackground, boolean vertical,
                  double dotSize, double circleCanvasWidth, double circleCanvasHeight, double elementSpacing) {
        if (selectable)
            selectionModel = new SelectionModel();

        if (vertical)
            legendGraphic = new VBox();
        else
            legendGraphic = new HBox();

        List<Cluster> clusters = ControllerMediator.getInstance().getClusters(onlySelected);

        Iterator<Cluster> iterator = clusters.iterator();
        while(iterator.hasNext()) {
            Cluster cluster= iterator.next();
            LegendElement legendElement = new LegendElement(includeLabels, selectable, dotSize, circleCanvasWidth, circleCanvasHeight, cluster);

            if (iterator.hasNext() && vertical)
                VBox.setMargin(legendElement, new Insets(0, 0, elementSpacing, 0));
            else if (iterator.hasNext())
                HBox.setMargin(legendElement, new Insets(0,  elementSpacing, 0, 0));

            legendGraphic.getChildren().add(legendElement);
        }
        if (includeBackground)
            addBackground();

    }

    public void clearSelectedLegendElements() {
        selectionModel.clearSansNotifyingTSNE();
    }

    public Pane getLegendGraphic() {
        return legendGraphic;
    }

    private void addBackground() {
        Pane legendElements = legendGraphic;
        legendGraphic = new HBox();
        HBox.setMargin(legendElements, new Insets(5));
        legendGraphic.getChildren().add(legendElements);
        legendGraphic.setStyle("-fx-background-color: rgba(255, 255, 255, 0.65);" +
                               "-fx-border-color: transparent transparent rgba(173, 173, 173, 0.65) rgba(173, 173, 173, 0.65);");
    }

    private class LegendElement extends HBox{
        private Cluster cluster;
        private Canvas legendCircle;
        private Text label;

        public LegendElement(boolean includeLabels, boolean selectable, double dotSize, double circleCanvasWidth,
                             double circleCanvasHeight, Cluster cluster){
            this.cluster = cluster;
            legendCircle = createLegendCircle(selectable, dotSize, circleCanvasWidth, circleCanvasHeight);
            getChildren().add(legendCircle);
            if (includeLabels) {
                label = createLabel(selectable);
                getChildren().add(label);
                HBox.setMargin(legendCircle, new Insets(0, LEGEND_CIRCLE_LABEL_SPACING, 0, 0));
            }
        }

        public Cluster getCluster() {
            return cluster;
        }

        private Canvas createLegendCircle(boolean selectable, double dotSize, double circleCanvasWidth,
                                          double circleCanvasHeight) {
            Canvas legendCircle = drawLegendCircleShape(dotSize, circleCanvasWidth, circleCanvasHeight);
            if (selectable)
                legendCircle.setOnMouseClicked(new LegendElementMouseHandler(this));
            return legendCircle;
        }

        private Text createLabel(boolean selectable) {
            Text label = new Text(cluster.getName());
            if (selectable)
                label.setOnMouseClicked(new LegendElementMouseHandler(this));
            return label;
        }

        private Canvas drawLegendCircleShape(double dotSize, double circleCanvasWidth, double circleCanvasHeight) {
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

        private Paint getLegendCircleLabelColor(Color circleColor) {
            if (getLuminence(circleColor) >= LIGHT_COLOR_LUMINENCE_LIMIT)
                return Color.BLACK;
            else
                return Color.WHITE;
        }

        private double getLuminence(Color color) {
            double r = color.getRed();
            double g = color.getGreen();
            double b = color.getBlue();

            // Minimum and Maximum RGB values are used in the HSL calculations
            double min = Math.min(r, Math.min(g, b));
            double max = Math.max(r, Math.max(g, b));
            return (max + min) / 2;
        }

    }

    private class SelectionModel {
        private HashSet<LegendElement> selected;

        public SelectionModel() {
            selected = new HashSet<>();
        }

        public void add(LegendElement legendElement, boolean unselectRest) {
            boolean realUnselectRest = unselectRest || selected.isEmpty(); // shouldn't unselect anything, if nothing to unselect
            if (realUnselectRest)
                clearSansNotifyingTSNE();
            ControllerMediator.getInstance().deselectAllIsoforms();
            ControllerMediator.getInstance().selectCluster(legendElement.getCluster(), realUnselectRest);
            legendElement.setStyle("-fx-effect: dropshadow(one-pass-box, #ffafff, 7, 7, 0, 0);");
            selected.add(legendElement);
        }

        public void remove(LegendElement legendElement) {
            if (selected.contains(legendElement)) {
                removeSansNotifyingTSNE(legendElement);
                ControllerMediator.getInstance().unselectCluster(legendElement.getCluster());
            }
        }

        public void clearSansNotifyingTSNE() {
            while (!selected.isEmpty())
                removeSansNotifyingTSNE(selected.iterator().next());
        }

        private void removeSansNotifyingTSNE(LegendElement legendElement) {
            legendElement.setStyle("-fx-effect: null");
            selected.remove(legendElement);
        }

    }

    private class LegendElementMouseHandler implements EventHandler<MouseEvent> {
        LegendElement legendElement;

        public LegendElementMouseHandler(LegendElement legendElement) {
            this.legendElement = legendElement;
        }

        @Override
        public void handle(MouseEvent event) {
            if (event.isControlDown())
                selectionModel.remove(legendElement);
            else
                selectionModel.add(legendElement, !event.isShiftDown());
        }
    }
}
