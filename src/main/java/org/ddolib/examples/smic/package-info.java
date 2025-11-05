/**
 * This package implements the acs, astar and ddo models for the Single Machine with Inventory Constraint (SMIC).
 * Given a set J of n jobs, partitioned into a set J1
 * of n1 loading jobs and set J2 of n2 unloading jobs. Each job j ∈ J has a
 * processing time p ∈ R+, a release date r ∈ R+ and a positive (resp. negative) inventory
 * modification for loading (resp. unloading) task. The objective is to sequence
 * the jobs in J such that the makespan is minimized while the inventory is between a given range.
 * This problem is considered in the paper:
 * <p>
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0377221720302435"> Morteza Davari, Mohammad Ranjbar, Patrick De
 * Causmaecker, Roel Leus:
 * Minimizing makespan on a single machine with release dates and inventory constraints. Eur. J.
 * Oper. Res. 286(1): 115-128 (2020)</a>
 */
package org.ddolib.examples.smic;
