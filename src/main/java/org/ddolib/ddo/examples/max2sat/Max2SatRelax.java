package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Integer.signum;
import static java.lang.Math.abs;


public class Max2SatRelax implements Relaxation<Max2SatState> {

    private final Max2SatProblem problem;

    public Max2SatRelax(Max2SatProblem problem) {
        this.problem = problem;
    }

    @Override
    public Max2SatState mergeStates(Iterator<Max2SatState> states) {
        ArrayList<Integer> merged = new ArrayList<>(Collections.nCopies(problem.nbVars(), 0));
        int depth = problem.nbVars();
        while (states.hasNext()) {
            Max2SatState current = states.next();
            depth = current.depth();
            for (int i = 0; i < current.netBenefit().size(); i++) {
                Integer mergedI = merged.get(i);
                Integer currentI = current.netBenefit().get(i);
                if (signum(mergedI) == 1 && signum(currentI) == 1) {
                    merged.set(i, min(mergedI, currentI));
                } else if (signum(mergedI) == -1 && signum(currentI) == 1) {
                    merged.set(i, max(mergedI, currentI));
                } else {
                    merged.set(i, 0);
                }
            }
        }
        return new Max2SatState(merged, depth);
    }

    @Override
    public int relaxEdge(Max2SatState from, Max2SatState to, Max2SatState merged, Decision d, int cost) {
        int toReturn = cost;
        for (int i = d.var() + 1; i < problem.nbVars(); i++) {
            toReturn += abs(to.netBenefit().get(i)) - abs(merged.netBenefit().get(i));
        }

        return toReturn;
    }

    @Override
    public int fastUpperBound(Max2SatState state, Set<Integer> variables) {
        int rub = Max2SatRanking.rank(state);
        for (Integer i : variables) {
            for (Integer j : variables) {
                if (j > i) {
                    int gtt = problem.weight(problem.t(i), problem.t(j)) + problem.weight(problem.t(i), problem.f(j)) +
                            problem.weight(problem.f(i), problem.t(j));

                    int gtf = problem.weight(problem.t(i), problem.t(j)) + problem.weight(problem.t(i), problem.f(j)) +
                            problem.weight(problem.f(i), problem.f(j));

                    int gft = problem.weight(problem.t(i), problem.t(j)) + problem.weight(problem.f(i), problem.t(j)) +
                            problem.weight(problem.f(i), problem.f(j));

                    int gff = problem.weight(problem.t(i), problem.f(j)) + problem.weight(problem.f(i), problem.t(j)) +
                            problem.weight(problem.f(i), problem.f(j));
                    rub += IntStream.of(gtt, gtf, gft, gff).max().getAsInt();
                }
            }
        }

        return rub;
    }
}
