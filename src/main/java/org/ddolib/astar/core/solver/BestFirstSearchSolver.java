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

import java.util.*;

public final class BestFirstSearchSolver<T, K> implements Solver {

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
     * Value of the best known upper bound (incumbent).
     */
    private double bestUB;

    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T, K> dominance;

    private final PriorityQueue<SubProblem<T>> frontier = new PriorityQueue<>(
            Comparator.comparingDouble(SubProblem<T>::f));

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
     * 3: 2 + every developed subproblem
     * 4: 3 + details about the developed state
     */
    private final int verbosityLevel;

    /**
     * Creates a fully qualified instance
     *
     * @param config The parameters of the solver
     */
    public BestFirstSearchSolver(SolverConfig<T, K> config) {
        this.problem = config.problem;
        this.varh = config.varh;
        this.lb = config.flb;
        this.dominance = config.dominance;
        this.bestUB = Integer.MAX_VALUE;
        this.bestSol = Optional.empty();
        this.verbosityLevel = config.verbosityLevel;
    }

    static int maxDepth = 0;

    @Override
    public SearchStatistics minimize() {
        long t0 = System.currentTimeMillis();
        long ti = t0;
        int nbIter = 0;
        int queueMaxSize = 0;
        frontier.add(root());

        while (!frontier.isEmpty()) {
            if (verbosityLevel >= 1) {
                if (System.currentTimeMillis() - ti > 2000) {
                    System.out.println("it " + nbIter + "\t frontier:" + frontier.size() + "\t " + "bestObj:" + bestUB);
                    ti = System.currentTimeMillis();
                }
            }

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, frontier.size());

            SubProblem<T> sub = frontier.poll();

            if (sub.getDepth() > maxDepth) {
                maxDepth = sub.getDepth();
                System.out.println("max depth"+maxDepth);
            }

            if (sub.getPath().size() == problem.nbVars()) {
                // optimal solution found
                bestSol = Optional.of(sub.getPath());
                bestUB = sub.getValue();
                break;
            }

            double nodeLB = sub.getLowerBound();

            if (verbosityLevel >= 2) {
                System.out.println("subProblem(lb:" + nodeLB + " val:" + sub.getValue() + " depth:" + sub.getPath().size() + " fastLowerBound:" + (nodeLB - sub.getValue()) + "):" + sub.getState());
            }
            if (nodeLB >= bestUB) { // if the smallest lower-bound in the frontier is already larger than the best known solution, we stop
                frontier.clear();
                return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.OPTIMAL, 0.0);
            }
            addChildren(sub);
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
     * @return the root subproblem
     */
    private SubProblem<T> root() {
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Integer.MIN_VALUE,
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
            double fastLowerBound = lb.fastLowerBound(newState, varSet(path));
            // if the new state is dominated, we skip it
            if (!dominance.updateDominance(newState, path.size(), value)) {
                frontier.add(new SubProblem<>(newState, value, fastLowerBound, path));
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
