package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.Decision;
import org.ddolib.examples.smic.SMICState;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import javax.xml.XMLConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Represents an instance of the Maximum Coverage (MaxCover) problem.
 *
 * <p>
 * This class implements the {@link Problem} interface and provides:
 * <ul>
 *   <li>Creation of random or file-based problem instances</li>
 *   <li>Evaluation of solutions and state transitions for Decision Diagram Optimization (DDO)</li>
 *   <li>Computation of centralities for each item, used in heuristics</li>
 *   <li>Support for reading/writing instances in a standard text format</li>
 * </ul>
 *
 * <p>
 * The problem is defined by:
 * <ul>
 *   <li>{@code nbItems}: the total number of items to cover</li>
 *   <li>{@code nbSubSets}: the total number of available subsets</li>
 *   <li>{@code nbSubSetsToChoose}: the number of subsets allowed to select</li>
 *   <li>{@code subSets}: the array of BitSets representing the items covered by each subset</li>
 *   <li>{@code centralities}: the relative frequency of each item appearing in subsets</li>
 *   <li>{@code optimal}: optionally, the optimal solution value (for benchmarking)</li>
 * </ul>
 */

public class MaxCoverProblem implements Problem<MaxCoverState> {
    /** Number of items in the instance. */
    public final int nbItems;
    /** Number of subsets in the instance. */
    public final int nbSubSets;
    /** Number of subsets that can be selected. */
    public final int nbSubSetsToChoose;
    /** Array of subsets represented as BitSets, where each BitSet indicates the items it covers. */
    public final BitSet[] subSets;
    /** Relative frequency (centrality) of each item in all subsets. */
    public final double[] centralities;
    /** Optional instance name. */
    public Optional<String> name;
    /** Optional optimal solution value. */
    public Optional<Double> optimal;
    /**
     * Constructs a MaxCover instance with full specification.
     *
     * @param name optional instance name
     * @param nbItems number of items
     * @param nbSubSets number of subsets
     * @param nbSubSetsToChoose number of subsets allowed to select
     * @param subSets array of subsets represented as BitSets
     * @param optimal optional optimal solution value
     */
    public MaxCoverProblem(Optional<String> name, int nbItems, int nbSubSets, int nbSubSetsToChoose, BitSet[] subSets, Optional<Double> optimal) {
        this.name = name;
        this.nbItems = nbItems;
        this.nbSubSets = nbSubSets;
        this.nbSubSetsToChoose = nbSubSetsToChoose;
        this.subSets = subSets;
        this.optimal = optimal;
        this.centralities = new double[nbItems];
        computeCentralities();
    }


