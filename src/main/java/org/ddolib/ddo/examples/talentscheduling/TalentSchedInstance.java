package org.ddolib.ddo.examples.talentscheduling;

public record TalentSchedInstance(int nbScene,
                                  int nbActors,
                                  int[] costs,
                                  int[] duration,
                                  int[][] actors) {
}
