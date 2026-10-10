package org.ddolib.examples.layered.pdptw;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.ddolib.layered.modeling.FastLowerBound;

/** Implementation of a fast upper bound for the PDPTW. */
public class PDPTWFastLowerBound implements FastLowerBound<PDPTWState> {
    private final double[] leastIncidentEdge;
    PDPTWProblem problem;

    /**
     * Builds a fast lower bound calculator for the given PDPTW problem, precomputing the least
     * incident edge of every node.
     *
     * @param problem the PDPTW problem to compute lower bounds for
     */
    public PDPTWFastLowerBound(PDPTWProblem problem) {
        this.problem = problem;
        this.leastIncidentEdge = new double[problem.n];
        for (int i = 0; i < problem.n; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < problem.n; j++) {
                if (i != j) {
                    // both directions, so that it bounds the edge entering i even if the
                    // matrix is not symmetric
                    min =
                            Math.min(
                                    min,
                                    Math.min(problem.timeMatrix[i][j], problem.timeMatrix[j][i]));
                }
            }
            leastIncidentEdge[i] = min;
        }
    }

    @Override
    public double fastLowerBound(PDPTWState state, Set<Integer> variables) {
        return fastLowerBound(state, variables.size());
    }

    /**
     * Computes a fast lower bound on the remaining cost from the given state.
     *
     * <p>The remaining cost is the arrival time at the depot minus the current time. Let {@code m}
     * be the number of remaining hops, {@code t} the current time, {@code pre[i]} the sum of the
     * {@code i} smallest incoming edges of the nodes to visit and {@code e(j)} their {@code j}-th
     * smallest earliest start. The arrival time at the {@code i}-th next node is at least {@code t
     * + pre[i]}. It is also at least {@code max(t, e(j)) + pre[i - j]} for every {@code j <= i}: at
     * least {@code i - j + 1} of the first {@code i} visited nodes cannot be served before {@code
     * e(j)}, the first of them is visited at position {@code j} or before, and at least {@code i -
     * j} hops follow it.
     *
     * <p>On a merged state, the set of nodes to visit may contain more nodes than the number of
     * remaining hops: the bounds are then computed on all of them, which can only decrease them.
     *
     * @param state the current state
     * @param nbUnassignedVariables the number of variables (nodes) still to visit, including the
     *     final return to the depot
     * @return the lower bound on the remaining cost, or {@link Double#POSITIVE_INFINITY} if no
     *     feasible completion respecting the time windows exists
     */
    public double fastLowerBound(PDPTWState state, int nbUnassignedVariables) {
        int[] toVisit = state.allToVisit.stream().toArray();

        // smallest incoming edge of each node to visit, plus the depot for the final hop
        ArrayList<Double> incoming = new ArrayList<>(toVisit.length + 1);
        ArrayList<Double> earlyLines = new ArrayList<>(toVisit.length + 1);
        ArrayList<Double> deadlines = new ArrayList<>(toVisit.length + 1);
        incoming.add(leastIncidentEdge[0]);
        earlyLines.add(problem.timeWindows[0].start());
        deadlines.add(problem.timeWindows[0].end());
        for (int i : toVisit) {
            incoming.add(leastIncidentEdge[i]);
            earlyLines.add(problem.timeWindows[i].start());
            deadlines.add(problem.timeWindows[i].end());
        }
        Collections.sort(incoming);
        Collections.sort(earlyLines);
        Collections.sort(deadlines);

        int hops = Math.min(nbUnassignedVariables, incoming.size());
        double[] pre = new double[hops + 1];
        for (int i = 1; i <= hops; i++) {
            pre[i] = pre[i - 1] + incoming.get(i - 1);
        }

        // On a merged state, only hops nodes among the nodes to visit are visited: the i-th
        // smallest deadline of these nodes is at least the (offset + i)-th smallest of all of them
        int offsetForDeadlines = deadlines.size() - hops;
        double t = state.minCurrentTime;
        double arrival = t;
        for (int i = 1; i <= hops; i++) {
            // lower bound on the arrival time at the i-th next node
            arrival = t + pre[i];
            for (int j = 1; j <= i; j++) {
                arrival = max(arrival, max(t, earlyLines.get(j - 1)) + pre[i - j]);
            }
            // the i nodes with the smallest deadlines cannot all be visited in time
            if (arrival > deadlines.get(offsetForDeadlines + i - 1)) {
                return Double.POSITIVE_INFINITY;
            }
        }
        // the transition costs of a merged state are computed from its latest current time
        return arrival - state.maxCurrentTime;
    }
}
