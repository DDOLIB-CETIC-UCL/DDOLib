package org.ddolib.examples.nolayer.gruler;

import org.ddolib.nolayer.modeling.FastLowerBound;

/**
 * Fast lower bound for the nolayer Golomb Ruler Problem. It assumes that the next marks will add
 * the smallest missing distances.
 */
public class GRFlb implements FastLowerBound<GRState> {

    private final GRProblem problem;

    /**
     * Creates a new fast lower bound for the given Golomb Ruler problem.
     *
     * @param problem the Golomb Ruler problem instance
     */
    public GRFlb(GRProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(GRState state) {
        int missingMarks = problem.order() - state.getNumberOfMarks();

        int i = 0;
        int cost = 0;
        while (missingMarks != 0) {
            if (i != 0 && !state.getDistances().get(i)) {
                cost += i;
                missingMarks--;
            }
            i++;
        }
        return cost;
    }
}
