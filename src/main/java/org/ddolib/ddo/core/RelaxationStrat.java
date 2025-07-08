package org.ddolib.ddo.core;

/**
 * How nodes to relax are chosen ?
 */
public enum RelaxationStrat {
    /** Select the worst nodes on the layer and merge them together*/
    Cost,
    /** Clusters the nodes following their similarities and merge the nodes of a same cluster*/
    MinDist,
    KClosest,
    OneD,
    GHP, // General Hyperplan Partition
    Kmeans,
}
