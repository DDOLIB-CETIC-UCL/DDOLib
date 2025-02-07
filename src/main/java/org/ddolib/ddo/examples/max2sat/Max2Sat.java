package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.util.Arrays;
import java.util.HashMap;

public final class Max2Sat {

    public static void main(String[] args) {
        HashMap<BinaryClause, Integer> weights = new HashMap<>();
        weights.put(new BinaryClause(1, 3), 3);
        weights.put(new BinaryClause(-1, -3), 5);
        weights.put(new BinaryClause(-1, 3), 4);
        weights.put(new BinaryClause(2, -3), 2);
        weights.put(new BinaryClause(-2, -3), 1);
        weights.put(new BinaryClause(2, 3), 5);

        Max2SatProblem problem = new Max2SatProblem(3, weights);
        Max2SatRelax relax = new Max2SatRelax();
        Max2SatRanking ranking = new Max2SatRanking();

        final FixedWidth<Max2SatState> width = new FixedWidth<>(500);
        final VariableHeuristic<Max2SatState> varh = new DefaultVariableHeuristic<Max2SatState>();

        final Frontier<Max2SatState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<Max2SatState> solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        // Expected: (F, T, T) cost : 19
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));


    }
}
