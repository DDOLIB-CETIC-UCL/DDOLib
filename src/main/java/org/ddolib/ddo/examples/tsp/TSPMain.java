package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.util.Arrays;
import java.util.Random;

public final class TSPMain {

    //
    public static TSPProblem genInstance(int n, Random random) {

        int[] x = new int[n];
        int[] y = new int[n];
        for(int i = 0 ; i < n ;  i++){
            x[i] = random.nextInt(100);
            y[i] = random.nextInt(100);
        }

        int[][] distance = new int[n][];
        for(int i = 0 ; i < n ;  i++){
            distance[i] = new int[n];
            for(int j = 0 ; j < n ;  j++){
                distance[i][j] = dist(x[i] - x[j] , y[i]-y[j]);
            }
        }
        return new TSPProblem(distance);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) {

        final TSPProblem problem = genInstance(15, new Random(1));

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());
        Solver s = solveTsp(problem,1);

        int[] solution = extractSolution(s);
        System.out.printf("Objective: %d%n", s.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));

        System.out.println("end");
    }

    public static Solver solveTsp(TSPProblem problem, int verbosityLevel) {

        final TSPRelax relax = new TSPRelax(problem);
        final TSPRanking ranking = new TSPRanking();
        final FixedWidth<TSPState> width = new FixedWidth<>(1000);
        final DefaultVariableHeuristic varh = new DefaultVariableHeuristic();

        final Frontier<TSPState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        SearchStatistics stats = solver.maximize(verbosityLevel);
        System.out.println(stats);

        return solver;
    }

    public static int[] extractSolution(Solver solver){

        return solver.bestSolution()
            .map(decisions -> {
                int[] route = new int[decisions.size()+1];
                route[0] = 0;
                for (Decision d : decisions) {
                    route[d.var() + 1] = d.val();
                }
                return route;
            })
            .get();

}
}



