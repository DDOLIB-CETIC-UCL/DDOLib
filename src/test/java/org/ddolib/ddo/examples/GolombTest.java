package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GolombTest {

    public int[] solve(int n) {
        Golomb.GolombProblem problem = new Golomb.GolombProblem(n);
        final Golomb.GolombRelax relax = new Golomb.GolombRelax();
        final Golomb.GolombRanking ranking = new Golomb.GolombRanking();
        final FixedWidth<Golomb.GolombState> width = new FixedWidth<>(250);
        final VariableHeuristic<Golomb.GolombState> varh = new DefaultVariableHeuristic();
        final Frontier<Golomb.GolombState> frontier = new SimpleFrontier<>(ranking);

        final Solver solver = new ParallelSolver<Golomb.GolombState>(
                Runtime.getRuntime().availableProcessors(),
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
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        values[d.var()] = d.val();
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
        for (int n = 3; n < 8; n++) {
            int[] result = solve(n);
            System.out.println(Arrays.toString(result));
            assertEquals(solution[n], result[n-1]);
        }
    }



}