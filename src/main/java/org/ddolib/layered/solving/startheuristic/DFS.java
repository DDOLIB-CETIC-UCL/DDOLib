package org.ddolib.layered.solving.startheuristic;

import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.common.util.StateAndDepth;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.solver.Solution;
import org.ddolib.layered.solving.ddo.core.Decision;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Depth-first start heuristic returning the first feasible solution it encounters.
 *
 * <p>Its purpose is to quickly provide an upper bound to a complete search algorithm. The
 * children of a node are explored by increasing {@code g + h}, where {@code h} is given by the
 * model's {@link FastLowerBound}, so that the first dive follows the most promising decisions.
 *
 * <p>Since the search stops at the first solution, every node whose subtree has been fully
 * explored is a dead end. States already explored at the same depth, or dominated (according to
 * the model's {@link DominanceChecker}) by an explored state, are therefore skipped.
 *
 * <p>The returned statistics reuse {@link AstarStats}.
 *
 * @param <T> the type of state
 */
public final class DFS<T> {

    private final Problem<T> problem;
    private final FastLowerBound<T> lb;
    private final DominanceChecker<T> dominance;

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
        Set<StateAndDepth<T>> explored = new HashSet<>();

        frontier.push(new Node<>(problem.initialState(), problem.initialValue(), 0, null, null));

        while (!frontier.isEmpty()) {
            statistics =
                    statistics
                            .incrementNbIter()
                            .updateFrontierMaxSize(frontier.size())
                            .updateTime(System.currentTimeMillis());

            if (limit.test(statistics)) {
                return new Solution(Optional.empty(), statistics.updateStatus(SearchStatus.UNKNOWN));
            }

            Node<T> current = frontier.pop();

            if (current.depth() == problem.nbVars()) {
                statistics = statistics
                        .updateTime(System.currentTimeMillis())
                        .updateIncumbent(current.value(), Double.POSITIVE_INFINITY)
                        .updateStatus(SearchStatus.SAT);
                return new Solution(Optional.of(buildPath(current)), statistics);
            }

            // Every node popped before at the same depth has had its whole subtree explored
            // without reaching a solution. Skipping states equal to, or dominated by, such a node
            // assumes that a dominating state is at least as feasible as the dominated one.
            if (!explored.add(new StateAndDepth<>(current.state(), current.depth()))
                    || dominance.updateDominance(
                    current.state(), current.depth(), current.value())) {
                continue;
            }

            pushChildren(current, frontier);
        }

        return new Solution(Optional.empty(), statistics.updateStatus(SearchStatus.UNSAT));
    }

    private void pushChildren(Node<T> node, Deque<Node<T>> frontier) {
        T state = node.state();
        int var = node.depth();
        Set<Integer> unassigned =
                IntStream.range(var + 1, problem.nbVars()).boxed().collect(Collectors.toSet());

        List<Candidate<T>> children = new ArrayList<>();
        var domain = problem.domain(state, var);
        while (domain.hasNext()) {
            Decision decision = new Decision(var, domain.next());
            T newState = problem.transition(state, decision);
            double g = node.value() + problem.transitionCost(state, decision);
            double h = lb.fastLowerBound(newState, unassigned);
            children.add(new Candidate<>(new Node<>(newState, g, var + 1, decision, node), g + h));
        }

        // Pushed from the worst to the best estimate, so the best child is popped first.
        children.sort(Comparator.comparingDouble(Candidate<T>::f).reversed());
        for (Candidate<T> child : children) {
            frontier.push(child.node());
        }
    }

    private Set<Decision> buildPath(Node<T> leaf) {
        Set<Decision> path = new HashSet<>();
        for (Node<T> n = leaf; n.decision() != null; n = n.parent()) {
            path.add(n.decision());
        }
        return path;
    }

    /**
     * A node of the search tree. The path is stored through the parent link to avoid copying it
     * for each child.
     */
    private record Node<T>(T state, double value, int depth, Decision decision, Node<T> parent) {
    }

    /**
     * A child waiting to be pushed, with its estimated cost {@code g + h}.
     */
    private record Candidate<T>(Node<T> node, double f) {
    }
}
