package org.ddolib.ddo.examples.routing.pdp;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public final class DPDIncrementalHop {

    static class PDState {

        //the nodes that we can visit, including
        // all non-visited pick-up nodes
        // all non-visited  delivery nodes such that the related pick-up has been reached
        BitSet openToVisit;

        //every node that has not been visited yet
        BitSet allToVisit;

        //the current node. It is a set because in case of a fusion, we must take the union.
        // However, most of the time, it is a singleton
        BitSet current;

        //the sorted list of all edges that are incident to the current node and to the allToVisitNodes
        //this list might include more edges
        EdgeList sortedEdgeListIncidentToToVisitNodesAndCurrentNode;

        public PDState(BitSet current, BitSet openToVisit, BitSet allToVisit, EdgeList sortedEdgeListIncidentToToVisitNodesAndCurrentNode) {
            this.openToVisit = openToVisit;
            this.allToVisit = allToVisit;
            this.current = current;
            this.sortedEdgeListIncidentToToVisitNodesAndCurrentNode = sortedEdgeListIncidentToToVisitNodesAndCurrentNode;
        }

        public int hashCode() {
            return Objects.hash(openToVisit, allToVisit, current);
        }

        @Override
        public boolean equals(Object obj) {
            PDState that = (PDState) obj;
            if (!that.current.equals(this.current)) return false;
            if (!that.openToVisit.equals(this.openToVisit)) return false;
            return (that.allToVisit.equals(this.allToVisit));
        }

        public PDState goTo(int node, PDProblem problem) {
            BitSet newOpenToVisit = (BitSet) openToVisit.clone();
            newOpenToVisit.clear(node);

            BitSet newAllToVisit = (BitSet) allToVisit.clone();
            newAllToVisit.clear(node);

            if (problem.pickupToAssociatedDelivery.containsKey(node)) {
                newOpenToVisit.set(problem.pickupToAssociatedDelivery.get(node));
            }

            if (problem.deliveryToAssociatedPickup.containsKey(node)) {
                int p = problem.deliveryToAssociatedPickup.get(node);
                if (newOpenToVisit.get(p)) {
                    newOpenToVisit.clear(p);
                }
            }

            return new PDState(
                    singleton(node),
                    newOpenToVisit,
                    newAllToVisit,
                    sortedEdgeListIncidentToToVisitNodesAndCurrentNode);
        }

        public BitSet singleton(int singletonValue) {
            BitSet toReturn = new BitSet(singletonValue + 1);
            toReturn.set(singletonValue);
            return toReturn;
        }

        public int getSummedLengthOfNSmallestHops(int nbHops, int[][] distance) {

            if (current.cardinality() != 1) throw new Error("no bound for merged");
            int currentNode = current.nextSetBit(0);
            allToVisit.set(currentNode);
            sortedEdgeListIncidentToToVisitNodesAndCurrentNode =
                    sortedEdgeListIncidentToToVisitNodesAndCurrentNode.filterUpToLength(nbHops, allToVisit);
            allToVisit.clear(currentNode);

            int total = 0;
            int hopsToDo = nbHops;
            EdgeList current = sortedEdgeListIncidentToToVisitNodesAndCurrentNode;
            while (hopsToDo > 0 && current != null) {
                total += distance[current.nodeA][current.nodeB];
                hopsToDo--;
                current = current.next;
            }
            return total;
        }

        @Override
        public String toString() {
            BitSet closedToVisit = (BitSet) allToVisit.clone();
            closedToVisit.xor(openToVisit);
            if (current.cardinality() != 1) {
                return "PDState(possibleCurrent:" + current + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + ")";
            } else {
                return "PDState(current:" + current.nextSetBit(0) + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + ")";
            }
        }
    }

    private static class PDProblem implements Problem<PDState> {
        final int n;
        final int[][] distanceMatrix;

        HashMap<Integer, Integer> pickupToAssociatedDelivery;
        HashMap<Integer, Integer> deliveryToAssociatedPickup;

        Set<Integer> unrelatedNodes;
        EdgeList initSortedEdges;

        @Override
        public String toString() {
            return "PDProblem(\n\tn:" + n + "\n" +
                    "\tpdp:" + pickupToAssociatedDelivery.keySet().stream().map(p -> p + "->" + pickupToAssociatedDelivery.get(p)).toList() + "\n" +
                    "\tunrelated:" + unrelatedNodes.stream().toList() + "\n" +
                    "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList();
        }

        public int eval(int[] solution) {
            int toReturn = 0;
            for (int i = 1; i < solution.length; i++) {
                toReturn = toReturn + distanceMatrix[solution[i - 1]][solution[i]];
            }
            return toReturn;
        }

        public PDProblem(final int[][] distanceMatrix, HashMap<Integer, Integer> pickupToAssociatedDelivery) {
            this.distanceMatrix = distanceMatrix;
            this.n = distanceMatrix.length;
            this.pickupToAssociatedDelivery = pickupToAssociatedDelivery;
            this.unrelatedNodes = new HashSet<Integer>(IntStream.range(0, n).boxed().toList());

            deliveryToAssociatedPickup = new HashMap<>();
            for (int p : pickupToAssociatedDelivery.keySet()) {
                int d = pickupToAssociatedDelivery.get(p);
                unrelatedNodes.remove(p);
                unrelatedNodes.remove(d);
                deliveryToAssociatedPickup.put(d, p);
            }

            //TODO: remove all edges that go from delivery to related pickup?
            Iterator<EdgeList> sortedEdges = IntStream.range(1, n).boxed().flatMap(
                    node1 ->
                            IntStream.range(1, n)
                                    .filter(node2 -> node1 > node2)
                                    .boxed()
                                    .map(node2 -> new EdgeList(node1, node2, null))
            ).sorted(Comparator.comparing(e -> distanceMatrix[e.nodeA][e.nodeB])).iterator();

            sortedEdges.hasNext();
            EdgeList current = sortedEdges.next();
            this.initSortedEdges = current;
            while (sortedEdges.hasNext()) {
                EdgeList newCurrent = sortedEdges.next();
                current.next = newCurrent;
                current = newCurrent;
            }
        }

        @Override
        public int nbVars() {
            return n - 1; //since zero is the initial point
        }

        @Override
        public PDState initialState() {
            System.out.println("init");
            BitSet openToVisit = new BitSet(n);
            openToVisit.set(1, n);

            for (int p : pickupToAssociatedDelivery.keySet()) {
                openToVisit.clear(pickupToAssociatedDelivery.get(p));
            }

            BitSet allToVisit = new BitSet(n);
            allToVisit.set(1, n);

            return new PDState(singleton(0), openToVisit, allToVisit, initSortedEdges);
        }

        public BitSet singleton(int singletonValue) {
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
            ArrayList<Integer> domain = new ArrayList<>(state.openToVisit.stream().boxed().toList());
            return domain.iterator();
        }

        @Override
        public PDState transition(PDState state, Decision decision) {
            return state.goTo(decision.val(), this);
        }

        @Override
        public int transitionCost(PDState state, Decision decision) {
            return -state.current.stream()
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

            while (states.hasNext()) {
                PDState state = states.next();
                //take the union; loose precision here
                openToVisit.or(state.openToVisit);
                allToVisit.or(state.allToVisit);
                current.or(state.current);
            }
            //the heuristics is reset to the initial sorted edges and will be filtered again from scratch
            return new PDState(current, openToVisit, allToVisit, problem.initSortedEdges);
        }

        @Override
        public int relaxEdge(PDState from, PDState to, PDState merged, Decision d, int cost) {
            return cost;
        }

        @Override
        public int fastUpperBound(PDState state, Set<Integer> variables) {
            if (state.current.cardinality() != 1) {
                throw new Error("no fast upper bound when no current");
            } else {
                int nbHopsToDo = variables.size();
                int lb = state.getSummedLengthOfNSmallestHops(nbHopsToDo, problem.distanceMatrix);
                return -lb;
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
     * @param n         the number of nodes of the PDP problem
     * @param unrelated the number of nodes that are not involved in a pickup-delivery pair.
     *                  there might be one more unrelated node than specified here
     * @return a PDP problem
     */
    public static PDProblem genInstance(int n, int unrelated) {

        int[] x = new int[n];
        int[] y = new int[n];
        Random r = new Random(1);
        for (int i = 0; i < n; i++) {
            x[i] = r.nextInt(100);
            y[i] = r.nextInt(100);
        }

        int[][] distance = new int[n][];
        for (int i = 0; i < n; i++) {
            distance[i] = new int[n];
            for (int j = 0; j < n; j++) {
                distance[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }

        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<Integer, Integer>();

        int firstDelivery = (n - unrelated - 1) / 2 + 1; //some  nodes are not pdp nodes
        for (int p = 1; p < firstDelivery; p++) {
            int d = firstDelivery + p - 1;
            pickupToAssociatedDelivery.put(p, d);
        }

        return new PDProblem(distance, pickupToAssociatedDelivery);
    }

    static int dist(int dx, int dy) {
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    public static void main(final String[] args) throws IOException {

        final PDProblem problem = genInstance(24, 0);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());

        solveDPD(problem);
        System.out.println("end");
    }

    public static void solveDPD(PDProblem problem) {

        final PDPRelax relax = new PDPRelax(problem);
        final PDPRanking ranking = new PDPRanking();
        final FixedWidth<PDState> width = new FixedWidth<>(2000);
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
        solver.maximize(1);
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] route = new int[problem.nbVars() + 1];
                    route[0] = 0;
                    for (Decision d : decisions) {
                        route[d.var() + 1] = d.val();
                    }
                    return route;
                })
                .get();

        System.out.printf("Duration : %.3f%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.println("Eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
        System.out.println("Problem:" + problem);
    }
}



