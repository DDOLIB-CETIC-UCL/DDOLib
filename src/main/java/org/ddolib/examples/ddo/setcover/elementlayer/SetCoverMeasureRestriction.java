package org.ddolib.examples.ddo.setcover.elementlayer;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.ClusterStrat;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
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

import static org.ddolib.examples.ddo.setcover.elementlayer.SetCover.readInstance;
import static org.ddolib.factory.Solvers.relaxationSolver;
import static org.ddolib.factory.Solvers.restrictionSolver;

public class SetCoverMeasureRestriction {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final String output = args[1];

        Map<ClusterStrat, String> stratNameMap = new HashMap<>();
        stratNameMap.put(ClusterStrat.Cost, "Cost");
        stratNameMap.put(ClusterStrat.GHP, "GHP");
        stratNameMap.put(ClusterStrat.GHPMD, "GHPMD");
        stratNameMap.put(ClusterStrat.GHPMDP, "GHPMDP");
        stratNameMap.put(ClusterStrat.GHPMDPMD, "GHPMDPMD");

        final SetCoverProblem problem = readInstance(instance);
        final SetCoverRelax relax = new SetCoverRelax();
        final SetCoverRanking ranking = new SetCoverRanking();
        FixedWidth<SetCoverState> width;
        VariableHeuristic<SetCoverState> varh;
        DominanceChecker<SetCoverState, Integer> dominance;
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final StateDistance<SetCoverState> distance = new SetCoverDistance();
        final StateCoordinates<SetCoverState> coordinates = new DefaultStateCoordinates<>();

        FileWriter writer = new FileWriter(output);

        StringBuilder csvString;
        for (ClusterStrat restrictionStrat : stratNameMap.keySet()) {
            for (int maxWidth = 2; maxWidth <= 1000; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.5))) {
                List<Integer> seeds;
                if (restrictionStrat == ClusterStrat.Cost || restrictionStrat == ClusterStrat.Kmeans) {
                    seeds = List.of(684651);
                } else {
                    seeds = List.of(1323438797, 132343, 54646);
                }
                for (int seed: seeds) {
                    dominance = new DefaultDominanceChecker<>();
                    varh = new SetCoverHeuristics.MinCentrality(problem);
                    csvString = new StringBuilder();
                    // System.out.print(maxWidth + ", ");
                    width = new FixedWidth<>(maxWidth);
                    Solver solver = restrictionSolver(
                            problem,
                            relax,
                            varh,
                            ranking,
                            width,
                            frontier,
                            dominance,
                            restrictionStrat,
                            distance,
                            coordinates,
                            seed);

                    SearchStatistics stats = solver.maximize();
                    System.out.println(stats);
                    // System.out.println(duration);

                    csvString.append(args[0]).append(";");
                    csvString.append("restriction").append(";");
                    csvString.append(seed).append(";");
                    csvString.append(maxWidth).append(";");
                    csvString.append("SetCoverElement").append(";");
                    csvString.append("").append(";");
                    csvString.append(stratNameMap.get(restrictionStrat)).append(";");
                    csvString.append("symDiff").append(";");
                    csvString.append("").append(";");
                    csvString.append(stats.runTimeMS()).append(";");
		    csvString.append(stats.Gap()).append(";");
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
