package org.ddolib.examples.tsp;

import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;

public class TSPAstarMain {

    public static void main(final String[] args) throws IOException {

        //TSPInstance instance = new TSPInstance("data/TSP/gr21.xml");
        TSPInstance instance = new TSPInstance("data/TSP/instance_18_0.xml");

        Model<TSPState> model = new Model<TSPState>() {
            private TSPProblem problem;

            @Override
            public Problem<TSPState> problem() {
                try {
                    problem = new TSPProblem(instance.distanceMatrix);
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
