package org.ddolib.ddo.examples.max2sat;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Max2SatIO {

    public static Max2SatProblem readInstance(String fileName) throws IOException {
        try (BufferedReader bf = new BufferedReader(new FileReader(fileName))) {
            final Context context = new Context();
            String line;
            while ((line = bf.readLine()) != null) {
                if (context.firstLine) {
                    context.firstLine = false;
                    String[] tokens = line.split("\\s");
                    context.n = Integer.parseInt(tokens[0]);
                } else {
                    String[] tokens = line.split("\\s");
                    int i = Integer.parseInt(tokens[0]);
                    int j = Integer.parseInt(tokens[1]);
                    int w = Integer.parseInt(tokens[2]);
                    context.weights.put(new BinaryClause(i, j), w);
                }
            }
            return new Max2SatProblem(context.n, context.weights);
        }

    }


    private static class Context {
        boolean firstLine = true;
        int n = 0;
        HashMap<BinaryClause, Integer> weights = new HashMap<>();

    }

    public static void generateInstance(int numVar, String fileName, long seed) throws IOException {
        Stream<Integer> positive = IntStream.rangeClosed(1, numVar).boxed();
        Stream<Integer> negative = IntStream.rangeClosed(-numVar - 1, -1).boxed();

        List<Integer> literal = Stream.concat(positive, negative).toList();
        ArrayList<BinaryClause> pairs = new ArrayList<>();
        for (int i = 0; i < literal.size(); i++) {
            for (int j = i + 1; j < literal.size(); j++) {
                int xi = literal.get(i);
                int xj = literal.get(j);
                if (Math.abs(xi) != Math.abs(xj)) {
                    pairs.add(new BinaryClause(xi, xj));
                }
            }
        }
        Random rng = new Random(seed);
        Collections.shuffle(pairs, rng);

        var selected = pairs.subList(0, rng.nextInt(pairs.size() + 1));
        System.out.println(selected);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("" + numVar);
            for (BinaryClause bc : selected) {
                bw.newLine();
                int w = rng.nextInt(1, 11);
                String line = String.format("%d %d %d", bc.i, bc.j, w);
                bw.write(line);
            }
        }


    }

    public static void main(String[] args) throws IOException {
        generateInstance(4, "data/Max2Sat/instance_2.txt", 42L);
    }

}
