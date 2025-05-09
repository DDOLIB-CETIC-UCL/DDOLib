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

import java.util.Arrays;

public final class SRFLP {


    public static void main(String[] args) {
        int[] lengths = {5, 3, 2, 6};
        int[][] flows = {
                {0, 8, 3, 5},
                {8, 0, 1, 4},
                {3, 1, 0, 6},
                {5, 4, 6, 0}
        };

        final SRFLPProblem problem = new SRFLPProblem(lengths, flows);
        final SRFLPRelax relax = new SRFLPRelax();
        final SRFLPRanking ranking = new SRFLPRanking();

        final WidthHeuristic<SRFLPState> width = new FixedWidth<>(500);
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


        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", obj);
        System.out.printf("Solution : %s", Arrays.toString(solution));


    }
}
