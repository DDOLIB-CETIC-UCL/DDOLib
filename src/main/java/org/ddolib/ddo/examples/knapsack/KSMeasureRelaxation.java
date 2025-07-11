package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import static org.ddolib.ddo.examples.knapsack.KSMain.readInstance;
import static org.ddolib.ddo.implem.solver.Solvers.relaxationSolver;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

public class KSMeasureRelaxation {

    public static void main(String[] args) throws IOException {

        File dir = new File("./data/Knapsack/Nafar_2024");
        FilenameFilter filter = (dir1, name) -> name.endsWith(".txt");
        File[] instances = dir.listFiles(filter);

        String header = "Name;Solver;Seed;MaxWidth;Model;VarHeuristic;MergeStrategy;DistanceFunction;CoordFunction;" +
                "Time(ms);Objective;Optimal;nbIterations;RootFUB;Dominance;FastUpperBound\n";
        FileWriter writer = new FileWriter("tmp/knapsackStats.csv", false);
        writer.write(header);

        // final String instance = args[0];
        // final String output = args[1];

        Map<RelaxationStrat, String> stratNameMap = new HashMap<>();
        stratNameMap.put(RelaxationStrat.Cost, "Cost");
        stratNameMap.put(RelaxationStrat.GHP, "GHP");
        stratNameMap.put(RelaxationStrat.Kmeans, "Kmeans");


        for (File file: instances) {
            System.out.println(file.getName());
            final KSProblem problem = readInstance(file.getPath());
            final KSRelax relax = new KSRelax(problem);
            final KSRanking ranking = new KSRanking();
            FixedWidth<Integer> width;
            VariableHeuristic<Integer> varh;
            SimpleDominanceChecker<Integer, Integer> dominance;
            final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            final StateDistance<Integer> distance = new KSDistance();
            final StateCoordinates<Integer> coordinates = new KSCoordinates();
            final int[] seeds = {546645}; //, 684565, 68464};

            StringBuilder csvString;

            final HashSet<Integer> varSet = new HashSet<>();
            for (int i = 0; i < problem.nbVars(); i++) {
                varSet.add(i);
            }
            double fub = relax.fastUpperBound(problem.initialState(), varSet);

            for (RelaxationStrat relaxStrat : stratNameMap.keySet()) {
                for (int maxWidth = 2; maxWidth < 500; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.1))) {
                    for (int seed : seeds) {
                        dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
                        varh = new DefaultVariableHeuristic<Integer>();
                        csvString = new StringBuilder();
                        // System.out.print(maxWidth + ", ");
                        width = new FixedWidth<>(maxWidth);
                        Solver solver = relaxationSolver(
                                problem,
                                relax,
                                varh,
                                ranking,
                                width,
                                frontier,
                                dominance,
                                relaxStrat,
                                distance,
                                coordinates,
                                seed);

                        SearchStatistics stats = solver.maximize();
                        // System.out.println(stats);
                        // System.out.println(duration);

                        csvString.append(file.getName()).append(";"); // Name
                        csvString.append("relaxation").append(";"); // solver
                        csvString.append(seed).append(";"); // Seed
                        csvString.append(maxWidth).append(";"); // maxWidth
                        csvString.append("Knapsack").append(";"); // problem
                        csvString.append(stratNameMap.get(relaxStrat)).append(";"); // relaxStrat
                        csvString.append("capaDiff").append(";"); // distance function
                        csvString.append("capacity").append(";"); // coordinate function
                        csvString.append(stats.runTimeMS()).append(";"); // runtime
                        csvString.append(solver.bestValue().get()).append(";"); // Objective
                        csvString.append(problem.optimal).append(";"); // optimal value
                        csvString.append(stats.nbIterations()).append(";"); // nb iterations
                        csvString.append(fub).append(";"); // value of fast upper bound
                        csvString.append("true").append(";"); // Use dominance ?
                        csvString.append("true").append("\n"); // Use fast upper bound ?

                        writer.write(csvString.toString());
                    }
                }
            }
        }
        writer.close();
    }
}
