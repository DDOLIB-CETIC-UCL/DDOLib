package org.ddolib.examples.bench;

import org.ddolib.common.solver.Solution;
import org.ddolib.examples.talentscheduling.TSFastLowerBound;
import org.ddolib.examples.talentscheduling.TSProblem;
import org.ddolib.examples.talentscheduling.TSState;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * The talent scheduling problem (tsp) with Acs.
 * Entry point for solving instances of the Talent Scheduling Problem (TSP) using
 * an Anytime Column Search (Acs) strategy.
 *
 * <p>
 * This class reads a problem instance from a file, initializes the corresponding
 * {@link TSProblem}, and creates an {@link AcsModel} for {@link TSState}.
 * It then runs the ACS solver to find a (near-)optimal schedule and prints
 * both the solution and search statistics.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 * java TSAcsMain [instanceFile]
 * </pre>
 * If no {@code instanceFile} argument is provided, the default instance
 * {@code data/TalentScheduling/film-12} will be used.
 */
public class TSAcsMain {
    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final long timeout = args.length == 2 ? Long.parseLong(args[1]) : 100;


        final TSProblem problem = new TSProblem(instance);
        AcsModel<TSState> model = new AcsModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }

            @Override
            public int columnWidth() {
                return 10;
            }
        };

        Solution bestSolution = Solvers.minimizeAcs(model, s -> s.runTimeMs() > timeout, (sol, s) -> {
            System.out.println("%%incumbent:" + s.incumbent() + " gap:" + s.gap() + " time:" + s.runTimeMs());
        });
        System.out.println("%%optimality:" + bestSolution.statistics().status()
                + " gap:" + bestSolution.statistics().gap() + " time:" + bestSolution.statistics().runTimeMs());

    }
}