    /**
     * Constructs a MaxCover instance without a name.
     *
     * @param nbItems number of items
     * @param nbSubSets number of subsets
     * @param nbSubSetsToChoose number of subsets allowed to select
     * @param subSets array of subsets represented as BitSets
     * @param optimal optional optimal solution value
     */
    public MaxCoverProblem(int nbItems, int nbSubSets, int nbSubSetsToChoose, BitSet[] subSets, Optional<Double> optimal) {
        this.name = Optional.empty();
        this.nbItems = nbItems;
        this.nbSubSets = nbSubSets;
        this.nbSubSetsToChoose = nbSubSetsToChoose;
        this.subSets = subSets;
        this.optimal = optimal;
        this.centralities = new double[nbItems];
        computeCentralities();
    }
    /**
     * Constructs a MaxCover instance without name or optimal value.
     *
     * @param nbItems number of items
     * @param nbSubSets number of subsets
     * @param nbSubSetsToChoose number of subsets allowed to select
     * @param subSets array of subsets represented as BitSets
     */
    public MaxCoverProblem(int nbItems, int nbSubSets, int nbSubSetsToChoose, BitSet[] subSets) {
        this.name = Optional.empty();
        this.nbItems = nbItems;
        this.nbSubSets = nbSubSets;
        this.nbSubSetsToChoose = nbSubSetsToChoose;
        this.subSets = subSets;
        this.optimal = Optional.empty();
        this.centralities = new double[nbItems];
        computeCentralities();
    }
    /**
     * Generates a random MaxCover instance using coordinates and a distance threshold.
     *
     * @param n number of items
     * @param m number of subsets
     * @param k number of subsets to select
     * @param maxR maximum coverage radius
     * @param seed random seed
     */
    public MaxCoverProblem(int n, int m, int k, double maxR, int seed) {
        Random rand = new Random(seed);
        double[] xcoord = new double[n];
        double[] ycoord = new double[n];
        for (int i = 0; i < n; i++) {
            xcoord[i] = rand.nextDouble();
            ycoord[i] = rand.nextDouble();
        }
        BitSet[] subSets = new BitSet[m];
        for (int i = 0; i < m; i++) {
            subSets[i] = new BitSet(n);
            for (int j = 0; j < n; j++) {
                double dist = distance(xcoord, ycoord, i, j);
                if (dist <= maxR) {
                    subSets[i].set(j, true);
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (!isContained(subSets, i)) {
                int f = minItems(xcoord, ycoord, subSets, i);
                subSets[f].set(i, true);
            }
        }

        this.name = Optional.of("maxCoverage_" + n + "_" + m + "_" + k + "_" + maxR + "_" + seed);
        this.nbItems = n;
        this.nbSubSets = m;
        this.nbSubSetsToChoose = k;
        this.subSets = subSets;
        this.optimal = Optional.empty();
        this.centralities = new double[nbItems];
        computeCentralities();
    }

    /**
     * Loads a MaxCover instance from a file.
     *
     * <p>
     * The file should contain the number of items, number of subsets, budget,
     * optional optimal value, and the item indices for each subset.
     *
     * @param fname path to the instance file
     * @throws IOException if the file cannot be read
     */
    public MaxCoverProblem(final String fname) throws IOException{
        final File f = new File(fname);
        int context = 0;
        int nElem = 0;
        int nSet = 0;
        int budget = 0;
        Optional<Double> optimal = Optional.empty();
        BitSet[] sets = null;
        int setCount = 0;
        String s;
        try (final BufferedReader br = new BufferedReader(new FileReader(f))) {
            while ((s = br.readLine()) != null) {
                if (context == 0) {
                    context++;

                    String[] tokens = s.split("\\s");
                    nElem = Integer.parseInt(tokens[0]);

                } else if (context == 1) {
                    context++;

                    String[] tokens = s.split("\\s");
                    nSet = Integer.parseInt(tokens[0]);
                    sets = new BitSet[nSet];

                } else if (context == 2) {
                    context++;
                    String[] tokens = s.split("\\s");
                    budget  = Integer.parseInt(tokens[0]);
                } else if (context == 3) {
                    context++;
                    if (!s.isBlank()) {
                        String[] tokens = s.split("\\s");
                        optimal = Optional.of(Double.parseDouble(tokens[0]));
                    }
                }
                else {
                    if (setCount< nSet) {
                        if (!s.isBlank()) {
                            String[] tokens = s.split("\\s");

                            sets[setCount] = new BitSet(nElem);
                            for (String token : tokens) {
                                sets[setCount].set(Integer.parseInt(token));
                            }
                            setCount++;
                        }
                    }
                }
            }
        }
        this.name = Optional.of("maxCoverage_" + nElem + "_" + nSet + "_" + budget);
        this.nbItems = nElem;
        this.nbSubSets = nSet;
        this.nbSubSetsToChoose = budget;
        this.subSets = sets;
        this.optimal = optimal;
        this.centralities = new double[nbItems];
        computeCentralities();
    }
    /**
     * Returns a formatted string representing the instance for writing to a file.
     *
     * @return the formatted instance as a string
     */
    public String instanceFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d%n%d%n%d%n", nbItems, nbSubSets, nbSubSetsToChoose));
        optimal.ifPresent(aDouble -> sb.append(String.format("%d%n", (int) Math.ceil(aDouble))));
        sb.append("\n");
        for (int i = 0; i < nbSubSets; i++) {
            for (int j = subSets[i].nextSetBit(0); j >= 0; j = subSets[i].nextSetBit(j + 1)) {
                sb.append(String.format("%d ", j));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
    /**
     * Returns the optimal value of the instance, if known.
     *
     * @return the negative of the optimal value (for minimization DDO)
     */
    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x );
    }

    /**
     * Returns the number of decision variables in the problem.
     *
     * @return number of variables (subsets to select)
     */
    @Override
    public int nbVars() {
        return nbSubSetsToChoose;
    }
    /**
     * Returns the initial state for the DDO search.
     *
     * @return a state with no items covered
     */
    @Override
    public MaxCoverState initialState() {
        return new MaxCoverState(new BitSet(nbItems));
    }
    /**
     * Returns the initial objective value for the initial state.
     *
     * @return 0 (no items covered yet)
     */
    @Override
    public double initialValue() {
        return 0;
    }
    /**
     * Returns an iterator over the domain of values for a given variable in a state.
     *
     * @param state the current state
     * @param var the variable index
     * @return an iterator over feasible subset indices (or -1 if no options)
     */
    @Override
    public Iterator<Integer> domain(MaxCoverState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (int i = 0; i < nbSubSets; i++) {
            BitSet ss = (BitSet) subSets[i].clone();
            if (!isInclude(ss, state.coveredItems())) {
                domain.add(i);
            }
        }
        if (domain.isEmpty())
            domain.add(-1);
        return domain.iterator();
    }
    /**
     * Applies a decision to a state to produce a new state.
     *
     * @param state the current state
     * @param decision the decision to apply
     * @return a new state reflecting the added subset
     */
    @Override
    public MaxCoverState transition(MaxCoverState state, Decision decision) {
        int val = decision.val();
        BitSet coveredItems = (BitSet) state.coveredItems().clone();
        if (val != -1)
            coveredItems.or(subSets[val]);
        return new MaxCoverState(coveredItems);
    }
    /**
     * Returns the cost of applying a decision to a state.
     *
     * <p>
     * Cost is defined as the negative number of newly covered items.
     *
     * @param state the current state
     * @param decision the decision to apply
     * @return the transition cost
     */
    @Override
    public double transitionCost(MaxCoverState state, Decision decision) {
        int val = decision.val();
        if (val == -1)
            return 0;
        BitSet coveredItems = (BitSet) state.coveredItems().clone();
        coveredItems.or(subSets[val]);
        coveredItems.andNot(state.coveredItems());
        return -coveredItems.cardinality();
    }
    /**
     * Evaluates a complete solution.
     *
     * @param solution array of selected subset indices
     * @return negative number of items covered
     * @throws InvalidSolutionException if the solution length is incorrect
     */
    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }
        BitSet coveredItems = new BitSet(nbItems);
        for (int selected: solution) {
            coveredItems.or(subSets[selected]);
        }

        return -coveredItems.cardinality();
    }

