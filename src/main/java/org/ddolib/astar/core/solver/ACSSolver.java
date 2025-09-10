package org.ddolib.astar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;

import java.util.*;

import static java.lang.Math.*;

public final class ACSSolver<T, K> implements Solver {

    /**
     * The problem we want to maximize
     */
    private final Problem<T> problem;
    /**
     * A suitable ub for the problem we want to maximize
     */
    private final FastUpperBound<T> ub;
    /**
     * A heuristic to choose the next variable to branch on when developing a DD
     */
    private final VariableHeuristic<T> varh;

    /**
     * Value of the best known lower bound.
     */
    private double bestLB;

    /**
     * If set, this keeps the info about the best solution so far.
     */
    private Optional<Set<Decision>> bestSol;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final DominanceChecker<T, K> dominance;

    private HashSet<T>[] closed;

    private HashSet<T>[] present;

    private HashMap<T, Double> g;

    private final int K;

    private final int timeout;

    private double min;

    private double max;



    private ArrayList<PriorityQueue<SubProblem<T>>> open = new ArrayList<>();


    /**
     * <ul>
     *     <li>0: no verbosity</li>
     *     <li>1: display newBest whenever there is a newBest</li>
     *     <li>2: 1 + statistics about the front every half a second (or so)</li>
     *     <li>3: 2 + every developed sub-problem</li>
     *     <li>4: 3 + details about the developed state</li>
     * </ul>
     * <p>
     * <p>
     * 3: 2 + every developed sub-problem
     * 4: 3 + details about the developed state
     */
    private final int verbosityLevel;

    /**
     * Whether we want to export the first explored restricted and relaxed mdd.
     */
    private final boolean exportAsDot;

    /**
     * Creates a fully qualified instance. The parameters of this solver are given via a
     * {@link SolverConfig}<br><br>
     *
     * <b>Mandatory parameters:</b>
     * <ul>
     *     <li>An implementation of {@link Problem}</li>
     *         <li>An implementation of {@link FastUpperBound}</li>
     *     <li>An implementation of {@link VariableHeuristic}</li>
     * </ul>
     * <br>
     * <b>Optional parameters: </b>
     * <ul>
     *     <li>An implementation of {@link DominanceChecker}</li>
     *     <li>A verbosity level</li>
     * </ul>
     *
     * @param config All the parameters needed to configure the solver.
     */
    public ACSSolver(
            SolverConfig<T, K> config,
            final int K) {
        this.problem = config.problem;
        this.varh = config.varh;
        this.ub = config.fub;
        this.dominance = config.dominance;
        this.bestLB = config.lb;
        this.bestSol = Optional.empty();
        this.closed = new HashSet[problem.nbVars() + 1];
        this.present = new HashSet[problem.nbVars() + 1];
        this.g = new HashMap<>();
        this.K = K;
        this.timeout = config.timeLimit;
        if (config.debugLevel != 0) {
            throw new IllegalArgumentException("The debug mode for this solver is not available " +
                    "for the moment.");
        }

        for (int i = 0; i < problem.nbVars() + 1; i++) {
            open.add(new PriorityQueue<>(Comparator.comparingDouble(SubProblem<T>::f).reversed()));
            present[i] = new HashSet<>();
            closed[i] = new HashSet<>();
        }

        this.verbosityLevel = config.verbosityLevel;
        this.exportAsDot = config.exportAsDot;

    }

