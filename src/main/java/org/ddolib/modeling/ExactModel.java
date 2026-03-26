package org.ddolib.modeling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;

import java.util.Iterator;

/**
 * Defines the interface for a Dynamic Decision Diagram Optimization (DDO) model, used by the
 * {@link org.ddolib.ddo.core.solver.ExactSolver}
 * <p>
 * It specifies the
 * {@link Problem} instance to solve and optionally provides custom heuristics,
 * dominance relations, and debugging or verbosity configurations.
 * </p>
 *
 * @param <T> the type representing the state space of the problem
 */
public abstract class ExactModel<T> implements DdoModel<T> {


    @Override
    public final Relaxation<T> relaxation() {
        return new DummyRelaxation<>();
    }

    @Override
    public final StateRanking<T> ranking() {
        return DdoModel.super.ranking();
    }

    @Override
    public final WidthHeuristic<T> widthHeuristic() {
        return DdoModel.super.widthHeuristic();
    }

    @Override
    public final Frontier<T> frontier() {
        return DdoModel.super.frontier();
    }

    @Override
    public final boolean useCache() {
        return DdoModel.super.useCache();
    }

    @Override
    public final ReductionStrategy<T> relaxStrategy() {
        return DdoModel.super.relaxStrategy();
    }

    @Override
    public final ReductionStrategy<T> restrictStrategy() {
        return DdoModel.super.restrictStrategy();
    }

    @Override
    public final StateDistance<T> stateDistance() {
        return DdoModel.super.stateDistance();
    }

    @Override
    public final VariableHeuristic<T> variableHeuristic() {
        return DdoModel.super.variableHeuristic();
    }

    /**
     * Relaxation that does nothing
     */
    private static class DummyRelaxation<T> implements Relaxation<T> {

        @Override
        public T mergeStates(Iterator<T> states) {
            return null;
        }

        @Override
        public double relaxEdge(T from, T to, T merged, Decision d, double cost) {
            return cost;
        }
    }
}


