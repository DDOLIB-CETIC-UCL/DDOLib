package org.ddolib.examples.misp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;

import javax.lang.model.type.NullType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

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
        ArrayList<Integer> weight = new ArrayList<>();
        BitSet[] neighbor;
        Optional<Double> optimal = Optional.empty();
        int n;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null && !line.contains("--")) {
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


    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        final String file = args.length == 0 ? Paths.get("data", "MISP", "weighted.dot").toString() :
                args[0];
        final int maxWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 250;

        SolverConfig<BitSet, NullType> config = new SolverConfig<>();

        MispProblem problem = readFile(file);
        config.problem = problem;
        config.relax = new MispRelax(problem);
        config.ranking = new MispRanking();
        config.fub = new MispFastLowerBound(problem);
        config.width = new FixedWidth<>(maxWidth);
        config.varh = new DefaultVariableHeuristic<>();

        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        final Solver solver = new SequentialSolver<>(config);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.constructBestSolution(problem.nbVars());

        System.out.printf("Instance : %s%n", file);
        System.out.printf("Max width : %d%n", maxWidth);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

}
