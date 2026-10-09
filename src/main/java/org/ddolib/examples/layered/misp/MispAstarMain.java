package org.ddolib.examples.layered.misp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * The Maximum Independent Set Problem (MISP) with AsTar. Entry point for solving the Maximum
 * Independent Set Problem (MISP) using an A* solver.
 *
 * <p>This class demonstrates how to configure and run an A* search algorithm for a MISP instance.
 * The model used for the search includes:
 *
 * <ul>
 *   <li>The problem instance {@link MispProblem} read from a file.
 *   <li>A dominance checker {@link SimpleDominanceChecker} with {@link MispDominance} to prune
 *       dominated states.
 *   <li>A fast lower bound {@link MispFastLowerBound} to guide the A* search.
 *   <li>Debug mode enabled through {@link DebugLevel#ON}.
 * </ul>
 *
 * <p>The best solutions and search statistics are printed to the standard output.
 */
public final class MispAstarMain {

    private MispAstarMain() {}

    /**
     * Main method to execute the A* solver on a MISP instance.
     *
     * <p>If no command-line argument is provided, the default instance <code>
     * data/MISP/tadpole_4_2.dot</code> is used.
     *
     * @param args optional command-line arguments; args[0] can specify the path to the MISP
     *     instance file
     * @throws IOException if there is an error reading the problem instance from the file
     */
    public static void main(String[] args) throws IOException {
        final String instance =
                args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = new MispProblem(instance);
        Model<BitSet> model =
                new Model<>() {
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
                };

        Solution bestSolution =
                Solvers.minimizeAstar(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
