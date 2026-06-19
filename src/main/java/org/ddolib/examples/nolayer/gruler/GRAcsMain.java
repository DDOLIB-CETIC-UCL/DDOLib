package org.ddolib.examples.nolayer.gruler;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.nolayer.Solvers;
import org.ddolib.modeling.nolayer.AcsModel;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Problem;
import org.ddolib.util.io.SolutionPrinter;

public final class GRAcsMain {
    public static void main(String[] args) {
        final int n = args.length == 0 ? 10 : Integer.parseInt(args[0]);
        final GRProblem problem = new GRProblem(n);
        final GRModel baseModel = new GRModel(problem);

        final AcsModel<GRState> model = new AcsModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<GRState> lowerBound() {
                return baseModel.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<GRState> dominance() {
                return baseModel.dominance();
            }

            @Override
            public int columnWidth() {
                return 10;
            }
        };

        Solution bestSolution = org.ddolib.modeling.nolayer.Solvers.minimizeAcs(model,
                stats -> false,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal GR value: " + bestSolution.value());
    }
}
