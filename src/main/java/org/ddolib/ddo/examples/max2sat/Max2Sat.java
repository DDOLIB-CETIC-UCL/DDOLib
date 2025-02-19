package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.ddolib.ddo.examples.max2sat.Max2SatIO.*;

public final class Max2Sat {

    public static void main(String[] args) throws IOException {
        Max2SatProblem problem = readInstance("data/Max2Sat/wcnf_var_3_opti_19.txt");

        Max2SatRelax relax = new Max2SatRelax(problem);
        Max2SatRanking ranking = new Max2SatRanking();

        final FixedWidth<ArrayList<Integer>> width = new FixedWidth<>(500);
        final VariableHeuristic<ArrayList<Integer>> varh = new DefaultVariableHeuristic<>();

        final Frontier<ArrayList<Integer>> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<ArrayList<Integer>> solver = new SequentialSolver<>(
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


        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));

    }
}
