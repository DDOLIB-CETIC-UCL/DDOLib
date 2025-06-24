package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TSPTests {

    static Stream<TSPInstance> dataProvider() throws IOException {
        String dir = Paths.get("src", "test", "resources", "TSP").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> new TSPInstance(filePath.toString()));
    }

    static Stream<TSPInstance> dataProvider2() throws IOException {
        return IntStream.range(0, 100).boxed().map(i ->
                new TSPInstance(3+i%10, i, 1000));
    }

    @ParameterizedTest
    @MethodSource("dataProvider2")
    public void testTSP(TSPInstance instance) {

        Solver s = TSPMain.solveTSP(instance);
        TSPProblem problem = new TSPProblem(instance.distanceMatrix);
        int[] solution = TSPMain.extractSpolution(problem, s);
        assertEquals(s.bestValue().get() , -problem.eval(solution));
        //TODO compare with best in instance;
    }
}
