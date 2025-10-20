package org.ddolib.astar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.DebugLevel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.DebugUtil;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

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


    private PriorityQueue<SubProblem<T>>[] open;

    private final int columnWidth;

    private final SubProblem<T> root;

    private final VerbosityLevel verbosityLevel;


    /**
     * Creates a fully qualified instance. The parameters of this solver are given via a
     * {@link org.ddolib.modeling.AcsModel}<br><br>
     *
     * <b>Mandatory parameters:</b>
     * <ul>
     *     <li>An implementation of {@link Problem}</li>
     *         <li>An implementation of {@link FastLowerBound}</li>
     *     <li>An implementation of {@link VariableHeuristic}</li>
     * </ul>
     * <br>
     * <b>Optional parameters: </b>
     * <ul>
     *     <li>An implementation of {@link DominanceChecker}</li>
     *     <li>A verbosity level</li>
     * </ul>
     *
     * @param model All the parameters needed to configure the solver.
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


        this.open = new PriorityQueue[problem.nbVars() + 1];
        for (int i = 0; i < problem.nbVars() + 1; i++) {
            open[i] = new PriorityQueue<>(Comparator.comparingDouble(SubProblem<T>::f));
        }

        this.verbosityLevel = model.verbosityLevel();

        this.root = constructRoot(problem.initialState(), problem.initialValue(), 0);
        if (model.debugMode() != DebugLevel.OFF) {
            throw new IllegalArgumentException("The debug mode for this solver is not available " +
                    "for the moment.");
        }

    }

    private boolean allEmpty() {
        for (PriorityQueue<SubProblem<T>> q : open) {
            if (!q.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public SearchStatistics minimize(Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        long t0 = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        open[0].add(root);
        present.put(new ACSKey<>(root.getState(), root.getDepth()), root.f());

        ArrayList<SubProblem<T>> candidates = new ArrayList<>();
        while (!allEmpty()) {

            SearchStatistics stats = new SearchStatistics(SearchStatus.UNKNOWN, nbIter, queueMaxSize,
                    System.currentTimeMillis() - t0, bestValue().orElse(Double.POSITIVE_INFINITY), 0);

            if (limit.test(stats)) {
                return stats;
            }

            for (int i = 0; i < problem.nbVars() + 1; i++) {
                candidates.clear();
                int l = min(columnWidth, open[i].size());
                for (int j = 0; j < l; j++) {
                    SubProblem<T> sub = open[i].poll();
                    ACSKey<T> subKey = new ACSKey<>(sub.getState(), sub.getDepth());
                    present.remove(subKey);
                    if (sub.f() < bestUB) {
                        candidates.add(sub);
                    } else {
                        // all the next ones will be worse since f is a lower-bound
                        open[i].clear();
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
                        double gap = 0; // TODO compute the gap when we have a valid LB
                        stats = new SearchStatistics(SearchStatus.UNKNOWN, nbIter, queueMaxSize,
                                System.currentTimeMillis() - t0, bestUB, gap);
                        onSolution.accept(constructSolution(bestSol.get()), stats);
                    } else {
                        addChildren(sub, i + 1);
                    }
                }

            }
            queueMaxSize = Math.max(queueMaxSize, Arrays.stream(open).mapToInt(q -> q.size()).sum());
        }
        return new SearchStatistics(SearchStatus.OPTIMAL, nbIter, queueMaxSize,
                System.currentTimeMillis() - t0, bestValue().orElse(Double.POSITIVE_INFINITY), 0);
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
        return new SubProblem<>(
                state,
                value,
                lb.fastLowerBound(state, vars),
                Collections.EMPTY_SET);
    }


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
                    open[newSub.getDepth()].add(newSub);
                    present.put(newKey, newSub.f());
                } else {
                    Double closedValue = closed.get(newKey);
                    if (closedValue != null && closedValue > newSub.f()) {
                        open[newSub.getDepth()].add(newSub);
                        closed.remove(newKey);
                        present.put(newKey, newSub.f());
                    } else {
                        open[newSub.getDepth()].add(newSub);
                        present.put(newKey, newSub.f());
                    }
                }

            }
        }
    }


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
     * Class containing a state and its depth in the main search.
     *
     * @param state A state of the solved problem.
     * @param depth The depth of the input state in the main search.
     * @param <T>   The type of the state.
     */
    private record ACSKey<T>(T state, int depth) {
    }
}
