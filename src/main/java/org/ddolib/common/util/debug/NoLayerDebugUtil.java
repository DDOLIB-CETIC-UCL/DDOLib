package org.ddolib.common.util.debug;

import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.solver.Solver;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NoLayerDebugUtil {

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
