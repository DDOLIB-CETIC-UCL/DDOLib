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

import static java.lang.Math.max;
import static java.lang.Math.min;

public final class DPDCapacityKruskal {

    static class PDState{

        //we use a random tiebreak when selecting nodes that will be merged.
        //the random tiebreak is based on this random value to ensure transitivity, antisymmetry, reflexivity
        public int random = new Random().nextInt(100);

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
        int minContent;
        int maxContent;

        public PDState(BitSet current, BitSet openToVisit, BitSet allToVisit, int minContent, int maxContent) {
            this.openToVisit = openToVisit;
            this.allToVisit = allToVisit;
            this.current = current;
            this.minContent = minContent;
            this.maxContent = maxContent;
        }

        public int hashCode() {
            return Objects.hash(openToVisit, allToVisit, current);
        }

        @Override
        public boolean equals(Object obj) {
            PDState that = (PDState) obj;
            if(this.minContent != that.minContent) return false;
            if(this.maxContent != that.maxContent) return false;
            if(!this.current.equals(that.current)) return false;
            if(!this.openToVisit.equals(that.openToVisit)) return false;
            return (this.allToVisit.equals(that.allToVisit));
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
            return "PDState(current:" + current + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + " content:[" + minContent + ";" + maxContent + "])";
        }
    }

    private static class PDProblem implements Problem<PDState> {
        final int   n;
        final int[][] distanceMatrix;

        final int maxCapacity;
        HashMap<Integer,Integer> pickupToAssociatedDelivery;
        HashMap<Integer,Integer> deliveryToAssociatedPickup;
        HashMap<Integer,Integer> pickupToSize;
        HashMap<Integer,Integer> deliveryToSize;

        Set<Integer> unrelatedNodes;

        @Override
        public String toString() {
            return "PDProblem(n:" + n + "\n" +
                    "pdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> "(" + p + "->" + pickupToAssociatedDelivery.get(p) + ":" + pickupToSize.get(p) + ")").toList() + ")\n" +
                    "unrelated:" + unrelatedNodes.stream().toList() + "\n" +
                    "maxCapacity:" + maxCapacity + "\n" +
                    Arrays.stream(distanceMatrix).map(l -> Arrays.toString(l)+"\n").toList();
        }

        public int eval(int[] solution){
            int toReturn = 0;
            for(int i = 1 ; i < solution.length ; i ++){
                toReturn = toReturn + distanceMatrix[solution[i-1]][solution[i]];
            }
            return toReturn;
        }

