package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Pigment Sequencing Problem (PSP) with Ddo.
 * Main class for solving a Precedence-constrained Scheduling Problem (PSP)
 * using the Decision Diagram Optimization (DDO) algorithm.
 * <p>
 * This program loads a PSP instance (from a file or a default path),
 * constructs a {@link DdoModel} that defines the problem structure,
 * its relaxation, ranking strategy, and lower bound,
 * then runs the DDO solver to minimize the total scheduling cost.
 * </p>
 * <p>
 * The solution and search statistics are printed to the console once the solver terminates.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * java PSDdoMain [instanceFile]
 * </pre>
 * If no {@code instanceFile} argument is provided, the program defaults to the instance located at:
 * {@code data/PSP/instancesWith2items/10}.
 */
public class PSDdoMain {
    /**
     * Entry point of the program.
     * <p>
     * This method performs the following steps:
     * </p>
     * <ol>
     *     <li>Loads a PSP instance from the provided file path (or uses a default path).</li>
     *     <li>Builds a {@link DdoModel} specifying the problem definition, relaxation, ranking, and lower bound.</li>
     *     <li>Invokes the DDO solver via {@link Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)}.</li>
     *     <li>Prints the computed solution and the corresponding {@link SearchStatistics} to standard output.</li>
     * </ol>
     * @param args optional command-line argument specifying the path to a PSP instance file.
     *             If omitted, a default instance path is used.
     * @throws IOException if an error occurs while reading the problem instance.
     */
    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data","PSP","instancesWith5items","3").toString() : args[0];
        final PSProblem problem = new PSProblem(instance);
        DdoModel<PSState> model = new DdoModel<>() {
            @Override
            public PSProblem problem() {
                return problem;
            }

            @Override
            public PSRelax relaxation() {
                return new PSRelax(problem);
            }

            @Override
            public PSRanking ranking() {
                return new PSRanking();
            }

            @Override
            public PSFastLowerBound lowerBound() {
                return new PSFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic<PSState> widthHeuristic() {
                return new FixedWidth<>(100);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);

    }
}
