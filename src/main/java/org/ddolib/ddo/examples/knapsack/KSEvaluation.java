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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.ddolib.ddo.examples.knapsack.KSMain.readInstance;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolverWithCache;

public class KSEvaluation {

    public static class KSNoDominance {
        String filename;
        int w;
        CutSetType cutsetType;
        double elapsedTime;
        double optimalSolution;
        int[] solution;
        int numberIterations;
        int queueMaxSize;
        SearchStatistics.SearchStatus searchStatus;
        public KSNoDominance(String filename, int w, CutSetType cutsetType) throws Exception{
            this.filename = filename;
            this.w = w;
            this.cutsetType = cutsetType;
            final KSProblem problem = readInstance(filename);
            final KSRelax relax = new KSRelax(problem);
            final KSRanking ranking = new KSRanking();
            final FixedWidth<Integer> width = new FixedWidth<>(w);
            final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
            final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, cutsetType);

            final Solver solver = sequentialSolver(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier
            );


            long start = System.currentTimeMillis();
            SearchStatistics stats = solver.maximize(0, false);
            elapsedTime = stats.runTimeMS() / 1000.0;
            optimalSolution = solver.bestValue().get();
            queueMaxSize = stats.queueMaxSize();
            numberIterations = stats.nbIterations();
            searchStatus = stats.SearchStatus();
            solution = solver.bestSolution().map(decisions -> {
                int[] values = new int[problem.nbVars()];
                for (Decision d : decisions) {
                    values[d.var()] = d.val();
                }
                return values;
            }).get();

