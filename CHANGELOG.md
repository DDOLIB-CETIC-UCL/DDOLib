# Changelog

This file documents the DDOLib changes.

## [Unreleased]

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
- LNS: when the initial solution is never improved, it is now returned as the best solution
  (it was previously returned empty).
- LNS: `fixWidth`, `setInitialSolution` and `setProbability` now keep all the LNS parameters of
  the model (initial solution, probability, restriction strategy, state distance).
- BKS: `BKSFastLowerBound` is now the linear relaxation of the bounded knapsack. It used to add
  the weights of the items to their values and to ignore the capacity already used, which gave a
  very weak bound (LNS gaps above 100 %).
- PSP: the stocking cost part of `PSFastLowerBound` was negative (sign inherited from the
  maximization formulation of the original ddo solver), counted some demands several times and
  scheduled the cheapest demands first. The bound is now positive, which gave LNS gaps above
  100 % before.
- PDPTW generator: the case removing both time windows of a pickup/delivery pair was never drawn
  (`random.nextInt(2)` instead of `random.nextInt(3)`). The generated instances change for a
  given seed; the instance files of the tests are not affected.

### Added

- LNS: `LnsModel.seed()` and `LnsModel.setSeed(long)`. The LNS search is now deterministic by
  default (`LnsModel.DEFAULT_SEED`), as in the original ddo solver.
- Checkstyle configuration based on the Google Java Style (with a 4-space indentation and the
  abbreviations allowed in names to keep the public API), run with `mvn checkstyle:check` and in a
  blocking CI workflow (any warning fails the build).

### Changed

- The whole code base follows the Checkstyle configuration (0 warning). The sources are formatted
  with google-java-format in AOSP mode (4-space indentation) with the Google import order. Braces
  were added around all the `if`/`else`/`for`/`while` bodies, star imports were replaced by
  explicit imports, missing Javadoc was written and non-public members were renamed. The public
  API is unchanged.
- The package-private classes `ALPSchedule`, `TSPNode`, `VirtualNodes` and the no-layer
  `*DdoModel` example classes were moved to their own source files.

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
