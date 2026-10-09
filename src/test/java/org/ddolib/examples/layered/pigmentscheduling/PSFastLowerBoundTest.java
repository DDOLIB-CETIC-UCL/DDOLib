package org.ddolib.examples.layered.pigmentscheduling;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests of the fast lower bound of the Pigment Sequencing Problem ({@link PSFastLowerBound}). */
public class PSFastLowerBoundTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void testRootBoundIsPositiveAndValid(int instance) throws IOException {
        PSProblem problem =
                new PSProblem(
                        Path.of("data", "PSP", "instancesWith5items", "" + instance).toString());
        Set<Integer> vars =
                IntStream.range(0, problem.nbVars()).boxed().collect(Collectors.toSet());
        double bound = new PSFastLowerBound(problem).fastLowerBound(problem.initialState(), vars);
        double optimum = problem.optimalValue().orElseThrow();

        // all the costs are positive: the stocking part of the bound must not be negative
        assertTrue(bound > 0, "The bound " + bound + " should be positive");
        assertTrue(bound <= optimum, "The bound " + bound + " exceeds the optimum " + optimum);
    }
}
