package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

public class TalentSchedRelax implements Relaxation<TalentSchedState> {

    private final TalentSchedulingProblem problem;

    public TalentSchedRelax(TalentSchedulingProblem problem) {
        this.problem = problem;
    }

    @Override
    public TalentSchedState mergeStates(Iterator<TalentSchedState> states) {
        BitSet mergedRemaining = new BitSet(problem.nbVars());
        mergedRemaining.set(0, problem.nbVars());
        BitSet mergedMaybe = new BitSet(problem.nbVars());

        while (states.hasNext()) {
            TalentSchedState state = states.next();
            mergedRemaining.and(state.remainingScenes());
            mergedMaybe.or(state.remainingScenes());
            mergedMaybe.or(state.maybeScenes());
        }
        mergedMaybe.andNot(mergedRemaining);

        return new TalentSchedState(mergedRemaining, mergedMaybe);
    }

    @Override
    public int relaxEdge(TalentSchedState from, TalentSchedState to, TalentSchedState merged, Decision d, int cost) {
        return cost;
    }


    @Override
    public int fastUpperBound(TalentSchedState state, Set<Integer> variables) {
        return -fastLowerBound(state);
    }

    private int fastLowerBound(TalentSchedState state) {
        double lb = 0.0;

        BitSet presentActors = problem.presentActors(state);
        RatioAndActor[] ratios = new RatioAndActor[problem.instance.nbActors()];
        for (int i = 0; i < ratios.length; i++) {
            ratios[i] = new RatioAndActor(0.0, i);
        }


        for (int scene = state.remainingScenes().nextSetBit(0); scene >= 0; scene = state.remainingScenes().nextSetBit(scene + 1)) {

            BitSet actorsOnLocation = (BitSet) problem.actors[scene].clone();
            actorsOnLocation.and(presentActors);
            if (actorsOnLocation.cardinality() != 0) {
                double totalCost = 0.0;
                double squaredCost = 0.0;

                totalCost += Arrays.stream(problem.instance.costs()).sum();
                squaredCost += Arrays.stream(problem.instance.costs()).map(x -> x * x).sum();

                for (int actor = actorsOnLocation.nextSetBit(0); actor >= 0; actor = actorsOnLocation.nextSetBit(actor + 1)) {
                    ratios[actor].ratio += problem.instance.duration()[scene];
                }

                lb -= problem.instance.duration()[scene] * (totalCost + squaredCost / totalCost) / 2.0;
            }
        }

        Arrays.sort(ratios);

        double sumE = 0.0;
        for (RatioAndActor ra : ratios) {
            if (presentActors.get(ra.actor)) {
                int a = ra.actor;
                sumE += ratios[a].ratio * problem.instance.costs()[a];
                lb += problem.instance.costs()[a] * sumE;
            }
        }

        return (int) Math.ceil(lb);
    }


    private static class RatioAndActor implements Comparable<RatioAndActor> {
        public double ratio;
        public int actor;

        public RatioAndActor(double ratio, int actor) {
            this.ratio = ratio;
            this.actor = actor;
        }

        @Override
        public int compareTo(RatioAndActor o) {
            int cmp = Double.compare(ratio, o.ratio);
            if (cmp == 0) {
                return Integer.compare(actor, o.actor);
            } else {
                return cmp;
            }
        }
    }
}
