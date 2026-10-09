package org.ddolib.nolayer.testbench;

import java.util.List;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.Problem;

/**
 * Defines how to generate problem and model for tests in the NoLayer API.
 *
 * @param <T> The type of states.
 * @param <P> The type of problem to test.
 */
public abstract class NoLayerTestDataSupplier<T, P extends Problem<T>> {

    /**
     * Generates {@link Problem} instances to test.
     *
     * @return A list of problems used for tests.
     */
    protected abstract List<P> generateProblems();

    /**
     * Given a problem instance returns the whole model used to solve this problem.
     *
     * @param problem The problem to solve.
     * @return A model containing all the components to solve it.
     */
    protected abstract DdoModel<T> model(P problem);
}
