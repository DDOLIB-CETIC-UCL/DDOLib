package org.ddolib.examples.nolayer.tsp;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

public class TSPModel implements Model<TSPState> {

    private final TSPProblem problem;
    private final FastLowerBound<TSPState> lowerBound;

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
