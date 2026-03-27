.. _concepts:


*****************************************
Core Concepts
*****************************************

This chapter introduces the main abstractions you need to understand before
modelling a problem with DDOLib. Everything revolves around a small set of
interfaces; once you know what each one does, wiring up a new problem is
straightforward.


The Big Picture
===============

DDOLib solves **combinatorial optimisation problems** that can be expressed as
dynamic programs (DPs). You describe your problem as a *labelled transition
system*: a set of **states**, a set of **decisions** that move from one state
to the next, and a **cost** associated with every transition. The library
then explores this transition system with different search strategies —
building *decision diagrams* on the fly — to find an optimal solution.

.. code-block:: text

         ┌──────────┐
         │   root   │  (initial state)
         └──┬───┬───┘
        d=1 │   │ d=0       ← decision on variable 0
       ┌────▼┐ ┌▼────┐
       │ s1  │ │ s2  │      (layer 1 states)
       └─┬─┬┘ └┬──┬─┘
         │ │   │  │         ← decisions on variable 1
         ...   ...
         └──┬──┘
         ┌──▼──┐
         │ terminal │       (solution)
         └─────┘

A decision diagram encodes a set of solutions as paths from the *root*
(initial state) to a *terminal* node. Each arc carries a decision and
its associated cost.

.. note::
   If you are familiar with the book
   *Bergman, Cire, Van Hoeve & Hooker (2016). Decision Diagrams for Optimization*,
   DDOLib implements the algorithms described there, extended with dominance
   pruning, caching, and anytime column search.


Problem — the transition system
================================

The ``Problem<T>`` interface is the **most important** abstraction you will
implement. It tells the solver everything about your problem.

.. code-block:: java

   public interface Problem<T> {
       int nbVars();                                       // number of decision variables
       T initialState();                                   // starting state
       double initialValue();                              // objective value at the root
       Iterator<Integer> domain(T state, int var);         // feasible values for variable `var`
       T transition(T state, Decision decision);           // next state after a decision
       double transitionCost(T state, Decision decision);  // cost of that transition
   }

The type parameter ``T`` is the **state type** — it can be any Java object.
For instance, the Knapsack problem uses a plain ``Integer`` (remaining
capacity), while other problems use records that hold sets, arrays, or
combinations thereof.

Key rules to remember:

* **Minimisation convention** – DDOLib always *minimises*. If your problem is
  a maximisation, negate the costs. For example, in the Knapsack problem the
  transition cost is ``-profit[i]``.
* **Immutability** – ``transition()`` must return a **new** state object. Never
  mutate the state in place; the solver may reuse it in several branches.
* **hashCode / equals** – If your state is anything other than a primitive
  wrapper, make sure ``hashCode`` and ``equals`` are correctly implemented.
  They are needed for caching and dominance.


Decisions
=========

A ``Decision`` is a (variable, value) pair. The ``variable`` field tells the
solver *which* decision variable is being assigned, and ``value`` tells it
*what* value that variable takes.

In a layer-by-layer solver the variable is determined by the current layer;
you only need to enumerate the feasible *values* in ``domain()``.


State
=====

The state captures all the information about "where you are" in the problem
after a sequence of decisions. A good state representation is:

1. **Complete** – it must carry enough information so that ``domain()``,
   ``transition()`` and ``transitionCost()`` can be computed without looking
   at the decisions already taken.
2. **Compact** – the fewer distinct states there are, the smaller the
   decision diagram will be and the faster the solver can work.

For the Knapsack problem the state is simply the *remaining capacity*.
For a TSP the state is the *set of visited cities* together with the
*current city*.


Relaxation — building relaxed decision diagrams
=================================================

The ``Relaxation<T>`` interface is required by the DDO solver
(branch-and-bound with decision diagrams). It specifies how to
**merge** states when a layer in the decision diagram exceeds the
allowed width.

.. code-block:: java

   public interface Relaxation<T> {
       T mergeStates(Iterator<T> states);
       double relaxEdge(T from, T to, T merged, Decision d, double cost);
   }

* ``mergeStates`` receives the states to merge and returns a single state that
  *over-approximates* all of them. For the Knapsack this is simply the
  maximum remaining capacity.
* ``relaxEdge`` adjusts the edge cost that used to arrive at state ``to`` so
  that it correctly arrives at ``merged``. In many problems (including the
  Knapsack) the cost does not change, so it just returns ``cost``.

A good relaxation provides **tight lower bounds**: the closer the merged state
is to the real states, the more pruning the solver can do.


StateRanking — choosing which states to keep
==============================================

