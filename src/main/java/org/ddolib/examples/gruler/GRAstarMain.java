package org.ddolib.examples.gruler;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;

/**
 * ########## Golomb Rule Problem (GRP) ################
 */
public class GRAstarMain {

    public static void main(final String[] args) throws IOException {
        GRProblem problem = new GRProblem(8);
        final Model<GRState> model = new Model<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

        };

        Solvers<GRState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeAstar(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}
