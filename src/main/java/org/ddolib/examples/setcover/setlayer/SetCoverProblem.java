package org.ddolib.examples.setcover.setlayer;

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
    final public List<Set<Integer>> sets;
    public int nbrElemRemoved;
    final public Optional<Double> optimal = Optional.empty();
    final public List<Double> weights;
    // Used to monitor how many times each type of branching is used
    public int countZeroOnly;
    public int countOneOnly;
    public int countZeroOne;
    final public List<Double> elemMinWeights; // for each elem, the minimal weight among the set containing the element


    public SetCoverProblem(int nElem, int nSet, List<Set<Integer>> sets, List<Double> weights) {
        this.nElem = nElem;
        this.nSet = nSet;
        this.sets = sets;
        this.weights = weights;
        this.nbrElemRemoved = nbrElemRemoved;
        this.elemMinWeights = new ArrayList<>(nElem);
        for (int i = 0; i < nElem; i++) {
            elemMinWeights.add(Double.MAX_VALUE);
        }
        for (int set = 0; set < nSet; set++) {
            for (Integer elem : sets.get(set)) {
                elemMinWeights.set(elem, Math.min(elemMinWeights.get(elem), weights.get(set)));
            }
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
                coveredElements.addAll(sets.get(decision));
            }
        }
        if (coveredElements.size() != nElem) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all the elements",
                    Arrays.toString(solution)));
        }

        return -cost;
    }

    /**
     *
     * @param nbrElemRemoved
     */
    public void setNbrElemRemoved(int nbrElemRemoved) {
        this.nbrElemRemoved = nbrElemRemoved;
    }

    @Override
    public int nbVars() {
        return sets.size();
    }

    @Override
    public SetCoverState initialState() {
        Map<Integer, Integer> uncoveredElements = new HashMap<>();
        for(int i = 0; i < nElem; i++) {
            uncoveredElements.put(i, 0);
        }
        for (Set<Integer> set : sets) {
            for (Integer element : set) {
                uncoveredElements.replace(element, uncoveredElements.get(element) + 1);
            }
        }
        return new SetCoverState(uncoveredElements);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(SetCoverState state, int var) {
        // System.out.println("var: " + sets[var]);
        // If the considered set is useless, it cannot be taken

        if (Collections.disjoint(state.uncoveredElements.keySet(), sets.get(var))) {
            countZeroOnly++;
            return List.of(0).iterator();
        } else {
            // If the considered set is the last one that can cover a particular element,
            // it is forced to be selected
            for (Integer elem: state.uncoveredElements.keySet()) {
                if (state.uncoveredElements.get(elem) == 1 && sets.get(var).contains(elem)) {
                    countOneOnly++;
                    return List.of(1).iterator();
                }
            }
            countZeroOne++;
            return Arrays.asList(1, 0).iterator();
        }
    }

    @Override
    public SetCoverState transition(SetCoverState state, Decision decision) {
        if (decision.val() == 1) {
                /*System.out.println("Computing transition");
                System.out.println("Initial State: " + state);
                System.out.println("Set: " + sets[decision.var()]);*/
            SetCoverState newState = state.clone();
            for (Integer element: sets.get(decision.var())) {
                newState.uncoveredElements.remove(element);
            }
            // System.out.println("New State: " + newState);
            return newState;
        } else {
            SetCoverState newState = state.clone();
            for (Integer element: sets.get(decision.var())) {
                if (newState.uncoveredElements.containsKey(element)) {
                    newState.uncoveredElements.replace(element, newState.uncoveredElements.get(element) - 1);
                }
            }
            return newState;
        }
    }

    @Override
    public double transitionCost(SetCoverState state, Decision decision) {
        return decision.val()*weights.get(decision.var());
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
            this.sets = sets;
            this.weights = weights;

            this.elemMinWeights = new ArrayList<>(nElem);
            for (int i = 0; i < nElem; i++) {
                double minWeight = Double.MAX_VALUE;
                for (int set : sets.get(i)) {
                    minWeight = Math.min(minWeight, weights.get(set));
                }
                elemMinWeights.add(minWeight);
            }
        }
    }
}
