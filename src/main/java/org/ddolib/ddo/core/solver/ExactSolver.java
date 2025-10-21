package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.compilation.CompilationConfig;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.modeling.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Solver that compile an unique exact mdd.
 * <p>
 * <b>Note:</b> By only using exact mdd, this solver can consume a lot of memory. It is advisable to use this solver to
 * test your model on small instances. See {@link SequentialSolver} for other use cases.
 *
 * @param <T> The type of states.
 */
public final class ExactSolver<T> implements Solver {

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
     * A heuristic to choose the next variable to branch on when developing a DD
     */
    private final VariableHeuristic<T> varh;

    /**
     * The heuristic defining a lower bound of the optimal value.
     */
    private final FastLowerBound<T> flb;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T> dominance;

    /**
     * This is the cache used to prune the search tree
     */
    private final Optional<SimpleCache<T>> cache;


    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    private Optional<Double> bestValue = Optional.empty();


    private final VerbosityLevel verbosityLevel;

    /**
     * Whether we want to export the first explored restricted and relaxed mdd.
     */
    private final boolean exportAsDot;

    /**
     * The debug level of the compilation to add additional checks (see
     * {@link org.ddolib.modeling.DebugLevel for details}
     */
    private final DebugLevel debugLevel;


    /**
     * Creates a fully qualified instance.
     *
     * @param model All parameters needed ton configure the solver.
     *
     */
    public ExactSolver(DdoModel<T> model) {
        this.problem = model.problem();
        this.relax = model.relaxation();
        this.ranking = model.ranking();
        this.varh = model.variableHeuristic();
        this.flb = model.lowerBound();
        this.dominance = model.dominance();
        this.cache = model.useCache() ? Optional.of(new SimpleCache<>()) : Optional.empty();
        this.bestSol = Optional.empty();
        this.verbosityLevel = model.verbosityLevel();
        this.exportAsDot = model.exportDot();
        this.debugLevel = model.debugMode();
    }

    @Override
    public SearchStatistics minimize(Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        long start = System.currentTimeMillis();
        SubProblem<T> root = new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Double.POSITIVE_INFINITY,
                Collections.emptySet());
        cache.ifPresent(c -> c.initialize(problem));

        CompilationConfig<T> compilation = new CompilationConfig<>();
        compilation.compilationType = CompilationType.Exact;
        compilation.problem = this.problem;
        compilation.relaxation = this.relax;
        compilation.variableHeuristic = this.varh;
        compilation.stateRanking = this.ranking;
        compilation.residual = root;
        compilation.maxWidth = Integer.MAX_VALUE;
        compilation.flb = flb;
        compilation.dominance = this.dominance;
        compilation.cache = this.cache;
        compilation.bestUB = Double.POSITIVE_INFINITY;
        compilation.cutSetType = CutSetType.LastExactLayer;
        compilation.exportAsDot = this.exportAsDot;
        compilation.debugLevel = this.debugLevel;

        DecisionDiagram<T> mdd = new LinkedDecisionDiagram<>(compilation);
        mdd.compile();
        extractBest(mdd);
        if (exportAsDot) {
            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            exportDot(mdd.exportAsDot(),
                    Paths.get("output", problemName + "_exact.dot").toString());
        }

        long end = System.currentTimeMillis();

        return new SearchStatistics(SearchStatus.OPTIMAL,
                1,
                1,
                end - start,
                bestValue.orElse(Double.POSITIVE_INFINITY),
                0);
    }


    @Override
    public Optional<Double> bestValue() {
        return bestValue;
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * Method that extract the best solution from the compiled mdd
     */
    private void extractBest(DecisionDiagram<T> mdd) {
        Optional<Double> ddval = mdd.bestValue();
        if (ddval.isPresent()) {
            bestSol = mdd.bestSolution();
            bestValue = ddval;
            DecimalFormat df = new DecimalFormat("#.##########");
            if (verbosityLevel != VerbosityLevel.SILENT)
                System.out.printf("best solution found: %s\n", df.format(ddval.get()));
        }
    }

    private void exportDot(String dot, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(dot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
