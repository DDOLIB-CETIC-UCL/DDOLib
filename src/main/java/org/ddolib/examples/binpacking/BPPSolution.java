package org.ddolib.examples.binpacking;

import java.util.ArrayList;
import java.util.HashMap;

public class BPPSolution {

    private final BPPProblem problem;
    private HashMap<Integer, ArrayList<Integer>> bins;

    public BPPSolution(BPPProblem problem, int[] decisions) {
        this.problem = problem;
        fillBins(decisions);
    }

    private void fillBins(int[] decisions){
        bins = new HashMap<>();
        int b = -1;
        int currentSpace = 0;
        for (int item : decisions) {
            int itemWeight = problem.itemWeight[item];
            if (itemWeight <= currentSpace) {
                bins.get(b).add(item);
                currentSpace -= itemWeight;
            } else {
                b += 1;
                currentSpace += problem.binMaxSpace - itemWeight;
                bins.put(b, new ArrayList<>(item));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int bin = 0; bin < bins.size(); bin++) {
            int totalWeight = bins.get(bin).stream().mapToInt(Integer::intValue).sum();
            sb.append(String.format("Bin number %d :\tWeight %d/%d\n", bin, totalWeight, problem.binMaxSpace));
            ArrayList<Integer> items = bins.get(bin);
            for (int item : items) {
                sb.append(String.format("item %d\t weight %d\n", item, problem.itemWeight[item]));
            }
        }
        return sb.toString();
    }
}
