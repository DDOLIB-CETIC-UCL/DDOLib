package org.ddolib.examples.msct;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.File;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;

/**
 * ################ Minimum Sum Completion Time (MSCT) #####################
 */
public class MSCTAstarMain {

    public static void main(final String[] args) throws Exception {
        final String instance = args.length == 0 ? Path.of("data","MSCT","msct1.txt").toString() : args[0];
        final MSCTProblem problem = new MSCTProblem(instance);
        Model<MSCTState> model = new Model<>() {
            @Override
            public Problem<MSCTState> problem() {
                return problem;
            }
            @Override
            public FastLowerBound<MSCTState> lowerBound() {
                return new MSCTFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<MSCTState> dominance() {
                return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
            }
        };

        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}


