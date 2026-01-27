package org.ddolib.ddo.core.solver;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solution;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.compilation.CompilationConfig;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.ExactModel;
import org.ddolib.modeling.Problem;
import org.ddolib.util.verbosity.VerbosityLevel;

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
 * Solver that compiles a single exact decision diagram (MDD) to find the optimal solution.
 * <p>
 * <b>Warning:</b> Using only exact MDDs can consume a significant amount of memory.
 * It is recommended to use this solver for small instances or for testing your model.
 * For larger instances or more advanced strategies, consider using {@link SequentialSolver}.
 * </p>
 *
 * <p>
 * This solver constructs the MDD in a single pass:
 * </p>
 * <ul>
 *     <li>Initializes the root subproblem from the initial state.</li>
 *     <li>Compiles the MDD using exact node expansion without any width restriction.</li>
 *     <li>Optionally uses dominance rules and caching to prune redundant subproblems.</li>
 *     <li>Extracts the best solution and value from the compiled MDD.</li>
 *     <li>Can export the MDD in DOT format if enabled.</li>
 * </ul>
 *
 * @param <T> the type of state used by the problem
 */
public final class ExactSolver<T> implements Solver {

    /**
     * The problem instance to be minimized.
     */
    private final Problem<T> problem;

    /**
     * Verbosity level controlling output during the solving process.
     */
    private final VerbosityLevel verbosityLevel;
    /**
     * Flag to indicate whether the compiled MDD should be exported as a DOT file.
     */
    private final boolean exportAsDot;
    private final ExactModel<T> model;
    /**
     * Optional set containing the best solution found so far.
     */
    private Optional<Set<Decision>> bestSol;
    /**
     * Optional value of the best solution found so far.
     */
    private Optional<Double> bestValue = Optional.empty();


    /**
     * Creates a fully-configured ExactSolver instance.
     *
     * @param model The {@link DdoModel} containing all necessary parameters and heuristics
     *              to configure the solver, including the problem, relaxation, ranking,
     *              variable heuristic, lower bound, dominance checker, caching, and verbosity settings.
     */
    public ExactSolver(ExactModel<T> model) {
        this.problem = model.problem();
        this.bestSol = Optional.empty();
        this.verbosityLevel = model.verbosityLevel();
        this.exportAsDot = model.exportDot();
        this.model = model;
    }

    /**
     * Minimizes the problem by compiling an exact decision diagram (MDD).
     * <p>
     * The method performs the following steps:
     * <ul>
     *     <li>Initializes the root subproblem with the initial state.</li>
     *     <li>Configures the compilation parameters for an exact MDD.</li>
     *     <li>Compiles the MDD and optionally prunes using dominance and caching.</li>
     *     <li>Extracts the best solution and value.</li>
     *     <li>Optionally exports the MDD in DOT format.</li>
     * </ul>
     *
     * @param limit      a predicate that may be used to limit the search based on statistics
     * @param onSolution a callback invoked when a solution is found
     * @return statistics about the search process, including the best value found
     */
    @Override
    public Solution minimize(Predicate<SearchStatistics> limit,
                             BiConsumer<int[], SearchStatistics> onSolution) {
        long start = System.currentTimeMillis();
        SubProblem<T> root = new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Double.POSITIVE_INFINITY,
                Collections.emptySet());

        CompilationConfig<T> compilation = new CompilationConfig<>(model);
        compilation.compilationType = CompilationType.Exact;
        compilation.problem = this.problem;
        compilation.relaxation = model.relaxation();
        compilation.variableHeuristic = model.variableHeuristic();
        compilation.stateRanking = model.ranking();
        compilation.residual = root;
        compilation.maxWidth = Integer.MAX_VALUE;
        compilation.flb = model.lowerBound();
        compilation.dominance = model.dominance();
        compilation.cache = Optional.empty();
        compilation.bestUB = Double.POSITIVE_INFINITY;
        compilation.cutSetType = CutSetType.LastExactLayer;
        compilation.exportAsDot = this.exportAsDot;
        compilation.debugLevel = model.debugMode();

        DecisionDiagram<T> mdd = new LinkedDecisionDiagram<>(compilation);
        mdd.compile();
        extractBest(mdd);
        if (exportAsDot) {
            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            exportDot(mdd.exportAsDot(),
                    Paths.get("output", problemName + "_exact.dot").toString());
        }

        long end = System.currentTimeMillis();
        SearchStatistics stats = new SearchStatistics(SearchStatus.OPTIMAL,
                1,
                1,
                end - start,
                bestValue.orElse(Double.POSITIVE_INFINITY),
                0);


        bestSol.ifPresent(sol -> onSolution.accept(constructSolution(bestSol.get()), stats));
        return new Solution(bestSolution(), stats);
    }

    /**
     * Returns the value of the best solution found so far.
     *
     * @return an {@link Optional} containing the best solution value or empty if none was found
     */
    @Override
    public Optional<Double> bestValue() {
        return bestValue;
    }

    /**
     * Returns the best solution found so far as a set of decisions.
     *
     * @return an {@link Optional} containing the best solution or empty if none was found
     */
    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * Extracts the best solution and value from a compiled decision diagram.
     *
     * @param mdd the compiled {@link DecisionDiagram} from which to extract the best solution
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

    /**
     * Exports a DOT representation of the MDD to a file.
     *
     * @param dot      the DOT string representing the MDD
     * @param fileName the output file path
     */
    private void exportDot(String dot, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(dot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
