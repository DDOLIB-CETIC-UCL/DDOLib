package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TSPProblem implements Problem<TSPState> {
    int n;
    int[][] distanceMatrix;
    final SortedAdjacents sortedAdjacents;

    @Override
    public String toString() {
        return "TSP(n:" + n + "\n" +
                "\t" + Arrays.stream(distanceMatrix).map(l -> "\n\t " + Arrays.toString(l)).toList() + "\n)";
    }

    public int eval(int[] solution) {
        int toReturn = 0;
        for (int i = 1; i < solution.length; i++) {
            toReturn = toReturn + distanceMatrix[solution[i - 1]][solution[i]];
        }
        //toReturn += distanceMatrix[solution[solution.length - 1]][solution[0]];
        return toReturn;
    }

    public TSPProblem(final int[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;
        this.sortedAdjacents = new SortedAdjacents(distanceMatrix);
    }


    /**
     * Creates instance from data files.<br>
     * <p>
     * The expected format is the following:
     * <ul>
     *     <li>
     *         The first line must contain the number of points
     *     </li>
     *     <li>
     *         The distance matrix.
     *     </li>
     * </ul>
     *
     * @param fileName The path to the input file.
     * @throws IOException If something goes wrong while reading input file.
     */
    public TSPProblem(String fileName) throws IOException {
        int numVar = 0;
        int[][] myDistanceMatrix = null;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int lineCount = 0;
            String line;
            while ((line = br.readLine()) != null) {
                //Skip comment
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                if (lineCount == 0) {
                    String[] tokens = line.split("\\s+");
                    numVar = Integer.parseInt(tokens[0]);
                    myDistanceMatrix = new int[numVar][numVar];
                } else if (1 <= lineCount && lineCount <= numVar) {
                    int i = lineCount - 1;
                    String[] distanceFromI = line.split("\\s+");
                    myDistanceMatrix[i] = Arrays.stream(distanceFromI).mapToInt(Integer::parseInt).toArray();
                }
                lineCount++;
            }
        }
        this.distanceMatrix = myDistanceMatrix;
        this.n = distanceMatrix.length;
        this.sortedAdjacents = new SortedAdjacents(distanceMatrix);
    }

    @Override
    public int nbVars() {
        return n - 1; //since zero is the initial point
    }

    @Override
    public TSPState initialState() {
        System.out.println("init");
        BitSet toVisit = new BitSet(n);
        toVisit.set(1, n);

        return new TSPState(singleton(0), toVisit, sortedAdjacents.initialHeuristics());
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
    public Iterator<Integer> domain(TSPState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>(state.toVisit.stream().boxed().toList());
        return domain.iterator();
    }

    @Override
    public TSPState transition(TSPState state, Decision decision) {
        return state.goTo(decision.val());
    }

    @Override
    public int transitionCost(TSPState state, Decision decision) {
        return -state.current.stream()
                .filter(possibleCurrentNode -> possibleCurrentNode != decision.val())
                .map(possibleCurrentNode -> distanceMatrix[possibleCurrentNode][decision.val()])
                .min()
                .getAsInt();
    }
}
