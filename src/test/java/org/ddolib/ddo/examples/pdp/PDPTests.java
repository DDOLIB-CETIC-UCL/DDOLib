package org.ddolib.ddo.examples.pdp;

import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.tsp.TSPProblem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PDPTests {

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
                        return new TSPProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }




    static Stream<PDPProblem> dataProvider2() throws IOException {
        return IntStream.range(0, 100).boxed().map(i ->
                DPDMain.genInstance(3+i%10, 1+i%5, new Random(i)));
    }

    @ParameterizedTest
    @MethodSource("dataProvider2")
    public void testPDP(PDPProblem problem) {

        Solver s = DPDMain.solveDPD(problem);

        PDPSolution solution = DPDMain.extractSolution(s, problem);

        assertEquals(s.bestValue().get() , -problem.eval(solution.solution));
    }
}
