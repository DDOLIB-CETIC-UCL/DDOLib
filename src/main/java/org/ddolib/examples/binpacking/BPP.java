package org.ddolib.examples.binpacking;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

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
        BPPProblem problem = new BPPProblem(nbItems, binMaxSize, Arrays.stream(itemWeights).mapToInt(i -> i).toArray(), optimal);
        problem.setName(fileName);

        return problem;
    }

    public static void main(String[] args) throws IOException, InvalidSolutionException {
        final String file = args.length == 0 ? "all" : args[0];
        final int maxWidth = args.length >= 2 ? Integer.parseInt(args[1]) : 20;

        List<BPPProblem> problems = new ArrayList<>();

        if (file.equals("all")) {
            Path path = Path.of("data", "BPP");
            problems.addAll(generateProblems(path));
        } else {
            problems.add(extractFile(file));
        }

        problems.forEach(
                problem -> {
                    System.out.println(problem.name.get());
                    BPPDdoModel model = new BPPDdoModel(problem, maxWidth);

                    Solution bestSolution = Solvers.minimizeDdo(model, (sol, s) -> {
                        SolutionPrinter.printSolution(s, sol);
                    });
                    System.out.println(bestSolution);
                    if(problem.optimal.isPresent()){
                        System.out.printf("Found : %f \t Optimal : %f\n", bestSolution.value(), problem.optimal.get());
                    } else {
                        System.out.printf("Found : %f\n", bestSolution.value());
                    }
                }
        );

    }

    private static List<BPPProblem> generateProblems(Path dir) {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .sorted(Comparator.comparing(Path::toString))
                    .map(filePath -> {
                        try {
                            return BPP.extractFile(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
