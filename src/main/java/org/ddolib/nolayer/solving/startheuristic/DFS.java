package org.ddolib.nolayer.solving.startheuristic;

import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.nolayer.solver.Solution;

import java.util.*;
import java.util.function.Predicate;

/**
 * Depth-first start heuristic returning the first feasible solution it encounters.
 *
 * <p>Its purpose is to quickly provide an upper bound to a complete search algorithm. The
 * children of a node are explored by increasing {@code g + h}, where {@code h} is given by the
 * model's {@link FastLowerBound}, so that the first dive follows the most promising decisions.
 *
 * <p>Since the search stops at the first solution, every node whose subtree has been fully
 * explored is a dead end. States already explored, or dominated (according to the model's {@link
 * NoLayerDominanceChecker}) by an explored state, are therefore skipped.
 *
 * <p>The returned statistics reuse {@link AstarStats}.
 *
 * @param <T> the type of state
 */
public final class DFS<T> {

    private final Problem<T> problem;
    private final FastLowerBound<T> lb;
    private final NoLayerDominanceChecker<T> dominance;

    /**
     * Creates the heuristic from a model. Only its problem, fast lower bound and dominance checker
     * are used.
     *
     * @param model the model of the problem to solve
     */
    public DFS(Model<T> model) {
        this.problem = model.problem();
        this.lb = model.lowerBound();
        this.dominance = model.dominance();
    }

    /**
     * Searches the first feasible solution without any stopping criterion.
     *
     * @return the first solution found, or an empty solution with status {@code UNSAT} if the
     * problem has none
     */
    public Solution findSolution() {
        return findSolution(s -> false);
    }

    /**
     * Searches the first feasible solution.
     *
     * @param limit stopping criterion tested at each iteration
     * @return the first solution found (status {@code SAT}), an empty solution with status {@code
     * UNSAT} if the problem has none, or an empty solution with status {@code UNKNOWN} if the
     * limit was reached first
     */
    public Solution findSolution(Predicate<SearchStatistics> limit) {
        SearchStatistics statistics =
                new AstarStats(System.currentTimeMillis(), Double.POSITIVE_INFINITY);
        Deque<Node<T>> frontier = new ArrayDeque<>();
        Set<T> explored = new HashSet<>();

        frontier.push(new Node<>(problem.initialState(), problem.initialValue(), -1, null));

        while (!frontier.isEmpty()) {
            statistics = statistics
                    .incrementNbIter()
                    .updateFrontierMaxSize(frontier.size())
                    .updateTime(System.currentTimeMillis());

            if (limit.test(statistics)) {
                return new Solution(List.of(), statistics.updateStatus(SearchStatus.UNKNOWN));
            }

            Node<T> current = frontier.pop();

            if (problem.isTarget(current.state())) {
                statistics = statistics
                        .updateTime(System.currentTimeMillis())
                        .updateIncumbent(current.value(), Double.POSITIVE_INFINITY)
                        .updateStatus(SearchStatus.SAT);
                return new Solution(buildPath(current), statistics);
            }

            // A state popped before is either fully explored without reaching a target, or an
            // ancestor of the current node (the current path is a cycle). In both cases, it is
            // useless to explore it again, which also prevents infinite loops on cycles. Skipping
            // dominated states assumes that a dominating state is at least as feasible as the
            // dominated one.
            if (!explored.add(current.state())
                    || dominance.updateDominance(current.state(), current.value())) {
                continue;
            }

            pushChildren(current, frontier);
        }

        return new Solution(List.of(), statistics.updateStatus(SearchStatus.UNSAT));
    }

    private void pushChildren(Node<T> node, Deque<Node<T>> frontier) {
        T state = node.state();

        List<Candidate<T>> children = new ArrayList<>();
        Iterator<Integer> domain = problem.domain(state);
        while (domain.hasNext()) {
            int label = domain.next();
            T newState = problem.transition(state, label);
            double g = node.value() + problem.transitionCost(state, label);
            double h = lb.fastLowerBound(newState);
            children.add(new Candidate<>(new Node<>(newState, g, label, node), g + h));
        }

        // Pushed from the worst to the best estimate, so the best child is popped first.
        children.sort(Comparator.comparingDouble(Candidate<T>::f).reversed());
        for (Candidate<T> child : children) {
            frontier.push(child.node());
        }
    }

    private List<Integer> buildPath(Node<T> leaf) {
        List<Integer> path = new ArrayList<>();
        for (Node<T> n = leaf; n.parent() != null; n = n.parent()) {
            path.add(n.label());
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * A node of the search tree. The path is stored through the parent link to avoid copying it
     * for each child. The root has no parent and its label is meaningless.
     */
    private record Node<T>(T state, double value, int label, Node<T> parent) {
    }

    /**
     * A child waiting to be pushed, with its estimated cost {@code g + h}.
     */
    private record Candidate<T>(Node<T> node, double f) {
    }
}
