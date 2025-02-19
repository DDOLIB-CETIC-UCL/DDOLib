package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;

public class Max2SatProblem implements Problem<ArrayList<Integer>> {

    final static int T = 1;
    final static int F = 0;

    final ArrayList<Integer> netBenefit;
    private final int numVar;
    final HashMap<BinaryClause, Integer> weights;
    public final Optional<Integer> optimal;


    public Max2SatProblem(int numVar, HashMap<BinaryClause, Integer> weights, Optional<Integer> optimal) {
        this.numVar = numVar;
        this.weights = weights;
        this.netBenefit = new ArrayList<>(Collections.nCopies(numVar, 0));
        this.optimal = optimal;
    }

    public Max2SatProblem(int numVar, HashMap<BinaryClause, Integer> weights) {
        this.numVar = numVar;
        this.weights = weights;
        this.netBenefit = new ArrayList<>(Collections.nCopies(numVar, 0));
        this.optimal = Optional.empty();
    }

    @Override
    public int nbVars() {
        return numVar;
    }

    @Override
    public ArrayList<Integer> initialState() {
        return netBenefit;
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
    public Iterator<Integer> domain(ArrayList<Integer> state, int var) {
        return List.of(F, T).iterator();
    }

    @Override
    public ArrayList<Integer> transition(ArrayList<Integer> state, Decision decision) {
        ArrayList<Integer> newBenefit = new ArrayList<>(Collections.nCopies(numVar, 0));
        int k = decision.var();
        if (decision.val() == T) {
            for (int l = k + 1; l < nbVars(); l++) {
                newBenefit.set(l, state.get(l) + weight(f(k), t(l)) - weight(f(k), f(l)));
            }
        } else {
            for (int l = k + 1; l < nbVars(); l++) {
                newBenefit.set(l, state.get(l) + weight(t(k), t(l)) - weight(t(k), f(l)));
            }
        }

        return newBenefit;
    }

    @Override
    public int transitionCost(ArrayList<Integer> state, Decision decision) {

        int k = decision.var();
        int toReturn;
        if (decision.val() == T) {
            toReturn = positiveOrNull(state.get(k)) + weight(t(k), t(k));
            for (int l = k + 1; l < nbVars(); l++) {
                toReturn += weight(t(k), f(l)) + weight(t(k), t(l));
                int s_k_l = state.get(l);
                toReturn += Integer.min(positiveOrNull(s_k_l) + weight(f(k), t(l)),
                        positiveOrNull(-s_k_l) + weight(f(k), f(l)));
            }
        } else {
            toReturn = positiveOrNull(-state.get(k)) + weight(f(k), f(k));
            for (int l = k + 1; l < nbVars(); l++) {
                toReturn += weight(f(k), f(l)) + weight(f(k), t(l));
                int s_k_l = state.get(l);
                toReturn += Integer.min(positiveOrNull(s_k_l) + weight(t(k), t(l)),
                        positiveOrNull(-s_k_l) + weight(t(k), f(l)));
            }
        }
        return toReturn;
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
    public int t(int x) {
        return toBinaryClauseVariable(x);
    }

    /**
     * Returns a value to model the literal <code>NOT a_x</code> in a Binary clause.
     *
     * @param x The index of a decision variable.
     * @return A value to model the literal <code>NOT a_x</code> in a Binary clause.
     */
    public int f(int x) {
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
    public int weight(int x, int y) {
        BinaryClause bc = new BinaryClause(x, y);
        BinaryClause bcCommuted = new BinaryClause(y, x);
        return weights.getOrDefault(bc, weights.getOrDefault(bcCommuted, 0));
    }

}
