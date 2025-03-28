package org.ddolib.ddo.examples.PDPCapacityKruskal;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.examples.TSPKruskal.Kruskal;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public final class DPDCapacityKruskal {

    static class PDState{

        //the nodes that we can visit, including
        // all non-visited pick-up nodes
        // all non-visited  delivery nodes such that the related pick-up has been reached
        BitSet openToVisit;

        //every node that has not been visited yet
        BitSet allToVisit;

        //the current node. It is a set because in case of a fusion, we must take the union.
        // However, most of the time, it is a singleton
        BitSet current;

        //the current content; ie the number of passed pickups such that we did not yet reach the associated delivery
        BitSet currentContent;

        public PDState(BitSet current, BitSet openToVisit, BitSet allToVisit, BitSet currentContent) {
            this.openToVisit = openToVisit;
            this.allToVisit = allToVisit;
            this.current = current;
            this.currentContent = currentContent;
        }

        public int hashCode() {
            return Objects.hash(openToVisit, allToVisit, current);
        }

        @Override
        public boolean equals(Object obj) {
            PDState that = (PDState) obj;
            if(!that.current.equals(this.current)) return false;
            if(!that.openToVisit.equals(this.openToVisit)) return false;
            if(!that.currentContent.equals(this.currentContent)) return false;
            return (that.allToVisit.equals(this.allToVisit));
        }

        public PDState goTo(int node, PDProblem problem){
            BitSet newOpenToVisit = (BitSet) openToVisit.clone();
            newOpenToVisit.clear(node);

            BitSet newAllToVisit = (BitSet) allToVisit.clone();
            newAllToVisit.clear(node);

            BitSet newContent;

            if(problem.pickupToAssociatedDelivery.containsKey(node)){
                //it is a pick-up node
                newOpenToVisit.set(problem.pickupToAssociatedDelivery.get(node));
                newContent = new BitSet();
                for(int possibleContent: currentContent.stream().boxed().toList()){
                    newContent.set(possibleContent+1);
                }
            }else if(problem.deliveryToAssociatedPickup.containsKey(node)){
                //it is a delivery node
                newContent = new BitSet();
                for(int possibleContent: currentContent.stream().boxed().toList()){
                    if(possibleContent > 0) newContent.set(possibleContent-1);
                }
            }else{
                newContent = (BitSet) currentContent.clone();
            }

            //good idea, but wrong idea
            if(problem.deliveryToAssociatedPickup.containsKey(node) ){
                int p = problem.deliveryToAssociatedPickup.get(node);
                if(newOpenToVisit.get(p)){
                    //System.out.println("Pruning toVisitPickups " + p);
                    newOpenToVisit.clear(p);
                }
            }

            //System.out.println("goto(from:" + this + " step:" + node+ ")=" + next);
            return new PDState(
                    singleton(node),
                    newOpenToVisit,
                    newAllToVisit,
                    newContent);
        }

        public BitSet singleton(int singletonValue){
            BitSet toReturn = new BitSet(singletonValue+1);
            toReturn.set(singletonValue);
            return toReturn;
        }

        @Override
        public String toString() {
            BitSet closedToVisit = (BitSet) allToVisit.clone();
            closedToVisit.xor(openToVisit);
            if(current.cardinality() != 1){
                return "PDState(possibleCurrent:" + current + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + " content:" + currentContent + ")";
            }else{
                return "PDState(current:" + current.nextSetBit(0) + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit  + " content:" + currentContent + ")";
            }
        }
    }

    private static class PDProblem implements Problem<PDState> {
        final int   n;
        final int[][] distanceMatrix;

        final int maxCapacity;
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

        public PDProblem(final int[][] distanceMatrix, HashMap<Integer,Integer> pickupToAssociatedDelivery, int maxCapacity) {
            this.distanceMatrix = distanceMatrix;
            this.n = distanceMatrix.length;
            this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
            this.unrelatedNodes = new HashSet<Integer>(IntStream.range(0,n).boxed().toList());
            this.maxCapacity = maxCapacity;

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
            BitSet openToVisit = new BitSet(n);
            openToVisit.set(1,n);

            for(int p : pickupToAssociatedDelivery.keySet()) {
                openToVisit.clear(pickupToAssociatedDelivery.get(p));
            }

            BitSet allToVisit = new BitSet(n);
            allToVisit.set(1,n);

            return new PDState(singleton(0), openToVisit ,allToVisit, singleton(0));
        }

        public BitSet singleton(int singletonValue){
            BitSet toReturn = new BitSet(n);
            toReturn.set(singletonValue);
            return toReturn;
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(PDState state, int var) {
            //remaining capacity
            int minContent = state.currentContent.nextSetBit(0);
            int maxContent = state.currentContent.previousSetBit(n);
            if(minContent >= maxCapacity) {
                //only the open nodes that are not pick-ups nodes, so deliveries or unrelated
                return new ArrayList<>(state.openToVisit.stream().boxed().filter(node -> !pickupToAssociatedDelivery.containsKey(node)).toList()).iterator();
            }else if (maxContent == 0){
                //only the open nodes that are not delivery nodes, so deliveries or unrelated
                return new ArrayList<>(state.openToVisit.stream().boxed().filter(node -> !deliveryToAssociatedPickup.containsKey(node)).toList()).iterator();
            }else{
                //any that is open to visit
                return new ArrayList<>(state.openToVisit.stream().boxed().toList()).iterator();
            }
        }

        @Override
        public PDState transition(PDState state, Decision decision) {
            return state.goTo(decision.val(),this);
        }

        @Override
        public int transitionCost(PDState state, Decision decision) {
            return - state.current.stream()
                    .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                    .map(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                    .min()
                    .getAsInt();
        }
    }

    private static class PDPRelax implements Relaxation<PDState> {
        private final PDProblem problem;

        public PDPRelax(PDProblem problem) {
            this.problem = problem;
        }

        @Override
        public PDState mergeStates(final Iterator<PDState> states) {
            //NB: the current node is normally the same in all states

            BitSet openToVisit = new BitSet(problem.n);
            BitSet current = new BitSet(problem.n);
            BitSet allToVisit = new BitSet(problem.n);
            BitSet content = new BitSet(problem.maxCapacity+1);

            while (states.hasNext()) {
                PDState state = states.next();
                //take the union; loose precision here
                openToVisit.or(state.openToVisit);
                allToVisit.or(state.allToVisit);
                current.or(state.current);
                content.or(state.currentContent);
            }
            return new PDState(current,openToVisit,allToVisit,content);
        }

        @Override
        public int relaxEdge(PDState from, PDState to, PDState merged, Decision d, int cost) {
            return cost;
        }

        @Override
        public int fastUpperBound(PDState state, Set<Integer> variables) {
            //min spanning tree on current U toVisit
            //we can only take the variables.size() first edges, so Kruskal might not run until it ends
            if (state.current.cardinality() != 1) {
                throw new Error("no fast upper bound when no current");
            } else {
                BitSet toConsider = (BitSet) state.allToVisit.clone();
                toConsider.or(state.current);
                int nbStepsToRemain = variables.size();
                Kruskal a = new Kruskal(problem.distanceMatrix, toConsider, nbStepsToRemain);
                int ub = a.minimalSpanningTreeWeight;
                return -ub;
            }
        }
    }

    public static class PDPRanking implements StateRanking<PDState> {
        @Override
        public int compare(final PDState o1, final PDState o2) {
            return 0;
        }
    }

    /**
     * Generates a PDP problem
     * a TSP problem such that
     * nodes are grouped by pair: (pickup node; delivery node)
     * in a pair, the pickup node must be reached before the delivery node
     * the problem can also have "unrelated nodes" that are not involved in such a pair
     *
     * @param n the number of nodes of the PDP problem
     * @param unrelated the number of nodes that are not involved in a pickup-delivery pair.
     *                  there might be one more unrelated node than specified here
     * @return a PDP problem
     */
    public static PDProblem genInstance(int n, int unrelated, int maxCapacity) {

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

        int firstDelivery = (n-unrelated-1)/2+1;
        for(int p = 1; p < firstDelivery ; p ++){
            int d = firstDelivery + p - 1;
            pickupToAssociatedDelivery.put(p,d);
        }

        return new PDProblem(distance,pickupToAssociatedDelivery,maxCapacity);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) throws IOException {

        final PDProblem problem = genInstance(24,1, 3);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());

        solveDPD(problem);
        System.out.println("end");
    }

    public static void solveDPD(PDProblem problem){

        final PDPRelax relax = new PDPRelax(problem);
        final PDPRanking ranking = new PDPRanking();
        final FixedWidth<PDState> width = new FixedWidth<>(3000);
        final DefaultVariableHeuristic varh = new DefaultVariableHeuristic();

        final Frontier<PDState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new ParallelSolver<>(Runtime.getRuntime().availableProcessors(),//new SequentialSolver<>(//
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize(2);
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



