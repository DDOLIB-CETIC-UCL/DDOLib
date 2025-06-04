package org.ddolib.ddo.examples.routing.util;

import org.ddolib.ddo.examples.routing.tsptw.TSPTWState;

/**
 * Interface to model the position of the vehicle in a {@link TSPTWState}.
 */
public sealed interface RoutePosition permits RoutingNode, VirtualRoutingNodes {
}


