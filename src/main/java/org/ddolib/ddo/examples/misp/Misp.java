package org.ddolib.ddo.examples.misp;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.util.SupplierUtil;

import java.io.*;
import java.util.*;

public final class Misp {

    public static MispProblem cycleGraph(int n) {
        BitSet state = new BitSet(n);
        state.set(0, n, true);
        int[] weight = new int[n];
        BitSet[] neighbor = new BitSet[n];
        for (int i = 0; i < n; i++) {
            weight[i] = 1;
            neighbor[i] = new BitSet(n);
        }

        for (int i = 0; i < n; i++) {
            if (i != 0) neighbor[i].set(i - 1);
            if (i != n - 1) neighbor[i].set(i + 1);
        }
        neighbor[0].set(n - 1);
        neighbor[n - 1].set(0);

        return new MispProblem(state, neighbor, weight, Optional.of(n / 2));
    }

    /**
     * Creates an instance of Maximum Independent Set Problem from a .dot file.
     *
     * @param fileName A .dot file containing a graph.
     * @return An instance of the Maximum Independent Set Problem.
     */
    public static MispProblem readGraph(String fileName) throws IOException {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(),
                SupplierUtil.createDefaultEdgeSupplier(),
                false
        );
        DOTImporter<Integer, DefaultEdge> importer = new DOTImporter<>();
        File f = new File(fileName);
        importer.importGraph(g, f);


        int n = g.vertexSet().size();
        int[] weight = new int[n];
        BitSet[] neighbor = new BitSet[n];
        for (int i = 0; i < n; i++) {
            weight[i] = 1;
            neighbor[i] = new BitSet(n);
        }

        for (DefaultEdge e : g.edgeSet()) {
            int source = g.getEdgeSource(e);
            int target = g.getEdgeTarget(e);
            neighbor[source].set(target);
            neighbor[target].set(source);
        }

        BitSet initialState = new BitSet(n);
        initialState.set(0, n, true);

        return new MispProblem(initialState, neighbor, weight, findOptimum(fileName));
    }

    private static Optional<String> findLine(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("optimal")) {
                return Optional.of(line);
            }
        }
        return Optional.empty();
    }

    private static Optional<Integer> findOptimum(String fileName) throws IOException {
        Optional<String> line = findLine(fileName);
        if (line.isEmpty()) {
            return Optional.empty();
        } else {
            String optiLine = line.get();
            int optimalStrLength = "optimal=".length();
            int opti = Integer.parseInt(optiLine.substring(optimalStrLength, optiLine.length() - 1));
            return Optional.of(opti);
        }

    }


    public static void main(String[] args) throws IOException {
        final MispProblem problem = readGraph("data/MISP/S_20.dot");
        final MispRelax relax = new MispRelax(problem);
        final MispRanking ranking = new MispRanking();
        final FixedWidth<BitSet> width = new FixedWidth<>(250);
        final VariableHeuristic<BitSet> varh = new DefaultVariableHeuristic<BitSet>();

        final Frontier<BitSet> frontier = new SimpleFrontier<>(ranking);

        final Solver solver = new ParallelSolver<BitSet>(
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

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

}
