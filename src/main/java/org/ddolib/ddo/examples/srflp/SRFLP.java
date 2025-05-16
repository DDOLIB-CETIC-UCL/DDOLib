package org.ddolib.ddo.examples.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.heuristics.WidthHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public final class SRFLP {


    public static int eval(int[] solution, int[] length, int[][] flows) {
        int toReturn = 0;
        for (int k = 0; k < solution.length; k++) {
            for (int i = 0; i < k; i++) {
                for (int j = k + 1; j < solution.length; j++) {
                    toReturn += length[solution[k]] * flows[solution[i]][solution[j]];
                }
            }

        }
        return toReturn;
    }

    public static void main(String[] args) throws IOException {
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "Cl7").toString() : args[0];

        final SRFLPProblem problem = SRFLPIO.readInstance(filename);
        final SRFLPRelax relax = new SRFLPRelax(problem);
        final SRFLPRanking ranking = new SRFLPRanking();

        int[] sol1 = {0, 4, 1, 3, 5, 2, 6};
        int[] sol2 = {0, 1, 3, 4, 5, 2, 6};

        System.out.printf("Root value: %s%n", problem.rootValue());
        System.out.printf("Value of %s%n: %d%n", Arrays.toString(sol1), eval(sol1, problem.lengths, problem.flows));
        System.out.printf("Value of %s%n: %d%n", Arrays.toString(sol2), eval(sol2, problem.lengths, problem.flows));

        final WidthHeuristic<SRFLPState> width = new FixedWidth<>(2);
        final VariableHeuristic<SRFLPState> varh = new DefaultVariableHeuristic<>();
        final Frontier<SRFLPState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<SRFLPState> solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        long start = System.currentTimeMillis();
        SearchStatistics stat = solver.maximize();
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

        double obj = -solver.bestValue().get() + problem.rootValue();


        System.out.printf("Instance: %s%n", filename);
        System.out.printf("Duration : %f seconds%n", duration);
        System.out.printf("Objective: %s%n", obj);
        System.out.printf("Solution : %s", Arrays.toString(solution));

    }
}
