package org.ddolib.acs.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.debug.DebugUtil;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

/**
 * Implementation of an Anytime Column Search (ACS) solver for decision diagram-based optimization problems.
 * <p>
 * The solver uses a combination of a lower bound, dominance rules, and variable heuristics to explore the
 * search space efficiently. It maintains open and closed lists of subproblems organized by depth (columns)
 * and attempts to minimize the objective function while keeping track of the best known solution (incumbent).
 * </p>
 *
 * <p>
 * Features:
 * </p>
 * <ul>
 *     <li>Maintains a frontier of subproblems using a priority queue at each column.</li>
 *     <li>Applies a {@link FastLowerBound} to prune subproblems unlikely to improve the incumbent.</li>
 *     <li>Optionally uses {@link DominanceChecker} to avoid exploring dominated states.</li>
 *     <li>Supports variable selection heuristics through {@link VariableHeuristic}.</li>
 *     <li>Can report search statistics and provide the best solution found at any time.</li>
 * </ul>
 *
 * @param <T> The type representing a state in the problem.
 * @see Solver
 * @see Problem
 * @see FastLowerBound
 * @see VariableHeuristic
 * @see DominanceChecker
 * @see AcsModel
 */
public final class ACSSolver<T> implements Solver {

    /**
     * The problem we want to minimize
     */
    private final Problem<T> problem;
    /**
     * A suitable lb for the problem we want to minimize
     */
    private final FastLowerBound<T> lb;
    /**
     * A heuristic to choose the next variable to branch on when developing a DD
     */
    private final VariableHeuristic<T> varh;
    /**
     * Value of the best known upper bound (incumbent solution).
     */
    private double bestUB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;
    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T> dominance;

    /**
     * HashMap mapping (state,depth) to the f value.
     * Closed nodes are the ones for which their children have been generated.
     */
    private final HashMap<ACSKey<T>, Double> closed;

    /**
     * HashMap mapping (state,depth) to the f value.
     * Open nodes are the ones in the frontier.
     */
    private final HashMap<ACSKey<T>, Double> present;


    private final List<PriorityQueue<SubProblem<T>>> open;

    private final int columnWidth;

    private final SubProblem<T> root;

    private final VerboseMode verboseMode;


    /**
     * Constructs an ACS solver with all required and optional components provided via an {@link AcsModel}.
     *
     * <p>
     * Mandatory parameters:
     * </p>
     * <ul>
     *     <li>{@link Problem} implementation</li>
     *     <li>{@link FastLowerBound} implementation</li>
     *     <li>{@link VariableHeuristic} implementation</li>
     * </ul>
     * Optional parameters:
     * <ul>
     *     <li>{@link DominanceChecker} implementation</li>
     *     <li>{@link VerbosityLevel} for debug/logging</li>
     * </ul>
     *
     * @param model Provides all parameters needed to configure the solver
     * @throws IllegalArgumentException if the debug mode is enabled (not supported)
     */
    public ACSSolver(AcsModel<T> model) {
        this.problem = model.problem();
        this.varh = model.variableHeuristic();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = Integer.MAX_VALUE;
        this.bestSol = Optional.empty();
        this.columnWidth = model.columnWidth();

        this.closed = new HashMap<>();
        this.present = new HashMap<>();


        this.open = new ArrayList<>(problem.nbVars() + 1);
        for (int i = 0; i < problem.nbVars() + 1; i++) {
            this.open.add(new PriorityQueue<>(Comparator.comparingDouble(SubProblem<T>::f)));
        }

        this.verboseMode = new VerboseMode(model.verbosityLevel(), 500);

        this.root = constructRoot(problem.initialState(), problem.initialValue(), 0);
        if (model.debugMode() != DebugLevel.OFF) {
            throw new IllegalArgumentException("The debug mode for this solver is not available " +
                    "for the moment.");
        }

    }

