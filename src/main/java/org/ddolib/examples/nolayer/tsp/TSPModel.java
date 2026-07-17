package org.ddolib.examples.nolayer.tsp;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

/**
 * Base model for the Traveling Salesman Problem (TSP), using the no-layer modeling API.
 * <p>
 * Bundles a {@link TSPProblem} instance together with the {@link TSPFlb} fast lower bound
 * used to guide and prune the search.
 * </p>
 */
public class TSPModel implements Model<TSPState> {

    private final TSPProblem problem;
    private final FastLowerBound<TSPState> lowerBound;

    /**
     * Creates a new model for the given TSP instance.
     *
     * @param problem the TSP instance to solve
     */
    public TSPModel(TSPProblem problem) {
        this.problem = problem;
        this.lowerBound = new TSPFlb(problem);
    }

    @Override
    public Problem<TSPState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<TSPState> lowerBound() {
        return lowerBound;
    }
}
