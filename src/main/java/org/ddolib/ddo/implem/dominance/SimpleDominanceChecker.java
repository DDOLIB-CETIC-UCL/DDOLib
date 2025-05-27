package org.ddolib.ddo.implem.dominance;


import java.util.*;


public class SimpleDominanceChecker<T, K> {

    private final Dominance<T, K> dominance;

    /**
     * Container for a state and the value of the longest path to this state
     */
    private class ValueState implements Comparable<ValueState> {

        int value;
        T state;

        /**
         * Instantiate a new ValueState
         *
         * @param value The length of the longest path to the input state.
         * @param state The input state.
         */
        ValueState(int value, T state) {
            this.value = value;
            this.state = state;
        }

        @Override
        public int compareTo(ValueState o) {
            return Integer.compare(value, o.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, value);
        }
    }

    private final ArrayList<Map<K, TreeSet<ValueState>>> fronts;

    public SimpleDominanceChecker(Dominance<T, K> dominance, int nVars) {
        this.dominance = dominance;
        this.fronts = new ArrayList<>(nVars);
        for (int i = 0; i < nVars; i++) {
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
    public boolean updateDominance(T state, int depth, int objValue) {
        Map<K, TreeSet<ValueState>> front = fronts.get(depth);
        K key = dominance.getKey(state);
        boolean dominated = false;
        if (front.containsKey(key)) {
            for (ValueState vs : front.get(key)) {
                if (vs.value > objValue && dominance.isDominatedOrEqual(state, vs.state)) {
                    dominated = true;
                    break;
                } else if (objValue > vs.value && dominance.isDominatedOrEqual(vs.state, state)) {
                    front.get(key).remove(vs);
                }
            }
        }
        if (!dominated) {
            TreeSet<ValueState> set = front.computeIfAbsent(key, k -> new TreeSet<>());
            set.add(new ValueState(objValue, state));
        }
        return dominated;
    }

}
