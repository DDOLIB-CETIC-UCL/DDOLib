# Changelog

This file documents the DDOLib changes.

## Unreleased

### Added

- `MispCliqueCoverLowerBound`: a fast lower bound for the layered MISP based on a clique cover of
  the graph (a coloring of its complement), computed once greedily. An independent set contains
  at most one vertex per clique, so that the bound is minus the sum, over the cliques, of the
  largest weight still selectable in the clique. It is admissible, at least as tight as
  `MispFastLowerBound` (which ignores the edges between the selectable vertices) and costs the
  same per node. It replaces `MispFastLowerBound` in the MISP examples and tests. On `data/MISP`
  (20 s), A*, ACS and DDO now prove 14/15 instances instead of 13/15; `B_100_150_2000` is solved
  in 30 ms to 140 ms instead of not at all, and A* finds no solution on 1 instance instead of 2.
- `NbUnassignedWidth`: the default width heuristic of ddo, equal to the number of unassigned
  variables of the subproblem (times an optional factor). The solvers now call the new method
  `WidthHeuristic.maximumWidth(state, depth)` with the depth of the subproblem; by default it
  returns `maximumWidth(state)`, so that the existing heuristics are unchanged. On the 50 TSPTW
  AFG instances (DDO, 20 s), it proves 4 instances, against 10 with a fixed width of 100: these
  instances have at most 51 variables, so that the width is always smaller than 100.
- `MispDominanceChecker`: a dominance checker for the layered MISP, used by its examples. The
  dominance by inclusion forbids grouping the states by a key, so that `SimpleDominanceChecker`
  compares each new state with all the states of its layer. This checker groups them by their
  number of remaining vertices, like `MispNoLayerDominanceChecker`: a state is only compared with
  the states that can dominate it or that it can dominate. It prunes exactly the same states. On
  the two largest instances of `data/MISP` (100 vertices, DDO, 20 s), DDO does 1.7 and 1.8 times
  more iterations, with a gap 2 to 3 points smaller; the other instances are solved in a few
  milliseconds either way.
- `SearchStatus.MEMOUT`: when the memory runs out, every solver (A*, AWA*, ACS, DDO, LNS, exact,
  DFS, layered and no-layer) now returns the best solution found so far with this status, its
  statistics (bound, gap, node counts) and frees its frontier, instead of letting the
  `OutOfMemoryError` escape (the solution was then only saved by the `onSolution` callback).
  `StopCriterion.maxFrontierSize(n)` is now an optional memory limit: when the frontier (or the
  open list) reaches `n` nodes, the search stops with the status `MEMOUT`, also when the limit is
  combined with other criteria. It is signaled by the new `MemoryLimitException`, caught by the
  solvers; previously the search stopped with the status `SAT` or `UNKNOWN`, like a time limit.
  For the LNS, which has no frontier, the size is its number of iterations.
- `SearchStatistics.nbExpandedNodes()` and `nbGeneratedNodes()` for every solver (A*, AWA*, ACS,
  DDO, LNS, exact, DFS, layered and no-layer), to compare the search effort of the solvers, for
  instance with the `expanded` / `generated` counts of DIDP. A node is expanded when its
  successors are computed, and one node is generated per successor computed (one per transition
  applied, before any pruning). For the DD-based solvers, they are counted in the compiled
  decision diagrams (`DecisionDiagram.nbExpandedNodes()` and `nbGeneratedNodes()`). Previously
  only `DdoStats.totalNodes()` (distinct nodes created in the DDs) was available. Both counts are
  also printed by `toString()`.
- `Problem.hasNonNegativeCosts()` (layered and no-layer, `false` by default): a model without
  fast lower bound can declare that its transition costs are non-negative, so that A*, AWA* and
  ACS can use the cost of the best open node as lower bound.
- `SearchStatistics.bestBound()`: the best lower bound on the optimal value proved so far, for
  every solver (A*, AWA*, ACS and DDO, layered and no-layer, and LNS). It is known when the search
  is stopped early, for instance by a time limit, and does not require an incumbent. It is the
  incumbent value when the search is `OPTIMAL` and `+∞` when the problem is `UNSAT`. Previously
  only `DdoStats.bestLowerBound()` existed, so the bound of A* was not available. The bound is
  also printed by `toString()`.

### Fixed

- Talent scheduling: the reader of `TSProblem` failed with a `NumberFormatException` on the
  numbers of scenes and actors written on one line, and on lines starting with spaces (e.g. the
  `Shaw2020` and `Warwick1201` files of ddo), two variants accepted by ddo and DIDP. After the
  first line (name and optional optimum), the file is now read as a sequence of integers
  separated by any whitespace, so that both forms of the header, the leading spaces and the blank
  lines are accepted. A truncated or invalid file now raises an `IOException` naming the file.
