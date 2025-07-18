package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.compilation.CompilationInput;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.mdd.DecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The branch and bound with mdd paradigm parallelizes *VERY* well. This is why
 * it has been chosen to show students how to implement such a parallel solver.
 * <p>
 * # Note:
 * IF YOU ARE INTERESTED IN READING THE BRANCH AND BOUND WITH MDD ALGORITHM TO
 * SEE WHAT IT LOOKS LIKE WITHOUT PAYING ATTENTION TO THE PARALLEL STUFFS, YOU
 * WILL WANT TO TAKE A LOOK AT THE `processOneNode()`. THIS IS WHERE THE INFO
 * YOU ARE LOOKING FOR IS LOCATED.
 *
 * @param <T> the type of state
 * @param <K> the type of key
 */
public final class ParallelSolver<T, K> implements Solver {
    /*
     * The various threads of the solver share a common zone of memory. That
     * zone of shared memory is split in two:
     */

    /**
     * The portion of the shared state that can be accessed concurrently
     */
    private final Shared<T, K> shared;
    /**
     * The portion of the shared state that can only be accessed from the critical sections
     */
    private final Critical<T> critical;

    /**
     * Creates a fully qualified instance
     *
     * @param nbThreads The number of threads that can be used in parallel.
     * @param problem   The problem we want to maximize.
     * @param relax     A suitable relaxation for the problem we want to maximize
     * @param varh      A heuristic to choose the next variable to branch on when developing a DD.
     * @param ranking   A heuristic to identify the most promising nodes.
     * @param width     A heuristic to choose the maximum width of the DD you compile.
     * @param frontier  The set of nodes that must still be explored before
     *                  the problem can be considered 'solved'.
     *                  <p>
     *                  # Note:
     *                  This fringe orders the nodes by upper bound (so the highest ub is going
     *                  to pop first). So, it is guaranteed that the upper bound of the first
     *                  node being popped is an upper bound on the value reachable by exploring
     *                  any of the nodes remaining on the fringe. As a consequence, the
     *                  exploration can be stopped as soon as a node with an ub &#8804; current best
     *                  lower bound is popped.
     * @param fub       The heuristic defining a very rough estimation (upper bound) of the optimal value.
     * @param dominance The dominance object that will be used to prune the search space.
     */
    public ParallelSolver(
            final int nbThreads,
            final Problem<T> problem,
            final Relaxation<T> relax,
            final VariableHeuristic<T> varh,
            final StateRanking<T> ranking,
            final WidthHeuristic<T> width,
            final Frontier<T> frontier,
            final FastUpperBound<T> fub,
            final DominanceChecker<T, K> dominance) {
        this.shared = new Shared<>(nbThreads, problem, relax, varh, ranking, width, fub, dominance);
        this.critical = new Critical<>(nbThreads, frontier);
    }

    @Override
    public SearchStatistics maximize() {
        return maximize(0, false);
    }