    private boolean allEmpty() {
        for (PriorityQueue<SubProblem<T>> q : open) {
            if (!q.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public SearchStatistics maximize() {
        long t0 = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        open.get(0).add(root());
        present[0].add(root().getState());
        g.put(root().getState(), 0.0);
        min = root().f();
        max = min;
        ArrayList<SubProblem<T>> candidates = new ArrayList<>();
        while (!allEmpty()) {
            for (int i = 0; i < problem.nbVars() + 1; i++) {
                int l = min(K, open.get(i).size());
                if(l<K) {
                    for (int j = 0; j < l; j++) {
                        SubProblem<T> s = open.get(i).poll();
                        present[i].remove(s.getState());
                        if (s.f() > bestLB) {
                            candidates.add(s);
                        }
                    }
                }else{
                    ArrayList<SubProblem<T>> toAdd = new ArrayList<>();
                    while(candidates.size()<K){
                        if (open.get(i).isEmpty()) {
                            for (SubProblem<T> subProblem : toAdd) {
                                open.get(i).add(subProblem);
                            }
                            toAdd.clear();
                        }
                        SubProblem<T> s = open.get(i).poll();
                        if (s.f()==max){
                            candidates.add(s);
                        }else {
                            double prob = abs((s.f() - min) / (max - min)) + 0.01;
                            if (verbosityLevel > 0) {
                                System.out.println("prob = " + prob + " s :" + s.f() + " min = " + min + " max = " + max);
                            }
                            double r = random();
                            if (r <= prob) {
                                candidates.add(s);
                            } else {
                                toAdd.add(s);
                            }
                        }

                    }
                    for (SubProblem<T> subProblem : toAdd) {
                        open.get(i).add(subProblem);
                    }
                }
                min = Integer.MAX_VALUE;
                max = Integer.MIN_VALUE;
                for (int k = 0; k < candidates.size(); k++) {
                    if (verbosityLevel >= 2) {
                        System.out.println("it " + nbIter + "\t frontier:" + candidates.get(k) + "\t " + "bestObj:" + bestLB);
                    }
                    nbIter++;

                    SubProblem<T> sub = candidates.get(k);
                    this.closed[i].add(sub.getState());
                    if (sub.getPath().size() == problem.nbVars()) {
                        // optimal solution found
                        if (bestLB < sub.getValue()) {
                            bestSol = Optional.of(sub.getPath());
                            bestLB = sub.getValue();
                            problem.setLB(bestLB);
                            if (verbosityLevel >= 1) {
                                System.out.println("NEWOBJ:" + bestLB);
                            }
                        }
                        continue;
                    }
                    addChildren(sub, i + 1);
                }
                candidates.clear();
            }

            if (System.currentTimeMillis() - t0> timeout* 1000L){
                return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.UNKNOWN, gap());
            }

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, open.stream().mapToInt(q -> q.size()).sum());
        }
        return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.OPTIMAL, 0.0);
    }

    @Override
    public Optional<Double> bestValue() {
        if (bestSol.isPresent()) {
            return Optional.of(bestLB);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        return bestSol;
    }

    /**
     * @return the root subproblem
     */
    private SubProblem<T> root() {
        return new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Integer.MAX_VALUE,
                Collections.emptySet());
    }


    private void addChildren(SubProblem<T> subProblem, int varIndex) {
        T state = subProblem.getState();
        int var = varIndex - 1;
        final Iterator<Integer> domain = problem.domain(state, var);
        while (domain.hasNext()) {
            final int val = domain.next();
            final Decision decision = new Decision(var, val);
            T newState = problem.transition(state, decision);
            if(newState==null){
                continue;
            }
            double cost = problem.transitionCost(state, decision);
            double value = subProblem.getValue() + cost;
            Set<Decision> path = new HashSet<>(subProblem.getPath());
            path.add(decision);
            double fastUpperBound = ub.fastUpperBound(newState, varSet(path), bestLB);
            // if the new state is dominated, we skip it
            if (!dominance.updateDominance(newState, path.size(), value)) {
                SubProblem<T> newSubProblem = new SubProblem<>(newState, value, fastUpperBound, path);
                if (((present[varIndex].contains(newState) || closed[varIndex].contains(newState)) && g.getOrDefault(newState, Double.MAX_VALUE) > value) || newSubProblem.f() <= bestLB) {
                    continue;
                }
                g.put(newState, value);
                open.get(varIndex).add(newSubProblem);
                min = min(min,subProblem.f());
                max = max(max,subProblem.f());
                if (closed[varIndex].contains(newState)) {
                    closed[varIndex].remove(newState);
                }
            }
        }
    }

    private Set<Integer> varSet(Set<Decision> path) {
        final HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            set.add(i);
        }
        for (Decision d : path) {
            set.remove(d.var());
        }
        return set;
    }

    private double gap() {
        if (allEmpty()) {
            return 0.0;
        } else {
            double bestInFrontier = bestInFrontier();
            return 100 * (bestInFrontier - bestLB) / bestInFrontier;
        }
    }
    public double bestInFrontier() {
        double bestInFrontier = Double.MIN_VALUE;
        for (PriorityQueue<SubProblem<T>> q : open) {
           if (!q.isEmpty()) {
               bestInFrontier = max(bestInFrontier,q.peek().f());
           }
        }
        return bestInFrontier;
    }
}
