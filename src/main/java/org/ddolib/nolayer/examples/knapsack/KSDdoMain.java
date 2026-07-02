package org.ddolib.nolayer.examples.knapsack;

import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.common.solver.Solution;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.Relaxation;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.nolayer.solving.ddo.core.solver.DdoSolver;
import org.ddolib.util.io.SolutionPrinter;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;

public class KSDdoMain {

    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "Knapsack",
                "instance_n1000_c1000_10_5_10_5_0").toString() : args[0];
        final KSProblem problem = KSProblem.fromFile(instance);

        DdoModel<KSState> model = new KSDdoModel(problem) {
            @Override
            public Relaxation<KSState> relaxation() {
                return new Relaxation<KSState>() {
                    @Override
                    public KSState merge(Collection<KSState> states) {
                        Iterator<KSState> it = states.iterator();
                        KSState first = it.next();
                        int maxCapacity = first.remainingCapacity();
                        int currentItem = first.currentItem();

                        while (it.hasNext()) {
                            KSState s = it.next();
                            maxCapacity = Math.max(maxCapacity, s.remainingCapacity()); // Relax by taking max capacity
                            currentItem = Math.min(currentItem, s.currentItem()); // Relax by taking min currentItem to
                            // overapproximate
                        }
                        return new KSState(currentItem, maxCapacity);
                    }
                };
            }

            @Override
            public StateRanking<KSState> ranking() {
                // Rank by largest remaining capacity first
                return (s1, s2) -> Integer.compare(s2.remainingCapacity(), s1.remainingCapacity());
            }

            @Override
            public WidthHeuristic<KSState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

            @Override
            public NoLayerDominanceChecker<KSState> dominance() {
                return new NoLayerDominanceChecker<KSState>() {
                    private final java.util.Map<Integer, java.util.TreeSet<ValueState>> bestStates = new java.util.HashMap<>();

                    @Override
                    public boolean updateDominance(KSState state, double value) {
                        java.util.TreeSet<ValueState> set = bestStates
                                .computeIfAbsent(state.currentItem(), k -> new java.util.TreeSet<>());
                        ValueState vs = new ValueState(state, value);
                        ValueState floor = set.floor(vs);
                        if (floor != null && floor.state.remainingCapacity() >= state.remainingCapacity() && floor.value <= value) {
                            return true; // Dominated!
                        }

                        java.util.Iterator<ValueState> iterator = set.tailSet(vs).iterator();
                        while (iterator.hasNext()) {
                            ValueState higher = iterator.next();
                            if (state.remainingCapacity() >= higher.state.remainingCapacity() && value <= higher.value) {
                                iterator.remove();
                            } else {
                                break;
                            }
                        }
                        set.add(vs);
                        return false;
                    }

                    @Override
                    public void clear() {
                        bestStates.clear();
                    }

                    class ValueState implements Comparable<ValueState> {
                        final KSState state;
                        final double value;

                        ValueState(KSState state, double value) {
                            this.state = state;
                            this.value = value;
                        }

                        @Override
                        public int compareTo(ValueState o) {
                            if (this.value != o.value) return Double.compare(this.value, o.value);
                            return Integer.compare(this.state.remainingCapacity(), o.state.remainingCapacity());
                        }
                    }
                };
            }

            @Override
            public ReductionStrategy<KSState> relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy<KSState> restrictStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.NORMAL;
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        DdoSolver<KSState> solver = new DdoSolver<>(model);
        Solution bestSolution = solver.minimize(
                limit -> limit.nbIterations() > 500, // ADD LIMIT SO IT STOPS
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                    try {
                        double val = problem.evaluate(sol);
                        System.out.println("ON SOLUTION EVALUATE: " + val);
                    } catch (Exception e) {
                        System.out.println("EVALUATE ERROR: " + e.getMessage());
                    }
                });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal KS value: " + -bestSolution.value());
        try {
            double val = problem.evaluate(bestSolution.solution());
            System.out.println("Evaluated value: " + val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

abstract class KSDdoModel extends KSModel implements DdoModel<KSState> {
    public KSDdoModel(KSProblem problem) {
        super(problem);
    }
}
