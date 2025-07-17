package org.ddolib.ddo.examples.pdp;

import org.ddolib.example.ddo.pdp.PDPInstance;
import org.ddolib.example.ddo.pdp.PDPMain;
import org.ddolib.example.ddo.pdp.PDPProblem;
import org.ddolib.example.ddo.pdp.PDPSolution;
import org.ddolib.solver.Solver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PDPTests {

    static Stream<PDPInstance> dataProvider2() {
        return IntStream.range(0, 10).boxed().map(i ->
                PDPMain.genInstance(5 + i % 14, i % 3, new Random(i)));
    }

    @ParameterizedTest
    @MethodSource("dataProvider2")
    public void testPDP(PDPInstance instance) {

        PDPProblem problem = new PDPProblem(instance);
        Solver s = PDPMain.solveDPD(problem);

        PDPSolution solution = PDPMain.extractSolution(s, problem);

        assertEquals(solution.value, instance.eval(solution.solution));
    }
}
