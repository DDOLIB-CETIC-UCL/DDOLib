package org.ddolib.examples.alp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

public final class ALPDdoMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        DdoModel<ALPState> model = new DdoModel<>() {
            private ALPProblem problem;

            @Override
            public ALPProblem problem() {
                try {
                    ALPInstance instance = new ALPInstance(fileStr);
                    problem = new ALPProblem(instance);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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

        Solvers<ALPState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeDdo(model);

        System.out.println(stats);
    }
}