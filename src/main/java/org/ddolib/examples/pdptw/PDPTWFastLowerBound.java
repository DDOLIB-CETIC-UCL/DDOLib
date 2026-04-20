package org.ddolib.examples.pdptw;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

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
        //TODO use something better based on the actual remaining nodes to reach
        for (int i : toVisit.stream().toArray()) {
            toVisitLB.add(leastIncidentEdge[i]);
        }
        Collections.sort(toVisitLB);

        ArrayList<Double> toVisitEarlyLines = new ArrayList<>(nbUnassignedVariables);
        toVisitEarlyLines.add(problem.timeWindows[0].start()); //one more earlyLine because there is the final hop
        for (int i : toVisit.stream().toArray()) {
            toVisitEarlyLines.add(problem.timeWindows[i].start());
        }
        Collections.sort(toVisitEarlyLines);

        ArrayList<Double> toVisitDeadlines = new ArrayList<>(nbUnassignedVariables);
        toVisitDeadlines.add(problem.timeWindows[0].end()); //one more deadline because there is the final hop
        for (int i :toVisit.stream().toArray()) {
            toVisitDeadlines.add(problem.timeWindows[i].end());
        }

        Collections.sort(toVisitDeadlines);

        //first iteration, to check for deadline enforcement
        //We take the min because it will not overprune
        int offsetForDeadlines = toVisitDeadlines.size() - nbUnassignedVariables;
        double currentSimulationTime = state.minCurrentTime;
         for (int i = 0; i < nbUnassignedVariables-1; i++) { //variable.size already includes the final come back
             double incomingHop = toVisitLB.get(i);
             double earlyLine = toVisitEarlyLines.get(i);
             currentSimulationTime = max(currentSimulationTime + incomingHop,earlyLine);
             double deadLine = toVisitDeadlines.get(offsetForDeadlines + i);
             if(currentSimulationTime>deadLine){
                 return Double.POSITIVE_INFINITY;
             }
         }

        //second iteration, to compute actual bound,
        // we take the max because it under-estimate the remaining cost
        currentSimulationTime = state.maxCurrentTime;
        for (int i = 0; i < nbUnassignedVariables-1; i++) { //variable.size already includes the final come back
            double incomingHop = toVisitLB.get(i);
            double earlyLine = toVisitEarlyLines.get(i);
            currentSimulationTime = max(currentSimulationTime + incomingHop,earlyLine);
        }

        return currentSimulationTime - state.minCurrentTime;
    }
}
