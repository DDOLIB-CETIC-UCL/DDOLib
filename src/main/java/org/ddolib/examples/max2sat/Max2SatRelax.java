package org.ddolib.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static java.lang.Integer.*;
import static java.lang.Math.abs;


public class Max2SatRelax implements Relaxation<Max2SatState> {

    private final Max2SatProblem problem;

    public Max2SatRelax(Max2SatProblem problem) {
        this.problem = problem;
    }

    @Override
    public Max2SatState mergeStates(Iterator<Max2SatState> states) {
        //Being to optimistic can lead to bad decision. For the merged state we keep the net
        // benefit near to 0.
        ArrayList<Integer> merged = new ArrayList<>(Collections.nCopies(problem.nbVars(), 0));
        int depth = problem.nbVars();
        while (states.hasNext()) {
            Max2SatState current = states.next();
            depth = current.depth();
            for (int i = 0; i < current.netBenefit().size(); i++) {
                Integer mergedI = merged.get(i);
                Integer currentI = current.netBenefit().get(i);
                // If all the net benefits have the same sign, we keep the smallest one in absolute value.
                if (signum(mergedI) == 1 && signum(currentI) == 1) {
                    merged.set(i, min(mergedI, currentI));
                } else if (signum(mergedI) == -1 && signum(currentI) == -1) {
                    merged.set(i, max(mergedI, currentI));
                } else {
                    // Otherwise, we set the benefit to 0.
                    merged.set(i, 0);
                }
            }
        }
        return new Max2SatState(merged, depth);
    }

    @Override
    public double relaxEdge(Max2SatState from, Max2SatState to, Max2SatState merged, Decision d,
                            double cost) {
        // The net benefits in merged state are smaller than the net benefit in exact states.
        // To offset the losses of benefit and guarantee an over-approximation of the optimal
        // solution, we add the losses to the transition cost.
        double toReturn = -cost;
        for (int i = d.var() + 1; i < problem.nbVars(); i++) {
            toReturn += abs(to.netBenefit().get(i)) - abs(merged.netBenefit().get(i));
        }
        return -toReturn;
    }


}
