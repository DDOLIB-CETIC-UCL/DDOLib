package org.ddolib.examples.binPacking;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BPPMain {


    public static void main(String[] args) throws IOException, InvalidSolutionException {

        BPPProblem problem = readInstance("data/BPP/Falkenauer_t60_01.txt");

        BPPDdoModel model = new BPPDdoModel(problem, 20);
        BPPAcsModel model2 = new BPPAcsModel(problem);

        Solution bestSolution = Solvers.minimizeDdo(model, (sol, s) -> {
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

    public static BPPProblem readInstance(String fileName) throws IOException {
        final File f = new File(fileName);
        int nbItems = 0;
        int binMaxSize = 0;
        Integer[] itemWeights = new Integer[1];
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
        BPPProblem problem = new BPPProblem(nbItems, binMaxSize, Arrays.stream(itemWeights).mapToInt(i -> i).toArray(), optimal);
        problem.setName(fileName);

        return problem;
    }
}
