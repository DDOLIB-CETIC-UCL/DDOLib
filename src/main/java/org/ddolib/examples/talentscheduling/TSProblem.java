package org.ddolib.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;

/**
 * The Talent Scheduling Problem (TSP) instance.
 *
 * <p>
 * In the Talent Scheduling Problem, we have a set of scenes to shoot, each requiring a subset of actors,
 * and each actor has an associated cost per day. The objective is to schedule the scenes to minimize
 * the total cost of actors while respecting scene requirements.
 * </p>
 *
 * <p>
 * This class implements the {@link Problem} interface for use with search algorithms (ACS, A*, DDO, etc.).
 * It provides methods to get the initial state, compute transition costs, and define the domain for each state variable.
 * </p>
 *
 * <p>
 * The problem can be constructed either:
 * </p>
 * <ul>
 *     <li>From explicit data arrays (number of scenes, number of actors, actor costs, scene durations, and actor requirements),</li>
 *     <li>Or by reading a data file in the format used in the Talent Scheduling Problem dataset
 *     (<a href="https://people.eng.unimelb.edu.au/pstuckey/talent/">source</a>).</li>
 * </ul>
 */
public class TSProblem implements Problem<TSState> {

    /** Number of scenes in the instance. */
    final int nbScene;

    /** Number of actors in the instance. */
    final int nbActors;

    /** Cost for each actor per day. */
    final int[] costs;

    /** Duration of each scene. */
    final int[] duration;

    /** For each scene, the set of actors required to perform that scene. */
    final BitSet[] actors;

    /** The optimal solution value if known (optional, used for testing and benchmarking). */
    public final Optional<Double> optimal;

    /** Optional descriptive name for the instance. */
    private Optional<String> name = Optional.empty();

    /**
     * Constructs a TSP instance from explicit parameters.
     *
     * @param nbScene  Number of scenes.
     * @param nbActors Number of actors.
     * @param costs    Array containing the cost of each actor per day.
     * @param duration Array containing the duration of each scene.
     * @param actors   Array of BitSets representing the actors required for each scene.
     * @param optimal  Optional value of the optimal solution, if known.
     */
    public TSProblem(int nbScene, int nbActors, int[] costs, int[] duration, BitSet[] actors, Optional<Double> optimal) {
        this.nbScene = nbScene;
        this.nbActors = nbActors;
        this.costs = costs;
        this.duration = duration;
        this.actors = actors;
        this.optimal = optimal;
    }


    /**
     * Constructs a TSP instance by reading a file in the standard dataset format.
     *
     * @param fname Path to the input file.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    public TSProblem(String fname) throws IOException {
        int nbScenes = 0;
        int nbActors = 0;
        int[] cost = new int[0];
        int[] duration = new int[0];
        BitSet[] actors = new BitSet[0];
        Optional<Double> opti = Optional.empty();

        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            String line;

            int lineCount = 0;
            int skip = 0;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    skip++;
                } else if (lineCount == 0) {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length == 3) {
                        opti = Optional.of(Double.parseDouble(tokens[2]));
                    }
                } else if (lineCount == 1) {
                    nbScenes = Integer.parseInt(line);
                    duration = new int[nbScenes];
                } else if (lineCount == 2) {
                    nbActors = Integer.parseInt(line);
                    cost = new int[nbActors];
                    actors = new BitSet[nbScenes];
                    for (int i = 0; i < nbScenes; i++) {
                        actors[i] = new BitSet(nbActors);
                    }
                } else if (lineCount - skip - 3 < nbActors) {
                    int actor = lineCount - skip - 3;
                    String[] tokens = line.split("\\s+");
                    cost[actor] = Integer.parseInt(tokens[nbScenes]);
                    for (int i = 0; i < nbScenes; i++) {
                        int x = Integer.parseInt(tokens[i]);
                        if (Integer.parseInt(tokens[i]) == 1) {
                            actors[i].set(actor);
                        }
                    }
                } else {
                    String[] tokens = line.split("\\s+");
                    for (int i = 0; i < nbScenes; i++) {
                        duration[i] = Integer.parseInt(tokens[i]);
                    }
                }
                lineCount++;
            }
        }
        this.nbScene = nbScenes;
        this.nbActors = nbActors;
        this.costs = cost;
        this.duration = duration;
        this.actors = actors;
        this.optimal = opti;
        this.name = Optional.of(fname);
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public int nbVars() {
        return nbScene;
    }

    @Override
    public TSState initialState() {
        BitSet scenes = new BitSet(nbScene);
        scenes.set(0, nbScene, true); // All scenes must be performed
        return new TSState(scenes, new BitSet(nbScene));
    }

    @Override
    public double initialValue() {
        int cost = 0;
        for (int scene = 0; scene < nbScene; scene++) {
            for (int actor = actors[scene].nextSetBit(0);
                 actor >= 0;
                 actor = actors[scene].nextSetBit(actor + 1)) {
                cost += costs[actor] * duration[scene];
            }
        }
        return cost;
    }

    @Override
    public Iterator<Integer> domain(TSState state, int var) {
        BitSet toReturn = new BitSet(nbVars());
        toReturn.or(state.remainingScenes());

        // state inherits from a merged state. There is not enough remaining scenes to assign each variable.
        // So, we select scene form maybeScenes
        if (var + toReturn.cardinality() < nbVars()) toReturn.or(state.maybeScenes());

        return toReturn.stream().iterator();
    }

    @Override
    public TSState transition(TSState state, Decision decision) {
        BitSet newRemaining = (BitSet) state.remainingScenes().clone();
        BitSet newMaybe = (BitSet) state.maybeScenes().clone();
        newRemaining.set(decision.val(), false);
        newMaybe.set(decision.val(), false);

        return new TSState(newRemaining, newMaybe);
    }

    @Override
    public double transitionCost(TSState state, Decision decision) {
        int scene = decision.val();

        // All the already present actors (playing for this scene or waiting)
        // Actors not longer needed are discarded from this BitSet.
        BitSet toPay = onLocationActors(state);
        toPay.andNot(actors[scene]); // Add new actors

        int cost = 0;
        for (int actor = toPay.nextSetBit(0); actor >= 0; actor = toPay.nextSetBit(actor + 1)) {
            cost += costs[actor] * duration[scene];
        }


        return cost;
    }


    /**
     * Returns the set of actors already present on location at the current state,
     * i.e., actors involved in past scenes and needed for future scenes.
     *
     * @param state Current state of the MDD.
     * @return BitSet of actors currently on location.
     */
    public BitSet onLocationActors(TSState state) {
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
        if (name.isPresent()) {
            return name.get();
        } else {
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
}
