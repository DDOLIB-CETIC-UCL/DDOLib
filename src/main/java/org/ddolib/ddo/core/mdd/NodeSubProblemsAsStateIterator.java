package org.ddolib.ddo.core.mdd;

import java.util.Iterator;

/**
 * An iterator that transforms the inner subroblems into their representing states
 */
final class NodeSubProblemsAsStateIterator<T> implements Iterator<T> {
    /**
     * The collection being iterated upon
     */
    private final Iterator<NodeSubProblem<T>> it;

    /**
     * Creates a new instance
     *
     * @param it the decorated iterator
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