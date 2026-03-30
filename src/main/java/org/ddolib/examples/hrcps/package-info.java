/**
 * Human-Robot Collaborative Planning with Stations (HRCPS).
 * <p>
 * This package solves a <b>Type-I Assembly Line Balancing</b> problem where
 * the objective is to <b>minimise the number of stations</b> given a fixed
 * cycle time.  Each station is staffed by one human and one robot who
 * collaborate according to the <b>HRCP</b> (Human-Robot Collaboration with
 * Precedences) scheduling model.
 * <p>
 * <b>Nested optimisation approach:</b>
 * <ul>
 *   <li><em>Outer DDO</em> – assigns tasks to stations, respecting
 *       precedence constraints and aiming to minimise the station count.</li>
 *   <li><em>Inner solver (HRCP via A*)</em> – for every candidate station,
 *       solves the HRCP scheduling sub-problem to verify that the optimal
 *       makespan does not exceed the cycle time.</li>
 * </ul>
 * <p>
 * Every station is assumed to have both a human worker and a robot; there is
 * no separate robot-allocation decision (unlike the {@code ssalbrb1207nested}
 * package).  The validity of each task-to-station assignment is checked by
 * solving the corresponding HRCP sub-problem and comparing the resulting
 * makespan against the cycle time.
 * <p>
 * <b>Key classes:</b>
 * <ul>
 *   <li>{@link org.ddolib.examples.hrcps.HRCPSProblem} – outer problem
 *       definition (task-to-station assignment).</li>
 *   <li>{@link org.ddolib.examples.hrcps.HRCPSState} – outer state
 *       (completed tasks, current station tasks, tentatively assigned tasks).</li>
 *   <li>{@link org.ddolib.examples.hrcps.HRCPSFastLowerBound} – admissible
 *       lower bound on the remaining number of stations.</li>
 * </ul>
 *
 * @see org.ddolib.examples.hrcp.HRCPProblem
 */
package org.ddolib.examples.hrcps;

