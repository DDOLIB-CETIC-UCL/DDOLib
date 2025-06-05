package org.ddolib.ddo.examples.routing.cvrp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class CVRPIO {


    public static CVRPProblem readInstance(String fileName) throws IOException {
        int n = 0;
        int v = 0;
        int maxCapacity = 0;
        String edgesType = "";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            while (line != null && !line.contains("SECTION")) {
                if (line.contains("COMMENT")) {
                    String[] tokens = line.split(",");
                    String[] nbVehicles = tokens[1].split(":");
                    v = Integer.parseInt(nbVehicles[1].trim());
                } else if (line.contains("DIMENSION")) {
                    String[] tokens = line.split(":");
                    n = Integer.parseInt(tokens[1].trim());
                } else if (line.contains("EDGE_WEIGHT_TYPE")) {
                    String[] tokens = line.split(":");
                    edgesType = tokens[1].trim();
                } else if (line.contains("CAPACITY")) {
                    String[] tokens = line.split(":");
                    maxCapacity = Integer.parseInt(tokens[1].trim());
                }
                line = br.readLine();
            }
        }

        int[][] distanceMatrix = edgesType.equals("EXPLICIT") ? readMatrix(fileName, n) : readCoordinates(fileName, n);
        int[] demands = readDemand(fileName, n);

        return new CVRPProblem(v, n, maxCapacity, distanceMatrix, demands);
    }


    private static int[][] readMatrix(String fileName, int n) throws IOException {
        int[][] toReturn = new int[n][n];
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            while (line != null && !line.equals("EDGE_WEIGHT_SECTION")) line = br.readLine();

            line = br.readLine();
            int node = 0;
            while (node < n) {
                String[] tokens = line.split("\\s+");
                int[] row = Arrays.stream(tokens).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
                toReturn[node] = row;
                node++;
                line = br.readLine();
            }
        }
        return toReturn;
    }

    private static int[][] readCoordinates(String fileName, int n) throws IOException {
        int[][] coord = new int[n][2];
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            while (line != null && !line.equals("NODE_COORD_SECTION")) line = br.readLine();

            line = br.readLine();
            int node = 0;
            while (node < n) {
                String[] tokens = line.split("\\s+");
                coord[node][0] = Integer.parseInt(tokens[1].trim());
                coord[node][1] = Integer.parseInt(tokens[2].trim());
                node++;
                line = br.readLine();
            }
        }

        int[][] toReturn = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int[] x = coord[i];
                int[] y = coord[j];
                int d = (x[0] - y[0]) * (x[0] - y[0]) + (x[1] - y[1]) * (x[1] - y[1]);
                toReturn[i][j] = d;
            }
        }
        return toReturn;
    }

    private static int[] readDemand(String fileName, int n) throws IOException {
        int[] toReturn = new int[n];
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            while (line != null && !line.equals("DEMAND_SECTION")) line = br.readLine();

            line = br.readLine();
            int node = 0;
            while (node < n) {
                String[] tokens = line.split("\\s+");
                toReturn[node] = Integer.parseInt(tokens[1].trim());
                node++;
                line = br.readLine();
            }
        }
        return toReturn;
    }
}
