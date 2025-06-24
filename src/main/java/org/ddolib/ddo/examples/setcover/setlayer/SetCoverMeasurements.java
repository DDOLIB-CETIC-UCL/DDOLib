package org.ddolib.ddo.examples.setcover.setlayer;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.RelaxationType;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.setcover.setlayer.SetCoverIntersectionDistance;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.RelaxationSolver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ddolib.ddo.examples.setcover.setlayer.SetCover.readInstance;

public class SetCoverMeasurements {

    public static void main(String[] args) throws IOException {

        String[] files = {
                "data/SetCover/1id_problem/tripode",
                "data/SetCover/generated/n_6_b_5_d_5",
                "data/SetCover/generated/n_10_b_8_d_3",
                "data/SetCover/1id_problem/abilene",
                "data/SetCover/1id_problem/ai3",
                "data/SetCover/1id_problem/gblnet",
                "data/SetCover/1id_problem/aarnet"};

        String header = "Name;Solver;Seed;MaxWidth;Model;VarHeuristic;MergeStrategy;DistanceFunction;Time(s);Objective";

        FileWriter writer = new FileWriter("tmp/setCoverSetClusterStats.csv", false);

        for (String file: files) {
            SetCoverProblem problem = readInstance(file);
            final SetCoverRanking ranking = new SetCoverRanking();
            SetCoverRelax relax;
            FixedWidth<SetCoverState> width;
            final Frontier<SetCoverState> frontier = new SimpleFrontier<>(ranking);
            final StateDistance<SetCoverState> distance = new SetCoverDistance();
            final VariableHeuristic<SetCoverState> varh = new SetCoverHeuristics.FocusClosingElements(problem);

            StringBuilder csvString;
            for (int maxWidth = 1; maxWidth < 10000; maxWidth = maxWidth + Math.max(1, (int) (maxWidth * 0.1))) {
                for (int seed: List.of(54646, 8797, 132343)) {
                    csvString = new StringBuilder();
                    System.out.print(maxWidth + ", ");
                    relax = new SetCoverRelax();
                    width = new FixedWidth<>(maxWidth);
                    Solver solver = new RelaxationSolver<>(
                            RelaxationType.MinDist,
                            problem,
                            relax,
                            varh,
                            ranking,
                            distance,
                            null,
                            width,
                            frontier,
                            seed);

                    long start = System.currentTimeMillis();
                    SearchStatistics stats = solver.maximize();
                    double duration = (System.currentTimeMillis() - start) / 1000.0;
                    System.out.println(duration);

                    csvString.append(file).append(";");
                    csvString.append("relax").append(";");
                    csvString.append(seed).append(";");
                    csvString.append(maxWidth).append(";");
                    csvString.append("set").append(";");
                    csvString.append("focusClosing").append(";");
                    csvString.append("MinDist").append(";");
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
