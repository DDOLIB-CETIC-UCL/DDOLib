package org.ddolib.examples.ddo.tsptw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class TSPTWInstance {

    public int[][] distance;
    public TimeWindow[] timeWindows;
    public Optional<Double> optimal = Optional.empty();
    public String name;

    /**
     * Creates instance from data files.<br>
     * <p>
     * The expected format is the following:
     * <ul>
     *     <li>
     *         The first line must contain the number of variable. A second  optional value can be
     *         given: the expected objective value for an optimal solution.
     *     </li>
     *     <li>
     *         The time matrix.
     *     </li>
     *     <li>
     *         A time window for each node.
     *     </li>
     * </ul>
     *
     * @param fileName The path to the input file.
     * @throws IOException If something goes wrong while reading input file.
     */
    public TSPTWInstance(String fileName) throws IOException {
        this.name = fileName;
        int numVar = 0;
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
                    distance = new int[numVar][numVar];
                    timeWindows = new TimeWindow[numVar];
                    if (tokens.length == 2) optimal = Optional.of(Double.parseDouble(tokens[1]));
                } else if (1 <= lineCount && lineCount <= numVar) {
                    int i = lineCount - 1;
                    String[] distanceFromI = line.split("\\s+");
                    distance[i] = Arrays.stream(distanceFromI).mapToInt(Integer::parseInt).toArray();
                } else {
                    int i = lineCount - 1 - numVar;
                    String[] tw = line.split("\\s+");
                    timeWindows[i] = new TimeWindow(Integer.parseInt(tw[0]), Integer.parseInt(tw[1]));
                }
                lineCount++;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append(Arrays.toString(timeWindows)).append("\n");
        String timeStr = Arrays.stream(distance)
                .map(row -> Arrays.stream(row)
                        .mapToObj(x -> String.format("%4s", x))
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
        sb.append(timeStr);
        return sb.toString();
    }
}

record TimeWindow(int start, int end) {
}
