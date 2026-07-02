/**
 * This package implements the acs, astar and ddo models for the Aircraft Landing Problem (ALP).
 * The Aircraft Landing Problem (ALP) requires to schedule the landing of a set
 * of aircraft 𝑁= {0,...,𝑛−1} on a set of runways 𝑅 = {0,...,𝑟−1}. The
 * aircraft have a target time 𝑇_𝑖 that gives the earliest landing time, and latest
 * landing time 𝐿_𝑖. Moreover, the set of aircraft is partitioned in disjoint sets
 * 𝐴_0,...,𝐴_{c-1} corresponding to different aircraft classes in 𝐶= {0,...,𝑐−1}.
 * For each pair of aircraft classes 𝑎,𝑏 ∈ 𝐶, a minimum separation time 𝑆𝑎,𝑏
 * between the landings is given. The goal is to find a feasible schedule that
 * contains all the aircraft and minimizes the total waiting time of the aircraft
 * – the delay between their target time and scheduled landing time – while
 * respecting their latest landing time.
 */
package org.ddolib.layered.examples.alp;