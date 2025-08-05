package org.ddolib.ddo.core;

/**
 * How nodes to relax are chosen ?
 */
public enum ClusterStrat {
    /** Select the worst nodes on the layer and merge them together*/
    Cost,
    CostFUB,
    /** Clusters the nodes following their similarities and merge the nodes of a same cluster*/
    GHP, // General Hyperplan Partition
    GHPMDP, // GHP with most distant pivot
    GHPMD, // GHP where the cluster with max distance in it is the next one to divide
    GHPMDPMD, // combinaison of GHPMDP and GHPMD
    Kmeans,
}
