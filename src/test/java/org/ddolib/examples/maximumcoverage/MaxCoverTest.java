package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.ProblemTestBench;
import org.ddolib.util.verbosity.VerbosityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class MaxCoverTest {

    @DisplayName("MaxCover")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var dataSupplier = new MaxCoverTestDataSupplier(Path.of("src", "test", "resources", "MaxCover"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = true;
        return bench.generateTests();
    }
}
