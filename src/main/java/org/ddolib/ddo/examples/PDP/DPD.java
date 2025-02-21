package org.ddolib.ddo.examples.PDP;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public final class DPD {

    static class PDState{

        BitSet toVisit; //principe: toVisit contient tout ce qu'on doit encore visiter
        int current = -1; //We might not know where we are in case of merge
        BitSet currentSet;

        public PDState(int current, BitSet toVisit){
            this.toVisit = toVisit;
            this.current = current;
        }

        public PDState(BitSet currentSet, BitSet toVisit){
            this.toVisit = toVisit;
            this.currentSet = currentSet;
        }

        public PDState goTo(int node, PDProblem problem){
            BitSet newToVisit = (BitSet) toVisit.clone();
            newToVisit.clear(node);

            if(problem.pickupToAssociatedDelivery.containsKey(node)){
                newToVisit.set(problem.pickupToAssociatedDelivery.get(node));
            }

            /*good idea, but wrong idea
            if(problem.deliveryToAssociatedPickup.containsKey(node) ){
                int p = problem.deliveryToAssociatedPickup.get(node);
                if(newToVisit.get(p)){
                    System.out.println("Pruning toVisitPickups " + p);
                    newToVisit.clear(p);
                }
            }*/

            PDState next = new PDState(node, newToVisit);
            //System.out.println("goto(from:" + this + " step:" + node+ ")=" + next);
            return next;
        }

        @Override
        public String toString() {
            if(current == -1){
                return "PDState(possibleCurrent:" + currentSet + " toVisit:" + toVisit + ")";
            }else{
                return "PDState(current:" + current + " toVisit:" + toVisit + ")";
            }
        }
    }

    private static class PDProblem implements Problem<PDState> {
        final int   n;
        final int[][] distanceMatrix;

        HashMap<Integer,Integer> pickupToAssociatedDelivery;
        HashMap<Integer,Integer> deliveryToAssociatedPickup;

        Set<Integer> unrelatedNodes;

        @Override
        public String toString() {
            return "PDProblem(n:" + n + "\n" +
                    "pdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> "(" + p + "->" + pickupToAssociatedDelivery.get(p) + ")").toList() + ")\n" +
                    "unrelated:" + unrelatedNodes.stream().toList() + "\n" +
                    Arrays.stream(distanceMatrix).map(l -> Arrays.toString(l)+"\n").toList();
        }

        public int eval(int[] solution){
            int toReturn = 0;
            for(int i = 1 ; i < solution.length ; i ++){
                toReturn = toReturn + distanceMatrix[solution[i-1]][solution[i]];
            }
            return toReturn;
        }

        public PDProblem(final int[][] distanceMatrix, HashMap<Integer,Integer> pickupToAssociatedDelivery) {
            this.distanceMatrix = distanceMatrix;
            this.n = distanceMatrix.length;
            this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
            this.unrelatedNodes = new HashSet<Integer>(IntStream.range(0,n).boxed().toList());

            deliveryToAssociatedPickup = new HashMap<>();
            for(int p : pickupToAssociatedDelivery.keySet()) {
                int d = pickupToAssociatedDelivery.get(p);
                unrelatedNodes.remove(p);
                unrelatedNodes.remove(d);
                deliveryToAssociatedPickup.put(d,p);
            }
        }

        @Override
        public int nbVars() {
            return n-1; //since zero is the initial point
        }

        @Override
        public PDState initialState() {
            System.out.println("init");
            BitSet toVisit = new BitSet(n);
            toVisit.set(1,n);

            for(int p : pickupToAssociatedDelivery.keySet()) {
                toVisit.clear(pickupToAssociatedDelivery.get(p));
            }

            return new PDState(0, toVisit);
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(PDState state, int var) {
            ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
            return  domain.iterator();
        }

        @Override
        public PDState transition(PDState state, Decision decision) {
            return state.goTo(decision.val(),this);
        }

        @Override
        public int transitionCost(PDState state, Decision decision) {
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

    private static class TSPRelax implements Relaxation<PDState> {
        private final PDProblem problem;

        public TSPRelax(PDProblem problem) {
            this.problem = problem;
        }

        @Override
        public PDState mergeStates(final Iterator<PDState> states) {
            //take the union
            //the current node is normally the same in all states

            //TODO problème en prenant l'union, on va ajouter des noeuds qui sont peut-être unreachable dans certains cas.

            BitSet toVisit = new BitSet(problem.n);
            BitSet current = new BitSet(problem.n);
            while (states.hasNext()) {
                PDState state = states.next();
                toVisit.or(state.toVisit);

                if(state.current != -1) current.set(state.current);
                else current.or(state.currentSet);
            }
            PDState merged = new PDState(current,toVisit);
            //System.out.println("merged:" + merged);
            return merged;
        }

        @Override
        public int relaxEdge(PDState from, PDState to, PDState merged, Decision d, int cost) {
            return cost;
        }

        @Override
        public int fastUpperBound(PDState state, Set<Integer> variables) {
            return Integer.MAX_VALUE;
        }
    }

    private static void require(Boolean a, String str){
        if(!a) throw new Error(str);
    }

    public static class TSPRanking implements StateRanking<PDState> {
        @Override
        public int compare(final PDState o1, final PDState o2) {
            return 0;
        }
    }

    public static PDProblem genInstance(int n,int unrelated) {

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

        HashMap<Integer,Integer> pickupToAssociatedDelivery = new HashMap<Integer,Integer>();

        int firstDelivery = (n-unrelated-1)/2+1; //for fun, some are not pdp nodes
        //    /2 rounds to lower
        for(int p = 1; p < firstDelivery ; p ++){
            int d = firstDelivery + p - 1;
            pickupToAssociatedDelivery.put(p,d);
        }

        return new PDProblem(distance,pickupToAssociatedDelivery);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) throws IOException {

        final PDProblem problem = genInstance(15,0);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());

        solveDPD(problem);
        System.out.println("end");
    }

    public static void solveDPD(PDProblem problem){

        final TSPRelax                    relax = new TSPRelax(problem);
        final TSPRanking                ranking = new TSPRanking();
        final FixedWidth<PDState> width = new FixedWidth<>(2000);
        final DefaultVariableHeuristic varh = new DefaultVariableHeuristic();

        final Frontier<PDState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>( //ParallelSolver<>(Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize(1);
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
        System.out.println("problem:" + problem);
    }
}



