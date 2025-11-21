package org.ddolib.examples.smic.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.LinkedList;
import java.util.List;

public class SmicChartView extends StackPane {

    private final TaskLineChart lineChart;
    private final XYChart.Series<Number, Number> series;
    private final Pane overlayPane; // Le calque pour dessiner "hors cadre"
    private final int initInventory;

    public SmicChartView(int initInventory, int maxInventory) {
        this.initInventory = initInventory;


        final NumberAxis xAxis = new NumberAxis(0, 30, 5);
        xAxis.setLabel("Time");

        final int yLimit = maxInventory + 5 - maxInventory % 5;
        final NumberAxis yAxis = new NumberAxis(0, yLimit, 5);
        yAxis.setLabel("Inventory");

        lineChart = new TaskLineChart(xAxis, yAxis, maxInventory);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.setLegendVisible(false);

        lineChart.setPadding(new Insets(20, 20, 50, 20));


        series = new XYChart.Series<>();
        lineChart.getData().add(series);

        overlayPane = new Pane();
        overlayPane.setPickOnBounds(false);
        overlayPane.setMouseTransparent(true);

        lineChart.setOnLayoutCallback(() -> drawOverlayTasks(lineChart.getTasks()));

        setGlobalBackgroundColor(Color.WHITE);
        setPlotBackgroundColor(Color.WHITE);
        setCurveColor(Color.rgb(111, 176, 82));

        this.getChildren().addAll(lineChart, overlayPane);
    }

    public void refresh(List<SmicTask> tasks) {
        Platform.runLater(() -> {
            series.getData().clear();
            ObservableList<XYChart.Data<Number, Number>> list = FXCollections.observableArrayList();
            list.add(new XYChart.Data<>(0, initInventory));

            int prevInventory = initInventory;
            int prevTime = 0;

            for (SmicTask task : tasks) {
                if (task.start() != prevTime) {
                    list.add(new XYChart.Data<>(task.start(), prevInventory));
                }
                list.add(new XYChart.Data<>(task.end(), task.inventoryAtEnd()));
                prevTime = task.end();
                prevInventory = task.inventoryAtEnd();
            }

            series.getData().setAll(list);

            lineChart.setTasks(tasks);
        });
    }

    public void setCurveColor(Color color) {
        String hexColor = toHexString(color);

        lineChart.setStyle("CHART_COLOR_1: " + hexColor + ";");
    }

    public void setGlobalBackgroundColor(Color color) {
        String hex = toHexString(color);
        this.setStyle("-fx-background-color: " + hex + ";");
        lineChart.setStyle("-fx-background-color: " + hex + ";");
    }

    public void setPlotBackgroundColor(Color color) {
        String hex = toHexString(color);

        Node plotArea = lineChart.lookup(".chart-plot-background");

        if (plotArea != null) {
            plotArea.setStyle("-fx-background-color: " + hex + ";");
        }
    }

    /**
     * Dessine les rectangles et IDs dans le calque supérieur (Overlay).
     * Utilise la conversion de coordonnées pour s'aligner avec les axes.
     */
    private void drawOverlayTasks(List<SmicTask> tasks) {
        overlayPane.getChildren().clear();

        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        double yPos = this.getHeight() - 40;

        for (SmicTask task : tasks) {
            double xStartAxis = xAxis.getDisplayPosition(task.start());
            double xEndAxis = xAxis.getDisplayPosition(task.end());

            Point2D pStart = xAxis.localToScene(xStartAxis, 0);
            Point2D pEnd = xAxis.localToScene(xEndAxis, 0);

            if (pStart == null || pEnd == null) continue;

            Point2D pStartOverlay = overlayPane.sceneToLocal(pStart);
            Point2D pEndOverlay = overlayPane.sceneToLocal(pEnd);

            double realXStart = Math.min(pStartOverlay.getX(), pEndOverlay.getX());
            double width = Math.abs(pEndOverlay.getX() - pStartOverlay.getX());
            double height = 20;

            Rectangle rect = new Rectangle(realXStart, yPos, width, height);
            rect.setFill(Color.ORANGE.deriveColor(0, 1, 1, 0.5));
            rect.setStroke(Color.ORANGE);

            Text text = new Text("" + task.id());
            text.setFont(Font.font("Arial", FontWeight.BOLD, 10));

            text.setX(realXStart + (width - text.getLayoutBounds().getWidth()) / 2);
            text.setY(yPos + (height / 2) + 4);

            overlayPane.getChildren().addAll(rect, text);
        }
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    private static class TaskLineChart extends LineChart<Number, Number> {
        private final int maxInventory;
        private List<SmicTask> tasks = new LinkedList<>();

        private Runnable onLayoutCallback;

        public TaskLineChart(Axis<Number> xAxis, Axis<Number> yAxis, int maxInventory) {
            super(xAxis, yAxis);
            this.maxInventory = maxInventory;
        }

        public void setOnLayoutCallback(Runnable callback) {
            this.onLayoutCallback = callback;
        }

        public List<SmicTask> getTasks() {
            return tasks;
        }

        public void setTasks(List<SmicTask> tasks) {
            this.tasks = tasks;
            requestChartLayout();
        }

        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();

            drawLimits();

            if (onLayoutCallback != null) {
                onLayoutCallback.run();
            }
        }

        private void drawLimits() {
            ObservableList<Node> plotChildren = getPlotChildren();
            plotChildren.removeIf(node -> "LIMIT_LINE".equals(node.getUserData()));

            NumberAxis yAxis = (NumberAxis) getYAxis();
            NumberAxis xAxis = (NumberAxis) getXAxis();

            drawHorizontalLine(plotChildren, xAxis, yAxis, maxInventory, Color.RED, "Max inventory");
        }

        private void drawHorizontalLine(ObservableList<Node> children,
                                        NumberAxis xAxis,
                                        NumberAxis yAxis,
                                        int value,
                                        Color color,
                                        String label) {
            double yPixel = yAxis.getDisplayPosition(value);
            if (Double.isNaN(yPixel)) return;

            double xEnd = xAxis.getWidth();

            Line line = new Line(0, yPixel, xEnd, yPixel);
            line.setStroke(color);
            line.setStrokeWidth(2);
            line.getStrokeDashArray().addAll(10d, 5d);
            line.setUserData("LIMIT_LINE"); // Tag important pour le nettoyage

            Text text = new Text(label + " (" + value + ")");
            text.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            text.setFill(color);
            text.setX(xEnd - 100);
            text.setY(yPixel - 5);
            text.setUserData("LIMIT_LINE");

            children.add(line);
            children.add(text);
        }
    }
}