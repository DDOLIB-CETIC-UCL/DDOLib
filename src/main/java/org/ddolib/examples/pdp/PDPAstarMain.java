package org.ddolib.examples.pdp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.util.Random;

import static org.ddolib.examples.pdp.PDPGenerator.genInstance;
/**
 * ############# Single Vehicle Pick-up and Delivery Problem (PDP)
 */
public final class PDPAstarMain {


    public static void main(final String[] args) throws IOException {
        final PDPProblem problem = genInstance(18, 2, 3, new Random(1));
        Model<PDPState> model = new Model<>() {
            @Override
            public Problem<PDPState> problem() {
                return problem;
            }

            @Override
            public PDPFastLowerBound lowerBound() {
                return new PDPFastLowerBound(problem);
            }
        };

        Solvers<PDPState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeAstar(model);

        System.out.println(stats);
    }

}
