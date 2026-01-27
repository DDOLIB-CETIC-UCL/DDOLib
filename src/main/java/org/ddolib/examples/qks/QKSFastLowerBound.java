package org.ddolib.examples.qks;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

public class QKSFastLowerBound implements FastLowerBound<QKSState> {

    private final QKSProblem problem;

    public QKSFastLowerBound(QKSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(QKSState state, Set<Integer> variables) {
        // dummy lb to pass test
        // it simply corresponds to the cost of adding all the items
        double maxProfit = 0.0;
        double[] currentProfits = state.itemsProfit.clone();
        for (int a : variables) {
            for (int b : variables) {
                currentProfits[b] += problem.profitMatrix[a][b];
            }
        }
        for (int a : variables) {
            maxProfit += currentProfits[a];
        }
        return -maxProfit;
    }
}
