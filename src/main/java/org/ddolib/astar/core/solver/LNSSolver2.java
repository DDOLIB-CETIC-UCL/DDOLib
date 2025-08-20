package org.ddolib.astar.core.solver;

import org.ddolib.astar.examples.JobShop.JSProblem;
import org.ddolib.astar.examples.JobShop.JSState;
import org.ddolib.common.dominance.AstarDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;

import java.util.*;

import static java.lang.Math.min;

public class LNSSolver2<T, K> implements Solver {

    /**
     * The problem we want to maximize
     */
    public final JSProblem problem;
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

    private T bestState;

    private final long t0;

    /**
     * The dominance object that will be used to prune the search space.
     */
    private final AstarDominanceChecker<T, K> dominance;

    private int nRestart;

    private final int nMaxRestart;
    private int nMaxFail;

    private HashMap<T, Double> g;

    private final int K;

    private ArrayList<PriorityQueue<SubProblem<T>>> open = new ArrayList<>();



    public LNSSolver2(
            final JSProblem problem,
            final VariableHeuristic<T> varh,
            final FastUpperBound<T> ub,
            final AstarDominanceChecker<T, K> dominance,
            final int K) {
        this.problem = problem;
        this.varh = varh;
        this.ub = ub;
        this.dominance = dominance;
        this.bestLB = Integer.MIN_VALUE;
        this.bestSol = Optional.empty();
        this.g = new HashMap<>();
        this.K = K;
        this.nRestart = 0;
        this.t0 = System.currentTimeMillis();
        this.nMaxRestart = 100;
        this.nMaxFail = 1000;

        for (int i = 0; i < problem.nbVars()+1; i++) {
            open.add(new PriorityQueue<>(Comparator.comparingDouble(SubProblem<T>::fam).reversed()));
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
    public void setLB(double LB){
        this.bestLB = LB;
    }

    public boolean relaxation(T solution, double lb){
        this.bestLB = lb;
        this.bestState = solution;
        return this.problem.getRelaxation((JSState) solution);
    }
    public void reset(){
        this.problem.removePrecedenceConstraint();
        this.dominance.reset();
        this.g.clear();
    }
    public void reset2(){
        this.bestSol = Optional.empty();
        this.bestLB = Integer.MIN_VALUE;
        this.bestState = null;
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel, boolean exportAsDot) {

//        System.out.println("bestLB : "+ this.bestLB);
//        System.out.println("bestState : "+this.bestState);
//        System.out.println("bestSol : "+ this.bestSol);
        int nbIter = 0;
        int queueMaxSize = 0;
        open.get(0).add(root());
        g.put(root().getState(), 0.0);
        ArrayList<SubProblem<T>> candidates = new ArrayList<>();
        int nFails = 0;
        while (!allEmpty()) {
            for (int i = 0; i < problem.nbVars()+1; i++) {
                int l = min(K, open.get(i).size());
                while (candidates.size() < l) {
                    SubProblem<T> s = open.get(i).poll();
                    if (s==null){
                        break;
                    }
                    if (s.f() > bestLB || !this.dominance.dominated(s.getState(),s.getValue(),i)) {
                        if(!this.problem.fullExplored((JSState) s.getState())){
                            candidates.add(s);
                        }

                    }
                }
                if (!candidates.isEmpty()) {
                    for (int k = 0; k < candidates.size(); k++) {
                        nbIter++;

                        SubProblem<T> sub = candidates.get(k);
                        if (sub.getPath().size() == problem.nbVars()) {
                            // optimal solution found
                            if (bestLB < sub.getValue()) {
                                bestSol = Optional.of(sub.getPath());
                                bestLB = sub.getValue();
                                bestState = sub.getState();
                                this.problem.resetPercentage();
                                if (verbosityLevel >= 1) {
                                    System.out.println("it " + nbIter + "\t frontier:" + candidates.get(k) + "\t " + "bestObj:" + bestLB + "\t " + "time:" + (System.currentTimeMillis() - t0));
                                }
                            }
                            continue;
                        }
                        addChildren(sub, i + 1);
                    }
                    candidates.clear();
                }else if (i==problem.nbVars()){
                    nFails+=1;
                    if(nRestart==this.nMaxRestart){
                        return new SearchStatistics(nbIter, queueMaxSize, System.currentTimeMillis() - t0, SearchStatistics.SearchStatus.OPTIMAL, 0.0);
                    }
                    if (nFails==this.nMaxFail){
                        System.out.println("------Restart LNS-------");
                        this.reset();
                        this.problem.getRelaxation((JSState) bestState);
                        this.nRestart+=1;
                        return maximize(verbosityLevel, exportAsDot);
                    }
                    if (nRestart>0 && nRestart%5==0){
                        this.problem.increasePercentage();
                        this.nMaxFail -= (int) (this.nMaxFail*0.2);
                    }
                }
            }

            nbIter++;
            queueMaxSize = Math.max(queueMaxSize, open.stream().mapToInt(q -> q.size()).sum());
        }
        this.reset();
        this.problem.getRelaxation((JSState) bestState);
        this.nRestart+=1;
        return maximize(verbosityLevel, exportAsDot);

    }

    @Override
    public SearchStatistics maximize() {
        return maximize(0, false);
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

    public void increasePercentage(){
        problem.increasePercentage();
    }

    public void resetPercentage(){
        problem.resetPercentage();
    }

    public Optional<T> bestState() {
        if (bestSol.isPresent()) {
            return Optional.of(bestState);
        }else{
            return Optional.empty();
        }
    }

    /**
     * @return the root subproblem
     */
    private SubProblem<T> root() {
        return new SubProblem<T>(
                (T) problem.initialState(),
                problem.initialValue(),
                Integer.MAX_VALUE,
                Collections.emptySet());
    }


    private void addChildren(SubProblem<T> subProblem, int varIndex) {
        T state = subProblem.getState();
        int var = varIndex-1;
        final Iterator<Integer> domain = problem.domain((JSState) state, var);
        while (domain.hasNext()) {
            final int val = domain.next();
            final Decision decision = new Decision(var, val);
            T newState = (T) problem.transition((JSState) state, decision);
            double cost = problem.transitionCost((JSState)state, decision);
            double value = subProblem.getValue() + cost;

            Set<Decision> path = new HashSet<>(subProblem.getPath());
            path.add(decision);
            double fastUpperBound = ub.fastUpperBound( newState, varSet(path));
            // if the new state is dominated, we skip it
            if (!dominance.updateDominance(newState,path.size(),value)) {
                SubProblem<T> newSubProblem = new SubProblem<>(newState, value, fastUpperBound,path);
                g.put(newState,value);
                open.get(varIndex).add(newSubProblem);

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
}
