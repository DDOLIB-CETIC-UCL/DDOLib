package org.ddolib.ddo.implem.dominance;

import java.util.*;

public class SimpleDominanceChecker<S,K> {

    private Dominance <S,K> dominance;
    private int nVars;
    private Comparator<S> cmp;

    class ValueState {
        int value;
        S state;
        ValueState(int value, S state) {
            this.value = value;
            this.state = state;
        }
        @Override
        public int hashCode() {
            return Objects.hash(state, value);
        }
    }

    public Map<K, Set<ValueState>> [] fronts;

    public SimpleDominanceChecker(Dominance<S,K> dominance, int nVars) {
        this.dominance = dominance;
        this.nVars = nVars;
        this.fronts = new Map[nVars];
        for (int i = 0; i < nVars; i++) {
            fronts[i] = new HashMap<>();
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
    public boolean updateDominance(S state, int depth, int val) {
        Map<K, Set<ValueState>> front = fronts[depth];
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
            Set<ValueState> set = front.get(key);
            if (set == null) {
                set = new HashSet<>();
                front.put(key, set);
            }
            set.add(new ValueState(val, state));
        }
        return dominated;
    }

}
