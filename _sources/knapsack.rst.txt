.. _knapsack:


****************************************************
Tutorial: Solving the Knapsack Problem
****************************************************

In this chapter we build a complete DDOLib model for the classic
**0/1 Knapsack Problem** (KP). By the end you will have implemented every
interface introduced in :ref:`concepts` and will be able to run the solver
with three different strategies.

The full source code lives in the package
``org.ddolib.examples.knapsack``.


Problem description
====================

You are given a knapsack with capacity :math:`C` and :math:`n` items.
Item :math:`i` has profit :math:`p_i` and weight :math:`w_i`.
Select a subset of items that **maximises** the total profit without
exceeding the capacity.

The classic DP recurrence is:

.. math::

   KS(i, c) = \max\bigl( KS(i{-}1,\, c),\;
                          KS(i{-}1,\, c - w_i) + p_i \bigr)

where :math:`KS(i, c)` is the best profit achievable using items
:math:`0 \ldots i{-}1` with remaining capacity :math:`c`.


Step 1 — Define the state
===========================

For the Knapsack the state is simply the **remaining capacity** of the
knapsack — a single ``Integer``. This is about as simple as a state can get,
which makes KP an ideal first example.


Step 2 — Implement ``Problem<Integer>``
=========================================

.. code-block:: java

   public class KSProblem implements Problem<Integer> {

       public final int   capa;      // maximum capacity
       public final int[] profit;    // profit of each item
       public final int[] weight;    // weight of each item

       /* --- constructors omitted for brevity --- */

       @Override
       public int nbVars() {
           return profit.length;       // one decision variable per item
       }

       @Override
       public Integer initialState() {
           return capa;                // the knapsack starts empty
       }

       @Override
       public double initialValue() {
           return 0;                   // no profit collected yet
       }

       @Override
       public Iterator<Integer> domain(Integer state, int var) {
           if (state >= weight[var])          // item fits
               return Arrays.asList(1, 0).iterator();  // take it or not
           else
               return List.of(0).iterator();  // cannot take it
       }

       @Override
       public Integer transition(Integer state, Decision decision) {
           // decrease capacity when the item is taken
           return state - weight[decision.variable()] * decision.value();
       }

       @Override
       public double transitionCost(Integer state, Decision decision) {
           // negate profit because DDOLib minimises
           return -profit[decision.variable()] * decision.value();
       }
   }

**Important points:**

* ``nbVars()`` returns the total number of items. Each item corresponds to
  one *layer* of the decision diagram.
* ``domain()`` returns ``{1, 0}`` when the item fits (prefer ``1`` first so
  the solver tries taking the item before skipping it) and ``{0}`` otherwise.
* Costs are **negated** profits. DDOLib always minimises, so we convert
  maximisation into minimisation.
* ``transition()`` returns a **new** ``Integer`` — it never mutates the input.


Step 3 — Implement ``Relaxation<Integer>``
============================================

The relaxation merges several states into one that *over-approximates* all
of them. For KP the natural relaxation is to keep the **maximum** remaining
capacity:

.. code-block:: java

   public class KSRelax implements Relaxation<Integer> {

       @Override
       public Integer mergeStates(Iterator<Integer> states) {
           int capa = 0;
           while (states.hasNext())
               capa = Math.max(capa, states.next());
           return capa;
       }

       @Override
       public double relaxEdge(Integer from, Integer to,
                               Integer merged, Decision d, double cost) {
           return cost;   // cost is independent of the capacity state
       }
   }

Why does this produce a valid relaxation? Because a higher remaining capacity
can never *decrease* the set of feasible continuations. The merged state
therefore admits *at least* all the solutions that were reachable from the
original states — exactly what the solver needs for a sound lower bound.


Step 4 — Implement ``StateRanking<Integer>``
===============================================

When a layer of the decision diagram grows too wide, the solver needs to
choose which states to keep. We rank by remaining capacity — more capacity
means more potential:

.. code-block:: java

   public class KSRanking implements StateRanking<Integer> {
       @Override
       public int compare(Integer o1, Integer o2) {
           return o1 - o2;   // higher capacity → higher rank
       }
   }


Step 5 — Implement ``FastLowerBound<Integer>``
================================================

A good lower bound lets the solver prune subtrees early. For KP we use the
classic *fractional relaxation*: fill the remaining capacity greedily by
profit-to-weight ratio.

.. code-block:: java

   public class KSFastLowerBound implements FastLowerBound<Integer> {

       private final KSProblem problem;

       public KSFastLowerBound(KSProblem problem) {
           this.problem = problem;
       }

       @Override
       public double fastLowerBound(Integer state, Set<Integer> variables) {
           // sort remaining items by decreasing profit/weight ratio
           Integer[] sorted = variables.toArray(new Integer[0]);
           double[] ratio = new double[problem.nbVars()];
           for (int v : variables)
               ratio[v] = (double) problem.profit[v] / problem.weight[v];
           Arrays.sort(sorted,
               Comparator.comparingDouble((Integer v) -> ratio[v]).reversed());

           int capacity = state;
           int maxProfit = 0;
           for (int item : sorted) {
               if (capacity <= 0) break;
               if (capacity >= problem.weight[item]) {
                   maxProfit += problem.profit[item];
                   capacity  -= problem.weight[item];
               } else {
                   maxProfit += (int) Math.floor(ratio[item] * capacity);
                   capacity = 0;
               }
           }
           return -maxProfit;   // negated because we minimise
       }
   }

.. note::
   The returned value must be a **lower bound** on the cost (which is the
   negated profit). Since the fractional relaxation gives an *upper* bound on
   profit, negating it gives a valid lower bound on cost.


Step 6 — Implement ``Dominance<Integer>``
===========================================

