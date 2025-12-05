package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;

/**
 * The Maximum Independent Set Problem (MISP) with Ddo.
 * Entry point for solving the Maximum Independent Set Problem (MISP) using a Decision Diagram Optimization (DDO) solver.
 * <p>
 * This class demonstrates how to configure and run a DDO search algorithm for a MISP instance.
 * The model used for the search includes:
 * </p>
 * <ul>
 *     <li>The problem instance {@link MispProblem} read from a file.</li>
 *     <li>A relaxation {@link MispRelax} to merge multiple states during the DDO search.</li>
 *     <li>A ranking {@link MispRanking} to order states within the DDO search.</li>
 *     <li>A dominance checker {@link SimpleDominanceChecker} with {@link MispDominance} to prune dominated states.</li>
 *     <li>A fast lower bound {@link MispFastLowerBound} to guide the search.</li>
 * </ul>
 *
 * <p>
 * The best solutions and search statistics are printed to the standard output.
 * </p>
 */
public final class MispDdoMain {
    /**
     * Main method to execute the DDO solver on a MISP instance.
     * <p>
     * If no command-line argument is provided, the default instance
     * <code>data/MISP/tadpole_4_2.dot</code> is used.
     * </p>
     *
     * @param args optional command-line arguments; args[0] can specify the path to the MISP instance file
     * @throws IOException if there is an error reading the problem instance from the file
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = new MispProblem(instance);
        DdoModel<BitSet> model = new DdoModel<>() {
            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public MispFastLowerBound lowerBound() {
                return new MispFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<BitSet> dominance() {
                return new SimpleDominanceChecker<>(new MispDominance(), problem.nbVars());
            }

            @Override
            public MispRelax relaxation() {
                return new MispRelax(problem);
            }

            @Override
            public MispRanking ranking() {
                return new MispRanking();
            }
        };

        Solution bestSolution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }

}
