package org.ddolib.examples.ddo.talentscheduling;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.util.testbench.ProblemTestBench;
import org.ddolib.util.testbench.SolverConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TalenSchedTest {

    private static class TSBench extends ProblemTestBench<TSState, Integer, TSProblem> {

        public TSBench() {
            super(true, true, false);
        }

        @Override
        protected List<TSProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "TalentScheduling").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            TSProblem problem = TSMain.readFile(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<TSState, Integer> configSolver(TSProblem problem) {
            TSRelax relax = new TSRelax(problem);
            TSRanking ranking = new TSRanking();
            TSFastUpperBound fub = new TSFastUpperBound(problem);

            VariableHeuristic<TSState> varh = new DefaultVariableHeuristic<>();
            Frontier<TSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<TSState> dominanceChecker = new DefaultDominanceChecker<>();

            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominanceChecker);
        }
    }

    @DisplayName("Talent Scheduling")
    @TestFactory
    public Stream<DynamicTest> testMCP() {
        var bench = new TSBench();
        return bench.generateTests();
    }

}
