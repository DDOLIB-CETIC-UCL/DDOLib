package org.ddolib.examples.layered.smic;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * The Single Machine with Inventory Constraint (SMIC) with AsTar. The {@code SMICAstarMain} class
 * provides the entry point for solving instances of the <b>Single Machine with Inventory Constraint
 * (SMIC)</b> problem using the <b>As star Search (A*)</b> algorithm.
 *
 * <p>This main program:
 *
 * <ul>
 *   <li>Loads an instance of the SMIC problem from a file (by default {@code
 *       data/SMIC/data10_2.txt});
 *   <li>Constructs an {@link Model} composed of the problem definition, a fast lower bound
 *       estimator, and a dominance checker to prune dominated states during search;
 *   <li>Invokes the {@link Solvers#minimizeAstar(Model, java.util.function.BiConsumer)} method to
 *       perform the optimization using A*;
 *   <li>Prints the best found solution and search statistics.
 * </ul>
 *
 * <p><b>Usage:</b>
 *
 * <pre>
 *   java SMICAstarMain [instanceFile]
 * </pre>
 *
 * <p>If no instance file is provided as an argument, the program defaults to {@code
 * data/SMIC/data10_2.txt}.
 *
 * <p><b>Example:</b>
 *
 * <pre>
 *   java SMICAstarMain data/SMIC/data20_3.txt
 * </pre>
 *
 * @see SMICProblem
 * @see SMICFastLowerBound
 * @see SMICDominance
 * @see SimpleDominanceChecker
 * @see Solvers#minimizeAstar(Model, java.util.function.BiConsumer)
 */
public class SMICAstarMain {

    private SMICAstarMain() {}

    /**
     * Entry point of the SMIC A* solver. Initializes the problem instance, builds the A* model, and
     * executes the optimization process.
     *
     * @param args command-line arguments; the first argument may specify the path to the SMIC
     *     instance file. If omitted, the default instance {@code data/SMIC/data10_2.txt} is used.
     * @throws IOException if the instance file cannot be read
     */
    public static void main(String[] args) throws IOException {
        final String instance =
                args.length == 0 ? Path.of("data", "SMIC", "example.txt").toString() : args[0];
        final SMICProblem problem = new SMICProblem(instance);
        Model<SMICState> model =
                new Model<>() {
                    @Override
                    public Problem<SMICState> problem() {
                        return problem;
                    }

                    @Override
                    public SMICFastLowerBound lowerBound() {
                        return new SMICFastLowerBound(problem);
                    }

                    @Override
                    public DominanceChecker<SMICState> dominance() {
                        return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
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
