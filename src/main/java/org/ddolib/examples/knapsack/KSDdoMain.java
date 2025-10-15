package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
/**
 * ######### Knapsack Problem (KS) #############
 */
public class KSDdoMain {
    public static void main(final String[] args) throws IOException {

        final String instance = "data/Knapsack/instance_n1000_c1000_10_5_10_5_0";
        final KSProblem problem = new KSProblem(instance);
        final DdoModel<Integer> model = new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
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
            public Frontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.LARGE;
            }
        };

        Solver<Integer> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeDdo(model, s -> s.runTimeMs() >= 10000, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);


    }
}