- MSCT: `MSCTDominance.getKey()` returned `0`, so that each state was compared with all the
  states of its layer, although only states with the same remaining jobs can dominate each other.
  The key is now the set of remaining jobs, like `SMICDominance`. The pruning is unchanged; DDO
  (width 100, 20 s) is 1.6 to 2.4 times faster on the instances it solves (14 tasks: 12.6 s to
  5.3 s) and does 1.3 to 7 times more iterations on the others, with a smaller gap. The number of
  solved instances is unchanged. `MispDominance.getKey()` keeps a single key, required by a
  dominance by inclusion; its Javadoc documents the cost of this choice.
- `Solvers.restrictedDdo` stopped the JVM (`System.exit(0)`) when the restricted DD contained no
  solution. It now returns an empty solution, with the status `UNSAT` if the DD is exact (the
  problem is infeasible) or `UNKNOWN` otherwise (the restriction dropped the feasible nodes).
- `Solvers.relaxedDdo` and `Solvers.restrictedDdo` always failed with a `NullPointerException`:
  `RelaxationSolver` and `RestrictionSolver` did not set `CompilationConfig.useLNS`. They now
  set it to `false`, like the DDO solver.
- DDO: the stop criterion (e.g. a time limit) was only tested between two subproblems, never
  during the compilation of a decision diagram, so that the limit could be exceeded by the
  compilation of a single DD (25 s on a TSPTW instance with 233 nodes, width 100 and a 5 s limit).
  It is now tested at each layer of the restricted and relaxed compilations; an interrupted DD is
  discarded, its subproblem is put back in the frontier and the best solution found so far is
  returned. The gap is also computed before the next subproblem is popped from the frontier, so
  that it no longer ignores the lower bound of that subproblem.
- A*, AWA* and ACS (layered and no-layer): without fast lower bound, the gap was computed with the
  cost of the best open node, which is not a lower bound when costs can be negative (maximization
  problems modeled with negated profits): the reported gap could be smaller than the true gap.
  This cost is now only used when `Problem.hasNonNegativeCosts()` is `true`; otherwise no bound is
  known and the gap is infinite. The no-layer ACS also did not detect the default fast lower bound
  (it returns 0, not `Integer.MIN_VALUE`).
- LNS: the dominance checker is now obtained once and cleared before each compilation, as in the
  DDO solver. Previously the LNS called `LnsModel.dominance()` at each compilation and never
  cleared the checker: when a model returned always the same checker, the states remembered from
  other neighborhoods pruned the states of the next DDs (a state seen before with the same value
  was dominated by its own copy). The LNS could then report a non-optimal solution as optimal or a
  feasible problem as `UNSAT` (e.g. TSPTW `rbg021.6`: 7220 reported as optimal, the optimum is
  4492). The result no longer depends on how the model provides its checker. The statuses of the
  LNS are documented: `OPTIMAL` and `UNSAT` are only reported with a global dual bound (exact DD
  compiled from the root, or incumbent reaching the fast lower bound at the root).
- A*-family stats: the moving average behind `validChildrenPercent` now also takes into account
  the expanded nodes whose children are all pruned or redundant. Previously these nodes were
  ignored, so the indicator decreased far too slowly to signal stagnation.
- A* and ACS (layered and no-layer) now update `validChildrenPercent`. Previously only AWA* did,
  so the `minValidChildrenPercent` stop criterion could never trigger with A* or ACS.
- No-layer ACS: a better solution found while generating children now updates the incumbent in
  the statistics and is reported to the `onSolution` callback. Previously only the internal upper
  bound was updated.

## [0.2.0] - 26/10/2026

### Fixed

- LNS: the restriction of the restricted decision diagrams is now applied before the children
  are generated. Previously, the width of the diagrams compiled by the LNS solver was never
  bounded, which could lead to very slow iterations or memory exhaustion on large instances.
- LNS: the width of a restricted layer never exceeds the maximum width anymore. The nodes
  consistent with the incumbent are kept first, then the randomly sampled ones, then the
  remaining nodes ordered by lower bound.
- LNS: the solver no longer claims optimality after an exact compilation of a neighborhood in
  which some variables are fixed. Optimality is only proved by an exact compilation from the root
  or when the incumbent reaches the global lower bound.
- LNS: the reported gap is now computed with a global lower bound (the fast lower bound at the
  root) instead of the lower bound of the last explored neighborhood, so that it is a valid
  optimality gap.
