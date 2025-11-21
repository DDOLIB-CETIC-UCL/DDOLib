package org.ddolib.examples.binpacking;

import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;

import javax.lang.model.type.NullType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BPP {
    public static BPPProblem extractFile(String fileName) throws IOException {
        final File f = new File(fileName);


        int nbItems = 0;
        int binMaxSize = 0;
        Integer[] itemWeights = new Integer[1];
        int lineCounter = 0;
        Optional<Integer> optimal = Optional.empty();
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = bf.readLine()) != null) {
                if (lineCounter == 0) {
                    String[] splitLine = line.split("\\s+");
                    if(splitLine.length > 1) {
                        optimal = Optional.of(Integer.parseInt(splitLine[1]));
                    }
                    nbItems = Integer.parseInt(splitLine[0]);
                    itemWeights = new Integer[nbItems];
                } else if (lineCounter == 1) {
                    binMaxSize = Integer.parseInt(line);
                } else {
                    itemWeights[lineCounter - 2] = Integer.parseInt(line);
                }
                lineCounter++;
            }
        }

        Arrays.sort(itemWeights, Comparator.reverseOrder());

        return new BPPProblem(nbItems, binMaxSize, Arrays.stream(itemWeights).mapToInt(i -> i).toArray(), optimal);
    }

    public static void main(String[] args) throws IOException {
        final String file = args.length == 0 ? "data/BPP/test.txt" : args[0];
        final int maxWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 250;

        SolverConfig<BPPState, NullType> config = new SolverConfig<>();
        BPPProblem problem = extractFile(file);
        config.relax = new BPPRelax(problem);
        config.flb = new BPPFastUpperBound(problem);
        config.ranking = new BPPRanking();
        config.width = new FixedWidth<>(maxWidth);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, org.ddolib.ddo.core.frontier.CutSetType.LastExactLayer);
        config.verbosityLevel = 1;

        ParallelSolver<BPPState,NullType> solver = new ParallelSolver<>(config);
        solver.

        long start = System.currentTimeMillis();
        solver.maximize(1,true);
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        Decision[] solution = solver.bestSolution()
                .map(decisions -> {
                    Decision[] values = new Decision[problem.nbVars()];
                    for (Decision d : decisions) {
                        values[d.var()] = d;
                    }
                    return values;
                })
                .orElse(new Decision[0]);




        System.out.printf("Instance : %s%n", file);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %3f%n", solver.bestValue().orElse(Double.MIN_VALUE));
        System.out.printf("Upper Bnd : %s%n", solver.upperBound());
        System.out.printf("Lower Bnd : %s%n", solver.lowerBound());
        System.out.printf("Explored : %s%n", solver.explored());
        System.out.printf("Max width : %d%n", maxWidth);
        System.out.println("Solution : \n############\n");

        int d = 0;

        for(int bin = 0; bin < -solver.bestValue().orElse(0.0); bin++) {
            int remSpace = problem.binMaxSpace;
            boolean newBin = false;
            StringBuffer sb = new StringBuffer();
            sb.append(String.format("Bin number %d :\n", bin));
            while(!newBin && d < problem.nbVars()) {
                int item = solution[d].val();
                int weight = problem.itemWeight[item];
                if(weight < remSpace) {
                    sb.append(String.format("\tItem %d of weight %d\n",item,weight));
                    remSpace -= weight;
                    d++;
                } else {
                    newBin = true;
                }
            }
            sb.append(String.format("\tTotal weight %d - Free weight %d\n",problem.binMaxSpace-remSpace,remSpace));
            System.out.println(sb);
        }
    }

}
