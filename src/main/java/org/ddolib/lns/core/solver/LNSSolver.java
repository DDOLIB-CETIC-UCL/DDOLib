package org.ddolib.lns.core.solver;

import org.ddolib.common.solver.Solution;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.stat.DdoStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.compilation.CompilationConfig;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.util.verbosity.VerboseMode;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Large Neighborhood Search (LNS) solver based on restricted decision diagrams.
 *
 * <p>The solver iteratively destroys part of the incumbent solution, recompiles a restricted
 * decision diagram on the residual subproblem, and accepts strict improvements on the objective
 * value.</p>
 *
 * @param <T> type of states manipulated by the underlying problem
 */
public final class LNSSolver<T> implements Solver {
    private final Problem<T> problem;
    private final WidthHeuristic<T> width;
    private final VerbosityLevel verbosityLevel;
    private final VerboseMode verboseMode;
    private final boolean exportAsDot;
    private final LnsModel<T> model;
    private double bestUB;
    private Optional<Set<Decision>> bestSol;
    private boolean firstRestricted = true;
    private int maxDepth;
    private int d;
    private int[] solution;

    /**
     * Creates a new LNS solver for the provided model.
     *
     * @param model model containing the problem definition and LNS/DD configuration
     */
    public LNSSolver(LnsModel<T> model) {
        this.problem = model.problem();
        this.width = model.widthHeuristic();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verbosityLevel = model.verbosityLevel();
        this.verboseMode = new VerboseMode(verbosityLevel, 500L);
        this.exportAsDot = model.exportDot();
        this.model = model;
        this.maxDepth = Math.max(0, problem.nbVars() - 2);
        this.d = maxDepth;
        this.solution = new int[problem.nbVars()];
    }

