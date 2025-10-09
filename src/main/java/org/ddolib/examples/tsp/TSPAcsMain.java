package org.ddolib.examples.tsp;

import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.*;

import java.io.IOException;

public class TSPAcsMain {

    public static void main(final String[] args) throws IOException {

        //TSPInstance instance = new TSPInstance("data/TSP/gr21.xml");
        TSPInstance instance = new TSPInstance("data/TSP/instance_18_0.xml");

        AcsModel<TSPState> model = new AcsModel<TSPState>() {
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

        Solve<TSPState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeAcs(model);

        solve.onSolution(stats);



        //data/TSP/instance_18_0.xml
        //sans cache
        //SearchStatistics{nbIterations=35118, queueMaxSize=34377, runTimeMS=36805, SearchStatus=OPTIMAL, Gap=-0.0}
        //avec cache
        //SearchStatistics{nbIterations=35278, queueMaxSize=34807, runTimeMS=46383, SearchStatus=OPTIMAL, Gap=-0.0}
        //après correction de la hash
        //SearchStatistics{nbIterations=8338, queueMaxSize=7680, runTimeMS=13543, SearchStatus=OPTIMAL, Gap=-0.0, cacheStats=stats(nbHits: 94100, nbTests: 12887939, size:9638)}


    }


}
