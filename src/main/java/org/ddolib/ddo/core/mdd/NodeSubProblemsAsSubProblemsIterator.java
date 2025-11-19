package org.ddolib.ddo.core.mdd;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;

import java.util.Iterator;
import java.util.Set;

/**
 * An iterator that converts inner {@link NodeSubProblem} instances into full {@link SubProblem} objects.
 * <p>
 * This iterator decorates another iterator over {@code NodeSubProblem} objects and, for each element,
 * applies the path of decisions from the root to create a complete {@code SubProblem}.
 * </p>
 *
 * @param <T> the type of state contained in the subproblems
 */
final class NodeSubProblemsAsSubProblemsIterator<T> implements Iterator<SubProblem<T>> {
    /** The underlying iterator over node subproblems. */
    private final Iterator<NodeSubProblem<T>> it;

    /** The set of decisions that form the path from the root to the node. */
    private final Set<Decision> ptr;

    /**
     * Constructs a new iterator that converts {@link NodeSubProblem} instances into {@link SubProblem}.
     *
     * @param it  the iterator over {@code NodeSubProblem} objects
     * @param ptr the path of decisions from the root to each node
     */
    public NodeSubProblemsAsSubProblemsIterator(final Iterator<NodeSubProblem<T>> it, final Set<Decision> ptr) {
        this.it = it;
        this.ptr = ptr;
    }
    /**
     * Returns {@code true} if the underlying iterator has more elements.
     *
     * @return {@code true} if there are more subproblems, {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
        return it.hasNext();
    }
    /**
     * Returns the next {@link SubProblem} by converting the next {@link NodeSubProblem} using the path to root.
     *
     * @return the next full {@code SubProblem}
     */
    @Override
    public SubProblem<T> next() {
        return it.next().toSubProblem(ptr);
    }
}