package org.ddolib.nolayer.util.debug;

import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.solver.Solver;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class providing methods useful for debugging state transitions.
 * <p>
 * This class helps to verify the correctness of the state transition function by
 * checking that states generated from the same origin with the same label
 * are both equal and have the same hash code.
 */
public class DebugUtil {

    private DebugUtil() {
    }

    /**
     * Checks the consistency of a transition function by generating two states
     * from the same origin state and label, then verifying that they are equal
     * and have the same hash code.
     * <p>
     * If the generated states either differ in hash code or are not equal, a
     * {@link RuntimeException} is thrown with detailed information about the
     * origin state, label, and resulting states.
     *
     * @param state      the original state from which the new states are generated
     * @param label      the label applied to the original state
     * @param transition the transition function that generates a new state from a state and a label
     * @param <T>        the type of the states
     * @throws RuntimeException if the generated states are not equal or have different hash codes
     */
    public static <T> void checkHashCodeAndEquality(T state, int label, BiFunction<T, Integer, T> transition) {
        T newState = transition.apply(state, label);
        T duplicate = transition.apply(state, label);

        String transitionDescription = String.format("\torigin state: %s\n", state);
        transitionDescription += String.format("\tlabel: %s\n", label);
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

    /**
     * Given a set of states, checks if the {@link org.ddolib.nolayer.modeling.FastLowerBound} is
     * admissible, i.e., whether the bound does not overestimate the path from the states to a
     * target state.
     * <p>
     * The checks are performed by running solvers starting from the tested states.
     *
     * @param toCheck the states to check
     * @param model   a model used to initialize solvers run during the tests
     * @param solver  returns a solver given a root state
     * @param <T>     the type of the states
     */
    public static <T> void checkFlbAdmissibility(Set<T> toCheck, Model<T> model, Function<T, Solver> solver) {
        for (T current : toCheck) {
            Solver internalSolver = solver.apply(current);
            double currentFlb = model.lowerBound().fastLowerBound(current);
            internalSolver.minimize(s -> false, (sol, stats) -> {
            });

            Optional<Double> shortestFromCurrent = internalSolver.bestValue();
            List<Integer> shortestPath = internalSolver.bestSolution();
            if (shortestFromCurrent.isPresent() && currentFlb - 1e-10 > shortestFromCurrent.get()) {
                DecimalFormat df = new DecimalFormat("#.#########");
                String failureMsg = "Your lower bound is not admissible.\n" +
                        "State: " + current.toString() + "\n" +
                        "Path estimation: " + df.format(currentFlb) + "\n" +
                        "Shortest path length to end: " + df.format(shortestFromCurrent.get())
                        + "\n\nFull Path to end:\n" +
                        shortestPath.stream().map(d -> "\t" + d).collect(Collectors.joining("\n"))
                        + "\n";

                throw new RuntimeException(failureMsg);
            }
        }
    }
}
