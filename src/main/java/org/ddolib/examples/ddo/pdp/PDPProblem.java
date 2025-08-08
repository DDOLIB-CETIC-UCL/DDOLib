package org.ddolib.examples.ddo.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class PDPProblem implements Problem<PDPState> {
    PDPInstance instance;
    public int n;

    public PDPProblem(PDPInstance instance) {
        this.instance = instance;
        this.n = instance.n;
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

        return new PDPState(singleton(0), openToVisit, allToVisit,0,0);
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

            boolean canIncludePickups = state.minContent < instance.maxCapa;
            boolean canIncludeDeliveries = state.maxContent !=0;

            return state
                    .openToVisit
                    .stream()
                    .filter(point ->
                            ((canIncludePickups | !instance.pickupToAssociatedDelivery.containsKey(point))
                                    && (canIncludeDeliveries | ! instance.deliveryToAssociatedPickup.containsKey(point))))
                    .boxed()
                    .iterator();
        }
    }

    @Override
    public PDPState transition(PDPState state, Decision decision) {
        int node = decision.val();
        BitSet newOpenToVisit = (BitSet) state.openToVisit.clone();
        newOpenToVisit.clear(node);

        BitSet newAllToVisit = (BitSet) state.allToVisit.clone();
        newAllToVisit.clear(node);
        int newMinContent = state.minContent;
        int newMaxContent = state.maxContent;
        if (instance.pickupToAssociatedDelivery.containsKey(node)) {
            newOpenToVisit.set(instance.pickupToAssociatedDelivery.get(node));
            newMinContent += 1;
            newMaxContent += 1;
        }

        if (instance.deliveryToAssociatedPickup.containsKey(node)) {
            int p = instance.deliveryToAssociatedPickup.get(node);
            if (newOpenToVisit.get(p)) {
                newOpenToVisit.clear(p);
            }
            newMinContent -= 1;
            newMaxContent -= 1;
        }

        if(newMinContent <0) newMinContent = 0;
        if(newMaxContent > instance.maxCapa) newMaxContent = instance.maxCapa;
        return new PDPState(
                state.singleton(node),
                newOpenToVisit,
                newAllToVisit,
                newMinContent,
                newMaxContent);
    }

    @Override
    public double transitionCost(PDPState state, Decision decision) {
        return -state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .mapToDouble(possibleCurrentNode -> instance.distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsDouble();
    }

    @Override
    public String toString() {
        return instance.toString();
    }
}
