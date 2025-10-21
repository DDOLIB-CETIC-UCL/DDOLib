package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * ################  The Pigment Sequencing Problem (PSP)  ######################
 */
public class PSAstarMain {

    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data","PSP","instancesWith2items","10").toString() : args[0];
        final PSProblem problem = new PSProblem(instance);
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

        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}
