package org.ddolib.examples.layered.tsp;

import java.io.IOException;
import java.nio.file.Paths;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Main class to solve a Traveling Salesman Problem (TSP) instance using the Decision Diagram
 * Optimization (DDO) method.
 *
 * <p>This class reads a TSP instance from an XML file, initializes a {@link TSPProblem} and a
 * {@link DdoModel} with:
 *
 * <ul>
 *   <li>a relaxation strategy ({@link TSPRelax}),
 *   <li>a state ranking ({@link TSPRanking}),
 *   <li>a fast lower bound ({@link TSPFastLowerBound}),
 *   <li>and a fixed width heuristic ({@link FixedWidth}) for limiting the decision diagram width.
 * </ul>
 *
 * <p>The DDO solver is then used to minimize the TSP objective, printing the best solution and
 * search statistics.
 *
 * <p>Usage:
 *
 * <pre>
 * java TSPDdoMain [instanceFile]
 * </pre>
 *
 * <p>If no {@code instanceFile} argument is provided, the default instance
 * ("data/TSP/instance_18_0.xml") is used. The width of the decision diagram is fixed at 500.
 */
public class TSPDdoMain {

    private TSPDdoMain() {}

    /**
     * Entry point of the program. Builds a TSP instance and solves it using the DDO algorithm.
     *
     * @param args optional command-line argument: path to the TSP instance file (default: {@code
     *     data/TSP/instance_18_0.xml})
     * @throws IOException if there is an error reading the instance file
     */
    public static void main(final String[] args) throws IOException {
        String instance =
                args.length == 0
                        ? Paths.get("data", "TSP", "instance_18_0.xml").toString()
                        : args[0];
        final TSPProblem problem = new TSPProblem(instance);
        DdoModel<TSPState> model =
                new DdoModel<TSPState>() {
                    @Override
                    public Problem<TSPState> problem() {
                        return problem;
                    }

                    @Override
                    public TSPFastLowerBound lowerBound() {
                        return new TSPFastLowerBound(problem);
                    }

                    @Override
                    public Relaxation<TSPState> relaxation() {
                        return new TSPRelax(problem);
                    }

                    @Override
                    public TSPRanking ranking() {
                        return new TSPRanking();
                    }

                    @Override
                    public WidthHeuristic<TSPState> widthHeuristic() {
                        return new FixedWidth<>(500);
                    }

                    @Override
                    public boolean useCache() {
                        return true;
                    }
                };

        Solution bestSolution =
                Solvers.minimizeDdo(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });
        System.out.println(bestSolution);
    }
}
