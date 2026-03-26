package org.ddolib.examples.hrcp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Solves the Human-Robot Collaboration with Precedences (HRCP) problem using A* search.
 * <p>
 * Demonstrates how to build an {@link HRCPProblem}, wrap it in a {@link Model},
 * and solve it with {@link Solvers#minimizeAstar}.
 * <p>
 * Solution values are encoded as {@code task * 3 + mode} where
 * mode 0 = Human, 1 = Robot, 2 = Collaborative.
 */
public class HRCPAstarMain {

    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0
                ? Path.of("data", "HRCP", "example_11").toString()
                : args[0];
        final HRCPProblem problem = new HRCPProblem(instance);
        System.out.println(problem);

        final Model<HRCPState> model = new Model<>() {
            @Override
            public Problem<HRCPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<HRCPState> lowerBound() {
                return new HRCPFastLowerBoundJiaxin(problem);
                //return new HRCPFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<HRCPState> dominance() {
                return new SimpleDominanceChecker<>(new HRCPDominance(), problem.nbVars());
            }
        };

        Solution bestSolution = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
            // decode the solution for clarity
            System.out.print("  Schedule: ");
            for (int i = 0; i < problem.n; i++) {
                int task = sol[i] / 3;
                int mode = sol[i] % 3;
                String modeName = switch (mode) {
                    case 0 -> "Human";
                    case 1 -> "Robot";
                    case 2 -> "Collab";
                    default -> "?";
                };
                System.out.printf("step %d → task %d (%s)  ", i, task, modeName);
            }
            System.out.println();
        });

        System.out.println();
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
