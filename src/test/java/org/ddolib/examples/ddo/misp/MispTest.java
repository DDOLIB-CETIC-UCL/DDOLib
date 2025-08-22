package org.ddolib.examples.ddo.misp;

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
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

public class MispTest {

    private static class MispBench extends ProblemTestBench<BitSet, Integer, MispProblem> {


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
        protected SolverConfig<BitSet, Integer> configSolver(MispProblem problem) {
            MispRelax relax = new MispRelax(problem);
            MispRanking ranking = new MispRanking();
            MispFastUpperBound fub = new MispFastUpperBound(problem);
            VariableHeuristic<BitSet> varh = new DefaultVariableHeuristic<>();
            Frontier<BitSet> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<BitSet> dominanceChecker = new DefaultDominanceChecker<>();
            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominanceChecker);
        }
    }

    @DisplayName("MISP")
    @TestFactory
    public Stream<DynamicTest> testMISP() {
        var bench = new MispBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        return bench.generateTests();
    }


}
