package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solver;

import java.io.IOException;
/**
 * ################  The Pigment Sequencing Problem (PSP)  ######################
 */
public class PSAstarMain {

    public static void main(final String[] args) throws IOException {
        final PSProblem problem = new PSProblem("data/PSP/instancesWith2items/10");

        Model<PSState> model = new Model<>() {
            @Override
            public PSProblem problem() {
                return problem;
            }

            @Override
            public PSFastLowerBound lowerBound() {
                return new PSFastLowerBound(problem);
            }
        };

        Solver<PSState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAstar(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}
