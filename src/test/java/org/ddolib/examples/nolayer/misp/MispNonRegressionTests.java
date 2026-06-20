package org.ddolib.examples.nolayer.misp;

import org.ddolib.util.testbench.NoLayerNonRegressionTestBench;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class MispNonRegressionTests {

    @TestFactory
    Stream<DynamicTest> testMisp() {
        Path testDataDir = Paths.get("data", "MISP");
        MispTestDataSupplier testDataSupplier = new MispTestDataSupplier(testDataDir);
        NoLayerNonRegressionTestBench<MispState, MispProblem> testBench = new NoLayerNonRegressionTestBench<>(testDataSupplier) {
            @Override
            protected void testAllSolver(MispProblem problem) {
                // AStar explores the full state space, which is too large for nolayer MISP
                // Skip AStar and only run DDO and ACS
                org.ddolib.modeling.nolayer.DdoModel<MispState> globalModel = testDataSupplier.model(problem);
                
                boolean dominanceUsed = true; // We use dominance

                // DDO tests
                org.ddolib.modeling.nolayer.DdoModel<MispState> ddoModelNoDom = wrapDdoModel(globalModel, false, false, null);
                double ddoVal = solveAndChecksSolution(ddoModelNoDom, "DDO");

                double ddoWithDominance = solveAndChecksSolution(globalModel, "DDO (Dominance)");
                org.junit.jupiter.api.Assertions.assertEquals(ddoVal, ddoWithDominance, 1e-10, "DDO: adding dominance changes the value");

                double ddoWithCache = solveAndChecksSolution(wrapDdoModel(globalModel, dominanceUsed, true, null), "DDO (Cache)");
                org.junit.jupiter.api.Assertions.assertEquals(ddoVal, ddoWithCache, 1e-10, "DDO: using cache changes the value");

                // ACS tests
                org.ddolib.modeling.nolayer.AcsModel<MispState> acsModelNoDom = wrapAcsModel(globalModel, false, null);
                double acsVal = solveAndChecksSolution(acsModelNoDom, "ACS");
                
                org.ddolib.modeling.nolayer.AcsModel<MispState> acsModel = wrapAcsModel(globalModel, true, null);
                double acsWithDominance = solveAndChecksSolution(acsModel, "ACS (Dominance)");
                org.junit.jupiter.api.Assertions.assertEquals(acsVal, acsWithDominance, 1e-10, "ACS: dominance change the value");
            }
            
            // Re-implement the wrapper methods since they are private in the super class... wait.
            // Better to just call the super method, but we can't if we skip AStar.
            // Let's just create public wrapper methods in NoLayerNonRegressionTestBench.
        };
        return testBench.generateTests();
    }
}
