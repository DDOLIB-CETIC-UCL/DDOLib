package org.ddolib.examples.ddo.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to model the SRFLP.
 */
public class SRFLPProblem implements Problem<SRFLPState> {

    final int[] lengths;
    final int[][] flows;
    /**
     * The optimal solution of this instance if known (used for tests).
     */
    private Optional<Double> optimal;

    private Optional<String> name = Optional.empty();

    private Double rootValue;

    /**
     * Constructs a new instance of the SRFLP.
     *
     * @param lengths The length of each department to place.
     * @param flows   The traffic flow between each pair of departments. It is assumed to be symmetrical.
     * @param optimal The optimal solution of this instance if known (used for tests).
     */
    public SRFLPProblem(int[] lengths, int[][] flows, Optional<Double> optimal) {
        this.lengths = lengths;
        this.flows = flows;
        this.optimal = optimal;

        for (int i = 0; i < nbVars(); i++) {
            for (int j = i + 1; j < nbVars(); j++) {
                if (flows[i][j] != flows[j][i]) {
                    throw new IllegalArgumentException("flows matrix is not symmetric");
                }
            }
        }
    }

    /**
     * Constructs a new instance of the SRFLP.
     *
     * @param lengths The length of each department to place.
     * @param flows   The traffic flow between each pair of departments. It is assumed to be symmetrical.
     */
    public SRFLPProblem(int[] lengths, int[][] flows) {
        this(lengths, flows, Optional.empty());
    }

    /**
     * Used to replace the default {@code toString()} value.
     *
     * @param name A descriptive of the instance. It will be used instead of the default {@code toString()} value.
     */
    public void setName(String name) {
        this.name = Optional.of(name);
    }


    @Override
    public int nbVars() {
        return lengths.length;
    }

    @Override
    public SRFLPState initialState() {
        BitSet all = new BitSet(nbVars());
        all.set(0, nbVars());
        return new SRFLPState(all, new BitSet(nbVars()), new int[nbVars()], 0);
    }

    /**
     * Returns a constant accounting for all contributions of half department lengths.
     *
     * @return The problem's initial value
     */
    @Override
    public double initialValue() {
        if (rootValue == null) {
            rootValue = 0.0;
            for (int i = 0; i < nbVars(); i++) {
                for (int j = i + 1; j < nbVars(); j++) {
                    rootValue += flows[i][j] * (lengths[i] + lengths[j]) * 0.5;
                }
            }
        }
        return rootValue;
    }

    @Override
    public Iterator<Integer> domain(SRFLPState state, int var) {
        BitSet toReturn = new BitSet(nbVars());
        toReturn.or(state.must());
        if (state.depth() + toReturn.cardinality() < nbVars()) {
            //If there is not enough  "must node" to reach the end of the diagram we can select
            // some nodes from
            toReturn.or(state.maybe());
        }

        return toReturn.stream().iterator();
    }

    @Override
    public SRFLPState transition(SRFLPState state, Decision decision) {
        BitSet newMust = new BitSet(nbVars());
        BitSet newMaybe = new BitSet(nbVars());
        int[] newCut = new int[nbVars()];

        for (int i = state.must().nextSetBit(0); i >= 0; i = state.must().nextSetBit(i + 1)) {
            if (i != decision.val()) {
                newMust.set(i);
                newCut[i] = state.cut()[i] + flows[decision.val()][i];
            }
        }

        for (int i = state.maybe().nextSetBit(0); i >= 0; i = state.maybe().nextSetBit(i + 1)) {
            if (i != decision.val()) {
                newMaybe.set(i);
                newCut[i] = state.cut()[i] + flows[decision.val()][i];
            }
        }


        return new SRFLPState(newMust, newMaybe, newCut, state.depth() + 1);
    }

    @Override
    public double transitionCost(SRFLPState state, Decision decision) {
        int cut = 0;
        int complete = nbVars() - (state.depth() + 1);
        for (int i = state.must().nextSetBit(0); i >= 0; i = state.must().nextSetBit(i + 1)) {
            if (i != decision.val()) {
                cut += state.cut()[i];
                complete--;
            }
        }

        if (complete > 0) {
            // The current state is a relaxed state or has a relaxed state among its ancestors.
            // The complete the solution we need to pick up some department from the "maybe" set.
            // As we are compiling a relaxed mdd, we want to get a lower bound on the optimal
            // solution. So we assume that we will select the department with the smallest cut
            // values.
            ArrayList<Integer> maybesCut = new ArrayList<>();
            for (int i = state.maybe().nextSetBit(0); i >= 0; i = state.maybe().nextSetBit(i + 1)) {
                if (i != decision.val()) maybesCut.add(i);
            }

            Collections.sort(maybesCut);
            cut += maybesCut.subList(0, complete).stream().mapToInt(x -> x).sum();
        }

        return cut * lengths[decision.val()];
    }

    @Override
    public String toString() {
        if (name.isPresent()) {
            return name.get();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("SRFLP Problem:\n");
            String lengthsStr = Arrays.stream(lengths)
                    .mapToObj(x -> String.format("%2s", x))
                    .collect(Collectors.joining(", "));
            sb.append("Lengths: ").append(lengthsStr).append("\n");
            String flowStr = Arrays.stream(flows)
                    .map(row -> Arrays.stream(row)
                            .mapToObj(x -> String.format("%2s", x))
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining("\n"));
            sb.append("Flows: \n").append(flowStr);

            return sb.toString();
        }

    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }
}
