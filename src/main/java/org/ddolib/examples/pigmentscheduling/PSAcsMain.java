package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Solvers;

import java.io.IOException;

/**
 * ################  The Pigment Sequencing Problem (PSP)  ######################
 */
public class PSAcsMain {

    public static void main(final String[] args) throws IOException {
        final PSProblem problem = new PSProblem("data/PSP/instancesWith2items/10");

        AcsModel<PSState> model = new AcsModel<>() {
            @Override
            public PSProblem problem() {
                return problem;
            }

            @Override
            public PSFastLowerBound lowerBound() {
                return new PSFastLowerBound(problem);
            }
        };

        Solvers<PSState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeAcs(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}
