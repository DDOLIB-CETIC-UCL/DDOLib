/**
 * Human-Robot Collaboration (HRC) Scheduling Problem.
 * <p>
 * Given a set of tasks, each task must be executed in exactly one of three modes:
 * <ul>
 *     <li><b>Human</b>: the human completes the task alone.</li>
 *     <li><b>Robot</b>: the robot completes the task alone.</li>
 *     <li><b>Collaborative</b>: both the human and robot work together on the task,
 *         tying up both resources simultaneously.</li>
 * </ul>
 * The objective is to minimize the overall makespan (total completion time).
 * <p>
 * The makespan is computed as:
 * <pre>
 *     C_max = T_C + max(T_H, T_R)
 * </pre>
 * where {@code T_H} is the total time spent on human-only tasks, {@code T_R} the total
 * time on robot-only tasks, and {@code T_C} the total time on collaborative tasks.
 */
package org.ddolib.examples.hrc;

