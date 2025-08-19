package org.ddolib.examples.ddo.misp;

import org.ddolib.common.dominance.DefaultDominanceChecker;
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
import org.ddolib.examples.ddo.mks.*;
import org.ddolib.modeling.FastUpperBound;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ddolib.examples.ddo.mks.MKSMain.readInstance;
import static org.ddolib.factory.Solvers.sequentialSolver;

public class MispMeasureFrontier {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final String output = args[1];

        Map<ClusterStrat, String> stratNameMap = new HashMap<>();
        stratNameMap.put(ClusterStrat.Cost, "Cost");
        stratNameMap.put(ClusterStrat.GHP, "GHP");
        stratNameMap.put(ClusterStrat.GHPMD, "GHPMD");
        stratNameMap.put(ClusterStrat.GHPMDP, "GHPMDP");
        stratNameMap.put(ClusterStrat.GHPMDPMD, "GHPMDPMD");

        final MispProblem problem = MispMain.readFile(instance);
        final MispRelax relax = new MispRelax(problem);
        final MispRanking ranking = new MispRanking();
        FixedWidth<BitSet> width;
        VariableHeuristic<BitSet> varh;
        DefaultDominanceChecker<BitSet> dominance;
        Frontier<BitSet> frontier;
        final StateDistance<BitSet> distance = new MispDistance();
        final StateCoordinates<BitSet> coordinates = new DefaultStateCoordinates<>();
        final FastUpperBound<BitSet> fub = new MispFastUpperBound(problem);

        final String optimal = problem.optimal.isPresent() ? problem.optimal.get().toString() : "";

        FileWriter writer = new FileWriter(output);

        StringBuilder csvString;
        for (ClusterStrat relaxStrat : stratNameMap.keySet()) {
            for (int maxWidth = 100; maxWidth <= 100; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.5))) {
                List<Integer> seeds;
                if (relaxStrat == ClusterStrat.Cost || relaxStrat == ClusterStrat.Kmeans) {
                    seeds = List.of(684651);
                } else {
                    seeds = List.of(1323438797, 132343, 54646);
                }
                for (int seed: seeds) {
                    dominance = new DefaultDominanceChecker<>();
                    varh = new DefaultVariableHeuristic<>();
                    frontier = new SimpleFrontier<>(ranking, CutSetType.Frontier);
                    csvString = new StringBuilder();
                    width = new FixedWidth<>(maxWidth);
                    Solver solver = sequentialSolver(
                            problem,
                            relax,
                            varh,
                            ranking,
                            width,
                            frontier,
                            fub,
                            dominance,
                            900,
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
                    csvString.append("Misp").append(";");
                    csvString.append(stratNameMap.get(relaxStrat)).append(";");
                    csvString.append(stratNameMap.get(relaxStrat)).append(";");
                    csvString.append("symDiff").append(";");
                    csvString.append("").append(";");
                    csvString.append(stats.runTimeMS()).append(";");
                    csvString.append(stats.Gap()).append(";");
                    csvString.append(stats.SearchStatus()).append(";");
                    csvString.append(solver.bestValue().get()).append(";");
                    csvString.append(optimal).append(";");
                    csvString.append("").append(";");
                    csvString.append(stats.nbIterations()).append(";");
		            csvString.append("frontier").append(";");
		            csvString.append(true).append(";");
		            csvString.append(true).append("\n");
                    writer.write(csvString.toString());
		    writer.flush();
		    System.gc();
                }

            }
        }
        writer.close();
    }
}
