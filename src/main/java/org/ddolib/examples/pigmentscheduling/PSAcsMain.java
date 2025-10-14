package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Solver;

import java.io.IOException;

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
public class PSAcsMain {

    public static void main(final String[] args) throws IOException {
        PSInstance instance = new PSInstance("data/PSP/instancesWith2items/10");

        AcsModel<PSState> model = new AcsModel<>() {
            private PSProblem problem;

            @Override
            public PSProblem problem() {
                problem = new PSProblem(instance);
                return problem;
            }

            @Override
            public PSFastLowerBound lowerBound() {
                return new PSFastLowerBound(instance);
            }
        };

        Solver<PSState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAcs(model);

        System.out.println(stats);

    }
}
