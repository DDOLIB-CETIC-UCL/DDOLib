package org.ddolib.astar.core.solver;

import org.ddolib.astar.examples.JobShop.JSProblem;
import org.ddolib.astar.examples.JobShop.JSState;
import org.ddolib.common.dominance.AstarDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ACSwLNSSolver <T, K> implements Solver {

    /**
     * The problem we want to maximize
     */
    private final JSProblem problem;
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

    private T  bestState;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final AstarDominanceChecker<T, K> dominance;

    private  HashSet<T>[] closed;

    private  HashSet<T>[] present;

    private HashMap<T, Double> g;

    private final int K;

    private LNSSolver<T,K> LNSSolver;

    private final int timeLimit;


    private ArrayList<PriorityQueue<SubProblem<T>>> open = new ArrayList<>();

    /**
     * Creates a fully qualified instance
     *
     * @param problem   The problem we want to maximize.
     * @param ub        A suitable upper-bound for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param dominance The dominance object that will be used to prune the search space.
     */
    public ACSwLNSSolver(
            final JSProblem problem,
            final VariableHeuristic<T> varh,
            final FastUpperBound<T> ub,
            final AstarDominanceChecker<T, K> dominance,
            final int K, final LNSSolver LNSSolver,
            final int timeLimit) {
        this.problem = problem;
        this.varh = varh;
        this.ub = ub;
        this.dominance = dominance;
        this.bestLB = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
        this.closed = new HashSet[problem.nbVars()+1];
        this.present = new HashSet[problem.nbVars()+1];
        this.g = new HashMap<>();
        this.K = K;
        this.LNSSolver = LNSSolver;
        this.timeLimit = timeLimit;

        for (int i = 0; i < problem.nbVars()+1; i++) {
            open.add(new PriorityQueue<>(Comparator.comparingDouble(SubProblem<T>::f).reversed()));
            present[i] = new HashSet<>();
            closed[i] = new HashSet<>();
        }

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
        return maximize(0, false);
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel, boolean exportAsDot) {
        long t0 = System.currentTimeMillis();
        int nbIter = 0;
        int queueMaxSize = 0;
        int nbFailed = 0;
        open.get(0).add(root());
        present[0].add(root().getState());
        g.put(root().getState(), 0.0);
        ArrayList<SubProblem<T>> candidates = new ArrayList<>();
        while (!allEmpty()) {
            for (int i = 0; i < problem.nbVars()+1; i++) {
                int l = min(K, open.get(i).size());
                while (candidates.size() < l) {
                    SubProblem<T> s = open.get(i).poll();
                    if (s==null){
                        break;
                    }
                    present[i].remove(s.getState());
                    if (s.f() > bestLB || !this.dominance.dominated(s.getState(),s.getValue(),i)) {
                        candidates.add(s);
                    }else{
                        this.LNSSolver.problem.childrenFullExplored((JSState) s.getState());
                    }
                }
                if (!candidates.isEmpty()) {

                    for (int k = 0; k < candidates.size(); k++) {

                        nbIter++;

                        SubProblem<T> sub = candidates.get(k);
                        this.closed[i].add(sub.getState());
                        if (sub.getPath().size() == problem.nbVars()) {
                            // optimal solution found
                            this.LNSSolver.problem.childrenFullExplored((JSState) sub.getState());
                            if (bestLB < sub.getValue()) {
                                bestSol = Optional.of(sub.getPath());
                                bestLB = sub.getValue();
                                bestState = sub.getState();
                                this.LNSSolver.resetPercentage();
                                if (verbosityLevel >= 1) {
                                    System.out.println("it " + nbIter + "\t sol:" + candidates.get(k) + "\t " + "bestObj:" + bestLB);
                                }
                            }
                            continue;
                        }
                        addChildren(sub, i + 1);
                    }
                    candidates.clear();
                }else if (i==problem.nbVars()){
                    nbFailed+=1;
                    if (nbFailed%5==0 ){
                        if (verbosityLevel >= 1) {
                            System.out.println("----LNSSolver relaxation----");
                        }
                        if (!this.LNSSolver.relaxation(bestState, bestLB)){
                            if (verbosityLevel >= 1) {
                                System.out.println("---- END LNSSolver relaxation----");
                            }

                            return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.OPTIMAL, 0.0, bestLB);
                        };

                        this.LNSSolver.maximize(0, false);
                        Optional<T> sol = this.LNSSolver.bestState();

                        if (sol.isPresent()){
                            bestSol = this.LNSSolver.bestSolution();
                            bestLB = this.LNSSolver.bestValue().get();
                            bestState = sol.get();
                            this.LNSSolver.resetPercentage();
                            if (verbosityLevel >= 1) {
                                System.out.println("it " + nbIter + "\t sol:" + bestState + "\t " + "bestObj:" + bestLB);
                            }
                        }else{
                            this.LNSSolver.increasePercentage();
                        }
                        this.LNSSolver.reset();
                        if (verbosityLevel >= 1) {
                            System.out.println("---- END LNSSolver relaxation----");
                        }
                        nbFailed=0;
                    }
                }
            }
            if (System.currentTimeMillis() - t0 > 1000 * timeLimit){
                return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.UNKNOWN, gap(), bestLB);
            }

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, open.stream().mapToInt(q -> q.size()).sum());
        }
        return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.OPTIMAL, 0.0, bestLB);
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

    private double gap() {
        if (this.allEmpty()) {
            return 0.0;
        } else {
            double bestInFrontier = this.bestInFrontier();
            return 100 * (bestInFrontier - bestLB) / bestLB;
        }
    }
    public double bestInFrontier() {
        double bestValue = Integer.MIN_VALUE;
        for (int i = 0; i < problem.nbVars()+1; i++) {
            if (!open.get(i).isEmpty()) {
                bestValue = max(bestValue, open.get(i).peek().f());
            }
        }
        return bestValue;
    }

    /**
     * @return the root subproblem
     */
    private SubProblem<T> root() {
        return (SubProblem<T>) new SubProblem<>(
                problem.initialState(),
                problem.initialValue(),
                Integer.MAX_VALUE,
                Collections.emptySet());
    }


    private void addChildren(SubProblem<T> subProblem, int varIndex) {
        T state = subProblem.getState();
        int var = varIndex-1;
        final Iterator<Integer> domain = problem.domain((JSState) state, var);
        int children = 0;
        while (domain.hasNext()) {
            final int val = domain.next();
            final Decision decision = new Decision(var, val);
            T newState = (T) problem.transition((JSState) state, decision);
            double cost = problem.transitionCost((JSState) state, decision);
            double value = subProblem.getValue() + cost;

            Set<Decision> path = new HashSet<>(subProblem.getPath());
            path.add(decision);
            double fastUpperBound = ub.fastUpperBound(newState, varSet(path));
            // if the new state is dominated, we skip it
            if (!dominance.updateDominance(newState,path.size(),value)) {
                SubProblem<T> newSubProblem = new SubProblem<>(newState, value, fastUpperBound,path);
                if(((present[varIndex].contains(newState) || closed[varIndex].contains(newState))&&g.getOrDefault(newState,Double.MAX_VALUE)>value)||newSubProblem.f()<=bestLB){
                    continue;
                }
                g.put(newState,value);
                open.get(varIndex).add(newSubProblem);
                children++;
                if (closed[varIndex].contains(newState)) {
                    closed[varIndex].remove(newState);
                }
            }
        }
        this.LNSSolver.problem.addInFullExplored((JSState) state,children);
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
}
