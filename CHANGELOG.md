# Changelog

This file documents the DDOLib changes.

## [1.0.0]

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
