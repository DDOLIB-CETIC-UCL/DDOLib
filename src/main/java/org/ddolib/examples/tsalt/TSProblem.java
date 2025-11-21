package org.ddolib.examples.tsalt;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;

public class TSProblem implements Problem<TSState> {

    final int nbScene;
    final int nbActors;
    final int[] costs;
    final int[] duration;
    final BitSet[] actors;

    public final Optional<Double> optimal;
    private Optional<String> name = Optional.empty();

    /**
     * @param nbScene  The number of scenes in the instance.
     * @param nbActors The number of actors in the problem.
     * @param costs    For each actor {@code i}, gives its cost.
     * @param duration For each scene {@code i}, gives its duration.
     * @param actors   For each scene, returns the set of actors needed
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

    public TSProblem(int nbScene, int nbActors, int[] costs, int[] duration, BitSet[] actors) {
        this(nbScene, nbActors, costs, duration, actors, Optional.empty());
    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }

        // For each actor, return the position in the solution of the first scene in which he is present.
        int[] firstScenePos = new int[nbActors];
        Arrays.fill(firstScenePos, -1);
        // For each actor, return the position in the solution of the last scene in which he is
        // present.
        int[] lastScenePos = new int[nbActors];
        Arrays.fill(lastScenePos, -1);
        for (int i = 0; i < nbVars(); i++) {
            int scene = solution[i];
            BitSet neededActors = actors[scene];
            for (int actor = neededActors.nextSetBit(0); actor >= 0; actor = neededActors.nextSetBit(actor + 1)) {
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
                if (firstScenePos[actor] <= i && i <= lastScenePos[actor]) { //The actor is present
                    value += costs[actor] * duration[scene];
                }
            }
        }

        return value;
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
        return new TSState(scenes, onLocationActors(scenes));
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

        return toReturn.stream().iterator();
    }

    @Override
    public TSState transition(TSState state, Decision decision) {
        BitSet newRemaining = (BitSet) state.remainingScenes().clone();
        newRemaining.set(decision.val(), false);

        // return new TSState(newRemaining, newMaybe);
        return new TSState(newRemaining, onLocationActors(newRemaining));
    }

    @Override
    public double transitionCost(TSState state, Decision decision) {
        int scene = decision.val();

        // All the already present actors (playing for this scene or waiting)
        // Actors not longer needed are discarded from this BitSet.
        BitSet toPay = (BitSet ) state.onLocationActors().clone();
        toPay.andNot(actors[scene]); // Add new actors

        int cost = 0;
        for (int actor = toPay.nextSetBit(0); actor >= 0; actor = toPay.nextSetBit(actor + 1)) {
            cost += costs[actor] * duration[scene];
        }

        return cost;
    }

    public BitSet onLocationActors(BitSet remainingScenes) {
        BitSet before = new BitSet(); //Actors for past scenes
        BitSet after = new BitSet(); // Actors for future scenes

        for (int i = 0; i < nbScene; i++) {
            if (remainingScenes.get(i)) after.or(actors[i]);
            else before.or(actors[i]);
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
