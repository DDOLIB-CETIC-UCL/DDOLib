package org.ddolib.awastar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solution;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.AwAstarModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.SolverUtil;
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

public final class AwAstar<T> implements Solver {


    // The problem we want to minimize
    private final Problem<T> problem;
    // A suitable lb for the problem we want to minimize
    private final FastLowerBound<T> lb;
    // A heuristic to choose the next variable to branch on
    private final VariableHeuristic<T> varh;
    // HashMap mapping (state,depth) to the f value
    private final HashMap<StateAndDepth<T>, Double> closed;
    // HashMap mapping (state,depth) open nodes to the f value.
    private final HashMap<StateAndDepth<T>, Double> present;
    // The dominance object that will be used to prune the search space.
    private final DominanceChecker<T> dominance;

    private final double weight;

    // The priority queue containing the open subproblems by decreasing f' = g + w *  h (lower-bound
    private final PriorityQueue<SubProblem<T>> open;
    private final SubProblem<T> root;


    /**
     * <ul>g
     *     <li>0: no verbosity</li>
     *     <li>1: display newBest whenever there is a newBest</li>
     *     <li>2: 1 + statistics about the front every half a second (or so)</li>
     *     <li>3: 2 + every developed subproblem</li>
     *     <li>4: 3 + details about the developed state</li>
     * </ul>
     * <p>
     * <p>
     * 3: 2 + every developed subproblem
     * 4: 3 + details about the developed state
     */
    private final VerboseMode verboseMode;
    /**
     * The debug level of the compilation to add additional checks (see
     * {@link DebugLevel for details}
     */
    private final DebugLevel debugLevel;
    // Statistics
    long t0; // time at the beginning of the search
    int nbIter; // number of iterations
    int queueMaxSize; // maximum size reached by the queue
    // Value of the best known upper bound.
    private double bestUB;
    // If set, this keeps the info about the best solution so far.
    private Optional<Set<Decision>> bestSol;


    public AwAstar(AwAstarModel<T> model) {
        this.problem = model.problem();
        this.varh = model.variableHeuristic();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verboseMode = new VerboseMode(model.verbosityLevel(), 500L);
        this.debugLevel = model.debugMode();

        this.weight = model.weight();
        this.open = new PriorityQueue<>(
                Comparator.comparingDouble(sub -> sub.getValue() + weight * sub.getLowerBound()));
        this.root = constructRoot(problem.initialState(), problem.initialValue(), 0);
    }


    private AwAstar(AwAstarModel<T> model, StateAndDepth<T> rootKey) {
        this.problem = model.problem();
        this.varh = model.variableHeuristic();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verboseMode = new VerboseMode(VerbosityLevel.SILENT, 500L);
        this.debugLevel = DebugLevel.OFF;

        this.weight = model.weight();
        this.open = new PriorityQueue<>(
                Comparator.comparingDouble(sub -> sub.getValue() + weight * sub.getLowerBound()));
        this.root = constructRoot(rootKey.state(), 0, rootKey.depth());
    }

    @Override
    public Solution minimize(Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        t0 = System.currentTimeMillis();
        nbIter = 0;
        queueMaxSize = 0;
        open.add(root);
        present.put(new StateAndDepth<>(root.getState(), root.getDepth()),
                root.getValue() + weight * root.getLowerBound());

        while (!open.isEmpty()) {
            // -- debug, stats, verbosity, stopping  ---
            verboseMode.detailedSearchState(nbIter, open.size(), bestUB,
                    open.peek().getLowerBound(), 100 * gap());

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, open.size());

            SearchStatistics stats = new SearchStatistics(
                    SearchStatus.UNKNOWN,
                    nbIter,
                    queueMaxSize,
                    System.currentTimeMillis() - t0,
                    bestValue().orElse(Double.POSITIVE_INFINITY),
                    gap());


            if (limit.test(stats)) {
                // user-defined stopping criterion
                stats = new SearchStatistics(
                        bestSolution().map(sol -> SearchStatus.SAT).orElse(SearchStatus.UNKNOWN),
                        nbIter,
                        queueMaxSize,
                        System.currentTimeMillis() - t0,
                        bestValue().orElse(Double.POSITIVE_INFINITY),
                        gap());
                return new Solution(bestSolution(), stats);
            }
            // -- end debug, stats, verbosity, stopping  ---

            SubProblem<T> sub = open.poll();
            StateAndDepth<T> subKey = new StateAndDepth<>(sub.getState(), sub.getDepth());
            Double subValue = present.remove(subKey);

            // The current has been explored, or it can only lead to less good solution
            if (closed.containsKey(subKey) || sub.f() + 1e-10 >= bestUB) {
                continue;
            }

            if (sub.getDepth() < problem.nbVars()) {
                addChildren(sub, onSolution);
                closed.put(subKey, sub.getValue() + weight * sub.getLowerBound());
            }
        }

