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
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solve;

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
public class PSMain2 {

    public static void main(final String[] args) throws IOException {
        PSInstance instance = new PSInstance("data/PSP/instancesWith2items/10");

        DdoModel<PSState> model = new DdoModel<>() {
            private PSProblem problem;
            @Override
            public PSProblem problem() {
                problem = new PSProblem(instance);
                return problem;
            }
            @Override
            public PSRelax relaxation() {
                return new PSRelax(instance);
            }
            @Override
            public PSRanking ranking() {
                return new PSRanking();
            }
            @Override
            public PSFastLowerBound lowerBound() {
                return new PSFastLowerBound(instance);
            }
        };

        Solve<PSState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

        solve.onSolution(stats);

    }
}
