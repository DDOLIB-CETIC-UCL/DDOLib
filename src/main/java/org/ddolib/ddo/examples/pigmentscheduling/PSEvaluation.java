package org.ddolib.ddo.examples.pigmentscheduling;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.examples.knapsack.KSEvaluation;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.cache.SimpleCache;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.dominance.DominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolverWithCache;

import java.io.File;
import java.io.FileWriter;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

public class PSEvaluation {
    public static class PSWithOutDominance {
        String filename;
        int w;
        CutSetType cutsetType;
        double elapsedTime;
        double optimalSolution;
        int[] solution;
        int numberIterations;
        int queueMaxSize;
        public PSWithOutDominance(String filename, int w, CutSetType cutsetType) throws Exception{
            this.filename = filename;
            this.w = w;
            this.cutsetType = cutsetType;
            PSInstance instance = new PSInstance(filename);
            PSProblem problem = new PSProblem(instance);
            final PSRelax relax = new PSRelax(instance);
            final PSRanking ranking = new PSRanking();
            final FixedWidth<PSState> width = new FixedWidth<>(w);
            final VariableHeuristic<PSState> varh = new DefaultVariableHeuristic<>();
            final Frontier<PSState> frontier = new SimpleFrontier<>(ranking, cutsetType);
            final Solver solver = sequentialSolver(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier);
            SearchStatistics stats = solver.maximize();
            elapsedTime = stats.runTimeMS() / 1000.0;
            optimalSolution = solver.bestValue().get();
            queueMaxSize = stats.queueMaxSize();
            numberIterations = stats.nbIterations();

            solution = solver.bestSolution()
                    .map(decisions -> {
                        int[] values = new int[problem.nbVars()];
                        for (Decision d : decisions) {
                            int t = (instance.horizon - d.var() - 1);
                            values[t] = d.val();
                        }
                        return values;
                    })
                    .get();
            System.out.println(filename + " | " + w + " | " + cutsetType + " | " + optimalSolution + " | " + elapsedTime + " | " + numberIterations + " | " + queueMaxSize + " | " + "dominance + cache");
        }
    }


    public static class PSWithCache {
        String filename;
        int w;
        CutSetType cutsetType;
        double elapsedTime;
        double optimalSolution;
        int[] solution;
        int numberIterations;
        int queueMaxSize;
        public PSWithCache(String filename, int w, CutSetType cutsetType) throws Exception{
            this.filename = filename;
            this.w = w;
            this.cutsetType = cutsetType;
            PSInstance instance = new PSInstance(filename);
            PSProblem problem = new PSProblem(instance);
            final PSRelax relax = new PSRelax(instance);
            final PSRanking ranking = new PSRanking();
            final FixedWidth<PSState> width = new FixedWidth<>(w);
            final VariableHeuristic<PSState> varh = new DefaultVariableHeuristic<>();
            final Frontier<PSState> frontier = new SimpleFrontier<>(ranking, cutsetType);
            final DominanceChecker dominance = new DefaultDominanceChecker();
            final SimpleCache<PSState> cache = new SimpleCache<>();
            final Solver solver = new SequentialSolverWithCache(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier,
                    dominance,
                    cache,
                    Long.MAX_VALUE);
            SearchStatistics stats = solver.maximize();
            elapsedTime = stats.runTimeMS() / 1000.0;
            optimalSolution = solver.bestValue().get();
            queueMaxSize = stats.queueMaxSize();
            numberIterations = stats.nbIterations();

            solution = solver.bestSolution()
                    .map(decisions -> {
                        int[] values = new int[problem.nbVars()];
                        for (Decision d : decisions) {
                            int t = (instance.horizon - d.var() - 1);
                            values[t] = d.val();
                        }
                        return values;
                    })
                    .get();
            System.out.println(filename + " | " + w + " | " + cutsetType + " | " + optimalSolution + " | " + elapsedTime + " | " + numberIterations + " | " + queueMaxSize + " | " + "dominance + cache");
        }
    }


    public static void main(String[] args) throws Exception {
        File folder = new File("data/PSP/instancesWith2items");
        File[] listOfFiles = folder.listFiles();
        int[] Width = new int[]{100,250};
        CutSetType[] CSType = new CutSetType[]{CutSetType.LastExactLayer, CutSetType.Frontier};
        for (int w : Width) {
            for (CutSetType cs : CSType) {
                String name = "PS_"+cs+"_"+w+".txt";
                File fileResults = new File("data/PSP/Results/"+ name);
                if (fileResults.createNewFile()) {
                    FileWriter writer = new FileWriter(fileResults);
                    for (File file : listOfFiles) {
                        if (file.isFile()/* && file.toString().contains("pigment")*/) {
                            String filename = file.toString();
                            PSWithOutDominance psDom = new PSWithOutDominance(filename, w, cs);
                            writer.write(filename + " | " + w + " | " + psDom.cutsetType + " | " + psDom.optimalSolution + " | " + psDom.elapsedTime + " | " + psDom.numberIterations + " | " + psDom.queueMaxSize + " | " + "no dominance"+ "\n");
                            PSWithCache psCache = new PSWithCache(filename, w, cs);
                            writer.write(filename + " | " + w + " | " + psCache.cutsetType + " | " + psCache.optimalSolution + " | " + psCache.elapsedTime + " | " + psCache.numberIterations + " | " + psCache.queueMaxSize + " | " + "no dominance + cache"+ "\n");
                        }
                    }
                }
            }
        }
    }
}
