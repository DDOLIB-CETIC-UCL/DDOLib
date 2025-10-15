package org.ddolib.examples.tsp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.nio.file.Paths;

public class TSPAstarMain {

    public static void main(final String[] args) throws IOException {

        String file = Paths.get("data", "TSP", "instance_18_0.xml").toString();

        Model<TSPState> model = new Model<TSPState>() {
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

        Solver<TSPState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAstar(model);

        System.out.println(stats);

    }


}
