package org.ddolib.util;

import org.ddolib.ddo.core.Decision;

import java.util.function.BiFunction;

/**
 * Contains methods helpful for debug.
 */
public class DebugUtil {

    /**
     * Given a state, generates two copies a new state based on the transition model and checks
     * if the two copies have the same hash code and are equal.
     *
     * @param state      A state used to generate two other states.
     * @param decision   The decision used to generates the new states.
     * @param transition The transition function used to generate new states.
     * @param <T>        The type of the states.
     */
    public static <T> void checkHashCodeAndEquality(T state, Decision decision,
                                                    BiFunction<T, Decision, T> transition) {
        T newState = transition.apply(state, decision);
        T duplicate = transition.apply(state, decision);
        String transitionDescription = String.format("\torigin state: %s\n", state);
        transitionDescription += String.format("\tdecision: %s\n", decision);
        transitionDescription += String.format("\tnew state: %s\n", newState);
        transitionDescription += String.format("\tduplicate: %s\n", duplicate);
        if (newState.hashCode() != duplicate.hashCode()) {
            String failureMsg = "Two states generated from the same origin state with the same " +
                    "decision does not have the same hash code !\n";
            failureMsg += transitionDescription;

            throw new RuntimeException(failureMsg);
        } else if (!newState.equals(duplicate)) {
            String failureMsg = "Two states generated from the same origin state with the same " +
                    "decision have the same hash code but are not equals!\n";
            failureMsg += transitionDescription;
            throw new RuntimeException(failureMsg);
        }
    }
}
