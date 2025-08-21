package org.ddolib.astar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;

import java.util.*;

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
    private HashMap<T, Double> closed;

    /**
     * HashMap with state in the Pirority Queue
     */
    private HashMap<T, Double> present;

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


    /**
     * <ul>
     *     <li>0: no verbosity</li>
     *     <li>1: display newBest whenever there is a newBest</li>
     *     <li>2: 1 + statistics about the front every half a second (or so)</li>
     *     <li>3: 2 + every developed sub-problem</li>
     *     <li>4: 3 + details about the developed state</li>
     * </ul>
     * <p>
     * <p>
     * 3: 2 + every developed sub-problem
     * 4: 3 + details about the developed state
     */
    private final int verbosityLevel;

    /**
     * Whether we want to export the first explored restricted and relaxed mdd.
     */
    private final boolean exportAsDot;

    /**
     * Creates a new instance.
     *
     * @param config All the parameters needed to configure the solver.
     */
    public AStarSolver(
            SolverConfig<T, K> config) {
        this.problem = config.problem;
        this.varh = config.varh;
        this.ub = config.fub;
        this.dominance = config.dominance;
        this.bestLB = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verbosityLevel = config.verbosityLevel;
        this.exportAsDot = config.exportAsDot;
    }


    @Override
    public SearchStatistics maximize() {
        long t0 = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        SubProblem<T> root = root();
        frontier.add(root);
        present.put(root.getState(), root.f());
        while (!frontier.isEmpty()) {
            if (verbosityLevel >= 1) {
                System.out.println("it " + nbIter + "\t frontier:" + frontier.size() + "\t " + "bestObj:" + bestLB);
            }

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, frontier.size());

            SubProblem<T> sub = frontier.poll();
            present.remove(sub.getState());
            if (closed.containsKey(sub.getState())) {
                continue;
            }
            if (sub.getPath().size() == problem.nbVars()) {
                // optimal solution found
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
            addChildren(sub);
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
    private SubProblem<T> root() {
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Integer.MAX_VALUE,
                Collections.emptySet());
    }


    private void addChildren(SubProblem<T> subProblem) {
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
                if (present.containsKey(newState)) {
                    if (present.get(newState) < newSub.f()) {
                        frontier.add(newSub);
                    }
                } else if (closed.containsKey(newState)) {
                    if (closed.get(newState) < newSub.f()) {
                        frontier.add(newSub);
                        closed.remove(newState);
                        present.put(newState, newSub.f());

                    }
                } else {
                    frontier.add(newSub);
                    present.put(newState, newSub.f());
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
}
