package org.ddolib.util.testbench;

import org.ddolib.modeling.nolayer.DdoModel;
import org.ddolib.modeling.nolayer.Problem;

import java.util.List;

/**
 * Defines how to generate problem and model for tests in the NoLayer API
 *
 * @param <T> The type of states.
 * @param <P> The type of problem to test.
 */
abstract public class NoLayerTestDataSupplier<T, P extends Problem<T>> {

    /**
     * Generates {@link Problem} instances to test.
     *
     * @return A list of problems used for tests.
     */
    abstract protected List<P> generateProblems();

    /**
     * Given a problem instance returns the whole model used to solve this problem.
     *
     * @param problem The problem to solve.
     * @return A model containing all the components to solve it.
     */
    abstract protected DdoModel<T> model(P problem);
}
