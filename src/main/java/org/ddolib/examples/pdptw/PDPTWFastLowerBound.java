package org.ddolib.examples.pdptw;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

import static java.lang.Double.valueOf;
import static java.lang.Math.max;

/**
 * Implementation of a fast upper bound for the PDPTW
 */
public class PDPTWFastLowerBound implements FastLowerBound<PDPTWState> {
    private final double[] leastIncidentEdge;
    PDPTWProblem problem;

    public PDPTWFastLowerBound(PDPTWProblem problem) {
        this.problem = problem;
        this.leastIncidentEdge = new double[problem.n];
        for (int i = 0; i < problem.n; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < problem.n; j++) {
                if (i != j) {
                    min = Math.min(min, problem.timeMatrix[i][j]);
                }
            }
            leastIncidentEdge[i] = min;
        }
    }

    @Override
    public double fastLowerBound(PDPTWState state, Set<Integer> variables) {
        return fastLowerBound(state, variables.size());
    }


    public double fastLowerBound(PDPTWState state, int nbUnassignedVariables) {
        BitSet toVisit = state.allToVisit;

        // for each unvisited node, we take the smallest incident edge
        ArrayList<Double> toVisitLB = new ArrayList<>(nbUnassignedVariables);
        toVisitLB.add(leastIncidentEdge[0]); //adding zero for the final come back
        //TODO prune based on the actual remaining nodes to reach
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            toVisitLB.add(leastIncidentEdge[i]);
        }
        Collections.sort(toVisitLB);

        ArrayList<Double> toVisitEarlyLines = new ArrayList<>(nbUnassignedVariables);
        toVisitEarlyLines.add(0.0); //one more earlyLine because there is the final hop
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            toVisitEarlyLines.add(problem.timeWindows[i].start());
        }
        Collections.sort(toVisitEarlyLines);

        ArrayList<Double> toVisitDeadlines = new ArrayList<>(nbUnassignedVariables);
        toVisitDeadlines.add(problem.timeWindows[0].end()); //one more deadline because there is the final hop
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            toVisitDeadlines.add(problem.timeWindows[i].end());
        }
        Collections.sort(toVisitDeadlines);

        int offsetForDeadlines = toVisitDeadlines.size() - nbUnassignedVariables;
        double currentSimulationTime = state.currentTime;
         for (int i = 0; i < nbUnassignedVariables; i++) { //variable.size already includes the final come back
             double incomingHop = toVisitLB.get(i);
             double earlyLine = toVisitEarlyLines.get(i);
             currentSimulationTime = max(currentSimulationTime + incomingHop,earlyLine);
             double deadLine = toVisitDeadlines.get(offsetForDeadlines + i);
             if(currentSimulationTime>deadLine){
                 return Double.POSITIVE_INFINITY;
             }
         }

        return currentSimulationTime - state.currentTime;
    }
}
