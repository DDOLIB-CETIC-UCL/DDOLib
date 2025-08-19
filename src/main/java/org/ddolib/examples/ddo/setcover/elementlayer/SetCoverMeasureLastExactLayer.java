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
import static org.ddolib.factory.Solvers.sequentialSolver;

public class SetCoverMeasureLastExactLayer {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final String output = args[1];
        final int maxWidth = Integer.parseInt(args[2]);
        final int timelimit = Integer.parseInt(args[3]);

        Map<ClusterStrat, String> stratNameMap = new HashMap<>();
        stratNameMap.put(ClusterStrat.Cost, "Cost");
        stratNameMap.put(ClusterStrat.GHPMDPMD, "GHPMDPMD");

        final SetCoverProblem problem = readInstance(instance);
        final SetCoverRelax relax = new SetCoverRelax();
        final SetCoverRanking ranking = new SetCoverRanking();
        FixedWidth<SetCoverState> width;
        VariableHeuristic<SetCoverState> varh;
        Frontier<SetCoverState> frontier;
        final StateDistance<SetCoverState> distance = new SetCoverDistance();
        final StateCoordinates<SetCoverState> coordinates = new DefaultStateCoordinates<>();

        FileWriter writer = new FileWriter(output);

        StringBuilder csvString;
        for (ClusterStrat relaxStrat : stratNameMap.keySet()) {
                List<Integer> seeds;
                if (relaxStrat == ClusterStrat.Cost || relaxStrat == ClusterStrat.Kmeans) {
                    seeds = List.of(684651);
                } else {
                    seeds = List.of(1323438797, 132343, 54646);
                }
                for (int seed: seeds) {
                    frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
                    varh = new SetCoverHeuristics.MinCentrality(problem);
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
                            new DefaultDominanceChecker<>(),
                            timelimit,
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
                    csvString.append("SetCoverElement").append(";");
                    csvString.append(stratNameMap.get(relaxStrat)).append(";");
                    csvString.append(stratNameMap.get(relaxStrat)).append(";");
                    csvString.append("symDiff").append(";");
                    csvString.append("").append(";");
                    csvString.append(stats.runTimeMS()).append(";");
		            csvString.append(stats.Gap()).append(";");
                    csvString.append(stats.SearchStatus()).append(";");
                    csvString.append(solver.bestValue().get()).append(";");
                    csvString.append("").append(";");
                    csvString.append(stats.nbIterations()).append(";");
		            csvString.append("lastExactLayer").append(";");
                    csvString.append(timelimit).append(";");
		            csvString.append(true).append(";");
		            csvString.append(false).append("\n");
                    writer.write(csvString.toString());
                    writer.flush();
                }
        }
        writer.close();
    }
}
