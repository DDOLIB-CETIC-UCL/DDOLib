/**
 * ######### Knapsack Problem (KS) #############
 * This package contains the implementation of the knapsack problem.
 *
 * The Knapsack problem is a classic optimization problem
 * where the goal is to maximize the total value of items
 * while staying within a given weight limit.
 * The dynamic programming model is used to solve this problem is
 * using the recurrence relation:
 * KS(i, c) = max(KS(i-1, c), KS(i-1, c - w[i]) + p[i])
 * where KS(i, c) is the maximum value of the first i items
 * with a knapsack capacity of c, p[i] is the profit of item i,
 * w[i] is the weight of item i.
 */
package org.ddolib.examples.knapsack;
