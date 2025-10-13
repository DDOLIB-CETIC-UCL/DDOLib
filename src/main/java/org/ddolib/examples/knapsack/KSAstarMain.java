package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;

/**
 * The Knapsack problem is a classic optimization problem
 * where the goal is to maximize the total value of items
 * while staying within a given weight limit.
 * The dynamic programming model is used to solve this problem is
 * using the recurrence relation:
 * KS(i, c) = max(KS(i-1, c), KS(i-1, c - w[i]) + p[i])
 * where KS(i, c) is the maximum value of the first i items
 * with a knapsack capacity of c, p[i] is the profit of item i,
 * w[i] is the weight of item i.
 */
public class KSAstarMain {
    public static void main(final String[] args) throws IOException {

        final String instance = "data/Knapsack/instance_n1000_c1000_10_5_10_5_0";

        final Model<Integer> model = new Model<>() {

            private KSProblem problem = new KSProblem(instance);

            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }
        };

        Solver<Integer> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAstar(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found "+ s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);

    }

}
