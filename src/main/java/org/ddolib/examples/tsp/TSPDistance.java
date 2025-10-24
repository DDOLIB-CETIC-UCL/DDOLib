package org.ddolib.examples.tsp;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

public class TSPDistance implements StateDistance<TSPState> {

    private final TSPProblem problem;

    public TSPDistance(TSPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double distance(TSPState a, TSPState b) {
        double distance = 0.0;
        for (int i = 0; i < problem.n; i++) {
            if (a.toVisit.get(i) != b.toVisit.get(i)) {
                distance += 1.0;
            }
        }

        distance += problem.distanceMatrix[a.current.nextSetBit(0)][b.current.nextSetBit(0)];

        return distance;
    }
}
