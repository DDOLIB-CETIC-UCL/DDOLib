[Taken from or-library](https://people.brunel.ac.uk/~mastjjb/jeb/orlib/mknapinfo.html)

 There are 11 data files.
  
 The first data file is mknap1.
  
 This data file contains 7 test problems which are
 the test problems from C.C.Petersen "Computational experience
 with variants of the Balas algorithm applied to the selection
 of R&D projects" Management Science 13(9) (1967) 736-750.
  
 The problem to be solved is:
 Max  sum{j=1,...,n} p(j)x(j)
 st   sum{j=1,...,n} r(i,j)x(j) <= b(i)       i=1,...,m
                     x(j)=0 or 1
  
 The format of this data file is:
 number of test problems (K)
 then for each test problem k (k=1,...,K) in turn:
    number of variables (n), number of constraints (m), optimal
    solution value (zero if unavailable)
    the coefficients p(j); j=1,...,n
    for each constraint i (i=1,...,m): the coefficients r(i,j); j=1,...,n
    the constraint right-hand sides b(i); i=1,...,m
  
 The second data file is mknap2.
 This data file contains 48 test problems taken
 from the literature. The format of these problems
 is described within the file.

 The remaining data files are the problems solved in P.C.Chu and
 J.E.Beasley "A genetic algorithm for the multidimensional knapsack
 problem", Journal of Heuristics, vol. 4, 1998, pp63-86.

 These data files are mknapcb1, mknapcb2, ..., mknapcb9

 The format of these data files is the same as the format of mknap1
 These data files each contain 30 test problems, the first ten problems 
 have a tightness ratio of 0.25, the second ten problems have a tightness
 ratio of 0.50 and the last ten problems have a tightness ratio of 0.75 (see
 the above paper).

 The best feasible solution values found and the value of the LP 
 relaxation for these problems are given in the file mkcbres

 The largest file is mknapcb9 of size 2000Kb (approximately)
 The entire set of files are of size 5400KB (approximately).