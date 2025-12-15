package org.ddolib.examples.bench;

import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.talentscheduling.*;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;

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
public class TSDdoMain {
    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final long timeout = args.length == 2 ? Long.parseLong(args[1]) : 100;
        final TSProblem problem = new TSProblem(instance);
        DdoModel<TSState> model = new DdoModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
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
            public WidthHeuristic<TSState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public Frontier<TSState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        Solution bestSolution = Solvers.minimizeDdo(model,
                s -> s.runTimeMs() > timeout,
                (sol, s) -> {
                    System.out.println("%%incumbent:" + s.incumbent() + " gap:" + s.gap() + " time:" + s.runTimeMs());
                });
        System.out.println("%%optimality:" + bestSolution.statistics().status() + " gap:" + bestSolution.statistics().gap() + " time:" + bestSolution.statistics().runTimeMs());

    }
}
