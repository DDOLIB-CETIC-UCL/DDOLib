package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

public class MispModel implements Model<MispState> {

    private final MispProblem problem;
    private final FastLowerBound<MispState> lowerBound;

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
