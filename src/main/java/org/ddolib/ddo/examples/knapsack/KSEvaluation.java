package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.cache.SimpleCache;
import org.ddolib.ddo.implem.dominance.DominanceChecker;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolverWithCache;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import static org.ddolib.ddo.examples.knapsack.KSMain.readInstance;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolverWithCache;

public class KSEvaluation {
    public static class KSWithDominance {
        String filename;
        int w;
        CutSetType cutsetType;
        double elapsedTime;
        double optimalSolution;
        int[] solution;
        int numberIterations;
        int queueMaxSize;
        public KSWithDominance(String filename, int w, CutSetType cutsetType) throws Exception{
            this.filename = filename;
            this.w = w;
            this.cutsetType = cutsetType;
            final KSProblem problem = readInstance(filename);
            final KSRelax relax = new KSRelax(problem);
            final KSRanking ranking = new KSRanking();
            final FixedWidth<Integer> width = new FixedWidth<>(w);
            final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
            final SimpleDominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, cutsetType);

            final Solver solver = sequentialSolver(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier,
                    dominance,
                    10
            );


            long start = System.currentTimeMillis();
            SearchStatistics stats = solver.maximize(0, false);
            elapsedTime = stats.runTimeMS() / 1000.0;
            optimalSolution = solver.bestValue().get();
            queueMaxSize = stats.queueMaxSize();
            numberIterations = stats.nbIterations();
            solution = solver.bestSolution().map(decisions -> {
                int[] values = new int[problem.nbVars()];
                for (Decision d : decisions) {
                    values[d.var()] = d.val();
                }
                return values;
            }).get();

            System.out.println(filename + " | " + w + " | " + cutsetType + " | " + optimalSolution + " | " + elapsedTime + " | " + numberIterations + " | " + queueMaxSize + " | " + "dominance only");//Arrays.toString(solution));
        }
    }


    public static class KSWithCache {
        String filename;
        int w;
        CutSetType cutsetType;
        double elapsedTime;
        double optimalSolution;
        int[] solution;
        int numberIterations;
        int queueMaxSize;
        public KSWithCache(String filename, int w, CutSetType cutsetType) throws Exception{
            this.filename = filename;
            this.w = w;
            this.cutsetType = cutsetType;
            final KSProblem problem = readInstance(filename);
            final KSRelax relax = new KSRelax(problem);
            final KSRanking ranking = new KSRanking();
            final FixedWidth<Integer> width = new FixedWidth<>(w);
            final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
            final DominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            final SimpleCache<Integer> cache = new SimpleCache();
            final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, cutsetType);

            final Solver solver = sequentialSolverWithCache(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier,
                    dominance,
                    cache,
                    10
            );


            long start = System.currentTimeMillis();
            SearchStatistics stats = solver.maximize(0, false);
            elapsedTime = stats.runTimeMS() / 1000.0;
            optimalSolution = solver.bestValue().get();
            queueMaxSize = stats.queueMaxSize();
            numberIterations = stats.nbIterations();
            solution = solver.bestSolution().map(decisions -> {
                int[] values = new int[problem.nbVars()];
                for (Decision d : decisions) {
                    values[d.var()] = d.val();
                }
                return values;
            }).get();

            System.out.println(filename + " | " + w + " | " + cutsetType + " | " + optimalSolution + " | " + elapsedTime + " | " + numberIterations + " | " + queueMaxSize + " | " + "dominance + cache"); //Arrays.toString(solution));
        }
    }



    public static void main(String[] args) throws Exception {
        File folder = new File("data/Knapsack");
        File[] listOfFiles = folder.listFiles();
        int[] Width = new int[]{100,250};
        CutSetType[] CSType = new CutSetType[]{CutSetType.LastExactLayer, CutSetType.Frontier};
        for (int w : Width) {
            for (CutSetType cs : CSType) {
                String name = "KS_"+cs+"_"+w+".txt";
                File fileResults = new File("data/Knapsack/Results/"+ name);
                if (fileResults.createNewFile()) {
//                    FileWriter writer = new FileWriter(fileResults);
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.toString().contains("instance")) {
                            String filename = file.toString();
                            KSWithDominance ksDom = new KSWithDominance(filename, w, cs);
//                            writer.write(filename + " | " + w + " | " + ksDom.cutsetType + " | " + ksDom.optimalSolution + " | " + ksDom.elapsedTime + " | " + ksDom.numberIterations + " | " + ksDom.queueMaxSize + " | " + "dominance + only"+ "\n");
                            KSWithCache ksCache = new KSWithCache(filename, w, cs);
//                            writer.write(filename + " | " + w + " | " + ksCache.cutsetType + " | " + ksCache.optimalSolution + " | " + ksCache.elapsedTime + " | " + ksCache.numberIterations + " | " + ksCache.queueMaxSize + " | " + "dominance + cache"+ "\n");
                        }
                    }
//                    writer.close();
                }
            }
        }
    }
}
