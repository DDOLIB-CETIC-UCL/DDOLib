package org.ddolib.examples.qks;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class QKSProblem implements Problem<QKSState> {
    /** Capacity of the knapsack */
    final int capacity;
    /** Matrix of profit for each item */
    final int[][] profitMatrix;
    /** Weight of each item */
    final int[] weights;
    /** Optional known optimal solution value. */
    public final Optional<Double> optimal;
    /** Optional problem name or file name. */
    final Optional<String> name;
    /** The maximal profit of each item, used for normalization in QKSDistance */
    final double[] maxProfits;

    /**
     * Constructs a Quadratic Knapsack Problem with given capacity, profits, weights and known optimal value.
     *
     * @param capacity maximum capacity of the knapsack
     * @param profitMatrix matrix of item profits
     * @param weights array of item weights
     * @param optimal known optimal value
     */
    public QKSProblem(int capacity, int[][] profitMatrix, int[] weights, Optional<Double> optimal) {
        this.capacity = capacity;
        this.profitMatrix = profitMatrix;
        this.weights = weights;
        this.optimal = optimal;
        this.name = Optional.empty();

        this.maxProfits = new double[capacity];
        for (int i = 0; i < capacity; i++) {
            for (int j = 0; j < capacity; j++) {
                maxProfits[i] += 2 * profitMatrix[i][j];
            }
        }
    }

    @Override
    public int nbVars() {
        return weights.length;
    }

    @Override
    public QKSState initialState() {
        double[] initialProfits = new double[profitMatrix.length];
        for (int i = 0; i < profitMatrix.length; i++) {
            initialProfits[i] = profitMatrix[i][i];
        }
        BitSet remainingItems = new BitSet(profitMatrix.length);
        remainingItems.set(0, profitMatrix.length);
        return new QKSState(capacity, initialProfits, remainingItems);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(QKSState state, int var) {
        if (state.capacity >= weights[var]) { // The item can be taken or not
            return Arrays.asList(1, 0).iterator();
        } else { // The item cannot be taken
            return List.of(0).iterator();
        }
    }

    @Override
    public QKSState transition(QKSState state, Decision decision) {
        double newCapacity = state.capacity - weights[decision.var()] * decision.val();
        double[] newProfits = state.itemsProfit.clone();
        newProfits[decision.var()] = newCapacity;
        for (int i = 0; i < newProfits.length; i++) {
            newProfits[i] += 2 * profitMatrix[decision.var()][i] * decision.val();
        }
        BitSet newRemainingItems = (BitSet) state.remainingItems.clone();
        newRemainingItems.set(decision.var(), false);
        return new QKSState(newCapacity, newProfits, newRemainingItems);
    }

    @Override
    public double transitionCost(QKSState state, Decision decision) {
        return -state.itemsProfit[decision.var()] * decision.val();
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

        double totalProfit = 0;
        double totalWeight = 0;
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution.length; j++) {
                totalProfit += profitMatrix[i][j] * solution[i] * solution[j];
            }
            totalWeight += weights[i] * solution[i];
        }

        if (totalWeight > capacity) {
            String msg = String.format("The weight of %s (%d) exceeds the capatity of the " +
                    "knapsack (%d)", Arrays.toString(solution), totalWeight, capacity);
            throw new InvalidSolutionException(msg);
        }

        return -totalProfit;
    }

    /**
     * Constructs a Quadratic Knapsack problem from a file.
     * <p>
     * The file format should contain:
     * <ul>
     *     <li>First line: number of items, capacity, (optional) optimal value.</li>
     *     <li>Following lines: item profit and weight for each item.</li>
     * </ul>
     *
     * @param fname path to the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public QKSProblem(final String fname) throws IOException {
        boolean isFirst = true;
        boolean isSecond = false;
        int n = 0;
        int c = 0;
        int count = 0;
        final File f = new File(fname);
        String line;
        int[][] profit = new int[0][0];
        int[] weights = new int[0];
        Optional<Double> optimal = Optional.empty();
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            while ((line = bf.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    isSecond = true;
                    String[] tokens = line.split("\\s");
                    n = Integer.parseInt(tokens[0]);
                    c = Integer.parseInt(tokens[1]);
                    if (tokens.length == 3) {
                        optimal = Optional.of(Double.parseDouble(tokens[2]));
                    }
                    profit = new int[n][n];
                    weights = new int[n];
                } else if (isSecond) {
                    isSecond = false;
                    String[] tokens = line.split("\\s");
                    assert tokens.length == n;
                    for (int i = 0; i < tokens.length; i++) {
                        weights[i] = Integer.parseInt(tokens[i]);
                    }
                } else {
                    if (count < n) {
                        String[] tokens = line.split("\\s");
                        assert tokens.length == n;
                        for (int i = 0; i < tokens.length; i++) {
                            profit[count][i] = Integer.parseInt(tokens[i]);
                        }
                        count++;
                    }
                }
            }
        }
        this.capacity = c;
        this.profitMatrix = profit;
        this.weights = weights;
        this.optimal = optimal;
        this.name = Optional.of(fname);
        this.maxProfits = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights.length; j++) {
                maxProfits[i] += 2 * profitMatrix[i][j];
            }
        }
    }

    /**
     * Construct a Quadratic Knapsack Problem by randomly generating the instance
     *
     * @param n the number of objects
     * @param sparsity the sparsity of the profit matrix
     * @param seed the seed for random generation
     */
    public QKSProblem(int n, double sparsity, int seed) {
        this(n, sparsity, seed, false);
    }

    /**
     * Construct a Quadratic Knapsack Problem by randomly generating the instance
     *
     * @param n the number of objects
     * @param sparsity the sparsity of the profit matrix
     * @param seed the seed for random generation
     * @param bruteForce a boolean indicating if the optimal solution should be computed with brute force
     */
    public QKSProblem(int n, double sparsity, int seed, boolean bruteForce) {
        Random rand = new Random(seed);

        profitMatrix = new int[n][n];
        weights = new int[n];
        int sumWeights = 0;
        for (int i = 0; i < n; i++) {
            weights[i] = rand.nextInt(100);
            sumWeights += weights[i];
            for (int j = i; j < n; j++) {
                if (rand.nextDouble() < sparsity) {
                    profitMatrix[i][j] = 0;
                } else {
                    profitMatrix[i][j] = 1 + rand.nextInt(100);
                    profitMatrix[j][i] = profitMatrix[i][j];
                }
            }
        }
        this.capacity = rand.nextInt(50, sumWeights);

        if (bruteForce) {
            this.optimal = Optional.of(bruteForce());
        } else {
            this.optimal = Optional.empty();
        }

        this.maxProfits = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights.length; j++) {
                maxProfits[i] += 2 * profitMatrix[i][j];
            }
        }

        this.name = Optional.empty();
    }

    public String instanceFormat() {
        StringBuilder sb = new StringBuilder();
        if (optimal.isPresent()) {
            sb.append(String.format("%d %d %d%n", weights.length, (int) capacity, optimal.get().intValue()));
        } else {
            sb.append(String.format("%d %d%n", weights.length, (int) capacity));
        }
        for (int weight : weights) {
            sb.append(weight).append(" ");
        }
        sb.append("\n");
        for (int i = 0; i < profitMatrix.length; i++) {
            for (int j = 0; j < profitMatrix[i].length; j++) {
                sb.append(profitMatrix[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Find the optimal solution by brute forcing the problem.
     *
     * @return the cost of the optimal solution
     */
    private double bruteForce() {
        double[] solution = new double[profitMatrix.length];
        return recursion(solution, 0, capacity);
    }

    private double recursion(double[] solution, int index, double capa) {
        if (index == solution.length) {
            double cost = 0;
            for (int i = 0; i < solution.length; i++) {
                for (int j = 0; j < solution.length; j++) {
                    cost += profitMatrix[i][j] * solution[i] * solution[j];
                }
            }
            return cost;
        }
        if (capa >= weights[index]) {
            double[] solutionTakingObj = solution.clone();
            solutionTakingObj[index] = 1;
            return Math.max(recursion(solution, index + 1, capa),
                    recursion(solutionTakingObj, index + 1, capa - weights[index]));
        } else {
            return recursion(solution, index + 1, capa);
        }
    }
}
