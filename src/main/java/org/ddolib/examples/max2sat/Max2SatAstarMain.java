package org.ddolib.examples.max2sat;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Max2SatAstarMain {
    /**
     * ******* Maximum 2-Satisfiability Problem (MAX2SAT) *******
     */
    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Path.of("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString() : args[0];
        final Max2SatProblem problem = new Max2SatProblem(instance);
        Model<Max2SatState> model = new Model<>() {
            @Override
            public Problem<Max2SatState> problem() {
                return problem;
            }

            @Override
            public Max2SatFastLowerBound lowerBound() {
                return new Max2SatFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAstar(model);

        System.out.println(stats);

    }
}
