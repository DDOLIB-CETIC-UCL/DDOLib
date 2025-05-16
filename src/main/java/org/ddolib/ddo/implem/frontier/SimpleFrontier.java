package org.ddolib.ddo.implem.frontier;


import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.heuristics.StateRanking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * The simple frontier is a plain priority queue of subproblems which are
 * pushed and popped from by the solver.
 */

/**
 *
 * @param <T> the type of state
 */
public final class SimpleFrontier<T> implements Frontier<T> {
    /**
     * The underlying priority sub problem priority queue
     */
    private final PriorityQueue<SubProblem<T>> heap;

    /**
     * Creates a new instance
     *
     * @param ranking an ordering to tell which of the subproblem is the most promising
     *                and should be explored first.
     */
    public SimpleFrontier(final StateRanking<T> ranking) {
        heap = new PriorityQueue<>(new SubProblemComparator<>(ranking).reversed());
    }

    @Override
    public void push(final SubProblem<T> sub) {
        heap.add(sub);
    }

    @Override
    public SubProblem<T> pop() {
        return heap.poll();
    }

    @Override
    public void clear() {
        heap.clear();
    }

    @Override
    public int size() {
        return heap.size();
    }

    /**
     * This utility class implements a decorator pattern to sort SubProblems by their ub then state
     */
    private static final class SubProblemComparator<T> implements Comparator<SubProblem<T>> {
        /**
         * This is the decorated ranking
         */
        private final StateRanking<T> delegate;

        /**
         * Creates a new instance
         *
         * @param delegate the decorated ranking
         */
        public SubProblemComparator(final StateRanking<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(SubProblem<T> o1, SubProblem<T> o2) {
            int cmp = o1.getUpperBound() - o2.getUpperBound();
            if (cmp == 0) {
                return delegate.compare(o1.getState(), o2.getState());
            } else {
                return cmp;
            }
        }
    }

    @Override
    public String toString() {
        return heap.toString();
    }
}
