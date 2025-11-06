package org.ddolib.examples.pdp;
/**
 * Represents a solution to a Pickup and Delivery Problem (PDP) instance.
 * <p>
 * This class stores the sequence of visited nodes (solution), the total cost or value
 * of the solution, and a reference to the original {@link PDPProblem} instance.
 * </p>
 *
 * <p>
 * The {@link #toString()} method provides a detailed textual representation of the solution,
 * including for each node whether it is a pickup, a delivery, or an unrelated node,
 * as well as the vehicle content after visiting that node.
 * </p>
 */
public class PDPSolution {
    /** The PDP problem instance for which this solution was computed. */
    PDPProblem problem;

    /** The sequence of nodes representing the solution, including pickups, deliveries, and unrelated nodes. */
    public int[] solution;

    /** The total value (cost or distance) of the solution. */
    public double value;
    /**
     * Constructs a PDP solution with the given problem, solution sequence, and value.
     *
     * @param problem  the PDP problem instance
     * @param solution the sequence of nodes representing the solution
     * @param value    the total cost or distance of the solution
     */
    public PDPSolution(PDPProblem problem, int[] solution, double value) {
        this.problem = problem;
        this.solution = solution;
        this.value = value;
    }
    /**
     * Returns a human-readable representation of the solution.
     * <p>
     * Each line represents a node visited in order. For pickups and deliveries, the output
     * indicates the change in vehicle content and the associated pickup/delivery relationship.
     * For unrelated nodes, only the node and current vehicle content are shown.
     * </p>
     *
     * @return a formatted string showing the solution path and vehicle contents
     */
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
