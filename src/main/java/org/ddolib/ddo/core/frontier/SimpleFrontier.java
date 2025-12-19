package org.ddolib.ddo.core.frontier;


import org.ddolib.ddo.core.SubProblem;
import org.ddolib.modeling.StateRanking;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * A simple implementation of a {@link Frontier} for a solver, based on a plain priority queue.
 * <p>
 * The {@code SimpleFrontier} maintains a collection of {@link SubProblem} instances, which are pushed
 * and popped by the solver according to their priority determined by a {@link StateRanking}.
 * This frontier supports cutset-based compilation strategies.
 * </p>
 *
 * @param <T> the type of state in the subproblems
 */
public final class SimpleFrontier<T> implements Frontier<T> {
    /** The underlying priority queue storing the subproblems. */
    private final PriorityQueue<SubProblem<T>> heap;

    /** The type of cutset used in the decision diagram compilation. */
    private final CutSetType cutSetType;

    /**
     * Constructs a new {@code SimpleFrontier}.
     *
     * @param ranking    the ordering used to determine which subproblem is most promising
     *                   and should be explored first
     * @param cutSetType the type of cutset to use: {@link CutSetType#LastExactLayer} or {@link CutSetType#Frontier}
     */
    public SimpleFrontier(final StateRanking<T> ranking, final CutSetType cutSetType) {
        heap = new PriorityQueue<>(new SubProblemComparator<>(ranking));
        this.cutSetType = cutSetType;
    }
    /**
     * Adds a subproblem to the frontier.
     *
     * @param sub the subproblem to add
     */
    @Override
    public void push(final SubProblem<T> sub) {
        heap.add(sub);
    }
    /**
     * Removes and returns the most promising subproblem from the frontier.
     *
     * @return the subproblem with highest priority, or {@code null} if the frontier is empty
     */
    @Override
    public SubProblem<T> pop() {
        return heap.poll();
    }
    /**
     * Clears all subproblems from the frontier.
     */
    @Override
    public void clear() {
        heap.clear();
    }
    /**
     * Returns the number of subproblems currently in the frontier.
     *
     * @return the size of the frontier
     */
    @Override
    public int size() {
        return heap.size();
    }
    /**
     * Returns the type of cutset used in the frontier.
     *
     * @return the cutset type
     */
    @Override
    public CutSetType cutSetType() {
        return this.cutSetType;
    }

    /**
     * A comparator for {@link SubProblem} that sorts subproblems first by their lower bound,
     * and then by the state ranking if bounds are equal.
     *
     * @param <T> the type of state in the subproblems
     */
    private static final class SubProblemComparator<T> implements Comparator<SubProblem<T>> {
        /** The decorated state ranking used as a tiebreaker. */
        private final StateRanking<T> delegate;

        /**
         * Constructs a new comparator decorating the given ranking.
         *
         * @param delegate the ranking to use for tie-breaking
         */

        public SubProblemComparator(final StateRanking<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(SubProblem<T> o1, SubProblem<T> o2) {
            double cmp = o1.getLowerBound() - o2.getLowerBound();
            if (cmp == 0) {
                return delegate.reversed().compare(o1.getState(), o2.getState());
            } else {
                return Double.compare(o1.getLowerBound(), o2.getLowerBound());
            }
        }
    }

    @Override
    public String toString() {
        return heap.toString();
    }
    /**
     * Returns the best (lowest) lower bound among the subproblems in the frontier.
     *
     * @return the lower bound of the most promising subproblem
     */
    @Override
    public double bestInFrontier() {
        return heap.peek().getLowerBound();
    }
}
