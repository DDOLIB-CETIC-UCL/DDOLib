package org.ddolib.examples.alp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Aircraft Landing Problem (ALP) with Ddo.
 * Main class to solve the <b>Aircraft Landing Problem (ALP)</b> using
 * the Decision Diagram Optimization (DDO) algorithm.
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *   <li>Load an ALP instance from a data file.</li>
 *   <li>Define a {@link DdoModel} for the problem, including relaxation, ranking,
 *       width heuristic, and fast lower bound.</li>
 *   <li>Solve the problem using the {@link Solvers} with the DDO algorithm.</li>
 *   <li>Monitor and print intermediate incumbent solutions found during the search.</li>
 * </ul>
 *
 * @see ALPProblem
 * @see ALPState
 * @see ALPRelax
 * @see ALPRanking
 * @see ALPFastLowerBound
 * @see Solvers
 * @see DdoModel
 */
public final class ALPDdoMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = args.length == 0 ?
                Path.of("data", "ALP", "alp_n100_r2_c2_std20_s0").toString() : args[0];
        final ALPProblem problem = new ALPProblem(fileStr);
        DdoModel<ALPState> model = new DdoModel<>() {
            @Override
            public ALPProblem problem() {
                return problem;
            }

            @Override
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
            }

            @Override
            public ALPRelax relaxation() {
                return new ALPRelax(problem);
            }

            @Override
            public ALPRanking ranking() {
                return new ALPRanking();
            }

            @Override
            public WidthHeuristic<ALPState> widthHeuristic() {
                return new FixedWidth<>(500);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
            //System.out.println(new ALPSolution(problem, sol));
        });

        System.out.println(stats);
    }
}