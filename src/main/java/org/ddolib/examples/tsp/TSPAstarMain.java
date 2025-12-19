package org.ddolib.examples.tsp;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * The Traveling Salesman Problem (TSP) with AsTar.
 * Main class to solve a Traveling Salesman Problem (TSP) instance using the A* algorithm.
 *
 * <p>
 * This class reads a problem instance from a file (XML format), initializes a {@link TSPProblem} and a
 * {@link Model} with a fast lower bound ({@link TSPFastLowerBound}), and then solves the problem using
 * an A* search solver. The solution and search statistics are printed to the console.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 * java TSPAstarMain [instanceFile]
 * </pre>
 * If no {@code instanceFile} argument is provided, a default instance
 * ("data/TSP/instance_18_0.xml") is used.
 */
public class TSPAstarMain {

    public static void main(final String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TSP", "instance_18_0.xml").toString() : args[0];
        final TSPProblem problem = new TSPProblem(instance);
        Model<TSPState> model = new Model<TSPState>() {
            @Override
            public Problem<TSPState> problem() {
                return problem;
            }

            @Override
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }
        };

        Solution bestSolution = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);

    }


}
