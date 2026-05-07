package org.ddolib.common.solver.stopcriterion;

import org.ddolib.common.solver.stat.SearchStatistics;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;

import java.util.ArrayList;

public class InferenceCriterion implements StopCriterion {
    public final ArrayList<Long> time = new ArrayList<>();
    public final ArrayList<Double> objective = new ArrayList<>();

    @Override
    public boolean test(SearchStatistics searchStatistics) {
        return false;
    }

    public void addStat(SearchStatistics stats) {
        time.add(stats.runtime());
        objective.add(stats.incumbent());
    }

    public void showChart() {
        if (time.isEmpty() || objective.isEmpty()) {
            System.out.println("No data to display yet");
        }

        XYChart chart = QuickChart.getChart(
                "Objective Evolution",
                "Time (ms)",
                "Objective",
                "Evolution Trace",
                time,
                objective);

        XYSeries series = chart.getSeriesMap().get("Evolution Trace");
        series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Step);

        chart.getStyler().setMarkerSize(8);

        new SwingWrapper<>(chart).displayChart();

    }
}
