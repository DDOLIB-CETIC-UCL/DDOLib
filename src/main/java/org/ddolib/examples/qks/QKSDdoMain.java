package org.ddolib.examples.qks;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.knapsack.KSDominance;
import org.ddolib.examples.knapsack.KSFastLowerBound;
import org.ddolib.examples.knapsack.KSRanking;
import org.ddolib.examples.knapsack.KSRelax;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class QKSDdoMain {

    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("src", "test", "resources", "qks", "qks_3_0_2.txt").toString() : args[0];
        final QKSProblem problem = new QKSProblem(instance);
        final DdoModel<QKSState> model = new DdoModel<>() {
            @Override
            public Problem<QKSState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<QKSState> lowerBound() {
                return new DefaultFastLowerBound<>();
            }

            @Override
            public DominanceChecker<QKSState> dominance() {
                return new DefaultDominanceChecker<>();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.LARGE;
            }

            @Override
            public Relaxation<QKSState> relaxation() {
                return new QKSRelax();
            }

            @Override
            public QKSRanking ranking() {
                return new QKSRanking();
            }

            @Override
            public WidthHeuristic<QKSState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public Frontier<QKSState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        Solution bestSolution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);

    }

    /**
     * Find the optimal solution by brute forcing the problem.
     *
     * @return the cost of the optimal solution
     */
    private static double bruteForce(QKSProblem problem) {
        int[] solution = new int[problem.profitMatrix.length];
        return recursion(solution, 0, problem.capacity, problem);
    }

    private static double recursion(int[] solution, int index, int capacity, QKSProblem problem) {
        if (index == solution.length) {
            double cost = 0;
            for (int i = 0; i < solution.length; i++) {
                for (int j = 0; j < solution.length; j++) {
                    cost += problem.profitMatrix[i][j] * solution[i] * solution[j];
                }
            }
            System.out.println(Arrays.toString(solution));
            System.out.println(cost);
            try {
                System.out.println(problem.evaluate(solution));
            } catch (InvalidSolutionException e) {
                System.err.println(e.getMessage());
            }
            return cost;
        }
        if (capacity >= problem.weights[index]) {
            int[] solutionTakingObj = solution.clone();
            solutionTakingObj[index] = 1;
            return Math.max(recursion(solution, index + 1, capacity, problem),
                    recursion(solutionTakingObj, index + 1, capacity - problem.weights[index],  problem));
        } else {
            return recursion(solution, index + 1, capacity, problem);
        }
    }

}
