package org.ddolib.examples.nolayer.gruler;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

/**
 * Nolayer model for the Golomb Ruler Problem, bundling the {@link GRProblem} with its
 * {@link GRFlb} fast lower bound.
 */
public class GRModel implements Model<GRState> {

    private final GRProblem problem;
    private final FastLowerBound<GRState> lowerBound;

    /**
     * Creates a new model for the given Golomb Ruler problem.
     *
     * @param problem the Golomb Ruler problem instance
     */
    public GRModel(GRProblem problem) {
        this.problem = problem;
        this.lowerBound = new GRFlb(problem);
    }

    @Override
    public Problem<GRState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<GRState> lowerBound() {
        return lowerBound;
    }
}
