package org.ddolib.examples.max2sat;

import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.*;

import java.io.IOException;
import java.nio.file.Paths;

import static org.ddolib.examples.max2sat.Max2SatIO.readInstance;

public final class Max2SatAcsMain {

    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.examples.ddo.max2sat.Max2Sat"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="oorg.ddolib.ddo.examples.max2sat.Max2Sat -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        String file = Paths.get("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString();

        AcsModel<Max2SatState> model = new AcsModel<>() {
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

        Solve<Max2SatState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeAcs(model);

        solve.onSolution(stats);

    }
}
