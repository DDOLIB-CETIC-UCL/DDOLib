package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

public final class MispAcsMain {
    /**
     * ***** The Maximum Independent Set Problem (MISP) *****
     * Given a weighted graph 𝐺 = (𝑉,𝐸,𝑤) where 𝑉= {1,...,𝑛}
     * is a set of vertices, 𝐸 \subset 𝑉 ×𝑉 the set of edges connecting those vertices and
     * 𝑤 = {𝑤1,𝑤2,...,𝑤𝑛} is a set of weights s.t. 𝑤𝑖 is the weight of node 𝑖.
     * The problem consists in finding a subset of vertices in a graph such that
     * no edge exists in the graph that connects two of the selected nodes and
     * the sum of the weight of the selected nodes is maximal.
     * This problem is considered in the paper:
     * - David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and Michael Wooldridge. Springer, 2016.
     * - David Bergman et al. “Discrete Optimization with Decision Diagrams”. In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.
     * /**
     * <p>
     * <p>
     * /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        final String file = Paths.get("data", "MISP", "tadpole_4_2.dot").toString();

        AcsModel<BitSet> model = new AcsModel<>() {
            private MispProblem problem;

            @Override
            public Problem<BitSet> problem() {
                try {
                    problem = readFile(file);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public DominanceChecker<BitSet> dominance() {
                return new SimpleDominanceChecker<>(new MispDominance(), problem.nbVars());
            }

            @Override
            public MispFastLowerBound lowerBound() {
                return new MispFastLowerBound(problem);
            }
        };

        Solver<BitSet> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAcs(model);

        System.out.println(stats);
    }


    /**
     * Creates an instance of Maximum Independent Set Problem from a .dot file.
     * If given, the expected value of the optimal solution {@code x} of the problem must in the second line written as
     * {@code optimal=x}.
     * <p>
     * To be correctly read, the file must contain first the list of the nodes and then the edges. If given the
     * optimal value must be written before the nodes.
     *
     * @param fileName A .dot file containing a graph.
     * @return An instance of the Maximum Independent Set Problem.
     */
    public static MispProblem readFile(String fileName) throws IOException {
        ArrayList<Integer> weight = new ArrayList<>();
        BitSet[] neighbor;
        Optional<Double> optimal = Optional.empty();
        int n;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null && !line.contains("--")) {
                if (line.isEmpty()) continue;

                if (line.contains("optimal")) {
                    String optiStr = line.replace(";", "");
                    String[] tokens = optiStr.split("=");
                    optimal = Optional.of(Double.parseDouble(tokens[1]));
                } else if (line.contains("weight")) {
                    String w = line.trim().split(" ")[1];
                    w = w.replace("[weight=", "").replace("];", "");
                    weight.add(Integer.parseInt(w));
                } else {
                    weight.add(1);
                }
            }
            n = weight.size();
            neighbor = new BitSet[n];
            Arrays.setAll(neighbor, i -> new BitSet(n));
            while (line != null && !line.equals("}")) {
                if (line.isEmpty()) {
                    line = br.readLine();
                    continue;
                }
                String[] tokens = line.replace(" ", "").replace(";", "").split("--");
                int source = Integer.parseInt(tokens[0]) - 1;
                int target = Integer.parseInt(tokens[1]) - 1;
                neighbor[source].set(target);
                neighbor[target].set(source);
                line = br.readLine();
            }
        }
        BitSet initialState = new BitSet(n);
        initialState.set(0, n, true);

        if (optimal.isPresent()) {
            return new MispProblem(initialState, neighbor, weight.stream().mapToInt(i -> i).toArray(), optimal.get());
        } else {
            return new MispProblem(initialState, neighbor, weight.stream().mapToInt(i -> i).toArray());
        }
    }


}
