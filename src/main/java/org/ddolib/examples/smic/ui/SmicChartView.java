package org.ddolib.examples.smic.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
    private final int initInventory;

    public SmicChartView(int initInventory, int maxInventory) {
        this.initInventory = initInventory;

        final NumberAxis xAxis = new NumberAxis(0, 65, 1);
        xAxis.setLabel("Time");
        final int yLimit = maxInventory + 5 - maxInventory % 5;
        final NumberAxis yAxis = new NumberAxis(-1, yLimit, yLimit / 5.);
        yAxis.setLabel("Inventory");

        lineChart = new TaskLineChart(xAxis, yAxis, maxInventory);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.setLegendVisible(false);

        series = new XYChart.Series<>();
        lineChart.getData().add(series);

        this.getChildren().add(lineChart);

    }

    public void refresh(List<SmicTask> tasks) {
        Platform.runLater(() -> {
            series.getData().clear();
            lineChart.getData().clear();
            ObservableList<XYChart.Data<Number, Number>> list = FXCollections.observableArrayList();
            list.add(new XYChart.Data<>(0, initInventory));
            int prevTime = 0;
            int prevInventory = initInventory;
            for (SmicTask task : tasks) {
                if (task.start() != prevTime) {
                    list.add(new XYChart.Data<>(task.start(), prevInventory));
                }
                list.add(new XYChart.Data<>(task.end(), task.inventoryAtEnd()));
                prevTime = task.end();
                prevInventory = task.inventoryAtEnd();
            }

            series.getData().addAll(list);
            lineChart.getData().add(series);
            lineChart.drawTasks(tasks);
        });
    }


    private static class TaskLineChart extends LineChart<Number, Number> {

        private final int maxInventory;

        private List<SmicTask> tasks = new LinkedList<>();

        public TaskLineChart(Axis<Number> axis, Axis<Number> axis1, int maxInventory) {
            super(axis, axis1);
            this.maxInventory = maxInventory;
        }

        public void drawTasks(List<SmicTask> tasks) {
            this.tasks = tasks;
            requestChartLayout();
        }

        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();

            ObservableList<Node> plotChildren = getPlotChildren();

            plotChildren.removeIf(node -> "TASK_RECT".equals(node.getUserData()) || "LIMIT_LINE".equals(node.getUserData()));

            NumberAxis xAxis = (NumberAxis) getXAxis();
            NumberAxis yAxis = (NumberAxis) getYAxis();

            double yZeroPixel = yAxis.getDisplayPosition(-1);
            if (Double.isNaN(yZeroPixel)) return;

            for (SmicTask task : tasks) {
                double xStart = xAxis.getDisplayPosition(task.start());
                double xEnd = xAxis.getDisplayPosition(task.end());

                if (Double.isNaN(xStart) || Double.isNaN(xEnd)) continue;

                double width = Math.abs(xEnd - xStart);
                double realXStart = Math.min(xStart, xEnd);
                double height = 20;
                double yPos = yZeroPixel - height;

                Rectangle rect = new Rectangle(realXStart, yPos, width, height);
                rect.setFill(Color.ORANGE.deriveColor(0, 1, 1, 0.5));
                rect.setStroke(Color.ORANGE);
                rect.setUserData("TASK_RECT");

                Text text = new Text("" + task.id());
                text.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                text.setUserData("TASK_RECT");
                text.setX(realXStart + (width - text.getLayoutBounds().getWidth()) / 2);
                text.setY(yPos + (height / 2) + 4);

                plotChildren.add(rect);
                plotChildren.add(text);

            }

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

            double xStart = 0;
            double xEnd = xAxis.getWidth();

            Line line = new Line(xStart, yPixel, xEnd, yPixel);
            line.setStroke(color);
            line.setStrokeWidth(2);
            line.getStrokeDashArray().addAll(10d, 5d); // Pattern: 10px line, 5px space
            line.setUserData("LIMIT_LINE"); // Tag for removal

            Text text = new Text(label + " (" + value + ")");
            text.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            text.setFill(color);
            text.setX(xEnd - 100); // Position on the right side
            text.setY(yPixel - 5); // Slightly above the line
            text.setUserData("LIMIT_LINE");

            children.add(line);
            children.add(text);
        }


    }
}
