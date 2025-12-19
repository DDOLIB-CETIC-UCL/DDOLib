package org.ddolib.examples.mcp;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class MCPTestDataSupplier extends TestDataSupplier<MCPState, MCPProblem> {

    private final Path dir;

    public MCPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<MCPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new MCPProblem(filePath.toString());
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
    protected DdoModel<MCPState> model(MCPProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<MCPState> problem() {
                return problem;
            }

            @Override
            public MCPFastLowerBound lowerBound() {
                return new MCPFastLowerBound(problem);
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
            public Relaxation<MCPState> relaxation() {
                return new MCPRelax(problem);
            }

            @Override
            public MCPRanking ranking() {
                return new MCPRanking();
            }
        };
    }
}
