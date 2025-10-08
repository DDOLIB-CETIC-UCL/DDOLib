package org.ddolib.examples.ddo.srflp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;

import javax.lang.model.type.NullType;
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
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "Cl5").toString() : args[0];
        final int maxWidth = args.length > 1 ? Integer.parseInt(args[1]) : 50;

        final SRFLPProblem problem = SRFLPIO.readInstance(filename);
        SolverConfig<SRFLPState, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SRFLPRelax(problem);
        config.flb = new SRFLPFastLowerBound(problem);
        config.ranking = new SRFLPRanking();

        config.width = new FixedWidth<>(maxWidth);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        Solver solver = new SequentialSolver<>(config);

        long start = System.currentTimeMillis();
        solver.minimize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.constructBestSolution(problem.nbVars());
        double obj = solver.bestValue().get() + problem.rootValue();

        System.out.printf("Instance: %s%n", filename);
        System.out.printf("Max width: %s%n", maxWidth);
        System.out.printf("Duration : %f seconds%n", duration);
        System.out.printf("Objective: %s%n", obj);
        System.out.printf("Solution : %s", Arrays.toString(solution));

    }
}
