package org.ddolib.lns.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solution;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.ddo.core.compilation.CompilationConfig;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.util.verbosity.VerboseMode;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
    private DominanceChecker<T> dominance;

    private int maxDepth;
    private int d;
    private int[] solution;

    public LNSSolver(LnsModel<T> model) {
        this.problem = model.problem();
        this.width = model.widthHeuristic();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verbosityLevel = model.verbosityLevel();
        this.verboseMode = new VerboseMode(verbosityLevel, 500L);
        this.exportAsDot = model.exportDot();
        this.dominance = model.dominance();
        this.model = model;
        this.maxDepth = problem.nbVars() - 2;
        this.d = maxDepth;
        this.solution = new int[problem.nbVars()];
    }

    @Override
    public Solution minimize(Predicate<SearchStatistics> limit,
                             BiConsumer<int[], SearchStatistics> onSolution) {
        long start = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        SearchStatistics stats;
        SubProblem<T> rootPrime;
        double gap = 100;
        SearchStatus status = SearchStatus.UNKNOWN;
        while (true) {
            nbIter++;
            queueMaxSize++;
            stats = new SearchStatistics(status, nbIter,
                    queueMaxSize, System.currentTimeMillis() - start, bestUB, gap);
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
                } else {
                    solution = null;
                }
            }

            if (solution == null) {
                rootPrime = root();
            } else {
                rootPrime = buildInitialSubProblem(solution, d-1);
            }
            // 1. RESTRICTION
            SubProblem<T> sub = rootPrime;

            verboseMode.currentSubProblem(nbIter, sub);

            int maxWidth = width.maximumWidth(sub.getState());

            CompilationConfig<T> compilation = configureCompilation(CompilationType.Restricted,
                    sub, maxWidth, model.exportDot() && this.firstRestricted);

            DecisionDiagram<T> restrictedMdd = new LinkedDecisionDiagram<>(compilation);
            restrictedMdd.compile();

            gap = 100.0 * Math.abs(bestUB - restrictedMdd.minLowerBound()) / Math.abs(bestUB);

            String problemName = problem.getClass().getSimpleName().replace("Problem", "");
            boolean newbest = maybeUpdateBest(restrictedMdd, exportAsDot && firstRestricted);
            if (newbest) {
                status = SearchStatus.SAT;
                gap = 100.0 * Math.abs(bestUB - restrictedMdd.minLowerBound()) / Math.abs(bestUB);
                stats = new SearchStatistics(status, nbIter, queueMaxSize, System.currentTimeMillis() - start, bestUB, gap);
                onSolution.accept(constructSolution(bestSol.get()), stats);
            }
            if (exportAsDot && firstRestricted) {
                exportDot(restrictedMdd.exportAsDot(),
                        Paths.get("output", problemName + "_restricted.dot").toString());
            }
            firstRestricted = false;

            if (d == 0 && restrictedMdd.isExact()) {
                stats = new SearchStatistics(SearchStatus.OPTIMAL, nbIter, queueMaxSize, System.currentTimeMillis() - start, bestUB, 0.0);
                return new Solution(bestSolution(), stats);
            }
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

    private void exportDot(String dot, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(dot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private SubProblem<T> root() {
        Set<Integer> vars =
                IntStream.range(0, problem.nbVars()).boxed().collect(Collectors.toSet());
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                model.lowerBound().fastLowerBound(problem.initialState(),  vars),
                Collections.emptySet());
    }


    private SubProblem<T> buildInitialSubProblem(int[] solution,  int depth) {
        final HashSet<Integer> vars = new HashSet<>();
        Set<Decision> decisionSet = new HashSet<>();
        T state = problem.initialState();
        double sum = problem.initialValue();
        int k = 0;
        while (k < problem.nbVars()) {
            Decision dec =  new Decision(k, solution[k]);
            if (k < depth) {
                decisionSet.add(dec);
                sum += problem.transitionCost(state, dec);
                state = problem.transition(state, dec);
            } else {
                vars.add(dec.var());
            }
            k++;
        }
        return new SubProblem<>(
                stateInSolutionAtDepth(solution, depth),
                costInSolutionAtDepth(solution, depth),
                model.lowerBound().fastLowerBound(stateInSolutionAtDepth(solution, depth), vars),
                decisionSet
        );
    }


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
        compilation.useLNS = true;
        if (bestSol.isPresent()) {
            compilation.solution = constructSolution(bestSol.get());
        } else {
            solution = model.initialSolution();
        }
        return compilation;
    }


}
