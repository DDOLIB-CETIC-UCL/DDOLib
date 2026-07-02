package org.ddolib.nolayer.solving.ddo.core.solver;

import org.ddolib.common.cache.Cache;
import org.ddolib.common.cache.SimpleCache;
import org.ddolib.common.compilation.CompilationType;
import org.ddolib.common.mdd.DecisionDiagram;
import org.ddolib.common.solver.stat.DdoStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.nolayer.common.solver.Solution;
import org.ddolib.nolayer.common.solver.Solver;
import org.ddolib.layered.solving.ddo.core.Decision;
import org.ddolib.layered.solving.ddo.core.SubProblem;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.nolayer.solving.ddo.core.mdd.NoLayerDecisionDiagram;
import org.ddolib.util.verbosity.VerboseMode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class DdoSolver<T> implements Solver {

    private final Problem<T> problem;
    private final DdoModel<T> model;
    private final Optional<SimpleCache<T>> cache;
    private final VerboseMode verboseMode;

    // Using a priority queue for the B&B frontier
    // Lower bound is the primary key
    private final PriorityQueue<SubProblem<T>> frontier;

    private double bestUB;
    private Optional<Set<Decision>> bestSol;

    public DdoSolver(DdoModel<T> model) {
        this.model = model;
        this.problem = model.problem();
        this.cache = model.useCache() ? Optional.of(new SimpleCache<>()) : Optional.empty();
        this.bestUB = Double.POSITIVE_INFINITY;
        this.bestSol = Optional.empty();
        this.verboseMode = new VerboseMode(model.verbosityLevel(), 500L);

        this.frontier = new PriorityQueue<>(Comparator.comparingDouble(SubProblem::getLowerBound));
    }

    @Override
    public Solution minimize(Predicate<SearchStatistics> limit, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        DdoStats statistics = new DdoStats(System.currentTimeMillis(), bestUB);
        frontier.add(root());
        cache.ifPresent(c -> c.initialize());

        while (!frontier.isEmpty()) {
            SubProblem<T> sub = frontier.poll();

            // Check cache before expanding
            if (cache.isPresent() && !cache.get().mustExplore(sub, sub.getDepth())) {
                continue;
            }

            double nodeLB = sub.getLowerBound();

            verboseMode.detailedSearchState(statistics.nbIterations(), frontier.size(), bestUB,
                    frontier.isEmpty() ? nodeLB : frontier.peek().getLowerBound(), gap());

            statistics = statistics.incrementNbIter()
                    .updateFrontierMaxSize(frontier.size() + 1)
                    .updateLowerBound(nodeLB)
                    .updateTime(System.currentTimeMillis())
                    .updateGap(gap())
                    .updateMaxDepth(sub.getDepth());

            if (limit.test(statistics)) {
                return new Solution(bestSolution(), statistics);
            }

            verboseMode.currentSubProblem(statistics.nbIterations(), sub);
            if (nodeLB >= bestUB) {
                frontier.clear();
                statistics = statistics.updateTime(System.currentTimeMillis())
                        .updateStatus(SearchStatus.OPTIMAL).updateGap(0);
                return new Solution(bestSolution(), statistics);
            }

            int maxWidth = model.widthHeuristic().maximumWidth(sub.getState());

            // 1. RELAXATION
            if (model.dominance() != null) model.dominance().clear();
            NoLayerDecisionDiagram<T> relaxedMdd = new NoLayerDecisionDiagram<>(
                    model, sub, CompilationType.Relaxed, maxWidth, bestUB, cache.map(c -> (Cache<T>) c));

            relaxedMdd.compile();
            statistics = statistics.addNodes(relaxedMdd.nbNodes());

            if (relaxedMdd.relaxedBestPathIsExact()) {
                boolean newbest = maybeUpdateBest(relaxedMdd);
                if (newbest) {
                    statistics = statistics.updateTime(System.currentTimeMillis())
                            .updateIncumbent(bestUB, gap())
                            .updateStatus(SearchStatus.SAT);
                    onSolution.accept(constructSolution(bestSol.get()), statistics);
                }
            }

            if (!relaxedMdd.isExact() && (relaxedMdd.bestValue().isEmpty() || relaxedMdd.bestValue().get() < bestUB)) {

                // 2. RESTRICTION
                if (model.dominance() != null) model.dominance().clear();
                NoLayerDecisionDiagram<T> restrictedMdd = new NoLayerDecisionDiagram<>(
                        model, sub, CompilationType.Restricted, maxWidth, bestUB, cache.map(c -> (Cache<T>) c));

                restrictedMdd.compile();
                statistics = statistics.addNodes(restrictedMdd.nbNodes());

                boolean newbest = maybeUpdateBest(restrictedMdd);
                if (newbest) {
                    statistics = statistics.updateTime(System.currentTimeMillis())
                            .updateIncumbent(bestUB, gap())
                            .updateStatus(SearchStatus.SAT);
                    onSolution.accept(constructSolution(bestSol.get()), statistics);
                }

                enqueueCutset(relaxedMdd);
            }
        }

        statistics = statistics.updateTime(System.currentTimeMillis());
        if (bestSol.isPresent()) statistics = statistics.updateStatus(SearchStatus.OPTIMAL).updateGap(0);
        else statistics = statistics.updateStatus(SearchStatus.UNSAT);

        return new Solution(bestSolution(), statistics);
    }

    private void enqueueCutset(NoLayerDecisionDiagram<T> relaxedMdd) {
        Iterator<SubProblem<T>> cutset = relaxedMdd.exactCutset();
        while (cutset.hasNext()) {
            SubProblem<T> cutsetNode = cutset.next();
            // Dominance check is already applied inside the MDD, but we can double check
            if (cutsetNode.getLowerBound() < bestUB) {
                frontier.add(cutsetNode);
            }
        }
    }

    private SubProblem<T> root() {
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Double.NEGATIVE_INFINITY,
                Collections.emptySet()
        );
    }

    private double gap() {
        if (frontier.isEmpty() || Double.isInfinite(bestUB)) {
            return Double.POSITIVE_INFINITY;
        } else {
            double globalLB = frontier.peek().getLowerBound();
            return 100 * Math.abs(bestUB - globalLB) / Math.abs(bestUB);
        }
    }

    private boolean maybeUpdateBest(DecisionDiagram<T> currentMdd) {
        Optional<Double> ddval = currentMdd.bestValue();
        if (ddval.isPresent() && ddval.get() < bestUB) {
            bestUB = ddval.get();
            bestSol = currentMdd.bestSolution();
            verboseMode.newBest(bestUB);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Double> bestValue() {
        return bestSol.isPresent() ? Optional.of(bestUB) : Optional.empty();
    }

    @Override
    public List<Integer> bestSolution() {
        return bestSol.map(this::constructSolution).orElse(List.of());
    }

    private List<Integer> constructSolution(Set<Decision> decisions) {
        int maxVar = -1;
        for (Decision d : decisions) maxVar = Math.max(maxVar, d.variable());
        if (maxVar == -1) return List.of();

        int[] sol = new int[maxVar + 1];
        for (Decision d : decisions) sol[d.variable()] = d.value();
        return Arrays.stream(sol).boxed().toList();
    }
}
