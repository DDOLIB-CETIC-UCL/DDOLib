package org.ddolib.util;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Collection of utility functions for formatting data for display.
 */
public final class PrettyPrint {

    /**
     * Formats a duration in milliseconds into a human-readable string.
     * The format includes hours, minutes, seconds, and milliseconds as needed (e.g., " 1 h 12 min 30 sec 500 ms").
     *
     * @param durationMs the duration in milliseconds to format
     * @return a formatted string representing the duration
     */
    public static String formatMs(long durationMs) {
        Duration duration = Duration.ofMillis(durationMs);
        long h = duration.toHours();
        long min = duration.toMinutesPart();
        long sec = duration.toSecondsPart();
        long ms = duration.toMillisPart();

        StringBuilder sb = new StringBuilder();

        if (h > 0) sb.append("%2d h ".formatted(h));
        if (duration.toMinutes() > 0) sb.append("%2d min ".formatted(min));
        if (duration.toSeconds() > 0) sb.append("%2d sec ".formatted(sec));
        sb.append("%3d ms".formatted(ms));

        return sb.toString();
    }

    public static String buildTable(List<String> labels, List<List<String>> rows) {
        if (labels.isEmpty()) return "";
        int[] colWidths = new int[labels.size()];
        for (int i = 0; i < labels.size(); i++) {
            int max = labels.get(i).length();
            for (List<String> row : rows) {
                if (i < row.size()) {
                    max = Math.max(max, row.get(i).length());
                }
            }
            colWidths[i] = max + 2;
        }

        StringBuilder sb = new StringBuilder();
        String rowSeparator = "+" + IntStream.of(colWidths)
                .mapToObj("-"::repeat)
                .collect(Collectors.joining("+")) + "+\n";

        String headerSeparator = "+" + IntStream.of(colWidths)
                .mapToObj("="::repeat)
                .collect(Collectors.joining("+")) + "+\n";

        sb.append(rowSeparator);

        sb.append("|");
        for (int i = 0; i < labels.size(); i++) {
            sb.append(String.format(" %-" + (colWidths[i] - 1) + "s|", labels.get(i)));
        }
        sb.append("\n").append(headerSeparator);

        for (List<String> row : rows) {
            sb.append("|");
            for (int i = 0; i < labels.size(); i++) {
                String value = i < row.size() ? row.get(i) : "";
                sb.append(String.format(" %-" + (colWidths[i] - 1) + "s|", value));
            }
            sb.append("\n");
        }

        sb.append(rowSeparator);
        return sb.toString();
    }

}
