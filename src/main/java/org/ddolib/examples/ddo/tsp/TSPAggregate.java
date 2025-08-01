package org.ddolib.examples.ddo.tsp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Aggregate;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.SolverInput;

import java.util.*;


public class TSPAggregate implements Aggregate<TSPAggregateState, Integer> {
    private final TSPProblem problem;
    private SolverInput<TSPAggregateState, Integer> input;
    private TSPProblem aggregatedProblem;
    private TSPRelax aggregatedRelax;
    private TSPFastUpperBound aggregatedFub;
    private int[] map; // Node in the initial problem -> node in the aggregated problem
    private int[] nToVisit; // Number of nodes in the initial problem corresponding to each node in the aggregated problem

    private final int N = 12;

    public TSPAggregate(TSPProblem problem) {
        this.problem = problem;
        aggregateProblem();
    }


    @Override
    public SolverInput<TSPAggregateState, Integer> getProblem() {
        return input;
    }


    @Override
    public TSPAggregateState aggregateTransition(TSPAggregateState state, Decision decision) {
        int[] nToVisit = Arrays.copyOf(state.nToVisit, N);
        int aggregatedVal = map[decision.val()];
        nToVisit[aggregatedVal]--;
        BitSet toVisit = (BitSet)state.state.toVisit.clone();
        if (nToVisit[aggregatedVal] == 0) { // Aggregated node doesn't need to be visited anymore
            toVisit.clear(aggregatedVal);
        }
        return new TSPAggregateState(new TSPState(state.state.singleton(aggregatedVal), toVisit), nToVisit);
    }


    @Override
    public int assignedVariable(TSPAggregateState state, Decision decision, Set<Integer> variables) {
        if (state.nToVisit[map[decision.val()]] == 1) return N - variables.size();
        return -1;
    }


    private void aggregateProblem() {
        double[][] distances = new double[problem.n][];
        for (int i = 0; i < problem.n; i++) {
            distances[i] = Arrays.copyOf(problem.distanceMatrix[i], problem.n);
        }
        int[] merged = new int[problem.n];
        Arrays.fill(merged, -1);
        int currentN = problem.n;

        // Merge nodes together until required number of nodes
        while (currentN > N) {
            // Find 2 nodes as close as possible to merge
            double minDist = Integer.MAX_VALUE;
            int minNode1 = 0, minNode2 = 0;
            for (int i = 0; i < problem.n; i++) {
                if (merged[i] != -1) continue;
                for (int j = 0; j < problem.n; j++) {
                    if (merged[j] != -1 || i == j) continue;
                    double dist = distances[i][j];
                    if (dist < minDist) {
                        minDist = dist;
                        minNode1 = i;
                        minNode2 = j;
                    }
                }
            }

            // Merge node 2 into node 1
            merged[minNode1] = minNode2;
            distances[minNode1][minNode2] = 0;
            distances[minNode2][minNode1] = 0;
            for (int i = 0; i < problem.n; i++) {
                if (merged[i] == -1 && i != minNode1 && i != minNode2) {
                    double newDist = Math.min(distances[minNode1][i], distances[minNode2][i]);
                    distances[minNode1][i] = distances[i][minNode1] = newDist;
                    distances[minNode2][i] = distances[i][minNode2] = newDist;
                }
            }
            currentN--;
        }

        // Create new problem
        double[][] aggregatedDistances = new double[N][N];
        map = new int[problem.n];
        nToVisit = new int[N];
        int i = 0;
        for (int j = 0; j < problem.n; j++) {
            if (merged[j] != -1) continue;
            for (int k = 0; k < j; k++) {
                if (merged[k] != -1) continue;
                aggregatedDistances[i][map[k]] = aggregatedDistances[map[k]][i] = distances[j][k];
            }
            nToVisit[i] = 1;
            map[j] = i++;
        }
        for (int j = 0; j < problem.n; j++) {
            if (merged[j] == -1) continue;
            int index = j, parent = merged[j];
            while (parent != -1) {
                index = parent;
                parent = merged[parent];
            }
            nToVisit[map[index]]++;
            map[j] = map[index];
        }

        aggregatedProblem = new TSPProblem(aggregatedDistances);
        aggregatedRelax = new TSPRelax(aggregatedProblem);
        aggregatedFub = new TSPFastUpperBound(aggregatedProblem);
        input = SolverInput.defaultInput(new TSPAggregateProblem(), new TSPAggregateRelax());
        input.fub = (s, v) -> aggregatedFub.fastUpperBound(s.state, v);
    }


    private class TSPAggregateProblem implements Problem<TSPAggregateState> {
        @Override
        public int nbVars() {
            return N;
        }

        @Override
        public TSPAggregateState initialState() {
            return new TSPAggregateState(aggregatedProblem.initialState(), nToVisit);
        }

        @Override
        public double initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(TSPAggregateState state, int var) {
            return aggregatedProblem.domain(state.state, var);
        }

        @Override
        public TSPAggregateState transition(TSPAggregateState state, Decision decision) {
            return new TSPAggregateState(aggregatedProblem.transition(state.state, decision), state.nToVisit);
        }

        @Override
        public double transitionCost(TSPAggregateState state, Decision decision) {
            return -state.state.current.stream()
                    .mapToDouble(possibleCurrentNode -> aggregatedProblem.distanceMatrix[possibleCurrentNode][decision.val()])
                    .min()
                    .getAsDouble();
        }
    }


    private class TSPAggregateRelax implements Relaxation<TSPAggregateState> {
        @Override
        public TSPAggregateState mergeStates(Iterator<TSPAggregateState> states) {
            ArrayList<TSPAggregateState> statesList = new ArrayList<>();
            while (states.hasNext()) statesList.add(states.next());
            TSPState state = aggregatedRelax.mergeStates(statesList.stream().map(s -> s.state).iterator());
            int[] nToVisit = new int[N];
            for (int i = 0; i < problem.n; i++) { // Rebuild nToVisit
                if (state.toVisit.get(i)) nToVisit[map[i]]++;
            }
            return new TSPAggregateState(state, nToVisit);
        }

        @Override
        public double relaxEdge(TSPAggregateState from, TSPAggregateState to, TSPAggregateState merged, Decision d, double cost) {
            return cost;
        }
    }
}