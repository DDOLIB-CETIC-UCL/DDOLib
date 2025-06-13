package org.ddolib.ddo.examples.tsp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Random;

/**
 * Author: Pierre Schaus
 * Class to read and generate TSP instance instances
 */
public class TSPInstance {
    public static void main(String[] args) {
        TSPInstance instance = new TSPInstance("data/TSP/gr21.xml");
    }

    public double [][] distanceMatrix;
    public int n;
    public final int objective;

    public TSPInstance(double [][] distanceMatrix) {
        n = distanceMatrix.length;
        this.distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.distanceMatrix[i][j] = distanceMatrix[i][j];
            }
        }
        this.objective = -1;
    }

    public TSPInstance(int [][] distanceMatrix) {
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
     * Create a Euclidean TSP Instance by sampling coordinate on a square
     * @param n number of nodes
     * @param seed for the random number generator
     * @param squareLength the square length for the sampling of x/y coordinates of nodes
     */
    public TSPInstance(int n, int seed, int squareLength) {
        this.n = n;
        Random rand = new Random(seed);
        double [] xCoord =  new double[n];
        double [] yCoord = new double[n];
        distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            xCoord[i] = rand.nextInt(squareLength);
            yCoord[i] = rand.nextInt(squareLength);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                distanceMatrix[i][j] = dist(xCoord[i],yCoord[i],xCoord[j],yCoord[j]);
                distanceMatrix[j][i] = distanceMatrix[i][j];
            }
        }
        this.objective = -1;
    }


    /**
     * Create a Euclidean TSP Instance from x/y coordinates
     * @param xCoord
     * @param yCoord
     */
    public TSPInstance(int [] xCoord, int [] yCoord) {
        this.n = xCoord.length;
        distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                distanceMatrix[i][j] = dist(xCoord[i],yCoord[i],xCoord[j],yCoord[j]);
                distanceMatrix[j][i] = distanceMatrix[i][j];
            }
        }
        this.objective = -1;
    }

    /**
     * Read TSP Instance from xml
     * See http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/XML-TSPLIB/Description.pdf
     * @param xmlPath path to the file
     */
    public TSPInstance (String xmlPath) {
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        int obj = -1;
        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(xmlPath));
            doc.getDocumentElement().normalize();

            NodeList objlist = doc.getElementsByTagName("objective");
            if (objlist.getLength() > 0) {
                obj = Integer.parseInt(objlist.item(0).getTextContent());
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
            e.printStackTrace();
        }

        this.objective = obj;
    }


    /**
     * Number of cities
     * @return the number of cities
     */
    public int nCities(){
        return n;
    }

    /**
     * Distance between two cities
     * @param city1
     * @param city2
     * @return the distance between city1 and city2
     */
    public double distance(int city1, int city2){
        return distanceMatrix[city1][city2];
    }


    /**
     * Euclidean distance between two points
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return the Euclidean distance between (x1,y1) and (x2,y2)
     */
    public double dist(double x1, double y1, double x2, double y2) {
        double dx = x1-x2;
        double dy = y1-y2;
        return Math.rint(Math.sqrt(dx*dx+dy*dy));
    }

    /**
     * Save TSP Instance to xml format
     * See http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/XML-TSPLIB/Description.pdf
     * @param path to the xml file
     * @param objective of the best known solution
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
            bestObjective.setTextContent(""+objective);
            rootElement.appendChild(bestObjective);

            Element graph = doc.createElement("graph");

            for (int i = 0; i < n; i++) {
                Element vertex = doc.createElement("vertex");
                for (int j = 0; j < n; j++) {
                    if (j != i) {
                        Element edge = doc.createElement("edge");
                        edge.setAttribute("cost",""+distanceMatrix[i][j]);
                        edge.setTextContent(""+j);
                        vertex.appendChild(edge);
                    }
                }
                graph.appendChild(vertex);
            }


            rootElement.appendChild(graph);

            FileOutputStream output = new FileOutputStream(path);
            writeXml(doc, output);

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // write doc to output stream

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