package org.ddolib.examples.pdp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.util.Random;

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

        Solvers<PDPState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeAcs(model, s -> s.runTimeMs() > 1000);

        System.out.println(stats);
    }

}
