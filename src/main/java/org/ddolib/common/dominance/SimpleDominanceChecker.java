package org.ddolib.common.dominance;


import org.ddolib.modeling.Dominance;

import java.util.*;

/**
 * The {@code SimpleDominanceChecker} class implements a straightforward dominance
 * management mechanism used in search algorithms based on Multi-valued Decision Diagrams (MDDs),
 * such as DDO (Decision Diagram Optimization).
 * <p>
 * It maintains, for each depth level of the MDD, a collection of non-dominated states,
 * according to a user-defined {@link Dominance} relation. When a new state is explored,
 * this checker determines whether it is dominated by an existing state, or if it dominates
 * others already stored in the same level.
 * </p>
 *
 * <p>
 * The class uses a nested structure of:
 * </p>
 * <ul>
 *     <li>A list indexed by depth (one per decision variable or time step),</li>
 *     <li>For each depth, a map associating a {@code key} (computed from the state)
 *         to a set of non-dominated states,</li>
 *     <li>Each state is wrapped in a {@link ValueState} object containing its
 *         associated objective value and a deterministic ID for ordering.</li>
 * </ul>
 * <p>
 * The goal is to avoid exploring redundant or inferior states during optimization,
 * which significantly reduces the search space and improves efficiency.
 * </p>
 *
 * @param <T> The type of the states managed by the dominance relation.
 *
 * @see Dominance
 * @see DominanceChecker
 * @see ValueState
 */
public class SimpleDominanceChecker<T> extends DominanceChecker<T> {

    /**
     * Internal container class that associates a state with its objective value.
     * <p>
     * This class implements {@link Comparable} to allow ordering by descending objective value,
     * and uses a deterministic unique ID as a tie-breaker to ensure consistent ordering.
     * </p>
     */
    private class ValueState implements Comparable<ValueState> {

        /** The objective or path value associated with the state. */
        double value;

        /** The corresponding state instance. */
        T state;

        /** Static counter used to generate unique deterministic IDs. */
        static long nextId = Long.MIN_VALUE;

        /** Unique identifier for tie-breaking in comparisons. */
        final long id = nextId++;
        /**
         * Constructs a new {@code ValueState}.
         *
         * @param value the objective or path value associated with the state
         * @param state the state itself
         */
        ValueState(double value, T state) {
            this.value = value;
            this.state = state;
        }
        /**
         * Compares this {@code ValueState} with another for ordering.
         * <p>
         * States are primarily compared by descending objective value, and in case of a tie,
         * by ascending ID (ensuring deterministic ordering).
         * </p>
         *
         * @param o the other {@code ValueState} to compare to
         * @return a negative integer, zero, or a positive integer depending on the order
         */
        @Override
        public int compareTo(ValueState o) {
            int cmp = Double.compare(o.value, value);
            if (cmp == 0) return Long.compare(o.id, id);
            return cmp;
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, value);
        }
    }
    /**
     * The dominance fronts organized per depth level.
     * Each level contains a map of key → sorted set of {@link ValueState}s.
     */
    private final ArrayList<Map<Object, TreeSet<ValueState>>> fronts;
    /**
     * Constructs a {@code SimpleDominanceChecker} with the given dominance relation
     * and number of decision variables.
     *
     * @param dominance the {@link Dominance} relation used to compare states
     * @param nVars the number of decision variables or depth levels in the MDD
     */
    public SimpleDominanceChecker(Dominance<T> dominance, int nVars) {
        super(dominance);
        this.fronts = new ArrayList<>(nVars + 1);
        for (int i = 0; i <= nVars; i++) {
            fronts.add(new HashMap<>());
        }
    }

    /**
     * Updates the dominance front at a given depth level based on the provided state.
     * <p>
     * The method performs the following steps:
     * </p>
     * <ol>
     *     <li>Retrieves the dominance front corresponding to the given {@code depth}.</li>
     *     <li>Checks whether the state is dominated by an existing one:
     *         if yes, returns {@code true} (no insertion).</li>
     *     <li>If not dominated, removes all states dominated by the new one,
     *         then inserts the new state in the front.</li>
     * </ol>
     * @param state    the state to evaluate for dominance
     * @param depth    the depth (or variable index) in the MDD
     * @param objValue the objective value associated with this state
     * @return {@code true} if the state is dominated and should not be added;
     *         {@code false} if the state is non-dominated and has been added
     */
    @Override
    public boolean updateDominance(T state, int depth, double objValue) {
        Map<Object, TreeSet<ValueState>> front = fronts.get(depth);
        Object key = dominance.getKey(state);
        boolean dominated = false;
        if (front.containsKey(key)) {
            TreeSet<ValueState> set = front.get(key);
            ArrayList<ValueState> removed = new ArrayList<>();
            for (ValueState vs : set) {
                if (vs.value < objValue && dominance.isDominatedOrEqual(state, vs.state)) {
                    dominated = true;
                    break;
                } else if (objValue <= vs.value && dominance.isDominatedOrEqual(vs.state, state)) {
                    removed.add(vs);
                }
            }
            for (ValueState vs : removed) {
                set.remove(vs);
            }
        }
        if (!dominated) {
            TreeSet<ValueState> set = front.computeIfAbsent(key, k -> new TreeSet<>());
            set.add(new ValueState(objValue, state));
        }
        return dominated;
    }

}