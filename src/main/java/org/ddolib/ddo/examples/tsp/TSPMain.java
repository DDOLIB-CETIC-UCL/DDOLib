package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.util.*;

public final class TSPMain {

    public static TSPPRoblem genInstance(int n) {

        int[] x = new int[n];
        int[] y = new int[n];
        Random r = new Random(1);
        for(int i = 0 ; i < n ;  i++){
            x[i] = r.nextInt(100);
            y[i] = r.nextInt(100);
        }

        int[][] distance = new int[n][];
        for(int i = 0 ; i < n ;  i++){
            distance[i] = new int[n];
            for(int j = 0 ; j < n ;  j++){
                distance[i][j] = dist(x[i] - x[j] , y[i]-y[j]);
            }
        }
        return new TSPPRoblem(distance);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) {

        final TSPPRoblem problem = genInstance(15);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());
        solveTsp(problem);
        System.out.println("end");
    }

    public static void solveTsp(TSPPRoblem problem){

        final TSPRelax                    relax = new TSPRelax(problem);
        final TSPRanking                ranking = new TSPRanking();
        final FixedWidth<TSPState> width = new FixedWidth<>(500);
        final DefaultVariableHeuristic varh = new DefaultVariableHeuristic();

        final Frontier<TSPState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new ParallelSolver<>(
                Runtime.getRuntime().availableProcessors()/2,
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize(2);
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] route = new int[problem.nbVars()+1];
                    route[0] = 0;
                    for (Decision d : decisions) {
                        route[d.var()+1] = d.val();
                    }
                    return route;
                })
                .get();


        System.out.printf("Duration : %.3f%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}



