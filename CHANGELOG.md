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

### Added

- LNS: `LnsModel.seed()` and `LnsModel.setSeed(long)`. The LNS search is now deterministic by
  default (`LnsModel.DEFAULT_SEED`), as in the original ddo solver.

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
