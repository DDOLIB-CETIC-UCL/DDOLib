package org.ddolib.examples.mks;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.maximumcoverage.MaxCoverProblem;
import org.ddolib.examples.maximumcoverage.MaxCoverTest;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MKSTest {
    @DisplayName("MKS")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var dataSupplier = new MKSTestDataSupplier(Path.of("src", "test", "resources", "MKS"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = false;
        return bench.generateTests();
    }

}
