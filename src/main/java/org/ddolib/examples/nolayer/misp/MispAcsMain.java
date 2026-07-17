package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.*;
import org.ddolib.nolayer.solver.Solution;
import org.ddolib.common.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Maximum Independent Set Problem (MISP) with Acs.
 * Entry point for solving the Maximum Independent Set Problem (MISP) using an Anytime Column
 * Search (ACS) solver, in the no-layer modeling API.
 */
public final class MispAcsMain {

    private MispAcsMain() {
    }

    /**
     * Main method to execute the ACS solver on a given MISP instance.
     * <p>
     * If no command-line argument is provided, a default instance
     * <code>data/MISP/tadpole_4_2.dot</code> is used.
     *
     * @param args optional command-line arguments; args[0] can be the path to the MISP instance file
     * @throws IOException if there is an error reading the problem instance from the file
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = MispProblem.fromFile(instance);
        final MispModel baseModel = new MispModel(problem);

        final AcsModel<MispState> model = new AcsModel<>() {
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

            @Override
            public int columnWidth() {
                return 10;
            }
        };

        Solution bestSolution = Solvers.minimizeAcs(model, (sol, stats) -> {
            SolutionPrinter.printSolution(stats, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal MISP value: " + -bestSolution.value());
    }
}
