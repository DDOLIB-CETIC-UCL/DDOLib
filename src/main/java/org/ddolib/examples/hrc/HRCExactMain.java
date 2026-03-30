package org.ddolib.examples.hrc;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Solves the Human-Robot Collaboration (HRC) scheduling problem using A* search.
 * <p>
 * This class demonstrates how to solve an HRC instance with the A* algorithm
 * provided by DDOLib. It creates a small example instance, defines a
 * {@link Model} with a fast lower bound and dominance checker, and runs the search.
 * </p>
 *
 * <p>Example output mapping: {@code 0 = Human, 1 = Robot, 2 = Collaborative}.</p>
 */
public class HRCExactMain {

    /**
     * Entry point.
     *
     * @param args optionally the path to an HRC instance file
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0
                ? Path.of("data", "HRC", "example_11").toString()
                : args[0];
        final HRCProblem problem = new HRCProblem(instance);

        System.out.println(problem);
        System.out.println();

        final ExactModel<HRCState> model = new ExactModel<HRCState>() {
            @Override
            public Problem<HRCState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<HRCState> lowerBound() {
                return new HRCFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<HRCState> dominance() {
                return new SimpleDominanceChecker<>(new HRCDominance(), problem.nbVars());
            }
        };

        Solution bestSolution = Solvers.minimizeExact(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println();
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
