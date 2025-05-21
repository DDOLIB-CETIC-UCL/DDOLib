package org.ddolib.ddo.examples.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

public class SRFLPProblem implements Problem<SRFLPState> {

    public final int[] lengths;
    public final int[][] flows;
    public Optional<Integer> optimal;

    private Optional<String> name = Optional.empty();

    public SRFLPProblem(int[] lengths, int[][] flows, Optional<Integer> optimal) {
        this.lengths = lengths;
        this.flows = flows;
        this.optimal = optimal;
    }

    public SRFLPProblem(int[] lengths, int[][] flows) {
        this(lengths, flows, Optional.empty());
    }

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

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(SRFLPState state, int var) {
        BitSet toReturn = new BitSet(nbVars());
        toReturn.or(state.must());
        if (state.depth() + toReturn.cardinality() < nbVars()) {
            toReturn.or(state.maybe());
        }

        return toReturn.stream().iterator();
    }

    @Override
    public SRFLPState transition(SRFLPState state, Decision decision) {
        BitSet remaining = new BitSet(nbVars());
        BitSet newMaybe = new BitSet(nbVars());
        int[] newCut = new int[nbVars()];

        for (int i = state.must().nextSetBit(0); i >= 0; i = state.must().nextSetBit(i + 1)) {
            if (i != decision.val()) {
                remaining.set(i);
                newCut[i] = state.cut()[i] + flows[decision.val()][i];
            }
        }

        for (int i = state.maybe().nextSetBit(0); i >= 0; i = state.maybe().nextSetBit(i + 1)) {
            if (i != decision.val()) {
                newMaybe.set(i);
                newCut[i] = state.cut()[i] + flows[decision.val()][i];
            }
        }


        return new SRFLPState(remaining, newMaybe, newCut, state.depth() + 1);
    }

    @Override
    public int transitionCost(SRFLPState state, Decision decision) {
        int cut = 0;

        for (int i = state.must().nextSetBit(0); i >= 0; i = state.must().nextSetBit(i + 1)) {
            if (i != decision.val()) cut += state.cut()[i];
        }

        return -cut * lengths[decision.val()];
    }

    public double rootValue() {
        double toReturn = 0;
        for (int i = 0; i < nbVars(); i++) {
            for (int j = i + 1; j < nbVars(); j++) {
                toReturn += flows[i][j] * (lengths[i] + lengths[j]) * 0.5;
            }
        }
        return toReturn;
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
}
