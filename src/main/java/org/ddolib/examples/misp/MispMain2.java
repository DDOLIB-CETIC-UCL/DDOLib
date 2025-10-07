package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solve;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

public final class MispMain2 {


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


    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.misp.MispMain -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.
     */
    public static void main(String[] args) throws IOException {
        final String file = Paths.get("data", "MISP", "tadpole_4_2.dot").toString();

        DdoModel<BitSet> model = new DdoModel<>(){
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
            public MispRelax relaxation() {
                return new MispRelax(problem);
            }

            @Override
            public MispRanking ranking() {
                return new MispRanking();
            }

            @Override
            public DominanceChecker<BitSet> dominance() {
                return new SimpleDominanceChecker<>(new MispDominance(), problem.nbVars());
            }
        };

        Solve<BitSet> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

        solve.onSolution(stats);
    }

}
