package implem.solver;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.Dominance;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultFastUpperBound;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class SequentialSolverTest {
    public static void main(String[] args) {

        final BKP problem = new BKP(15, new int[]{2, 3, 6, 6, 1}, new int[]{4, 6, 4, 2, 5}, new int[]{1, 1, 2, 2, 1});
        final BKPRelax relax = new BKPRelax(problem);
        final BKPRanking ranking = new BKPRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(3);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final DefaultFastUpperBound<Integer> fub = new DefaultFastUpperBound<>();
        final SimpleDominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new BKPDominance(),
                problem.nbVars());
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.Frontier);

        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub,
                dominance);


        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

    public static class BKP implements Problem<Integer> {
        final int capacity;
        final int[] values;
        final int[] weights;
        final int[] quantity;

        public BKP(int capacity, int[] values, int[] weights, int[] quantity) {
            this.capacity = capacity;
            this.values = values;
            this.weights = weights;
            this.quantity = quantity;
        }

        @Override
        public int nbVars() {
            return values.length;
        }

        @Override
        public Integer initialState() {
            return capacity;
        }

        @Override
        public double initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(Integer state, int var) {
            ArrayList<Integer> domain = new ArrayList<>();
            domain.add(0);
            for (int v = 1; v <= quantity[var]; v++) {
                if (state >= v * weights[var]) {
                    domain.add(v);
                }
            }
            return domain.iterator();
        }

        @Override
        public Integer transition(Integer state, Decision decision) {
            // If the item is taken (1), we decrease the capacity of the knapsack, otherwise leave it unchanged
            return state - weights[decision.var()] * decision.val();
        }

        @Override
        public double transitionCost(Integer state, Decision decision) {
            // If the item is taken (1) the cost is the profit of the item, 0 otherwise
            return values[decision.var()] * decision.val();
        }
    }

    public static class BKPRelax implements Relaxation<Integer> {
        private final BKP problem;

        public BKPRelax(BKP problem) {
            this.problem = problem;
        }

        @Override
        public Integer mergeStates(final Iterator<Integer> states) {
            int capa = 0;
            while (states.hasNext()) {
                final Integer state = states.next();
                capa = Math.max(capa, state);
            }
            return capa;
        }

        @Override
        public double relaxEdge(Integer from, Integer to, Integer merged, Decision d, double cost) {
            return cost;
        }
    }

    public static class BKPRanking implements StateRanking<Integer> {
        @Override
        public int compare(final Integer o1, final Integer o2) {
            return o1 - o2;
        }
    }

    public static class BKPDominance implements Dominance<Integer, Integer> {
        @Override
        public Integer getKey(Integer capa) {
            return 0;
        }

        @Override
        public boolean isDominatedOrEqual(Integer capa1, Integer capa2) {
            return capa1 < capa2;
        }
    }

}
