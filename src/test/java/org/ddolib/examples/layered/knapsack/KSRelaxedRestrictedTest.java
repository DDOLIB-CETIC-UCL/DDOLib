package org.ddolib.examples.layered.knapsack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests of {@link Solvers#relaxedDdo} and {@link Solvers#restrictedDdo}, which compile a single
 * relaxed or restricted decision diagram from the root.
 */
public class KSRelaxedRestrictedTest {

    private static final Path DIR = Path.of("src", "test", "resources", "Knapsack");
    private static final KSTestDataSupplier SUPPLIER = new KSTestDataSupplier(DIR);

    static Stream<KSProblem> problems() throws IOException {
        return Files.list(DIR)
                .sorted()
                .map(
                        p -> {
                            try {
                                return new KSProblem(p.toString());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .filter(p -> p.optimalValue().isPresent());
    }

    @ParameterizedTest
    @MethodSource("problems")
    void testRelaxedDdoGivesALowerBound(KSProblem problem) {
        Solution sol = Solvers.relaxedDdo(SUPPLIER.model(problem));

        assertTrue(sol.value() <= problem.optimalValue().get() + 1e-9);
    }

    @ParameterizedTest
    @MethodSource("problems")
    void testRestrictedDdoGivesAFeasibleSolution(KSProblem problem)
            throws InvalidSolutionException {
        Solution sol = Solvers.restrictedDdo(SUPPLIER.model(problem));

        assertFalse(sol.isEmpty());
        assertEquals(sol.value(), problem.evaluate(sol.solution()), 1e-9);
        assertTrue(sol.value() >= problem.optimalValue().get() - 1e-9);
    }
}
