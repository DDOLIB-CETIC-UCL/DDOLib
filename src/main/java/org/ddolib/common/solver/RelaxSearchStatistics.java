package org.ddolib.common.solver;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;

public record RelaxSearchStatistics(
        long runTimeMs,
        double incumbent,
        int nbRelaxations,
        List<DoubleSummaryStatistics> stateCardinalities,
        IntSummaryStatistics exactStates,
        List<DoubleSummaryStatistics> stateDegradations,
        boolean isExact
        ) {

    @Override
    public String toString() {
        double avgMinCardinality = stateCardinalities.stream().mapToDouble(DoubleSummaryStatistics::getMin).sum() / stateCardinalities.size();
        double avgMaxCardinality = stateCardinalities.stream().mapToDouble(DoubleSummaryStatistics::getMax).sum() / stateCardinalities.size();
        double avgAvgCardinality = stateCardinalities.stream().mapToDouble(DoubleSummaryStatistics::getAverage).sum() / stateCardinalities.size();

        double avgMinDegradation = stateDegradations.stream().mapToDouble(DoubleSummaryStatistics::getMin).sum() / stateDegradations.size();
        double avgMaxDegradation = stateDegradations.stream().mapToDouble(DoubleSummaryStatistics::getMax).sum() / stateDegradations.size();
        double avgAvgDegradation = stateDegradations.stream().mapToDouble(DoubleSummaryStatistics::getAverage).sum() / stateDegradations.size();
        // System.out.println(stateDegradations.get(0).getMax());

        return String.format("%b;%d;%f;%d;%f;%d;%d;%f;%f;%f;%f;%f;%f",
                isExact,
                runTimeMs,
                incumbent,
                nbRelaxations,
                exactStates.getAverage(),
                exactStates.getMin(),
                exactStates.getMax(),
                avgMinCardinality,
                avgMaxCardinality,
                avgAvgCardinality,
                avgMinDegradation,
                avgMaxDegradation,
                avgAvgDegradation
        );
    }

}