    @Override
    public SearchStatistics maximize(int verbosityLevel, boolean exportAsDot) {
        long start = System.currentTimeMillis();
        final AtomicInteger nbIter = new AtomicInteger(0);
        final AtomicInteger queueMaxSize = new AtomicInteger(0);

        initialize();

        Thread[] workers = new Thread[shared.nbThreads];
        for (int i = 0; i < shared.nbThreads; i++) {
            final int threadId = i;
            workers[i] = new Thread() {
                @Override
                public void run() {
                    DecisionDiagram<T, K> mdd = new LinkedDecisionDiagram<>();
                    while (true) {
                        Workload<T> wl = getWorkload(threadId);
                        switch (wl.status) {
                            case Complete:
                                return;
                            case Starvation:
                                continue;
                            case WorkItem:
                                nbIter.incrementAndGet();
                                queueMaxSize.updateAndGet(current -> Math.max(current, critical.frontier.size()));
                                if (verbosityLevel >= 2)
                                    System.out.println("subProblem(ub:" + wl.subProblem.getUpperBound() + " val:" + wl.subProblem.getValue() + " depth:" + wl.subProblem.getPath().size() + " fastUpperBound:" + (wl.subProblem.getUpperBound() - wl.subProblem.getValue()) + "):" + wl.subProblem.getState());
                                processOneNode(wl.subProblem, mdd, verbosityLevel, exportAsDot);
                                notifyNodeFinished(threadId);
                                break;
                        }
                    }
                }
            };
            workers[i].start();
        }

        for (int i = 0; i < shared.nbThreads; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
            }
        }
        long end = System.currentTimeMillis();
        return new SearchStatistics(nbIter.get(), queueMaxSize.get(), end - start);
    }

    @Override
    public Optional<Double> bestValue() {
        synchronized (critical) {
            if (critical.bestSol.isPresent()) {
                return Optional.of(critical.bestLB);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        synchronized (critical) {
            return critical.bestSol;
        }
    }

    /**
     * @return the number of nodes that have been explored
     */
    public int explored() {
        synchronized (critical) {
            return critical.explored;
        }
    }

    /**
     * @return best known lower bound so far
     */
    public double lowerBound() {
        synchronized (critical) {
            return critical.bestLB;
        }
    }

    /**
     * @return best known upper bound so far
     */
    public double upperBound() {
        synchronized (critical) {
            return critical.bestUB;
        }
    }

    /**
     * @return the root subproblem
     */
    private SubProblem<T> root() {
        return new SubProblem<>(
                shared.problem.initialState(),
                shared.problem.initialValue(),
                Double.POSITIVE_INFINITY,
                Collections.emptySet());
    }

    /**
     * Utility method to initialize the solver structure
     */
    private void initialize() {
        synchronized (critical) {
            critical.frontier.push(root());
        }
    }

    /**
     * This method processes one node from the solver frontier.
     * <p>
     * This is typically the method you are searching for if you are searching after an implementation
     * of the branch and bound with mdd algo.
     */
    private void processOneNode(final SubProblem<T> sub, final DecisionDiagram<T, K> mdd, int verbosityLevel, boolean exportAsDot) {
        // 1. RESTRICTION
        double nodeUB = sub.getUpperBound();
        double bestLB = bestLB();

        if (nodeUB <= bestLB) {
            return;
        }

        int width = shared.width.maximumWidth(sub.getState());
        CompilationInput<T, K> compilation = new CompilationInput<>(
                CompilationType.Restricted,
                shared.problem,
                shared.relax,
                shared.varh,
                shared.ranking,
                sub,
                width,
                shared.fub,
                shared.dominance,
                bestLB,
                critical.frontier.cutSetType(),
                false
        );

        mdd.compile(compilation);
        maybeUpdateBest(mdd, verbosityLevel);
        if (mdd.isExact()) {
            return;
        }

        // 2. RELAXATION
        bestLB = bestLB();
        compilation = new CompilationInput<>(
                CompilationType.Relaxed,
                shared.problem,
                shared.relax,
                shared.varh,
                shared.ranking,
                sub,
                width,
                shared.fub,
                shared.dominance,
                bestLB,
                critical.frontier.cutSetType(),
                false
        );
        mdd.compile(compilation);
        if (mdd.isExact()) {
            maybeUpdateBest(mdd, verbosityLevel);
        } else {
            enqueueCutset(mdd);
        }
    }

    /**
     * @return the current best known lower bound
     */
    private double bestLB() {
        synchronized (critical) {
            return critical.bestLB;
        }
    }

    /**
     * This private method updates the shared best known node and lower bound in
     * case the best value of the current `mdd` expansion improves the current
     * bounds.
     */
    private void maybeUpdateBest(final DecisionDiagram<T, K> mdd, int verbosityLevel) {
        synchronized (critical) {
            Optional<Double> ddval = mdd.bestValue();

            if (ddval.isPresent() && ddval.get() > critical.bestLB) {
                critical.bestLB = ddval.get();
                critical.bestSol = mdd.bestSolution();
                if (verbosityLevel >= 1) System.out.println("new best: " + ddval.get());
            }
        }
    }

    /**
     * If necessary, tightens the bound of nodes in the cutset of `mdd` and
     * then add the relevant nodes to the shared fringe.
     */
    private void enqueueCutset(final DecisionDiagram<T, K> mdd) {
        synchronized (critical) {
            double bestLB = critical.bestLB;
            Iterator<SubProblem<T>> cutset = mdd.exactCutset();
            while (cutset.hasNext()) {
                SubProblem<T> cutsetNode = cutset.next();
                if (cutsetNode.getUpperBound() > bestLB) {
                    critical.frontier.push(cutsetNode);
                }
            }
        }
    }

    /**
     * Acknowledges that a thread finished processing its node.
     */
    private void notifyNodeFinished(final int threadId) {
        synchronized (critical) {
            critical.ongoing -= 1;
            critical.upperBounds[threadId] = Integer.MAX_VALUE;
            critical.notifyAll();
        }
    }

    /**
     * Consults the shared state to fetch a workload. Depending on the current
     * state, the workload can either be:
     * <p>
     * + Complete, when the problem is solved and all threads should stop
     * + Starvation, when there is no subproblem available for processing
     * at the time being (but some subproblem are still being processed
     * and thus the problem cannot be considered solved).
     * + WorkItem, when the thread successfully obtained a subproblem to
     * process.
     */
    private Workload<T> getWorkload(int threadId) {
        synchronized (critical) {
            // Are we done ?
            if (critical.ongoing == 0 && critical.frontier.isEmpty()) {
                critical.bestUB = critical.bestLB;
                return new Workload<>(WorkloadStatus.Complete, null);
            }
            // Nothing to do yet ? => Wait for someone to post jobs
            if (critical.frontier.isEmpty()) {
                try {
                    critical.wait();
                } catch (InterruptedException e) {
                }
                return new Workload<>(WorkloadStatus.Starvation, null);
            }
            // Nothing relevant ? =>  Wait for someone to post jobs
            SubProblem<T> nn = critical.frontier.pop();
            if (nn.getUpperBound() <= critical.bestLB) {
                critical.frontier.clear();
                if (critical.ongoing == 0) {
                    return new Workload<>(WorkloadStatus.Complete, null);
                } else {
                    try {
                        critical.wait();
                    } catch (InterruptedException e) {
                    }
                    return new Workload<>(WorkloadStatus.Starvation, null);
                }
            }

            // Consume the current node and process it
            critical.ongoing += 1;
            critical.explored += 1;
            critical.upperBounds[threadId] = nn.getUpperBound();

            return new Workload<>(WorkloadStatus.WorkItem, nn);
        }
    }

    /**
     * The status of when a workload is retrieved
     */
    private static enum WorkloadStatus {
        /**
         * When the complete state space has been explored
         */
        Complete,
        /**
         * When we are waiting for new nodes to appear on the solver frontier
         */
        Starvation,
        /**
         * When we are given a workitem which is ready to be processed
         */
        WorkItem,
    }

    /**
     * A work load that has been retrieved from the solver frontier
     */
    private static final class Workload<T> {
        /**
         * The status associated with this workload
         */
        final WorkloadStatus status;
        /**
         * The subproblem that was returned if one has been found
         */
        final SubProblem<T> subProblem;

        public Workload(final WorkloadStatus status, final SubProblem<T> subProblem) {
            this.status = status;
            this.subProblem = subProblem;
        }
    }

    /**
     * The various threads of the solver share a common zone of memory. That
     * zone of shared memory is split in two:
     * <p>
     * - what is publicly and concurrently accessible
     * - what is synchronized and can only be accessed within critical sections
     */
    private static class Shared<T, K> {
        /**
         * The number of threads that must be spawned to solve the problem
         */
        private final int nbThreads;
        /**
         * The problem we want to maximize
         */
        private final Problem<T> problem;
        /**
         * A suitable relaxation for the problem we want to maximize
         */
        private final Relaxation<T> relax;
        /**
         * A heuristic to identify the most promising nodes
         */
        private final StateRanking<T> ranking;
        /**
         * A heuristic to choose the maximum width of the DD you compile
         */
        private final WidthHeuristic<T> width;

        /**
         * The heuristic defining a very rough estimation (upper bound) of the optimal value.
         */
        private final FastUpperBound<T> fub;

        private final DominanceChecker<T, K> dominance;
        /**
         * A heuristic to choose the next variable to branch on when developing a DD
         */
        private final VariableHeuristic<T> varh;

        public Shared(
                final int nbThreads,
                final Problem<T> problem,
                final Relaxation<T> relax,
                final VariableHeuristic<T> varh,
                final StateRanking<T> ranking,
                final WidthHeuristic<T> width,
                FastUpperBound<T> fub,
                final DominanceChecker<T, K> dominance) {
            this.nbThreads = nbThreads;
            this.problem = problem;
            this.relax = relax;
            this.fub = fub;
            this.varh = varh;
            this.ranking = ranking;
            this.width = width;
            this.dominance = dominance;
        }
    }

    /**
     * The shared data that may only be manipulated within critical sections
     */
    private static final class Critical<T> {
        /**
         * This is the fringe: the set of nodes that must still be explored before
         * the problem can be considered 'solved'.
         * <p>
         * # Note:
         * This fringe orders the nodes by upper bound (so the highest ub is going
         * to pop first). So, it is guaranteed that the upper bound of the first
         * node being popped is an upper bound on the value reachable by exploring
         * any of the nodes remaining on the fringe. As a consequence, the
         * exploration can be stopped as soon as a node with an ub &#8804; current best
         * lower bound is popped.
         */
        private final Frontier<T> frontier;
        /**
         * This vector is used to store the upper bound on the node which is
         * currently processed by each thread.
         * <p>
         * # Note
         * When a thread is idle (or more generally when it is done with processing
         * it node), it should place the value i32::min_value() in its corresponding
         * cell.
         */
        final double[] upperBounds;
        /**
         * This is the number of nodes that are currently being explored.
         * <p>
         * # Note
         * This information may seem innocuous/superfluous, whereas in fact it is
         * very important. Indeed, this is the piece of information that lets us
         * distinguish between a node-starvation and the completion of the problem
         * resolution. The bottom line is, this counter needs to be carefully
         * managed to guarantee the termination of all threads.
         */
        int ongoing;
        /**
         * This is a counter that tracks the number of nodes that have effectively
         * been explored. That is, the number of nodes that have been popped from
         * the fringe, and for which a restricted and relaxed mdd have been developed.
         */
        int explored;
        /**
         * This is the value of the best known lower bound.
         */
        double bestLB;
        /**
         * This is the value of the best known lower bound.
         * *WARNING* This one only gets set when the interrupt condition is satisfied
         */
        double bestUB;
        /**
         * If set, this keeps the info about the best solution so far.
         */
        Optional<Set<Decision>> bestSol;

        public Critical(final int nbThreads, final Frontier<T> frontier) {
            this.frontier = frontier;
            this.ongoing = 0;
            this.explored = 0;
            this.bestLB = Double.NEGATIVE_INFINITY;
            this.bestUB = Double.POSITIVE_INFINITY;
            this.upperBounds = new double[nbThreads];
            this.bestSol = Optional.empty();
            for (int i = 0; i < nbThreads; i++) {
                upperBounds[i] = Double.POSITIVE_INFINITY;
            }
        }
    }
}
