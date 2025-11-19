package org.ddolib.examples.tsp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;
/**
 * The Traveling Salesman Problem (TSP) with Acs.
 * Main class to solve a Traveling Salesman Problem (TSP) instance using the ACS (Anytime Column Search) algorithm.
 *
 * <p>
 * This class reads a problem instance from a file (XML format), initializes a {@link TSPProblem} and an
 * {@link AcsModel} with a fast lower bound ({@link TSPFastLowerBound}), and then solves the problem using
 * the ACS solver. The solution and search statistics are printed to the console.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 * java TSPAcsMain [instanceFile]
 * </pre>
 * If no {@code instanceFile} argument is provided, a default instance
 * ("data/TSP/instance_18_0.xml") is used.
 */
public class TSPAcsMain {

    public static void main(final String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TSP", "instance_18_0.xml").toString() : args[0];
        final TSPProblem problem = new TSPProblem(instance);
        AcsModel<TSPState> model = new AcsModel<TSPState>() {
            @Override
            public Problem<TSPState> problem() {
                return problem;
            }

            @Override
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);

    }


}
