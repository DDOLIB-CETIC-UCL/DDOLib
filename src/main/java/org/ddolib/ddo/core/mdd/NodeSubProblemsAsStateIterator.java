package org.ddolib.ddo.core.mdd;

import java.util.Iterator;

/**
 * An iterator that transforms the inner subproblems into their representing states.
 * <p>
 * This iterator allows for iterating over subproblems and extracting the states that
 * they represent, simplifying access to the actual state without needing to deal
 * with the subproblem details directly.
 * </p>
 *
 * @param <T> the type of state
 */
final class NodeSubProblemsAsStateIterator<T> implements Iterator<T> {
    /**
     * The collection of {@link NodeSubProblem} instances being iterated upon.
     */
    private final Iterator<NodeSubProblem<T>> it;

    /**
     * Creates a new instance of the iterator.
     *
     * @param it the decorated iterator over {@link NodeSubProblem} instances
     */
    public NodeSubProblemsAsStateIterator(final Iterator<NodeSubProblem<T>> it) {
        this.it = it;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public T next() {
        return it.next().state;
    }
}