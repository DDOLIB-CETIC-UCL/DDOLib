package org.ddolib.examples.ddo.pdp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class PDPInstance {

    final int n;
    final double[][] distanceMatrix;

    HashMap<Integer, Integer> pickupToAssociatedDelivery;
    HashMap<Integer, Integer> deliveryToAssociatedPickup;

    Set<Integer> unrelatedNodes;

    @Override
    public String toString() {
        return "PDPInstance(\n\tn:" + n + "\n" +
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

    public PDPInstance(final double[][] distanceMatrix, HashMap<Integer, Integer> pickupToAssociatedDelivery) {
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
}
