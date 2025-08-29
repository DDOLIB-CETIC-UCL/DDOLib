package org.ddolib.examples.ddo.misp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.lang.model.type.NullType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MispTest {

    static Stream<MispProblem> dataProvider() throws IOException {
        String dir = Paths.get("src", "test", "resources", "MISP").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        MispProblem problem = MispMain.readFile(filePath.toString());
                        problem.setName(filePath.getFileName().toString());
                        return problem;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBound(MispProblem problem) {
        final MispFastUpperBound fub = new MispFastUpperBound(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.remainingNodes, vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %.2f is not bigger than the expected optimal solution %.2f",
                        rub,
                        problem.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testMISP(MispProblem problem) {

        SolverConfig<BitSet, NullType> config = new SolverConfig<>();

        config.problem = problem;
        config.relax = new MispRelax(problem);
        config.ranking = new MispRanking();
        config.fub = new MispFastUpperBound(problem);
        config.width = new FixedWidth<>(250);
        config.varh = new DefaultVariableHeuristic<>();

        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);


        final Solver solver = new SequentialSolver<>(config);
        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal.get());
    }


}
