package org.ddolib.examples.max2sat;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solve;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.ddolib.examples.max2sat.Max2SatIO.readInstance;

public final class Max2Sat2 {

    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.examples.ddo.max2sat.Max2Sat"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="oorg.ddolib.ddo.examples.max2sat.Max2Sat -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        String file = Paths.get("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString();

        DdoModel<Max2SatState> ddoModel = new DdoModel<>() {
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
            public Relaxation<Max2SatState> relaxation() {
                return new Max2SatRelax(problem);
            }

            @Override
            public Max2SatRanking ranking() {
                return new Max2SatRanking();
            }
        };

        Solve<Max2SatState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(ddoModel);

        solve.onSolution(stats);

    }
}
