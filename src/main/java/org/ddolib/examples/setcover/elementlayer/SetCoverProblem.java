package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SetCoverProblem implements Problem<SetCoverState> {
    final public int nElem;
    final public int nSet;
    final public List<Set<Integer>> constraints; // for each element, the collection of sets that contain this element
    final public Optional<Double> optimal = Optional.empty();
    final public List<Double> weights;
    final public List<Double> elemMinWeights; // for each elem, the minimal weight among the set containing the element

    public SetCoverProblem(int nElem, int nSet, List<Set<Integer>> constraints, List<Double> weights) {
        this.nElem = nElem;
        this.nSet = nSet;
        this.constraints = constraints;
        this.weights = weights;
        this.elemMinWeights = new ArrayList<>(nElem);

        for (int i = 0; i < nElem; i++) {
            double minWeight = Double.MAX_VALUE;
            for (int set : constraints.get(i)) {
                minWeight = Math.min(minWeight, weights.get(set));
            }
            elemMinWeights.add(minWeight);
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
        double cost = 0;
        Set<Integer> coveredElements = new HashSet<>();
        for (int decision: solution) {
            if (decision != -1) {
                cost += weights.get(decision);
                coveredElements.addAll(getSetDefinition(decision));
            }
        }
        if (coveredElements.size() != nElem) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all the elements",
                    Arrays.toString(solution)));
        }

        return -cost;
    }

    @Override
    public int nbVars() {
        return nElem;
    }


    /**
     * The initial state contains all element in the universe
     * @return the initial state
     */
    @Override
    public SetCoverState initialState() {
        Set<Integer> uncoveredElements = new HashSet<>();
        for (int i = 0; i < nElem; i++) {
            uncoveredElements.add(i);
        }
        return new SetCoverState(uncoveredElements);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * If the considered element in var is uncovered in the given state,
     * then the domain contains all the sets that covers this element.
     * Else, no set is added to the solution
     * @param state the state from which the transitions should be applicable
     * @param var the variable whose domain in being queried
     * @return
     */
    @Override
    public Iterator<Integer> domain(SetCoverState state, int var) {
        if (state.uncoveredElements.contains(var)) { // the element is uncovered
            return constraints.get(var).iterator();
        } else { // the element is already covered
            return List.of(-1).iterator();
        }
    }

    /**
     * If a set has been added to the solution, then all the elements
     * that it contains are removed from the uncovered elements.
     * Else the state is unchanged.
     * @param state the state from which the transition originates
     * @param decision the decision which is applied to `state`.
     * @return
     */
    @Override
    public SetCoverState transition(SetCoverState state, Decision decision) {
        if (decision.val() != -1) { // a set is added to the solution
            SetCoverState newState = state.clone();
            for (int element: state.uncoveredElements) {
                if (constraints.get(element).contains(decision.val())) {
                    newState.uncoveredElements.remove(element);
                }
            }
            return newState;
        } else { // no set is added to the solution
            return state.clone();
        }
    }

    @Override
    public double transitionCost(SetCoverState state, Decision decision) {
        if (decision.val() != -1) { // a set is added to the solution
            return weights.get(decision.val());
        } else { // no set is added to the solution
            return 0;
        }
    }

    /**
     * @param index the index of the required set
     * @return a Set containing the elements covered by the required set
     */
    public Set<Integer> getSetDefinition(int index) {
        Set<Integer> definition = new HashSet<>();
        for (int elem = 0; elem < nElem; elem++) {
            if (constraints.get(elem).contains(index)) {
                definition.add(elem);
            }
        }

        return definition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("nElem: ").append(nElem).append("\n");
        builder.append("nSet: ").append(nSet).append("\n");
        builder.append("constraints: ").append(constraints).append("\n");
        return builder.toString();
    }

    /**
     * The instance file contains the definition of each set, i.e. the element that it covers.
     * For this model we instead need, for each element, a description of the collection of
     * sets that cover it, i.e. the description of each constraint.
     * This method makes the required conversion.
     * @param sets the collection of sets from the instance file
     * @param nElem the number of element in the universe
     * @return the collection of constraints
     */
    private static List<Set<Integer>> convertSetsToConstraints(List<Set<Integer>> sets, int nElem) {
        List<Set<Integer>> constraints = new ArrayList<>(nElem);
        for (int elem = 0; elem < nElem; elem++) {
            constraints.add(new HashSet<>());
            for (int set = 0; set < sets.size(); set++) {
                if (sets.get(set).contains(elem)) {
                    constraints.get(elem).add(set);
                }
            }
        }
        return constraints;
    }

    /**
     * Load the SetCoverProblem from a file
     * @param fname the path to the file describing the instance
     * @return a SetCoverProblem representing the instance
     * @throws IOException if the file cannot be found or is not readable
     */
    public SetCoverProblem(final String fname) throws IOException {
        this(fname, false);
    }

    /**
     * Load the SetCoverProblem from a file
     * @param fname the path to the file describing the instance
     * @param weighted true if the instance has cost for the set, false otherwise
     * @return a SetCoverProblem representing the instance
     * @throws IOException if the file cannot be found or is not readable
     */
    public SetCoverProblem(final String fname, final boolean weighted) throws IOException {
        final File f = new File(fname);
        int context = 0;
        int nElem = 0;
        int nSet = 0;
        List<Set<Integer>> sets = null;
        List<Double> weights = null;
        int setCount = 0;
        String s;
        try (final BufferedReader br = new BufferedReader(new FileReader(f))) {
            while ((s = br.readLine()) != null) {
                if (context == 0) {
                    context++;

                    String[] tokens = s.split("\\s");
                    nElem = Integer.parseInt(tokens[0]);
                    nSet = Integer.parseInt(tokens[1]);

                    sets = new ArrayList<>(nSet);
                } else if (context == 1 && weighted) {
                    context++;
                    String[] tokens = s.split("\\s");
                    weights = new ArrayList<>(nSet);
                    for (int i = 0; i < nSet; i++) {
                        weights.add(Double.parseDouble(tokens[i]));
                    }
                }
                else {
                    if (setCount < nSet) {
                        String[] tokens = s.split("\\s");
                        sets.add(new HashSet<>(tokens.length));
                        for (String token : tokens) {
                            sets.get(setCount).add(Integer.parseInt(token));
                        }
                        setCount++;
                    }
                }
            }
            if (!weighted) {
                weights = new ArrayList<>(nSet);
                for (int i = 0; i < nSet; i++) {
                    weights.add(1.0);
                }
            }
            this.nElem = nElem;
            this.nSet = nSet;
            this.constraints = convertSetsToConstraints(sets, nElem);
            this.weights = weights;

            this.elemMinWeights = new ArrayList<>(nElem);
            for (int i = 0; i < nElem; i++) {
                double minWeight = Double.MAX_VALUE;
                for (int set : constraints.get(i)) {
                    minWeight = Math.min(minWeight, weights.get(set));
                }
                elemMinWeights.add(minWeight);
            }
        }
    }
}
