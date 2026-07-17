package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

/**
 * Base model for the Knapsack Problem (KS), using the no-layer modeling API.
 * <p>
 * Bundles a {@link KSProblem} instance together with the {@link KSFlb} fast lower bound
 * used to guide and prune the search.
 * </p>
 */
public class KSModel implements Model<KSState> {

    private final KSProblem problem;
    private final FastLowerBound<KSState> lowerBound;

    /**
     * Creates a new model for the given knapsack instance.
     *
     * @param problem the knapsack instance to solve
     */
    public KSModel(KSProblem problem) {
        this.problem = problem;
        this.lowerBound = new KSFlb(problem);
    }

    @Override
    public Problem<KSState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<KSState> lowerBound() {
        return lowerBound;
    }
}
