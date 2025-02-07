package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;

public class Max2SatProblem implements Problem<Max2SatState> {

    final static int T = 1;
    final static int F = 0;

    final Max2SatState state;
    private final int numVar;
    final HashMap<BinaryClause, Integer> weights;

    public Max2SatProblem(int numVar, HashMap<BinaryClause, Integer> weights) {
        this.numVar = numVar;
        this.weights = weights;
        this.state = new Max2SatState(new int[numVar], 0);

    }

    @Override
    public int nbVars() {
        return numVar;
    }

    @Override
    public Max2SatState initialState() {
        return state;
    }

    @Override
    public int initialValue() {
        int toReturn = 0;
        for (int i = 0; i < nbVars(); i++) {
            toReturn += weight(t(i), f(i));
        }
        return toReturn;
    }

    @Override
    public Iterator<Integer> domain(Max2SatState state, int var) {
        return List.of(T, F).iterator();
    }

    @Override
    public Max2SatState transition(Max2SatState state, Decision decision) {
        int[] newCost = new int[nbVars()];
        int k = decision.var();
        if (decision.val() == T) {
            for (int l = state.depth() + 1; l < nbVars(); l++) {
                newCost[l] = state.marginalCosts()[l] + weight(f(k), t(l)) - weight(f(k), f(l));
            }
        } else {
            for (int l = state.depth() + 1; l < nbVars(); l++) {
                newCost[l] = state.marginalCosts()[l] + weight(t(k), t(l)) - weight(t(k), f(l));
            }
        }

        return new Max2SatState(newCost, state.depth() + 1);
    }

    @Override
    public int transitionCost(Max2SatState state, Decision decision) {

        int k = decision.var();
        if (decision.val() == T) {
            int toReturn = positiveOrNull(state.marginalCosts()[k]) + weight(t(k), t(k));
            for (int l = k + 1; l < nbVars(); l++) {
                toReturn += weight(t(k), f(l)) + weight(t(k), t(l));
                int s_k_l = state.marginalCosts()[l];
                toReturn += Integer.min(positiveOrNull(s_k_l) + weight(f(k), t(l)),
                        positiveOrNull(-s_k_l) + weight(f(k), f(l)));
            }
            return toReturn;
        } else {
            int toReturn = positiveOrNull(-state.marginalCosts()[k]) + weight(f(k), f(k));
            for (int l = k + 1; l < nbVars(); l++) {
                toReturn += weight(f(k), f(l)) + weight(f(k), t(l));
                int s_k_l = state.marginalCosts()[l];
                toReturn += Integer.min(positiveOrNull(s_k_l) + weight(t(k), t(l)),
                        positiveOrNull(-s_k_l) + weight(t(k), f(l)));
            }
            return toReturn;
        }
    }


    /**
     * Decision Variables are indexed from 0. However, BinaryClause do not allow index 0 for variable.
     * This method shift the index
     *
     * @param x The index of a variable
     * @return An allowed index in BinaryClause for the variable.
     */
    private int toBinaryClauseVariable(int x) {
        return x + 1;
    }

    /**
     * Returns a value to model the literal <code>a_x</code> in a Binary clause.
     *
     * @param x The index of a decision variable.
     * @return A value to model the literal <code>a_x</code> in a Binary clause.
     */
    private int t(int x) {
        return toBinaryClauseVariable(x);
    }

    /**
     * Returns a value to model the literal <code>NOT a_x</code> in a Binary clause.
     *
     * @param x The index of a decision variable.
     * @return A value to model the literal <code>NOT a_x</code> in a Binary clause.
     */
    private int f(int x) {
        return -toBinaryClauseVariable(x);
    }

    /**
     * Returns a marginal cost if it is positive, else 0.
     *
     * @param x A marginal cost associated to decision variable.
     * @return <code>max(0, x)</code>
     */
    private int positiveOrNull(int x) {
        return Integer.max(0, x);
    }

    /**
     * Returns the weight of the binary clause.
     *
     * @param x The index of variable in BinaryClause
     * @param y Another index of variable in BinaryClause.
     * @return The weigh of the BinaryClause.
     */
    private int weight(int x, int y) {
        BinaryClause bc = new BinaryClause(x, y);
        return weights.getOrDefault(bc, 0);
    }

}
