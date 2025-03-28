package org.ddolib.ddo.examples.TSPIncrementalHop;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public final class TSPIncrementalHop {

    static class TSPStateIncrementalBound{

        BitSet toVisit;
        int current = -1; //We might not know where we are in case of merge
        BitSet currentSet;
        EdgeList sortedEdgeListIncidentToToVisitNodesAndCurrentNode;
        int prunedLength = 0;

        public int hashCode() {
            if(current == -1) return Objects.hash(toVisit, currentSet);
            else return Objects.hash(toVisit, current);
        }

        @Override
        public boolean equals(Object obj) {
            TSPStateIncrementalBound that = (TSPStateIncrementalBound) obj;
            if(that.current != this.current) return false;
            if(current == -1){
                if(!that.currentSet.equals(this.currentSet)) return false;
            }
            return that.toVisit.equals(this.toVisit);
        }

        public TSPStateIncrementalBound(int current, BitSet toVisit, EdgeList sortedEdgeListIncidentToToVisitNodesAndCurrentNode){
            this.toVisit = toVisit;
            this.current = current;
            this.sortedEdgeListIncidentToToVisitNodesAndCurrentNode = sortedEdgeListIncidentToToVisitNodesAndCurrentNode;
        }

        public TSPStateIncrementalBound(BitSet currentSet, BitSet toVisit, EdgeList sortedEdgeListIncidentToToVisitNodes){
            this.toVisit = toVisit;
            this.currentSet = currentSet;
            this.sortedEdgeListIncidentToToVisitNodesAndCurrentNode = sortedEdgeListIncidentToToVisitNodes;
            //System.out.println(this);
        }
        public TSPStateIncrementalBound goTo(int node){
            BitSet newToVisit = (BitSet) toVisit.clone();
            newToVisit.clear(node);

            return new TSPStateIncrementalBound(node, newToVisit,
                    sortedEdgeListIncidentToToVisitNodesAndCurrentNode);
        }

        public int getSummedLengthOfNSmallestHops(int nbHops, int[][] distance){
            if(prunedLength < nbHops){
                if(current == -1) throw new Error("no bound for merged");
                toVisit.set(current);
                sortedEdgeListIncidentToToVisitNodesAndCurrentNode =
                        sortedEdgeListIncidentToToVisitNodesAndCurrentNode.filterUpToLength(nbHops, toVisit);
                toVisit.clear(current);
            }
            int total = 0;
            int hopsToDo = nbHops;
            EdgeList current = sortedEdgeListIncidentToToVisitNodesAndCurrentNode;
            while(hopsToDo > 0 && current != null){
                total += distance[current.nodeA][current.nodeB];
                hopsToDo --;
                current = current.next;
            }
            return total;
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

    private static class TSPIncrementalBound implements Problem<TSPStateIncrementalBound> {
        final int   n;
        final int[][] distanceMatrix;
        EdgeList initSortedEdges;
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

        public TSPIncrementalBound(final int[][] distanceMatrix) {
            this.distanceMatrix = distanceMatrix;
            this.n = distanceMatrix.length;

            Iterator<EdgeList> sortedEdges = IntStream.range(1,n).boxed().flatMap(
                    node1 ->
                            IntStream.range(1,n)
                                    .filter(node2 -> node1 > node2)
                                    .boxed()
                                    .map(node2 -> new EdgeList(node1, node2, null))
            ).sorted(Comparator.comparing(e -> distanceMatrix[e.nodeA][e.nodeB])).iterator();

            sortedEdges.hasNext();
            EdgeList current = sortedEdges.next();
            this.initSortedEdges = current;
            while(sortedEdges.hasNext()){
                EdgeList newCurrent = sortedEdges.next();
                current.next = newCurrent;
                current = newCurrent;
            }
        }

        @Override
        public int nbVars() {
            return n-1; //since zero is the initial point
        }

        @Override
        public TSPStateIncrementalBound initialState() {
            System.out.println("init");
            BitSet toVisit = new BitSet(n);
            toVisit.set(1,n);

            return new TSPStateIncrementalBound(0, toVisit, initSortedEdges);
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(TSPStateIncrementalBound state, int var) {
            ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
            return  domain.iterator();
        }

        @Override
        public TSPStateIncrementalBound transition(TSPStateIncrementalBound state, Decision decision) {
            return state.goTo(decision.val());
        }

        @Override
        public int transitionCost(TSPStateIncrementalBound state, Decision decision) {
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

    private static class TSPRelax implements Relaxation<TSPStateIncrementalBound> {
        private final TSPIncrementalBound problem;

        public TSPRelax(TSPIncrementalBound problem) {
            this.problem = problem;
        }

        @Override
        public TSPStateIncrementalBound mergeStates(final Iterator<TSPStateIncrementalBound> states) {
            //take the union
            //the current node is normally the same in all states
            BitSet toVisit = new BitSet(problem.n);
            BitSet current = new BitSet(problem.n);
            while (states.hasNext()) {
                TSPStateIncrementalBound state = states.next();
                toVisit.or(state.toVisit);

                if(state.current != -1) current.set(state.current);
                else current.or(state.currentSet);
            }

            return new TSPStateIncrementalBound(current,toVisit,problem.initSortedEdges);
        }

        @Override
        public int relaxEdge(TSPStateIncrementalBound from, TSPStateIncrementalBound to, TSPStateIncrementalBound merged, Decision d, int cost) {
            return cost;
        }

        @Override
        public int fastUpperBound(TSPStateIncrementalBound state, Set<Integer> variables) {
            int nbHopsToDo = variables.size();
            int lb = state.getSummedLengthOfNSmallestHops(nbHopsToDo,problem.distanceMatrix);
            return -lb;
        }
    }

    private static void require(Boolean a, String str){
        if(!a) throw new Error(str);
    }

    public static class TSPRanking implements StateRanking<TSPStateIncrementalBound> {
        @Override
        public int compare(final TSPStateIncrementalBound o1, final TSPStateIncrementalBound o2) {
            return 0;
        }
    }

    public static TSPIncrementalBound genInstance(int n) {

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
        return new TSPIncrementalBound(distance);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) throws IOException {

        final TSPIncrementalBound problem = genInstance(20);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());
        solveTsp(problem);
        System.out.println("end");
    }

    public static void solveTsp(TSPIncrementalBound problem){

        final TSPRelax                    relax = new TSPRelax(problem);
        final TSPRanking                ranking = new TSPRanking();
        final FixedWidth<TSPStateIncrementalBound> width = new FixedWidth<>(500);
        final DefaultVariableHeuristic varh = new DefaultVariableHeuristic();

        final Frontier<TSPStateIncrementalBound> frontier = new SimpleFrontier<>(ranking);
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



