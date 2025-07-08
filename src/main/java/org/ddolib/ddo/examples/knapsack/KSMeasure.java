package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import static org.ddolib.ddo.examples.knapsack.KSMain.readInstance;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

public class KSMeasure {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final String output = args[1];

        Map<RelaxationStrat, String> stratNameMap = new HashMap<>();
        stratNameMap.put(RelaxationStrat.Cost, "Cost");
        stratNameMap.put(RelaxationStrat.GHP, "GHP");
        stratNameMap.put(RelaxationStrat.Kmeans, "Kmeans");

        final KSProblem problem = readInstance(instance);
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        FixedWidth<Integer> width;
        VariableHeuristic<Integer> varh;
        SimpleDominanceChecker<Integer, Integer> dominance;
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final StateDistance<Integer> distance = new KSDistance();
        final StateCoordinates<Integer> coordinates = new KSCoordinates();
        final int[] seeds = {546645, 684565, 68464};

        FileWriter writer = new FileWriter(output);

        StringBuilder csvString;
        for (RelaxationStrat relaxStrat : stratNameMap.keySet()) {
            for (int maxWidth = 2; maxWidth < 500; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.1))) {
                for (int seed: List.of(1323438797, 132343, 54646)) {
                    dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
                    varh = new DefaultVariableHeuristic<Integer>();
                    csvString = new StringBuilder();
                    // System.out.print(maxWidth + ", ");
                    width = new FixedWidth<>(maxWidth);
                    Solver solver = sequentialSolver(
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
                    System.out.println(stats);
                    // System.out.println(duration);

                    csvString.append(args[0]).append(";");
                    csvString.append("sequential").append(";");
                    csvString.append(seed).append(";");
                    csvString.append(maxWidth).append(";");
                    csvString.append("Knapsack").append(";");
                    csvString.append(stratNameMap.get(relaxStrat)).append(";");
                    csvString.append("capaDiff").append(";");
                    csvString.append("capacity").append(";");
                    csvString.append(stats.runTimeMS()).append(";");
                    csvString.append(solver.bestValue().get()).append(";");
                    csvString.append(problem.optimal).append(";");
                    csvString.append(stats.nbIterations()).append("\n");

                    writer.write(csvString.toString());
                }

            }
        }
        writer.close();
    }
}
