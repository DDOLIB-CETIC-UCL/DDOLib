package org.ddolib.examples.pdp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.util.Random;
/**
 * ############# Single Vehicle Pick-up and Delivery Problem (PDP)
 */
public final class PDPAcsMain {


    public static void main(final String[] args) throws IOException {

        final PDPProblem problem = PDPGenerator.genInstance(18, 2, 3, new Random(1));
        AcsModel<PDPState> model = new AcsModel<>() {

            @Override
            public Problem<PDPState> problem() {
                return problem;
            }

            @Override
            public PDPFastLowerBound lowerBound() {
                return new PDPFastLowerBound(problem);
            }

            @Override
            public int columnWidth() {
                return 30;
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);
    }

}
