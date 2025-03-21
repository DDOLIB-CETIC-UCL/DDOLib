package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.BitSet;
import java.util.Iterator;

public class TalentSchedulingProblem implements Problem<TalentSchedState> {

    /**
     * For each scene, returns the needed actors
     */
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
        scenes.set(0, instance.nbScene()); // All scenes must be performed
        return new TalentSchedState(scenes, new BitSet(instance.nbScene()));
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(TalentSchedState state, int var) {
        BitSet toReturn = new BitSet(nbVars());
        toReturn.or(state.remainingScenes());

        // state inherit from a merged state. There is not enough remaining scenes to assign each variable.
        // So, we select scene form maybeScenes
        if (toReturn.cardinality() < nbVars())
            toReturn.or(state.maybeScenes());

        return toReturn.stream().iterator();
    }

    @Override
    public TalentSchedState transition(TalentSchedState state, Decision decision) {
        BitSet newRemaining = (BitSet) state.remainingScenes().clone();
        BitSet newMaybe = (BitSet) state.maybeScenes().clone();
        newRemaining.set(decision.val(), false);
        newMaybe.set(decision.val(), false);

        return new TalentSchedState(newRemaining, newMaybe);
    }

    @Override
    public int transitionCost(TalentSchedState state, Decision decision) {
        int scene = decision.val();

        BitSet toPay = presentActors(state); // All the already present actors (playing for this scene or waiting)
        toPay.or(actors[scene]); // Add new actors

        int cost = toPay.stream()
                .map(actor -> instance.costs()[actor] * instance.duration()[scene])
                .sum(); // Costs of the playing actors


        return -cost; // Talent scheduling is a minimization problem. To get a maximization
    }


    public BitSet presentActors(TalentSchedState state) {
        BitSet before = new BitSet(); //Actors for past scenes
        BitSet after = new BitSet(); // Actors for future scenes

        for (int i = 0; i < instance.nbScene(); i++) {
            if (!state.maybeScenes().get(i)) {
                if (state.remainingScenes().get(i)) after.or(actors[i]);
                else before.or(actors[i]);
            }
        }
        after.and(before); // Already present actors
        return after;
    }
}
