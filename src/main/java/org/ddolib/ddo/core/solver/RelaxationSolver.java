package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.compilation.CompilationInput;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Solver to only compute a relaxed DD from the root
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 */
public final class RelaxationSolver<T, K> implements Solver {
    /**
     * The problem we want to maximize
     */
    private final Problem<T> problem;
    /**
     * A suitable relaxation for the problem we want to maximize
     */
    private final Relaxation<T> relax;
    /**
     * A heuristic to identify the most promising nodes
     */
    private final StateRanking<T> ranking;
    /**
     * A heuristic to choose the maximum width of the DD you compile
     */
    private final WidthHeuristic<T> width;
    /**
     * A heuristic to choose the next variable to branch on when developing a DD
     */
    private final VariableHeuristic<T> varh;

    /**
     * Your implementation (just like the parallel version) will reuse the same
     * data structure to compile all mdds.
     * <p>
     * # Note:
     * This approach is recommended, however we do not force this design choice.
     * You might decide against reusing the same object over and over (even though
     * it has been designed to be reused). Should you decide to not reuse this
     * object, then you can simply ignore this field (and remove it altogether).
     */
    private final DecisionDiagram<T, K> mdd;

    /**
     * Value of the best known lower bound.
     */
    private double bestLB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * The heuristic defining a very rough estimation (upper bound) of the optimal value.
     */
    private final FastUpperBound<T> fub;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T, K> dominance;

    /**
     * This is the cache used to prune the search tree
     */
    private Optional<SimpleCache<T>> cache;

    /**
     * Only the first restricted mdd can be exported to a .dot file
     */
    private boolean firstRestricted = true;
    /**
     * Only the first relaxed mdd can be exported to a .dot file
     */
    private boolean firstRelaxed = true;


    /**
     * <ul>
     *     <li>0: no verbosity</li>
     *     <li>1: display newBest whenever there is a newBest</li>
     *     <li>2: 1 + statistics about the front every half a second (or so)</li>
     *     <li>3: 2 + every developed sub-problem</li>
     *     <li>4: 3 + details about the developed state</li>
     * </ul>
     * <p>
     * <p>
     * 3: 2 + every developed sub-problem
     * 4: 3 + details about the developed state
     */
    private final int verbosityLevel;

    /**
     * Whether we want to export the first explored restricted and relaxed mdd.
     */
    private final boolean exportAsDot;

    /**
     * Strategy to select which nodes should be merged together on a relaxed DD.
     */
    private final ReductionStrategy<T> relaxStrategy;

    /**
     * <ul>
     *     <li>0: no additional tests</li>
     *     <li>1: checks if the upper bound is well-defined</li>
     *     <li>2: 1 + export diagram with failure in {@code output/failure.dot}</li>
     * </ul>
     */
    private final int debugLevel;

    /**
     * Creates a fully qualified instance. The parameters of this solver are given via a
     * {@link SolverConfig}<br><br>
     *
     * <b>Mandatory parameters:</b>
     * <ul>
     *     <li>An implementation of {@link Problem}</li>
     *     <li>An implementation of {@link Relaxation}</li>
     *     <li>An implementation of {@link StateRanking}</li>
     *     <li>An implementation of {@link VariableHeuristic}</li>
     *     <li>An implementation of {@link WidthHeuristic}</li>
     *     <li>An implementation of {@link Frontier}</li>
     * </ul>
     * <br>
     * <b>Optional parameters: </b>
     * <ul>
     *     <li>An implementation of {@link FastUpperBound}</li>
     *     <li>An implementation of {@link DominanceChecker}</li>
     *     <li>A time limit</li>
     *     <li>A gap limit</li>
     *     <li>A verbosity level</li>
     *     <li>A boolean to export some mdd as .dot file</li>
     * </ul>
     *
     * @param config All the parameters needed to configure the solver.
     */
    public RelaxationSolver(SolverConfig<T, K> config) {
        this.problem = config.problem;
        this.relax = config.relax;
        this.varh = config.varh;
        this.ranking = config.ranking;
        this.width = config.width;
        this.fub = config.fub;
        this.dominance = config.dominance;
        this.cache = config.cache == null ? Optional.empty() : Optional.of(config.cache);
        this.mdd = new LinkedDecisionDiagram<>();
        this.bestLB = Double.NEGATIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verbosityLevel = config.verbosityLevel;
        this.exportAsDot = config.exportAsDot;
        this.relaxStrategy = config.relaxStrategy;
        this.debugLevel = config.debugLevel;
    }


    @Override
    public SearchStatistics maximize() {
        long start = System.currentTimeMillis();
        SubProblem<T> root = root();
        int maxWidth = width.maximumWidth(root.getState());
        // 2. RELAXATION
        CompilationInput<T,K> compilation = new CompilationInput<>(
                CompilationType.Relaxed,
                problem,
                relax,
                varh,
                ranking,
                root,
                maxWidth,
                fub,
                dominance,
                cache,
                bestLB,
                CutSetType.None,
                relaxStrategy,
                exportAsDot && firstRelaxed,
                debugLevel
        );
        mdd.compile(compilation);

        maybeUpdateBest(exportAsDot);
        long end = System.currentTimeMillis();
        return new SearchStatistics(1, 0,end-start, SearchStatistics.SearchStatus.UNKNOWN, 0.0);
    }

    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestLB);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * @return the root subproblem
     */
    private SubProblem<T> root() {
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Double.POSITIVE_INFINITY,
                Collections.emptySet());
    }

    /**
     * This private method updates the best known node and lower bound in
     * case the best value of the current `mdd` expansion improves the current
     * bounds.
     */
    private void maybeUpdateBest(boolean exportDot) {
        Optional<Double> ddval = mdd.bestValue();
        if (ddval.isPresent() && ddval.get() > bestLB) {
            bestLB = ddval.get();
            bestSol = mdd.bestSolution();
            if (verbosityLevel >= 1) System.out.println("new best: " + bestLB);
        } else if (exportDot) {
            mdd.exportAsDot(); // to be sure to update the color of the edges.
        }
    }

}
