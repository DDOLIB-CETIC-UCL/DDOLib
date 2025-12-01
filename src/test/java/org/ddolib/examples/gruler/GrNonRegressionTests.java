package org.ddolib.examples.gruler;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Tag("non-regression")
public class GrNonRegressionTests {

    @DisplayName("G ruler: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonregressionGr() {
        var dataSupplier = new GrNonRegressionDataSupplier();
        var bench = new NonRegressionTestBench<>(dataSupplier);
        return bench.generateTests();
    }

    private static class GrNonRegressionDataSupplier extends GRTestDataSupplier {
        @Override
        protected List<GRProblem> generateProblems() {
            return IntStream.range(7, 11).mapToObj(GRProblem::new).toList();
        }
    }

}
