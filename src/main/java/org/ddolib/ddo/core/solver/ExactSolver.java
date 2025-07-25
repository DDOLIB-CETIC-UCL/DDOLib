package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.compilation.CompilationInput;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Solver that compile an unique exact mdd.
 * <p>
 * <b>Note:</b> By only using exact mdd, this solver can consume a lot of memory. It is advisable to use this solver to
 * test your model on small instances. See {@link SequentialSolver} or {@link ParallelSolver} for other use cases.
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 */
public final class ExactSolver<T, K> implements Solver {

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
     * The heuristic defining a very rough estimation (upper bound) of the optimal value.
     */
    private final FastUpperBound<T> fub;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T, K> dominance;

    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * Creates a new instance.
     *
     * @param problem   The problem we want to maximize.
     * @param relax     A suitable relaxation for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking   A heuristic to identify the most promising nodes.
     * @param fub       The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param dominance The dominance object that will be used to prune the search space.
     */
    public ExactSolver(final Problem<T> problem,
                       final Relaxation<T> relax,
                       final VariableHeuristic<T> varh,
                       final StateRanking<T> ranking,
                       final FastUpperBound<T> fub,
                       final DominanceChecker<T, K> dominance) {
        this.problem = problem;
        this.relax = relax;
        this.ranking = ranking;
        this.varh = varh;
        this.fub = fub;
        this.dominance = dominance;
        this.mdd = new LinkedDecisionDiagram<>();
        this.bestSol = Optional.empty();
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel, boolean exportAsDot) {
        long start = System.currentTimeMillis();
        SubProblem<T> root = new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Double.POSITIVE_INFINITY,
                Collections.emptySet());

        CompilationInput<T, K> compilation = new CompilationInput<>(
                CompilationType.Exact,
                problem,
                relax,
                varh,
                ranking,
                root,
                Integer.MAX_VALUE,
                fub,
                dominance,
                Double.NEGATIVE_INFINITY,
                CutSetType.LastExactLayer,
                exportAsDot
        );
        mdd.compile(compilation);
        extractBest(verbosityLevel);
        if (exportAsDot) {
            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            exportDot(mdd.exportAsDot(),
                    Paths.get("output", problemName + "_exact.dot").toString());
        }

        long end = System.currentTimeMillis();
        return new SearchStatistics(1, 1, end - start, SearchStatistics.SearchStatus.OPTIMAL, 0.0);
    }

    @Override
    public SearchStatistics maximize() {
        return maximize(0, false);
    }

    @Override
    public Optional<Double> bestValue() {
        return mdd.bestValue();
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * Method that extract the best solution from the compiled mdd
     */
    private void extractBest(int verbosityLevel) {
        Optional<Double> ddval = mdd.bestValue();
        if (ddval.isPresent()) {
            bestSol = mdd.bestSolution();
            DecimalFormat df = new DecimalFormat("#.##########");
            if (verbosityLevel >= 1)
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
