package org.ddolib.examples.ddo.tsp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolverWithCache;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.util.Arrays;

public class TSPMain {

    public static void main(final String[] args) throws IOException {

        //TSPInstance instance = new TSPInstance("data/TSP/gr21.xml");
        TSPInstance instance = new TSPInstance("data/TSP/instance_18_0.xml");

        //data/TSP/instance_18_0.xml
        //sans cache
        //SearchStatistics{nbIterations=35118, queueMaxSize=34377, runTimeMS=36805, SearchStatus=OPTIMAL, Gap=-0.0}
        //avec cache
        //SearchStatistics{nbIterations=35278, queueMaxSize=34807, runTimeMS=46383, SearchStatus=OPTIMAL, Gap=-0.0}
        //après correction de la hash
        //SearchStatistics{nbIterations=8338, queueMaxSize=7680, runTimeMS=13543, SearchStatus=OPTIMAL, Gap=-0.0, cacheStats=stats(nbHits: 94100, nbTests: 12887939, size:9638)}

        Solver solver = solveTSP(instance);


        TSPProblem problem = new TSPProblem(instance.distanceMatrix);
        int[] solution = extractSolution(problem, solver);
        System.out.printf("Objective: %.1f%n", solver.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
        if (instance.objective >= 0) {
            System.out.println("real best: " + instance.objective);
        }
    }

    public static int[] extractSolution(TSPProblem problem, Solver solver) {
        return solver.bestSolution()
                .map(decisions -> {
                    int[] route = new int[problem.nbVars() + 1];
                    route[0] = 0;
                    for (Decision d : decisions) {
                        route[d.var() + 1] = d.val();
                    }
                    return route;
                })
                .get();
    }

    public static Solver solveTSP(TSPInstance instance) {
        SolverConfig<TSPState, NullType> config = new SolverConfig<>();

        final TSPProblem problem = new TSPProblem(instance.distanceMatrix);
        config.problem = problem;
        config.relax = new TSPRelax(problem);
        config.ranking = new TSPRanking();
        config.fub = new TSPFastUpperBound(problem);
        config.width = new FixedWidth<>(500);
        config.varh = new DefaultVariableHeuristic<>();
        config.cache = new SimpleCache<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        final Solver solver = new SequentialSolverWithCache<>(config);

        SearchStatistics stats = solver.maximize(2, false);
        System.out.println(stats);
        return solver;
    }
}
