package org.ddolib.ddo.core.mdd;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;

import java.util.Iterator;
import java.util.Set;

/**
 * An iterator that transforms the inner subroblems into actual subroblems
 */
final class NodeSubProblemsAsSubProblemsIterator<T> implements Iterator<SubProblem<T>> {
    /**
     * The collection being iterated upon
     */
    private final Iterator<NodeSubProblem<T>> it;
    /**
     * The list of decisions constitutive of the path to root
     */
    private final Set<Decision> ptr;

    /**
     * Creates a new instance
     *
     * @param it  the decorated iterator
     * @param ptr the path to root
     */
    public NodeSubProblemsAsSubProblemsIterator(final Iterator<NodeSubProblem<T>> it, final Set<Decision> ptr) {
        this.it = it;
        this.ptr = ptr;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public SubProblem<T> next() {
        return it.next().toSubProblem(ptr);
    }
}