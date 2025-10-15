package org.ddolib.examples.alp;

import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.nio.file.Paths;
/**
 * ######### Aircraft Landing Problem (ALP) #############
 */
public final class ALPDdoMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        final ALPProblem problem = new ALPProblem(fileStr);
        DdoModel<ALPState> model = new DdoModel<>() {
            @Override
            public ALPProblem problem() {
                return problem;
            }

            @Override
            public ALPRelax relaxation() {
                return new ALPRelax(problem);
            }

            @Override
            public ALPRanking ranking() {
                return new ALPRanking();
            }

            @Override
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
            }

            @Override
            public WidthHeuristic widthHeuristic() {
                return new FixedWidth<>(100);
            }
        };

        Solver<ALPState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeDdo(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}