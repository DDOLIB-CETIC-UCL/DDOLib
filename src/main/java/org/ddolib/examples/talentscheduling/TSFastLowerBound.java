package org.ddolib.examples.talentscheduling;

import org.ddolib.modeling.FastLowerBound;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;

/**
 * Implementation of a fast lower bound for the Talent Scheduling Problem (TSP).
 * <p>
 * This class computes a heuristic lower bound on the total cost of a partially scheduled solution,
 * based on the method described in
 * <a href="https://pubsonline.informs.org/doi/abs/10.1287/ijoc.1090.0378">Garcia et al.</a>.
 * It takes into account both the duration of scenes and the actor costs for scenes that are not yet scheduled.
 * </p>
 *
 * <p>
 * The bound is computed in two main steps:
 * </p>
 * <ol>
 *     <li>Compute a contribution for each unscheduled scene based on actors present and their costs.</li>
 *     <li>Adjust for cumulative actor contributions using ratios and sort actors to estimate the remaining cost.</li>
 * </ol>
 * The result is rounded up to handle floating-point errors.
 *
 * <p>
 * This lower bound is intended for use with search algorithms (e.g., ACS, A*, DDO)
 * to prune suboptimal branches efficiently.
 * </p>
 */
public class TSFastLowerBound implements FastLowerBound<TSState> {
    /** The TSP instance associated with this lower bound computation. */
    private final TSProblem problem;

    /**
     * Constructs a fast lower bound calculator for a given TSP problem.
     *
     * @param problem The Talent Scheduling Problem instance.
     */

    public TSFastLowerBound(TSProblem problem) {
        this.problem = problem;
    }

    /**
     * Computes a fast lower bound on the total cost from the given partial state.
     *
     * @param state     The current state of the scheduling problem.
     * @param variables The set of variables (scenes) still to be scheduled.
     * @return The computed lower bound on the total cost, rounded up.
     */
    @Override
    public double fastLowerBound(TSState state, Set<Integer> variables) {
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

    /**
     * Helper class that stores a ratio and the corresponding actor index.
     * Used for sorting actors when computing the lower bound.
     */
    private static class RatioAndActor implements Comparable<RatioAndActor> {
        /** The ratio associated with this actor. */
        public double ratio;
        /** The index of the actor. */
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
