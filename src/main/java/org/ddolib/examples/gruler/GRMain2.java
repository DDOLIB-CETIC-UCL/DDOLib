package org.ddolib.examples.gruler;

import org.ddolib.astar.core.solver.ACSSolver;
import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DefaultFastLowerBound;

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
public class GRMain2 {

    public static void main(final String[] args) throws IOException {
        SolverConfig<GRState, Integer> config = new SolverConfig<>();
        GRProblem problem = new GRProblem(3);
        config.problem = problem;
//        config.relax = new GRRelax();
//        config.ranking = new GRRanking();
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
//        config.exportAsDot = true;
        config.flb = new GRFastLowerBound(problem);
//        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
//        final SequentialSolver<GRState, Integer> solver = new SequentialSolver<>(config);
        final AStarSolver<GRState, Integer> solver = new AStarSolver<>(config);

        long start = System.currentTimeMillis();
        solver.minimize();
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

        System.out.printf("Duration : %.3f%n", duration);
        System.out.printf("Objective: %s%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}
