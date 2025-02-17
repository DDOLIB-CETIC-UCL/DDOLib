package org.ddolib.ddo.examples.max2sat;

import java.io.*;
import java.util.*;

public class Max2SatIO {

    /**
     * Returns a Max2SatProblem given an input file.
     * <br>
     * The expected format is the following:
     * <ul>
     *     <li>The first line must contain the number of variable. A second value can be
     *     given: the expected objective value for an optimal solution.
     *     </li>
     *     <li>
     *         The other lines must contain 3 values. The two first are non-null indices of variables. If one of
     *         these value is positive, it models the literal <code>x<sub>i</sub></code>. If it is negative,
     *         it models the literal <code>NOT x<sub>i</sub></code>.
     *     </li>
     *     <li>
     *         The third value is the weight of the clause formed by the two literals.
     *     </li>
     * </ul>
     *
     * @param fileName The path to the input file.
     * @return An instance of Max2SatProblem.
     * @throws IOException If something goes wrong while reading input file.
     */
    public static Max2SatProblem readInstance(String fileName) throws IOException {
        try (BufferedReader bf = new BufferedReader(new FileReader(fileName))) {
            final Context context = new Context();
            String line;
            while ((line = bf.readLine()) != null) {
                if (context.firstLine) {
                    context.firstLine = false;
                    String[] tokens = line.split("\\s");
                    context.n = Integer.parseInt(tokens[0]);
                    if (tokens.length == 2) {
                        context.opti = Optional.of(Integer.parseInt(tokens[1]));
                    }
                } else {
                    String[] tokens = line.split("\\s");
                    int i = Integer.parseInt(tokens[0]);
                    int j = Integer.parseInt(tokens[1]);
                    int w = Integer.parseInt(tokens[2]);
                    context.weights.put(new BinaryClause(i, j), w);
                }
            }
            return new Max2SatProblem(context.n, context.weights, context.opti);
        }

    }


    private static class Context {
        boolean firstLine = true;
        int n = 0;
        HashMap<BinaryClause, Integer> weights = new HashMap<>();
        Optional<Integer> opti = Optional.empty();

    }

    public static void generateInstance(int numVar, String fileName, long seed) throws IOException {

        ArrayList<Integer> literal = new ArrayList<>();
        for (int i = 1; i <= numVar; i++) {
            literal.add(i);
            literal.add(-i);
        }

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

        List<BinaryClause> selected = pairs.subList(0, rng.nextInt(0, pairs.size() + 1));
        Collections.sort(selected);
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

    public static void generateInstance(int numVar, String fileName) throws IOException {
        Random random = new Random();
        generateInstance(numVar, fileName, random.nextLong());
    }

    public static void main(String[] args) throws IOException {
        //generateInstance(5, "data/Max2Sat/wcnf_var_5_opti_53.txt", 42);
        Max2SatProblem problem = readInstance("data/Max2Sat/wcnf_var_4_opti_39.txt");

        NaiveMax2SatSolver s = new NaiveMax2SatSolver(problem);
        long start = System.currentTimeMillis();
        s.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", s.best());
        System.out.printf("Solution : %s%n", Arrays.toString(s.bestSolution()));
    }

}
