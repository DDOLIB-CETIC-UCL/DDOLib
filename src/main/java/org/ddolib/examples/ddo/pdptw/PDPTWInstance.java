package org.ddolib.examples.ddo.pdptw;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class PDPTWInstance {

    final int n;
    final int[][] timeAndDistanceMatrix;
    final TimeWindow[] timeWindows;
    final int maxCapa;

    HashMap<Integer, Integer> pickupToAssociatedDelivery;
    HashMap<Integer, Integer> deliveryToAssociatedPickup;

    Set<Integer> unrelatedNodes;

    @Override
    public String toString() {
        return "PDPTWInstance(\n\tn:" + n + "\n" +
                "\tpdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> p + "->" + pickupToAssociatedDelivery.get(p)).toList() + "\n" +
                "\tunrelated:" + unrelatedNodes.stream().toList() + "\n" +
                "\t" + Arrays.stream(timeAndDistanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList();
    }

    public int eval(int[] solution) {
        int vehicleContent = 0;
        int distance = 0;
        int currentTime = timeWindows[0].start();
        for (int i = 1; i < solution.length; i++) {
            distance = distance + timeAndDistanceMatrix[solution[i - 1]][solution[i]];
            if(pickupToAssociatedDelivery.containsKey(solution[i])) {
                vehicleContent += 1;
            }else if (deliveryToAssociatedPickup.containsKey(solution[i])){
                vehicleContent -= 1;
            }
            if(vehicleContent > maxCapa) {
                return -1;
            }
            TimeWindow window = timeWindows[solution[i]];
            currentTime += timeAndDistanceMatrix[solution[i - 1]][solution[i]];
            if(currentTime > window.end()){
                return -1;
            }
            if(currentTime <= window.start()) {
                currentTime = window.start();
            }
        }
        currentTime += timeAndDistanceMatrix[solution[solution.length - 1]][0];
        if(currentTime > timeWindows[0].end()) {
            return -1;
        }
        distance = distance + timeAndDistanceMatrix[solution[solution.length - 1]][0]; //final come back
        if(vehicleContent !=0){
            return -1;
        }
        return distance;
    }

    public PDPTWInstance(final int[][] distanceMatrix,
                         HashMap<Integer, Integer> pickupToAssociatedDelivery,
                         int maxCapa, TimeWindow[] timeWindows) {
        this.timeAndDistanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
        this.timeWindows = timeWindows;
        this.maxCapa = maxCapa;

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


