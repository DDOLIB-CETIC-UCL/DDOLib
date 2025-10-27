package org.ddolib.util;

import org.ddolib.ddo.core.Decision;

import java.util.function.BiFunction;

/**
 * Utility class providing methods useful for debugging state transitions.
 * <p>
 * This class helps to verify the correctness of the state transition function by
 * checking that states generated from the same origin with the same decision
 * are both equal and have the same hash code.
 */
public class DebugUtil {

    /**
     * Checks the consistency of a transition function by generating two states
     * from the same origin state and decision, then verifying that they are equal
     * and have the same hash code.
     * <p>
     * If the generated states either differ in hash code or are not equal, a
     * {@link RuntimeException} is thrown with detailed information about the
     * origin state, decision, and resulting states.
     *
     * @param state      the original state from which the new states are generated
     * @param decision   the decision applied to the original state
     * @param transition the transition function that generates a new state from a state and a decision
     * @param <T>        the type of the states
     * @throws RuntimeException if the generated states are not equal or have different hash codes
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
