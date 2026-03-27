.. _allmodels:


*****************************************
Catalogue of Example Models
*****************************************

DDOLib ships with a rich set of example models covering scheduling, routing,
packing, graph, and sequencing problems. Each example lives in its own
sub-package under ``org.ddolib.examples`` and comes with DDO, A*, and ACS
main classes ready to run. Browse the
`source on GitHub <https://github.com/DDOLIB-CETIC-UCL/DDOLib/tree/main/src/main/java/org/ddolib/examples>`_
for the complete code.

Below is a quick-reference card for every model.


Packing & Knapsack Problems
=============================

Knapsack Problem (KP)
---------------------

:Package: ``knapsack``
:Objective: Maximise total profit
:State: Remaining capacity (``Integer``)
:Reference: Classic DP recurrence

Select a subset of items to maximise profit without exceeding the knapsack
capacity.
See the :ref:`knapsack` chapter for a full tutorial.

Bounded Knapsack Problem (BKP)
-------------------------------

:Package: ``boundedknapsack``
:Objective: Maximise total profit
:State: Remaining capacity

A variation where each item type can be included up to a given number of
copies.

Multidimensional Knapsack Problem (MKP)
-----------------------------------------

:Package: ``mks``
:Objective: Maximise total profit
:State: Remaining capacity vector

Generalisation of KP to *m* capacity constraints. An item occupies a weight
in each dimension; all capacity bounds must hold simultaneously.


Graph & Partition Problems
============================

Maximum Independent Set Problem (MISP)
----------------------------------------

:Package: ``misp``
:Objective: Maximise total weight of selected nodes
:State: Set of candidate vertices

Find a subset of vertices in a weighted graph such that no two selected
vertices are adjacent and the total weight is maximal.

*Reference:* Bergman et al., *Decision Diagrams for Optimization* (2016).

Maximum Cut Problem (MCP)
--------------------------

:Package: ``mcp``
:Objective: Maximise total weight of cut edges
:State: Partition assignment + benefit vector

Partition the vertices of a weighted graph into two sets so as to maximise
the total weight of edges crossing the partition.

*Reference:* Bergman et al., *Decision Diagrams for Optimization* (2016).

Maximum 2-Satisfiability (MAX-2SAT)
-------------------------------------

:Package: ``max2sat``
:Objective: Maximise total weight of satisfied clauses
:State: Partial assignment + clause weights

Given a weighted CNF formula with at most two literals per clause, find an
assignment that maximises the weight of satisfied clauses.

*Reference:* Bergman et al., *Discrete Optimization with Decision Diagrams* (2016).

Maximum Coverage Problem
--------------------------

:Package: ``maximumcoverage``
:Objective: Maximise covered elements
:State: Covered-element bitset

Select a limited number of sets from a collection to maximise the number of
elements covered by the union of the selected sets.


Routing Problems
=================

Travelling Salesman Problem (TSP)
-----------------------------------

:Package: ``tsp``
:Objective: Minimise tour length
:State: Set of visited cities + current city

Find the shortest Hamiltonian cycle visiting every city exactly once.

TSP with Time Windows (TSPTW)
-------------------------------

:Package: ``tsptw``
:Objective: Minimise tour length
:State: Visited cities + current city + current time

TSP variant where each city must be visited within a specified time window.

Single Vehicle Pick-up and Delivery (PDP)
-------------------------------------------

:Package: ``pdp``
:Objective: Minimise total travel cost
:State: Visited nodes + current node

A TSP-like problem where nodes are grouped into pick-up/delivery pairs; each
pick-up must be visited before its corresponding delivery.


Scheduling Problems
====================

Aircraft Landing Problem (ALP)
--------------------------------

:Package: ``alp``
:Objective: Minimise total waiting time
:State: Scheduled aircraft set + runway last-landing times

Schedule aircraft landings on multiple runways respecting separation times,
earliest/latest landing times, and aircraft class constraints.

Minimum Sum of Completion Times (MSCT)
---------------------------------------

:Package: ``msct``
:Objective: Minimise sum of completion times
:State: Set of remaining jobs + current time

Sequence *n* jobs on a single machine respecting release dates so as to
minimise the total sum of completion times.

*Reference:* Beck et al., *Transition Dominance in Domain-Independent DP* (CP 2025).

Single Machine with Inventory Constraints (SMIC)
---------------------------------------------------

:Package: ``smic``
:Objective: Minimise makespan
:State: Remaining jobs + current time + inventory level

Schedule loading and unloading jobs on a single machine so that the
inventory level stays within bounds and the makespan is minimised.

*Reference:* Davari et al., *Minimizing makespan on a single machine with
release dates and inventory constraints* (EJOR 2020).

Talent Scheduling Problem
----------------------------

:Package: ``talentscheduling``
:Objective: Minimise total actor wages
:State: Remaining scenes + actor presence intervals

Order the shooting of movie scenes to minimise the total wages paid to
actors, who are paid for every day between their first and last scene.

Pigment Sequencing Problem (PSP)
----------------------------------

:Package: ``pigmentscheduling``
:Objective: Minimise stocking + changeover costs
:State: Remaining orders + last produced item type

Plan single-machine production of different item types to minimise the sum
of stocking costs (holding orders until their deadline) and changeover costs
(switching between item types).


Sequencing & Layout Problems
==============================

Longest Common Subsequence (LCS)
----------------------------------

:Package: ``lcs``
:Objective: Maximise subsequence length
:State: Position indices in each input string

Find the longest subsequence that appears in all given input strings.

Single-Row Facility Layout Problem (SRFLP)
--------------------------------------------

:Package: ``srflp``
:Objective: Minimise weighted sum of inter-department distances
:State: Set of placed departments + accumulated half-lengths

Arrange departments along a single row to minimise the weighted
inter-department distances.

*Reference:* Coppé, Gillard & Schaus, *Solving the Constrained SRFLP with
Decision Diagrams* (CP 2022).

Golomb Ruler
--------------

:Package: ``gruler``
:Objective: Minimise ruler length
:State: Placed marks + distance set

Place marks on a ruler so that no two pairs of marks share the same
distance. Minimise the total ruler length.

*Model by:* Willem-Jan van Hoeve.


Running an example
====================

Every example package contains at least one ``*DdoMain.java``,
``*AstarMain.java``, and ``*AcsMain.java``. Run them with Maven:

.. code-block:: bash

   # Example: run the TSP DDO solver
   mvn exec:java -Dexec.mainClass="org.ddolib.examples.tsp.TSPDdoMain"

   # Example: run the MISP A* solver
   mvn exec:java -Dexec.mainClass="org.ddolib.examples.misp.MISPAstarMain"

Or open the relevant ``*Main.java`` file in IntelliJ and click **Run**.
Data files are in the ``data/`` directory at the project root.


Adding your own model
======================

1. Create a new package under ``org.ddolib.examples``.
2. Implement ``Problem<T>`` — define your state, transitions, and costs.
3. Optionally implement ``Relaxation<T>``, ``Dominance<T>``,
   ``FastLowerBound<T>``, and ``StateRanking<T>``.
4. Write a ``Main`` class that assembles a ``Model`` / ``DdoModel`` /
   ``AcsModel`` and calls the appropriate ``Solvers.minimize*`` method.
5. Add test instances in ``data/`` and unit tests in
   ``src/test/java/org/ddolib/examples/``.

Use the Knapsack package as a template — it covers every interface in the
simplest possible way.

