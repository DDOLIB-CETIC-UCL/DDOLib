package org.ddolib.examples.pdptw;

import org.ddolib.common.solver.Solution;

public class PDPTWSolution {
    PDPTWProblem problem;
    public int[] solution;
    public double value;

    public PDPTWSolution(PDPTWProblem problem, Solution solution, double value) {
        this.problem = problem;
        this.solution = solution.solution();
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        try {
            toReturn.append("eval from scratch: " + problem.evaluate(solution) + "\n");
        }catch(Exception e) {
            toReturn.append(e.getMessage() + "\n");
        }
        toReturn.append("0\tcontent:" + 0);

        int currentNode = 0;
        int currentContent = 0;
        double currentTime = problem.timeWindows[0].start();

        for (int i = 0; i < solution.length; i++) {
            int prevNode = currentNode;
            currentNode = solution[i];
            currentTime = currentTime + problem.timeMatrix[prevNode][currentNode];
            toReturn.append("\ntravelTime:" + problem.timeMatrix[prevNode][currentNode]);
            double earlyLine = problem.timeWindows[currentNode].start();
            double waitTime = 0;
            if(currentTime < earlyLine) {
                waitTime = earlyLine - currentTime;
                currentTime = earlyLine;
            }
            if (problem.deliveryToAssociatedPickup.containsKey(currentNode)) {
                //it is a delivery
                currentContent = currentContent - 1;
                toReturn.append("\n" + currentNode + " \tcontentOut:" + currentContent + "\ttime:" + currentTime + "\twaitTime:" + waitTime + "\t(delivery from " + problem.deliveryToAssociatedPickup.get(currentNode) + " -" + 1 + ")");
            } else if (problem.pickupToAssociatedDelivery.containsKey(currentNode)) {
                // it is a pickup
                currentContent = currentContent + 1;
                toReturn.append("\n" + currentNode + "\tcontentOut:" + currentContent + "\ttime:" + currentTime  + "\twaitTime:" + waitTime+ "\t(pickup to " + problem.pickupToAssociatedDelivery.get(currentNode) + " +" + 1 + ")");
            } else {
                //an unrelated node
                toReturn.append("\n" + currentNode + "\tcontent:" + currentContent + "\ttime:" + currentTime + "\twaitTime:" + waitTime);
            }
        }
        return toReturn.toString();
    }
}
