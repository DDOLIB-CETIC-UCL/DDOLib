package org.ddolib.examples.ddo.tsp;

import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.cache.SimpleCache;
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
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TSPTests {

    private static class TSPBench extends ProblemTestBench<TSPState, NullType, TSPProblem> {

        public TSPBench() {
            super();
        }

        @Override
        protected List<TSPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "TSP").toString();
            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        TSPInstance instance = new TSPInstance(filePath.toString());
                        TSPProblem problem = new TSPProblem(instance.distanceMatrix, instance.objective);
                        problem.setName(filePath.getFileName().toString());
                        return problem;
                    }).toList();
        }

        @Override
        protected SolverConfig<TSPState, NullType> configSolver(TSPProblem problem) {
            SolverConfig<TSPState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new TSPRelax(problem);
            config.ranking = new TSPRanking();
            config.fub = new TSPFastUpperBound(problem);
            config.width = new FixedWidth<>(500);
            config.varh = new DefaultVariableHeuristic<>();
            config.cache = new SimpleCache<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

            return config;
        }
    }

    @DisplayName("TSP")
    @TestFactory
    public Stream<DynamicTest> testTSP() {
        var bench = new TSPBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        return bench.generateTests();
    }
}
