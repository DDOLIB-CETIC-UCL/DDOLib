package org.ddolib.examples.ddo.srflp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class SRFLPIO {

    public static SRFLPProblem readInstance(String filename) throws IOException {
        int[] length = new int[0];
        int[][] flows = new int[0][0];
        Optional<Double> optimal = Optional.empty();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            int lineCount = 0;
            int skip = 0;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    skip++;
                } else if (lineCount == 0) {
                    String[] tokens = line.replace(",", " ").split("\\s+");
                    int n = Integer.parseInt(tokens[0]);
                    length = new int[n];
                    flows = new int[n][n];
                    if (tokens.length > 1) optimal = Optional.of(Double.parseDouble(tokens[1]));

                } else if (lineCount - skip == 1) {
                    String[] tokens = line.replace(",", " ").split("\\s+");
                    length = Arrays.stream(tokens).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
                } else {
                    int department = lineCount - skip - 2;
                    String[] tokens = line.replace(",", " ").split("\\s+");
                    int[] row = Arrays.stream(tokens).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
                    flows[department] = row;
                }
                lineCount++;
            }

        }
        return new SRFLPProblem(length, flows, optimal);
    }
}
