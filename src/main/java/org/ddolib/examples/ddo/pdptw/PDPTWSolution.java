package org.ddolib.examples.ddo.pdptw;

public class PDPTWSolution {
    PDPTWProblem problem;
    public int[] solution;
    public double value;

    public PDPTWSolution(PDPTWProblem problem, int[] solution, double value) {
        this.problem = problem;
        this.solution = solution;
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("eval from scratch: " + problem.instance.eval(solution) + "\n");
        toReturn.append("0\tcontent:" + 0);

        int currentNode = 0;
        int currentContent = 0;
        int currentTime = problem.instance.timeWindows[0].start();

        for (int i = 1; i < solution.length; i++) {
            int prevNode = currentNode;
            currentNode = solution[i];
            currentTime = currentTime + problem.instance.timeAndDistanceMatrix[prevNode][currentNode];
            toReturn.append("\ntravelTime:" + problem.instance.timeAndDistanceMatrix[prevNode][currentNode]);
            int earlyLine = problem.instance.timeWindows[currentNode].start();
            int waitTime = 0;
            if(currentTime < earlyLine) {
                waitTime = earlyLine - currentTime;
                currentTime = earlyLine;
            }
            if (problem.instance.deliveryToAssociatedPickup.containsKey(currentNode)) {
                //it is a delivery
                currentContent = currentContent - 1;
                toReturn.append("\n" + currentNode + " \tcontentOut:" + currentContent + "\ttime:" + currentTime + "\twaitTime:" + waitTime + "\t(delivery from " + problem.instance.deliveryToAssociatedPickup.get(currentNode) + " -" + 1 + ")");
            } else if (problem.instance.pickupToAssociatedDelivery.containsKey(currentNode)) {
                // it is a pickup
                currentContent = currentContent + 1;
                toReturn.append("\n" + currentNode + "\tcontentOut:" + currentContent + "\ttime:" + currentTime  + "\twaitTime:" + waitTime+ "\t(pickup to " + problem.instance.pickupToAssociatedDelivery.get(currentNode) + " +" + 1 + ")");
            } else {
                //an unrelated node
                toReturn.append("\n" + currentNode + "\tcontent:" + currentContent + "\ttime:" + currentTime + "\twaitTime:" + waitTime);
            }
        }
        return toReturn.toString();
    }
}
