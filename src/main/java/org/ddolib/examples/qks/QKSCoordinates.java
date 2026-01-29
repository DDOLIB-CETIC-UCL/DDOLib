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
        double[] coordinates = new double[1];
        coordinates[0] = state.capacity;
        /*int index = 1;
        for (int item = state.remainingItems.nextSetBit(0); item >= 0; item = state.remainingItems.nextSetBit(item+1)){
            coordinates[index++] = state.itemsProfit[item];
        }*/

        // System.arraycopy(state.itemsProfit, 0, coordinates, 1, problem.nbVars());
        return coordinates;
        // return new double[0];
    }
}
