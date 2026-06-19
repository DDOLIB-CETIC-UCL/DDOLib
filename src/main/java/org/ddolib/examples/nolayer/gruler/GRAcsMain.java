package org.ddolib.examples.nolayer.gruler;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Solvers;
import org.ddolib.modeling.nolayer.NoLayerAcsModel;
import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerProblem;
import org.ddolib.util.io.SolutionPrinter;

public final class GRAcsMain {
    public static void main(String[] args) {
        final int n = args.length == 0 ? 10 : Integer.parseInt(args[0]);
        final GRProblem problem = new GRProblem(n);
        final GRModel baseModel = new GRModel(problem);

        final NoLayerAcsModel<GRState> model = new NoLayerAcsModel<>() {
            @Override
            public NoLayerProblem<GRState> problem() {
                return problem;
            }

            @Override
            public NoLayerFastLowerBound<GRState> lowerBound() {
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

        Solution bestSolution = Solvers.minimizeNoLayerAcs(model,
                stats -> false,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal GR value: " + bestSolution.value());
    }
}
