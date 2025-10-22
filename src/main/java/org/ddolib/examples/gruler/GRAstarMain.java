package org.ddolib.examples.gruler;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;

/**
 * Golomb Rule Problem (GRP) with AsTar.
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

        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}
