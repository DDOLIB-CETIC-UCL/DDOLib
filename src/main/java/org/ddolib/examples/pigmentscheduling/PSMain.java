package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.util.Arrays;

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

        SolverConfig<PSState, NullType> config = new SolverConfig<>();
        PSProblem problem = new PSProblem(instance);
        config.problem = problem;
        config.relax = new PSRelax(instance);
        config.ranking = new PSRanking();
        config.flb = new PSFastLowerBound(instance);
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        final Solver solver = new SequentialSolver<>(config);

        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.minimize();
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
