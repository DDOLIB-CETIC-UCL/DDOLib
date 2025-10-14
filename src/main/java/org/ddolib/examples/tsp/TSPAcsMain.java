package org.ddolib.examples.tsp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

public class TSPAcsMain {

    public static void main(final String[] args) throws IOException {

        String file = Paths.get("data", "TSP", "instance_18_0.xml").toString();

        AcsModel<TSPState> model = new AcsModel<TSPState>() {
            private TSPProblem problem;

            @Override
            public Problem<TSPState> problem() {
                try {
                    problem = new TSPProblem(file);
                    return problem;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }
        };

        Solvers<TSPState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeAcs(model);

        System.out.println(stats);

    }


}
