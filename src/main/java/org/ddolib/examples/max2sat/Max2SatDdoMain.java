package org.ddolib.examples.max2sat;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

public final class Max2SatDdoMain {
    /**
     * ******* Maximum 2-Satisfiability Problem (MAX2SAT) *******
     */
    public static void main(String[] args) throws IOException {
        String file = Paths.get("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString();
        final Max2SatProblem problem = new Max2SatProblem(file);
        DdoModel<Max2SatState> model = new DdoModel<>() {
            @Override
            public Problem<Max2SatState> problem() {
                return problem;
            }

            @Override
            public Relaxation<Max2SatState> relaxation() {
                return new Max2SatRelax(problem);
            }

            @Override
            public Max2SatRanking ranking() {
                return new Max2SatRanking();
            }

            @Override
            public Max2SatFastLowerBound lowerBound() {
                return new Max2SatFastLowerBound(problem);
            }
        };

        Solvers<Max2SatState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeDdo(model);

        System.out.println(stats);

    }
}
