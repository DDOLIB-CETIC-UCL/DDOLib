package org.ddolib.examples.misp;

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
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

public class MispTest {

    private static class MispBench extends ProblemTestBench<BitSet, NullType, MispProblem> {


        public MispBench() {
            super();
        }

        @Override
        protected List<MispProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "MISP").toString();
            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            MispProblem problem = MispMain.readFile(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<BitSet, NullType> configSolver(MispProblem problem) {
            SolverConfig<BitSet, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new MispRelax(problem);
            config.ranking = new MispRanking();
            config.flb = new MispFastLowerBound(problem);
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.restrictStrategy = new CostBased<>(config.ranking);
            config.relaxStrategy = new CostBased<>(config.ranking);
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            return config;
        }
    }

    @DisplayName("MISP")
    @TestFactory
    public Stream<DynamicTest> testMISP() {
        var bench = new MispBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }


}
