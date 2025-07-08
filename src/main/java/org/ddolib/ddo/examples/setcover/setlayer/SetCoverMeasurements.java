package org.ddolib.ddo.examples.setcover.setlayer;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultStateCoordinates;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.RelaxationSolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.ddolib.ddo.examples.setcover.setlayer.SetCover.readInstance;
import static org.ddolib.ddo.implem.solver.Solvers.relaxationSolver;

public class SetCoverMeasurements {

    public static void main(String[] args) throws IOException {

        File dir = new File("./data/SetCover/measurements/or_library/set_covering");
        // FilenameFilter filter = (dir1, name) -> name.endsWith(".txt");
        // File[] instances = dir.listFiles(filter);
        File[] instances = dir.listFiles();

        String header = "Name;Solver;Seed;MaxWidth;Model;VarHeuristic;MergeStrategy;DistanceFunction;Time(s);Objective";

        // String file = args[0];
        FileWriter writer = new FileWriter("tmp/setCoverElementClusterStats.csv", false);
        writer.write(header + "\n");

        for (File file : instances) {
            System.out.println(file.getName());
            SetCoverProblem problem = readInstance(file.getPath());
            final SetCoverRanking ranking = new SetCoverRanking();
            SetCoverRelax relax;

            FixedWidth<SetCoverState> width;
            final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            final StateDistance<SetCoverState> distance = new SetCoverDistance();
            final VariableHeuristic<SetCoverState> varh = new SetCoverHeuristics.FocusClosingElements(problem);
            final DefaultDominanceChecker<SetCoverState> dominance = new DefaultDominanceChecker<>();
            final DefaultStateCoordinates<SetCoverState> coordinates = new DefaultStateCoordinates<>();

            StringBuilder csvString;
            for (int maxWidth = 1; maxWidth < 10000; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.1))) {
                boolean isExact = false;
                for (int seed : List.of(54646)) { //, 8797, 132343)) {
                    csvString = new StringBuilder();
                    System.out.print(maxWidth + ", ");
                    relax = new SetCoverRelax();
                    width = new FixedWidth<>(maxWidth);
                    Solver solver = relaxationSolver(
                            problem,
                            relax,
                            varh,
                            ranking,
                            width,
                            frontier,
                            dominance,
                            RelaxationStrat.GHP,
                            distance,
                            coordinates,
                            seed);

                    long start = System.currentTimeMillis();
                    SearchStatistics stats = solver.maximize();
                    double duration = (System.currentTimeMillis() - start) / 1000.0;
                    System.out.println(duration);

                    csvString.append(file.getName()).append(";");
                    csvString.append("relax").append(";");
                    csvString.append(seed).append(";");
                    csvString.append(maxWidth).append(";");
                    csvString.append("element").append(";");
                    csvString.append("minCentrality").append(";");
                    csvString.append("GHPAvgDist").append(";");
                    csvString.append("symDif").append(";");
                    csvString.append(duration).append(";");
                    csvString.append(solver.bestValue().get()).append("\n");

                    writer.write(csvString.toString());

                }
            }
        }
        writer.close();

    }

}
