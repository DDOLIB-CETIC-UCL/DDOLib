package org.ddolib.examples.tsp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * Class to generate and handle instances of the Traveling Salesman Problem (TSP).
 *
 * <p>
 * This class provides functionality to:
 * </p>
 * <ul>
 *     <li>Create TSP instances from a distance matrix (int[][] or double[][]).</li>
 *     <li>Create Euclidean TSP instances by sampling coordinates within a square.</li>
 *     <li>Create Euclidean TSP instances from given x/y coordinates.</li>
 *     <li>Access distances between cities and the number of cities.</li>
 *     <li>Save a TSP instance in XML format following the XML-TSPLIB standard.</li>
 * </ul>
 *
 * <p>
 * The class maintains an internal distance matrix representing the cost between cities
 * and optionally stores the objective value (best known solution).
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre>
 *     // Generate a Euclidean TSP instance with 10 cities
 *     TSPGenerator tsp = new TSPGenerator(10, 42, 100);
 *     double dist = tsp.distance(0, 1);
 *     tsp.saveXml("instance.xml", 1234);
 * </pre>
 *
 * @author Pierre Schaus
 */
public class TSPGenerator {
    /**
     * Distance matrix between cities
     */
    public double[][] distanceMatrix;

    /**
     * Number of cities
     */
    public int n;

    /**
     * Best known objective value (optional, -1 if unknown)
     */
    public final double objective;

    /**
     * Constructs a TSP instance from a double[][] distance matrix.
     *
     * @param distanceMatrix the distance matrix
     */

    public TSPGenerator(double[][] distanceMatrix) {
        n = distanceMatrix.length;
        this.distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.distanceMatrix[i][j] = distanceMatrix[i][j];
            }
        }
        this.objective = -1;
    }

    /**
     * Constructs a TSP instance from an int[][] distance matrix.
     *
     * @param distanceMatrix the distance matrix
     */
    public TSPGenerator(int[][] distanceMatrix) {
        n = distanceMatrix.length;
        this.distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.distanceMatrix[i][j] = distanceMatrix[i][j];
            }
        }
        this.objective = -1;
    }

    /**
     * Constructs a Euclidean TSP instance by randomly sampling coordinates in a square.
     *
     * @param n            number of cities
     * @param seed         random seed for reproducibility
     * @param squareLength length of the square in which coordinates are sampled
     */
    public TSPGenerator(int n, int seed, int squareLength) {
        this.n = n;
        Random rand = new Random(seed);
        double[] xCoord = new double[n];
        double[] yCoord = new double[n];
        distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            xCoord[i] = rand.nextInt(squareLength);
            yCoord[i] = rand.nextInt(squareLength);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                distanceMatrix[i][j] = dist(xCoord[i], yCoord[i], xCoord[j], yCoord[j]);
                distanceMatrix[j][i] = distanceMatrix[i][j];
            }
        }
        this.objective = -1;
    }


    /**
     * Constructs a Euclidean TSP instance from given x/y coordinates.
     *
     * @param xCoord array of x-coordinates
     * @param yCoord array of y-coordinates
     */
    public TSPGenerator(int[] xCoord, int[] yCoord) {
        this.n = xCoord.length;
        distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                distanceMatrix[i][j] = dist(xCoord[i], yCoord[i], xCoord[j], yCoord[j]);
                distanceMatrix[j][i] = distanceMatrix[i][j];
            }
        }
        this.objective = -1;
    }

    /**
     * Returns the number of cities in the instance.
     *
     * @return the number of cities
     */
    public int nCities() {
        return n;
    }

    /**
     * Returns the distance between two cities.
     *
     * @param city1 first city index
     * @param city2 second city index
     * @return distance between city1 and city2
     */
    public double distance(int city1, int city2) {
        return distanceMatrix[city1][city2];
    }


    /**
     * Computes the Euclidean distance between two points.
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @return Euclidean distance between the points
     */
    public double dist(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.rint(Math.sqrt(dx * dx + dy * dy));
    }

    /**
     * Saves the TSP instance in XML format following XML-TSPLIB standards.
     *
     * @param path      path to the XML file
     * @param objective best known solution value
     */
    public void saveXml(String path, int objective) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;

            docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("travellingSalesmanProblemInstance");
            doc.appendChild(rootElement);

            Element bestObjective = doc.createElement("objective");
            bestObjective.setTextContent("" + objective);
            rootElement.appendChild(bestObjective);

            Element graph = doc.createElement("graph");

            for (int i = 0; i < n; i++) {
                Element vertex = doc.createElement("vertex");
                for (int j = 0; j < n; j++) {
                    if (j != i) {
                        Element edge = doc.createElement("edge");
                        edge.setAttribute("cost", "" + distanceMatrix[i][j]);
                        edge.setTextContent("" + j);
                        vertex.appendChild(edge);
                    }
                }
                graph.appendChild(vertex);
            }


            rootElement.appendChild(graph);

            FileOutputStream output = new FileOutputStream(path);
            writeXml(doc, output);

        } catch (ParserConfigurationException | TransformerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes an XML Document to an OutputStream.
     *
     * @param doc    the XML document
     * @param output the output stream
     * @throws TransformerException if an error occurs during transformation
     */
    private static void writeXml(Document doc,
                                 OutputStream output)
            throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);

    }

}