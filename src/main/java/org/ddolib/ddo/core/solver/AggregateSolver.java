package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.*;
import org.ddolib.util.BitsetSet;

import java.util.*;

public class AggregateSolver<T, K, TAgg, KAgg> implements Solver {
    private final Problem<T> problem;
    private final SolverInput<TAgg, KAgg> aggregated;
    private final Aggregate<TAgg, KAgg> mapping;
    private final Relaxation<T> relax;
    private final DominanceChecker<T, K> checker;
    private final SequentialSolver<AggregateState, Integer> solver;

    /**
     * Pre-computed best solution for states in the aggregated problem.
     * The states are organised by layer, the key of a layer is the number of unassigned variables in this layer.
     * It is assumed that variables for the aggregated problem are assigned in the same order when exploring the initial and the aggregated diagram.
     */
    private final HashMap<TAgg, Double>[] preComputed;

    /**
     * Wrapper that contains the initial and the aggregated states
     */
    private class AggregateState {
        private final T state;
        private final TAgg aggregated;
        private final BitsetSet unassigned; // Set of unassigned variables for the aggregated problem

        public AggregateState(T state, TAgg aggregated, BitsetSet unassigned) {
            this.state = state;
            this.aggregated = aggregated;
            this.unassigned = unassigned;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof AggregateSolver.AggregateState s)
                return state.equals(s.state);
            return false;
        }

