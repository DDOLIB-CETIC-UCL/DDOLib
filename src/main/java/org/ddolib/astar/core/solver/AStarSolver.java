package org.ddolib.astar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.debug.DebugUtil;
import org.ddolib.util.verbosity.VerboseMode;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AStarSolver<T> implements Solver {

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
     * Value of the best known upper bound.
     */
    private double bestUB;

    /**
     * HashMap mapping (state,depth) to the f value.
     * Closed nodes are the ones for which their children have been generated.
     */
    private final HashMap<AstarKey<T>, Double> closed;

    /**
     * HashMap mapping (state,depth) open nodes to the f value.
     */
    private final HashMap<AstarKey<T>, Double> present;

    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T> dominance;
    /**
     * The priority queue containing the subproblems to be explored,
     * ordered by decreasing f = value + fastUpperBound
     */
    private final PriorityQueue<SubProblem<T>> open = new PriorityQueue<>(
            Comparator.comparingDouble(SubProblem<T>::f));

    private final SubProblem<T> root;

    private boolean negativeTransitionCosts = false; // in case of negative transition cost, A* must not stop


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
    private final VerbosityLevel verbosityLevel;

    private final VerboseMode verboseMode;

    /**
     * The debug level of the compilation to add additional checks (see
     * {@link DebugLevel for details}
     */
    private final DebugLevel debugLevel;


    public AStarSolver(Model<T> model) {
        this.problem = model.problem();
        this.varh = model.variableHeuristic();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verbosityLevel = model.verbosityLevel();
        this.verboseMode = new VerboseMode(this.verbosityLevel, 500L);
        this.debugLevel = model.debugMode();
        this.root = constructRoot(problem.initialState(), problem.initialValue(), 0);

    }

    /**
     * Internal constructor used for debug. The solver start the search from a node of the main
     * search. For testing purpose, this constructor assumes that the path to the given node has
     * 0 length.
     *
     * @param model   All parameters needed ton configure the solver.
     * @param rootKey The state and the depth from which start the search.
     */
    private AStarSolver(
            Model<T> model,
            AstarKey<T> rootKey
    ) {
        this.problem = model.problem();
        this.varh = model.variableHeuristic();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verbosityLevel = VerbosityLevel.SILENT;
        this.verboseMode = new VerboseMode(VerbosityLevel.SILENT, 500);
        this.debugLevel = DebugLevel.OFF;
        this.root = constructRoot(rootKey.state(), 0, rootKey.depth());
    }

    @Override
    public SearchStatistics minimize(Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        long t0 = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        open.add(root);
        present.put(new AstarKey<>(root.getState(), root.getDepth()), root.f());
        while (!open.isEmpty()) {
            verboseMode.detailedSearchState(nbIter, open.size(), bestUB,
                    open.peek().getLowerBound(), 100 * gap());

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, open.size());

            SubProblem<T> sub = open.poll();
            AstarKey<T> subKey = new AstarKey<>(sub.getState(), sub.getDepth());

            SearchStatistics stats = new SearchStatistics(
                    SearchStatus.UNKNOWN,
                    nbIter,
                    queueMaxSize,
                    System.currentTimeMillis() - t0,
                    bestValue().orElse(Double.POSITIVE_INFINITY),
                    0);

            if (limit.test(stats)) {
                return stats;
            }

            present.remove(subKey);
            if (closed.containsKey(subKey)) {
                continue;
            }
            if (sub.getPath().size() == problem.nbVars()) {

                if (sub.getValue() >= bestUB) continue; // this solution is dominated by best sol
                bestSol = Optional.of(sub.getPath());
                bestUB = sub.getValue();


                onSolution.accept(constructSolution(bestSol.get()), new SearchStatistics(
                        SearchStatus.UNKNOWN,
                        nbIter,
                        queueMaxSize,
                        System.currentTimeMillis() - t0,
                        bestUB,
                        gap()
                ));

                verboseMode.newBest(bestUB);

                if (!negativeTransitionCosts) {
                    // with A*, the first complete solution is  guaranteed optimal only if there is
                    // no negative transition cost
                    break;
                }
                if (open.isEmpty() || open.peek().f() + 1e-10 >= bestUB) {
                    // gap is 0%
                    break;
                }
            } else if (sub.getPath().size() < problem.nbVars()) {
                verboseMode.currentSubProblem(nbIter, sub);
                addChildren(sub);
                closed.put(subKey, sub.f());
            }
        }
        if (debugLevel != DebugLevel.OFF) {
            checkFLBAdmissibility();
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


    private void addChildren(SubProblem<T> subProblem) {
        T state = subProblem.getState();
        int var = subProblem.getPath().size();


        final Iterator<Integer> domain = problem.domain(state, var);
        while (domain.hasNext()) {
            final int val = domain.next();
            final Decision decision = new Decision(var, val);
            if (debugLevel != DebugLevel.OFF) {
                DebugUtil.checkHashCodeAndEquality(state, decision, problem::transition);
            }
            T newState = problem.transition(state, decision);
            double cost = problem.transitionCost(state, decision);
            if (cost < 0) {
                negativeTransitionCosts = true;
            }
            double value = subProblem.getValue() + cost;
            Set<Decision> path = new HashSet<>(subProblem.getPath());
            path.add(decision);
            double fastLowerBound = lb.fastLowerBound(newState, varSet(path));


            // if the new state is dominated, we skip it
            if (!dominance.updateDominance(newState, path.size(), value)) {
                SubProblem<T> newSub = new SubProblem<>(newState, value, fastLowerBound, path);
                if (debugLevel == DebugLevel.EXTENDED) {
                    DebugUtil.checkFlbConsistency(subProblem, newSub, cost);
                }
                AstarKey<T> newKey = new AstarKey<>(newState, newSub.getDepth());
                Double presentValue = present.get(newKey);
                if (presentValue != null && presentValue > newSub.f()) {
                    open.add(newSub);
                    present.put(newKey, newSub.f());
                } else {
                    Double closedValue = closed.get(newKey);
                    if (closedValue != null && closedValue > newSub.f()) {
                        open.add(newSub);
                        closed.remove(newKey);
                        present.put(newKey, newSub.f());
                    } else {
                        open.add(newSub);
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
     * Checks if the lower bound of explored nodes of the search is admissible.
     */
    private void checkFLBAdmissibility() {

        HashSet<AstarKey<T>> toCheck = new HashSet<>(closed.keySet());
        toCheck.addAll(present.keySet());
        Model<T> model = new Model<>() {
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
        };

        DebugUtil.checkFlbAdmissibility(toCheck, model, key -> new AStarSolver<>(model, key));
    }


    private double gap() {
        if (open.isEmpty()) {
            return 0.0;
        } else {
            double bestInFrontier = open.peek().getLowerBound();
            return (bestUB - bestInFrontier) / Math.abs(bestUB);
        }
    }


}
