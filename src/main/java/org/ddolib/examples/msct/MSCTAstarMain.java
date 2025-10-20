package org.ddolib.examples.msct;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.File;
import java.util.Random;
import java.util.Scanner;

/**
 * ################ Minimum Sum Completion Time (MSCT) #####################
 */
public class MSCTAstarMain {

    public static void main(final String[] args) throws Exception {
        final String file = "data/MSCT/msct1.txt";
        final MSCTProblem problem = new MSCTProblem(file);
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

        SearchStatistics stats = Solvers.minimizeAstar(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found "+ s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}


