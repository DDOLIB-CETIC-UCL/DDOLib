package org.ddolib.ddo.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;
import java.util.stream.IntStream;

public class PDPProblem implements Problem<PDPState> {
    final int n;
    final double[][] distanceMatrix;

    HashMap<Integer, Integer> pickupToAssociatedDelivery;
    HashMap<Integer, Integer> deliveryToAssociatedPickup;

    Set<Integer> unrelatedNodes;

    @Override
    public String toString() {
        return "PDProblem(\n\tn:" + n + "\n" +
                "\tpdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> p + "->" + pickupToAssociatedDelivery.get(p)).toList() + "\n" +
                "\tunrelated:" + unrelatedNodes.stream().toList() + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList();
    }

    public double eval(int[] solution) {
        double toReturn = 0;
        for (int i = 1; i < solution.length; i++) {
            toReturn = toReturn + distanceMatrix[solution[i - 1]][solution[i]];
        }
        toReturn = toReturn + distanceMatrix[solution[solution.length - 1]][0]; //final come back
        return toReturn;
    }

    public PDPProblem(final double[][] distanceMatrix, HashMap<Integer, Integer> pickupToAssociatedDelivery) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;

        this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
        this.unrelatedNodes = new HashSet<Integer>(IntStream.range(0, n).boxed().toList());

        deliveryToAssociatedPickup = new HashMap<>();
        for (int p : pickupToAssociatedDelivery.keySet()) {
            int d = pickupToAssociatedDelivery.get(p);
            unrelatedNodes.remove(p);
            unrelatedNodes.remove(d);
            deliveryToAssociatedPickup.put(d, p);
        }
    }

    @Override
    public int nbVars() {
        return n; //the last decision will be to come back to point zero
    }

    @Override
    public PDPState initialState() {
        BitSet openToVisit = new BitSet(n);
        openToVisit.set(1, n);

        for (int p : pickupToAssociatedDelivery.keySet()) {
            openToVisit.clear(pickupToAssociatedDelivery.get(p));
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
        if(var == n-1) {
            //the final decision is to come back to node zero
            return singleton(0).stream().iterator();
        }else{
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

        if (pickupToAssociatedDelivery.containsKey(node)) {
            newOpenToVisit.set(pickupToAssociatedDelivery.get(node));
        }

        if (deliveryToAssociatedPickup.containsKey(node)) {
            int p = deliveryToAssociatedPickup.get(node);
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
                .mapToDouble(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsDouble();
    }
}
