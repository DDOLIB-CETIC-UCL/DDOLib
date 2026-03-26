package org.ddolib.examples.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Single Machine with Inventory Constraint (SMIC) with Acs.
 * The {@code SMICAcsMain} class provides the entry point for solving instances of the
 * <b>Single Machine with Inventory Constraint (SMIC)</b> problem using the
 * <b>Anytime Column Search (ACS)</b> algorithm.
 * <p>
 * This main program:
 * </p>
 * <ul>
 *   <li>Loads an instance of the SMIC problem from a file (by default {@code data/SMIC/data10_2.txt});</li>
 *   <li>Constructs an {@link AcsModel} composed of the problem definition, a fast lower bound estimator,
 *       and a dominance checker to prune dominated states during search;</li>
 *   <li>Invokes the {@link Solvers#minimizeAcs(AcsModel, java.util.function.BiConsumer)} method
 *       to perform the optimization using ACS;</li>
 *   <li>Prints the best found solution and search statistics.</li>
 * </ul>
 *
 *
 * <p><b>Usage:</b></p>
 * <pre>
 *   java SMICAcsMain [instanceFile]
 * </pre>
 * If no instance file is provided as an argument, the program defaults to
 * {@code data/SMIC/data10_2.txt}.
 *
 * <p><b>Example:</b></p>
 * <pre>
 *   java SMICAcsMain data/SMIC/data20_3.txt
 * </pre>
 *
 * @see SMICProblem
 * @see SMICState
 * @see SMICFastLowerBound
 * @see SMICDominance
 * @see SimpleDominanceChecker
 * @see Solvers#minimizeAcs(AcsModel, java.util.function.BiConsumer)
 */
public class SMICAcsMain {
    /**
     * Entry point of the SMIC Anytime Column Search solver.
     * Initializes the problem instance, builds the ACS model,
     * and executes the optimization process.
     *
     * @param args command-line arguments; the first argument may specify the path
     *             to the SMIC instance file. If omitted, the default instance
     *             {@code data/SMIC/data10_2.txt} is used.
     * @throws IOException if the instance file cannot be read.
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "SMIC", "data10_1.txt").toString() : args[0];
        final SMICProblem problem = new SMICProblem(instance);
        AcsModel<SMICState> model = new AcsModel<>() {
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

        Solution bestSolution = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