    /**
     * Checks if all columns are empty, i.e., no open subproblems remain.
     *
     * @return true if all open queues are empty, false otherwise
     */
    private boolean allEmpty() {
        for (PriorityQueue<SubProblem<T>> q : open) {
            if (!q.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Minimizes the problem using the ACS strategy.
     *
     * @param limit      a predicate to stop the search based on current {@link SearchStatistics}
     * @param onSolution a consumer invoked on each new solution found (solution array and statistics)
     * @return final {@link SearchStatistics} of the search
     */
    @Override
    public SearchStatistics minimize(Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        long t0 = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        open.getFirst().add(root);
        present.put(new ACSKey<>(root.getState(), root.getDepth()), root.f());

        ArrayList<SubProblem<T>> candidates = new ArrayList<>();
        while (!allEmpty()) {
            verboseMode.detailedSearchState(nbIter,
                    open.stream().map(PriorityQueue::size).mapToInt(x -> x).sum(),
                    bestUB,
                    open.stream()
                            .map(pq -> pq.peek() != null ? pq.peek().getLowerBound() : 0)
                            .mapToDouble(x -> x).min().orElse(Double.POSITIVE_INFINITY),
                    100 * gap());


            SearchStatistics stats = new SearchStatistics(SearchStatus.UNKNOWN, nbIter, queueMaxSize,
                    System.currentTimeMillis() - t0, bestValue().orElse(Double.POSITIVE_INFINITY), 0);

            if (limit.test(stats)) {
                return stats;
            }

            for (int i = 0; i < problem.nbVars() + 1; i++) {
                candidates.clear();
                int l = min(columnWidth, open.get(i).size());
                for (int j = 0; j < l; j++) {
                    SubProblem<T> sub = open.get(i).poll();
                    ACSKey<T> subKey = new ACSKey<>(sub.getState(), sub.getDepth());
                    present.remove(subKey);
                    if (sub.f() < bestUB) {
                        candidates.add(sub);
                    } else {
                        // all the next ones will be worse since f is a lower-bound
                        open.get(i).clear();
                        break;
                    }
                }
                for (SubProblem<T> sub : candidates) {
                    nbIter++;
                    ACSKey<T> subKey = new ACSKey<>(sub.getState(), sub.getDepth());
                    this.closed.put(subKey, sub.f());
                    if (sub.getPath().size() == problem.nbVars()) {
                        // new incumbent
                        if (bestUB > sub.getValue()) {
                            bestSol = Optional.of(sub.getPath());
                            bestUB = sub.getValue();
                        }

                        stats = new SearchStatistics(SearchStatus.SAT, nbIter, queueMaxSize,
                                System.currentTimeMillis() - t0, bestUB, gap());
                        onSolution.accept(constructSolution(bestSol.get()), stats);
                        verboseMode.newBest(bestUB);
                    } else {
                        verboseMode.currentSubProblem(nbIter, sub);
                        addChildren(sub, i + 1);
                    }
                }

            }
            queueMaxSize = Math.max(queueMaxSize, open.stream().mapToInt(PriorityQueue::size).sum());
        }
        return new SearchStatistics(SearchStatus.OPTIMAL, nbIter, queueMaxSize,
                System.currentTimeMillis() - t0, bestValue().orElse(Double.POSITIVE_INFINITY), gap());
    }

    /**
     * Returns the value of the best solution found, if any.
     *
     * @return {@link Optional} containing the value of the best solution or empty if none found
     */
    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestUB);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the set of decisions corresponding to the best solution found, if any.
     *
     * @return {@link Optional} containing the set of {@link Decision} objects or empty if none found
     */
    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * Constructs the root subproblem from a given state, value, and depth.
     *
     * @param state the initial state
     * @param value the initial value
     * @param depth depth of the root (used for debugging)
     * @return a {@link SubProblem} representing the root
     */
    private SubProblem<T> constructRoot(T state, double value, int depth) {
        Set<Integer> vars =
                IntStream.range(depth, problem.nbVars()).boxed().collect(Collectors.toSet());
        return new SubProblem<>(
                state,
                value,
                lb.fastLowerBound(state, vars),
                Collections.EMPTY_SET);
    }

    /**
     * Adds children of a subproblem to the open queues, applying lower bounds and dominance checks.
     *
     * @param subProblem the parent subproblem
     * @param debugLevel used for debug checking
     */
    private void addChildren(SubProblem<T> subProblem, int debugLevel) {
        T state = subProblem.getState();
        int var = subProblem.getPath().size();
        final Iterator<Integer> domain = problem.domain(state, var);
        while (domain.hasNext()) {
            final int val = domain.next();
            final Decision decision = new Decision(var, val);
            if (debugLevel >= 1)
                DebugUtil.checkHashCodeAndEquality(state, decision, problem::transition);
            T newState = problem.transition(state, decision);
            double cost = problem.transitionCost(state, decision);
            double value = subProblem.getValue() + cost;
            Set<Decision> path = new HashSet<>(subProblem.getPath());
            path.add(decision);
            double fastLowerBound = lb.fastLowerBound(newState, varSet(path));


            // if the new state is dominated, we skip it
            if (!dominance.updateDominance(newState, path.size(), value)) {
                SubProblem<T> newSub = new SubProblem<>(newState, value, fastLowerBound, path);
                ACSKey<T> newKey = new ACSKey<>(newState, newSub.getDepth());
                Double presentValue = present.get(newKey);
                if (presentValue != null && presentValue > newSub.f()) {
                    open.get(newSub.getDepth()).add(newSub);
                    present.put(newKey, newSub.f());
                } else {
                    Double closedValue = closed.get(newKey);
                    if (closedValue != null && closedValue > newSub.f()) {
                        open.get(newSub.getDepth()).add(newSub);
                        closed.remove(newKey);
                        present.put(newKey, newSub.f());
                    } else {
                        open.get(newSub.getDepth()).add(newSub);
                        present.put(newKey, newSub.f());
                    }
                }

            }
        }
    }

    /**
     * Returns the set of variables not yet assigned in a given path.
     *
     * @param path set of decisions already taken
     * @return set of remaining variable indices
     */
    private Set<Integer> varSet(Set<Decision> path) {
        final HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            set.add(i);
        }
        for (Decision d : path) {
            set.remove(d.var());
        }
        return set;
    }

    /**
     * Internal key for mapping a state and its depth in open/closed lists.
     *
     * @param <T> type of the state
     */
    private record ACSKey<T>(T state, int depth) {
    }

    /**
     * Computes the gap (percentage difference) between the best known upper bound and the lowest
     * f-value in the open nodes, for anytime search reporting.
     *
     * @return gap as a percentage
     */
    private double gap() {
        double minLb = Double.POSITIVE_INFINITY;
        for (int i = 0; i < problem.nbVars(); i++) {
            if (!open.get(i).isEmpty()) {
                minLb = min(minLb, Math.abs(open.get(i).peek().f()));
            }
        }
        if (minLb == Double.POSITIVE_INFINITY) {
            return 0.0;
        }
        return Math.abs(100.0 * (Math.abs(bestUB) - minLb) / bestUB);
    }
}
