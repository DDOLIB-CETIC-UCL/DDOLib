package org.ddolib.examples.tsalt;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;
/**
 * The talent scheduling problem (tsp) with Ddo.
 * Entry point for solving instances of the Talent Scheduling Problem (TSP)
 * using a Decision Diagram Optimization (Ddo) approach.
 *
 * <p>
 * This class reads a TSP instance from a file, initializes the corresponding
 * {@link TSProblem}, and creates a {@link DdoModel} for {@link TSState}.
 * The model uses a {@link TSRelax} relaxation, a {@link TSRanking} state ranking,
 * and {@link TSFastLowerBound} for efficient lower-bound computations.
 * The solver then minimizes the objective function using the Ddo algorithm,
 * printing both the solution and search statistics.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 * java TSDdoMain [instanceFile]
 * </pre>
 * If no {@code instanceFile} argument is provided, the default instance
 * {@code data/TalentScheduling/film-12} will be used.
 */
public class TSMain {
    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final TSProblem problem = new TSProblem(instance);
        DdoModel<TSState> model = new DdoModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public Relaxation<TSState> relaxation() {
                return new TSRelax(problem);
            }

            @Override
            public TSRanking ranking() {
                return new TSRanking();
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);
    }
}
