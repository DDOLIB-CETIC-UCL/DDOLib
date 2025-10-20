package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
/**
 * ################  The Pigment Sequencing Problem (PSP)  ######################
 */
public class PSDdoMain {

    public static void main(final String[] args) throws IOException {
        final PSProblem problem = new PSProblem("data/PSP/instancesWith2items/10");

        DdoModel<PSState> model = new DdoModel<>() {
            @Override
            public PSProblem problem() {
                return problem;
            }

            @Override
            public PSRelax relaxation() {
                return new PSRelax(problem);
            }

            @Override
            public PSRanking ranking() {
                return new PSRanking();
            }

            @Override
            public PSFastLowerBound lowerBound() {
                return new PSFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);

    }
}
