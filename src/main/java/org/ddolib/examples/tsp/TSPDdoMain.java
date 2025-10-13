package org.ddolib.examples.tsp;

import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solver;

import java.io.IOException;

public class TSPDdoMain {

    public static void main(final String[] args) throws IOException {

        //TSPInstance instance = new TSPInstance("data/TSP/gr21.xml");
        TSPInstance instance = new TSPInstance("data/TSP/instance_18_0.xml");

        DdoModel<TSPState> model = new DdoModel<TSPState>() {
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
            public Relaxation<TSPState> relaxation() {
                return new TSPRelax(problem);
            }

            @Override
            public TSPRanking ranking() {
                return new TSPRanking();
            }

            @Override
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public WidthHeuristic<TSPState> widthHeuristic() {
                return new FixedWidth<>(500);
            }
        };

        Solver<TSPState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeDdo(model);

        System.out.println(stats);

    }


}
