package org.ddolib.examples.layered.misp;

import java.util.BitSet;
import java.util.Set;
import org.ddolib.layered.modeling.FastLowerBound;

/**
 * Computes a fast lower bound for the Maximum Independent Set Problem (MISP).
 *
 * <p>This implementation of {@link FastLowerBound} estimates the best possible solution from a
 * given state by considering all remaining vertices optimistically. The bound is used in search
 * algorithms to prune suboptimal branches efficiently.
 */
public class MispFastLowerBound implements FastLowerBound<BitSet> {
    /** The MISP problem instance for which the lower bound is computed. */
    private final MispProblem problem;

    /**
     * Constructs a fast lower bound calculator for a given MISP problem.
     *
     * @param problem the MISP problem instance
     */
    public MispFastLowerBound(MispProblem problem) {
        this.problem = problem;
    }

    /**
     * Computes a fast lower bound for the given state and set of remaining variables.
     *
     * <p>The state contains the vertices that can still be selected (the unassigned vertices that
     * are not neighbors of a selected vertex). The method sums their weights and returns its
     * negation: this is the cost of selecting all of them, a lower bound on the cost of the
     * remaining decisions since the costs are the negated weights. This bound is weak, since it
     * ignores the edges between the selectable vertices; {@link MispCliqueCoverLowerBound} is
     * tighter for the same cost per node.
     *
     * @param state the current state of the solution as a {@link BitSet}
     * @param variables the set of remaining variable indices (unused in this implementation)
     * @return a fast lower bound on the optimal solution value
     */
    @Override
    public double fastLowerBound(BitSet state, Set<Integer> variables) {
        // We select all the remaining nodes
        return -state.stream().map(i -> problem.weight[i]).sum();
    }
}
