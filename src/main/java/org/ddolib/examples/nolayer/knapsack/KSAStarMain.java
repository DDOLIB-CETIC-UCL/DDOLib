package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.nolayer.modeling.Solvers;
import org.ddolib.nolayer.solver.Solution;
import org.ddolib.common.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Knapsack Problem (KS) with A*, using the no-layer modeling API.
 * <p>
 * This class demonstrates how to solve an instance of the Knapsack Problem
 * using the A* search algorithm, reusing the fast lower bound defined by
 * {@link KSModel}.
 * </p>
 */
public final class KSAStarMain {

    private KSAStarMain() {
    }

    /**
     * Entry point of the A* demonstration for the Knapsack Problem.
     *
     * @param args command-line arguments (optional: instance file path)
     * @throws IOException if the instance file cannot be read
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "Knapsack",
                "instance_n1000_c1000_10_5_10_5_0").toString() : args[0];
        final KSProblem problem = KSProblem.fromFile(instance);
        final KSModel model = new KSModel(problem);


        Solution bestSolution = Solvers.minimizeAstar(model,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal KS value: " + -bestSolution.statistics().incumbent());
    }
}
