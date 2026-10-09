package org.ddolib.examples.layered.tsptw;

/** Interface to model the position of the vehicle in a {@link TSPTWState}. */
sealed interface Position permits TSPNode, VirtualNodes {}
