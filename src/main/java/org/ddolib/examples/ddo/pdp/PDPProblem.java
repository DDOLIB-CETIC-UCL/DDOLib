package org.ddolib.examples.ddo.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;

public class PDPProblem implements Problem<PDPState> {
    PDPInstance instance;
    public int n;
    private Optional<Double> optimal;

    /**
     * A name to ease the readability of the tests.
     */
    private Optional<String> name = Optional.empty();

    public PDPProblem(PDPInstance instance) {
        this.instance = instance;
        this.n = instance.n;
        this.optimal = instance.optimal == -1 ? Optional.empty() : Optional.of(instance.optimal);
    }

    @Override
    public int nbVars() {
        return instance.n; //the last decision will be to come back to point zero
    }

    @Override
    public PDPState initialState() {
        BitSet openToVisit = new BitSet(n);
        openToVisit.set(1, n);

        for (int p : instance.pickupToAssociatedDelivery.keySet()) {
            openToVisit.clear(instance.pickupToAssociatedDelivery.get(p));
        }

        BitSet allToVisit = new BitSet(n);
        allToVisit.set(1, n);

        return new PDPState(singleton(0), openToVisit, allToVisit);
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(n);
        toReturn.set(singletonValue);
        return toReturn;
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(PDPState state, int var) {
        if (var == n - 1) {
            //the final decision is to come back to node zero
            return singleton(0).stream().iterator();
        } else {
            ArrayList<Integer> domain = new ArrayList<>(state.openToVisit.stream().boxed().toList());
            return domain.iterator();
        }
    }

    @Override
    public PDPState transition(PDPState state, Decision decision) {
        int node = decision.val();
        BitSet newOpenToVisit = (BitSet) state.openToVisit.clone();
        newOpenToVisit.clear(node);

        BitSet newAllToVisit = (BitSet) state.allToVisit.clone();
        newAllToVisit.clear(node);

        if (instance.pickupToAssociatedDelivery.containsKey(node)) {
            newOpenToVisit.set(instance.pickupToAssociatedDelivery.get(node));
        }

        if (instance.deliveryToAssociatedPickup.containsKey(node)) {
            int p = instance.deliveryToAssociatedPickup.get(node);
            if (newOpenToVisit.get(p)) {
                newOpenToVisit.clear(p);
            }
        }

        return new PDPState(
                state.singleton(node),
                newOpenToVisit,
                newAllToVisit);
    }

    @Override
    public double transitionCost(PDPState state, Decision decision) {
        return -state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .mapToDouble(possibleCurrentNode -> instance.distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsDouble();
    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public String toString() {
        return name.orElse(instance.toString());
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x);
    }
}
