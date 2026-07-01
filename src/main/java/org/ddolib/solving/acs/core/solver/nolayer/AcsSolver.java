package org.ddolib.solving.acs.core.solver.nolayer;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.layered.Solution;
import org.ddolib.common.solver.layered.Solver;
import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.solving.ddo.core.Decision;
import org.ddolib.modeling.nolayer.AcsModel;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Problem;
import org.ddolib.solving.astar.core.solver.nolayer.SubProblem;
import org.ddolib.util.verbosity.VerboseMode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class AcsSolver<T> implements Solver {

    private final Problem<T> problem;
    private final FastLowerBound<T> lb;
    private final NoLayerDominanceChecker<T> dominance;
    private final HashMap<T, Double> closed;
    private final HashMap<T, Double> present;
    private final List<PriorityQueue<SubProblem<T>>> open;
    private final int columnWidth;
    private final SubProblem<T> root;
    private final VerboseMode verboseMode;
    private boolean defaultLowerBoundValue;
    private double bestUB;
    private Optional<List<Integer>> bestSol;

    public AcsSolver(AcsModel<T> model) {
        this.problem = model.problem();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
        this.bestUB = model.upperBound();
        this.bestSol = Optional.empty();
        this.columnWidth = model.columnWidth();

        this.closed = new HashMap<>();
        this.present = new HashMap<>();
        this.open = new ArrayList<>();
        this.open.add(new PriorityQueue<>(Comparator.comparingDouble(SubProblem<T>::f)));

        this.verboseMode = new VerboseMode(model.verbosityLevel(), 500);

        this.root = constructRoot(problem.initialState(), problem.initialValue());
        this.defaultLowerBoundValue = lb.fastLowerBound(problem.initialState()) == Integer.MIN_VALUE;
    }

    private SubProblem<T> constructRoot(T state, double value) {
        return new SubProblem<>(state, value, lb.fastLowerBound(state), new ArrayList<>());
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
    public Solution minimize(Predicate<SearchStatistics> limit, BiConsumer<int[], SearchStatistics> onSolution) {
        AstarStats statistics = new AstarStats(System.currentTimeMillis(), bestUB);
        open.get(0).add(root);
        present.put(root.getState(), root.f());

        if (root.f() == Integer.MIN_VALUE) {
            defaultLowerBoundValue = true;
        }

        ArrayList<SubProblem<T>> candidates = new ArrayList<>();
        while (!allEmpty()) {
            verboseMode.detailedSearchState(statistics.nbIterations(),
                    open.stream().mapToInt(PriorityQueue::size).sum(),
                    bestUB,
                    open.stream()
                            .filter(pq -> !pq.isEmpty())
                            .mapToDouble(pq -> pq.peek().getLowerBound())
                            .min().orElse(Double.POSITIVE_INFINITY),
                    gap());

            statistics = statistics.updateTime(System.currentTimeMillis()).updateGap(gap());

            if (limit.test(statistics)) {
                return new Solution(bestSolution(), statistics);
            }

            int currentMaxDepth = open.size();
            for (int i = 0; i < currentMaxDepth; i++) {
                candidates.clear();
                int l = Math.min(columnWidth, open.get(i).size());
                for (int j = 0; j < l; j++) {
                    SubProblem<T> sub = open.get(i).poll();
                    if (sub.getState() != null && dominance.updateDominance(sub.getState(), sub.getValue())) {
                        continue;
                    }
                    present.remove(sub.getState());
                    if (sub.f() < bestUB) {
                        candidates.add(sub);
                    } else {
                        open.get(i).clear();
                        break;
                    }
                }
                for (SubProblem<T> sub : candidates) {
                    statistics = statistics.incrementNbIter();
                    this.closed.put(sub.getState(), sub.f());

                    if (problem.isTarget(sub.getState())) {
                        if (bestUB > sub.getValue()) {
                            bestSol = Optional.of(sub.getPath());
                            bestUB = sub.getValue();
                            statistics = statistics.updateIncumbent(bestUB, gap()).updateStatus(SearchStatus.SAT);
                            onSolution.accept(constructSolution(bestSol.get()), statistics);
                        }
                        verboseMode.newBest(bestUB);
                    } else {
                        addChildren(sub, onSolution, statistics);
                    }
                }
            }
            statistics = statistics.updateFrontierMaxSize(open.stream().mapToInt(PriorityQueue::size).sum());
        }

        statistics = statistics.updateTime(System.currentTimeMillis());

        if (bestSol.isPresent()) statistics = statistics.updateStatus(SearchStatus.OPTIMAL).updateIncumbent(bestUB, 0);
        else statistics = statistics.updateStatus(SearchStatus.UNSAT);

        return new Solution(bestSolution(), statistics);
    }

    private void addChildren(SubProblem<T> subProblem, BiConsumer<int[], SearchStatistics> onSolution, AstarStats statistics) {
        T state = subProblem.getState();
        Iterator<Integer> domain = problem.domain(state);
        while (domain.hasNext()) {
            int label = domain.next();
            T newState = problem.transition(state, label);
            double cost = problem.transitionCost(state, label);

            double g = subProblem.getValue() + cost;
            List<Integer> path = new ArrayList<>(subProblem.getPath());
            path.add(label);
            int newDepth = path.size();

            double h = lb.fastLowerBound(newState);
            double f = g + h;

            if (f + 1e-10 > bestUB) continue;

            SubProblem<T> newSub = new SubProblem<>(newState, g, h, path);
            Double presentValue = present.get(newState);

            while (open.size() <= newDepth) {
                open.add(new PriorityQueue<>(Comparator.comparingDouble(SubProblem<T>::f)));
            }

            if (presentValue != null && presentValue > newSub.f()) {
                open.get(newDepth).add(newSub);
                present.put(newState, newSub.f());
            } else if (presentValue == null) {
                Double closedValue = closed.get(newState);
                if (closedValue != null && closedValue > newSub.f()) {
                    open.get(newDepth).add(newSub);
                    closed.remove(newState);
                    present.put(newState, newSub.f());
                } else if (closedValue == null) {
                    open.get(newDepth).add(newSub);
                    present.put(newState, newSub.f());
                }
            }

            if (problem.isTarget(newState) && newSub.getValue() < bestUB) {
                bestSol = Optional.of(newSub.getPath());
                bestUB = newSub.getValue();
                this.bestUB = newSub.getValue();
            }
        }
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

    private double gap() {
        if (Double.isInfinite(bestUB)) return Double.POSITIVE_INFINITY;

        double globalLB = open.stream()
                .filter(pq -> !pq.isEmpty())
                .mapToDouble(pq -> defaultLowerBoundValue ? pq.peek().getValue() : pq.peek().f())
                .min()
                .orElse(bestUB);

        return 100 * Math.abs((bestUB - globalLB) / bestUB);
    }

    private int[] constructSolution(List<Integer> path) {
        int[] sol = new int[path.size()];
        for (int i = 0; i < path.size(); i++) {
            sol[i] = path.get(i);
        }
        return sol;
    }
}
