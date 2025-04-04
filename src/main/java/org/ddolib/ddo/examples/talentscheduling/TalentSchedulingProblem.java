package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;

/**
 * Class to model the Talent scheduling problem.
 */
public class TalentSchedulingProblem implements Problem<TalentSchedState> {

    final int nbScene;
    final int nbActors;
    final int[] costs;
    final int[] duration;
    final BitSet[] actors;

    public final Optional<Integer> optimal;

    /**
     * @param nbScene  The number of scenes in the instance.
     * @param nbActors The number of actors in the problem.
     * @param costs    For each actor {@code i}, gives its cost.
     * @param duration For each scene {@code}, gives its duration.
     * @param actors   For each scene, returns the set of actors needed
     */
    public TalentSchedulingProblem(int nbScene, int nbActors, int[] costs, int[] duration, BitSet[] actors, Optional<Integer> optimal) {
        this.nbScene = nbScene;
        this.nbActors = nbActors;
        this.costs = costs;
        this.duration = duration;
        this.actors = actors;
        this.optimal = optimal;
    }

    public TalentSchedulingProblem(int nbScene, int nbActors, int[] costs, int[] duration, BitSet[] actors) {
        this(nbScene, nbActors, costs, duration, actors, Optional.empty());
    }


    @Override
    public int nbVars() {
        return nbScene;
    }

    @Override
    public TalentSchedState initialState() {
        BitSet scenes = new BitSet(nbScene);
        scenes.set(0, nbScene); // All scenes must be performed
        return new TalentSchedState(scenes, new BitSet(nbScene));
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(TalentSchedState state, int var) {
        BitSet toReturn = new BitSet(nbVars());
        toReturn.or(state.remainingScenes());

        // state inherits from a merged state. There is not enough remaining scenes to assign each variable.
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

        BitSet toPay = onLocationActors(state); // All the already present actors (playing for this scene or waiting)
        toPay.or(actors[scene]); // Add new actors

        int cost = toPay.stream()
                .map(actor -> costs[actor] * duration[scene])
                .sum(); // Costs of the playing actors


        return -cost; // Talent scheduling is a minimization problem. To get a maximization
    }


    /**
     * Given a state, returns which actors are already in location, i.e. actors needed for past scenes
     * and needed for future scenes.
     *
     * @param state A state of the mdd
     * @return Which actors are currently on location.
     */
    public BitSet onLocationActors(TalentSchedState state) {
        BitSet before = new BitSet(); //Actors for past scenes
        BitSet after = new BitSet(); // Actors for future scenes

        for (int i = 0; i < nbScene; i++) {
            if (!state.maybeScenes().get(i)) {
                if (state.remainingScenes().get(i)) after.or(actors[i]);
                else before.or(actors[i]);
            }
        }
        after.and(before); // Already present actors
        return after;
    }

    @Override
    public String toString() {
        String nbSceneStr = String.format("Nb Scene: %d%n", nbScene);
        String nbActorsStr = String.format("Nb Actors: %d%n", nbActors);
        String costStr = String.format("Costs: %s%n", Arrays.toString(costs));
        String durationStr = String.format("Duration: %s%n", Arrays.toString(duration));
        StringBuilder actorsStr = new StringBuilder();
        for (int i = 0; i < actors.length; i++) {
            actorsStr.append(String.format("Scene %d needs actors: %s%n", i, actors[i]));
        }

        return nbSceneStr + nbActorsStr + costStr + durationStr + actorsStr;
    }
}
