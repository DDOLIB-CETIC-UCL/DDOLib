package org.ddolib.examples.layered.talentscheduling;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests of the reader of {@link TSProblem}: it accepts the variants of the format used by the
 * datasets of ddo and DIDP.
 */
public class TSProblemReaderTest {

    @TempDir Path tmp;

    static Stream<Path> instances() throws IOException {
        List<Path> files = new ArrayList<>();
        for (Path dir :
                List.of(
                        Path.of("data", "TalentScheduling"),
                        Path.of("src", "test", "resources", "TalentScheduling"))) {
            try (Stream<Path> stream = Files.list(dir)) {
                stream.filter(Files::isRegularFile)
                        // the file of the best known solutions is not an instance
                        .filter(p -> !p.getFileName().toString().equals("Solutions"))
                        .sorted()
                        .forEach(files::add);
            }
        }
        return files.stream();
    }

    /**
     * Rewrites an instance like the files of ddo: the numbers of scenes and actors on one line,
     * the lines starting with spaces, and blank lines.
     */
    private static List<String> ddoStyle(List<String> lines) {
        List<String> data = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            if (!line.isBlank()) {
                data.add(line.trim());
            }
        }
        List<String> result = new ArrayList<>();
        result.add(lines.get(0));
        result.add("  " + data.get(0) + " " + data.get(1));
        result.add("");
        for (String line : data.subList(2, data.size())) {
            result.add("   " + line.replace(" ", "  ") + "  ");
        }
        result.add("");
        return result;
    }

    private static void assertSameProblem(TSProblem expected, TSProblem actual) {
        assertEquals(expected.nbScene, actual.nbScene);
        assertEquals(expected.nbActors, actual.nbActors);
        assertArrayEquals(expected.costs, actual.costs);
        assertArrayEquals(expected.duration, actual.duration);
        assertArrayEquals(expected.actors, actual.actors);
        assertEquals(expected.optimal, actual.optimal);
    }

    @ParameterizedTest
    @MethodSource("instances")
    void testDdoStyleIsReadLikeTheOriginal(Path instance) throws IOException {
        TSProblem original = new TSProblem(instance.toString());
        Path copy = tmp.resolve(instance.getFileName());
        Files.write(copy, ddoStyle(Files.readAllLines(instance)));

        assertSameProblem(original, new TSProblem(copy.toString()));
    }

    @Test
    void testHeaderOnOneLineWithLeadingSpaces() throws IOException {
        Path file = tmp.resolve("one_line_header");
        Files.write(
                file,
                List.of("tiny x 7", " 3 2", "  1 0 1  2", "  0 1 1  5", "  1 2 3"));
        TSProblem problem = new TSProblem(file.toString());

        assertEquals(3, problem.nbScene);
        assertEquals(2, problem.nbActors);
        assertArrayEquals(new int[] {2, 5}, problem.costs);
        assertArrayEquals(new int[] {1, 2, 3}, problem.duration);
        assertEquals("{0}", problem.actors[0].toString());
        assertEquals("{1}", problem.actors[1].toString());
        assertEquals("{0, 1}", problem.actors[2].toString());
        assertEquals(7.0, problem.optimal.get());
    }

    @Test
    void testTruncatedFileIsRejected() throws IOException {
        Path file = tmp.resolve("truncated");
        Files.write(file, List.of("tiny", "3 2", "1 0 1 2"));

        assertThrows(IOException.class, () -> new TSProblem(file.toString()));
    }
}
