package org.ddolib.util;

import java.time.Duration;

/**
 * Collection of utility functions for formatting data for display.
 */
public class PrettyPrint {

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

}
