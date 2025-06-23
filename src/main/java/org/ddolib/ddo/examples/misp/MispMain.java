package org.ddolib.ddo.examples.misp;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

import static org.ddolib.ddo.implem.solver.Solvers.parallelSolver;

public final class MispMain {


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
        int[] weight;
        BitSet[] neighbor;
        Optional<Double> optimal = Optional.empty();
        int n = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null && !line.contains("--")) {

                if (line.contains("optimal")) {
                    String optiStr = line.replace(";", "");
                    String[] tokens = optiStr.split("=");
                    optimal = Optional.of(Double.parseDouble(tokens[1]));
                } else {
                    n++;
                }
            }
            weight = new int[n];
            neighbor = new BitSet[n];
            for (int i = 0; i < n; i++) {
                weight[i] = 1;
                neighbor[i] = new BitSet(n);
            }

            while (line != null && !line.equals("}")) {
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

        return new MispProblem(initialState, neighbor, weight, optimal);
    }


    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddo.examples.misp.Misp"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddo.examples.misp.Misp -Dexec.args="<your file> <maximum
     * width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        final String file = args.length == 0 ? "data/MISP/C6.dot" : args[0];
        final int maxWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 250;

        final MispProblem problem = readFile(file);

        System.out.println(problem.nbVars());
        System.out.println(Arrays.toString(problem.neighbors));

        final MispRelax relax = new MispRelax(problem);
        final MispRanking ranking = new MispRanking();
        final FixedWidth<BitSet> width = new FixedWidth<>(maxWidth);
        final VariableHeuristic<BitSet> varh = new DefaultVariableHeuristic<BitSet>();

        final Frontier<BitSet> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = parallelSolver(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        values[d.var()] = d.val();
                    }
                    return values;
                })
                .get();

        System.out.printf("Instance : %s%n", file);
        System.out.printf("Max width : %d%n", maxWidth);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

}
