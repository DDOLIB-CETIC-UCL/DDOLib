/**
 * This package contains solvers using the DDO (Branch-and-Bound with decision diagrams) algorithm
 * for the layered API: {@link org.ddolib.layered.solving.ddo.core.solver.SequentialSolver} (full
 * DDO with relaxed and restricted MDDs), {@link org.ddolib.layered.solving.ddo.core.solver.ExactSolver}
 * (exact MDD only), and {@link org.ddolib.layered.solving.ddo.core.solver.RelaxationSolver} /
 * {@link org.ddolib.layered.solving.ddo.core.solver.RestrictionSolver} (a single relaxed/restricted
 * MDD, mainly used internally and for testing bounds).
 */
package org.ddolib.layered.solving.ddo.core.solver;
