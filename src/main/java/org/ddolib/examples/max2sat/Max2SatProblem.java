package org.ddolib.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Represents a <b>Maximum 2-Satisfiability (MAX2SAT)</b> problem instance.
 * <p>
 * In a MAX2SAT problem, each clause involves at most two literals, and each clause
 * has an associated weight. The objective is to find an assignment to the variables
 * that maximizes the sum of the weights of satisfied clauses.
 * </p>
 *
 * <p>
 * This class implements the {@link Problem} interface with states of type {@link Max2SatState}.
 * It provides methods for generating the domain of a variable, computing transitions, and
 * evaluating the cost of a transition.
 * </p>
 *
 * <p>
 * Decision variables are indexed from 0 internally, but the {@link BinaryClause} representation
 * requires indices starting from 1. The methods {@link #t(int)} and {@link #f(int)} handle
 * this mapping for positive and negated literals, respectively.
 * </p>
 *
 * @see Max2SatState
 * @see BinaryClause
 * @see Problem
 */
public class Max2SatProblem implements Problem<Max2SatState> {

    /**
     * Value representing a decision of TRUE.
     */
    final static int T = 1;

    /**
     * Value representing a decision of FALSE.
     */
    final static int F = 0;

    /**
     * Number of decision variables in this MAX2SAT instance.
     */
    private final int numVar;

    /**
     * Map storing the weight of each binary clause.
     */
    final HashMap<BinaryClause, Integer> weights;

    /**
     * Optional value of the known optimal solution.
     */
    private final Optional<Double> optimal;

    /**
     * Optional name of the instance (e.g., filename).
     */
    private Optional<String> name = Optional.empty();

    /**
     * Constructs a MAX2SAT problem instance.
     *
     * @param numVar  Number of decision variables.
     * @param weights Map of binary clauses to their weights.
     * @param optimal Optional known optimal value.
     */
    public Max2SatProblem(int numVar, HashMap<BinaryClause, Integer> weights,
                          Optional<Double> optimal) {
        this.numVar = numVar;
        this.weights = weights;
        this.optimal = optimal;
    }

    /**
     * Constructs a MAX2SAT problem instance without specifying an optimal value.
     *
     * @param numVar  Number of decision variables.
     * @param weights Map of binary clauses to their weights.
     */
    public Max2SatProblem(int numVar, HashMap<BinaryClause, Integer> weights) {
        this.numVar = numVar;
        this.weights = weights;
        this.optimal = Optional.empty();
    }

    /**
     * Constructs a MAX2SAT problem instance from a file.
     * <p>
     * The file format:
     * <ul>
     *   <li>First line: number of variables [optimal value (optional)]</li>
     *   <li>Subsequent lines: each line is a clause:
     *     <ul>
     *       <li>Unary clause: variable index and weight</li>
     *       <li>Binary clause: two variable indices and weight</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param fname Path to the input file.
     * @throws IOException if the file cannot be read.
     */
    public Max2SatProblem(String fname) throws IOException {
        int n = 0;
        HashMap<BinaryClause, Integer> weights = new HashMap<>();
        Optional<Double> opti = Optional.empty();
        boolean firstLine = true;

        try (BufferedReader bf = new BufferedReader(new FileReader(fname))) {
            String line;
            while ((line = bf.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    String[] tokens = line.split("\\s");
                    n = Integer.parseInt(tokens[0]);
                    if (tokens.length == 2) {
                        opti = Optional.of(Double.parseDouble(tokens[1]));
                    }
                } else {
                    String[] tokens = line.split("\\s");
                    if (tokens.length == 2) { // Unary clause
                        int i = Integer.parseInt(tokens[0]);
                        int w = Integer.parseInt(tokens[1]);
                        weights.put(new BinaryClause(i, i), w);
                    } else {
                        int i = Integer.parseInt(tokens[0]);
                        int j = Integer.parseInt(tokens[1]);
                        int w = Integer.parseInt(tokens[2]);
                        weights.put(new BinaryClause(i, j), w);
                    }
                }
            }
        }
        this.numVar = n;
        this.weights = weights;
        this.optimal = opti;
        this.name = Optional.of(fname);
    }

    @Override
    public String toString() {
        return name.orElseGet(weights::toString);
    }

    @Override
    public int nbVars() {
        return numVar;
    }

    @Override
    public Max2SatState initialState() {
        return new Max2SatState(new ArrayList<>(Collections.nCopies(numVar, 0)), 0);
    }

    /**
     * Computes the initial value of the problem.
     * <p>
     * Unary clauses of the form (x_i OR NOT x_i) are always satisfied,
     * so their weight is added directly at the start.
     *
     * @return the initial value
     */
    @Override
    public double initialValue() {
        int toReturn = 0;
        for (int i = 0; i < nbVars(); i++) {
            toReturn += weight(t(i), f(i));
            toReturn += weight(f(i), t(i));
        }
        return -toReturn;
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
    public double transitionCost(Max2SatState state, Decision decision) {

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
        return -toReturn;
    }


    /**
     * Converts zero-based variable index to a 1-based index required by BinaryClause.
     *
     * @param x variable index (0-based)
     * @return 1-based index
     */
    private int toBinaryClauseVariable(int x) {
        return x + 1;
    }

    /**
     * Returns the positive literal for a variable.
     *
     * @param x variable index
     * @return value representing x_i
     */
    public int t(int x) {
        return toBinaryClauseVariable(x);
    }

    /**
     * Returns the negated literal for a variable.
     *
     * @param x variable index
     * @return value representing NOT x_i
     */
    public int f(int x) {
        return -toBinaryClauseVariable(x);
    }

    /**
     * Returns the positive part of a marginal value (or zero if negative).
     *
     * @param x marginal value
     * @return max(0, x)
     */
    private int positiveOrNull(int x) {
        return Integer.max(0, x);
    }

    /**
     * Returns the weight of a binary clause (x, y), considering commutativity.
     *
     * @param x first literal
     * @param y second literal
     * @return weight of the clause, or 0 if absent
     */
    public int weight(int x, int y) {
        BinaryClause bc = new BinaryClause(x, y);
        BinaryClause bcCommuted = new BinaryClause(y, x);
        return weights.getOrDefault(bc, weights.getOrDefault(bcCommuted, 0));
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x);
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }

        int value = 0;
        for (Map.Entry<BinaryClause, Integer> entry : weights.entrySet()) {
            BinaryClause bc = entry.getKey();
            int w = entry.getValue();
            int a = solution[Math.abs(bc.i) - 1];
            int b = solution[Math.abs(bc.j) - 1];
            value += bc.eval(a, b) * w;
        }

        return -value;
    }
}
