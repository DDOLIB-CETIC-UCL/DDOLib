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

import static org.ddolib.ddo.examples.Knapsack.readInstance;

public class KnapsackMeasurement {

    public static void main(String[] args) throws IOException {
        File dir = new File("./data/Knapsack/Nafar_2024");
        FilenameFilter filter = (dir1, name) -> name.endsWith(".txt");
        File[] instances = dir.listFiles(filter);

        String header = "Name;Solver;Seed;MaxWidth;Model;VarHeuristic;MergeStrategy;DistanceFunction;CoordFunction;Time(s);Objective";
        FileWriter writer = new FileWriter("tmp/knapsackStats.csv", false);

        RelaxationType[] relaxationTypes = new RelaxationType[] {
                RelaxationType.Cost,
                RelaxationType.Kmeans,
                RelaxationType.KClosest
        };

        Map<RelaxationType, String> rtMap = new HashMap<>();
        rtMap.put(RelaxationType.Cost, "Cost");
        rtMap.put(RelaxationType.Kmeans, "KMeans");
        rtMap.put(RelaxationType.KClosest, "KClosest");

        for (File instance : instances) {
            final KnapsackProblem problem = readInstance(instance.getPath());
            final KnapsackRelax relax = new KnapsackRelax(problem);
            final KnapsackRanking ranking = new KnapsackRanking();
            final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
            final Frontier<Integer> frontier = new SimpleFrontier<>(ranking);
            final StateDistance<Integer> distance = new KnapsackDistance();
            final StateCoordinates<Integer> coord = new KnapsackCoordinates();

            FixedWidth<Integer> width;

            StringBuilder csvString;
            for (RelaxationType relaxationType : relaxationTypes) {
                for (int maxWidth = 1; maxWidth < 500; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.1))) {
                    for (int seed: List.of(54646, 8797, 132343)) {
                        csvString = new StringBuilder();
                        System.out.print(maxWidth + ", ");
                        width = new FixedWidth<>(maxWidth);
                        Solver solver = new RelaxationSolver<>(
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
                        System.out.println(duration);

                        csvString.append(instance.getName()).append(";");
                        csvString.append("relax").append(";");
                        csvString.append(seed).append(";");
                        csvString.append(maxWidth).append(";");
                        csvString.append("Knapsack").append(";");
                        csvString.append("default").append(";");
                        csvString.append(rtMap.get(relaxationType)).append(";");
                        csvString.append("capaDiff").append(";");
                        csvString.append("capacity").append(";");
                        csvString.append(duration).append(";");
                        csvString.append(solver.bestValue().get()).append("\n");

                        writer.write(csvString.toString());
                    }
                }
            }

        }
    }

}
