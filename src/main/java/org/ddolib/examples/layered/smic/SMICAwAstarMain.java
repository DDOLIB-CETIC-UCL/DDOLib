package org.ddolib.examples.layered.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.layered.AwAstarModel;
import org.ddolib.modeling.layered.Problem;
import org.ddolib.modeling.layered.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public class SMICAwAstarMain {
    /**
     * Entry point of the SMIC A* solver.
     * Initializes the problem instance, builds the AW-A* model,
     * and executes the optimization process.
     *
     * @param args command-line arguments; the first argument may specify the path
     *             to the SMIC instance file. If omitted, the default instance
     *             {@code data/SMIC/data10_2.txt} is used.
     * @throws IOException if the instance file cannot be read.
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "SMIC", "example.txt").toString() : args[0];
        final SMICProblem problem = new SMICProblem(instance);
        AwAstarModel<SMICState> model = new AwAstarModel<>() {
            @Override
            public Problem<SMICState> problem() {
                return problem;
            }

            @Override
            public SMICFastLowerBound lowerBound() {
                return new SMICFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<SMICState> dominance() {
                return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            }

            @Override
            public double weight() {
                return 3.5;
            }
        };

        Solution bestSolution = Solvers.minimizeAwAStar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);

    }
}
