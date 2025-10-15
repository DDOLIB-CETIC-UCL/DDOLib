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

public class TSPProblem implements Problem<TSPState> {

    final int n;
    final double[][] distanceMatrix;

    private Optional<Double> optimal = Optional.empty();

    /**
     * A name to ease the readability of the tests.
     */
    private Optional<String> name = Optional.empty();

    @Override
    public String toString() {
        String defaultStr = "TSP(n:" + n + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList() + "\n)";
        return name.orElse(defaultStr);
    }

    public double eval(int[] solution) {
        double toReturn = 0;
        for (int i = 1; i < solution.length; i++) {
            toReturn = toReturn + distanceMatrix[solution[i - 1]][solution[i]];
        }
        toReturn = toReturn + distanceMatrix[solution[solution.length - 1]][0]; //final come back
        return toReturn;
    }

    public TSPProblem(final double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
    }

    public TSPProblem(final double[][] distanceMatrix, double optimal) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
        this.optimal = Optional.of(optimal);
    }


    /**
     * Read TSP Instance from xml
     * See <a href="http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/XML-TSPLIB/Description.pdf">
     * "http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/XML-TSPLIB/Description.pdf"</a>
     *
     * @param fname The path to the file.
     * @throws IOException If something goes wrong while reading the input file.
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

    @Override
    public int nbVars() {
        return n; //the last decision will be to come back to point zero
    }

    @Override
    public TSPState initialState() {
        BitSet toVisit = new BitSet(n);
        toVisit.set(1, n);

        return new TSPState(singleton(0), toVisit);
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(n);
        toReturn.set(singletonValue);
        return toReturn;
    }

    @Override
    public double initialValue() {
        return 0;
    }

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

    @Override
    public TSPState transition(TSPState state, Decision decision) {
        int node = decision.val();


        BitSet newToVisit = (BitSet) state.toVisit.clone();
        newToVisit.clear(node);

        return new TSPState(state.singleton(node), newToVisit);
    }

    @Override
    public double transitionCost(TSPState state, Decision decision) {
        return state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .mapToDouble(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsDouble();
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }
}
