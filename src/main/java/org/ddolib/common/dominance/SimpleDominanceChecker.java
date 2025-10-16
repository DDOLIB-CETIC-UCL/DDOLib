package org.ddolib.common.dominance;


import org.ddolib.modeling.Dominance;

import java.util.*;

/**
 * DominanceChecker that maintains lists of non dominated nodes for each level of the mdd.
 *
 * @param <T> The type of states.
 */
public class SimpleDominanceChecker<T> extends DominanceChecker<T> {

    /**
     * Container for a state and the value of the longest path to this state
     */
    private class ValueState implements Comparable<ValueState> {

        double value;
        T state;

        // Deterministic unique IDs used as a tie-breaker in compareTo
        static long nextId = Long.MIN_VALUE;
        final long id = nextId++;

        /**
         * Instantiate a new ValueState
         *
         * @param value The length of the longest path to the input state.
         * @param state The input state.
         */
        ValueState(double value, T state) {
            this.value = value;
            this.state = state;
        }

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

    private final ArrayList<Map<Object, TreeSet<ValueState>>> fronts;

    public SimpleDominanceChecker(Dominance<T> dominance, int nVars) {
        super(dominance);
        this.fronts = new ArrayList<>(nVars + 1);
        for (int i = 0; i <= nVars; i++) {
            fronts.add(new HashMap<>());
        }
    }

    /**
     * Check if the state is dominated by any of the states in the front
     * If it is, return true
     * If it is not, add the state and remove the dominated states from the front
     *
     * @param state    the state to check
     * @param depth    the depth of the state in the MDD
     * @param objValue the objective value of the state
     * @return true if the state is dominated, false otherwise
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