package org.ddolib.examples.max2sat;

import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.nio.file.Paths;

import static org.ddolib.examples.max2sat.Max2SatIO.readInstance;

public final class Max2SatAstarMain {
    /**
     * ******* Maximum 2-Satisfiability Problem (MAX2SAT) *******
     * Given a logic formula in CNF whose clauses (of the formula comprises at most two literals)
     * have each been assigned a weight, the MAX2SAT problem consists in finding a variable assignment
     * that maximizes the total weight of the satisfied clauses.
     * This problem is considered in the paper:
     *      - David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and Michael Wooldridge. Springer, 2016.
     *      - David Bergman et al. “Discrete Optimization with Decision Diagrams”. In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.
     */

    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.examples.ddo.max2sat.Max2Sat"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="oorg.ddolib.ddo.examples.max2sat.Max2Sat -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        String file = Paths.get("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString();

        Model<Max2SatState> ddoModel = new Model<>() {
            private Max2SatProblem problem;

            @Override
            public Problem<Max2SatState> problem() {
                try {
                    problem = readInstance(file);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Max2SatFastLowerBound lowerBound() {
                return new Max2SatFastLowerBound(problem);
            }
        };

        Solver<Max2SatState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAstar(ddoModel);

        System.out.println(stats);

    }
}