A state with *more* remaining capacity is always at least as good as one
with less (assuming the same items have been considered). Therefore:

.. code-block:: java

   public class KSDominance implements Dominance<Integer> {

       @Override
       public Integer getKey(Integer capa) {
           return 0;   // all states are comparable
       }

       @Override
       public boolean isDominatedOrEqual(Integer capa1, Integer capa2) {
           return capa1 <= capa2;   // less capacity → dominated
       }
   }

The key is constant because in KP, after the same number of decisions, any
two states are directly comparable. For problems with richer states (e.g. TSP)
the key is used to partition states into groups where dominance makes sense.


Step 7 — Wire everything into a model and solve
==================================================

Now we assemble the pieces and call a solver.


Solving with DDO (branch-and-bound with decision diagrams)
-----------------------------------------------------------

.. code-block:: java

   KSProblem problem = new KSProblem("data/Knapsack/example");

   DdoModel<Integer> model = new DdoModel<>() {
       @Override public Problem<Integer>       problem()        { return problem; }
       @Override public Relaxation<Integer>     relaxation()     { return new KSRelax(); }
       @Override public KSRanking               ranking()        { return new KSRanking(); }
       @Override public FastLowerBound<Integer>  lowerBound()     { return new KSFastLowerBound(problem); }
       @Override public DominanceChecker<Integer> dominance()     {
           return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
       }
       @Override public WidthHeuristic<Integer> widthHeuristic() { return new FixedWidth<>(50); }
       @Override public Frontier<Integer>       frontier()       {
           return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
       }
       @Override public boolean useCache() { return true; }
   };

   Solution bestSolution = Solvers.minimizeDdo(model);
   System.out.println(bestSolution);

The DDO solver builds *restricted* and *relaxed* decision diagrams in a
branch-and-bound loop. The width heuristic controls the maximum number of
nodes per layer. Larger widths give tighter bounds but cost more memory.


Solving with A*
-----------------

A* needs only a ``Model`` (no relaxation or ranking):

.. code-block:: java

   Model<Integer> model = new Model<>() {
       @Override public Problem<Integer>       problem()    { return problem; }
       @Override public FastLowerBound<Integer> lowerBound() { return new KSFastLowerBound(problem); }
       @Override public DominanceChecker<Integer> dominance() {
           return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
       }
   };

   Solution bestSolution = Solvers.minimizeAstar(model);

A* explores states in best-first order guided by the lower bound. It
guarantees optimality as long as the lower bound is *admissible* (never
over-estimates the remaining cost).


Solving with ACS (Anytime Column Search)
------------------------------------------

ACS iteratively refines solutions using bounded-width decision diagrams:

.. code-block:: java

   AcsModel<Integer> model = new AcsModel<>() {
       @Override public Problem<Integer>       problem()    { return problem; }
       @Override public FastLowerBound<Integer> lowerBound() { return new KSFastLowerBound(problem); }
       @Override public DominanceChecker<Integer> dominance() {
           return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
       }
       @Override public int columnWidth() { return 10; }
   };

   Solution bestSolution = Solvers.minimizeAcs(model);

ACS is a good choice when you want an *anytime* algorithm that quickly finds
good solutions and keeps improving them over time.


Data file format
=================

The knapsack data file used by ``KSProblem`` has a simple text format:

.. code-block:: text

   4 5
   2 1
   3 3
   3 3
   2 3

- **Line 1**: ``n`` (number of items) and ``C`` (knapsack capacity). An
  optional third column gives the known optimal value.
- **Lines 2 to n+1**: ``profit  weight`` for each item.

The example above has 4 items with capacity 5. Items have
profits [2, 3, 3, 2] and weights [1, 3, 3, 3].


Running the examples
=====================

You can run the three main classes directly from the command line (Maven):

.. code-block:: bash

   # DDO solver
   mvn exec:java -Dexec.mainClass="org.ddolib.examples.knapsack.KSDdoMain"

   # A* solver
   mvn exec:java -Dexec.mainClass="org.ddolib.examples.knapsack.KSAstarMain"

   # ACS solver
   mvn exec:java -Dexec.mainClass="org.ddolib.examples.knapsack.KSAcsMain"

Or open any of the ``*Main.java`` files in IntelliJ and click the green
*Run* button.


Time and stop conditions
=========================

All solver methods accept an optional ``Predicate<SearchStatistics>`` to
control when to stop:

.. code-block:: java

   // Stop after 10 seconds
   Solution sol = Solvers.minimizeDdo(model,
       stats -> stats.elapsedMs() > 10_000);

You can also pass a callback that is invoked every time the solver finds a
better solution:

.. code-block:: java

   Solution sol = Solvers.minimizeDdo(model,
       stats -> stats.elapsedMs() > 10_000,
       (solution, stats) -> {
           System.out.println("New best: " + stats.incumbent());
       });


Summary
========

.. list-table::
   :header-rows: 1

   * - Component
     - Class
     - Role
   * - Problem
     - ``KSProblem``
     - States, transitions, costs
   * - Relaxation
     - ``KSRelax``
     - Merge states for relaxed DDs
   * - Ranking
     - ``KSRanking``
     - Choose which states to keep
   * - Lower bound
     - ``KSFastLowerBound``
     - Greedy fractional bound for pruning
   * - Dominance
     - ``KSDominance``
     - Discard inferior states
   * - DDO entry
     - ``KSDdoMain``
     - DDO solver demo
   * - A* entry
     - ``KSAstarMain``
     - A* solver demo
   * - ACS entry
     - ``KSAcsMain``
     - ACS solver demo

You now have all the building blocks to model your own problem. Check out the
:ref:`solvers` chapter for a deeper comparison of DDO, A*, and ACS, or browse
the :ref:`allmodels` catalogue for more advanced examples.

