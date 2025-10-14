package org.ddolib.examples.pdp;

public class PDPSolution {
    PDPProblem problem;
    public int[] solution;
    public double value;

    public PDPSolution(PDPProblem problem, int[] solution, double value) {
        this.problem = problem;
        this.solution = solution;
        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder toReturn = new StringBuilder("0\tcontentOut:" + 0);
        int currentNode = 0;
        int currentContent = 0;
        for (int i = 1; i < solution.length; i++) {
            currentNode = solution[i];
            if (problem.deliveryToAssociatedPickup.containsKey(currentNode)) {
                //it is a delivery
                currentContent = currentContent - 1;
                toReturn.append("\n" + currentNode + " \tcontentOut:" + currentContent + "\t(delivery from " + problem.deliveryToAssociatedPickup.get(currentNode) + " -" + 1 + ")");
            } else if (problem.pickupToAssociatedDelivery.containsKey(currentNode)) {
                // it is a pickup
                currentContent = currentContent + 1;
                toReturn.append("\n" + currentNode + "\tcontentOut:" + currentContent + "\t(pickup to " + problem.pickupToAssociatedDelivery.get(currentNode) + " +" + 1 + ")");
            } else {
                //an unrelated node
                toReturn.append("\n" + currentNode + "\tcontent:" + currentContent);
            }
        }
        return toReturn.toString();
    }
}
