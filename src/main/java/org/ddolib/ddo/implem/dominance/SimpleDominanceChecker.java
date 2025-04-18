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

    public boolean isDominated(S state, int depth, int val) {
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
