package org.ddolib.examples.mcp;

import org.ddolib.examples.maximumcoverage.MaxCoverILP;
import org.ddolib.examples.maximumcoverage.MaxCoverProblem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.ddolib.examples.mcp.MCPILP.solveMCP;

public class MCPILPTest {

    protected List<MCPProblem> generateProblems() {
        String dir = Paths.get("src", "test", "resources", "MCP").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new MCPProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    @Test
    protected void testOptimalILP() throws IOException {
        for (MCPProblem problem : generateProblems()) {
            double objective = solveMCP(problem);
            if (objective == -0.0) {
                objective = 0;
            }
            Assertions.assertEquals(problem.optimal.get(), objective);
        }
    }

}
