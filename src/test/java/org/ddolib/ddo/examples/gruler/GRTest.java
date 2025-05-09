package org.ddolib.ddo.examples.gruler;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GRTest {
    public int[] solve(int n) {
        GRProblem problem = new GRProblem(n);
        final GRRelax relax = new GRRelax();
        final GRRanking ranking = new GRRanking();
        final FixedWidth<GRState> width = new FixedWidth<>(32);
        final VariableHeuristic<GRState> varh = new DefaultVariableHeuristic<>();
        final Frontier<GRState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver(
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
        return solution;
    }

    // unit test for Golomb sequence
    @org.junit.jupiter.api.Test
    void test() {
        // known solutions
        int[] solution = {0, 1, 3, 6, 11, 17, 25, 34, 44, 55, 72, 85, 106};
        for (int n = 3; n < 7; n++) {
            int[] result = solve(n);
            assertEquals(solution[n - 1], result[n - 1]);
        }
    }
}
