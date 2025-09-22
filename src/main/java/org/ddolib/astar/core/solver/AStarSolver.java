package org.ddolib.astar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.DebugUtil;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AStarSolver<T, K> implements Solver {

    /**
     * The problem we want to maximize
     */
    private final Problem<T> problem;
    /**
     * A suitable lb for the problem we want to maximize
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
     * HashMap with all explored nodes
     */
    private final HashMap<AstarKey<T>, Double> closed;

    /**
     * HashMap with states in the Priority Queue
     */
    private final HashMap<AstarKey<T>, Double> present;

    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T, K> dominance;
    /**
     * The priority queue containing the subproblems to be explored,
     * ordered by decreasing f = value + fastUpperBound
     */
    private final PriorityQueue<SubProblem<T>> frontier = new PriorityQueue<>(
            Comparator.comparingDouble(SubProblem<T>::f));

    private final SubProblem<T> root;

    private boolean negativeTransitionCosts = false; // in case of negative transition cost, A* must not stop


    /**
     * <ul>
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
    private final int verbosityLevel;

    /**
     * Whether to export the first explored restricted and relaxed mdd.
     */
    private final boolean exportAsDot;

    /**
     * <ul>
     *     <li>0: no additional tests</li>
     *     <li>1: checks if the upper bound is well-defined</li>
     *     <li>2: 1 + export diagram with failure in {@code output/failure.dot}</li>
     * </ul>
     */
    private final int debugLevel;

    public AStarSolver(
            SolverConfig<T, K> config) {
        this.problem = config.problem;
        this.varh = config.varh;
        this.lb = config.flb;
        this.dominance = config.dominance;
        this.bestUB = Integer.MAX_VALUE;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verbosityLevel = config.verbosityLevel;
        this.exportAsDot = config.exportAsDot;
        this.debugLevel = config.debugLevel;
        this.root = constructRoot(problem.initialState(), problem.initialValue(), 0);

    }

    /**
     * Internal constructor used for debug. The solver start the search from a node of the main
     * search. For testing purpose, this constructor assumes that the path to the given node has
     * 0 length.
     *
     * @param config  All parameters needed ton configure the solver
     * @param rootKey The state and the depth from which start the search.
     */
    private AStarSolver(
            SolverConfig<T, K> config,
            AstarKey<T> rootKey
    ) {
        this.problem = config.problem;
        this.varh = config.varh;
        this.lb = config.flb;
        this.dominance = config.dominance;
        this.bestUB = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verbosityLevel = config.verbosityLevel;
        this.exportAsDot = config.exportAsDot;
        this.debugLevel = config.debugLevel;
        this.root = constructRoot(rootKey.state, 0, rootKey.depth);
    }

    @Override
    public SearchStatistics minimize() {
        long t0 = System.currentTimeMillis();
        long ti = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        frontier.add(root);
        present.put(new AstarKey<>(root.getState(), root.getDepth()), root.f());
        while (!frontier.isEmpty()) {
            if (verbosityLevel >= 1) {
                if (System.currentTimeMillis() - ti > 500) {
                    System.out.println("bestObj:" + bestUB+ " lb min:"+frontier.peek().f());
                    System.out.println("it " + nbIter + "\t frontier:" + frontier.size() + "\t " + "bestObj:" + bestUB + " Gap=" + Math.round(100*Math.abs(frontier.peek().f()-bestUB)/bestUB)+ "%");
                    ti = System.currentTimeMillis();
                }
            }

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, frontier.size());

            SubProblem<T> sub = frontier.poll();
            AstarKey<T> subKey = new AstarKey<>(sub.getState(), sub.getDepth());
            present.remove(subKey);
            if (closed.containsKey(subKey)) {
                continue;
            }
            if (sub.getPath().size() == problem.nbVars()) {
                if (debugLevel >= 1) {
                    checkFLBAdmissibility();
                }
                if (sub.getValue() > bestUB) continue; // this solution is dominated by best sol
                bestSol = Optional.of(sub.getPath());
                bestUB = sub.getValue();
                if (!negativeTransitionCosts) {
                    // with A*, the first complete solution is optimal only if there is no negative transition cost
                    break;
                }
            } else if (sub.getPath().size() < problem.nbVars()) {
                double nodeUB = sub.getLowerBound();
                if (verbosityLevel >= 2) {
                    System.out.println("subProblem(ub:" + nodeUB + " val:" + sub.getValue() + " depth:" + sub.getPath().size() + " fastUpperBound:" + (nodeUB - sub.getValue()) + "):" + sub.getState());
                }
                addChildren(sub, debugLevel);
                closed.put(subKey, sub.f());
            }
        }
        return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.OPTIMAL, 0.0);
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
        Set<Decision> nullDecisions = new HashSet<>();
        for (int i = 0; i < depth; i++) {
            nullDecisions.add(new Decision(i, 0));
        }
        return new SubProblem<>(
                state,
                value,
                lb.fastLowerBound(state, vars),
                nullDecisions);
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
                if (debugLevel >= 2) {
                    checkFLBConsistency(subProblem, newSub, cost);
                }
                AstarKey<T> newKey = new AstarKey<>(newState, newSub.getDepth());
                Double presentValue = present.get(newKey);
                if (presentValue != null && presentValue > newSub.f()) {
                    frontier.add(newSub);
                    present.put(newKey, newSub.f());
                } else {
                    Double closedValue = closed.get(newKey);
                    if (closedValue != null && closedValue > newSub.f()) {
                        frontier.add(newSub);
                        closed.remove(newKey);
                        present.put(newKey, newSub.f());
                    } else {
                        frontier.add(newSub);
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
        SolverConfig<T, K> config = new SolverConfig<>();
        config.problem = this.problem;
        config.varh = this.varh;
        config.flb = this.lb;
        config.dominance = this.dominance;

        for (AstarKey<T> current : toCheck) {
            AStarSolver<T, K> internalSolver = new AStarSolver<>(config, current);
            Set<Integer> vars = IntStream.range(current.depth, problem.nbVars()).boxed().collect(Collectors.toSet());
            double currentFLB = lb.fastLowerBound(current.state, vars);

            internalSolver.minimize();
            Optional<Double> shortestFromCurrent = internalSolver.bestValue();
            if (shortestFromCurrent.isPresent() && currentFLB + 1e-10 > shortestFromCurrent.get()) {
                DecimalFormat df = new DecimalFormat("#.#########");
                String failureMsg = "Your lower bound is not admissible.\n" +
                        "State: " + current.state.toString() + "\n" +
                        "Depth: " + current.depth + "\n" +
                        "Path estimation: " + df.format(currentFLB) + "\n" +
                        "Longest path to end: " + df.format(shortestFromCurrent.get()) + "\n";

                throw new RuntimeException(failureMsg);
            }
        }
    }

    /**
     * Given the current node and one of its successor. Checks if the lower bound is consistent.
     *
     * @param current        The current node.
     * @param next           A successor of the current node.
     * @param transitionCost The transition cost from {@code current} to {@code next}.
     */
    private void checkFLBConsistency(
            SubProblem<T> current,
            SubProblem<T> next,
            double transitionCost
    ) {
        Logger logger = Logger.getLogger(AStarSolver.class.getName());
        if (current.getLowerBound() - 1e-10 > next.getLowerBound() + transitionCost) {
            String warningMsg = "Your upper is not consistent. You may lose performance.\n" +
                    "Current state " + current + "\n" +
                    "Next state: " + next + "\n" +
                    "Transition cost: " + transitionCost + "\n";
            logger.warning(warningMsg);
        }

    }

    /**
     * Class containing a state and its depth in the main search.
     *
     * @param state A state of the solved problem.
     * @param depth The depth of the input state in the main search.
     * @param <T>   The type of the state.
     */
    private record AstarKey<T>(T state, int depth) {
    }
}
