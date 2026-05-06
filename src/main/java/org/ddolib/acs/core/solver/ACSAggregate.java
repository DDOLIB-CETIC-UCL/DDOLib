package org.ddolib.acs.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solution;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.StateAndDepth;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.debug.DebugUtil;
import org.ddolib.util.verbosity.VerboseMode;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ACSAggregate<T> implements Solver {

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
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T> dominance;
    /**
     * HashMap mapping (state,depth) to the f value.
     * Closed nodes are the ones for which their children have been generated.
     */
    private final HashMap<StateAndDepth<T>, Double> closed;
    /**
     * HashMap mapping (state,depth) to the f value.
     * Open nodes are the ones in the frontier.
     */
    private final HashMap<StateAndDepth<T>, Double> present;
    private final List<PriorityQueue<SubProblem<T>>> open;
    private final int columnWidth;
    private final SubProblem<T> root;
    private final VerboseMode verboseMode;
    private final DebugLevel debugLevel;
    /**
     * Value of the best known upper bound (incumbent solution).
     */
    private double bestUB;
    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;
    private boolean negativeTransitionCosts = false;

    private boolean defaultLowerBoundValue = false;

    private int nbSameNodes = 0;

    private int dominatedNodes = 0;

    public HashMap<Set<Decision>, Double> solutionDecisions = new HashMap<>();

    public HashMap<T, Double> solutionState = new HashMap<>();

    private HashMap<T,T> parent = new HashMap<>();

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
    public ACSAggregate(AcsModel<T> model) {
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
        this.debugLevel = model.debugMode();

        this.root = constructRoot(problem.initialState(), problem.initialValue(), 0);

    }

    private ACSAggregate(AcsModel<T> model, StateAndDepth<T> rootKey) {
        this.problem = model.problem();
        this.varh = model.variableHeuristic();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.columnWidth = model.columnWidth();

        this.closed = new HashMap<>();
        this.present = new HashMap<>();
        this.open = new ArrayList<>(problem.nbVars() + 1);
        for (int i = 0; i < problem.nbVars() + 1; i++) {
            this.open.add(new PriorityQueue<>(Comparator.comparingDouble(SubProblem<T>::f)));
        }

        this.verboseMode = new VerboseMode(VerbosityLevel.SILENT, 500);
        this.debugLevel = DebugLevel.OFF;
        this.root = constructRoot(rootKey.state(), 0, rootKey.depth());
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
    public Solution minimize(Predicate<SearchStatistics> limit,
                             BiConsumer<int[], SearchStatistics> onSolution) {
        long t0 = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        int nbSols =0;
        open.getFirst().add(root);
        ArrayList<int[]> ubs = new ArrayList<>();
        present.put(new StateAndDepth<>(root.getState(), root.getDepth()), root.f());

        if (root.f() == Integer.MIN_VALUE) {
            defaultLowerBoundValue = true;
        }

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
                    System.currentTimeMillis() - t0, bestUB, gap(), nbSols, nbSameNodes, dominatedNodes, ubs);

            if (limit.test(stats)) {
                return new Solution(bestSolution(), stats);
            }

            for (int i = 0; i < problem.nbVars() + 1; i++) { // for each layer
                candidates.clear();
                int l = min(columnWidth, open.get(i).size());
                while(!open.get(i).isEmpty()&& candidates.size()<l) { // expand the layer by expanding the best columnWidth best nodes
                    SubProblem<T> sub = open.get(i).poll();
                    StateAndDepth<T> subKey = new StateAndDepth<>(sub.getState(), sub.getDepth());
                    present.remove(subKey);
                    if (dominance.dominated(sub.getState(),sub.getValue(), subKey.depth()) || problem.stateSpaceReduction(sub.getState())){
                        dominatedNodes+=1;
                        continue;
                    }
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
                    StateAndDepth<T> subKey = new StateAndDepth<>(sub.getState(), sub.getDepth());
                    this.closed.put(subKey, sub.f());
                    if (sub.getPath().size() == problem.nbVars()) {
                        // new incumbent
                        if (bestUB > sub.getValue()) {
                            bestSol = Optional.of(sub.getPath());
                            bestUB = sub.getValue();
                            ubs.add(new int[]{(int) bestUB, (int) (System.currentTimeMillis() - t0)});
                            nbSols+=1;
                            stats = new SearchStatistics(SearchStatus.SAT, nbIter, queueMaxSize,
                                    System.currentTimeMillis() - t0, bestUB, gap(), nbSols, nbSameNodes, dominatedNodes, ubs);
                            onSolution.accept(constructSolution(bestSol.get()), stats);
                        }
                        verboseMode.newBest(bestUB);
                    } else {
                        verboseMode.currentSubProblem(nbIter, sub);
                        addChildren(sub);
                    }
                }

            }
            queueMaxSize = max(queueMaxSize, open.stream().mapToInt(PriorityQueue::size).sum());
        }

        if (debugLevel != DebugLevel.OFF) {
            checkAdmissibility();
        }
        SearchStatistics stats = new SearchStatistics(SearchStatus.OPTIMAL, nbIter, queueMaxSize,
                System.currentTimeMillis() - t0, bestValue().orElse(Double.POSITIVE_INFINITY), 0, nbSols, nbSameNodes, dominatedNodes, ubs);
        return new Solution(bestSolution(), stats);
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

        Set<Decision> nullDecisions = new HashSet<>(); // needed for debug mode
        if (depth != 0) {
            for (int i = 0; i < depth; i++) {
                nullDecisions.add(new Decision(i, 0));
            }
        }
        SubProblem<T> sub = new SubProblem<>(
                state,
                value,
                lb.fastLowerBound(state, vars),
                nullDecisions);
        this.parent.put(state,null);
        return sub;
    }

    /**
     * Adds children of a subproblem to the open queues, applying lower bounds and dominance checks.
     *
     * @param subProblem the parent subproblem
     */
    private void addChildren(SubProblem<T> subProblem) {
        T state = subProblem.getState();
        int var = subProblem.getPath().size();
        final Iterator<Integer> domain = problem.domain(state, var);
        while (domain.hasNext()) {
            final int val = domain.next();
            final Decision decision = new Decision(var, val);
            if (debugLevel != DebugLevel.OFF)
                DebugUtil.checkHashCodeAndEquality(state, decision, problem::transition);
            T newState = problem.transition(state, decision);
            if(newState==null){
                continue;
            }
            double cost = problem.transitionCost(state, decision);
            if (cost < 0) {
                negativeTransitionCosts = true;
            }
            double value = subProblem.getValue() + cost;
            Set<Decision> path = new HashSet<>(subProblem.getPath());
            path.add(decision);
            double fastLowerBound = lb.fastLowerBound(newState, varSet(path));

            SubProblem<T> newSub = new SubProblem<>(newState, value, fastLowerBound, path);

            if (debugLevel == DebugLevel.EXTENDED) {
                DebugUtil.checkFlbConsistency(subProblem, newSub, cost);
            }
            StateAndDepth<T> newKey = new StateAndDepth<>(newState, newSub.getDepth());
            Double presentValue = present.get(newKey);
            if (presentValue != null && presentValue > newSub.f()) {
                open.get(newSub.getDepth()).add(newSub);
                present.put(newKey, newSub.f());
                dominance.updateDominance(newSub.getState(), newSub.getDepth(), newSub.getValue());
                parent.replace(newState, state);
            } else if (presentValue != null && presentValue <= newSub.f()) {
                nbSameNodes+=1;
            } else {
                Double closedValue = closed.get(newKey);
                if (closedValue != null && closedValue > newSub.f()) {
                    open.get(newSub.getDepth()).add(newSub);
                    closed.remove(newKey);
                    present.put(newKey, newSub.f());
                    dominance.updateDominance(newSub.getState(), newSub.getDepth(), newSub.getValue());
                    parent.replace(newState, state);
                }else if(closedValue != null && closedValue <= newSub.f()){
                    nbSameNodes+=1;
                }else {
                    open.get(newSub.getDepth()).add(newSub);
                    present.put(newKey, newSub.f());
                    dominance.updateDominance(newSub.getState(), newSub.getDepth(), newSub.getValue());
                    parent.put(newState,state);
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


    private void checkAdmissibility() {
        Set<StateAndDepth<T>> toCheck = new HashSet<>(closed.keySet());
        toCheck.addAll(present.keySet());

        AcsModel<T> model = new AcsModel<>() {


            @Override
            public Problem<T> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<T> lowerBound() {
                return lb;
            }

            @Override
            public DominanceChecker<T> dominance() {
                return dominance;
            }

            @Override
            public int columnWidth() {
                return columnWidth;
            }
        };

        DebugUtil.checkFlbAdmissibility(toCheck, model, key -> new ACSAggregate<>(model, key));
    }

    /**
     * Computes the gap (percentage difference) between the best known upper bound and the lowest
     * f-value in the open nodes, for anytime search reporting.
     *
     * @return gap as a percentage
     */
    private double gap() {
        if (defaultLowerBoundValue) {
            return Double.NaN;
        } else {
            double maxLB = Double.POSITIVE_INFINITY;
            if (negativeTransitionCosts){
                maxLB = Double.NEGATIVE_INFINITY;
            }
            for (int i = 0; i < problem.nbVars(); i++) {
                if (!open.get(i).isEmpty()) {
                    if (negativeTransitionCosts) {
                        maxLB = Math.max(maxLB, Math.abs(open.get(i).peek().f()));
                    } else {
                        maxLB = Math.min(maxLB, open.get(i).peek().f());
                    }
                }
            }
            return Math.abs(100.0 * (bestUB - maxLB) / Math.abs(bestUB));
        }
    }
}