        @Override
        public int hashCode() {
            return state.hashCode();
        }
    }


    /**
     * Converts an iterator of AggregateState to an iterator of its initial states
     */
    private class StateIterator implements Iterator<T> {
        private final Iterator<AggregateState> iterator;

        public StateIterator(Iterator<AggregateState> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return iterator.next().state;
        }
    }


    /**
     * Converts an iterator of AggregateState to an iterator of its aggregated states
     */
    private class AggregatedIterator implements Iterator<TAgg> {
        private final Iterator<AggregateState> iterator;

        public AggregatedIterator(Iterator<AggregateState> iterator) {
            this.iterator = iterator;
        }
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public TAgg next() {
            return iterator.next().aggregated;
        }
    }


    /**
     * Wrapper that applies decisions to the initial and the aggregated states
     */
    private class AggregateProblem implements Problem<AggregateState> {
        @Override
        public int nbVars() {
            return problem.nbVars();
        }

        @Override
        public AggregateState initialState() {
            BitSet unassigned = new BitSet();
            unassigned.set(0, aggregated.problem.nbVars());
            return new AggregateState(problem.initialState(), aggregated.problem.initialState(), new BitsetSet(unassigned));
        }

        @Override
        public double initialValue() {
            return problem.initialValue();
        }

        @Override
        public Iterator<Integer> domain(AggregateState state, int var) {
            return problem.domain(state.state, var);
        }

        @Override
        public AggregateState transition(AggregateState state, Decision decision) {
            TAgg nextAgg = mapping.aggregateTransition(state.aggregated, decision);
            int varAgg = mapping.assignedVariable(state.aggregated, decision, state.unassigned);
            BitsetSet unassigned = (BitsetSet)state.unassigned.clone();
            if (varAgg != -1) unassigned.remove(varAgg);
            return new AggregateState(
                problem.transition(state.state, decision),
                nextAgg,
                unassigned
            );
        }

        @Override
        public double transitionCost(AggregateState state, Decision decision) {
            return problem.transitionCost(state.state, decision);
        }
    }


    /**
     * Wrapper that relaxes the initial and the aggregated states
     */
    private class AggregateRelax implements Relaxation<AggregateState> {
        @Override
        public AggregateState mergeStates(Iterator<AggregateState> states) {
            ArrayList<AggregateState> statesList = new ArrayList<>();
            while (states.hasNext()) statesList.add(states.next());
            return new AggregateState(
                relax.mergeStates(new StateIterator(statesList.iterator())),
                aggregated.relax.mergeStates(new AggregatedIterator(statesList.iterator())),
                statesList.getFirst().unassigned
            );
        }

        @Override
        public double relaxEdge(AggregateState from, AggregateState to, AggregateState merged, Decision d, double cost) {
            return relax.relaxEdge(from.state, to.state, merged.state, d, cost);
        }
    }


    /**
     * Wrapper that uses the dominance for the initial problem
     */
    private class AggregateDominanceChecker extends DominanceChecker<AggregateState, Integer> {
        protected AggregateDominanceChecker() {
            super(new DefaultDominance<>());
        }

        @Override
        public boolean updateDominance(AggregateState state, int depth, double objValue) {
            return checker.updateDominance(state.state, depth, objValue);
        }
    }


    /**
     * Ranking to use to solve the initial problem
     */
    public enum RankingStrategy {
        SPECIFIED_ONLY, // Use only the specified ranking
        AGGREGATE_ONLY, // Use only the aggregated problem
        AGGREGATE_THEN_SPECIFIED, // Use the aggregated problem, then the specified ranking as tie-breaker
        SPECIFIED_THEN_AGGREGATE // Use the specified ranking, then the aggregated problem as a tie-breaker
    }


    /**
     * Creates a fully qualified instance
     *
     * @param problem   The problem we want to maximize.
     * @param aggregate The mapping to the aggregated problem
     * @param relax     A suitable relaxation for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking   A heuristic to identify the most promising nodes.
     * @param strategy  How to combine the specified ranking with the ranking based on the aggregated problem
     * @param width     A heuristic to choose the maximum width of the DD you compile.
     * @param frontier  The set of nodes that must still be explored before
     *                  the problem can be considered 'solved'.
     * @param fub       The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param dominance The dominance object that will be used to prune the search space.
     */
    public AggregateSolver(
        Problem<T> problem,
        Aggregate<TAgg, KAgg> aggregate,
        Relaxation<T> relax,
        VariableHeuristic<T> varh,
        StateRanking<T> ranking,
        RankingStrategy strategy,
        WidthHeuristic<T> width,
        CutSetType frontier,
        FastUpperBound<T> fub,
        DominanceChecker<T, K> dominance,
        int timeLimit,
        double gapLimit
    ) {
        this.problem = problem;
        this.aggregated = aggregate.getProblem();
        this.mapping = aggregate;
        this.relax = relax;
        this.checker = dominance;
        StateRanking<AggregateState> combinedRanking = combineRanking(ranking, strategy);
        solver = new SequentialSolver<>(
            new AggregateProblem(),
            new AggregateRelax(),
            (variables, states) -> varh.nextVariable(variables, new StateIterator(states)),
            combinedRanking,
            (state) -> width.maximumWidth(state.state),
            new SimpleFrontier<>(combinedRanking, frontier),
            (state, variables) -> Math.min(fub.fastUpperBound(state.state, variables), aggregateUpperBound(state)),
            new AggregateDominanceChecker(),
            timeLimit,
            gapLimit
        );
        preComputed = new HashMap[aggregated.problem.nbVars() + 1];
        for (int i = 0; i < aggregated.problem.nbVars() + 1; i++) preComputed[i] = new HashMap<>();
    }


    @Override
    public SearchStatistics maximize(int verbosityLevel, boolean exportAsDot) {
        return solver.maximize(verbosityLevel, exportAsDot);
    }

    @Override
    public SearchStatistics maximize() {
        return solver.maximize();
    }

    @Override
    public Optional<Double> bestValue() {
        return solver.bestValue();
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return solver.bestSolution();
    }


    /**
     * Combine the specified ranking and the ranking based on the aggregated problem using the specified strategy
     */
    private StateRanking<AggregateState> combineRanking(StateRanking<T> ranking, RankingStrategy strategy) {
        Comparator<AggregateState> specifiedRanking = (state1, state2) -> ranking.compare(state1.state, state2.state);
        Comparator<AggregateState> aggregateRanking = Comparator.comparingDouble(this::aggregateUpperBound).reversed();
        return switch (strategy) {
            case SPECIFIED_ONLY -> specifiedRanking::compare;
            case AGGREGATE_ONLY -> aggregateRanking::compare;
            case AGGREGATE_THEN_SPECIFIED -> aggregateRanking.thenComparing(specifiedRanking)::compare;
            case SPECIFIED_THEN_AGGREGATE -> specifiedRanking.thenComparing(aggregateRanking)::compare;
        };
    }


    /**
     * Get an upper bound for a state using the aggregated problem
     */
    private double aggregateUpperBound(AggregateState state) {
        HashMap<TAgg, Double> layer = preComputed[state.unassigned.size()];
        if (!layer.containsKey(state.aggregated)) {
            double sol = preCompute(state.aggregated, state.unassigned);
            layer.put(state.aggregated, sol);
            return sol;
        }
        return layer.get(state.aggregated);
    }


    /**
     * Pre-compute the cost of the best solution for some nodes in the aggregated problem
     * @param state State to explore
     * @param variables The set of unassigned variables
     * @return Best solution found from this node
     */
    private double preCompute(TAgg state, BitsetSet variables) {
        if (variables.isEmpty()) return 0;

        // Select next variable (we don't have the states in the next layer but some problems may require a specific order for variables)
        int var = aggregated.varh.nextVariable(variables, Collections.singleton(state).iterator());
        variables.remove(var);
        HashMap<TAgg, Double> layer = preComputed[variables.size()];

        // Find children
        double bestSol = Double.NEGATIVE_INFINITY;
        ArrayList<AggregateNode> nodes = new ArrayList<>();
        Iterator<Integer> values = aggregated.problem.domain(state, var);
        while (values.hasNext()) {
            int val = values.next();
            Decision decision = new Decision(var, val);
            TAgg child = aggregated.problem.transition(state, decision);
            double cost = aggregated.problem.transitionCost(state, decision);

            Double sol = layer.get(child);
            if (sol != null) { // Already visited
                if (sol + cost > bestSol) {
                    bestSol = sol + cost;
                }
            }
            else { // Add to explore
                double fub = aggregated.fub.fastUpperBound(child, variables);
                if (cost + fub > bestSol) {
                    nodes.add(new AggregateNode(child, cost, fub));
                }
            }
        }
        nodes.sort(
            Comparator.comparingDouble((AggregateNode n) -> -n.cost)
            .thenComparing((n1, n2) -> aggregated.ranking.compare(n1.state, n2.state))
        );

        // Explore children
        for (AggregateNode node : nodes) {
            if (node.cost + node.fub > bestSol) {
                double sol = preCompute(node.state, variables);
                layer.put(node.state, sol);
                if (node.cost + sol > bestSol) {
                    bestSol = sol + node.cost;
                }
            }
        }

        // Return best result
        variables.add(var);
        return bestSol;
    }

    private class AggregateNode {
        public final TAgg state;
        public final double cost;
        public final double fub;

        public AggregateNode(TAgg state, double cost, double fub) {
            this.state = state;
            this.cost = cost;
            this.fub = fub;
        }
    }
}