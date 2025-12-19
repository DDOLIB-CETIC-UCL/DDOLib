package org.ddolib.ddo.core.compilation;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;

import java.util.Optional;

/**
 * Represents the configuration parameters used during the compilation
 * of a Multi-valued Decision Diagram (MDD) or similar decision structure.
 * <p>
 * A {@code CompilationConfig} object stores all components and heuristics
 * required to guide the compilation process — including relaxations, heuristics,
 * bounds, and pruning mechanisms.
 * </p>
 *
 * @param <T> the type representing the state of the problem
 * @see CompilationType
 * @see Problem
 * @see Relaxation
 * @see VariableHeuristic
 * @see StateRanking
 * @see SubProblem
 * @see FastLowerBound
 * @see DominanceChecker
 * @see SimpleCache
 * @see CutSetType
 * @see DebugLevel
 */
public class CompilationConfig<T> {

    private final DdoModel<T> model;
    /**
     * Specifies how the MDD is compiled.
     * <p>Determines whether the compilation is exact, relaxed,
     * approximate, or hybrid, depending on the algorithm used.</p>
     */
    public CompilationType compilationType = null;
    /**
     * Reference to the original optimization or constraint problem to be solved.
     */
    public Problem<T> problem = null;
    /**
     * Defines the relaxation model used to merge or approximate nodes
     * during relaxed MDD compilation.
     */
    public Relaxation<T> relaxation = null;
    /**
     * Heuristic used to decide which variable to branch on next
     * during the compilation process.
     */
    public VariableHeuristic<T> variableHeuristic = null;
    /**
     * Ranking heuristic used to prioritize states and select
     * which nodes to keep when pruning the MDD.
     */
    public StateRanking<T> stateRanking = null;
    /**
     * Represents the subproblem or residual problem whose state space
     * must be explored in the compilation process.
     */
    public SubProblem<T> residual = null;
    /**
     * Defines the maximum number of nodes (width) allowed per MDD layer.
     * <p>Controls the trade-off between accuracy and performance.</p>
     */
    public Integer maxWidth = null;
    /**
     * Heuristic used to compute a quick estimation (lower bound)
     * of the optimal objective value during search.
     */
    public FastLowerBound<T> flb = null;
    /**
     * Dominance checker used to identify and prune dominated states
     * from the search space.
     */
    public DominanceChecker<T> dominance = null;
    /**
     * Optional cache used to avoid redundant computations
     * and prune the search space efficiently.
     */
    public Optional<SimpleCache<T>> cache = Optional.empty();
    /**
     * Stores the best known upper bound on the objective function
     * at the time of MDD compilation.
     */
    public Double bestUB = null;
    /**
     * Defines the type of cut set used to control the structure
     * and pruning of the MDD during compilation.
     */
    public CutSetType cutSetType = null;
    /**
     * Indicates whether the compiled MDD should be exported
     * to a DOT file (Graphviz format) for visualization.
     */
    public Boolean exportAsDot = null;
    /**
     * Defines the debugging level of the compilation process.
     * <p>Higher levels include more internal consistency checks
     * and detailed logging, at the cost of runtime performance.</p>
     *
     * @see DebugLevel
     */
    public DebugLevel debugLevel = null;

    /**
     * Initialize the parameters of the compilation.
     *
     * @param model the model containing the problem specific parameter of the compilation
     */
    public CompilationConfig(DdoModel<T> model) {
        this.model = model;
    }

    /**
     * Returns a human-readable string representation of this configuration.
     *
     * @return a formatted string containing the compilation type,
     * residual problem, and best known upper bound
     */
    @Override
    public String toString() {
        return String.format("Compilation: %s - Sub problem: %s - bestUB: %f", compilationType, residual, bestUB);
    }

    /**
     * Returns a copy of this configuration.
     *
     * @return a copy of this configuration.
     */
    public CompilationConfig<T> copy() {
        CompilationConfig<T> compilation = new CompilationConfig<>(this.model);

        compilation.compilationType = this.compilationType;
        compilation.problem = model.problem();
        compilation.relaxation = model.relaxation();
        compilation.variableHeuristic = model.variableHeuristic();
        compilation.stateRanking = model.ranking();
        compilation.residual = this.residual;
        compilation.maxWidth = this.maxWidth;
        compilation.flb = model.lowerBound();
        compilation.dominance = model.dominance();
        compilation.cache = this.cache;
        compilation.bestUB = this.bestUB;
        compilation.cutSetType = this.cutSetType;
        compilation.exportAsDot = this.exportAsDot;
        compilation.debugLevel = model.debugMode();

        return compilation;
    }
}
