package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Integer.signum;
import static java.lang.Math.abs;


public class Max2SatRelax implements Relaxation<ArrayList<Integer>> {

    private final Max2SatProblem problem;

    public Max2SatRelax(Max2SatProblem problem) {
        this.problem = problem;
    }

    @Override
    public ArrayList<Integer> mergeStates(Iterator<ArrayList<Integer>> states) {
        ArrayList<Integer> merged = new ArrayList<>(Collections.nCopies(problem.nbVars(), 0));
        while (states.hasNext()) {
            ArrayList<Integer> current = states.next();
            for (int i = 0; i < current.size(); i++) {
                Integer mergedI = merged.get(i);
                Integer currentI = current.get(i);
                if (signum(mergedI) == 1 && signum(currentI) == 1) {
                    merged.set(i, min(mergedI, currentI));
                } else if (signum(mergedI) == -1 && signum(currentI) == 1) {
                    merged.set(i, max(mergedI, currentI));
                } else {
                    merged.set(i, 0);
                }
            }
        }
        return merged;
    }

    @Override
    public int relaxEdge(ArrayList<Integer> from, ArrayList<Integer> to, ArrayList<Integer> merged, Decision d, int cost) {
        int toReturn = cost;
        for (int i = d.var() + 1; i < problem.nbVars(); i++) {
            toReturn += abs(to.get(i)) - abs(merged.get(i));
        }

        return toReturn;
    }
}
