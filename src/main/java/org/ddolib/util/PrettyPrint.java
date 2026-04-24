package org.ddolib.util;

import java.time.Duration;
import java.util.List;

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

    /**
     * Builds a formatted ASCII table string from labels and rows.
     * <p>
     * The table adjusts column widths automatically based on the longest element in each column.
     * </p>
     *
     * @param labels the headers for the table columns
     * @param rows   the data rows to populate the table
     * @return a formatted string representing the ASCII table
     */
    public static String buildTable(List<String> labels, List<List<String>> rows) {
        if (labels.isEmpty()) return "";

        int nbCols = labels.size();
        int[] colWidths = new int[nbCols];
        for (int i = 0; i < nbCols; i++) {
            int max = labels.get(i).length();
            for (List<String> row : rows) {
                if (i < row.size()) {
                    max = Math.max(max, row.get(i).length());
                }
            }
            colWidths[i] = max + 2;
        }

        StringBuilder sb = new StringBuilder();
        String rowSeparator = buildSeparator(colWidths, "-");
        String headerSeparator = buildSeparator(colWidths, "=");

        sb.append(rowSeparator);
        appendRow(sb, labels, colWidths);
        sb.append(headerSeparator);

        for (List<String> row : rows) {
            appendRow(sb, row, colWidths);
        }

        sb.append(rowSeparator);
        return sb.toString();
    }

    private static String buildSeparator(int[] colWidths, String character) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : colWidths) {
            sb.repeat(character, width).append("+");
        }
        return sb.append("\n").toString();
    }

    private static void appendRow(StringBuilder sb, List<String> values, int[] colWidths) {
        sb.append("|");
        for (int i = 0; i < colWidths.length; i++) {
            String value = i < values.size() ? values.get(i) : "";
            sb.append(" ").append(value);
            int padding = colWidths[i] - value.length() - 1;
            sb.repeat(" ", padding).append("|");
        }
        sb.append("\n");
    }

}
