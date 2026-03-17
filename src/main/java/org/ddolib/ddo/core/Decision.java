package org.ddolib.ddo.core;

/**
 * Represents a single decision within an optimization problem.
 * <p>
 * A {@code Decision} associates a variable identifier with a specific value.
 * It is an immutable data structure that captures a single assignment performed
 * during the search or compilation of a decision diagram (e.g., in an MDD-based solver).
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * Decision d = new Decision(2, 5);
 * System.out.println(d); // prints: Decision[variable=4, value=2]
 *
 * }</pre>
 *
 * <p><b>Use in DD-based solvers:</b></p>
 * <ul>
 *   <li>Encapsulates the assignment of a value to a variable during branching.</li>
 *   <li>Used to reconstruct solutions from paths in the decision diagram.</li>
 *   <li>Acts as a key element in comparing or storing decision paths in sets and maps.</li>
 * </ul>
 *
 */
public record Decision(int variable, int value) {
}
