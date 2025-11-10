package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.compilation.CompilationConfig;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Solver that compile a unique restricted MDD
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 */
public final class RestrictionSolver<T, K> implements Solver {
    /**
     * The problem we want to minimize
     */
    private final Problem<T> problem;
    /**
     * A suitable relaxation for the problem we want to minimize
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
     * Set of nodes that must still be explored before
     * the problem can be considered 'solved'.
     * <p>
     * # Note:
     * This fringe orders the nodes by lower bound (so the lowest lower bound is going
     * to pop first). So, it is guaranteed that the lower-bound of the first
     * node being popped is a lower bound on the value reachable by exploring
     * any of the nodes remaining on the fringe. As a consequence, the
     * exploration can be stopped as soon as a node with an lb &#8804; current best
     * lower bound is popped.
     */
    private final Frontier<T> frontier;


    /**
     * Value of the best known upper bound.
     */
    private double bestUB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * The heuristic defining a very rough estimation (lower bound) of the optimal value.
     */
    private final FastLowerBound<T> flb;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T, K> dominance;

    /**
     * This is the cache used to prune the search tree
     */
    private Optional<SimpleCache<T>> cache;

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
     * <ul>
     *     <li>0: no additional tests</li>
     *     <li>1: checks if the lower bound is well-defined</li>
     *     <li>2: 1 + export diagram with failure in {@code output/failure.dot}</li>
     * </ul>
     */
    private final int debugLevel;


    /*

    <ul>
                <li>0: no additional tests (default)</li>
                <li>1: checks if the lower bound is well-defined</li>
                <li>2: 1 + export diagram with failure in {@code output/failure.dot}</li>
            </ul>
          </li>
     */

    /**
     * Strategy to select which nodes should be merged together on a relaxed DD.
     */
    private final ReductionStrategy<T> relaxStrategy;

    /**
     * Strategy to select which nodes should be dropped on a restricted DD.
     */
    private final ReductionStrategy<T> restrictStrategy;

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
     *     <li>An implementation of {@link FastLowerBound}</li>
     *     <li>An implementation of {@link DominanceChecker}</li>
     *     <li>A time limit</li>
     *     <li>A gap limit</li>
     *     <li>A verbosity level</li>
     *     <li>A boolean to export some mdd as .dot file</li>
     *     <li>A debug level:
     *          <ul>
     *               <li>0: no additional tests (default)</li>
     *               <li>1: checks if the upper bound is well-defined and if the hash code
     *               of the states are coherent</li>
     *               <li>2: 1 + export diagram with failure in {@code output/failure.dot}</li>
     *             </ul>
     *     </li>
     * </ul>
     *
     * @param config All the parameters needed to configure the solver.
     */
    public RestrictionSolver(SolverConfig<T, K> config) {
        this.problem = config.problem;
        this.relax = config.relax;
        this.varh = config.varh;
        this.ranking = config.ranking;
        this.width = config.width;
        this.flb = config.flb;
        this.dominance = config.dominance;
        this.cache = config.cache == null ? Optional.empty() : Optional.of(config.cache);
        this.frontier = config.frontier;
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verbosityLevel = config.verbosityLevel;
        this.exportAsDot = config.exportAsDot;
        this.debugLevel = config.debugLevel;
        this.relaxStrategy = config.relaxStrategy;
        this.restrictStrategy = config.restrictStrategy;
    }


    @Override
    public SearchStatistics minimize() {
        long start = System.currentTimeMillis();
        int printInterval = 500; //ms; half a second
        long nextPrint = start + printInterval;
        int nbIter = 0;
        int queueMaxSize = 0;

        SubProblem<T> sub = root();
        int maxWidth = width.maximumWidth(sub.getState());
        CompilationConfig<T, K> compilation = new CompilationConfig<>();
        compilation.compilationType = CompilationType.Restricted;
        compilation.problem = this.problem;
        compilation.relaxation = this.relax;
        compilation.variableHeuristic = this.varh;
        compilation.stateRanking = this.ranking;
        compilation.residual = sub;
        compilation.maxWidth = maxWidth;
        compilation.flb = flb;
        compilation.dominance = this.dominance;
        compilation.cache = this.cache;
        compilation.bestUB = this.bestUB;
        compilation.cutSetType = frontier.cutSetType();
        compilation.exportAsDot = this.exportAsDot;
        compilation.debugLevel = this.debugLevel;
        compilation.reductionStrategy = restrictStrategy;
        DecisionDiagram<T, K> relaxedMdd = new LinkedDecisionDiagram<>(compilation);

        relaxedMdd.compile();
        if (compilation.compilationType == CompilationType.Relaxed && relaxedMdd.relaxedBestPathIsExact()
                && frontier.cutSetType() == CutSetType.Frontier) {
            maybeUpdateBest(relaxedMdd, exportAsDot);
        }
        if (exportAsDot) {
            if (!relaxedMdd.isExact())
                relaxedMdd.bestSolution(); // to update the best edges' color
            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            exportDot(relaxedMdd.exportAsDot(),
                    Paths.get("output", problemName + "_relaxed.dot").toString());
        }
        maybeUpdateBest(relaxedMdd, false);

        long end = System.currentTimeMillis();
        return new SearchStatistics(nbIter, queueMaxSize, end - start,
                SearchStatistics.SearchStatus.OPTIMAL, 0.0,
                cache.map(SimpleCache::stats).orElse("noCache"));
    }

    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestUB);
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
                Double.NEGATIVE_INFINITY,
                Collections.emptySet());
    }

    /**
     * Updates the best known node and upper bound in
     * case the best value of the current `mdd` expansion improves the current
     * bounds.
     */
    private void maybeUpdateBest(DecisionDiagram<T, K> currentMdd, boolean exportDot) {
        Optional<Double> ddval = currentMdd.bestValue();
        if (ddval.isPresent() && ddval.get() < bestUB) {
            bestUB = ddval.get();
            bestSol = currentMdd.bestSolution();
            if (verbosityLevel >= 1) System.out.println("new best: " + bestUB);
        } else if (exportDot) {
            currentMdd.exportAsDot(); // to be sure to update the color of the edges.
        }
    }

    /**
     * If necessary, tightens the bound of nodes in the cutset of `mdd` and
     * then add the relevant nodes to the shared fringe.
     */
    private void enqueueCutset(DecisionDiagram<T, K> currentMdd) {
        Iterator<SubProblem<T>> cutset = currentMdd.exactCutset();
        while (cutset.hasNext()) {
            SubProblem<T> cutsetNode = cutset.next();
            if (cutsetNode.getLowerBound() < bestUB) {
                frontier.push(cutsetNode);
            }
        }
    }

    private void exportDot(String dot, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(dot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SearchStatistics.SearchStatus currentSearchStatus(double gap) {
        if (bestSol.isEmpty()) {
            if (bestUB == Double.POSITIVE_INFINITY) {
                return SearchStatistics.SearchStatus.UNKNOWN;
            } else {
                return SearchStatistics.SearchStatus.UNSAT;
            }
        } else {
            if (gap > 0.0)
                return SearchStatistics.SearchStatus.SAT;
            else return SearchStatistics.SearchStatus.OPTIMAL;
        }
    }

    private double gap() {
        if (frontier.isEmpty()) {
            return 0.0;
        } else {
            double bestInFrontier = frontier.bestInFrontier();
            return 100 * (bestUB - bestInFrontier) / bestUB;
        }
    }
}
