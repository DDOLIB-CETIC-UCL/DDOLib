package org.ddolib.ddo.examples.talentscheduling;

import java.util.Arrays;
import java.util.stream.Collectors;

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
