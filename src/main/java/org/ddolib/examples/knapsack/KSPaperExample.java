package org.ddolib.examples.knapsack;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;
import org.ddolib.util.verbosity.VerbosityLevel;

public class KSPaperExample {
    public static void main(String[] args) {
        final KSProblem problem = new KSProblem(12, new int[]{5,6,1,6}, new int[]{6,5,6,6});
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

//            @Override
//            public FastLowerBound<Integer> lowerBound() {
//                return new KSFastLowerBound(problem);
//            }

            @Override
            public boolean exportDot() {
                return true;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.LARGE;
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(stats);

    }
}
