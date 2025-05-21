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

/**
 * This class demonstrates how to implement a solver for the SRFLP.
 * For more details and this problem and its model see
 * <a href= https://drops.dagstuhl.de/storage/00lipics/lipics-vol235-cp2022/LIPIcs.CP.2022.14/LIPIcs.CP.2022.14.pdf>
 * Coppé, V., Gillard, X., and Schaus, P. (2022).
 * Solving the constrained single-row facility layout problem with decision diagrams.
 * In 28th International Conference on Principles and Practice of Constraint Programming (CP 2022) (pp. 14-1).
 * Schloss Dagstuhl–Leibniz-Zentrum für Informatik.
 * </a>
 * <p>
 * In this model:
 * <ul>
 *     <li>Each variable/layer represent the position of the next department to be placed.</li>
 *     <li>The domain of each variable is the set of the remaining not placed department.</li>
 * </ul>
 */
public final class SRFLPMain {

    public static void main(String[] args) throws IOException {
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() : args[0];

        final SRFLPProblem problem = SRFLPIO.readInstance(filename);
        final SRFLPRelax relax = new SRFLPRelax(problem);
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


        System.out.printf("Instance: %s%n", filename);
        System.out.printf("Duration : %f seconds%n", duration);
        System.out.printf("Objective: %s%n", obj);
        System.out.printf("Solution : %s", Arrays.toString(solution));

    }
}
