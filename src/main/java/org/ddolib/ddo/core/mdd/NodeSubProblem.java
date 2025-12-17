package org.ddolib.ddo.core.mdd;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import static org.ddolib.util.MathUtil.saturatedAdd;

/**
 * Encapsulates the association of a node in a decision diagram with its corresponding state
 * and an associated rough lower bound.
 * <p>
 * This class serves two main purposes:
 * <ul>
 *     <li>Associates a node with a state during decision diagram compilation, allowing the state
 *         to be discarded afterward to save memory.</li>
 *     <li>Converts an exact MDD node into a {@link SubProblem}, which can then be used in the API
 *         for search or optimization.</li>
 * </ul>
 *
 * @param <T> the type of state associated with the node
 */
public final class NodeSubProblem<T> {
    /** The state associated with this node. */
    public final T state;

    /** The actual node from the decision diagram graph. */
    public final Node node;

    /** The lower bound associated with this node (root to terminal node) */
    public double lb;

    /** The fast lower bound of this node (this node to terminal node) */
    public double flb;

    /**
     * Creates a new NodeSubProblem associating a state with a node and a lower bound.
     *
     * @param state the state associated with the node
     * @param lb the rough lower bound associated with the state-node pair (g cost + fast lower bound)
     * @param node  the node in the decision diagram
     */
    public NodeSubProblem(final T state, final double lb, final Node node) {
        this.state = state;
        this.lb = lb;
        this.node = node;
    }

    /**
     * Converts this node-state association into an actual {@link SubProblem}.
     * <p>
     * The resulting {@link SubProblem} incorporates the path of decisions from the root to this node,
     * updates the lower bound based on the node's value and suffix, and can be used directly
     * in search or optimization routines.
     * </p>
     *
     * @param pathToRoot the set of decisions forming the path from the root to this node
     * @return a {@link SubProblem} representing this node-state association
     */
    public SubProblem<T> toSubProblem(final Set<Decision> pathToRoot) {
        HashSet<Decision> path = new HashSet<>();
        path.addAll(pathToRoot);

        Edge e = node.best;
        while (e != null) {
            path.add(e.decision);
            e = e.origin == null ? null : e.origin.best;
        }

        double locb = Double.POSITIVE_INFINITY;
        if (node.suffix != null) {
            locb = saturatedAdd(node.value, node.suffix);
        }
        lb = Math.max(lb, locb);

        return new SubProblem<>(state, node.value, lb, path);
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##########");
        return String.format("%s - lb: %s - value: %s", state, df.format(lb), df.format(node.value));
    }

    public double getValue() {
        return node.value;
    }

}