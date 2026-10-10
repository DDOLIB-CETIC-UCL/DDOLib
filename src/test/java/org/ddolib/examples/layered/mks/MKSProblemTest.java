package org.ddolib.examples.layered.mks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests of the instance reader of the Multi-dimensional Knapsack Problem ({@link MKSProblem}). */
public class MKSProblemTest {

    @TempDir Path tmp;

    private MKSProblem read(String firstLine) throws IOException {
        Path file = tmp.resolve("instance.txt");
        Files.writeString(file, firstLine + "\n10 12\n5 4 6\n3 2 1\n");
        return new MKSProblem(file.toString());
    }

    @Test
    void testZeroOptimumMeansUnknown() throws IOException {
        // OR-Library files use 0 when the optimum is not known
        assertTrue(read("2 2 0").optimalValue().isEmpty());
    }

    @Test
    void testKnownOptimumIsRead() throws IOException {
        assertEquals(Optional.of(-8.0), read("2 2 8").optimalValue());
    }

    @Test
    void testMissingOptimum() throws IOException {
        assertTrue(read("2 2").optimalValue().isEmpty());
    }
}
