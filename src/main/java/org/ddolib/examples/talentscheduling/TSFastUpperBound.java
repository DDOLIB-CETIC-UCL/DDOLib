package org.ddolib.examples.talentscheduling;

import org.ddolib.modeling.FastUpperBound;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;

/**
 * Implementation of a fast upper bound for the Talent Scheduling problem.
 */
public class TSFastUpperBound implements FastUpperBound<TSState> {
    private final TSProblem problem;

    public TSFastUpperBound(TSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(TSState state, Set<Integer> variables) {
        return -fastLowerBound(state);
    }

    /**
     * Based on the lower bound of
     * <a href="https://pubsonline.informs.org/doi/abs/10.1287/ijoc.1090.0378"> Garcia et al.</a>
     */
    private double fastLowerBound(TSState state) {
        double lb = 0.0;

        BitSet presentActors = problem.onLocationActors(state);
        RatioAndActor[] ratios = new RatioAndActor[problem.nbActors];
        for (int i = 0; i < ratios.length; i++) {
            ratios[i] = new RatioAndActor(0.0, i);
        }

        for (int scene = state.remainingScenes().nextSetBit(0); scene >= 0; scene = state.remainingScenes().nextSetBit(scene + 1)) {
            BitSet actorsOnLocation = (BitSet) problem.actors[scene].clone();
            actorsOnLocation.and(presentActors);
            if (actorsOnLocation.cardinality() != 0) {
                double totalCost = 0.0;
                double squaredCost = 0.0;


                for (int actor = actorsOnLocation.nextSetBit(0); actor >= 0; actor = actorsOnLocation.nextSetBit(actor + 1)) {
                    int cost = problem.costs[actor];
                    totalCost += cost;
                    squaredCost += cost * cost;
                }

                for (int actor = actorsOnLocation.nextSetBit(0); actor >= 0; actor = actorsOnLocation.nextSetBit(actor + 1)) {
                    ratios[actor].ratio += problem.duration[scene] / totalCost;
                }

                lb -= problem.duration[scene] * (totalCost + squaredCost / totalCost) / 2.0;
            }
        }

        Arrays.sort(ratios);

        double sumE = 0.0;
        for (RatioAndActor ra : ratios) {
            if (presentActors.get(ra.actor)) {
                int a = ra.actor;
                sumE += ra.ratio * problem.costs[a];
                lb += problem.costs[a] * sumE;
            }
        }

        //To manage rounding errors
        BigDecimal bd = new BigDecimal(lb).setScale(10, RoundingMode.HALF_UP);
        return Math.ceil(bd.doubleValue());
    }


    private static class RatioAndActor implements Comparable<RatioAndActor> {
        public double ratio;
        public int actor;

        public RatioAndActor(double ratio, int actor) {
            this.ratio = ratio;
            this.actor = actor;
        }

        @Override
        public int compareTo(RatioAndActor o) {
            int cmp = Double.compare(ratio, o.ratio);
            if (cmp == 0) {
                return Integer.compare(actor, o.actor);
            } else {
                return cmp;
            }
        }

        @Override
        public String toString() {
            return "(ratio: " + ratio + ", actor: " + actor + ")";
        }
    }
}
