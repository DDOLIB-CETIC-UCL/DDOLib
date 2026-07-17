package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.*;
import org.ddolib.nolayer.solver.Solution;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Maximum Independent Set Problem (MISP) with Anytime Weighted A* (AWA*).
 * Entry point for solving the Maximum Independent Set Problem (MISP) using an AWA* solver, in
 * the no-layer modeling API.
 */
public class MispAwAstarMain {

    private MispAwAstarMain() {
    }

    /**
     * Main method to execute the AWA* solver on a given MISP instance.
     * <p>
     * If no command-line argument is provided, a default instance
     * <code>data/MISP/weighted.dot</code> is used.
     *
     * @param args optional command-line arguments; args[0] can be the path to the MISP instance file
     * @throws IOException if there is an error reading the problem instance from the file
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "weighted.dot").toString() : args[0];
        final MispProblem problem = MispProblem.fromFile(instance);
        final MispModel baseModel = new MispModel(problem);

        final AwAstarModel<MispState> model = new AwAstarModel<MispState>() {
            @Override
            public Problem<MispState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<MispState> lowerBound() {
                return baseModel.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<MispState> dominance() {
                return baseModel.dominance();
            }

        };

        Solution bestSolution = Solvers.minimizeAwAstar(model, (sol, stats) -> {
            // SolutionPrinter.printSolution(stats, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal MISP value: " + -bestSolution.value());
    }
}
