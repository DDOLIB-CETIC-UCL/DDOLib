package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;

public class Max2SatProblem implements Problem<Max2SatState> {

    /**
     * Value for decision <code>true</code>.
     */
    final static int T = 1;
    /**
     * Value for decision <code>false</code>.
     */
    final static int F = 0;

    final Max2SatState root;
    private final int numVar;
    final HashMap<BinaryClause, Integer> weights;
    public final Optional<Integer> optimal;
    private Optional<String> name = Optional.empty();

    /**
     * Instantiates a Max2Sat problem.
     *
     * @param numVar  The number of variable in the problem.
     * @param weights A map from each binary clause of the problem to their weight.
     * @param optimal If known, the objective value of the optimal solution.
     */
    public Max2SatProblem(int numVar, HashMap<BinaryClause, Integer> weights, Optional<Integer> optimal) {
        this.numVar = numVar;
        this.weights = weights;
        this.root = new Max2SatState(new ArrayList<>(Collections.nCopies(numVar, 0)), 0);
        this.optimal = optimal;
    }

    /**
     * Instantiates a Max2Sat problem.
     *
     * @param numVar  The number of variable in the problem.
     * @param weights A map from each binary clause of the problem to their weight.
     */
    public Max2SatProblem(int numVar, HashMap<BinaryClause, Integer> weights) {
        this.numVar = numVar;
        this.weights = weights;
        this.root = new Max2SatState(new ArrayList<>(Collections.nCopies(numVar, 0)), 0);
        this.optimal = Optional.empty();
    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public String toString() {
        if (name.isPresent()) {
            return name.get();
        } else {
            return weights.toString();
        }
    }

    @Override
    public int nbVars() {
        return numVar;
    }

    @Override
    public Max2SatState initialState() {
        return root;
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
        return List.of(F, T).iterator();
    }

    @Override
    public Max2SatState transition(Max2SatState state, Decision decision) {
        ArrayList<Integer> newBenefit = new ArrayList<>(Collections.nCopies(numVar, 0));
        int k = decision.var();
        if (decision.val() == T) {
            for (int l = k + 1; l < nbVars(); l++) {
                // If the variable k has been set to T, and then we set the variable l to T, we gain the weight of the
                // clause (!xk || xl). But we lose the weight of the clause (!xk || !xl).
                newBenefit.set(l, state.netBenefit().get(l) + weight(f(k), t(l)) - weight(f(k), f(l)));
            }
        } else {
            for (int l = k + 1; l < nbVars(); l++) {
                // If the variable k has been set to F, and then we set the variable l to T, we gain the weight of the
                // clause (xk || xl). But we lose the weight of the clause (xk || !xl).
                newBenefit.set(l, state.netBenefit().get(l) + weight(t(k), t(l)) - weight(t(k), f(l)));
            }
        }

        return new Max2SatState(newBenefit, state.depth() + 1);
    }

    @Override
    public int transitionCost(Max2SatState state, Decision decision) {

        int k = decision.var();
        int toReturn;
        if (decision.val() == T) {
            // If k has been set to T, we gain the net benefit if it is > 0 and the weight of the unary clause xk.
            toReturn = positiveOrNull(state.netBenefit().get(k)) + weight(t(k), t(k));
            for (int l = k + 1; l < nbVars(); l++) {
                // We gain the weight of the clauses (xk || xl) and (xk || !xl)
                toReturn += weight(t(k), f(l)) + weight(t(k), t(l));
                int s_k_l = state.netBenefit().get(l);
                // According to the decision on l, we can gain (!xk || xl) or (!xk || !xl)
                toReturn += Integer.min(positiveOrNull(s_k_l) + weight(f(k), t(l)),
                        positiveOrNull(-s_k_l) + weight(f(k), f(l)));
            }
        } else {
            // If k has been set to F, we gain the net benefit if it is < 0 and the weight of the unary clause /xk.
            toReturn = positiveOrNull(-state.netBenefit().get(k)) + weight(f(k), f(k));
            for (int l = k + 1; l < nbVars(); l++) {
                // We gain the weight of the clauses (!xk || !xl) and (!xk || xl)
                toReturn += weight(f(k), f(l)) + weight(f(k), t(l));
                int s_k_l = state.netBenefit().get(l);
                // According to the decision on l, we can gain (xk || xl) or (xk || !xl)
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
