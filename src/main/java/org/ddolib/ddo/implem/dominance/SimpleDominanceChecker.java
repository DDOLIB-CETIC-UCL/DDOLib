package org.ddolib.ddo.implem.dominance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleDominanceChecker<T,K> {

    private Dominance <T,K> dominance;
    private int nVars;
    private Comparator<T> cmp;

    class ValueState implements Comparable<ValueState> {

        int value;
        T state;
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

    public Map<K, TreeSet<ValueState>>[] fronts;

    public SimpleDominanceChecker(Dominance<T,K> dominance, int nVars) {
        this.dominance = dominance;
        this.nVars = nVars;
        this.fronts = new Map[nVars];
        for (int i = 0; i < nVars; i++) {
            fronts[i] = new HashMap<>();//new ConcurrentHashMap<>();
        }
    }

    /**
     * Check if the state is dominated by any of the states in the front
     * If it is, return true
     * If it is not, add the state and remove the dominated states from the front
     * @param state the state to check
     * @param depth the depth of the state in the MDD
     * @param val the objective value of the state
     * @return true if the state is dominated, false otherwise
     */

    public boolean updateDominance(T state, int depth, int val) {
        Map<K, TreeSet<ValueState>> front = fronts[depth];
        K key = dominance.getKey(state);
        boolean dominated = false;
        if (front.containsKey(key)) {
            for (ValueState vs : front.get(key)) {
                if (vs.value > val && dominance.isDominatedOrEqual (state,vs.state)) {
                    dominated = true;
                    break;
                } else if (val > vs.value && dominance.isDominatedOrEqual (vs.state, state)) {
                    front.remove(vs);
                }
            }
        }
        if (!dominated) {
            TreeSet<ValueState> set = front.get(key);
            if (set == null) {
                set = new TreeSet<>();
                front.put(key, set);
            }
            set.add(new ValueState(val, state));
        }
        return dominated;
    }

}
