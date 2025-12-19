
/**
 * This package implements the acs, astar and ddo models for the Single Vehicle Pick-up and Delivery Problem (PDP).
 * A single vehicle pick-up and delivery problem is a pick-up and delivery problem restrict to one vehicle
 * and close to the TSP. Indeed, it is a TSP problem where nodes are grouped by pair: (pickup node; delivery node).
 * In a pair, the pickup node must be reached before the delivery node.
 * The problem can also have "unrelated nodes" that are not involved in such a pair
 */
package org.ddolib.examples.pdp;
