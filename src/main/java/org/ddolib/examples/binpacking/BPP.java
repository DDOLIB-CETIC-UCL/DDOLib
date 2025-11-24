package org.ddolib.examples.binpacking;

import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

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
        Optional<Double> optimal = Optional.empty();
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = bf.readLine()) != null) {
                if (lineCounter == 0) {
                    String[] splitLine = line.split("\\s+");
                    if(splitLine.length > 1) {
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

        return new BPPProblem(nbItems, binMaxSize, Arrays.stream(itemWeights).mapToInt(i -> i).toArray(), optimal);
    }

    public static void main(String[] args) throws IOException {
        final String file = args.length == 0 ? "data/BPP/test.txt" : args[0];
        final int maxWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 250;

        BPPProblem problem = extractFile(file);
        BPPDdoModel model = new BPPDdoModel(problem,maxWidth);

        long start = System.currentTimeMillis();
        Solvers.minimizeDdo(model,(sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
            BPPSolution bppSolution = new BPPSolution(problem, sol);
            System.out.println(bppSolution);
            System.out.println((System.currentTimeMillis() - start) / 1000.0);
        });

    }

}
