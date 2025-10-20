package org.ddolib.examples.msct;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Path;

/**
 * ################ Minimum Sum Completion Time (MSCT) #####################
 */
public class MSCTAcsMain {

    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data","MSCT","msct1.txt").toString() : args[0];
        final MSCTProblem problem = new MSCTProblem(instance);
        AcsModel<MSCTState> model = new AcsModel<>() {
            @Override
            public Problem<MSCTState> problem() {
                return problem;
            }

            @Override
            public DominanceChecker<MSCTState> dominance() {
                return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
            }

            @Override
            public FastLowerBound<MSCTState> lowerBound() {
                return new MSCTFastLowerBound(problem);
            }
        };

        final SearchStatistics stats = Solvers.minimizeAcs(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}


