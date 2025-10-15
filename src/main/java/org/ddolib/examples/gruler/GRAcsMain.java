package org.ddolib.examples.gruler;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;

/**
 * ########## Golomb Rule Problem (GRP) ################
 */
public class GRAcsMain {

    public static void main(final String[] args) throws IOException {
        GRProblem problem = new GRProblem(7);
        final AcsModel<GRState> model = new AcsModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

            @Override
            public int columnWidth() {
                return 20;
            }
        };

        Solver<GRState> solver = new Solver<>();

        final SearchStatistics stats = solver.minimizeAcs(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}
