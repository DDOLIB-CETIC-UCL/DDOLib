package org.ddolib.ddo.examples.pdp;

public class PDPSolution{
    PDPProblem problem;
    int[] solution;

    public PDPSolution(PDPProblem problem, int[] solution){
        this.problem = problem;
        this.solution = solution;
    }

    @Override
    public String toString() {

        StringBuilder toReturn = new StringBuilder("0\tcontent:" + 0);
        int currentNode = 0;
        int currentContent = 0;
        for(int i = 1 ; i < solution.length ; i++){
            currentNode = solution[i];
            if(problem.deliveryToAssociatedPickup.containsKey(currentNode)){
                //it is a delivery
                currentContent = currentContent-1;
                toReturn.append("\n" + currentNode + " \tcontent:" + currentContent + "\t(delivery from " + problem.deliveryToAssociatedPickup.get(currentNode) + " -" + 1 +")");
            }else if (problem.pickupToAssociatedDelivery.containsKey(currentNode)){
                // it is a pickup
                currentContent = currentContent+1;
                toReturn.append("\n" + currentNode + "\tcontent:" + currentContent +   "\t(pickup to " + problem.pickupToAssociatedDelivery.get(currentNode) + " +" + 1 + ")");
            }else{
                //an unrelated node
                toReturn.append("\n" + currentNode + "\tcontent:" + currentContent);
            }
        }
        return toReturn.toString();
    }
}