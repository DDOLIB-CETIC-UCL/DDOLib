package org.ddolib.ddo.examples.TSPSmallestAdjacentHopsIncremental;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.io.IOException;
import java.util.*;

public final class TSPIncrementalAdjacentEdge {

    static class TSPState{

        BitSet toVisit;
        int current = -1; //We might not know where we are in case of merge
        BitSet currentSet;
        SmallestIncidentHopIncremental heuristics;

        public TSPState(int current, BitSet toVisit, SmallestIncidentHopIncremental heuristics){
            this.toVisit = toVisit;
            this.current = current;
            this.heuristics = heuristics;
        }

        public TSPState(BitSet currentSet, BitSet toVisit, SmallestIncidentHopIncremental heuristics){
            this.toVisit = toVisit;
            this.currentSet = currentSet;
            this.heuristics = heuristics;

            //System.out.println(this);
        }
        public TSPState goTo(int node){
            BitSet newToVisit = (BitSet) toVisit.clone();
            newToVisit.clear(node);

            return new TSPState(node, newToVisit,
                    heuristics);
        }

        public int getHeuristics(int nbHops, SortedAdjacents sortedAdjacents){
            toVisit.set(current);
            heuristics.updateToRestrictedNodeSet(toVisit,sortedAdjacents);
            toVisit.clear(current);

            return heuristics.computeHeuristics(sortedAdjacents,nbHops);
        }

        @Override
        public String toString() {
            if(current == -1){
                return "TSPState(possibleCurrent:" + currentSet + " toVisit:" + toVisit + ")";
            }else{
                return "TSPState(current:" + current + " toVisit:" + toVisit + ")";
            }
        }
    }

    private static class TSP implements Problem<TSPState> {
        final int   n;
        final int[][] distanceMatrix;
        SortedAdjacents sortedAdjacents;
        @Override
        public String toString() {
            return "TSP(n:" + n + "\n" +
                    Arrays.stream(distanceMatrix).map(l -> Arrays.toString(l)+"\n").toList();
        }

        public int eval(int[] solution){
            int toReturn = 0;
            for(int i = 1 ; i < solution.length ; i ++){
                toReturn = toReturn + distanceMatrix[solution[i-1]][solution[i]];
            }
            return toReturn;
        }

        public TSP(final int[][] distanceMatrix) {
            this.distanceMatrix = distanceMatrix;
            this.n = distanceMatrix.length;
            this.sortedAdjacents = new SortedAdjacents(distanceMatrix);
        }

        @Override
        public int nbVars() {
            return n-1; //since zero is the initial point
        }

        @Override
        public TSPState initialState() {
            System.out.println("init");
            BitSet toVisit = new BitSet(n);
            toVisit.set(1,n);

            return new TSPState(0, toVisit, sortedAdjacents.initialHeuristics());
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(TSPState state, int var) {
            ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
            return  domain.iterator();
        }

        @Override
        public TSPState transition(TSPState state, Decision decision) {
            return state.goTo(decision.val());
        }

        @Override
        public int transitionCost(TSPState state, Decision decision) {
            if(state.current == -1) {
                //we can be anywhere in the currentStates, so we take the max
                return - state.currentSet.stream()
                        .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                        .map(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                        .min()
                        .getAsInt();
            }else{
                return -distanceMatrix[state.current][decision.val()];
            }
        }
    }

    private static class TSPRelax implements Relaxation<TSPState> {
        private final TSP problem;

        public TSPRelax(TSP problem) {
            this.problem = problem;
        }

        @Override
        public TSPState mergeStates(final Iterator<TSPState> states) {
            //take the union
            //the current node is normally the same in all states
            BitSet toVisit = new BitSet(problem.n);
            BitSet current = new BitSet(problem.n);
            while (states.hasNext()) {
                TSPState state = states.next();
                toVisit.or(state.toVisit);

                if(state.current != -1) current.set(state.current);
                else current.or(state.currentSet);
            }

            return new TSPState(current,toVisit,problem.sortedAdjacents.initialHeuristics());
        }

        @Override
        public int relaxEdge(TSPState from, TSPState to, TSPState merged, Decision d, int cost) {
            return cost;
        }

        @Override
        public int fastUpperBound(TSPState state, Set<Integer> variables) {
            int nbHopsToDo = variables.size();
            int lb = state.getHeuristics(nbHopsToDo, this.problem.sortedAdjacents);
            return -lb;
        }
    }

    private static void require(Boolean a, String str){
        if(!a) throw new Error(str);
    }

    public static class TSPRanking implements StateRanking<TSPState> {
        @Override
        public int compare(final TSPState o1, final TSPState o2) {
            return 0;
        }
    }

    public static TSP genInstance(int n) {

        int[] x = new int[n];
        int[] y = new int[n];
        Random r = new Random(1);
        for(int i = 0 ; i < n ;  i++){
            x[i] = r.nextInt(100);
            y[i] = r.nextInt(100);
        }

        int[][] distance = new int[n][];
        for(int i = 0 ; i < n ;  i++){
            distance[i] = new int[n];
            for(int j = 0 ; j < n ;  j++){
                distance[i][j] = dist(x[i] - x[j] , y[i]-y[j]);
            }
        }
        return new TSP(distance);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) throws IOException {

        final TSP problem = genInstance(15);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());
        solveTsp(problem);
        System.out.println("end");
    }

    public static void solveTsp(TSP problem){

        final TSPRelax                    relax = new TSPRelax(problem);
        final TSPRanking                ranking = new TSPRanking();
        final FixedWidth<TSPState> width = new FixedWidth<>(500);
        final DefaultVariableHeuristic varh = new DefaultVariableHeuristic();

        final Frontier<TSPState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new ParallelSolver<>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] route = new int[problem.nbVars()+1];
                    route[0] = 0;
                    for (Decision d : decisions) {
                        route[d.var()+1] = d.val();
                    }
                    return route;
                })
                .get();


        System.out.printf("Duration : %.3f%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}



