package org.ddolib.examples.srflp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class SRFLPAcsMain {
    public static void main(String[] args) throws IOException {
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() :
                args[0];

        final SRFLPProblem problem = new SRFLPProblem(filename);

        AcsModel<SRFLPState> model = new AcsModel<>() {
            @Override
            public Problem<SRFLPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<SRFLPState> lowerBound() {
                return new SRFLPFastLowerBound(problem);
            }

            @Override
            public int columnWidth() {
                return 50;
            }
        };

        int[] bestSolution = new int[problem.nbVars()];

        SearchStatistics finalStats = Solvers.minimizeAstar(model, (sol, stat) -> {
            SolutionPrinter.printSolution(stat, sol);
            System.arraycopy(sol, 0, bestSolution, 0, sol.length);
        });

        System.out.println("\n");
        System.out.println("===== Optimal Solution =====");
        System.out.println(finalStats);
        System.out.println("Best solution: " + Arrays.toString(bestSolution));
    }
}
