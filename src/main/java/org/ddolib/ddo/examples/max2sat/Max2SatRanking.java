package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.heuristics.StateRanking;

import java.util.ArrayList;

/**
 * Class used to compare two states for the Max2Sat problem.
 * <br>
 * A positive value means that we have a bigger benefit by setting <code>x<sub>k</sub></code> to <code>T</code>.<br>
 * A negative value means that we have a bigger benefit by setting <code>x<sub>k</sub></code> to <code>F</code>.<br>
 * When comparing two states, the best is the one that can generate the biggest benefit, independently of the decisions.
 * That's why we sum the absolute value of each benefit to compare the states.
 */
public class Max2SatRanking implements StateRanking<ArrayList<Integer>> {

    public int rank(ArrayList<Integer> state) {
        return state.stream().mapToInt(value -> value).map(Math::abs).sum();
    }

    @Override
    public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
        return Integer.compare(rank(o1), rank(o2));
    }
}
