package ui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import labelset.Cluster;
import mediator.ControllerMediator;

import java.util.*;

public class CategoryLabelsLegend {
    public static final double LIGHT_COLOR_LUMINANCE_LIMIT = 0.69;
    public static final int LEGEND_CIRCLE_LABEL_SPACING = 2;
    private static final Font LABEL_FONT = Font.font("Verdana", 12);
    private static final Font LEGEND_CIRCLE_NUMBER_FONT = Font.font("Arial", 11.5);

    private Pane legendGraphic;
    private SelectionModel selectionModel;
    private Map<String, Category> categories;

    public CategoryLabelsLegend(boolean includeLabels, boolean selectable, boolean onlySelected, boolean includeBackground, boolean vertical,
                                double dotSize, double circleCanvasWidth, double circleCanvasHeight, double elementSpacing) {
        if (vertical)
            legendGraphic = new VBox();
        else
            legendGraphic = new HBox();

        if (selectable)
            selectionModel = new SelectionModel();

        categories = new HashMap<>();

        List<Cluster> clusters = ControllerMediator.getInstance().getClusters(onlySelected);

        Iterator<Cluster> iterator = clusters.iterator();
        while(iterator.hasNext()) {
            Cluster cluster= iterator.next();
            Category category = new Category(includeLabels, selectable, dotSize, circleCanvasWidth, circleCanvasHeight, cluster);

            if (iterator.hasNext() && vertical)
                VBox.setMargin(category, new Insets(0, 0, elementSpacing, 0));
            else if (iterator.hasNext())
                HBox.setMargin(category, new Insets(0,  elementSpacing, 0, 0));

            legendGraphic.getChildren().add(category);
            categories.put(cluster.getName(), category);
        }
        if (includeBackground)
            addBackground();

    }

    public void clearSelectedCategories() {
        selectionModel.clearSansNotifyingClusterView();
    }

    public void selectCategoryWithGivenName(String name, boolean unselectRest, boolean updateIsoformView) {
        selectionModel.add(categories.get(name), unselectRest, updateIsoformView);
    }

    public Collection<String> getSelectedCategoryNames() {
        Collection<String> selectedCategoryNames = new HashSet<>();
        for (Map.Entry<String, Category> entry : categories.entrySet()) {
            if (selectionModel.isSelected(entry.getValue()))
                selectedCategoryNames.add(entry.getKey());
        }
        return selectedCategoryNames;
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

    private class Category extends HBox{
        private Cluster cluster;
        private Canvas legendCircle;
        private Text label;

        public Category(boolean includeLabels, boolean selectable, double dotSize, double circleCanvasWidth,
                        double circleCanvasHeight, Cluster cluster){
            setAlignment(Pos.CENTER_LEFT);
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
                legendCircle.setOnMouseClicked(new CategoryMouseHandler(this));
            return legendCircle;
        }

        private Text createLabel(boolean selectable) {
            Text label = new Text(cluster.getName());
            label.setFont(LABEL_FONT);
            if (selectable)
                label.setOnMouseClicked(new CategoryMouseHandler(this));
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
            graphicsContext.setFont(LEGEND_CIRCLE_NUMBER_FONT);
            if (number < 10)
                graphicsContext.fillText(String.valueOf(number), circleX - 3, circleY + 4);
            else
                graphicsContext.fillText(String.valueOf(number), circleX - 6, circleY + 4);
            return legendCircle;
        }

        private Paint getLegendCircleLabelColor(Color circleColor) {
            if (getLuminance(circleColor) >= LIGHT_COLOR_LUMINANCE_LIMIT)
                return Color.BLACK;
            else
                return Color.WHITE;
        }

        private double getLuminance(Color color) {
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
        private HashSet<Category> selected;

        public SelectionModel() {
            selected = new HashSet<>();
        }

        public void add(Category category, boolean unselectRest, boolean updateIsoformView) {
            boolean realUnselectRest = unselectRest || selected.isEmpty(); // shouldn't unselect anything, if nothing to unselect
            if (realUnselectRest)
                clearSansNotifyingClusterView();
            ControllerMediator.getInstance().deselectAllIsoforms();
            ControllerMediator.getInstance().selectCluster(category.getCluster(), realUnselectRest, updateIsoformView);
            category.setStyle("-fx-effect: dropshadow(one-pass-box, #ffafff, 7, 7, 0, 0);");
            selected.add(category);
        }

        public void remove(Category category) {
            if (selected.contains(category)) {
                removeSansNotifyingClusterView(category);
                ControllerMediator.getInstance().unselectCluster(category.getCluster());
            }
        }

        public void clearSansNotifyingClusterView() {
            while (!selected.isEmpty())
                removeSansNotifyingClusterView(selected.iterator().next());
        }

        public boolean isSelected(Category category) {
            return selected.contains(category);
        }

        private void removeSansNotifyingClusterView(Category category) {
            category.setStyle("-fx-effect: null");
            selected.remove(category);
        }

    }

    private class CategoryMouseHandler implements EventHandler<MouseEvent> {
        Category category;

        public CategoryMouseHandler(Category category) {
            this.category = category;
        }

        @Override
        public void handle(MouseEvent event) {
            if (event.isControlDown())
                selectionModel.remove(category);
            else
                selectionModel.add(category, !event.isShiftDown(), true);
        }
    }
}