When a layer exceeds the maximum width, the solver must decide which states
to *keep* and which to *merge* (in a relaxed DD) or *discard* (in a
restricted DD). The ``StateRanking<T>`` interface is a ``Comparator<T>``
that ranks states:

.. code-block:: java

   public interface StateRanking<T> extends Comparator<T> { }

States with a *higher* compare value are more likely to be kept intact.
For example, in the Knapsack problem, states with *more* remaining capacity
are ranked higher because they have more potential for future profit.


FastLowerBound — pruning by bounding
======================================

The ``FastLowerBound<T>`` interface provides a quick heuristic estimate of
the best objective value reachable from a given state:

.. code-block:: java

   public interface FastLowerBound<T> {
       double fastLowerBound(T state, Set<Integer> variables);
   }

The ``variables`` parameter is the set of decision variables not yet assigned.
The returned value must be a **valid lower bound** (i.e. no better than the
true optimum from that state). If the current accumulated cost plus this
lower bound already exceeds the best known solution, the solver prunes the
entire subtree.

For the Knapsack problem, the fast lower bound is a greedy heuristic that
fills the remaining capacity with items sorted by profit-to-weight ratio
(the *fractional relaxation*).


Dominance — discarding inferior states
========================================

The ``Dominance<T>`` interface lets you declare when one state is *at least
as good as* another in every possible continuation:

.. code-block:: java

   public interface Dominance<T> {
       Object getKey(T state);
       boolean isDominatedOrEqual(T state1, T state2);
   }

* ``getKey`` partitions states into groups. Only states with the **same key**
  are compared. Return a constant (e.g. ``0``) if all states are comparable.
* ``isDominatedOrEqual`` returns ``true`` when ``state1`` can never lead to a
  better solution than ``state2``. The solver then safely discards ``state1``.

For the Knapsack, state ``c1`` is dominated by ``c2`` whenever ``c1 ≤ c2``
(less remaining capacity means fewer items can be added).

.. important::
   Dominance checking can drastically reduce the search space. Take the time
   to define it — it often makes the difference between solving in seconds
   and timing out.


Models — packaging everything together
========================================

DDOLib provides three **model interfaces** that bundle the components above
depending on which solver you want to use:

.. list-table::
   :header-rows: 1
   :widths: 20 20 60

   * - Interface
     - Solver
     - Additional requirements
   * - ``Model<T>``
     - A*
     - ``Problem``, optional ``FastLowerBound``, ``Dominance``
   * - ``DdoModel<T>``
     - DDO (B&B with DDs)
     - Everything in ``Model`` plus ``Relaxation``, ``StateRanking``,
       width heuristic, frontier strategy
   * - ``AcsModel<T>``
     - Anytime Column Search
     - Same as ``Model`` plus ``columnWidth``

All three extend ``Model<T>``, so the core components (problem, lower bound,
dominance) are shared. If you implement ``DdoModel`` you automatically have
everything needed for A* and ACS too.

Each model interface provides sensible **defaults**: no dominance checking, a
trivial lower bound, fixed DD width of 10, etc. You override only what you
need.


Putting it all together
========================

Here is a bird's-eye view of how the pieces connect:

.. code-block:: text

   ┌────────────────────────────────────────────┐
   │                  Solver                     │
   │  (DDO / A* / ACS / Exact)                  │
   └───────────────┬────────────────────────────┘
                   │  uses
   ┌───────────────▼────────────────────────────┐
   │              Model                          │
   │  ┌─────────────┐  ┌────────────────────┐   │
   │  │  Problem     │  │  FastLowerBound    │   │
   │  │  (states,    │  │  (pruning heur.)   │   │
   │  │   transitions│  └────────────────────┘   │
   │  │   costs)     │  ┌────────────────────┐   │
   │  └─────────────┘  │  Dominance          │   │
   │                    │  (state pruning)    │   │
   │  ┌─────────────┐  └────────────────────┘   │
   │  │ Relaxation   │  ┌────────────────────┐   │
   │  │ (DDO only)   │  │  StateRanking      │   │
   │  └─────────────┘  │  (DDO only)         │   │
   │                    └────────────────────┘   │
   └─────────────────────────────────────────────┘

After defining the model you hand it to ``Solvers``:

.. code-block:: java

   Solution sol = Solvers.minimizeDdo(model);   // DDO solver
   Solution sol = Solvers.minimizeAstar(model);  // A* solver
   Solution sol = Solvers.minimizeAcs(model);    // ACS solver

Each method returns a ``Solution`` object containing the best solution found,
its objective value, and detailed search statistics (time, nodes explored,
etc.).


What's next?
============

In the next chapter we walk through a **complete example** — the 0/1 Knapsack
Problem — showing every interface implementation step by step.

