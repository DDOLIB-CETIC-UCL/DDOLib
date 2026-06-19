package org.ddolib.examples.layered.pdptw;

import org.ddolib.common.solver.Solution;

/**
 * Pretty-printer wrapper for PDPTW solutions.
 */
public class PDPTWSolution {
    public int[] solution;
    public double value;
    PDPTWProblem problem;

    public PDPTWSolution(PDPTWProblem problem, Solution solution, double value) {
        this.problem = problem;
        this.solution = solution.solution();
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        try {
            toReturn.append("eval from scratch: ").append(problem.evaluate(solution)).append("\n");
        } catch (Exception e) {
            toReturn.append(e.getMessage()).append("\n");
        }
        toReturn.append("0\tcontent:" + 0);

        int currentNode = 0;
        int currentContent = 0;
        double currentTime = problem.timeWindows[0].start();

        for (int j : solution) {
            int prevNode = currentNode;
            currentNode = j;
            currentTime = currentTime + problem.timeMatrix[prevNode][currentNode];
            toReturn.append("\ntravelTime:").append(problem.timeMatrix[prevNode][currentNode]);
            double earlyLine = problem.timeWindows[currentNode].start();
            double waitTime = 0;
            if (currentTime < earlyLine) {
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
                toReturn.append("\n" + currentNode + "\tcontentOut:" + currentContent + "\ttime:" + currentTime + "\twaitTime:" + waitTime + "\t(pickup to " + problem.pickupToAssociatedDelivery.get(currentNode) + " +" + 1 + ")");
            } else {
                //an unrelated node
                toReturn.append("\n" + currentNode + "\tcontent:" + currentContent + "\ttime:" + currentTime + "\twaitTime:" + waitTime);
            }
        }
        return toReturn.toString();
    }
}
