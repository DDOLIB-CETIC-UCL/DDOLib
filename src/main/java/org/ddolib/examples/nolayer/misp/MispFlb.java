package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.FastLowerBound;

/**
 * Fast lower bound for the Maximum Independent Set Problem (MISP), in the no-layer modeling API.
 * <p>
 * The bound sums the weights of all remaining nodes in a state, which is an admissible
 * (optimistic) estimate since selecting every remaining node ignores adjacency constraints.
 */
public class MispFlb implements FastLowerBound<MispState> {

    private final int[] weight;

    /**
     * Creates a new fast lower bound for the given MISP problem.
     *
     * @param problem the MISP problem instance providing the node weights
     */
    public MispFlb(MispProblem problem) {
        this.weight = problem.weight;
    }

    @Override
    public double fastLowerBound(MispState state) {
        double flb = 0;
        for (int i = state.remainingNodes().nextSetBit(0); i >= 0; i = state.remainingNodes().nextSetBit(i + 1)) {
            flb += weight[i];
        }
        return -flb;
    }
}
