package org.ddolib.common.solver;

import java.util.IntSummaryStatistics;

public record RestrictSearchStatistics (
    long runTimeMs,
    double incumbent,
    int nbRestrictions,
    boolean isExact,
    IntSummaryStatistics layerSizes
    ) {

    @Override
    public String toString() {
        return String.format("%b;%d;%f;%d;%f",
                isExact,
                runTimeMs,
                incumbent,
                nbRestrictions,
                layerSizes.getAverage());
    }

}
