package org.ddolib.examples.salbp;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SALBPMain {


    public static void main(String[] args) throws IOException, InvalidSolutionException {

        SALBProblem problem = readInstance("data/SALBP/medium/instance_n=50_10.alb");

        SALBPAcsModel model = new SALBPAcsModel(problem);

        Solution bestSolution = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });
        System.out.println(bestSolution);
        if (problem.optimal.isPresent()) {
            System.out.printf("Found : %f \t Optimal : %f\n", bestSolution.value(), problem.optimal.get());
        } else {
            System.out.printf("Found : %f\n", bestSolution.value());
        }
        try {
            problem.evaluate(bestSolution.solution());
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static SALBProblem readInstance(String fileName) throws IOException {
        final File f = new File(fileName);
        int nbItems = 0;
        int cycleTime = 0;
        Integer[] taskTime = new Integer[1];
        BitSet[] allPrecedences = new BitSet[1];
        int lineCounter = 0;
        Optional<Double> optimal = Optional.empty();
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = bf.readLine()) != null) {
                if (lineCounter == 0) {
                    String[] splitLine = line.split("\\s+");
                    if (splitLine.length > 1) {
                        optimal = Optional.of(Double.parseDouble(splitLine[1]));
                    }
                    nbItems = Integer.parseInt(splitLine[0]);
                    taskTime = new Integer[nbItems];
                    allPrecedences = new BitSet[nbItems];
                    for (int i = 0; i < nbItems; i++) {
                        allPrecedences[i] = new BitSet(nbItems);
                    }
                } else if (lineCounter == 1) {
                    cycleTime = Integer.parseInt(line);
                } else if (lineCounter > 1 && lineCounter <= nbItems + 1) {
                    taskTime[lineCounter - 2] = Integer.parseInt(line);
                } else {
                    String[] splitLine = line.split(",");
                    // Instance start counting indices at 1.
                    int before = Integer.parseInt(splitLine[0])-1;
                    int after = Integer.parseInt(splitLine[1])-1;
                    allPrecedences[after].set(before);
                }
                lineCounter++;
            }
        }

        Arrays.sort(taskTime, Comparator.reverseOrder());
        SALBProblem problem = new SALBProblem(nbItems, cycleTime, Arrays.stream(taskTime).mapToInt(i -> i).toArray(), allPrecedences, optimal);
        problem.setName(fileName);

        return problem;
    }
}