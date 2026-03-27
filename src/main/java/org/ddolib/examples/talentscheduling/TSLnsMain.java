package org.ddolib.examples.talentscheduling;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Entry point for solving the Talent Scheduling (TalentSched) problem using a
 * Large Neighborhood Search (LNS) approach.
 *
 * <p>This class reads an instance file describing a talent scheduling problem,
 * constructs an LNS model, and searches for an optimal or near-optimal schedule
 * within a time limit. The best solution found is printed along with runtime
 * statistics.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * java TSLnsMain [instanceFilePath]
 * </pre>
 * <ul>
 *     <li>{@code instanceFilePath} (optional): Path to the Talent Scheduling instance file.
 *     If omitted, the default instance located at {@code data/TalentScheduling/film-12} is used.</li>
 * </ul>
 *
 * <p>The LNS model is configured with:</p>
 * <ul>
 *     <li>A {@link TSFastLowerBound} for efficient lower bound estimation.</li>
 *     <li>A {@link TSRanking} to rank scheduling decisions during the search.</li>
 * </ul>
 *
 * <p>The search is limited to 1000 milliseconds per iteration, and the best
 * solution found is printed to {@code System.out} along with detailed statistics.</p>
 *
 * <p>This implementation does not currently include dominance checks or width heuristics.</p>
 *
 * @author
 * @version 1.0
 */
public class TSLnsMain {
    /**
     * Main method to run the Talent Scheduling LNS solver.
     *
     * @param args optional command-line argument:
     *             <ul>
     *                 <li>{@code args[0]}: path to the Talent Scheduling instance file
     *                 (default: {@code data/TalentScheduling/film-12})</li>
     *             </ul>
     * @throws IOException if there is an error reading the instance file.
     */
    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Paths.get("data", "TalentScheduling", "film-12").toString() : args[0];
        final TSProblem problem = new TSProblem(instance);
        LnsModel<TSState> model = new LnsModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public TSFastLowerBound lowerBound() {
                return new TSFastLowerBound(problem);
            }

            @Override
            public TSRanking ranking() {
                return new TSRanking();
            }
        };

        Solution bestSolution = Solvers.minimizeLns(model, s -> s.runTimeMs() < 1000, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}