        public PDProblem(final int[][] distanceMatrix, HashMap<Integer,Integer> pickupToAssociatedDelivery, HashMap<Integer,Integer> pickupToSize, int maxCapacity) {
            this.distanceMatrix = distanceMatrix;
            this.n = distanceMatrix.length;
            this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
            this.unrelatedNodes = new HashSet<Integer>(IntStream.range(0,n).boxed().toList());
            this.maxCapacity = maxCapacity;
            this.pickupToSize = pickupToSize;

            deliveryToAssociatedPickup = new HashMap<>();
            deliveryToSize = new HashMap<>();
            for(int p : pickupToAssociatedDelivery.keySet()) {
                int d = pickupToAssociatedDelivery.get(p);
                unrelatedNodes.remove(p);
                unrelatedNodes.remove(d);
                deliveryToAssociatedPickup.put(d,p);
                deliveryToSize.put(d,pickupToSize.get(p));
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

            return new PDState(singleton(0), openToVisit ,allToVisit, 0,0);
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
            //obviously these should be equal except if a fusion occurred

            return new ArrayList<Integer>(state.openToVisit.stream().boxed().filter(node -> {
                if (pickupToAssociatedDelivery.containsKey(node)) {
                    //it is a pickup node, only admissible if capacity might allow for it
                    int size = pickupToSize.get(node);
                    return state.minContent + size <= maxCapacity;
                }else if(deliveryToAssociatedPickup.containsKey(node)) {
                    //it is a delivery, and there might be imprecision about it
                    //so we only consider this node if it is coherent with the capacity
                    int size = deliveryToSize.get(node);
                    return state.maxContent - size >= 0;
                }else{
                    //it is a regular node, so always allowed
                    return true;
                }
            }).toList()).iterator();
        }

        @Override
        public PDState transition(PDState state, Decision decision) {
            int node = decision.val();
            BitSet newOpenToVisit = (BitSet) state.openToVisit.clone();
            newOpenToVisit.clear(node);

            BitSet newAllToVisit = (BitSet) state.allToVisit.clone();
            newAllToVisit.clear(node);

            if(pickupToAssociatedDelivery.containsKey(node)){
                //go to a pick-up node
                //the associated delivery is not open to visit
                newOpenToVisit.set(pickupToAssociatedDelivery.get(node));

                int size = pickupToSize.get(node);
                //the content is increased by 1; since there is an uncertainty, overflowing values are dropped
                int newMinContent = state.minContent + size;
                int newMaxContent = Math.min(maxCapacity, state.maxContent + size);

                assert(newMinContent <= maxCapacity);

                return new PDState(
                        state.singleton(node),
                        newOpenToVisit,
                        newAllToVisit,
                        newMinContent,
                        newMaxContent);
            }else if(deliveryToAssociatedPickup.containsKey(node)){
                //go to a delivery node
                int size = deliveryToSize.get(node);

                //the content is decreased by 1; since there is an uncertainty, underflowing values are dropped
                int newMinContent = Math.max(0, state.minContent - size);
                int newMaxContent = state.maxContent - size;
                assert(newMaxContent >= 0);

                //since there is uncertainty on openToVisit,
                // the associated pickup might be in openToVisit
                // we use the opportunity here to prune openToVisit
                int p = deliveryToAssociatedPickup.get(node);
                if(newOpenToVisit.get(p)){
                    newOpenToVisit.clear(p);
                }

                return new PDState(
                        state.singleton(node),
                        newOpenToVisit,
                        newAllToVisit,
                        newMinContent,
                        newMaxContent);
            }else{
                //it is another node, neither pickup nor delivery

                return new PDState(
                        state.singleton(node),
                        newOpenToVisit,
                        newAllToVisit,
                        state.minContent,
                        state.maxContent);
            }
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
            int newMinContent = Integer.MAX_VALUE;
            int newMaxContent = Integer.MIN_VALUE;

            while (states.hasNext()) {
                PDState state = states.next();
                //take the union; loose precision here
                openToVisit.or(state.openToVisit);
                allToVisit.or(state.allToVisit);
                current.or(state.current);
                newMaxContent = Math.max(newMaxContent, state.maxContent);
                newMinContent = Math.min(newMinContent, state.minContent);
            }

            return new PDState(current,openToVisit,allToVisit,newMinContent,newMaxContent);
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
            return (o1.random - o2.random);
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
        HashMap<Integer,Integer> pickupToSize = new HashMap<Integer,Integer>();

        int firstDelivery = (n-unrelated-1)/2+1;
        for(int p = 1; p < firstDelivery ; p ++){
            int d = firstDelivery + p - 1;
            pickupToAssociatedDelivery.put(p,d);
            pickupToSize.put(p, 1+r.nextInt(maxCapacity-1));
        }

        return new PDProblem(distance,pickupToAssociatedDelivery, pickupToSize, maxCapacity);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) throws IOException {

        final PDProblem problem = genInstance(20,5, 4);

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

        PDPSolution s = new PDPSolution(problem,solution);
        System.out.printf("Duration : %.3f%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
        System.out.println(s);
        System.out.println("problem:" + problem);
    }

    static class PDPSolution{
        PDProblem problem;
        int[] solution;

        public PDPSolution(PDProblem problem, int[] solution){
            this.problem = problem;
            this.solution = solution;
        }

        @Override
        public String toString() {

            StringBuilder toReturn = new StringBuilder("0\tcontent:" + 0);
            int currentNode = 0;
            int currentContent = 0;
            for(int i = 1 ; i < solution.length ; i++){
                currentNode = solution[i];
                if(problem.deliveryToAssociatedPickup.containsKey(currentNode)){
                    //it is a delivery
                    currentContent = currentContent-problem.deliveryToSize.get(currentNode);
                    toReturn.append("\n" + currentNode + " \tcontent:" + currentContent + "\t(delivery from " + problem.deliveryToAssociatedPickup.get(currentNode) + " -" + problem.deliveryToSize.get(currentNode) +")");
                }else if (problem.pickupToAssociatedDelivery.containsKey(currentNode)){
                    // it is a pickup
                    currentContent = currentContent+problem.pickupToSize.get(currentNode);
                    toReturn.append("\n" + currentNode + "\tcontent:" + currentContent +   "\t(pickup to " + problem.pickupToAssociatedDelivery.get(currentNode) + " +" + problem.pickupToSize.get(currentNode) + ")");
                }else{
                    //an unrelated node
                    toReturn.append("\n" + currentNode + "\tcontent:" + currentContent);
                }
            }
            return toReturn.toString();
        }
    }
}



