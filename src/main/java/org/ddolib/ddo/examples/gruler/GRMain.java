package org.ddolib.ddo.examples.gruler;

import org.ddolib.ddo.algo.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.algo.heuristics.FixedWidth;
import org.ddolib.ddo.algo.heuristics.VariableHeuristic;
import org.ddolib.ddo.algo.solver.ddosolver.SequentialSolver;
import org.ddolib.ddo.api.Solvers;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;

import java.io.IOException;
import java.util.Arrays;

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
public class GRMain {

    public static void main(final String[] args) throws IOException {
        GRProblem problem = new GRProblem(9);
        final GRRelax relax = new GRRelax();
        final GRRanking ranking = new GRRanking();
        final FixedWidth<GRState> width = new FixedWidth<>(10);
        final VariableHeuristic<GRState> varh = new DefaultVariableHeuristic();
        final Frontier<GRState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final SequentialSolver<GRState, Integer> solver = Solvers.sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars() + 1];
                    values[0] = 0;
                    for (Decision d : decisions) {
                        values[d.var() + 1] = d.val();
                    }
                    return values;
                })
                .get();

        System.out.println(String.format("Duration : %.3f", duration));
        System.out.println(String.format("Objective: %s", solver.bestValue().get()));
        System.out.println(String.format("Solution : %s", Arrays.toString(solution)));
    }
}
