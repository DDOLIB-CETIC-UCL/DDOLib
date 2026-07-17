package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

/**
 * Base model for the Maximum Independent Set Problem (MISP), in the no-layer modeling API.
 * <p>
 * Bundles the {@link MispProblem} together with a {@link MispFlb} fast lower bound, so that
 * the various MISP solver entry points can build on top of it.
 */
public class MispModel implements Model<MispState> {

    private final MispProblem problem;
    private final FastLowerBound<MispState> lowerBound;

    /**
     * Creates a new model wrapping the given MISP problem.
     *
     * @param problem the MISP problem instance to solve
     */
    public MispModel(MispProblem problem) {
        this.problem = problem;
        this.lowerBound = new MispFlb(problem);
    }

    @Override
    public Problem<MispState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<MispState> lowerBound() {
        return lowerBound;
    }
}
