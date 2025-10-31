package org.ddolib.examples.tsp;


import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class representing an instance of the Traveling Salesman Problem (TSP).
 *
 * <p>
 * This class provides functionality to:
 * </p>
 * <ul>
 *     <li>Store the number of nodes and the distance matrix.</li>
 *     <li>Evaluate the cost of a given TSP tour.</li>
 *     <li>Create TSP instances from a distance matrix (double[][]) or from an XML file following XML-TSPLIB.</li>
 *     <li>Provide the initial state, variable domains, and transition costs for search algorithms.</li>
 *     <li>Optionally store the optimal solution value for benchmarking purposes.</li>
 * </ul>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 *     double[][] distMatrix = ...;
 *     TSPProblem problem = new TSPProblem(distMatrix);
 *     double cost = problem.eval(new int[]{0,1,2,3});
 * </pre>
 *
 * @see <a href="http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/XML-TSPLIB/Description.pdf">
 * XML-TSPLIB specification</a>
 */
public class TSPProblem implements Problem<TSPState> {

    /**
     * Number of nodes (cities)
     */
    final int n;

    /**
     * Distance matrix between nodes
     */
    final double[][] distanceMatrix;

    /**
     * Optional value of the known optimal solution
     */
    private Optional<Double> optimal = Optional.empty();

    /**
     * Optional name for easier readability of tests
     */
    private Optional<String> name = Optional.empty();

    /**
     * Evaluates the total tour cost for a given solution.
     *
     * @param solution an array representing the node visit order
     * @return the total cost of the tour including return to the starting node
     */
    public double eval(int[] solution) {
        double toReturn = 0;
        for (int i = 1; i < solution.length; i++) {
            toReturn = toReturn + distanceMatrix[solution[i - 1]][solution[i]];
        }
        toReturn = toReturn + distanceMatrix[solution[solution.length - 1]][0]; //final come back
        return toReturn;
    }

    /**
     * Constructs a TSP instance from a given distance matrix.
     *
     * @param distanceMatrix the distance matrix of the instance
     */
    public TSPProblem(final double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
    }

    /**
     * Constructs a TSP instance from a given distance matrix and known optimal value.
     *
     * @param distanceMatrix the distance matrix of the instance
     * @param optimal        the optimal solution value
     */
    public TSPProblem(final double[][] distanceMatrix, double optimal) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
        this.optimal = Optional.of(optimal);
    }


    /**
     * Constructs a TSP instance by reading an XML file in the XML-TSPLIB format.
     *
     * @param fname the path to the XML file
     * @throws IOException if an error occurs while reading or parsing the file
     */
    public TSPProblem(final String fname) throws IOException {
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Optional<Double> obj = Optional.empty();
        int n;
        double[][] distanceMatrix;
        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(fname));
            doc.getDocumentElement().normalize();

            NodeList objlist = doc.getElementsByTagName("objective");
            if (objlist.getLength() > 0) {
                obj = Optional.of(Double.parseDouble(objlist.item(0).getTextContent()));
            }

            NodeList list = doc.getElementsByTagName("vertex");

            n = list.getLength();
            distanceMatrix = new double[n][n];

            for (int i = 0; i < n; i++) {
                NodeList edgeList = list.item(i).getChildNodes();
                for (int v = 0; v < edgeList.getLength(); v++) {

                    Node node = edgeList.item(v);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String cost = element.getAttribute("cost");
                        String adjacentNode = element.getTextContent();
                        int j = Integer.parseInt(adjacentNode);
                        distanceMatrix[i][j] = Math.rint(Double.parseDouble(cost));
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        this.n = n;
        this.distanceMatrix = distanceMatrix;
        this.optimal = obj;
        this.name = Optional.of(fname);

    }

    /**
     * Returns the number of decision variables in the problem.
     *
     * @return number of variables (equal to number of nodes)
     */
    @Override
    public int nbVars() {
        return n; //the last decision will be to come back to point zero
    }

    /**
     * Returns the initial state for a search algorithm.
     * The initial state starts at node 0, with all other nodes unvisited.
     *
     * @return the initial TSPState
     */
    @Override
    public TSPState initialState() {
        BitSet toVisit = new BitSet(n);
        toVisit.set(1, n);

        return new TSPState(singleton(0), toVisit);
    }

    /**
     * Creates a BitSet containing only a single node.
     *
     * @param singletonValue the node to include
     * @return a BitSet with only the specified node set
     */
    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(n);
        toReturn.set(singletonValue);
        return toReturn;
    }

    /**
     * Returns the initial cost of the problem, which is 0.
     *
     * @return 0.0
     */
    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * Returns the domain of possible decisions for a given state and variable index.
     * The last variable represents returning to the starting node.
     *
     * @param state the current state
     * @param var   the variable index
     * @return an iterator over possible node indices
     */
    @Override
    public Iterator<Integer> domain(TSPState state, int var) {
        if (var == n - 1) {
            //the final decision is to come back to node zero
            return singleton(0).stream().iterator();
        } else {
            ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
            return domain.iterator();
        }
    }

    /**
     * Computes the next state after making a decision from the current state.
     *
     * @param state    the current TSPState
     * @param decision the decision made
     * @return the resulting TSPState after applying the decision
     */
    @Override
    public TSPState transition(TSPState state, Decision decision) {
        int node = decision.val();


        BitSet newToVisit = (BitSet) state.toVisit.clone();
        newToVisit.clear(node);

        return new TSPState(state.singleton(node), newToVisit);
    }

    /**
     * Computes the transition cost of moving from the current state to the next state
     * by visiting a given node.
     *
     * @param state    the current TSPState
     * @param decision the decision to move to a node
     * @return the cost of the transition
     */
    @Override
    public double transitionCost(TSPState state, Decision decision) {
        return state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .mapToDouble(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsDouble();
    }

    /**
     * Returns the optimal value of the instance if known.
     *
     * @return an Optional containing the optimal value, or empty if unknown
     */
    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    /**
     * Returns a string representation of the instance.
     * If a name is set, it returns the name; otherwise, it prints the size and distance matrix.
     *
     * @return string representation of the TSPProblem
     */
    @Override
    public String toString() {
        String defaultStr = "TSP(n:" + n + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList() + "\n)";
        return name.orElse(defaultStr);
    }


    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), nbVars()));
        }

        Map<Integer, Long> count = Arrays.stream(solution)
                .boxed()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        if (count.values().stream().anyMatch(x -> x != 1)) {
            String msg = "The solution has duplicated nodes and does not reache each node";
            throw new InvalidSolutionException(msg);
        }

        double value = distanceMatrix[0][solution[0]]; //Start from the depot.
        for (int i = 1; i < nbVars(); i++) {
            value += distanceMatrix[solution[i - 1]][solution[i]];
        }


        return value;
    }
}
