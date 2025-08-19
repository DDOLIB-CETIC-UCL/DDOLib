package org.ddolib.examples.ddo.smic;

import org.ddolib.common.dominance.DominanceChecker;
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
import org.ddolib.ddo.implem.heuristics.DefaultStateCoordinates;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ddolib.examples.ddo.smic.SMICMain.readProblem;
import static org.ddolib.factory.Solvers.relaxationSolver;
import static org.ddolib.factory.Solvers.sequentialSolver;

public class SMICMeasureRelaxation {

    public static void main(String[] args) throws IOException, Exception {
        final String instance = args[0];
        final String output = args[1];

        Map<ClusterStrat, String> stratNameMap = new HashMap<>();
        stratNameMap.put(ClusterStrat.Cost, "Cost");
        stratNameMap.put(ClusterStrat.GHP, "GHP");
        stratNameMap.put(ClusterStrat.GHPMD, "GHPMD");
        stratNameMap.put(ClusterStrat.GHPMDP, "GHPMDP");
        stratNameMap.put(ClusterStrat.GHPMDPMD, "GHPMDPMD");

        final SMICProblem problem = readProblem(instance);
        final SMICRelax relax = new SMICRelax(problem);
        final SMICRanking ranking = new SMICRanking();
        FixedWidth<SMICState> width;
        VariableHeuristic<SMICState> varh;
        final Frontier<SMICState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final StateDistance<SMICState> distance = new SMICDistance();
        final StateCoordinates<SMICState> coordinates = new DefaultStateCoordinates<>();
        DominanceChecker<SMICState, Integer> dominance;

        FileWriter writer = new FileWriter(output);

        StringBuilder csvString;
        for (ClusterStrat relaxStrat : stratNameMap.keySet()) {
            for (int maxWidth = 2; maxWidth <= 1000; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.5))) {
                List<Integer> seeds;
                if (relaxStrat == ClusterStrat.Cost || relaxStrat == ClusterStrat.Kmeans) {
                    seeds = List.of(684651);
                } else {
                    seeds = List.of(1323438797, 132343, 54646);
                }
                for (int seed: seeds) {
                    varh = new DefaultVariableHeuristic<>();
                    csvString = new StringBuilder();
                    // System.out.print(maxWidth + ", ");
                    width = new FixedWidth<>(maxWidth);
                    dominance = new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
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
                    System.out.println(stats);
                    // System.out.println(duration);

                    csvString.append(args[0]).append(";");
                    csvString.append("relaxation").append(";");
                    csvString.append(seed).append(";");
                    csvString.append(maxWidth).append(";");
                    csvString.append("SMIC").append(";");
                    csvString.append(stratNameMap.get(relaxStrat)).append(";");
                    csvString.append("").append(";");
                    csvString.append("jobSymDiffAll").append(";");
                    csvString.append("").append(";");
                    csvString.append(stats.runTimeMS()).append(";");
                    csvString.append(solver.bestValue().get()).append(";");
                    csvString.append("").append(";");
                    csvString.append(stats.nbIterations()).append(";");
		            csvString.append("lastExactLayer").append(";");
		            csvString.append(true).append(";");
		            csvString.append(false).append("\n");
                    writer.write(csvString.toString());
                }

            }
        }
        writer.close();
    }
}
