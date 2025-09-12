package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
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
     *     <li>A debug level:
     *          <ul>
     *               <li>0: no additional tests (default)</li>
     *               <li>1: checks if the upper bound is well-defined and if the hash code
     *               of the states are coherent</li>
     *               <li>2: 1 + export diagram with failure in {@code output/failure.dot}</li>
     *           </ul>
     *     </li>
     * </ul>
     *
     * @param config All the parameters needed to configure the solver.
     */
    public ExactSolver(SolverConfig<T, K> config) {
        this.problem = config.problem;
        this.relax = config.relax;
        this.ranking = config.ranking;
        this.varh = config.varh;
        this.fub = config.fub;
        this.dominance = config.dominance;
        this.mdd = new LinkedDecisionDiagram<>();
        this.bestSol = Optional.empty();
        this.verbosityLevel = config.verbosityLevel;
        this.exportAsDot = config.exportAsDot;
        this.debugLevel = config.debugLevel;
    }

    @Override
    public SearchStatistics maximize() {
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
                Integer.MAX_VALUE, // exact mdd => max width = infinite
                fub,
                dominance,
                Double.NEGATIVE_INFINITY,
                CutSetType.LastExactLayer,
                exportAsDot,
                debugLevel
        );
        mdd.compile(compilation);
        extractBest();
        if (exportAsDot) {
            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            exportDot(mdd.exportAsDot(),
                    Paths.get("output", problemName + "_exact.dot").toString());
        }

        long end = System.currentTimeMillis();
        return new SearchStatistics(1, 1, end - start, SearchStatistics.SearchStatus.OPTIMAL, 0.0);
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
    private void extractBest() {
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
