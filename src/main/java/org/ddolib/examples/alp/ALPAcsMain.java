package org.ddolib.examples.alp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.nio.file.Paths;

public final class ALPAcsMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        AcsModel<ALPState> model = new AcsModel<>() {
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

        Solver<ALPState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAcs(model, s -> s.nbIterations() > 1000000);

        System.out.println(stats);
    }
}