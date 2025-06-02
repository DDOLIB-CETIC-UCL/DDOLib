package org.ddolib.ddo.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;
import java.util.stream.IntStream;

class PDPProblem implements Problem<PDPState> {
    final int n;
    final int[][] distanceMatrix;

    HashMap<Integer, Integer> pickupToAssociatedDelivery;
    HashMap<Integer, Integer> deliveryToAssociatedPickup;

    Set<Integer> unrelatedNodes;
    SortedEdgeList initSortedEdges;

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

    public PDPProblem(final int[][] distanceMatrix, HashMap<Integer, Integer> pickupToAssociatedDelivery) {
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
        Iterator<SortedEdgeList> sortedEdges = IntStream.range(1, n).boxed().flatMap(
                node1 ->
                        IntStream.range(1, n)
                                .filter(node2 -> node1 > node2)
                                .boxed()
                                .map(node2 -> new SortedEdgeList(node1, node2, null))
        ).sorted(Comparator.comparing(e -> distanceMatrix[e.nodeA][e.nodeB])).iterator();

        sortedEdges.hasNext();
        SortedEdgeList current = sortedEdges.next();
        this.initSortedEdges = current;
        while (sortedEdges.hasNext()) {
            SortedEdgeList newCurrent = sortedEdges.next();
            current.next = newCurrent;
            current = newCurrent;
        }
    }

    @Override
    public int nbVars() {
        return n - 1; //since zero is the initial point
    }

    @Override
    public PDPState initialState() {
        System.out.println("init");
        BitSet openToVisit = new BitSet(n);
        openToVisit.set(1, n);

        for (int p : pickupToAssociatedDelivery.keySet()) {
            openToVisit.clear(pickupToAssociatedDelivery.get(p));
        }

        BitSet allToVisit = new BitSet(n);
        allToVisit.set(1, n);

        return new PDPState(singleton(0), openToVisit, allToVisit, initSortedEdges);
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
    public Iterator<Integer> domain(PDPState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>(state.openToVisit.stream().boxed().toList());
        return domain.iterator();
    }

    @Override
    public PDPState transition(PDPState state, Decision decision) {
        return state.goTo(decision.val(), this);
    }

    @Override
    public int transitionCost(PDPState state, Decision decision) {
        return -state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .map(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsInt();
    }
}