- LNS: when the initial solution is never improved, it is now returned as the best solution (it was previously returned
  empty).
- LNS: `fixWidth`, `setInitialSolution` and `setProbability` now keep all the LNS parameters of
  the model (initial solution, probability, restriction strategy, state distance).
- LNS: as long as no solution is known, the restricted DD compiled from the root was the same at
  each iteration, so the solver could loop forever without finding a solution (e.g. tight TSPTW
  instances). Its width is now doubled at each iteration until a solution is found.
- LNS: an infeasible problem is now reported as `UNSAT` (it was reported as `OPTIMAL`).
- MaxCover: `evaluate` failed on the decision `-1` used once all the items are covered.
- MKS: an optimum of `0` in the instance files means that the optimum is unknown (OR-Library
  format); it was read as an optimum of 0.
- MSCT: on a merged state, `MSCTFastLowerBound` summed the completion times of all the jobs that
  can still be selected, although only the remaining number of decisions is scheduled. The bound
  overestimated the remaining cost and the DDO solver could return a non-optimal solution as
  optimal. It now sums the smallest completion times only.
- TSPTW: when the sum of the remaining cheapest edges was 0 (zero-cost edges), `TSPTWFastLowerBound`
  used the direct edge from the current position to the depot, which can be longer than a detour
  through the remaining nodes. The bound could overestimate the remaining cost and the DDO solver
  could return a non-optimal solution as optimal. The direct edge is now only used when no node
  remains to visit.
- PDPTW: `PDPTWFastLowerBound` paired the i-th smallest travel time with the i-th smallest earliest
  start, which is not a relaxation: it could overestimate the remaining cost and declare a feasible
  state infeasible. It is replaced by a valid bound (the arrival time at each position is at least
  `t + pre[i]` and `max(t, e(j)) + pre[i - j]`), which is also about twice tighter on the instances
  of the project.
- BKS: `BKSFastLowerBound` is now the linear relaxation of the bounded knapsack. It used to add
  the weights of the items to their values and to ignore the capacity already used, which gave a
  very weak bound (LNS gaps above 100 %).
- PSP: the stocking cost part of `PSFastLowerBound` was negative (sign inherited from the
  maximization formulation of the original ddo solver), counted some demands several times and
  scheduled the cheapest demands first. The bound is now positive, which gave LNS gaps above
  100 % before.
- PDPTW generator: the case removing both time windows of a pickup/delivery pair was never drawn (`random.nextInt(2)`
  instead of `random.nextInt(3)`). The generated instances change for a
  given seed; the instance files of the tests are not affected.

### Added

- LNS: `LnsModel.seed()` and `LnsModel.setSeed(long)`. The LNS search is now deterministic by
  default (`LnsModel.DEFAULT_SEED`), as in the original ddo solver.
- Checkstyle configuration based on the Google Java Style (with a 4-space indentation and the
  abbreviations allowed in names to keep the public API), run with `mvn checkstyle:check` and in a
  non-blocking CI workflow (the warnings are reported but never fail the build).
- DFS based start heuristic.
- Add statistic counting the number of pruning done by the `FastLowerBound`
- Add statistic counting the number of pruning done by the `Dominance`

### Changed

- The whole code base follows the Checkstyle configuration (0 warning). The sources are formatted
  with google-java-format in AOSP mode (4-space indentation) with the Google import order. Braces
  were added around all the `if`/`else`/`for`/`while` bodies, star imports were replaced by
  explicit imports, missing Javadoc was written and non-public members were renamed. The public
  API is unchanged.
- The package-private classes `ALPSchedule`, `TSPNode`, `VirtualNodes` and the no-layer
  `*DdoModel` example classes were moved to their own source files.
- All the solver can use an initial solution

## [0.1.0] - 03/07/2026

Foundation of the DDOLib library.

- Implementation of a decision diagram based on decision
- Modeling interfaces
    - `Problem`
    - `Relaxation`
    - `FastLowerBound`
    - `Ranking`
    - `Dominance`
- Implementation of a simple cache
- Various solver:
    - Decision Diagram based Optimization solver (`SequentialSolver`)
    - A* based solver (`AStarSolver`)
    - Anytime Column Search based solver (`ACSSolver`)
    - Large Neighborhood Search based solver (`LNSSolver`)
- Precomputed upper bound mechanism
- Clustering mechanism for the relaxation
- Debug mode to check to lower bound admissibility
- Verbose mode
- Export diagram to `.dot` file
- User API
- Various academic example
- Implementation of a generic test bench for examples
- No layer API (beta)
