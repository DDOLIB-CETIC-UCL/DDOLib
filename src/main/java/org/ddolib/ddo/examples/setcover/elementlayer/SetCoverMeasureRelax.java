package org.ddolib.ddo.examples.setcover.elementlayer;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultStateCoordinates;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ddolib.ddo.examples.setcover.elementlayer.SetCover.readInstance;
import static org.ddolib.ddo.implem.solver.Solvers.relaxationSolver;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

public class SetCoverMeasureRelax {

    public static void main(String[] args) throws IOException {
        final String instance = args[0];
        final String output = args[1];

        Map<RelaxationStrat, String> stratNameMap = new HashMap<>();
        stratNameMap.put(RelaxationStrat.Cost, "Cost");
        stratNameMap.put(RelaxationStrat.GHP, "GHP");
        // stratNameMap.put(RelaxationStrat.Kmeans, "Kmeans");

        final SetCoverProblem problem = readInstance(instance);
        final SetCoverRelax relax = new SetCoverRelax();
        final SetCoverRanking ranking = new SetCoverRanking();
        FixedWidth<SetCoverState> width;
        VariableHeuristic<SetCoverState> varh;
        final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final StateDistance<SetCoverState> distance = new SetCoverDistance();
        final StateCoordinates<SetCoverState> coordinates = new DefaultStateCoordinates<>();

        FileWriter writer = new FileWriter(output);

        StringBuilder csvString;
        for (RelaxationStrat relaxStrat : stratNameMap.keySet()) {
            for (int maxWidth = 2; maxWidth < 500; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.5))) {
                for (int seed: List.of(1323438797, 132343, 54646)) {
                    varh = new SetCoverHeuristics.MinCentrality(problem);
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
                            relaxStrat,
                            distance,
                            coordinates,
                            seed);

                    SearchStatistics stats = solver.maximize();
                    System.out.println(stats);
                    // System.out.println(duration);

                    assert solver.bestValue().isPresent();

                    csvString.append(args[0]).append(";");
                    csvString.append("relaxation").append(";");
                    csvString.append(seed).append(";");
                    csvString.append(maxWidth).append(";");
                    csvString.append("SetCoverElement").append(";");
                    csvString.append(stratNameMap.get(relaxStrat)).append(";");
                    csvString.append("symDiff").append(";");
                    csvString.append("None").append(";");
                    csvString.append(stats.runTimeMS()).append(";");
                    csvString.append(solver.bestValue().get()).append(";");
                    csvString.append(stats.nbIterations()).append("\n");

                    writer.write(csvString.toString());
                }

            }
        }
        writer.close();
    }
}
