package org.ddolib.examples.max2sat;


import org.ddolib.modeling.StateRanking;

/**
 * Class used to compare two states for the Max2Sat problem.
 * <br>
 * A positive value means that we have a bigger benefit by setting <code>x<sub>k</sub></code> to <code>T</code>.<br>
 * A negative value means that we have a bigger benefit by setting <code>x<sub>k</sub></code> to <code>F</code>.<br>
 * When comparing two states, the best is the one that can generate the biggest benefit, independently of the decisions.
 * That's why we sum the absolute value of each benefit to compare the states.
 */
public class Max2SatRanking implements StateRanking<Max2SatState> {

    public static int rank(Max2SatState state) {
        int toReturn = 0;
        for (int i = state.depth(); i < state.netBenefit().size(); i++) {
            toReturn += Math.abs(state.netBenefit().get(i));
        }

        return toReturn;
    }

    @Override
    public int compare(Max2SatState o1, Max2SatState o2) {
        return Integer.compare(rank(o1), rank(o2));
    }
}
