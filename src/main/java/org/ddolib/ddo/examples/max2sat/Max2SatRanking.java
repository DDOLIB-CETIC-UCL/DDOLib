package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.heuristics.StateRanking;

import java.util.ArrayList;

public class Max2SatRanking implements StateRanking<ArrayList<Integer>> {

    public int rank(ArrayList<Integer> state) {
        return state.stream().mapToInt(value -> value).map(Math::abs).sum();
    }

    @Override
    public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
        return Integer.compare(rank(o1), rank(o2));
    }
}
