package org.ddolib.examples.tsalt;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import org.ddolib.ddo.core.mdd.NodeSubProblem;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.lang.Math.pow;
import static java.lang.Math.abs;

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
                distance++;
            }
        }

        return distance;
    }

    private double weightedJaccardDistance(BitSet a, BitSet b) {
        double intersectionSize =0;
        double unionSize = 0;

        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) || b.get(i)) {
                unionSize += problem.costs[i];
            }
            if (a.get(i) && b.get(i)) {
                intersectionSize += problem.costs[i];
            }
        }

        return 1 - intersectionSize / unionSize;
    }

    private double jaccardDistance(BitSet a, BitSet b) {
        BitSet tmp = (BitSet) a.clone();
        tmp.and(b);
        int intersectionSize = tmp.cardinality();

        tmp = (BitSet) a.clone();
        tmp.or(b);
        int unionSize = tmp.cardinality();

        return (1.0 - ((double) intersectionSize) / unionSize);
    }

    private double weightedHammingDistance(BitSet a, BitSet b) {
        double distance = 0;
        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) != b.get(i)) {
                distance += problem.costs[i];
            }
        }

        return distance;
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

    private double convexCombination(double distanceOnRemainingScenes, double distanceOnActors) {
        double alpha = 0.5;
        return alpha * distanceOnActors + (1 - alpha) * distanceOnRemainingScenes;
    }

    private double convexCombination(double distanceOnRemainingScenes, double distanceOnActors, double distanceOnCost) {
        double alpha = 0.15;
        double beta = 0.10;
        double gamma = 0.75;
        return alpha * distanceOnActors + beta * distanceOnRemainingScenes + gamma*distanceOnCost;
    }

    private double euclideanDistance(double distanceOnRemainingScenes, double distanceOnActors) {
        return sqrt(pow(distanceOnRemainingScenes, 2) + pow(distanceOnActors, 2));
    }

    @Override
    public double distance(TSState a, TSState b) {
        double distanceOnActors = jaccardDistance(a.onLocationActors(),b.onLocationActors());
        double distanceOnRemainingScenes = jaccardDistance(a.remainingScenes(), b.remainingScenes());
        return convexCombination(distanceOnRemainingScenes, distanceOnActors);
        // return euclideanDistance(distanceOnActors, distanceOnRemainingScenes);
    }

    @Override
    public double distance(NodeSubProblem<TSState> a, NodeSubProblem<TSState> b) {
        double distanceOnActors = jaccardDistance(a.state.onLocationActors(),b.state.onLocationActors());
        double distanceOnRemainingScenes = jaccardDistance(a.state.remainingScenes(), b.state.remainingScenes());
        double distanceOnCost = abs(a.getValue() - b.getValue()); // TODO normalize this distance

        return convexCombination(distanceOnRemainingScenes, distanceOnActors, distanceOnCost);
    }

}
