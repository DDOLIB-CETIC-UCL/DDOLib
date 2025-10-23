package org.ddolib.examples.tsp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;
/**
 * Main class to solve a Traveling Salesman Problem (TSP) instance using the Decision Diagram Optimization (DDO) method.
 *
 * <p>
 * This class reads a TSP instance from an XML file, initializes a {@link TSPProblem} and a {@link DdoModel} with:
 * </p>
 * <ul>
 *     <li>a relaxation strategy ({@link TSPRelax}),</li>
 *     <li>a state ranking ({@link TSPRanking}),</li>
 *     <li>a fast lower bound ({@link TSPFastLowerBound}),</li>
 *     <li>and a fixed width heuristic ({@link FixedWidth}) for limiting the decision diagram width.</li>
 * </ul>
 * The DDO solver is then used to minimize the TSP objective, printing the best solution and search statistics.
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 * java TSPDdoMain [instanceFile]
 * </pre>
 * If no {@code instanceFile} argument is provided, the default instance
 * ("data/TSP/instance_18_0.xml") is used. The width of the decision diagram is fixed at 500.
 */
public class TSPDdoMain {

    public static void main(final String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TSP", "instance_18_0.xml").toString() : args[0];
        final TSPProblem problem = new TSPProblem(instance);
        DdoModel<TSPState> model = new DdoModel<TSPState>() {
            @Override
            public Problem<TSPState> problem() {
                return problem;
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
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public WidthHeuristic<TSPState> widthHeuristic() {
                return new FixedWidth<>(500);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);

    }


}
