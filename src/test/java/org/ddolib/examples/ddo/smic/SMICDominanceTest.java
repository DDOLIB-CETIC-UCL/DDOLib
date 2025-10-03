package org.ddolib.examples.ddo.smic;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.*;
import java.util.stream.Stream;

/*public class SMICDominanceTest {

    private static class SMICDominanceBench extends ProblemTestBench<SMICState, Integer, SMICProblem> {
        public SMICDominanceBench() {super();}

        @Override
        protected List<SMICProblem> generateProblems() {
            List<SMICProblem> problems = new ArrayList<>();
            int n = 10;
            int[] type = new int[n];
            int[] processing = new int[n];
            int[] width = new int[n];
            int[] release = new int[n];
            int[] inventory = new int[n];
            int initialInventory = 1;
            int maximumInventory = 1;
            long seed = 10;
            Random random = new Random(seed);

            double sumProc = 0.0;
            for (int i = 0; i < n; i++) {
                type[i] = (i % 2 == 0) ? 1 : 0;
                processing[i] = 3 + random.nextInt(n);
                sumProc -= processing[i];
                width[i] = 0;
                if (i % 2 == 0)
                    release[i] = random.nextInt(n);
                else release[i] = release[i-1];
                inventory[i] = 1;
            }
            int num = problems.size();
            Optional<Double> opti = Optional.of(sumProc);
            /*int n = 8;
            int[] type = new int[]{1,1,0,1,1,0,1,0};
            int[] processing = new int[]{5,5,10,5,5,5,5,5};
            int[] width = new int[]{0,0,0,0,0,0,0,0};
//            int[] release = new int[]{0,5,0,10,15,20,25,30};
            int[] release = new int[]{0,0,0,0,0,0,0,0};
            int[] inventory = new int[]{1,1,2,1,1,2,2,2};
            int initialInventory = 0;
            int maximumInventory = 2;
            Optional<Double> opti = Optional.of(-45.0);*/

            /*SMICProblem pb = new SMICProblem("PB_0", n, initialInventory, maximumInventory, type, processing, width, release, inventory, opti.get());
            problems.add(pb);
            int k = 0;
            while (k < 10) {
                SMICProblem pbs = shuffle(pb);
                problems.add(pbs);
                k++;
            }
            return problems;
        }

        private SMICProblem shuffle(SMICProblem pb) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < pb.nbJob; i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);

            int[] shuffleType = new int[pb.nbJob];
            int[] shuffleProcessing = new int[pb.nbJob];
            int[] shuffleRelease = new int[pb.nbJob];
            int[] shuffleInventory = new int[pb.nbJob];
            for (int i = 0; i < pb.nbJob; i++) {
                shuffleType[i] = pb.type[indices.get(i)];
                shuffleProcessing[i] = pb.processing[indices.get(i)];
                shuffleRelease[i] = pb.release[indices.get(i)];
                shuffleInventory[i] = pb.inventory[indices.get(i)];
            }
            return new SMICProblem("PBs", pb.nbJob, pb.initInventory, pb.capaInventory, shuffleType, shuffleProcessing, pb. weight, shuffleRelease, shuffleInventory, pb.optimalValue().get());
        }

        @Override
        protected SolverConfig<SMICState, Integer> configSolver(SMICProblem problem) {
            SolverConfig<SMICState, Integer> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new SMICRelax(problem);
            config.ranking = new SMICRanking();
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.dominance = new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

            return config;
        }

        @Override
        protected Solver solverForTests(SolverConfig<SMICState, Integer> config) {
            config.width = new FixedWidth<>(100);
            return new SequentialSolver<>(config);
        }
    }
    @DisplayName("SMIC")
    @TestFactory
    public Stream<DynamicTest> testDominanceSMIC() {
        var bench = new SMICDominanceBench();
        bench.testRelaxation = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}*/
