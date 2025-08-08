package org.ddolib.examples.ddo.max2sat;

import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.ddolib.examples.ddo.max2sat.Max2SatIO.readInstance;
import static org.ddolib.factory.Solvers.parallelSolver;

public final class Max2Sat {

    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.examples.ddo.max2sat.Max2Sat"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="oorg.ddolib.ddo.examples.max2sat.Max2Sat -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        String file = args.length == 0 ? Paths.get("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString() : args[0];
        int maxWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 50;

        Max2SatProblem problem = readInstance(file);

        Max2SatRelax relax = new Max2SatRelax(problem);
        Max2SatRanking ranking = new Max2SatRanking();

        final FixedWidth<Max2SatState> width = new FixedWidth<>(maxWidth);
        final VariableHeuristic<Max2SatState> varh = new DefaultVariableHeuristic<>();

        final Frontier<Max2SatState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        Solver solver = parallelSolver(
                Runtime.getRuntime().availableProcessors(),
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


        System.out.printf("Instance : %s%n", file);
        System.out.printf("Max width: %d%n", maxWidth);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));

    }
}
