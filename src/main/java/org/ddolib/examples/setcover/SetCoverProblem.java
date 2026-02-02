package org.ddolib.examples.setcover;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SetCoverProblem implements Problem<SetCoverState> {
    final public int nItems;
    final public int nSet;
    final public List<Set<Integer>> constraints; // for each item, the collection of sets that contain this item
    final public BitSet[] sets;
    final public double[] weights;
    final public double[] itemMinWeights; // for each elem, the minimal weight among the set containing the item
    /** Optional instance name. */
    public Optional<String> name;
    /** Optional optimal solution value. */
    public Optional<Double> optimal;


    public SetCoverProblem(int nItems, int nSet, BitSet[] sets, double[] weights) {
        this.nItems = nItems;
        this.nSet = nSet;
        this.sets = sets;
        this.constraints = new ArrayList<>(nItems);
        this.weights = weights;
        this.itemMinWeights = new double[nItems];
        this.optimal = Optional.empty();

        for (int i = 0; i < nItems; i++) {
            double minWeight = Double.MAX_VALUE;
            constraints.add(new HashSet<>());
            for (int setIndex = 0; setIndex < nSet; setIndex++) {
                if (sets[setIndex].get(i)) {
                    constraints.get(i).add(setIndex);
                    minWeight = Math.min(minWeight, weights[setIndex]);
                }
            }
            itemMinWeights[i] = minWeight;

        }

    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }
        Set<Integer> coveredElements = new HashSet<>();
        double cost = 0.0;
        for (int set: solution) {
            if (set != -1) {
                coveredElements.addAll(getSetDefinition(set));
                cost += weights[set];
            }
        }

        if (coveredElements.size() < nItems) {
            String msg = "Some items are not covered";
            throw new InvalidSolutionException(msg);
        } else if (coveredElements.size() > nItems) {
            String msg = "Too much items are covered";
            throw new InvalidSolutionException(msg);
        }

        return cost;
    }

    @Override
    public int nbVars() {
        return nItems;
    }


    /**
     * The initial state contains all item in the universe
     * @return the initial state
     */
    @Override
    public SetCoverState initialState() {
        BitSet uncoveredElements = new BitSet(nItems);
        uncoveredElements.set(0, nItems);

        return new SetCoverState(uncoveredElements);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * If the considered item in var is uncovered in the given state,
     * then the domain contains all the sets that covers this item.
     * Else, no set is added to the solution
     * @param state the state from which the transitions should be applicable
     * @param var the variable whose domain in being queried
     * @return
     */
    @Override
    public Iterator<Integer> domain(SetCoverState state, int var) {
        if (state.uncoveredItems().get(var)) { // the item is uncovered
            return constraints.get(var).iterator();
        } else { // the item is already covered
            return List.of(-1).iterator();
        }
    }

    /**
     * If a set has been added to the solution, then all the items
     * that it contains are removed from the uncovered items.
     * Else the state is unchanged.
     * @param state the state from which the transition originates
     * @param decision the decision which is applied to `state`.
     * @return
     */
    @Override
    public SetCoverState transition(SetCoverState state, Decision decision) {
        if (decision.val() != -1) { // a set is added to the solution
            SetCoverState newState = new SetCoverState((BitSet) state.uncoveredItems().clone());
            newState.uncoveredItems().andNot(sets[decision.val()]);
            return newState;
        } else { // no set is added to the solution
            return new SetCoverState((BitSet) state.uncoveredItems().clone());
        }
    }

    @Override
    public double transitionCost(SetCoverState state, Decision decision) {
        if (decision.val() != -1) { // a set is added to the solution
            return weights[decision.val()];
        } else { // no set is added to the solution
            return 0;
        }
    }

    /**
     * @param index the index of the required set
     * @return a Set containing the items covered by the required set
     */
    public Set<Integer> getSetDefinition(int index) {
        Set<Integer> definition = new HashSet<>();
        for (int elem = 0; elem < nItems; elem++) {
            if (constraints.get(elem).contains(index)) {
                definition.add(elem);
            }
        }

        return definition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("nItems: ").append(nItems).append("\n");
        builder.append("nSet: ").append(nSet).append("\n");
        builder.append("constraints: ").append(constraints).append("\n");
        return builder.toString();
    }

    /**
     * Load the SetCoverProblem from a file
     * @param fname the path to the file describing the instance
     * @param weighted true if the instance has cost for the set, false otherwise
     * @return a SetCoverProblem representing the instance
     * @throws IOException if the file cannot be found or is not readable
     */
    public SetCoverProblem (final String fname, final boolean weighted) throws IOException {
        final File f = new File(fname);
        try (final BufferedReader br = new BufferedReader(new FileReader(f))) {

            boolean isFirst = true;
            boolean isSecond = false;
            int nItems = 0;
            int nSet = 0;
            BitSet[] sets = new BitSet[0];
            double[] weights = new double[0];
            int count = 0;
            String s;
            this.optimal = Optional.empty();
            while ((s = br.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    isSecond = true;

                    String[] tokens = s.split("\\s");
                    nItems = Integer.parseInt(tokens[0]);
                    nSet = Integer.parseInt(tokens[1]);
                    if (tokens.length == 3) {
                        optimal = Optional.of(Double.parseDouble(tokens[2]));
                    }

                    sets = new BitSet[nSet];
                    weights = new double[nSet];
                } else if (isSecond && weighted) {
                    isSecond = false;
                    String[] tokens = s.split("\\s");
                    for (int i = 0; i < nSet; i++) {
                        weights[i] = Double.parseDouble(tokens[i]);
                    }
                }
                else {
                    if (count< nSet) {
                        String[] tokens = s.split("\\s");
                        BitSet newSet = new BitSet(nItems);
                        for (String token: tokens) {
                            newSet.set(Integer.parseInt(token));
                        }
                        sets[count] = newSet;
                        count++;
                    }
                }
            }
            if (!weighted) {
                Arrays.fill(weights, 1.0);
            }
            this.nItems = nItems;
            this.nSet = nSet;
            this.sets = sets;
            this.weights = weights;
            this.name = Optional.of(f.getName());

            this.constraints = new ArrayList<>(nSet);
            this.itemMinWeights = new double[nItems];
            for (int i = 0; i < nItems; i++) {
                double minWeight = Double.MAX_VALUE;
                constraints.add(new HashSet<>());
                for (int setIndex = 0; setIndex < nSet; setIndex++) {
                    if (sets[setIndex].get(i)) {
                        constraints.get(i).add(setIndex);
                        minWeight = Math.min(minWeight, weights[setIndex]);
                    }
                }
                itemMinWeights[i] = (minWeight);

            }

        }
    }
}
