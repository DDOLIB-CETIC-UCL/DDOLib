package org.ddolib.examples.layered.talentscheduling;

import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.ddolib.common.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * The talent scheduling problem (tsp) with AsTar.
 * Entry point for solving instances of the Talent Scheduling Problem (TSP)
 * using an A* search algorithm.
 *
 * <p>
 * This class reads a TSP instance from a file, initializes the corresponding
 * {@link TSProblem}, and creates a {@link Model} for {@link TSState}.
 * It then runs the A* solver to find a (near-)optimal schedule and prints
 * both the solution and search statistics.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 * java TSAstarMain [instanceFile]
 * </pre>
 * If no {@code instanceFile} argument is provided, the default instance
 * {@code data/TalentScheduling/film-12} will be used.
 */
public class TSAstarMain {

    private TSAstarMain() {
    }

    /**
     * Entry point of the program. Builds a {@link TSProblem} instance and solves it using
     * the A* algorithm.
     *
     * @param args optional command-line argument specifying the path to the instance file
     * @throws IOException if there is an error reading the instance file
     */
    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final TSProblem problem = new TSProblem(instance);
        Model<TSState> model = new Model<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }
        };

        Solution bestSolution = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
