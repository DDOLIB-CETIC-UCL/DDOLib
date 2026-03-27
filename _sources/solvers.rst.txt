.. _solvers:


*****************************************
Solver Strategies
*****************************************

DDOLib ships with four solver strategies. All share the same ``Problem``
definition; they differ in how they explore the search space and what
additional components they require.


Overview
========

.. list-table::
   :header-rows: 1
   :widths: 15 15 35 35

   * - Strategy
     - Entry point
     - Strengths
     - Requirements
   * - **DDO**
     - ``Solvers.minimizeDdo``
     - Tight bounds via relaxed DDs, strong pruning, scalable
     - ``DdoModel`` (Problem + Relaxation + Ranking + width + frontier)
   * - **A***
     - ``Solvers.minimizeAstar``
     - Optimal first-solution guarantee, simple to set up
     - ``Model`` (Problem, optional LowerBound + Dominance)
   * - **ACS**
     - ``Solvers.minimizeAcs``
     - Anytime behaviour, quickly finds good solutions
     - ``AcsModel`` (Problem + columnWidth, optional LowerBound + Dominance)
   * - **Exact**
     - ``Solvers.minimizeExact``
     - Enumerates the full exact MDD — useful for debugging
     - ``ExactModel`` (Problem only)


DDO — Branch-and-Bound with Decision Diagrams
================================================

The DDO solver is the flagship algorithm of the library. It works by
alternating two phases:

1. **Restricted DD** — build a decision diagram of bounded width. Because
   some states are *dropped*, the diagram may miss the optimal solution, but
   every path it does contain is a feasible solution. The best path provides
   an *upper bound*.

2. **Relaxed DD** — build a decision diagram of bounded width where excess
   states are *merged* rather than dropped. Every feasible solution is
   reachable in the relaxed DD, but some paths may correspond to infeasible
   solutions. The best path provides a *lower bound*.

The solver maintains a *frontier* of sub-problems (states whose subtrees
have not yet been fully explored). At each iteration it picks the most
promising sub-problem, compiles both a restricted and a relaxed DD, and
updates the global bounds. The search terminates when the best lower bound
meets the best upper bound.

When to use DDO
-----------------

- Your problem has a natural state-merging operator (relaxation).
- You want a complete solver that proves optimality.
- You need strong dual bounds for large instances.

DDO configuration knobs
--------------------------

``WidthHeuristic``
   Controls the maximum number of nodes per DD layer.
   ``FixedWidth(w)`` sets a constant width.

``Frontier``
   Determines the order in which sub-problems are explored.
   ``SimpleFrontier`` with ``CutSetType.Frontier`` is a good default.

``useCache()``
   Enable or disable sub-problem caching (default: ``false``).
   Caching avoids re-expanding identical states at the cost of memory.

``StateRanking``
   Decides which states to keep and which to merge/discard.

DDO example
-------------

.. code-block:: java

   DdoModel<Integer> model = new DdoModel<>() {
       @Override public Problem<Integer>   problem()        { return problem; }
       @Override public Relaxation<Integer> relaxation()     { return new KSRelax(); }
       @Override public KSRanking           ranking()        { return new KSRanking(); }
       @Override public WidthHeuristic<Integer> widthHeuristic() {
           return new FixedWidth<>(100);
       }
       @Override public boolean useCache() { return true; }
   };

   Solution sol = Solvers.minimizeDdo(model);


A* — Best-First Search
========================

The A* solver explores states one at a time, always expanding the state
with the smallest estimated total cost (accumulated cost + lower-bound
estimate). It guarantees that the **first** complete solution found is
optimal, provided the lower bound is *admissible* (never over-estimates
the true remaining cost).

When to use A*
-----------------

- The state space is moderate or your dominance / lower bound is very
  effective at pruning.
- You want a simple setup without having to design a relaxation.
- You care about finding the proven-optimal solution and can afford the
  memory to store the open list.

