package org.ddolib.examples.tsalt;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import static java.lang.Math.max;

import java.util.BitSet;

public class TSDistance implements StateDistance<TSState> {

    final private TSProblem problem;

    public TSDistance(TSProblem problem) {
        this.problem = problem;
    }



    private double hammingDistance(BitSet a, BitSet b) {
        double distance = 0;
        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) != b.get(i)) {
                // distance += problem.costs[i];
                distance++;
            }
        }

        return distance;
    }

    private double jaccardDistance(BitSet a, BitSet b) {
        double intersectionSize =0;
        double unionSize = 0;

        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) || b.get(i)) {
                unionSize++;
            }
            if (a.get(i) && b.get(i)) {
                intersectionSize++;
            }
        }

        return 1 - intersectionSize / unionSize;
    }

    private double diceDistance(BitSet a, BitSet b) {
        double distance = 0;

        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) && b.get(i)) {
                distance++;
            }
        }
        distance = distance*-2;
        distance = distance / (a.cardinality() + b.cardinality());
        distance += 1;

        return distance;
    }

    @Override
    public double distance(TSState a, TSState b) {
        double distanceOnActors = diceDistance(a.onLocationActors(),b.onLocationActors());
        double distanceOnRemainingScenes = diceDistance(a.remainingScenes(), b.remainingScenes());
        double alpha = 0.5;
        return alpha * distanceOnActors + (1 - alpha) * distanceOnRemainingScenes;
    }

}
