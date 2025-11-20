package org.ddolib.examples.smic.ui;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.LinkedList;
import java.util.List;

public class SmicChartView extends StackPane {


    private static class TaskLineChart extends LineChart<Number, Number> {

        private int maxInventory;

        private List<SmicTask> tasks =  new LinkedList<>();

        public TaskLineChart(Axis<Number> axis, Axis<Number> axis1) {
            super(axis, axis1);
        }

        public void drawTasks(List<SmicTask> tasks) {
            this.tasks = tasks;
            requestChartLayout();
        }

        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();

            ObservableList<Node> plotChildren = getPlotChildren();
            plotChildren.clear();

            plotChildren.removeIf(node -> "TASH_RECT".equals(node.getUserData()));

            NumberAxis xAxis = (NumberAxis) getXAxis();
            NumberAxis yAxis = (NumberAxis) getYAxis();

            double yZeroPixel = yAxis.getDisplayPosition(0);
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

                //TODO draw bound of inventory
            }
        }


    }
}
