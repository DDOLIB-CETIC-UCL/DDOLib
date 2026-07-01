package org.ddolib.solving.awastar.core.solver.nolayer;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.nolayer.Solution;
import org.ddolib.common.solver.nolayer.Solver;
import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.modeling.nolayer.AwAstarModel;
import org.ddolib.modeling.nolayer.DefaultFastLowerBound;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Problem;
import org.ddolib.solving.astar.core.solver.nolayer.SubProblem;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerboseMode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class AwAstarSolver<T> implements Solver {
    // The problem we want to minimize
    private final Problem<T> problem;
    // A suitable lb for the problem we want to minimize
    private final FastLowerBound<T> lb;
    // HashMap mapping (state,depth) to the f value
    private final HashMap<T, Double> closed;
    // HashMap mapping (state,depth) open nodes to the f value.
    private final HashMap<T, Double> present;
    // The dominance object that will be used to prune the search space.
    private final NoLayerDominanceChecker<T> dominance;

    // The weight of add to the heuristic function
    private final double weight;

    private final PriorityQueue<SubProblem<T>> open;
    private final PriorityQueue<SubProblem<T>> openByF;

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
    private final boolean defaultLowerBoundValue;

    private AstarStats statistics;
    // Value of the best known upper bound.
    private double bestUB;

    private List<Integer> bestSol;


    public AwAstarSolver(AwAstarModel<T> model) {
        if (model.weight() < 1) {
            throw new IllegalArgumentException("The weight associated to the heuristic function " +
                    "must be >= 1 !");
        }

        if (model.weight() < 1) {
            throw new IllegalArgumentException("The weight associated to the heuristic function " +
                    "must be >= 1 !");
        }

        this.problem = model.problem();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = model.upperBound();
        this.bestSol = List.of();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verboseMode = new VerboseMode(model.verbosityLevel(), 500L);
        this.debugLevel = model.debugMode();

        this.weight = model.weight();
        this.open = new PriorityQueue<>(
                Comparator.comparingDouble(sub -> sub.getValue() + weight * sub.getLowerBound()));

        this.openByF = new PriorityQueue<>(Comparator.comparingDouble(SubProblem::f));
        this.root = constructRoot(problem.initialState(), problem.initialValue());
        this.defaultLowerBoundValue = this.lb instanceof DefaultFastLowerBound<T>;
    }

    private SubProblem<T> constructRoot(T state, double value) {
        return new SubProblem<>(state, value, lb.fastLowerBound(state), new ArrayList<>());
    }


    @Override
    public Solution minimize(Predicate<SearchStatistics> limit, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        statistics = new AstarStats(System.currentTimeMillis(), bestUB);
        open.add(root);
        openByF.add(root);
        present.put(root.getState(), root.f());

        if (problem.isTarget(root.getState())) {
            bestSol = root.getPath();
            statistics = statistics.updateIncumbent(bestUB, gap())
                    .updateStatus(SearchStatus.OPTIMAL);
            return new Solution(bestSol, statistics);
        }

        while (!open.isEmpty()) {
            // -- debug, stat, verbosity, stopping  ---
            verboseMode.detailedSearchState(statistics.nbIterations(), open.size(), bestUB,
                    open.peek().getLowerBound(), statistics.gap());

            statistics = statistics.incrementNbIter()
                    .updateFrontierMaxSize(open.size())
                    .updateTime(System.currentTimeMillis())
                    .updateGap(gap());

            if (limit.test(statistics)) {
                return new Solution(bestSol, statistics.updateTime(System.currentTimeMillis()));
            }
            // -- end debug, stat, verbosity, stopping  ---

            SubProblem<T> sub = open.poll();
            openByF.remove(sub);
            // if current state is dominated, we skip it
            if (dominance.updateDominance(sub.getState(), sub.getValue())) continue;

            double subFprime = present.remove(sub.getState());

            // The current node has been explored. We can skip it.
            if (closed.containsKey(sub.getState())) continue;

            closed.put(sub.getState(), subFprime);

            // Sub can only lead to less good solution.
            if (sub.f() + 1e-10 >= bestUB) continue;

            if (!problem.isTarget(sub.getState())) addChildren(sub, onSolution);
        }

        statistics = statistics.updateTime(System.currentTimeMillis());
        if (!bestSol.isEmpty()) statistics = statistics.updateStatus(SearchStatus.OPTIMAL).updateIncumbent(bestUB, 0);
        else statistics = statistics.updateStatus(SearchStatus.UNSAT);


        return new Solution(bestSol, statistics);
    }

    @Override
    public Optional<Double> bestValue() {
        return bestSol.isEmpty() ? Optional.empty() : Optional.of(bestUB);
    }

    @Override
    public List<Integer> bestSolution() {
        return List.of();
    }

    private void addChildren(SubProblem<T> sub, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        T state = sub.getState();

        final Iterator<Integer> domain = problem.domain(state);
        int nbGeneratedChildren = 0;
        int nbSelectedChildren = 0;

        while (domain.hasNext()) {
            final int label = domain.next();

            T newState = problem.transition(state, label);
            double cost = problem.transitionCost(state, label);
            nbGeneratedChildren++;

            List<Integer> newPath = new ArrayList<>(sub.getPath());
            newPath.addLast(label);

            double g = sub.getValue() + cost;
            double h = lb.fastLowerBound(newState);
            double f = g + h;
            double fprime = g + weight * h;

            if (f + 1e-10 > bestUB) continue;

            SubProblem<T> newSub = new SubProblem<>(newState, g, h, newPath);
            Double previousFprime = present.get(newState);
            if (previousFprime != null && fprime < previousFprime) {
                open.remove(newSub);
                open.add(newSub);
                openByF.remove(newSub);
                openByF.add(newSub);
                present.put(newState, fprime);
                nbSelectedChildren++;
            } else if (previousFprime == null) {
                open.add(newSub);
                openByF.add(newSub);
                present.put(newState, fprime);
                nbSelectedChildren++;

                Double closedFprime = closed.get(newState);
                if (closedFprime != null && fprime < closedFprime) closed.remove(newState);
            }

            // is the new state a solution?
            if (problem.isTarget(newState) && newSub.getValue() < bestUB) {
                bestSol = newSub.getPath();
                bestUB = newSub.getValue();
                statistics = statistics.updateIncumbent(bestUB, gap())
                        .updateStatus(SearchStatus.SAT);
                onSolution.accept(newPath, statistics);
                verboseMode.newBest(bestUB);
            }
        }

        statistics = statistics.updateValidChildrenPercent(nbGeneratedChildren, nbSelectedChildren);
    }

    private double gap() {
        if (Double.isInfinite(bestUB)) return Double.POSITIVE_INFINITY;

        if (open.isEmpty()) return 0.0;

        double globalLB = defaultLowerBoundValue ? openByF.peek().getValue() : openByF.peek().f();
        return 100 * Math.abs((bestUB - globalLB) / bestUB);
    }
}
