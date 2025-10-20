package org.ddolib.examples.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.DebugLevel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.VerbosityLevel;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class SMICTest {

    private static class SMICBench extends ProblemTestBench<SMICState, SMICProblem> {

        public SMICBench() {
            super();
        }

        @Override
        protected List<SMICProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "SMIC").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return SMICDdoMain.readProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<SMICState> configSolver(SMICProblem problem) {
            SolverConfig<SMICState> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new SMICRelax(problem);
            config.ranking = new SMICRanking();
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.flb = new SMICFastLowerBound(problem);
            config.dominance = new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            config.verbosityLevel = VerbosityLevel.SILENT;

            return config;
        }

        @Override
        protected Solver solverForTests(SolverConfig<SMICState> config) {
            config.width = new FixedWidth<>(100);
            return new SequentialSolver<>(config);
        }

        @Override
        protected DdoModel<SMICState> model(SMICProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<SMICState> problem() {
                    return problem;
                }

                @Override
                public SMICRelax relaxation() {
                    return new SMICRelax(problem);
                }

                @Override
                public SMICRanking ranking() {
                    return new SMICRanking();
                }

                @Override
                public SMICFastLowerBound lowerBound() {
                    return new SMICFastLowerBound(problem);
                }

                @Override
                public DominanceChecker<SMICState> dominance() {
                    return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
                }


                @Override
                public VerbosityLevel verbosityLevel() {
                    return VerbosityLevel.SILENT;
                }

                @Override
                public DebugLevel debugMode() {
                    return DebugLevel.ON;
                }
            };
        }
    }

    @DisplayName("SMIC")
    @TestFactory
    public Stream<DynamicTest> testSMIC() {
        var bench = new SMICBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}
