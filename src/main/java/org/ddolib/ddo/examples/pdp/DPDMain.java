package org.ddolib.ddo.examples.pdp;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.IOException;
import java.util.*;

public final class DPDMain {


    /**
     * Generates a PDP problem
     * a TSP problem such that
     * nodes are grouped by pair: (pickup node; delivery node)
     * in a pair, the pickup node must be reached before the delivery node
     * the problem can also have "unrelated nodes" that are not involved in such a pair
     *
     * @param n the number of nodes of the PDP problem
     * @param unrelated the number of nodes that are not involved in a pickup-delivery pair.
     *                  there might be one more unrelated node than specified here
     * @return a PDP problem
     */
    public static PDPProblem genInstance(int n, int unrelated, Random random) {

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

        HashMap<Integer,Integer> pickupToAssociatedDelivery = new HashMap<>();

        int firstDelivery = (n-unrelated-1)/2+1; //some  nodes are not pdp nodes
        for(int p = 1; p < firstDelivery ; p ++){
            int d = firstDelivery + p - 1;
            pickupToAssociatedDelivery.put(p,d);
        }

        return new PDPProblem(distance,pickupToAssociatedDelivery);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) throws IOException {

        final PDPProblem problem = genInstance(24,3, new Random(1));

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());

        Solver solver = solveDPD(problem);

        PDPSolution solution = extractSolution(solver,problem);


        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.println("Eval from scratch: " + problem.eval(solution.solution));
        System.out.printf("Solution : %s%n", solution);
        System.out.println("Problem:" + problem);

        System.out.println("end");
    }

    public static Solver solveDPD(PDPProblem problem) {

        final PDPRelax relax = new PDPRelax(problem);
        final PDPRanking ranking = new PDPRanking();
        final FixedWidth<PDPState> width = new FixedWidth<>(2000);
        final DefaultVariableHeuristic varh = new DefaultVariableHeuristic();

        final Frontier<PDPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        SearchStatistics statistics = solver.maximize(2);
        System.out.printf("statistics: " + statistics);

        return solver;
    }

    public static PDPSolution extractSolution(Solver solver, PDPProblem problem){
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

        return new PDPSolution(problem, solution);
    }
}



