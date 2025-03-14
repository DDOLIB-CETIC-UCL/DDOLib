package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.BitSet;
import java.util.Iterator;

public class TalentSchedulingProblem implements Problem<TalentSchedState> {

    final BitSet[] actors;
    final TalentSchedInstance instance;

    public TalentSchedulingProblem(BitSet[] actors, TalentSchedInstance instance) {
        this.actors = actors;
        this.instance = instance;
    }

    @Override
    public int nbVars() {
        return instance.nbScene();
    }

    @Override
    public TalentSchedState initialState() {
        return new TalentSchedState(new BitSet());
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(TalentSchedState state, int var) {
        return state.remainingScenes().stream().iterator();
    }

    @Override
    public TalentSchedState transition(TalentSchedState state, Decision decision) {
        BitSet toReturn = (BitSet) state.remainingScenes().clone();
        toReturn.set(decision.val(), false);

        return new TalentSchedState(toReturn);
    }

    @Override
    public int transitionCost(TalentSchedState state, Decision decision) {
        int scene = decision.val();
        BitSet playing = actors[scene]; // The actors playing during this scene

        BitSet waiting = presentActors(state);
        waiting.andNot(playing); // Present actors waiting for their future scene

        int cost = playing.stream()
                .map(actor -> instance.costs()[actor] * instance.duration()[scene])
                .sum(); // Costs of the playing actors

        cost += waiting.stream()
                .map(actor -> instance.costs()[actor] * instance.duration()[scene])
                .sum(); // Costs of the waiting actors

        return -cost;
    }


    public BitSet presentActors(TalentSchedState state) {
        BitSet before = new BitSet(); //Actors for past scenes
        BitSet after = new BitSet(); // Actors for future scenes

        for (int i = 0; i < instance.nbScene(); i++) {
            if (state.remainingScenes().get(i)) after.set(i);
            else before.set(i);
        }
        after.and(before); // Already present actors
        return after;
    }
}
