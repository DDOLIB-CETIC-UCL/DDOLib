package org.ddolib.examples.bench;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.examples.smic.SMICDominance;
import org.ddolib.examples.smic.SMICFastLowerBound;
import org.ddolib.examples.smic.SMICProblem;
import org.ddolib.examples.smic.SMICState;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Single Machine with Inventory Constraint (SMIC) with AsTar.
 * The {@code SMICAstarMain} class provides the entry point for solving instances of the
 * <b>Single Machine with Inventory Constraint (SMIC)</b> problem using the
 * <b>As star Search (A*)</b> algorithm.
 * <p>
 * This main program:
 * </p>
 * <ul>
 *   <li>Loads an instance of the SMIC problem from a file (by default {@code data/SMIC/data10_2.txt});</li>
 *   <li>Constructs an {@link Model} composed of the problem definition, a fast lower bound estimator,
 *       and a dominance checker to prune dominated states during search;</li>
 *   <li>Invokes the {@link Solvers#minimizeAstar(Model, java.util.function.BiConsumer)} method
 *       to perform the optimization using A*;</li>
 *   <li>Prints the best found solution and search statistics.</li>
 * </ul>
 *
 *
 * <p><b>Usage:</b></p>
 * <pre>
 *   java SMICAstarMain [instanceFile]
 * </pre>
 * If no instance file is provided as an argument, the program defaults to
 * {@code data/SMIC/data10_2.txt}.
 *
 * <p><b>Example:</b></p>
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
    /**
     * Entry point of the SMIC A* solver.
     * Initializes the problem instance, builds the A* model,
     * and executes the optimization process.
     *
     * @param args command-line arguments; the first argument may specify the path
     *             to the SMIC instance file. If omitted, the default instance
     *             {@code data/SMIC/data10_2.txt} is used.
     * @throws IOException if the instance file cannot be read.
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "SMIC", "data10_2.txt").toString() : args[0];
        final long timeout = args.length == 2 ? Long.parseLong(args[1]) : 100;
        final SMICProblem problem = new SMICProblem(instance);
        Model<SMICState> model = new Model<>() {
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

        System.gc();
        long memoryBefore = MemoryUtil.getUsedMemory();

        Solution bestSolution = Solvers.minimizeAstar(model, s -> s.runTimeMs() > timeout,
                (sol, s) -> {
                    System.out.println("%%incumbent:" + s.incumbent() + " gap:" + s.gap() + " time:" + s.runTimeMs());
                });
        long memoryAfter = MemoryUtil.getUsedMemory();

        System.out.println("%%optimality:" + bestSolution.statistics().status()
                + " gap:" + bestSolution.statistics().gap()
                + " time:" + bestSolution.statistics().runTimeMs() + " "
                + MemoryUtil.printMemoryConsumption((memoryAfter - memoryBefore)));

    }
}
