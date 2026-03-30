package org.ddolib.examples.hrc;

/**
 * Represents the state of the Human-Robot Collaboration scheduling problem.
 * <p>
 * The state captures the accumulated time for tasks assigned exclusively to the
 * human ({@code tH}) and exclusively to the robot ({@code tR}).
 * The accumulated collaborative time is tracked separately via transition costs.
 * </p>
 *
 * @param tH total accumulated time of tasks assigned exclusively to the human
 * @param tR total accumulated time of tasks assigned exclusively to the robot
 */
public record HRCState(int tH, int tR) {
}