    @Override
    public Solution minimize(Predicate<SearchStatistics<?>> limit,
                             BiConsumer<int[], SearchStatistics<?>> onSolution) {
        long start = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        DdoStats stats = new DdoStats(start, bestUB);
        SubProblem<T> rootPrime;
        double gap = 100;
        while (true) {
            nbIter++;
            queueMaxSize++;
            stats = stats
                    .updateTime(System.currentTimeMillis())
                    .incrementNbIter()
                    .updateFrontierMaxSize(queueMaxSize)
                    .updateGap(gap);
            if (!limit.test(stats)) {
                break;
            }
            verboseMode.detailedSearchState(nbIter, queueMaxSize, bestUB,
                    Double.POSITIVE_INFINITY, gap);

            if (bestSol.isPresent()) {
                solution = constructSolution(bestSol.get());
            } else {
                if (model.initialSolution() != null) {
                    solution = model.initialSolution();
                    bestUB = costInSolutionAtDepth(model.initialSolution(), problem.nbVars());
                    if (Double.isInfinite(stats.incumbent())) {
                        stats = stats.updateIncumbent(bestUB, gap).updateStatus(SearchStatus.SAT);
                    }
                } else {
                    solution = null;
                }
            }

            if (solution == null || !model.useLNS()) {
                rootPrime = root();
            } else {
                rootPrime = buildInitialSubProblem(solution, d);
            }
            // 1. RESTRICTION
            SubProblem<T> sub = rootPrime;

            verboseMode.currentSubProblem(nbIter, sub);

            int maxWidth = width.maximumWidth(sub.getState());

            CompilationConfig<T> compilation = configureCompilation(CompilationType.Restricted,
                    sub, maxWidth, model.exportDot() && this.firstRestricted);

            DecisionDiagram<T> restrictedMdd = new LinkedDecisionDiagram<>(compilation);
            restrictedMdd.compile();

            gap = computeGap(bestUB, restrictedMdd.minLowerBound());

            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            boolean newbest = maybeUpdateBest(restrictedMdd, exportAsDot && firstRestricted);
            if (newbest) {
                gap = computeGap(bestUB, restrictedMdd.minLowerBound());
                stats = stats
                        .updateTime(System.currentTimeMillis())
                        .updateIncumbent(bestUB, gap)
                        .updateStatus(SearchStatus.SAT)
                        .updateGap(gap);
                onSolution.accept(constructSolution(bestSol.get()), stats);
            }
            if (exportAsDot && firstRestricted) {
                exportDot(restrictedMdd.exportAsDot(),
                        Paths.get("output", problemName + "_restricted.dot").toString());
            }
            firstRestricted = false;

            if (d == 0 && restrictedMdd.isExact()) {
                stats = stats
                        .updateTime(System.currentTimeMillis())
                        .updateIncumbent(bestUB, 0.0)
                        .updateStatus(SearchStatus.OPTIMAL)
                        .updateGap(0.0);
                return new Solution(bestSolution(), stats);
            }
        }

        stats = stats.updateTime(System.currentTimeMillis()).updateGap(gap);
        if (stats.incumbent() != bestUB) {
            stats = stats.updateIncumbent(bestUB, gap);
        }
        if (!Double.isInfinite(bestUB) && stats.status() == SearchStatus.UNKNOWN) {
            stats = stats.updateStatus(SearchStatus.SAT);
        }

        return new Solution(bestSolution(), stats);
    }

    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestUB);
        } else {
            if (model.initialSolution() != null) {
                return Optional.of(costInSolutionAtDepth(solution, problem.nbVars()));
            }
            return Optional.empty();
        }
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * Updates the incumbent solution if the current restricted DD found a strict improvement.
     *
     * @param currentMdd restricted decision diagram compiled at the current iteration
     * @param exportDot  whether to force DOT materialization to reflect incumbent edge coloring
     * @return {@code true} if the incumbent was improved, {@code false} otherwise
     */
    private boolean maybeUpdateBest(DecisionDiagram<T> currentMdd, boolean exportDot) {
        Optional<Double> ddval = currentMdd.bestValue();
        if (ddval.isPresent() && ddval.get() < bestUB) {
            bestUB = ddval.get();
            bestSol = currentMdd.bestSolution();
            if (model.useLNS())
                d = maxDepth;
            verboseMode.newBest(bestUB);
            return true;
        } else {
            if (exportDot)
                currentMdd.exportAsDot(); // to be sure to update the color of the edges.
            if (model.useLNS()) {
                if (d == 0) {
                    d = maxDepth;
                } else {
                    d--;
                }
            }
        }
        return false;
    }

    /**
     * Writes a DOT representation to disk.
     *
     * @param dot      DOT graph content
     * @param fileName output file path
     */
    private void exportDot(String dot, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(dot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Builds the root subproblem covering all decision variables.
     *
     * @return root residual subproblem
     */
    private SubProblem<T> root() {
        Set<Integer> vars =
                IntStream.range(0, problem.nbVars()).boxed().collect(Collectors.toSet());
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                model.lowerBound().fastLowerBound(problem.initialState(), vars),
                Collections.emptySet());
    }


    /**
     * Builds a residual subproblem by fixing a prefix of the current solution.
     *
     * @param solution incumbent solution used as reference for neighborhood destruction
     * @param depth    number of fixed variables in the prefix
     * @return residual subproblem rooted at the state reached after the fixed prefix
     */
    private SubProblem<T> buildInitialSubProblem(int[] solution, int depth) {
        final HashSet<Integer> vars = new HashSet<>();
        Set<Decision> decisionSet = new HashSet<>();
        T state = problem.initialState();
        double sum = problem.initialValue();
        int k = 0;
        while (k < problem.nbVars()) {
            Decision dec = new Decision(k, solution[k]);
            if (k < depth) {
                decisionSet.add(dec);
                sum += problem.transitionCost(state, dec);
                state = problem.transition(state, dec);
            } else {
                vars.add(dec.variable());
            }
            k++;
        }
        return new SubProblem<>(
                state,
                sum,
                model.lowerBound().fastLowerBound(state, vars),
                decisionSet
        );
    }


    /**
     * Computes the objective value accumulated by a solution prefix.
     *
     * @param solution full variable assignment
     * @param depth    prefix length to evaluate
     * @return cumulative objective value up to {@code depth}
     */
    private double costInSolutionAtDepth(int[] solution, int depth) {
        double sum = problem.initialValue();
        T state = problem.initialState();
        int k = 0;
        while (k < depth) {
            Decision dec = new Decision(k, solution[k]);
            sum += problem.transitionCost(state, dec);
            state = problem.transition(state, dec);
            k++;
        }
        return sum;
    }


    /**
     * Computes the state reached after applying a solution prefix.
     *
     * @param solution full variable assignment
     * @param depth    prefix length to apply
     * @return state reached at the requested depth
     */
    private T stateInSolutionAtDepth(int[] solution, int depth) {
        T state = problem.initialState();
        int k = 0;
        while (k < depth) {
            Decision dec = new Decision(k, solution[k]);
            state = problem.transition(state, dec);
            k++;
        }
        return state;
    }

    /**
     * Creates the compilation configuration for a restricted DD iteration.
     *
     * @param type        compilation type to use
     * @param sub         residual subproblem to compile
     * @param maxWidth    maximal width allowed during restriction
     * @param exportAsDot whether DOT export is enabled for this compilation
     * @return fully initialized compilation configuration
     */
    private CompilationConfig<T> configureCompilation(CompilationType type, SubProblem<T> sub,
                                                      int maxWidth, boolean exportAsDot) {
        CompilationConfig<T> compilation = new CompilationConfig<>(model);
        compilation.compilationType = type;
        compilation.problem = model.problem();
        compilation.variableHeuristic = model.variableHeuristic();
        compilation.stateRanking = model.ranking();
        compilation.residual = sub;
        compilation.maxWidth = maxWidth;
        compilation.flb = model.lowerBound();
        compilation.dominance = model.dominance();
        compilation.bestUB = this.bestUB;
        compilation.exportAsDot = exportAsDot;
        compilation.debugLevel = model.debugMode();
        compilation.reductionStrategy = model.restrictStrategy();
        compilation.initialSolution = model.initialSolution();
        compilation.probability = model.probability();
        compilation.useLNS = model.useLNS();
        if (bestSol.isPresent()) {
            compilation.solution = constructSolution(bestSol.get());
        } else {
            compilation.solution = model.initialSolution();
        }
        return compilation;
    }

    /**
     * Computes the relative optimality gap in percent.
     *
     * <p>Special cases are handled explicitly to avoid {@code NaN}: infinite upper bound returns
     * an infinite gap; zero upper bound returns 0 when lower bound is also zero, otherwise an
     * infinite gap.</p>
     *
     * @param upperBound incumbent objective value
     * @param lowerBound lower bound of the explored neighborhood
     * @return relative gap percentage
     */
    private double computeGap(double upperBound, double lowerBound) {
        if (Double.isInfinite(upperBound)) {
            return Double.POSITIVE_INFINITY;
        }
        double numerator = Math.abs(upperBound - lowerBound);
        double denominator = Math.abs(upperBound);
        if (denominator == 0.0) {
            return numerator == 0.0 ? 0.0 : Double.POSITIVE_INFINITY;
        }
        return 100.0 * numerator / denominator;
    }


}
