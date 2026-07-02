package org.ddolib.nolayer.examples.tsp;

import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.util.InvalidSolutionException;

import java.util.*;
import java.util.stream.Collectors;

public class TSPProblem implements Problem<TSPState> {

    public final double[][] distanceMatrix;
    private final int n;

    public TSPProblem(final double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
    }

    public TSPProblem(final String fname) throws java.io.IOException {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        int n;
        double[][] distanceMatrix;
        try {
            dbf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(new java.io.File(fname));
            doc.getDocumentElement().normalize();

            org.w3c.dom.NodeList list = doc.getElementsByTagName("vertex");
            n = list.getLength();
            distanceMatrix = new double[n][n];

            for (int i = 0; i < n; i++) {
                org.w3c.dom.NodeList edgeList = list.item(i).getChildNodes();
                for (int v = 0; v < edgeList.getLength(); v++) {
                    org.w3c.dom.Node node = edgeList.item(v);
                    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                        String cost = element.getAttribute("cost");
                        String adjacentNode = element.getTextContent();
                        int j = Integer.parseInt(adjacentNode);
                        distanceMatrix[i][j] = Math.rint(Double.parseDouble(cost));
                    }
                }
            }
        } catch (Exception e) {
            throw new java.io.IOException(e);
        }

        this.n = n;
        this.distanceMatrix = distanceMatrix;
    }

    @Override
    public TSPState initialState() {
        BitSet toVisit = new BitSet(n);
        toVisit.set(1, n);
        return new TSPState(singleton(0), toVisit);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public boolean isTarget(TSPState state) {
        // Target is reached when no more cities to visit and current is 0
        return state.toVisit.isEmpty() && state.current.get(0);
    }

    @Override
    public Iterator<Integer> domain(TSPState state) {
        if (state.toVisit.isEmpty()) {
            if (!state.current.get(0)) {
                return singleton(0).stream().iterator();
            }
            return Collections.emptyIterator(); // Already at target
        } else {
            ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
            return domain.iterator();
        }
    }

    @Override
    public TSPState transition(TSPState state, int label) {
        BitSet newToVisit = (BitSet) state.toVisit.clone();
        newToVisit.clear(label);
        return new TSPState(singleton(label), newToVisit);
    }

    @Override
    public double transitionCost(TSPState state, int label) {
        return state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != label)
                .mapToDouble(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][label])
                .min()
                .orElse(0.0);
    }

    @Override
    public double evaluate(List<Integer> solution) throws InvalidSolutionException {
        if (solution.size() != n) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", solution, n));
        }

        Map<Integer, Long> count = solution.stream()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        if (count.values().stream().anyMatch(x -> x != 1)) {
            String msg = "The solution has duplicated nodes and does not reach each node exactly once";
            throw new InvalidSolutionException(msg);
        }

        if (solution.get(n - 1) != 0) {
            throw new InvalidSolutionException("The solution does not return to the depot (node 0)");
        }

        double value = distanceMatrix[0][solution.get(0)];
        for (int i = 1; i < n; i++) {
            value += distanceMatrix[solution.get(i - 1)][solution.get(i)];
        }

        return value;
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(n);
        toReturn.set(singletonValue);
        return toReturn;
    }

    @Override
    public String toString() {
        return "TSPNoLayer(n:" + n + ")";
    }
}
