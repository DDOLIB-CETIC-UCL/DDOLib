package org.ddolib.astar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;

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
     * A suitable ub for the problem we want to maximize
     */
    private final FastUpperBound<T> ub;
    /**
     * A heuristic to choose the next variable to branch on when developing a DD
     */
    private final VariableHeuristic<T> varh;

    /**
     * Value of the best known lower bound.
     */
    private double bestLB;

    /**
     * HashMap with all explored nodes
     */
    private final HashMap<AstarKey<T>, Double> closed;

    /**
     * HashMap with state in the Priority Queue
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


    private final PriorityQueue<SubProblem<T>> frontier = new PriorityQueue<>(
            Comparator.comparingDouble(SubProblem<T>::f).reversed());

    private final SubProblem<T> root;

    /**
     * Creates a fully qualified instance
     *
     * @param problem   The problem we want to maximize.
     * @param ub        A suitable upper-bound for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param dominance The dominance object that will be used to prune the search space.
     */
    public AStarSolver(
            final Problem<T> problem,
            final VariableHeuristic<T> varh,
            final FastUpperBound<T> ub,
            final DominanceChecker<T, K> dominance) {
        this.problem = problem;
        this.varh = varh;
        this.ub = ub;
        this.dominance = dominance;
        this.bestLB = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.root = constructRoot(problem.initialState(), problem.initialValue(), 0);
    }

    public AStarSolver(
            final Problem<T> problem,
            final VariableHeuristic<T> varh,
            final FastUpperBound<T> ub,
            final DominanceChecker<T, K> dominance,
            T state,
            int depth
    ) {
        this.problem = problem;
        this.varh = varh;
        this.ub = ub;
        this.dominance = dominance;
        this.bestLB = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.root = constructRoot(state, 0, depth);
    }

    @Override
    public SearchStatistics maximize() {
        return maximize(0, 0, false);
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel, int debugLevel, boolean exportAsDot) {
        long t0 = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        frontier.add(root);
        present.put(new AstarKey<>(root.getState(), root.getDepth()), root.f());
        while (!frontier.isEmpty()) {
            //System.out.println("Frontier: " + frontier);
            if (verbosityLevel >= 1) {
                System.out.println("it " + nbIter + "\t frontier:" + frontier.size() + "\t " + "bestObj:" + bestLB);
            }

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, frontier.size());

            SubProblem<T> sub = frontier.poll();
            AstarKey<T> subKey = new AstarKey<>(sub.getState(), sub.getDepth());
           /* System.out.println("sub: " + sub);
            System.out.println("present: " + present);
            System.out.println("closed: " + closed);
            System.out.println("\n\n");*/

            present.remove(subKey);
            if (closed.containsKey(subKey)) {
                continue;
            }
            if (sub.getPath().size() == problem.nbVars()) {
                //System.out.println("opti: " + sub.getPath());
                // optimal solution found
                if (debugLevel >= 1) {
                    checkFUBAdmissibility();
                }
                bestSol = Optional.of(sub.getPath());
                bestLB = sub.getValue();
                break;
            }

            double nodeUB = sub.getUpperBound();

            if (verbosityLevel >= 2) {
                System.out.println("subProblem(ub:" + nodeUB + " val:" + sub.getValue() + " depth:" + sub.getPath().size() + " fastUpperBound:" + (nodeUB - sub.getValue()) + "):" + sub.getState());
            }
            if (verbosityLevel >= 1) {
                System.out.println("\n");
            }
            addChildren(sub, debugLevel);
            closed.put(subKey, sub.f());
        }
        return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.OPTIMAL, 0.0);
    }

    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestLB);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
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
                ub.fastUpperBound(state, vars),
                nullDecisions);
    }


    private void addChildren(SubProblem<T> subProblem, int debugLevel) {
        T state = subProblem.getState();
        int var = subProblem.getPath().size();
        final Iterator<Integer> domain = problem.domain(state, var);
        while (domain.hasNext()) {
            final int val = domain.next();
            final Decision decision = new Decision(var, val);
            T newState = problem.transition(state, decision);
            double cost = problem.transitionCost(state, decision);
            double value = subProblem.getValue() + cost;
            Set<Decision> path = new HashSet<>(subProblem.getPath());
            path.add(decision);
            double fastUpperBound = ub.fastUpperBound(newState, varSet(path));


            // if the new state is dominated, we skip it
            if (!dominance.updateDominance(newState, path.size(), value)) {
                SubProblem<T> newSub = new SubProblem<>(newState, value, fastUpperBound, path);
                if (debugLevel >= 1) {
                    checkFUBConsistency(subProblem, newSub, cost);
                }
                AstarKey<T> newKey = new AstarKey<>(newState, newSub.getDepth());
                Double presentValue = present.get(newKey);
                if (presentValue != null && presentValue < newSub.f()) {
                    frontier.add(newSub);
                    present.put(newKey, newSub.f());
                } else {
                    Double closedValue = closed.get(newKey);
                    if (closedValue != null && closedValue < newSub.f()) {
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

    private void checkFUBAdmissibility() {

        HashSet<AstarKey<T>> toCheck = new HashSet<>(closed.keySet());
        toCheck.addAll(present.keySet());

        for (AstarKey<T> current : toCheck) {
            AStarSolver<T, K> internalSolver = new AStarSolver<>(problem, varh, ub, dominance, current.state, current.depth);
            Set<Integer> vars = IntStream.range(current.depth, problem.nbVars()).boxed().collect(Collectors.toSet());
            double currentFUB = ub.fastUpperBound(current.state, vars);

            internalSolver.maximize();
            Optional<Double> longestFromCurrent = internalSolver.bestValue();
            if (longestFromCurrent.isPresent() && currentFUB + 1e-10 < longestFromCurrent.get()) {
                DecimalFormat df = new DecimalFormat("#.#########");
                String failureMsg = "Your upper bound is not admissible.\n" +
                        "State: " + current.state.toString() + "\n" +
                        "Depth: " + current.depth + "\n" +
                        "Path estimation: " + df.format(currentFUB) + "\n" +
                        "Longest path to end: " + df.format(longestFromCurrent.get()) + "\n";

                throw new RuntimeException(failureMsg);
            }
        }
    }

    private void checkFUBConsistency(SubProblem<T> current, SubProblem<T> next,
                                     double transitionCost) {
        Logger logger = Logger.getLogger(AStarSolver.class.getName());
        if (current.f() + 1e-10 < problem.initialValue() + current.getValue() + transitionCost + next.getUpperBound()) {
            String warningMsg = "Your upper is not consistent. You may lose performance.\n" +
                    "Current state " + current + "\n" +
                    "Next state: " + next + "\n" +
                    "Transition cost: " + transitionCost + "\n";
            logger.warning(warningMsg);
        }

    }

    private record AstarKey<T>(T state, int depth) {
    }
}
