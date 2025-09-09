package org.ddolib.ddo.core.compilation;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.util.Optional;

/**
 * The set of parameters used to tweak the compilation of an MDD.
 *
 * @param <T> The type used to model the state of your problem.
 * @param <K> The type of the dominance key.
 */
public class CompilationConfig<T, K> {


    /**
     * How is the mdd being compiled.
     */
    public CompilationType compilationType = null;

    /**
     * A reference to the original problem to solve.
     */
    public Problem<T> problem = null;

    /**
     * The relaxation used to merge nodes in a relaxed mdd.
     */
    public Relaxation<T> relaxation = null;

    /**
     * The variable heuristic used to decide the variable to branch on next.
     */
    public VariableHeuristic<T> variableHeuristic = null;

    /**
     * The state ranking heuristic to choose the nodes to keep and those to discard.
     */
    public StateRanking<T> stateRanking = null;

    /**
     * The subproblem whose state space must be explored.
     */
    public SubProblem<T> residual = null;

    /**
     * The maximum width of the mdd.
     */
    public Integer maxWidth = null;

    /**
     * The heuristic defining a very rough estimation (upper bound) of the optimal value.
     */
    public FastUpperBound<T> fub = null;

    /**
     * The dominance checker used to prune the search space.
     */
    public DominanceChecker<T, K> dominance = null;

    /**
     * The cache used to prune the search space.
     */
    public Optional<SimpleCache<T>> cache = Optional.empty();

    /**
     * The best known lower bound at the time when the dd is being compiled.
     */
    public Double bestLB = null;

    /**
     * The type of cut set used in the compilation.
     */
    public CutSetType cutSetType = null;

    /**
     * Whether the compiled diagram have to be exported to a dot file.
     */
    public Boolean exportAsDot = null;

    /**
     * The debug level of the compilation to add additional checks (see
     * {@link org.ddolib.common.solver.SolverConfig for details}
     */
    public Integer debugLevel = null;


    /**
     * Returns a string representation of this record class.
     *
     * @return Returns a string representation of this record class.
     */
    @Override
    public String toString() {
        return String.format("Compilation: %s - Sub problem: %s - bestLB: %f", compilationType, residual, bestLB);
    }
}
