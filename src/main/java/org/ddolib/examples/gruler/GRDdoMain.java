package org.ddolib.examples.gruler;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;

/**
 * Golomb Rule Problem (GRP) with Ddo.
 */
public class GRDdoMain {

    public static void main(final String[] args) throws IOException {
        GRProblem problem = new GRProblem(9);
        final DdoModel<GRState> model = new DdoModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

            @Override
            public Relaxation<GRState> relaxation() {
                return new GRRelax();
            }

            @Override
            public StateRanking<GRState> ranking() {
                return new GRRanking();
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}
