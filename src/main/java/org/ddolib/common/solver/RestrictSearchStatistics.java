package org.ddolib.common.solver;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;


public record RestrictSearchStatistics (
    long runTimeMs,
    double incumbent,
    int nbRestrictions,
    boolean isExact,
    SummaryStatistics layerSizes
    ) {

    @Override
    public String toString() {
        return String.format("%b;%d;%f;%d;%f",
                isExact,
                runTimeMs,
                incumbent,
                nbRestrictions,
                layerSizes.getMean());
    }

}
