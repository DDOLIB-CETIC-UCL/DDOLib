package org.ddolib.ddo.examples;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.RelaxationType;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.Knapsack.*;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.RelaxationSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import static org.ddolib.ddo.examples.Knapsack.readInstance;

public class KnapsackMeasurement {

    public static void main(String[] args) throws IOException {
        File dir = new File("./data/Knapsack/Nafar_2024");
        FilenameFilter filter = (dir1, name) -> name.endsWith(".txt");
        File[] instances = dir.listFiles(filter);

        String header = "Name;Solver;Seed;MaxWidth;Model;VarHeuristic;MergeStrategy;DistanceFunction;CoordFunction;Time(s);Objective;Optimal;nbIterations\n";
        FileWriter writer = new FileWriter("tmp/knapsackStats.csv", false);
        writer.write(header);

        RelaxationType[] rtTested = new RelaxationType[] {
                RelaxationType.Cost,
                RelaxationType.Kmeans,
                // RelaxationType.KClosest,
                RelaxationType.GHP
        };

        Map<RelaxationType, String> rtMap = new HashMap<>();
        rtMap.put(RelaxationType.Cost, "Cost");
        rtMap.put(RelaxationType.Kmeans, "KMeansSmile");
        rtMap.put(RelaxationType.KClosest, "KClosest");
        rtMap.put(RelaxationType.GHP, "GHP");

        // for (File instance : List.of(new File("./data/Knapsack/Nafar_2024/KP_45.txt"))) {
        for (File instance : instances) {
            System.out.println(instance.getName());
            final KnapsackProblem problem = readInstance(instance.getPath());
            final KnapsackRelax relax = new KnapsackRelax(problem);
            final KnapsackRanking ranking = new KnapsackRanking();
            final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
            final Frontier<Integer> frontier = new SimpleFrontier<>(ranking);
            final StateDistance<Integer> distance = new KnapsackDistance();
            final StateCoordinates<Integer> coord = new KnapsackCoordinates();

            FixedWidth<Integer> width;

            StringBuilder csvString;
            for (RelaxationType relaxationType : rtTested) {
                for (int maxWidth = 2; maxWidth < 500; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.1))) {
                    for (int seed: List.of(132343)) { //, 8797, 132343, 54646)) {
                        csvString = new StringBuilder();
                        // System.out.print(maxWidth + ", ");
                        width = new FixedWidth<>(maxWidth);
                        Solver solver = new SequentialSolver<>(
                                relaxationType,
                                problem,
                                relax,
                                varh,
                                ranking,
                                distance,
                                coord,
                                width,
                                frontier,
                                seed);

                        long start = System.currentTimeMillis();
                        SearchStatistics stats = solver.maximize();
                        double duration = (System.currentTimeMillis() - start) / 1000.0;
                        // System.out.println(duration);

                        csvString.append(instance.getName()).append(";");
                        csvString.append("sequential").append(";");
                        csvString.append(seed).append(";");
                        csvString.append(maxWidth).append(";");
                        csvString.append("Knapsack").append(";");
                        csvString.append("default").append(";");
                        csvString.append(rtMap.get(relaxationType)).append(";");
                        csvString.append("capaDiff").append(";");
                        csvString.append("capacity").append(";");
                        csvString.append(duration).append(";");
                        csvString.append(solver.bestValue().get()).append(";");
                        csvString.append(stats.nbIterations()).append("\n");
                        csvString.append(problem.optimal).append("\n");


                        writer.write(csvString.toString());
                    }
                }
            }

        }
        writer.close();
    }

}
