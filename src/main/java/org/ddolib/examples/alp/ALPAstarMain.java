package org.ddolib.examples.alp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.nio.file.Paths;
/**
 * ######### Aircraft Landing Problem (ALP) #############
 */
public final class ALPAstarMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        final ALPProblem problem = new ALPProblem(fileStr);
        Model<ALPState> model = new Model<>() {
            @Override
            public ALPProblem problem() {
                return problem;
            }

            @Override
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
            }

        };

        Solver<ALPState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAstar(model, s-> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);


    }
}