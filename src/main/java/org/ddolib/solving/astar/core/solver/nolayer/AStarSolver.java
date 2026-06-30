package org.ddolib.solving.astar.core.solver.nolayer;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.layered.Solution;
import org.ddolib.common.solver.layered.Solver;
import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.solving.ddo.core.Decision;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Model;
import org.ddolib.modeling.nolayer.Problem;
import org.ddolib.util.verbosity.VerboseMode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class AStarSolver<T> implements Solver {

    private final Problem<T> problem;
    private final FastLowerBound<T> lb;
    private final HashMap<T, Double> closed;
    private final HashMap<T, Double> present;
    private final NoLayerDominanceChecker<T> dominance;
    private final PriorityQueue<SubProblem<T>> open = new PriorityQueue<>(
            Comparator.comparingDouble(SubProblem<T>::f));
    private final SubProblem<T> root;
    private final VerboseMode verboseMode;

    private AstarStats statistics;
    private double bestUB;
    private Optional<List<Integer>> bestSol;

    public AStarSolver(Model<T> model) {
        this.problem = model.problem();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = model.upperBound();
        this.bestSol = Optional.empty();
        this.present = new HashMap<>();
        this.closed = new HashMap<>();
        this.verboseMode = new VerboseMode(model.verbosityLevel(), 500L);
        this.root = constructRoot(problem.initialState(), problem.initialValue());
    }

    private SubProblem<T> constructRoot(T state, double value) {
        return new SubProblem<>(state, value, lb.fastLowerBound(state), new ArrayList<>());
    }

    @Override
    public Solution minimize(Predicate<SearchStatistics> limit,
                             BiConsumer<int[], SearchStatistics> onSolution) {
        statistics = new AstarStats(System.currentTimeMillis(), bestUB);
        if (dominance != null) dominance.clear();
        open.add(root);
        present.put(root.getState(), root.f());

        while (!open.isEmpty()) {
            statistics = statistics.incrementNbIter()
                    .updateFrontierMaxSize(open.size())
                    .updateTime(System.currentTimeMillis())
                    .updateGap(gap());

            if (limit.test(statistics)) {
                return new Solution(bestSolution(), statistics);
            }

            SubProblem<T> sub = open.poll();
            T state = sub.getState();

            if (state != null && dominance.updateDominance(state, sub.getValue())) {
                continue;
            }

            present.remove(state);

            if (closed.containsKey(state)) continue;

            closed.put(state, sub.f());

            if (sub.f() + 1e-10 > bestUB) continue;

            if (problem.isTarget(state)) {
                // target reached
                bestSol = Optional.of(sub.getPath());
                bestUB = sub.getValue();
                break; // Because it's A*, the first target reached is optimal
            } else {
                addChildren(sub, onSolution);
            }
        }

        statistics = statistics.updateTime(System.currentTimeMillis());
        if (bestSol.isPresent()) statistics = statistics.updateStatus(SearchStatus.OPTIMAL).updateIncumbent(bestUB, 0);
        else statistics = statistics.updateStatus(SearchStatus.UNSAT);

        return new Solution(bestSolution(), statistics);
    }

    private void addChildren(SubProblem<T> subProblem, BiConsumer<int[], SearchStatistics> onSolution) {
        T state = subProblem.getState();
        Iterator<Integer> domain = problem.domain(state);
        while (domain.hasNext()) {
            int label = domain.next();
            T newState = problem.transition(state, label);
            double cost = problem.transitionCost(state, label);
            double g = subProblem.getValue() + cost;
            List<Integer> newPath = new ArrayList<>(subProblem.getPath());
            newPath.add(label);

            double h = lb.fastLowerBound(newState);
            double f = g + h;

            if (f + 1e-10 > bestUB) continue;

            SubProblem<T> newSub = new SubProblem<>(newState, g, h, newPath);

            Double presentValue = present.get(newState);
            if (presentValue != null && presentValue > newSub.f()) {
                open.add(newSub);
                present.put(newState, newSub.f());
            } else if (presentValue == null) {
                Double closedValue = closed.get(newState);
                if (closedValue != null && closedValue > newSub.f()) {
                    open.add(newSub);
                    closed.remove(newState);
                    present.put(newState, newSub.f());
                } else if (closedValue == null) {
                    open.add(newSub);
                    present.put(newState, newSub.f());
                }
            }

            if (problem.isTarget(newState) && newSub.getValue() < bestUB) {
                bestSol = Optional.of(newSub.getPath());
                bestUB = newSub.getValue();
                statistics = statistics.updateIncumbent(bestUB, gap())
                        .updateStatus(SearchStatus.SAT);
                onSolution.accept(constructSolution(newPath), statistics);
                verboseMode.newBest(bestUB);
            }
        }
    }

    private double gap() {
        if (Double.isInfinite(bestUB)) {
            return Double.POSITIVE_INFINITY;
        } else if (open.isEmpty()) {
            return 0.0;
        } else {
            double globalLB = open.peek().f();
            return 100 * Math.abs(bestUB - globalLB) / Math.abs(bestUB);
        }
    }

    private int[] constructSolution(List<Integer> path) {
        int[] sol = new int[path.size()];
        for (int i = 0; i < path.size(); i++) {
            sol[i] = path.get(i);
        }
        return sol;
    }

    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) return Optional.of(bestUB);
        return Optional.empty();
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        if (bestSol.isPresent()) {
            Set<Decision> sol = new HashSet<>();
            List<Integer> path = bestSol.get();
            for (int i = 0; i < path.size(); i++) {
                sol.add(new Decision(i, path.get(i)));
            }
            return Optional.of(sol);
        }
        return Optional.empty();
    }
}