A* configuration knobs
--------------------------

``FastLowerBound``
   The admissible heuristic guiding the search. A better bound means
   fewer states expanded.

``DominanceChecker``
   Prunes states that are provably worse than or equal to another
   already-seen state.

A* example
-----------

.. code-block:: java

   Model<Integer> model = new Model<>() {
       @Override public Problem<Integer>       problem()    { return problem; }
       @Override public FastLowerBound<Integer> lowerBound() {
           return new KSFastLowerBound(problem);
       }
       @Override public DominanceChecker<Integer> dominance() {
           return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
       }
   };

   Solution sol = Solvers.minimizeAstar(model);


ACS — Anytime Column Search
==============================

ACS is an iterative algorithm that builds bounded-width decision diagrams
column by column. At each iteration it refines the current solution by
exploring a neighbourhood defined by the diagram. It is an *anytime*
algorithm: it quickly finds a first feasible solution and keeps improving
it until it proves optimality or reaches a time limit.

When to use ACS
-----------------

- You need a good solution fast and can tolerate a gap.
- The instance is large and DDO / A* take too long.
- You want to monitor progress and stop when satisfied.

ACS configuration knobs
--------------------------

``columnWidth``
   Width of the decision diagrams built at each ACS iteration (default: 5).

ACS example
-------------

.. code-block:: java

   AcsModel<Integer> model = new AcsModel<>() {
       @Override public Problem<Integer>       problem()    { return problem; }
       @Override public FastLowerBound<Integer> lowerBound() {
           return new KSFastLowerBound(problem);
       }
       @Override public int columnWidth() { return 10; }
   };

   Solution sol = Solvers.minimizeAcs(model,
       stats -> stats.elapsedMs() > 30_000);   // stop after 30 s


Exact MDD Solver
==================

The exact solver builds a **full** decision diagram without any width
restriction. Every feasible solution appears as a path. Because the diagram
can grow exponentially, this solver is intended for **small instances** or
for **testing** your ``Problem`` implementation.

.. code-block:: java

   ExactModel<Integer> model = new ExactModel<>() {
       @Override public Problem<Integer> problem() { return problem; }
   };

   Solution sol = Solvers.minimizeExact(model);

.. warning::
   Exact MDDs can consume a very large amount of memory.
   Prefer ``minimizeDdo`` or ``minimizeAstar`` for anything beyond toy sizes.


Stopping conditions and callbacks
===================================

Every solver method accepts two optional parameters:

``Predicate<SearchStatistics> limit``
   A predicate evaluated after each iteration. Return ``true`` to stop.

``BiConsumer<int[], SearchStatistics> onSolution``
   A callback invoked every time a new incumbent (best-so-far) solution is
   found.

.. code-block:: java

   Solution sol = Solvers.minimizeDdo(model,
       stats -> stats.elapsedMs() > 60_000,          // 1-minute timeout
       (solution, stats) -> {
           System.out.printf("New best %f at %d ms%n",
               stats.incumbent(), stats.elapsedMs());
       }
   );

The ``SearchStatistics`` object gives you access to:

- ``incumbent()`` — current best objective value
- ``elapsedMs()`` — wall-clock time since the solver started
- and more runtime metrics


Choosing a solver — rules of thumb
====================================

.. list-table::
   :header-rows: 1
   :widths: 40 60

   * - Situation
     - Recommendation
   * - Small instance, need proof of optimality
     - **A*** or **DDO** with moderate width
   * - Large instance, good relaxation available
     - **DDO** with large width and caching
   * - Need a good solution quickly
     - **ACS** with a time limit
   * - Debugging / validating a ``Problem`` impl.
     - **Exact** solver on tiny instances
   * - No relaxation available
     - **A*** (only needs ``Problem`` + optional LB/dominance)

In practice, many users start with **A*** for simplicity, then add a
relaxation and switch to **DDO** when they need stronger bounds on harder
instances.

