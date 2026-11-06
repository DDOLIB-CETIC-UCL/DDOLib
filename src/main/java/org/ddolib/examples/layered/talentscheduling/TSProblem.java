package org.ddolib.examples.layered.talentscheduling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.solving.ddo.core.Decision;

/**
 * The Talent Scheduling Problem (TSP) instance.
 *
 * <p>In the Talent Scheduling Problem, we have a set of scenes to shoot, each requiring a subset of
 * actors, and each actor has an associated cost per day. The objective is to schedule the scenes to
 * minimize the total cost of actors while respecting scene requirements.
 *
 * <p>This class implements the {@link Problem} interface for use with search algorithms (ACS, A*,
 * DDO, etc.). It provides methods to get the initial state, compute transition costs, and define
 * the domain for each state variable.
 *
 * <p>The problem can be constructed either:
 *
 * <ul>
 *   <li>From explicit data arrays (number of scenes, number of actors, actor costs, scene
 *       durations, and actor requirements),
 *   <li>Or by reading a data file in the format used in the Talent Scheduling Problem dataset (<a
 *       href="https://people.eng.unimelb.edu.au/pstuckey/talent/">source</a>).
 * </ul>
 */
public class TSProblem implements Problem<TSState> {

    /** The optimal solution value if known (optional, used for testing and benchmarking). */
    public final Optional<Double> optimal;

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

    /** Optional descriptive name for the instance. */
    private Optional<String> name = Optional.empty();

