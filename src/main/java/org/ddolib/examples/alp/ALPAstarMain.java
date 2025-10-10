package org.ddolib.examples.alp;

import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solve;

import java.io.IOException;
import java.nio.file.Paths;

public final class ALPAstarMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        Model<ALPState> model = new Model<>() {
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
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
            }

        };

        Solve<ALPState> solve = new Solve<>();

        SearchStatistics stats = solve.minimize(model);

        solve.onSolution(stats);



    }
}