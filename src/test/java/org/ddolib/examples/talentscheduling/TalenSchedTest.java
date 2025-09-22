package org.ddolib.examples.talentscheduling;

import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.lang.model.type.NullType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TalenSchedTest {

    private static class TSBench extends ProblemTestBench<TSState, NullType, TSProblem> {

        public TSBench() {
            super();
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
        protected SolverConfig<TSState, NullType> configSolver(TSProblem problem) {
            SolverConfig<TSState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new TSRelax(problem);
            config.ranking = new TSRanking();
            config.flb = new TSFastLowerBound(problem);

            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

            return config;
        }
    }

    @DisplayName("Talent Scheduling")
    @TestFactory
    public Stream<DynamicTest> testMCP() {
        var bench = new TSBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }

}
