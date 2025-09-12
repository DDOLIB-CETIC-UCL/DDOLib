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

    final int knownSolutionValue;

    HashMap<Integer, Integer> pickupToAssociatedDelivery;
    HashMap<Integer, Integer> deliveryToAssociatedPickup;

    Set<Integer> unrelatedNodes;

    public PDPTWInstance(final int[][] distanceMatrix,
                         HashMap<Integer, Integer> pickupToAssociatedDelivery,
                         int maxCapa, TimeWindow[] timeWindows, int knownSolutionValue) {
        this.timeAndDistanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
        this.timeWindows = timeWindows;
        this.maxCapa = maxCapa;
        this.knownSolutionValue = knownSolutionValue;

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
    public String toString() {
        return "PDPTWInstance(\n\tn:" + n + "\n" +
                "\tknownSolutionValue:" + knownSolutionValue  + "\n" +
                "\tpdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> p + "->" + pickupToAssociatedDelivery.get(p)).toList() + "\n" +
                "\tmaxCapa:" + maxCapa + "\n" +
                "\tunrelated:" + unrelatedNodes.stream().toList() + "\n" +
                "\ttimeWindows" + Arrays.stream(timeWindows).map(l -> "\n\t " + l).toList() + "\n" +
                "\t" + Arrays.stream(timeAndDistanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList();
    }

    public int eval(int[] solution) {
        int vehicleContent = 0;
        int distance = 0;
        int currentTime = timeWindows[0].start();
        for (int i = 1; i < solution.length-1; i++) { //zero is in the solution as well
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
                System.out.println("currentTime:" + currentTime + " node:" + solution[i] + " trw:" + window);
                return -2;
            }
            if(currentTime <= window.start()) {
                currentTime = window.start();
            }
        }
        currentTime += timeAndDistanceMatrix[solution[solution.length - 2]][0]; //final come back
        distance += timeAndDistanceMatrix[solution[solution.length - 2]][0]; //final come back
        if(currentTime > timeWindows[0].end()) {
            System.out.println("currentTime:" + currentTime + " come back ToZero trw:" + timeWindows[0]);
            return -3;
        }
        if(vehicleContent !=0){
            return -4;
        }
        return currentTime;
    }
}


