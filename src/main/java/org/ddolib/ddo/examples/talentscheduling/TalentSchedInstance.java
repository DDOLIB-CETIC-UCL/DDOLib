package org.ddolib.ddo.examples.talentscheduling;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Contains useful data to instantiate a talent scheduling problem
 *
 * @param nbScene  The number of scenes in the instance.
 * @param nbActors The number of actors in the problem.
 * @param costs    For each actor {@code i}, gives its cost.
 * @param duration For each scene {@code}, gives its duration.
 * @param actors   If  actor {@code i} must be present in scene {@code j}, so {@code actors[i][j] == 1}.
 *                 Else, {@code actors[i][j] == 1}
 */
public record TalentSchedInstance(int nbScene,
                                  int nbActors,
                                  int[] costs,
                                  int[] duration,
                                  int[][] actors) {


    @Override
    public String toString() {
        String nbSceneStr = String.format("Nb Scene: %d%n", nbScene);
        String nbActorsStr = String.format("Nb Actors: %d%n", nbActors);
        String costStr = String.format("Costs: %s%n", Arrays.toString(costs));
        String durationStr = String.format("Duration: %s%n", Arrays.toString(duration));
        String actorsStr = "Actor / Scene:\n" + Arrays.stream(actors)
                .map(row -> Arrays.stream(row)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));

        return nbSceneStr + nbActorsStr + costStr + durationStr + actorsStr;
    }
}
