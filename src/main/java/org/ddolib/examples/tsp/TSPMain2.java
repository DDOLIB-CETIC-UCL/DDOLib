package org.ddolib.examples.tsp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solve;

import java.io.IOException;
import java.util.Arrays;

public class TSPMain2 {

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

        Solve<TSPState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

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
