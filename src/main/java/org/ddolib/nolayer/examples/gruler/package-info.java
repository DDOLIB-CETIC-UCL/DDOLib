/**
 * This package implements the acs and astar models for the Golomb Rule Problem (GRP) using the unlayered API.
 * In a Golomb rule problem, a set of marks at integer positions along a ruler are
 * posted such that no two pairs of marks are the same distance apart.
 * The number of marks on the ruler is its order, and the largest distance between two of its marks is its length.
 * By default, the first mark is posted at 0.
 * This class demonstrates how to implement a solver for the Golomb ruler problem.
 * For more information on this problem, see
 * <a href="https://en.wikipedia.org/wiki/Golomb_ruler">Golomb Ruler - Wikipedia</a>.
 * <p>
 * This model was introduced by Willem-Jan van Hoeve.
 * In this unlayered model:
 * - The state represents the current sequence of marks placed on the ruler and the set of distances already formed between these marks.
 * - A transition from a state corresponds to choosing the position for the next mark.
 * - A mark can only be added if the distance between the new mark and all previous marks
 * is not already present in the set of distances between marks.
 * <p>
 * The cost of a transition is defined as the distance between the new mark and the
 * previous last mark. Consequently, the cost of a solution is the position of the last mark.
 */
package org.ddolib.nolayer.examples.gruler;
