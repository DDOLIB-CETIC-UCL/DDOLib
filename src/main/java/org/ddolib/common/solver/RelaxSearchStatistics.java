package org.ddolib.common.solver;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LinkedList;
import java.util.List;

public record RelaxSearchStatistics(
        long runTimeMs,
        double incumbent,
        int nbRelaxations,
        SummaryStatistics stateCardinalities,
        SummaryStatistics exactStates,
        SummaryStatistics stateDegradationsPerNode,
        SummaryStatistics layerSize,
        boolean isExact,
        int nbNode,
        int nbExactNode
        ) {

    @Override
    public String toString() {


        return String.format("%b;%d;%f;%d;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%d;%d",
                isExact,
                runTimeMs,
                incumbent,
                nbRelaxations,
                exactStates.getMean(),
                exactStates.getGeometricMean(),
                exactStates.getMin(),
                exactStates.getMax(),
                exactStates.getVariance(),
                stateCardinalities.getMean(),
                stateCardinalities.getGeometricMean(),
                stateCardinalities.getMin(),
                stateCardinalities.getMax(),
                stateCardinalities.getVariance(),
                stateDegradationsPerNode.getMean(),
                stateDegradationsPerNode.getGeometricMean(),
                stateDegradationsPerNode.getMin(),
                stateDegradationsPerNode.getMax(),
                stateDegradationsPerNode.getVariance(),
                layerSize.getMean(),
                layerSize.getGeometricMean(),
                layerSize.getMin(),
                layerSize.getMax(),
                layerSize.getVariance(),
                nbNode,
                nbExactNode
        );
    }

}
