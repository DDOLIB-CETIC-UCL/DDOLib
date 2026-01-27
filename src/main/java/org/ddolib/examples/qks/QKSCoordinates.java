package org.ddolib.examples.qks;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

import java.util.Arrays;

public class QKSCoordinates implements StateCoordinates<QKSState> {

    final private QKSProblem problem;

    public QKSCoordinates(QKSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double[] getCoordinates(QKSState state) {
        double[] coordinates = new double[problem.nbVars()+1];
        coordinates[0] = state.capacity;
        System.arraycopy(state.itemsProfit, 0, coordinates, 1, problem.nbVars());
        return coordinates;
    }
}
