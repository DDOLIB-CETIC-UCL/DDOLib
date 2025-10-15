package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;
/**
 * ######### Knapsack Problem (KS) #############
 */
public class KSAcsMain {
    public static void main(final String[] args) throws IOException {
        final String instance = "data/Knapsack/instance_n1000_c1000_10_5_10_5_0";
        final KSProblem problem = new KSProblem(instance);
        final AcsModel<Integer> model = new AcsModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public int columnWidth() {
                return 10;
            }

        };

        Solver<Integer> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAcs(model,
                // stop afer 10 iterations
                s -> s.nbIterations() > 10,
                (sol,s) -> {
                    System.out.println("------");
                    System.out.println("new incumbent :" + s.incumbent() + " found  at iteration " + s.nbIterations());
                    System.out.println("New solution: " + sol);
        });

        System.out.println(stats);

    }
}