            System.out.println(filename + " | " + w + " | " + cutsetType + " | " + optimalSolution + " | " + elapsedTime + " | " + searchStatus + " | " + numberIterations + " | " + queueMaxSize + " | " + "no dominance");//Arrays.toString(solution));
        }
    }

    public static class KSWithDominance {
        String filename;
        int w;
        CutSetType cutsetType;
        double elapsedTime;
        double optimalSolution;
        int[] solution;
        int numberIterations;
        int queueMaxSize;
        SearchStatistics.SearchStatus searchStatus;
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
                    dominance
            );


            long start = System.currentTimeMillis();
            SearchStatistics stats = solver.maximize(0, false);
            elapsedTime = stats.runTimeMS() / 1000.0;
            optimalSolution = solver.bestValue().get();
            queueMaxSize = stats.queueMaxSize();
            numberIterations = stats.nbIterations();
            searchStatus = stats.SearchStatus();
            solution = solver.bestSolution().map(decisions -> {
                int[] values = new int[problem.nbVars()];
                for (Decision d : decisions) {
                    values[d.var()] = d.val();
                }
                return values;
            }).get();

            System.out.println(filename + " | " + w + " | " + cutsetType + " | " + optimalSolution + " | " + elapsedTime + " | " + searchStatus + " | " + numberIterations + " | " + queueMaxSize + " | " + "with dominance");//Arrays.toString(solution));
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
        SearchStatistics.SearchStatus searchStatus;
        public KSWithCache(String filename, int w, CutSetType cutsetType) throws Exception{
            this.filename = filename;
            this.w = w;
            this.cutsetType = cutsetType;
            final KSProblem problem = readInstance(filename);
            final KSRelax relax = new KSRelax(problem);
            final KSRanking ranking = new KSRanking();
            final FixedWidth<Integer> width = new FixedWidth<>(w);
            final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
            final SimpleCache<Integer> cache = new SimpleCache();
            final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, cutsetType);

            final Solver solver = sequentialSolverWithCache(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier,
                    cache
            );


            long start = System.currentTimeMillis();
            SearchStatistics stats = solver.maximize(0, false);
            elapsedTime = stats.runTimeMS() / 1000.0;
            optimalSolution = solver.bestValue().get();
            queueMaxSize = stats.queueMaxSize();
            numberIterations = stats.nbIterations();
            searchStatus = stats.SearchStatus();
            solution = solver.bestSolution().map(decisions -> {
                int[] values = new int[problem.nbVars()];
                for (Decision d : decisions) {
                    values[d.var()] = d.val();
                }
                return values;
            }).get();

            System.out.println(filename + " | " + w + " | " + cutsetType + " | " + optimalSolution + " | " + elapsedTime + " | " + searchStatus + " | " + numberIterations + " | " + queueMaxSize + " | " + "with cache"); //Arrays.toString(solution));
        }
    }

    public static class KSWithDominanceCache {
        String filename;
        int w;
        CutSetType cutsetType;
        double elapsedTime;
        double optimalSolution;
        int[] solution;
        int numberIterations;
        int queueMaxSize;
        SearchStatistics.SearchStatus searchStatus;
        public KSWithDominanceCache(String filename, int w, CutSetType cutsetType) throws Exception{
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
                    cache
            );


            long start = System.currentTimeMillis();
            SearchStatistics stats = solver.maximize(0, false);
            elapsedTime = stats.runTimeMS() / 1000.0;
            optimalSolution = solver.bestValue().get();
            queueMaxSize = stats.queueMaxSize();
            numberIterations = stats.nbIterations();
            searchStatus = stats.SearchStatus();
            solution = solver.bestSolution().map(decisions -> {
                int[] values = new int[problem.nbVars()];
                for (Decision d : decisions) {
                    values[d.var()] = d.val();
                }
                return values;
            }).get();

            System.out.println(filename + " | " + w + " | " + cutsetType + " | " + optimalSolution + " | " + elapsedTime + " | " + searchStatus + " | " + numberIterations + " | " + queueMaxSize + " | " + "with dominance + cache"); //Arrays.toString(solution));
        }
    }



    public static void main(String[] args) throws Exception {
        File folder = new File("data/Knapsack");
        File[] listOfFiles = folder.listFiles();
        int[] Width = new int[]{100};
        CutSetType[] CSType = new CutSetType[]{/*CutSetType.LastExactLayer, */CutSetType.Frontier};
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (int w : Width) {
            for (CutSetType cs : CSType) {
                String name = "KS_"+cs+"_"+w+ now.format(formatter);;
                File fileResults = new File("data/Knapsack/Results/"+ name);
                if (fileResults.createNewFile()) {
                    FileWriter writer = new FileWriter(fileResults);
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.toString().contains("instance")) {
                            String filename = file.toString();
                            KSNoDominance ks = new KSNoDominance(filename, w, cs);
                            writer.write(filename + " | " + w + " | " + ks.cutsetType + " | " + ks.optimalSolution + " | " + ks.elapsedTime  + " | " + ks.searchStatus + " | " + ks.numberIterations + " | " + ks.queueMaxSize + " | " + "no dominance"+ "\n");
                        }
                    }
                    writer.close();
                    name = "KS_"+cs+"_"+ w + now.format(formatter);;
                    fileResults = new File("data/Knapsack/Results/"+ name);
                    writer = new FileWriter(fileResults);
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.toString().contains("instance")) {
                            String filename = file.toString();
                            KSWithDominance ksDom = new KSWithDominance(filename, w, cs);
                            writer.write(filename + " | " + w + " | " + ksDom.cutsetType + " | " + ksDom.optimalSolution + " | " + ksDom.elapsedTime  + " | " + ksDom.searchStatus+ " | " + ksDom.numberIterations + " | " + ksDom.queueMaxSize + " | " + "with dominance"+ "\n");
                        }
                    }
                    writer.close();
                    name = "KS_"+cs+"_"+w+ now.format(formatter);;
                    fileResults = new File("data/Knapsack/Results/"+ name);
                    writer = new FileWriter(fileResults);
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.toString().contains("instance")) {
                            String filename = file.toString();
                            KSWithCache ksCache = new KSWithCache(filename, w, cs);
                            writer.write(filename + " | " + w + " | " + ksCache.cutsetType + " | " + ksCache.optimalSolution + " | " + ksCache.elapsedTime  + " | " + ksCache.searchStatus + " | " + ksCache.numberIterations + " | " + ksCache.queueMaxSize + " | " + "with + cache"+ "\n");
                        }
                    }
                    writer.close();
                    name = "KS_"+cs+"_"+w+now.format(formatter);;
                    fileResults = new File("data/Knapsack/Results/"+ name);
                    writer = new FileWriter(fileResults);
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.toString().contains("instance")) {
                            String filename = file.toString();
                            KSWithDominanceCache ksDomCache = new KSWithDominanceCache(filename, w, cs);
                            writer.write(filename + " | " + w + " | " + ksDomCache.cutsetType + " | " + ksDomCache.optimalSolution + " | " + ksDomCache.elapsedTime  + " | " + ksDomCache.searchStatus  + " | " + ksDomCache.optimalSolution + " | " + ksDomCache.numberIterations + " | " + ksDomCache.queueMaxSize + " | " + "withdominance + cache"+ "\n");
                        }
                    }
                    writer.close();
                }
            }
        }
    }
}
