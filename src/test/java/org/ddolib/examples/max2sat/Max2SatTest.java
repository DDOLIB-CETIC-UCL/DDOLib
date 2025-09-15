package org.ddolib.examples.max2sat;

import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
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

public class Max2SatTest {

    private static class Max2SatBench extends ProblemTestBench<Max2SatState, NullType, Max2SatProblem> {

        @Override
        protected List<Max2SatProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "Max2Sat").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            Max2SatProblem problem = Max2SatIO.readInstance(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<Max2SatState, NullType> configSolver(Max2SatProblem problem) {
            SolverConfig<Max2SatState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new Max2SatRelax(problem);
            config.ranking = new Max2SatRanking();
            config.fub = new Max2SatFastUpperBound(problem);
            config.restrictStrategy = new CostBased<>(config.ranking);
            config.relaxStrategy = new CostBased<>(config.ranking);
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

            return config;
        }
    }

    @DisplayName("Max2Sat")
    @TestFactory
    public Stream<DynamicTest> testMax2Sat() {
        var bench = new Max2SatBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        return bench.generateTests();
    }


}
