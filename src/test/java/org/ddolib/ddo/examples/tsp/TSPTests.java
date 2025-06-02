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

    static Stream<TSPProblem> dataProvider() throws IOException {
        String dir = Paths.get("src", "test", "resources", "TSP").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        TSPProblem problem = new TSPProblem(filePath.toString());
                        return problem;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    static Stream<TSPProblem> dataProvider2() throws IOException {
        return IntStream.range(0, 100).boxed().map(i ->
                TSPMain.genInstance(5+i%10, new Random(i)));
    }

    @ParameterizedTest
    @MethodSource("dataProvider2")
    public void testTSP(TSPProblem problem) {

        Solver s = TSPMain.solveTsp(problem, 0);

        int[] solution = TSPMain.extractSolution(s);
        assertEquals(s.bestValue().get() , -problem.eval(solution));

    }
}
