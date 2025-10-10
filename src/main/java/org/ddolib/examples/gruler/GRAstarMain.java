package org.ddolib.examples.gruler;

import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.*;

import java.io.IOException;

/**
 * This class demonstrates how to implement a solver for the Golomb ruler problem.
 * For more information on this problem, see
 * <a href="https://en.wikipedia.org/wiki/Golomb_ruler">Golomb Ruler - Wikipedia</a>.
 * <p>
 * This model was introduced by Willem-Jan van Hoeve.
 * In this model:
 * - Each variable/layer represents the position of the next mark to be placed.
 * - The domain of each variable is the set of all possible positions for the next mark.
 * - A mark can only be added if the distance between the new mark and all previous marks
 * is not already present in the set of distances between marks.
 * <p>
 * The cost of a transition is defined as the distance between the new mark and the
 * previous last mark. Consequently, the cost of a solution is the position of the last mark.
 */
public class GRAstarMain {

    public static void main(final String[] args) throws IOException {
        GRProblem problem = new GRProblem(8);
        final Model<GRState> model = new Model<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

        };

        Solve<GRState> solve = new Solve<>();

        SearchStatistics stats = solve.minimize(model);

        solve.onSolution(stats);
    }
}
