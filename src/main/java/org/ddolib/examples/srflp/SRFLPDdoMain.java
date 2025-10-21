package org.ddolib.examples.srflp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;


public final class SRFLPDdoMain {

    public static void main(String[] args) throws IOException {
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() :
                args[0];
        final int maxWidth = args.length > 1 ? Integer.parseInt(args[1]) : 50;

        final SRFLPProblem problem = new SRFLPProblem(filename);

        DdoModel<SRFLPState> model = new DdoModel<>() {
            @Override
            public Problem<SRFLPState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SRFLPState> relaxation() {
                return new SRFLPRelax(problem);
            }

            @Override
            public StateRanking<SRFLPState> ranking() {
                return new SRFLPRanking();
            }

            @Override
            public WidthHeuristic<SRFLPState> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public FastLowerBound<SRFLPState> lowerBound() {
                return new SRFLPFastLowerBound(problem);
            }
        };

        int[] bestSolution = new int[problem.nbVars()];

        SearchStatistics finalStats = Solvers.minimizeDdo(model, (sol, stat) -> {
            SolutionPrinter.printSolution(stat, sol);
            System.arraycopy(sol, 0, bestSolution, 0, sol.length);
        });

        System.out.println("\n");
        System.out.println("===== Optimal Solution =====");
        System.out.println(finalStats);
        System.out.println("Best solution: " + Arrays.toString(bestSolution));
    }
}
