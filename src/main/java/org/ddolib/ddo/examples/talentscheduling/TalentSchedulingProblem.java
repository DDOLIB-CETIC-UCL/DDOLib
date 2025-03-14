package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import javax.sound.midi.Soundbank;
import java.util.BitSet;
import java.util.Iterator;

public class TalentSchedulingProblem implements Problem<TalentSchedState> {

    final BitSet[] actors;
    final TalentSchedInstance instance;

    public TalentSchedulingProblem(TalentSchedInstance instance) {
        this.instance = instance;

        actors = new BitSet[instance.nbScene()];
        for (int i = 0; i < instance.nbScene(); i++) {
            actors[i] = new BitSet(instance.nbActors());
            for (int j = 0; j < instance.nbActors(); j++) {
                if (instance.actors()[j][i] == 1) {
                    actors[i].set(j);
                }
            }
        }
    }

    @Override
    public int nbVars() {
        return instance.nbScene();
    }

    @Override
    public TalentSchedState initialState() {
        BitSet scenes = new BitSet(instance.nbScene());
        scenes.set(0, instance.nbScene());
        return new TalentSchedState(scenes);
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
            if (state.remainingScenes().get(i)) after.or(actors[i]);
            else before.or(actors[i]);
        }
        after.and(before); // Already present actors
        return after;
    }
}
