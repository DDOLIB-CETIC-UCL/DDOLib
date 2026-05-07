package org.ddolib.common.solver.stopcriterion;

import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;

@FunctionalInterface
public interface AstarStopCriterion extends StopCriterion {

    static AstarStopCriterion minValidChildrenPercent(double minPercent) {
        return stats -> stats.validChildrenPercent() <= minPercent;
    }

    boolean testAstarStats(AstarStats stats);

    @Override
    default boolean test(SearchStatistics<?> stats) {
        if (stats instanceof AstarStats aStarStats) {
            return testAstarStats(aStarStats);
        }
        throw new IllegalArgumentException("AStarStopCriterion requires AstarSearchStatistics.");
    }
}
