package org.ddolib.examples.tsp;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

import java.util.HashSet;
import java.util.Set;

public class TSPCoordinates implements StateCoordinates<TSPState> {
    final private TSPProblem problem;

    public TSPCoordinates(TSPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double[] getCoordinates(TSPState state) {
        double[] toVisit = new double[problem.n];
        for (int i = state.toVisit.nextSetBit(0); i >= 0; i = state.toVisit.nextSetBit(i + 1)) {
            toVisit[i] = 1;
        }

        return toVisit;
    }
}
