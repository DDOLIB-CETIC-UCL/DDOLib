package org.ddolib.examples.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

/**
 * The Single Machine with Inventory Constraint (SMIC) with Ddo.
 * The {@code SMICDdoMain} class provides the entry point for solving instances of the
 * <b>Single Machine with Inventory Constraint (SMIC)</b> problem using the
 * <b> Decision Diagram Optimization (DDO)</b> approach.
 *
 * <p>
 * This main program performs the following steps:
 * </p>
 * <ul>
 *   <li>Loads a SMIC instance from a data file (default: {@code data/SMIC/data10_2.txt});</li>
 *   <li>Builds a {@link DdoModel} that defines the problem, relaxation operator, ranking function,
 *       lower bound estimator, and dominance relations between states;</li>
 *   <li>Specifies a frontier management policy ({@link SimpleFrontier}) with a
 *       {@link CutSetType#Frontier} strategy;</li>
 *   <li>Executes the optimization via {@link Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)};</li>
 *   <li>Prints the resulting solution and search statistics.</li>
 * </ul>
 *
 *
 * <p><b>Usage:</b></p>
 * <pre>
 *   java SMICDdoMain [instanceFile]
 * </pre>
 * If no file is provided as an argument, the program defaults to
 * {@code data/SMIC/data10_2.txt}.
 *
 * <p><b>Example:</b></p>
 * <pre>
 *   java SMICDdoMain data/SMIC/data20_3.txt
 * </pre>
 *
 * @see SMICProblem
 * @see SMICState
 * @see SMICRelax
 * @see SMICRanking
 * @see SMICFastLowerBound
 * @see SMICDominance
 * @see SimpleFrontier
 * @see Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)
 */
public class SMICDdoMain {
    /**
     * Entry point of the SMIC solver using the Dynamic Decision Diagram Optimization (DDO) algorithm.
     * Initializes the problem instance, builds the DDO model, and executes the optimization.
     *
     * @param args command-line arguments; the first argument may specify the path
     *             to the SMIC instance file. If omitted, the default instance
     *             {@code data/SMIC/data10_2.txt} is used.
     * @throws IOException if an error occurs while reading the instance file.
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data","SMIC","data10_2.txt").toString() : args[0];
        final SMICProblem problem = new SMICProblem(instance);
        DdoModel<SMICState> model = new DdoModel<>() {
            @Override
            public Problem<SMICState> problem() {
                return problem;
            }

            @Override
            public SMICRelax relaxation() {
                return new SMICRelax(problem);
            }

            @Override
            public SMICRanking ranking() {
                return new SMICRanking();
            }

            @Override
            public SMICFastLowerBound lowerBound() {
                return new SMICFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<SMICState> dominance() {
                return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            }

            @Override
            public Frontier<SMICState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}
