package org.ddolib.ddo.core.compilation;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;

import java.util.Optional;

/**
 * Represents the configuration parameters used during the compilation
 * of a Multi-valued Decision Diagram (MDD) or similar decision structure.
 * <p>
 * A {@code CompilationConfig} object centralizes all components, heuristics,
 * and algorithmic options required to guide the compilation process.
 * It defines how the search space is explored, bounded, reduced, and pruned.
 * </p>
 *
 * <p>
 * This configuration supports multiple compilation strategies such as exact,
 * relaxed, approximate, or hybrid approaches. It also integrates advanced
 * mechanisms like dominance checking, caching, and large neighborhood search (LNS).
 * </p>
 *
 * @param <T> the type representing the state of the problem
 *
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
 * @see ReductionStrategy
 */
public class CompilationConfig<T> {

    /**
     * The underlying model containing problem-specific components such as
     * constraints, heuristics, and bounds.
     */
    private final Model<T> model;

    /**
     * Specifies how the MDD is compiled.
     * <p>
     * Determines whether the compilation is exact, relaxed, approximate,
     * or hybrid depending on the selected algorithm.
     * </p>
     */
    public CompilationType compilationType = null;

    /**
     * Reference to the optimization or constraint problem being solved.
     */
    public Problem<T> problem = null;

    /**
     * Heuristic used to select the next variable to branch on during compilation.
     */
    public VariableHeuristic<T> variableHeuristic = null;

    /**
     * Ranking heuristic used to prioritize states when pruning nodes
     * in width-limited MDD layers.
     */
    public StateRanking<T> stateRanking = null;

    /**
     * The residual (sub)problem defining the remaining search space
     * to explore during compilation.
     */
    public SubProblem<T> residual = null;

    /**
     * Maximum allowed width (number of nodes) per layer in the MDD.
     * <p>
     * Smaller values improve performance but may reduce solution quality.
     * </p>
     */
    public Integer maxWidth = null;

    /**
     * Fast lower bound heuristic used to estimate the best achievable
     * objective value from a given state.
     */
    public FastLowerBound<T> flb = null;

    /**
     * Dominance checker used to prune dominated states and reduce
     * the size of the search space.
     */
    public DominanceChecker<T> dominance = null;

    /**
     * Best known upper bound on the objective value at compilation time.
     */
    public Double bestUB = null;

    /**
     * Indicates whether the compiled MDD should be exported as a DOT file
     * (Graphviz format) for visualization purposes.
     */
    public Boolean exportAsDot = null;

    /**
     * Debugging level controlling logging and internal consistency checks.
     * <p>
     * Higher levels provide more detailed diagnostics but may impact performance.
     * </p>
     */
    public DebugLevel debugLevel = null;

    /**
     * Strategy used to reduce the width of the MDD by merging or discarding nodes.
     */
    public ReductionStrategy<T> reductionStrategy = null;

    /**
     * Relaxation model used to approximate or merge states in relaxed MDD compilation.
     */
    public Relaxation<T> relaxation = null;

    /**
     * Optional cache to store previously computed states and avoid redundant work.
     */
    public Optional<SimpleCache<T>> cache = Optional.empty();

    /**
     * Defines the cut set strategy used to control node expansion and pruning.
     */
    public CutSetType cutSetType = null;

    /**
     * Initial solution provided to guide the search (e.g., for heuristics or LNS).
     */
    public int[] initialSolution = null;

    /**
     * Stores the current or best solution found during compilation.
     */
    public int[] solution = null;

    /**
     * Probability parameter used in randomized strategies (e.g., LNS or heuristics).
     */
    public double probability = 0;

    /**
     * Indicates whether Large Neighborhood Search (LNS) should be used
     * to improve solutions during compilation.
     */
    public Boolean useLNS = null;

    /**
     * Constructs a new compilation configuration for the given model.
     *
     * @param model the model containing problem-specific components
     *              (heuristics, bounds, dominance, etc.)
     */
    public CompilationConfig(Model<T> model) {
        this.model = model;
    }

    /**
     * Returns a human-readable string representation of this configuration.
     *
     * @return a formatted string containing the compilation type,
     *         residual problem, and best known upper bound
     */
    @Override
    public String toString() {
        return String.format(
                "Compilation: %s - Sub problem: %s - bestUB: %f",
                compilationType,
                residual,
                bestUB
        );
    }

    /**
     * Creates a shallow copy of this configuration.
     * <p>
     * Some components are re-fetched from the underlying model
     * (e.g., heuristics, bounds, dominance), while others are directly copied.
     * </p>
     *
     * @return a new {@code CompilationConfig} instance with the same parameters
     */
    public CompilationConfig<T> copy() {
        CompilationConfig<T> compilation = new CompilationConfig<>(this.model);

        compilation.compilationType = this.compilationType;
        compilation.problem = model.problem();
        compilation.variableHeuristic = model.variableHeuristic();
        compilation.stateRanking = this.stateRanking;
        compilation.residual = this.residual;
        compilation.maxWidth = this.maxWidth;
        compilation.flb = model.lowerBound();
        compilation.dominance = model.dominance();
        compilation.bestUB = this.bestUB;

        compilation.exportAsDot = this.exportAsDot;
        compilation.debugLevel = model.debugMode();
        compilation.relaxation = this.relaxation;
        compilation.cutSetType = this.cutSetType;
        compilation.cache = this.cache;

        compilation.initialSolution = this.initialSolution;
        compilation.probability = this.probability;
        compilation.useLNS = this.useLNS;
        compilation.solution = this.initialSolution;

        return compilation;
    }
}