    private boolean isInclude(BitSet A, BitSet B) {
        BitSet temp = (BitSet) A.clone();
        temp.andNot(B);
        return temp.isEmpty();
    }

    private double distance(double[] x, double[] y, int i, int j) {
        double dx = x[j] - x[i];
        double dy = y[j] - y[i];
        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean isContained(BitSet[] subSets, int j) {
        for (int i = 0; i < subSets.length; i++) {
            if (subSets[i].get(j)) {
                return true;
            }
        }
        return false;
    }

    private int minItems(double[] x, double[] y, BitSet[] subSets, int j) {
        int min = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < subSets.length; i++) {
            double dist = distance(x, y, i, j);
            if (dist < minDistance) {
                minDistance = dist;
                min = i;
            }
        }
        return min;
    }


    /**
     * Returns a string representation of the instance.
     *
     * @return instance name, parameters, and subsets
     */
    @Override
    public String toString() {
        return name + " " + nbItems + " " + nbSubSets + " " + nbSubSetsToChoose + " " + Arrays.toString(subSets);
    }


    /**
     * Computes centralities of all items, i.e., the fraction of subsets that cover each item.
     */
    private void computeCentralities() {
        for (int i = 0; i < nbItems; i++) {
            double centrality = 0;
            for (int j = 0; j < nbSubSets; j++) {
                if (subSets[j].get(i)) {
                    centrality++;
                }
            }
            centralities[i] = centrality/nbSubSets;
        }
    }
}





