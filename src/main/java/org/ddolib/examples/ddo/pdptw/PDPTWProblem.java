package org.ddolib.examples.ddo.pdptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;

public class PDPTWProblem implements Problem<PDPTWState> {
    PDPTWInstance instance;
    public int n;

    public PDPTWProblem(PDPTWInstance instance) {
        this.instance = instance;
        this.n = instance.n;
    }

    @Override
    public int nbVars() {
        return instance.n; //the last decision will be to come back to point zero
    }

    @Override
    public PDPTWState initialState() {
        BitSet openToVisit = new BitSet(n);
        openToVisit.set(1, n);

        for (int p : instance.pickupToAssociatedDelivery.keySet()) {
            openToVisit.clear(instance.pickupToAssociatedDelivery.get(p));
        }

        BitSet allToVisit = new BitSet(n);
        allToVisit.set(1, n);

        return new PDPTWState(singleton(0), openToVisit, allToVisit,0,0,
                instance.timeWindows[0].start());
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
    public Iterator<Integer> domain(PDPTWState state, int var) {
        if (var == n - 1) {
            //the final decision is to come back to node zero
            //only if we are before the deadline of node0
            if(state.currentTime
                    + state.current.stream().map(from -> instance.timeAndDistanceMatrix[from][0]).max().getAsInt()
                    > instance.timeWindows[0].end()) {
                return Collections.emptyIterator();
            }else{
                return singleton(0).stream().iterator();
            }
        } else {

            boolean canIncludePickups = state.minContent < instance.maxCapa;
            boolean canIncludeDeliveries = state.maxContent !=0;

            //how many we need to visit from now on?
            int howManyToVisit = n - 1 - var;

            //check that all states that must be visited can still be visited given the currentTime,
            // otherwise, there is no successor at all.
            long nbStillReachablePoints = state.allToVisit.stream().filter(point ->
                    (state.currentTime + state.current.stream().map(
                            from -> instance.timeAndDistanceMatrix[from][point]).min().getAsInt()) <= instance.timeWindows[point].end()
            ).count();

            if(nbStillReachablePoints < howManyToVisit) return Collections.emptyIterator();

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
    public PDPTWState transition(PDPTWState state, Decision decision) {
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

        if(newMinContent > instance.maxCapa) throw new Error("error");
        if(newMaxContent < 0) throw new Error("error");

        int arrivalTime = state.currentTime + state.current.stream()
                .map(possibleCurrentNode -> instance.timeAndDistanceMatrix[possibleCurrentNode][decision.val()])
                .min().getAsInt();

        if(arrivalTime < instance.timeWindows[node].start()){
            arrivalTime = instance.timeWindows[node].start();
        }

        return new PDPTWState(
                state.singleton(node),
                newOpenToVisit,
                newAllToVisit,
                newMinContent,
                newMaxContent,
                arrivalTime);
    }

    @Override
    public double transitionCost(PDPTWState state, Decision decision) {
        int travelTime= state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .map(
                        possibleCurrentNode -> instance.timeAndDistanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsInt();

        int waitTime = instance.timeWindows[decision.val()].waitTime(state.currentTime + travelTime);
        return travelTime + waitTime;
    }

    @Override
    public String toString() {
        return instance.toString();
    }
}
