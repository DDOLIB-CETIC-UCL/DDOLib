package org.ddolib.examples.ddo.knapsack;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.ClusterStrat;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.examples.ddo.mks.*;
import org.ddolib.modeling.FastUpperBound;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ddolib.examples.ddo.knapsack.KSMain.readInstance;
import static org.ddolib.factory.Solvers.sequentialSolver;

public class KSMeasureFrontier {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final String output = args[1];

        Map<ClusterStrat, String> stratNameMap = new HashMap<>();
        stratNameMap.put(ClusterStrat.Cost, "Cost");
        // stratNameMap.put(ClusterStrat.GHP, "GHP");
        // stratNameMap.put(ClusterStrat.GHPMD, "GHPMD");
        // stratNameMap.put(ClusterStrat.GHPMDP, "GHPMDP");
        stratNameMap.put(ClusterStrat.GHPMDPMD, "GHPMDPMD");
        stratNameMap.put(ClusterStrat.Kmeans, "Kmeans");

        final KSProblem problem = readInstance(instance);
        final KSRelax relax = new KSRelax();
        final KSRanking ranking = new KSRanking();
        FixedWidth<Integer> width;
        VariableHeuristic<Integer> varh;
        SimpleDominanceChecker<Integer, Integer> dominance;
        Frontier<Integer> frontier;
        final StateDistance<Integer> distance = new KSDistance();
        final StateCoordinates<Integer> coordinates = new KSCoordinates();
        final FastUpperBound<Integer> fub = new KSFastUpperBound(problem);

        FileWriter writer = new FileWriter(output);

        StringBuilder csvString;
        for (ClusterStrat relaxStrat : stratNameMap.keySet()) {
            for (int maxWidth = 300; maxWidth <= 300; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.5))) {
                List<Integer> seeds;
                if (relaxStrat == ClusterStrat.Cost || relaxStrat == ClusterStrat.Kmeans) {
                    seeds = List.of(684651);
                } else {
                    seeds = List.of(1323438797, 132343, 54646);
                }
                for (int seed: seeds) {
                    dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
                    varh = new DefaultVariableHeuristic<>();
                    csvString = new StringBuilder();
                    // System.out.print(maxWidth + ", ");
                    width = new FixedWidth<>(maxWidth);
                    frontier = new SimpleFrontier<>(ranking, CutSetType.Frontier);
                    Solver solver = sequentialSolver(
                            problem,
                            relax,
                            varh,
                            ranking,
                            width,
                            frontier,
                            fub,
                            dominance,
                            100,
                            0.0,
                            relaxStrat,
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
                    csvString.append(stratNameMap.get(relaxStrat)).append(";");
                    csvString.append("capaDiff").append(";");
                    csvString.append("capacity").append(";");
                    csvString.append(stats.runTimeMS()).append(";");
                    csvString.append(stats.Gap()).append(";");
                    csvString.append(stats.SearchStatus()).append(";");
                    csvString.append(solver.bestValue().get()).append(";");
                    csvString.append(problem.optimal).append(";");
                    csvString.append("").append(";");
                    csvString.append(stats.nbIterations()).append(";");
		            csvString.append("frontier").append(";");
		            csvString.append(true).append(";");
		            csvString.append(true).append("\n");
                    writer.write(csvString.toString());
                }

            }
        }
        writer.close();
    }
}
