package org.ddolib.ddo.examples.pigmentscheduling;

import org.ddolib.ddo.algo.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.algo.heuristics.FixedWidth;
import org.ddolib.ddo.algo.heuristics.VariableHeuristic;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.profiling.SearchStatistics;

import java.io.IOException;
import java.util.Arrays;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

/**
 * The Pigment Sequencing Problem (PSP) is a single-machine production planning problem
 * that aims to minimize the stocking and changeover costs while satisfying a set of orders.
 * There are different item types I = {0,...,𝑛−1}.
 * For each type, a given stocking cost S_i to pay for each time period
 * between the production and the deadline of an order.
 * For each pair i,j in I of item types, a changeover cost C_ij is incurred
 * whenever the machine switches the production from item type i to j.
 * Finally, the demand matrix Q contains all the orders: Q_i^p in {0,1}
 * indicates whether there is an order for item type i in I at time period p.
 * 0 ≤ p &lt; H where H is the time horizon.
 */
public class PSMain {

    public static void main(final String[] args) throws IOException {
        PSInstance instance = new PSInstance("data/PSP/instancesWith2items/10");
        ;
        PSProblem problem = new PSProblem(instance);
        final PSRelax relax = new PSRelax(instance);
        final PSRanking ranking = new PSRanking();
        final FixedWidth<PSState> width = new FixedWidth<>(10);
        final VariableHeuristic<PSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<PSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        int t = (instance.horizon - d.var() - 1);
                        values[t] = d.val();
                    }
                    return values;
                })
                .get();

        System.out.printf("Duration : %.3f%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
        System.out.println(stats);

    }
}
