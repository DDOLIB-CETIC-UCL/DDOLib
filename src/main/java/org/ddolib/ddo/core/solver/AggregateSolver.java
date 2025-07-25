package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.examples.ddo.carseq.*;
import org.ddolib.modeling.*;

import java.util.*;

public class AggregateSolver<T, TAgg, K> implements Solver {
    private final Problem<T> problem;
    private final Aggregate<TAgg> aggregate;
    private final Relaxation<T> relax;
    private final FastUpperBound<T> fub;
    private final DominanceChecker<T, K> checker;
    private final SequentialSolver<AggregateState, Integer> solver;
    private final HashMap<TAgg, Double> preComputed = new HashMap<>(); // Pre-computed best solution for each aggregated state

    // Test
    public int testAskedFub = 0;
    public int testBetterFub = 0;
    public HashSet<TAgg> testAskedStates = new HashSet<>();
    public int testPreComputed;

    /**
     * Wrapper that contains the initial and the aggregated states
     */
    private class AggregateState {
        private final T state;
        private final TAgg aggregated;

        public AggregateState(T state, TAgg aggregated) {
            this.state = state;
            this.aggregated = aggregated;
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
            return new AggregateState(problem.initialState(), aggregate.getProblem().initialState());
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
            return new AggregateState(
                problem.transition(state.state, decision),
                aggregate.getProblem().transition(state.aggregated, aggregate.mapDecision(decision))
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
                aggregate.getRelax().mergeStates(new AggregatedIterator(statesList.iterator()))
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
     * Creates a fully qualified instance
     *
     * @param problem   The problem we want to maximize.
     * @param aggregate The mapping to the aggregated problem
     * @param relax     A suitable relaxation for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking   A heuristic to identify the most promising nodes.
     * @param width     A heuristic to choose the maximum width of the DD you compile.
     * @param frontier  The set of nodes that must still be explored before
     *                  the problem can be considered 'solved'.
     * @param fub       The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param dominance The dominance object that will be used to prune the search space.
     */
    public AggregateSolver(
        Problem<T> problem,
        Aggregate<TAgg> aggregate,
        Relaxation<T> relax,
        VariableHeuristic<T> varh,
        StateRanking<T> ranking,
        WidthHeuristic<T> width,
        CutSetType frontier,
        FastUpperBound<T> fub,
        DominanceChecker<T, K> dominance
    ) {
        this.problem = problem;
        this.aggregate = aggregate;
        this.relax = relax;
        this.fub = fub;
        this.checker = dominance;
        StateRanking<AggregateState> aggregateRanking = (state1, state2) -> ranking.compare(state1.state, state2.state);
        solver = new SequentialSolver<>(
            new AggregateProblem(),
            new AggregateRelax(),
            (variables, states) -> varh.nextVariable(variables, new StateIterator(states)),
            aggregateRanking,
            (state) -> width.maximumWidth(state.state),
            new SimpleFrontier<>(aggregateRanking, frontier),
            new AggregateFastUpperBound(),
            new AggregateDominanceChecker()
        );
        preCompute(aggregate.getProblem().initialState(), 0);

        testPreComputed = preComputed.size();
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
     * Fast upper bound for the initial problem combining a given fast upper bound and the aggregated problem
     */
    private class AggregateFastUpperBound implements FastUpperBound<AggregateState> {
        @Override
        public double fastUpperBound(AggregateState state, Set<Integer> variables) {
            double initialBound = fub.fastUpperBound(state.state, variables);
            Double aggregateSol = preComputed.getOrDefault(state.aggregated, Double.POSITIVE_INFINITY);

            testAskedStates.add(state.aggregated);
            testAskedFub++;
            if (aggregateSol < initialBound) testBetterFub++;

            return Math.min(initialBound, aggregateSol);
        }
    }


    /**
     * Pre-compute the cost of the best solution for each node in the aggregated problem
     * @param state State to explore
     * @param var Index of the variable to assign
     * @return Best solution found from this node
     */
    private double preCompute(TAgg state, int var) {
        if (var == aggregate.getProblem().nbVars()) return 0;

        // Check if already visited
        Double sol = preComputed.get(state);
        if (sol != null) return sol;

        // Explore children
        double bestSol = Double.NEGATIVE_INFINITY;
        Iterator<Integer> values = aggregate.getProblem().domain(state, var);
        while (values.hasNext()) {
            int val = values.next();
            Decision decision = new Decision(var, val);
            TAgg child = aggregate.getProblem().transition(state, decision);
            double costToChild = aggregate.getProblem().transitionCost(state, decision);
            double childSol = preCompute(child, var + 1);
            if (childSol + costToChild > bestSol) {
                bestSol = childSol + costToChild;
            }
        }

        // Return best result
        preComputed.put(state, bestSol);
        return bestSol;
    }
}