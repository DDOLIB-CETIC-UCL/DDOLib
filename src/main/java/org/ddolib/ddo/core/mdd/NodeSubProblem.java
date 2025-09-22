package org.ddolib.ddo.core.mdd;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import static org.ddolib.util.MathUtil.saturatedAdd;

/**
 * This class encapsulates the association of a node with its state and
 * associated rough upper bound.
 * <p>
 * This class essentially serves two purposes:
 * <p>
 * - associate a node with a state during the compilation (and allow to
 * eagerly forget about the given state, which allows to save substantial
 * amounts of RAM while compiling the DD).
 * <p>
 * - turn an MDD node from the exact cutset into a subproblem which is used
 * by the API.
 */
final class NodeSubProblem<T> {
    /**
     * The state associated to this node
     */
    public final T state;
    /**
     * The actual node from the graph of decision diagrams
     */
    public final Node node;
    /**
     * The upper bound associated with this node (if state were the root)
     */
    public double lb;

    /**
     * Creates a new instance
     */
    public NodeSubProblem(final T state, final double lb, final Node node) {
        this.state = state;
        this.lb = lb;
        this.node = node;
    }

    /**
     * @return Turns this association into an actual subproblem
     */
    public SubProblem<T> toSubProblem(final Set<Decision> pathToRoot) {
        HashSet<Decision> path = new HashSet<>();
        path.addAll(pathToRoot);

        Edge e = node.best;
        while (e != null) {
            path.add(e.decision);
            e = e.origin == null ? null : e.origin.best;
        }

        double locb = Double.NEGATIVE_INFINITY;
        if (node.suffix != null) {
            locb = saturatedAdd(node.value, node.suffix);
        }
        lb = Math.min(lb, locb);

        return new SubProblem<>(state, node.value, lb, path);
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##########");
        return String.format("%s - ub: %s - value: %s", state, df.format(lb), df.format(node.value));
    }
}