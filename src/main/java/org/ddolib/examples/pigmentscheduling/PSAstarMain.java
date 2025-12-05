package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Pigment Sequencing Problem (PSP) with AsTar.
 * Main class for solving a Pigment Sequencing Problem (PSP)
 * using the A* search algorithm.
 * <p>
 * This program loads a PSP instance (either from a specified file or from a default path),
 * builds a corresponding model, and applies the A* algorithm to minimize the total scheduling cost.
 * </p>
 * <p>
 * The computed solution is printed to the console, followed by statistics about the search process.
 * </p>
 * <p><b>Usage:</b></p>
 * <pre>
 * java PSAstarMain [instanceFile]
 * </pre>
 * If no {@code instanceFile} argument is provided, the default instance located at
 * {@code data/PSP/instancesWith2items/10} is used.
 */
public class PSAstarMain {
    /**
     * Entry point of the program.
     * <p>
     * This method initializes a {@link PSProblem} from the given instance file,
     * constructs a {@link Model} for the A* search, and runs the solver using
     * {@link Solvers#minimizeAstar(Model, java.util.function.BiConsumer)}.
     * The resulting solution and search statistics are printed to standard output.
     * </p>
     *
     * @param args optional command-line argument specifying the path to a PSP instance file
     * @throws IOException if an error occurs while reading the problem instance
     */
    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "PSP", "instancesWith5items", "3").toString() : args[0];
        final PSProblem problem = new PSProblem(instance);
        Model<PSState> model = new Model<>() {
            @Override
            public PSProblem problem() {
                return problem;
            }

            @Override
            public PSFastLowerBound lowerBound() {
                return new PSFastLowerBound(problem);
            }
        };

        Solution bestSolution = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