    /**
     * Constructs a TSP instance from explicit parameters.
     *
     * @param nbScene number of scenes
     * @param nbActors number of actors
     * @param costs array containing the cost of each actor per day
     * @param duration array containing the duration of each scene
     * @param actors array of BitSets representing the actors required for each scene
     * @param optimal Optional value of the optimal solution, if known
     */
    public TSProblem(
            int nbScene,
            int nbActors,
            int[] costs,
            int[] duration,
            BitSet[] actors,
            Optional<Double> optimal) {
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
     * <p>The first line gives the name of the instance, optionally followed by two tokens, the last
     * one being the optimal value. The rest of the file is read as a sequence of integers separated
     * by any whitespace, so that the line breaks, the blank lines and the leading spaces do not
     * matter: the number of scenes and the number of actors (on one line or on two lines, both
     * forms are used by the datasets), then for each actor one 0/1 value per scene followed by the
     * cost of the actor, then the duration of each scene.
     *
     * @param fname path to the input file
     * @throws IOException if an I/O error occurs while reading the file, or if the file does not
     *     contain the expected numbers
     */
    public TSProblem(String fname) throws IOException {
        Optional<Double> opti = Optional.empty();
        List<String> numbers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] tokens = trimmed.split("\\s+");
                if (header) {
                    header = false;
                    if (tokens.length == 3) {
                        opti = Optional.of(Double.parseDouble(tokens[2]));
                    }
                } else {
                    numbers.addAll(Arrays.asList(tokens));
                }
            }
        }
        Iterator<String> it = numbers.iterator();
        try {
            int nbScenes = Integer.parseInt(it.next());
            int nbActors = Integer.parseInt(it.next());
            int[] cost = new int[nbActors];
            int[] duration = new int[nbScenes];
            BitSet[] actors = new BitSet[nbScenes];
            for (int i = 0; i < nbScenes; i++) {
                actors[i] = new BitSet(nbActors);
            }
            for (int actor = 0; actor < nbActors; actor++) {
                for (int i = 0; i < nbScenes; i++) {
                    if (Integer.parseInt(it.next()) == 1) {
                        actors[i].set(actor);
                    }
                }
                cost[actor] = Integer.parseInt(it.next());
            }
            for (int i = 0; i < nbScenes; i++) {
                duration[i] = Integer.parseInt(it.next());
            }
            this.nbScene = nbScenes;
            this.nbActors = nbActors;
            this.costs = cost;
            this.duration = duration;
            this.actors = actors;
        } catch (NoSuchElementException | NumberFormatException e) {
            throw new IOException("Invalid talent scheduling instance: " + fname, e);
        }
        this.optimal = opti;
        this.name = Optional.of(fname);
    }

    @Override
    public int nbVars() {
        return nbScene;
    }

    @Override
    public TSState initialState() {
        BitSet scenes = new BitSet(nbScene);
        scenes.set(0, nbScene, true); // All scenes must be performed
        // return new TSState(scenes, new BitSet(nbScene));
        return new TSState(
                scenes, new BitSet(nbScene), onLocationActors(scenes, new BitSet(nbScene)));
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

        // state inherits from a merged state. There is not enough remaining scenes to assign each
        // variable.
        // So, we select scene form maybeScenes
        if (var + toReturn.cardinality() < nbVars()) {
            toReturn.or(state.maybeScenes());
        }

        return toReturn.stream().iterator();
    }

    @Override
    public TSState transition(TSState state, Decision decision) {
        BitSet newRemaining = (BitSet) state.remainingScenes().clone();
        BitSet newMaybe = (BitSet) state.maybeScenes().clone();
        newRemaining.set(decision.value(), false);
        newMaybe.set(decision.value(), false);

        // return new TSState(newRemaining, newMaybe);
        return new TSState(newRemaining, newMaybe, onLocationActors(newRemaining, newMaybe));
    }

    @Override
    public double transitionCost(TSState state, Decision decision) {
        int scene = decision.value();

        // All the already present actors (playing for this scene or waiting)
        // Actors no longer needed are discarded from this BitSet.
        BitSet toPay = onLocationActors(state);
        toPay.andNot(actors[scene]); // Add new actors

        int cost = 0;
        for (int actor = toPay.nextSetBit(0); actor >= 0; actor = toPay.nextSetBit(actor + 1)) {
            cost += costs[actor] * duration[scene];
        }

        return cost;
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(
                    String.format(
                            "The solution %s does not cover all " + "the %d variables",
                            Arrays.toString(solution), nbVars()));
        }

        // For each actor, return the position in the solution of the first scene in which he is
        // present.
        int[] firstScenePos = new int[nbActors];
        Arrays.fill(firstScenePos, -1);
        // For each actor, return the position in the solution of the last scene in which he is
        // present.
        int[] lastScenePos = new int[nbActors];
        Arrays.fill(lastScenePos, -1);
        for (int i = 0; i < nbVars(); i++) {
            int scene = solution[i];
            BitSet neededActors = actors[scene];
            for (int actor = neededActors.nextSetBit(0);
                    actor >= 0;
                    actor = neededActors.nextSetBit(actor + 1)) {
                if (firstScenePos[actor] == -1) {
                    firstScenePos[actor] = i;
                }
                if (firstScenePos[actor] != -1) {
                    lastScenePos[actor] = i;
                }
            }
        }

        double value = 0;
        for (int i = 0; i < nbVars(); i++) {
            int scene = solution[i];
            for (int actor = 0; actor < nbActors; actor++) {
                if (firstScenePos[actor] <= i && i <= lastScenePos[actor]) { // The actor is present
                    value += costs[actor] * duration[scene];
                }
            }
        }

        return value;
    }

    /**
     * Computes the total cost per day of all actors required to perform a given scene.
     *
     * @param scene the index of the scene
     * @return the sum of the daily costs of the actors involved in {@code scene}
     */
    public int sceneCost(int scene) {
        int sum = 0;
        for (int actor = this.actors[scene].nextSetBit(0);
                actor >= 0;
                actor = this.actors[scene].nextSetBit(actor + 1)) {
            sum += costs[actor];
        }
        return sum;
    }

    /**
     * Returns the set of actors already present on location at the current state, i.e., actors
     * involved in past scenes and needed for future scenes.
     *
     * @param state current state of the MDD
     * @return BitSet of actors currently on location
     */
    public BitSet onLocationActors(TSState state) {
        BitSet before = new BitSet(); // Actors for past scenes
        BitSet after = new BitSet(); // Actors for future scenes

        for (int i = 0; i < nbScene; i++) {
            if (!state.maybeScenes().get(i)) {
                if (state.remainingScenes().get(i)) {
                    after.or(actors[i]);
                } else {
                    before.or(actors[i]);
                }
            }
        }
        after.and(before); // Already present actors
        return after;
    }

    /**
     * Returns the set of actors already present on location, i.e., actors involved in past scenes
     * and needed for future scenes, given explicit remaining and maybe scene sets.
     *
     * @param remainingScenes the scenes that still need to be scheduled
     * @param maybeScenes the scenes that may or may not still need to be scheduled
     * @return the BitSet of actors currently on location
     */
    public BitSet onLocationActors(BitSet remainingScenes, BitSet maybeScenes) {
        BitSet before = new BitSet(); // Actors for past scenes
        BitSet after = new BitSet(); // Actors for future scenes

        for (int i = 0; i < nbScene; i++) {
            if (!maybeScenes.get(i)) {
                if (remainingScenes.get(i)) {
                    after.or(actors[i]);
                } else {
                    before.or(actors[i]);
                }
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