        if (debugLevel != DebugLevel.OFF) {
            checkFLBAdmissibility();
        }

        SearchStatistics statistics = new SearchStatistics(
                SearchStatus.OPTIMAL,
                nbIter,
                queueMaxSize,
                System.currentTimeMillis() - t0,
                problem.nbVars() == 0 ? problem.initialValue() : bestValue().orElse(Double.POSITIVE_INFINITY),
                gap()
        );

        return new Solution(bestSolution(), statistics);
    }

    @Override
    public Optional<Double> bestValue() {
        return bestSol.map(sol -> bestUB);
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    private void addChildren(SubProblem<T> subProblem,
                             BiConsumer<int[], SearchStatistics> onSolution) {
        T state = subProblem.getState();
        int var = subProblem.getDepth();

        final Iterator<Integer> domain = problem.domain(state, var);
        while (domain.hasNext()) {
            final int val = domain.next();
            final Decision decision = new Decision(var, val);

            if (debugLevel != DebugLevel.OFF) {
                DebugUtil.checkHashCodeAndEquality(state, decision, problem::transition);
            }


            T newState = problem.transition(state, decision);
            double cost = problem.transitionCost(state, decision);
            double g = subProblem.getValue() + cost;
            Set<Decision> path = new HashSet<>(subProblem.getPath());
            path.add(decision);
            double h = lb.fastLowerBound(newState, SolverUtil.varSet(problem, path));
            // h-cost from this state to the target
            double f = g + h;
            double fprime = g + weight * h;


            // this child can only lead to less good solution
            if (f + 1e-10 >= bestUB) continue;

            // if the new state is dominated, we skip it
            if (dominance.updateDominance(newState, path.size(), g)) continue;

            SubProblem<T> newSub = new SubProblem<>(newState, g, h, path);

            StateAndDepth<T> newKey = new StateAndDepth<>(newState, newSub.getDepth());
            Double presentValue = present.get(newKey);
            if (presentValue != null && fprime < presentValue) {
                open.remove(newSub);
                open.add(newSub);
                present.put(newKey, fprime);
            } else if (presentValue == null) {
                Double closedValue = closed.get(newKey);
                open.add(newSub);
                if (closedValue != null && fprime < closedValue) {
                    closed.remove(newKey);
                }
                present.put(newKey, fprime);
            }

            // is the new state a solution?
            if (newSub.getDepth() == problem.nbVars() && (newSub.getValue() < bestUB)) {
                assert (Math.abs(h) <= 1e-10);
                bestSol = Optional.of(newSub.getPath());
                bestUB = newSub.getValue();
                SearchStatistics stats = new SearchStatistics(SearchStatus.SAT, nbIter,
                        queueMaxSize, System.currentTimeMillis() - t0, bestUB, gap());
                onSolution.accept(constructSolution(path), stats);
                verboseMode.newBest(bestUB);
            }
        }
    }

    /**
     * Construct the root of a problem given the state, the value and the depth of the root node.
     * A non-zero depth is used for debug. For debug, the value of root is 0.
     *
     * @param state The states of the current root.
     * @param value The value of the current root.
     * @param depth Used only for debug. The depth of the subproblem root in the main search.
     * @return the root subproblem
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
        return new SubProblem<>(
                state,
                value,
                lb.fastLowerBound(state, vars),
                nullDecisions);
    }


    private double gap() {
        return problem.optimalValue()
                .map(opti -> 100 * (Math.abs(bestUB - opti) / Math.abs(opti)))
                .orElse(100.);
    }

    /**
     * Checks if the lower bound of explored nodes of the search is admissible.
     */
    private void checkFLBAdmissibility() {

        HashSet<StateAndDepth<T>> toCheck = new HashSet<>(closed.keySet());
        toCheck.addAll(present.keySet());
        AwAstarModel<T> model = new AwAstarModel<>() {
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
            public double weight() {
                return weight;
            }
        };

        DebugUtil.checkFlbAdmissibility(toCheck, model, key -> new AwAstar<>(model, key));
    }
}
