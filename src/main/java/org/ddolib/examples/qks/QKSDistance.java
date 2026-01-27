package org.ddolib.examples.qks;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import static org.ddolib.util.DistanceUtil.euclideanDistance;
import static java.lang.Math.sqrt;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class QKSDistance implements StateDistance<QKSState> {

    private final QKSProblem problem;

    public QKSDistance(QKSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double distance(QKSState a, QKSState b) {
        return distanceOnProfits(a, b) * 0.5 + distanceOnCapacity(a, b) * 0.5;
    }

    private double distanceOnProfits(QKSState a, QKSState b) {
        double distance = 0.0;
        double maxDistance = 0.0;
        for (int i = 0; i < a.itemsProfit.length; i++) {
            if (a.remainingItems.get(i)) { // we only consider contribution of non-considered items
                distance += pow(a.itemsProfit[i] - b.itemsProfit[i], 2);
                maxDistance += pow(problem.maxProfits[i], 2);
            }
        }

        return sqrt(distance) / sqrt(maxDistance);
    }

    private double distanceOnCapacity(QKSState a, QKSState b) {
        return abs(a.capacity - b.capacity) / (double) problem.capacity;
    }

}
