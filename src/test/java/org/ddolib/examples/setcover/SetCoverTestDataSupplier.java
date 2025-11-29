package org.ddolib.examples.setcover;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.examples.knapsack.*;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SetCoverTestDataSupplier extends TestDataSupplier<SetCoverState, SetCoverProblem> {

    private final Path dir;

    public SetCoverTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<SetCoverProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new SetCoverProblem(filePath.toString(), false);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DdoModel<SetCoverState> model(SetCoverProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<SetCoverState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<SetCoverState> lowerBound() {
                return new DefaultFastLowerBound<>();
            }

            @Override
            public DominanceChecker<SetCoverState> dominance() {
                return new SimpleDominanceChecker<>(new SetCoverDominance(), problem.nbVars());
            }

            @Override
            public VariableHeuristic<SetCoverState> variableHeuristic() {
                return new SetCoverHeuristic(problem);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public Relaxation<SetCoverState> relaxation() {
                return new SetCoverRelax();
            }

            @Override
            public StateRanking<SetCoverState> ranking() {
                return new SetCoverRanking();
            }
        };
    }
}
